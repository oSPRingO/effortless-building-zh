package nl.requios.effortlessbuilding;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

public class Mirror {

    //TODO config file
    public static final int MAX_RADIUS = 200;

    public static class MirrorSettings {
        public boolean enabled = false;
        public Vec3d position = new Vec3d(0.5, 64.5, 0.5);
        public boolean mirrorX = true, mirrorY = false, mirrorZ = false;
        public int radius = 20;
        public boolean drawLines = true, drawPlanes = false;

        public MirrorSettings() {
        }

        public MirrorSettings(boolean mirrorEnabled, Vec3d position, boolean mirrorX, boolean mirrorY, boolean mirrorZ, int radius, boolean drawLines, boolean drawPlanes) {
            this.enabled = mirrorEnabled;
            this.position = position;
            this.mirrorX = mirrorX;
            this.mirrorY = mirrorY;
            this.mirrorZ = mirrorZ;
            this.radius = radius;
            this.drawLines = drawLines;
            this.drawPlanes = drawPlanes;
        }
    }

    //Called from EventHandler
    public static boolean onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return false;

        //find mirrorsettings for the player that placed the block
        MirrorSettings m = BuildSettingsManager.getBuildSettings(event.getPlayer()).getMirrorSettings();
        if (m == null) return false;

        if (!m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return false;

        //if within mirror distance, mirror
        BlockPos oldBlockPos = event.getPos();

        if (oldBlockPos.getX() + 0.5 < m.position.x - m.radius || oldBlockPos.getX() + 0.5 > m.position.x + m.radius ||
                oldBlockPos.getY() + 0.5 < m.position.y - m.radius || oldBlockPos.getY() + 0.5 > m.position.y + m.radius ||
                oldBlockPos.getZ() + 0.5 < m.position.z - m.radius || oldBlockPos.getZ() + 0.5 > m.position.z + m.radius)
            return false;

        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (event.getPlayer().getHeldItem(event.getHand()).getItem() == EffortlessBuilding.ITEM_RANDOMIZER_BAG) {
            bagInventory = ItemRandomizerBag.getBagInventory(event.getPlayer().getHeldItem(EnumHand.MAIN_HAND));
        }

        if (m.mirrorX) {
            placeMirrorX(event.getWorld(), event.getPlayer(), m, event.getPos(), event.getPlacedBlock(), bagInventory, itemStack);
        }

        if (m.mirrorY) {
            placeMirrorY(event.getWorld(), event.getPlayer(), m, oldBlockPos, event.getPlacedBlock(), bagInventory, itemStack);
        }

        if (m.mirrorZ) {
            placeMirrorZ(event.getWorld(), event.getPlayer(), m, oldBlockPos, event.getPlacedBlock(), bagInventory, itemStack);
        }

        return true;
    }

    private static void placeMirrorX(World world, EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState, IItemHandler bagInventory, ItemStack itemStack) {
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            if (itemStack.isEmpty()) return;
            oldBlockState = getBlockStateFromRandomizerBag(bagInventory, world, player, oldBlockPos, itemStack);
            if (oldBlockState == null) return;
        }

        IBlockState newBlockState = oldBlockState.withMirror(net.minecraft.util.Mirror.FRONT_BACK);
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            placeBlock(world, player, newBlockPos, newBlockState, itemStack);
        }
        if (m.mirrorY) placeMirrorY(world, player, m, newBlockPos, newBlockState, bagInventory, itemStack);
        if (m.mirrorZ) placeMirrorZ(world, player, m, newBlockPos, newBlockState, bagInventory, itemStack);
    }

    private static void placeMirrorY(World world, EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState, IItemHandler bagInventory, ItemStack itemStack) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            if (itemStack.isEmpty()) return;
            oldBlockState = getBlockStateFromRandomizerBag(bagInventory, world, player, oldBlockPos, itemStack);
            if (oldBlockState == null) return;
        }

        IBlockState newBlockState = getVerticalMirror(oldBlockState);
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            placeBlock(world, player, newBlockPos, newBlockState, itemStack);
        }
        if (m.mirrorZ) placeMirrorZ(world, player, m, newBlockPos, newBlockState, bagInventory, itemStack);
    }

    private static void placeMirrorZ(World world, EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState, IItemHandler bagInventory, ItemStack itemStack) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            if (itemStack.isEmpty()) return;
            oldBlockState = getBlockStateFromRandomizerBag(bagInventory, world, player, oldBlockPos, itemStack);
            if (oldBlockState == null) return;
        }

        IBlockState newBlockState = oldBlockState.withMirror(net.minecraft.util.Mirror.LEFT_RIGHT);
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            placeBlock(world, player, newBlockPos, newBlockState, itemStack);
        }
    }

    private static IBlockState getBlockStateFromRandomizerBag(IItemHandler bagInventory, World world, EntityPlayer player, BlockPos pos, ItemStack itemStack) {
        //TODO get facing from getPlacedAgainst and getPlacedBlock
        return Block.getBlockFromItem(itemStack.getItem()).getStateForPlacement(world, pos, EnumFacing.NORTH, 0, 0, 0, itemStack.getMetadata(), player, EnumHand.MAIN_HAND);
    }

    private static void placeBlock(World world, EntityPlayer player, BlockPos newBlockPos, IBlockState newBlockState, ItemStack itemStack) {
        //TODO check if can place
        //TODO check if can break

        SurvivalHelper.placeBlock(world, player, newBlockPos, newBlockState, itemStack, EnumFacing.NORTH, true, false);

        //Array synergy
        BlockSnapshot blockSnapshot = new BlockSnapshot(world, newBlockPos, newBlockState);
        BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(blockSnapshot, newBlockState, player, EnumHand.MAIN_HAND);
        Array.onBlockPlaced(placeEvent);
    }

    private static IBlockState getVerticalMirror(IBlockState blockState) {
        //Stairs
        if (blockState.getBlock() instanceof BlockStairs) {
            if (blockState.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM) {
                return blockState.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
            } else {
                return blockState.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM);
            }
        }

        //Slabs
        if (blockState.getBlock() instanceof BlockSlab) {
            if (((BlockSlab) blockState.getBlock()).isDouble()) return blockState;
            if (blockState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM) {
                return blockState.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
            } else {
                return blockState.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
            }
        }

        //Buttons, endrod, observer, piston
        if (blockState.getBlock() instanceof BlockDirectional) {
            if (blockState.getValue(BlockDirectional.FACING) == EnumFacing.DOWN) {
                return blockState.withProperty(BlockDirectional.FACING, EnumFacing.UP);
            } else if (blockState.getValue(BlockDirectional.FACING) == EnumFacing.UP) {
                return blockState.withProperty(BlockDirectional.FACING, EnumFacing.DOWN);
            }
        }

        //Dispenser, dropper
        if (blockState.getBlock() instanceof BlockDispenser) {
            if (blockState.getValue(BlockDispenser.FACING) == EnumFacing.DOWN) {
                return blockState.withProperty(BlockDispenser.FACING, EnumFacing.UP);
            } else if (blockState.getValue(BlockDispenser.FACING) == EnumFacing.UP) {
                return blockState.withProperty(BlockDispenser.FACING, EnumFacing.DOWN);
            }
        }

        return blockState;
    }

    //Called from EventHandler
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;

        //find mirrorsettings for the player that broke the block
        MirrorSettings m = BuildSettingsManager.getBuildSettings(event.getPlayer()).getMirrorSettings();
        if (m == null) return;

        if (!m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return;

        //if within mirror distance, break mirror block
        BlockPos oldBlockPos = event.getPos();

        if (oldBlockPos.getX() + 0.5 < m.position.x - m.radius || oldBlockPos.getX() + 0.5 > m.position.x + m.radius ||
                oldBlockPos.getY() + 0.5 < m.position.y - m.radius || oldBlockPos.getY() + 0.5 > m.position.y + m.radius ||
                oldBlockPos.getZ() + 0.5 < m.position.z - m.radius || oldBlockPos.getZ() + 0.5 > m.position.z + m.radius)
            return;

        if (m.mirrorX) {
            breakMirrorX(event, m, oldBlockPos);
        }

        if (m.mirrorY) {
            breakMirrorY(event, m, oldBlockPos);
        }

        if (m.mirrorZ) {
            breakMirrorZ(event, m, oldBlockPos);
        }
    }

    private static void breakMirrorX(BlockEvent.BreakEvent event, MirrorSettings m, BlockPos oldBlockPos) {
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());
        //break block
        breakBlock(event, newBlockPos);
        if (m.mirrorY) breakMirrorY(event, m, newBlockPos);
        if (m.mirrorZ) breakMirrorZ(event, m, newBlockPos);
    }

    private static void breakMirrorY(BlockEvent.BreakEvent event, MirrorSettings m, BlockPos oldBlockPos) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());
        //break block
        breakBlock(event, newBlockPos);
        if (m.mirrorZ) breakMirrorZ(event, m, newBlockPos);
    }

    private static void breakMirrorZ(BlockEvent.BreakEvent event, MirrorSettings m, BlockPos oldBlockPos) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);
        //break block
        breakBlock(event, newBlockPos);
    }

    private static void breakBlock(BlockEvent.BreakEvent event, BlockPos newBlockPos) {
        if (!event.getWorld().isBlockLoaded(newBlockPos, false)) return;

        SurvivalHelper.breakBlock(event.getWorld(), event.getPlayer(), newBlockPos);

        //Array synergy
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(event.getWorld(), newBlockPos, event.getWorld().getBlockState(newBlockPos), event.getPlayer());
        Array.onBlockBroken(breakEvent);
    }

}
