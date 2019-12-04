package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.InventoryHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

import java.util.*;

public class BuildModifiers {

    //Called from BuildModes
    public static void onBlockPlaced(EntityPlayer player, List<BlockPos> startCoordinates, EnumFacing sideHit, Vec3d hitVec, boolean placeStartPos) {
        World world = player.world;
        ItemRandomizerBag.renewRandomness();

        //Format hitvec to 0.x
        hitVec = new Vec3d(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)), Math.abs(hitVec.z - ((int) hitVec.z)));

        //find coordinates and blockstates
        List<BlockPos> coordinates = findCoordinates(player, startCoordinates);
        List<ItemStack> itemStacks = new ArrayList<>();
        List<IBlockState> blockStates = findBlockStates(player, startCoordinates, hitVec, sideHit, itemStacks);

        //check if valid blockstates
        if (blockStates.size() == 0 || coordinates.size() != blockStates.size()) return;

        //remember previous blockstates for undo
        List<IBlockState> previousBlockStates = new ArrayList<>(coordinates.size());
        List<IBlockState> newBlockStates = new ArrayList<>(coordinates.size());
        for (BlockPos coordinate : coordinates) {
            previousBlockStates.add(world.getBlockState(coordinate));
        }

        if (world.isRemote) {

            BlockPreviewRenderer.onBlocksPlaced();

            newBlockStates = blockStates;

        } else {

            //place blocks
            for (int i = placeStartPos ? 0 : 1; i < coordinates.size(); i++) {
                BlockPos blockPos = coordinates.get(i);
                IBlockState blockState = blockStates.get(i);
                ItemStack itemStack = itemStacks.get(i);

                if (world.isBlockLoaded(blockPos, true)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        //try to find new stack, otherwise continue
                        itemStack = InventoryHelper.findItemStackInInventory(player, blockState.getBlock());
                        if (itemStack.isEmpty()) continue;
                    }
                    SurvivalHelper.placeBlock(world, player, blockPos, blockState, itemStack, EnumFacing.UP, hitVec, false, false, false);
                }
            }

            //find actual new blockstates for undo
            for (BlockPos coordinate : coordinates) {
                newBlockStates.add(world.getBlockState(coordinate));
            }
        }

        //Set first previousBlockState to empty if in NORMAL mode, to make undo/redo work
        //(Block is placed by the time it gets here, and unplaced after this)
        if (!placeStartPos) previousBlockStates.set(0, Blocks.AIR.getDefaultState());

        //If all new blockstates are air then no use in adding it, no block was actually placed
        //Can happen when e.g. placing one block in yourself
        if (Collections.frequency(newBlockStates, Blocks.AIR.getDefaultState()) != newBlockStates.size()) {
            //add to undo stack
            BlockPos firstPos = startCoordinates.get(0);
            BlockPos secondPos = startCoordinates.get(startCoordinates.size() - 1);
            UndoRedo.addUndo(player, new BlockSet(coordinates, previousBlockStates, newBlockStates, hitVec, firstPos, secondPos));
        }
    }

    public static void onBlockBroken(EntityPlayer player, List<BlockPos> startCoordinates, boolean breakStartPos) {
        World world = player.world;

        List<BlockPos> coordinates = findCoordinates(player, startCoordinates);

        if (coordinates.isEmpty()) return;

        //remember previous blockstates for undo
        List<IBlockState> previousBlockStates = new ArrayList<>(coordinates.size());
        List<IBlockState> newBlockStates = new ArrayList<>(coordinates.size());
        for (BlockPos coordinate : coordinates) {
            previousBlockStates.add(world.getBlockState(coordinate));
        }

        if (world.isRemote) {
            BlockPreviewRenderer.onBlocksBroken();

            //list of air blockstates
            for (int i = 0; i < coordinates.size(); i++) {
                newBlockStates.add(Blocks.AIR.getDefaultState());
            }

        } else {

            //If the player is going to instabreak grass or a plant, only break other instabreaking things
            boolean onlyInstaBreaking = !player.isCreative() &&
                                        world.getBlockState(startCoordinates.get(0)).getBlockHardness(world, startCoordinates.get(0)) == 0f;

            //break all those blocks
            for (int i = breakStartPos ? 0 : 1; i < coordinates.size(); i++) {
                BlockPos coordinate = coordinates.get(i);
                if (world.isBlockLoaded(coordinate, false)) {
                    if (!onlyInstaBreaking || world.getBlockState(coordinate).getBlockHardness(world, coordinate) == 0f) {
                        SurvivalHelper.breakBlock(world, player, coordinate, false);
                    }
                }
            }

            //find actual new blockstates for undo
            for (BlockPos coordinate : coordinates) {
                newBlockStates.add(world.getBlockState(coordinate));
            }
        }

        //Set first newBlockState to empty if in NORMAL mode, to make undo/redo work
        //(Block isn't broken yet by the time it gets here, and broken after this)
        if (!breakStartPos) newBlockStates.set(0, Blocks.AIR.getDefaultState());

        //add to undo stack
        BlockPos firstPos = startCoordinates.get(0);
        BlockPos secondPos = startCoordinates.get(startCoordinates.size() - 1);
        Vec3d hitVec = new Vec3d(0.5, 0.5, 0.5);
        UndoRedo.addUndo(player, new BlockSet(coordinates, previousBlockStates, newBlockStates, hitVec, firstPos, secondPos));

    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, List<BlockPos> posList) {
        List<BlockPos> coordinates = new ArrayList<>();
        //Add current blocks being placed too
        coordinates.addAll(posList);

        //Find mirror/array/radial mirror coordinates for each blockpos
        for (BlockPos blockPos : posList) {
            List<BlockPos> arrayCoordinates = Array.findCoordinates(player, blockPos);
            coordinates.addAll(arrayCoordinates);
            coordinates.addAll(Mirror.findCoordinates(player, blockPos));
            coordinates.addAll(RadialMirror.findCoordinates(player, blockPos));
            //get mirror for each array coordinate
            for (BlockPos coordinate : arrayCoordinates) {
                coordinates.addAll(Mirror.findCoordinates(player, coordinate));
                coordinates.addAll(RadialMirror.findCoordinates(player, coordinate));
            }
        }

        return coordinates;
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos) {
        return findCoordinates(player, new ArrayList<>(Arrays.asList(blockPos)));
    }

    public static List<IBlockState> findBlockStates(EntityPlayer player, List<BlockPos> posList, Vec3d hitVec, EnumFacing facing, List<ItemStack> itemStacks) {
        List<IBlockState> blockStates = new ArrayList<>();
        itemStacks.clear();

        //Get itemstack
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (itemStack.isEmpty() || !CompatHelper.isItemBlockProxy(itemStack)) {
            itemStack = player.getHeldItem(EnumHand.OFF_HAND);
        }
        if (itemStack.isEmpty() || !CompatHelper.isItemBlockProxy(itemStack)) {
            return blockStates;
        }

        //Get ItemBlock stack
        ItemStack itemBlock = ItemStack.EMPTY;
        if (itemStack.getItem() instanceof ItemBlock) itemBlock = itemStack;
        else itemBlock = CompatHelper.getItemBlockFromStack(itemStack);
        ItemRandomizerBag.resetRandomness();

        //Add blocks in posList first
        for (BlockPos blockPos : posList) {
            if (!(itemStack.getItem() instanceof ItemBlock)) itemBlock = CompatHelper.getItemBlockFromStack(itemStack);
            IBlockState blockState = getBlockStateFromItem(itemBlock, player, blockPos, facing, hitVec, EnumHand.MAIN_HAND);
            blockStates.add(blockState);
            itemStacks.add(itemBlock);
        }

        for (BlockPos blockPos : posList) {
            IBlockState blockState = getBlockStateFromItem(itemBlock, player, blockPos, facing, hitVec, EnumHand.MAIN_HAND);

            List<IBlockState> arrayBlockStates = Array.findBlockStates(player, blockPos, blockState, itemStack, itemStacks);
            blockStates.addAll(arrayBlockStates);
            blockStates.addAll(Mirror.findBlockStates(player, blockPos, blockState, itemStack, itemStacks));
            blockStates.addAll(RadialMirror.findBlockStates(player, blockPos, blockState, itemStack, itemStacks));
            //add mirror for each array coordinate
            List<BlockPos> arrayCoordinates = Array.findCoordinates(player, blockPos);
            for (int i = 0; i < arrayCoordinates.size(); i++) {
                BlockPos coordinate = arrayCoordinates.get(i);
                IBlockState blockState1 = arrayBlockStates.get(i);
                blockStates.addAll(Mirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
                blockStates.addAll(RadialMirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
            }

            //Adjust blockstates for torches and ladders etc to place on a valid side
            //TODO optimize findCoordinates (done twice now)
            //TODO fix mirror
//            List<BlockPos> coordinates = findCoordinates(player, startPos);
//            for (int i = 0; i < blockStates.size(); i++) {
//                blockStates.set(i, blockStates.get(i).getBlock().getStateForPlacement(player.world, coordinates.get(i), facing,
//                        (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, itemStacks.get(i).getMetadata(), player, EnumHand.MAIN_HAND));
//            }
        }

        return blockStates;
    }

    public static boolean isEnabled(ModifierSettingsManager.ModifierSettings modifierSettings, BlockPos startPos) {
        return Mirror.isEnabled(modifierSettings.getMirrorSettings(), startPos) ||
               Array.isEnabled(modifierSettings.getArraySettings()) ||
               RadialMirror.isEnabled(modifierSettings.getRadialMirrorSettings(), startPos) ||
               modifierSettings.doQuickReplace();
    }

    public static IBlockState getBlockStateFromItem(ItemStack itemStack, EntityPlayer player, BlockPos blockPos, EnumFacing facing, Vec3d hitVec, EnumHand hand) {
        return Block.getBlockFromItem(itemStack.getItem()).getStateForPlacement(new BlockItemUseContext(player.world, player, itemStack, blockPos, facing,
                ((float) hitVec.x), ((float) hitVec.y), ((float) hitVec.z)));
    }

    //Returns true if equal (or both null)
    public static boolean compareCoordinates(List<BlockPos> coordinates1, List<BlockPos> coordinates2) {
        if (coordinates1 == null && coordinates2 == null) return true;
        if (coordinates1 == null || coordinates2 == null) return false;

        //Check count, not actual values
        if (coordinates1.size() == coordinates2.size()){
            if (coordinates1.size() == 1){
                return coordinates1.get(0).equals(coordinates2.get(0));
            }
            return true;
        } else {
            return false;
        }

//        return coordinates1.equals(coordinates2);
    }
}
