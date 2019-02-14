package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildModifiers {

    //Called from BuildModes
    public static void onBlockPlaced(EntityPlayer player, List<BlockPos> startCoordinates, EnumFacing sideHit, Vec3d hitVec) {
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

        if (world.isRemote) {
            BlockPreviewRenderer.onBlocksPlaced();
            return;
        }

        //place blocks
        for (int i = 0; i < coordinates.size(); i++) {
            BlockPos blockPos = coordinates.get(i);
            IBlockState blockState = blockStates.get(i);
            ItemStack itemStack = itemStacks.get(i);

            if (world.isBlockLoaded(blockPos, true)) {
                //check itemstack empty
                if (itemStack.isEmpty()) continue;
                SurvivalHelper.placeBlock(world, player, blockPos, blockState, itemStack, EnumFacing.UP, hitVec, false, false);
            }
        }

    }

    public static void onBlockBroken(EntityPlayer player, List<BlockPos> posList) {
        World world = player.world;

        List<BlockPos> coordinates = findCoordinates(player, posList);

        if (coordinates.isEmpty()) return;

        if (world.isRemote) {
            BlockPreviewRenderer.onBlocksBroken();
            return;
        }

        //If the player is going to instabreak grass or a plant, only break other instabreaking things
        boolean onlyInstaBreaking = world.getBlockState(posList.get(0)).getBlockHardness(world, posList.get(0)) == 0f;

        //break all those blocks
        for (BlockPos coordinate : coordinates) {
            if (world.isBlockLoaded(coordinate, false)) {
                if (!onlyInstaBreaking || world.getBlockState(coordinate).getBlockHardness(world, coordinate) == 0f) {
                    SurvivalHelper.breakBlock(world, player, coordinate);
                }
            }
        }
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
        return Block.getBlockFromItem(itemStack.getItem()).getStateForPlacement(player.world, blockPos, facing,
                ((float) hitVec.x), ((float) hitVec.y), ((float) hitVec.z), itemStack.getMetadata(), player, hand);
    }

    //Returns true if equal (or both null)
    public static boolean compareCoordinates(List<BlockPos> coordinates1, List<BlockPos> coordinates2) {
        if (coordinates1 == null && coordinates2 == null) return true;
        if (coordinates1 == null || coordinates2 == null) return false;

        return coordinates1.equals(coordinates2);
    }
}
