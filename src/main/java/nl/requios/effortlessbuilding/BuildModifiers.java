package nl.requios.effortlessbuilding;

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
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.network.BlockBrokenMessage;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;

import java.util.ArrayList;
import java.util.List;

public class BuildModifiers {

    //Uses a network message to get the previous raytraceresult from the player
    //The server could keep track of all raytraceresults but this might lag with many players
    //Raytraceresult is needed for sideHit and hitVec
    public static void onBlockPlacedMessage(EntityPlayer player, BlockPlacedMessage message) {
        if (!message.isBlockHit() || message.getBlockPos() == null) return;

        World world = player.world;
        ItemRandomizerBag.renewRandomness();

        BlockPos startPos = message.getBlockPos();

        //Offset in direction of sidehit if not quickreplace and not replaceable
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        boolean replaceable = world.getBlockState(startPos).getBlock().isReplaceable(player.world, startPos);
        if (!buildSettings.doQuickReplace() && !replaceable) {
            startPos = startPos.offset(message.getSideHit());
        }

        //Get under tall grass and other replaceable blocks
        if (buildSettings.doQuickReplace() && replaceable) {
            startPos = startPos.down();
        }

        //Check if player reach does not exceed startpos
        int maxReach = ReachHelper.getMaxReach(player);
        if (player.getPosition().distanceSq(startPos) > maxReach * maxReach) {
            EffortlessBuilding.log(player, "Placement exceeds your reach.");
            return;
        }

        //Format hitvec to 0.x
        Vec3d hitVec = message.getHitVec();
        hitVec = new Vec3d(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)), Math.abs(hitVec.z - ((int) hitVec.z)));

        //find coordinates and blockstates
        List<BlockPos> coordinates = findCoordinates(player, startPos);
        List<ItemStack> itemStacks = new ArrayList<>();
        List<IBlockState> blockStates = findBlockStates(player, startPos, hitVec, message.getSideHit(), itemStacks);

        //check if valid blockstates
        if (blockStates.size() == 0 || coordinates.size() != blockStates.size()) return;

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

    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(event.getPlayer());
        //Only use own place event if anything is enabled
        if (isEnabled(buildSettings, event.getPos())) {
            //EffortlessBuilding.packetHandler.sendTo(new BlockPlacedMessage(), (EntityPlayerMP) event.getPlayer());
            event.setCanceled(true);
        }

    }

    //Use a network message to break blocks in the distance using clientside mouse input
    public static void onBlockBrokenMessage(EntityPlayer player, BlockBrokenMessage message) {
        BlockPos blockPos = message.getBlockPos();
        if (ReachHelper.canBreakFar(player) && message.isBlockHit()) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(player.world, blockPos, player.world.getBlockState(blockPos), player);
            onBlockBroken(event);
        }
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        if (world.isRemote) return;

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(event.getPlayer());
        //Only use own break event if anything is enabled
        if (isEnabled(buildSettings, pos)) {
            //get coordinates
            List<BlockPos> coordinates = findCoordinates(event.getPlayer(), pos);

            //If the player is going to instabreak grass or a plant, only break other instabreaking things
            boolean onlyInstaBreaking = world.getBlockState(pos).getBlockHardness(
                    world, pos) == 0f;

            //break all those blocks
            for (BlockPos coordinate : coordinates) {
                if (world.isBlockLoaded(coordinate, false)) {
                    if (!onlyInstaBreaking || world.getBlockState(coordinate).getBlockHardness(world, coordinate) == 0f) {
                        SurvivalHelper.breakBlock(world, event.getPlayer(), coordinate);
                    }
                }
            }
        }
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();
        //Add current block being placed too
        coordinates.add(startPos);

        List<BlockPos> arrayCoordinates = Array.findCoordinates(player, startPos);
        coordinates.addAll(arrayCoordinates);
        coordinates.addAll(Mirror.findCoordinates(player, startPos));
        coordinates.addAll(RadialMirror.findCoordinates(player, startPos));
        //get mirror for each array coordinate
        for (BlockPos coordinate : arrayCoordinates) {
            coordinates.addAll(Mirror.findCoordinates(player, coordinate));
            coordinates.addAll(RadialMirror.findCoordinates(player, coordinate));
        }

        return coordinates;
    }

    public static List<IBlockState> findBlockStates(EntityPlayer player, BlockPos startPos, Vec3d hitVec, EnumFacing facing, List<ItemStack> itemStacks) {
        List<IBlockState> blockStates = new ArrayList<>();
        itemStacks.clear();

        //Get itemstack
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemBlock || itemStack.getItem() instanceof ItemRandomizerBag)) {
            itemStack = player.getHeldItem(EnumHand.OFF_HAND);
        }
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemBlock || itemStack.getItem() instanceof ItemRandomizerBag)) {
            return blockStates;
        }

        //Get ItemBlock stack
        ItemStack itemBlock = ItemStack.EMPTY;
        if (itemStack.getItem() instanceof ItemBlock) itemBlock = itemStack;
        ItemRandomizerBag.resetRandomness();
        if (itemStack.getItem() instanceof ItemRandomizerBag) itemBlock = ItemRandomizerBag.pickRandomStack(ItemRandomizerBag.getBagInventory(itemStack));

        IBlockState blockState = getBlockStateFromItem(itemBlock, player, startPos, facing, hitVec, EnumHand.MAIN_HAND);
        //Add current block being placed too
        blockStates.add(blockState);
        itemStacks.add(itemStack);

        List<IBlockState> arrayBlockStates = Array.findBlockStates(player, startPos, blockState, itemStack, itemStacks);
        blockStates.addAll(arrayBlockStates);
        blockStates.addAll(Mirror.findBlockStates(player, startPos, blockState, itemStack, itemStacks));
        blockStates.addAll(RadialMirror.findBlockStates(player, startPos, blockState, itemStack, itemStacks));
        //add mirror for each array coordinate
        List<BlockPos> arrayCoordinates = Array.findCoordinates(player, startPos);
        for (int i = 0; i < arrayCoordinates.size(); i++) {
            BlockPos coordinate = arrayCoordinates.get(i);
            IBlockState blockState1 = arrayBlockStates.get(i);
            blockStates.addAll(Mirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
            blockStates.addAll(RadialMirror.findBlockStates(player, coordinate, blockState1, itemStack, itemStacks));
        }

        //Adjust blockstates for torches and ladders etc to place on a valid side
        //TODO optimize findCoordinates (done twice now)
        //TODO fix mirror
//        List<BlockPos> coordinates = findCoordinates(player, startPos);
//        for (int i = 0; i < blockStates.size(); i++) {
//            blockStates.set(i, blockStates.get(i).getBlock().getStateForPlacement(player.world, coordinates.get(i), facing,
//                    (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, itemStacks.get(i).getMetadata(), player, EnumHand.MAIN_HAND));
//        }

        return blockStates;
    }

    public static boolean isEnabled(BuildSettingsManager.BuildSettings buildSettings, BlockPos startPos) {
        return Mirror.isEnabled(buildSettings.getMirrorSettings(), startPos) ||
               Array.isEnabled(buildSettings.getArraySettings()) ||
               RadialMirror.isEnabled(buildSettings.getRadialMirrorSettings(), startPos) ||
               buildSettings.doQuickReplace();
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
