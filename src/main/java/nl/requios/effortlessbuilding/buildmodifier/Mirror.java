package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import java.util.ArrayList;
import java.util.List;

public class Mirror {

    public static class MirrorSettings {
        public boolean enabled = false;
        public Vec3d position = new Vec3d(0.5, 64.5, 0.5);
        public boolean mirrorX = true, mirrorY = false, mirrorZ = false;
        public int radius = 10;
        public boolean drawLines = true, drawPlanes = true;

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

        public int getReach() {
            return radius * 2;
        }
    }
    
    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        //find mirrorsettings for the player
        MirrorSettings m = BuildSettingsManager.getBuildSettings(player).getMirrorSettings();
        if (!isEnabled(m, startPos)) return coordinates;

        if (m.mirrorX) coordinateMirrorX(m, startPos, coordinates);
        if (m.mirrorY) coordinateMirrorY(m, startPos, coordinates);
        if (m.mirrorZ) coordinateMirrorZ(m, startPos, coordinates);

        return coordinates;
    }

    private static void coordinateMirrorX(MirrorSettings m, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());
        coordinates.add(newBlockPos);

        if (m.mirrorY) coordinateMirrorY(m, newBlockPos, coordinates);
        if (m.mirrorZ) coordinateMirrorZ(m, newBlockPos, coordinates);
    }

    private static void coordinateMirrorY(MirrorSettings m, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());
        coordinates.add(newBlockPos);

        if (m.mirrorZ) coordinateMirrorZ(m, newBlockPos, coordinates);
    }

    private static void coordinateMirrorZ(MirrorSettings m, BlockPos oldBlockPos, List<BlockPos> coordinates) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);
        coordinates.add(newBlockPos);
    }

    public static List<IBlockState> findBlockStates(EntityPlayer player, BlockPos startPos, IBlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        List<IBlockState> blockStates = new ArrayList<>();

        //find mirrorsettings for the player
        MirrorSettings m = BuildSettingsManager.getBuildSettings(player).getMirrorSettings();
        if (!isEnabled(m, startPos)) return blockStates;

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(itemStack);
        }

        if (m.mirrorX) blockStateMirrorX(player, m, startPos, blockState, bagInventory, itemStack, EnumHand.MAIN_HAND, blockStates, itemStacks);
        if (m.mirrorY) blockStateMirrorY(player, m, startPos, blockState, bagInventory, itemStack, EnumHand.MAIN_HAND, blockStates, itemStacks);
        if (m.mirrorZ) blockStateMirrorZ(player, m, startPos, blockState, bagInventory, itemStack, EnumHand.MAIN_HAND, blockStates, itemStacks);

        return blockStates;
    }

    private static void blockStateMirrorX(EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState,
                                          IItemHandler bagInventory, ItemStack itemStack, EnumHand hand, List<IBlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            oldBlockState = BuildModifiers
                    .getBlockStateFromItem(itemStack, player, oldBlockPos, EnumFacing.UP, new Vec3d(0, 0, 0), hand);
        }

        //Find blockstate
        IBlockState newBlockState = oldBlockState == null ? null : oldBlockState.withMirror(net.minecraft.util.Mirror.FRONT_BACK);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);

        if (m.mirrorY) blockStateMirrorY(player, m, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
        if (m.mirrorZ) blockStateMirrorZ(player, m, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
    }

    private static void blockStateMirrorY(EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState,
                                          IItemHandler bagInventory, ItemStack itemStack, EnumHand hand, List<IBlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            oldBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, oldBlockPos, EnumFacing.UP, new Vec3d(0, 0, 0), hand);
        }

        //Find blockstate
        IBlockState newBlockState = oldBlockState == null ? null : getVerticalMirror(oldBlockState);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);

        if (m.mirrorZ) blockStateMirrorZ(player, m, newBlockPos, newBlockState, bagInventory, itemStack, hand, blockStates, itemStacks);
    }

    private static void blockStateMirrorZ(EntityPlayer player, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState,
                                          IItemHandler bagInventory, ItemStack itemStack, EnumHand hand, List<IBlockState> blockStates, List<ItemStack> itemStacks) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);

        //Randomizer bag synergy
        if (bagInventory != null) {
            itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
            oldBlockState = BuildModifiers.getBlockStateFromItem(itemStack, player, oldBlockPos, EnumFacing.UP, new Vec3d(0, 0, 0), hand);
        }

        //Find blockstate
        IBlockState newBlockState = oldBlockState == null ? null : oldBlockState.withMirror(net.minecraft.util.Mirror.LEFT_RIGHT);

        //Store blockstate and itemstack
        blockStates.add(newBlockState);
        itemStacks.add(itemStack);
    }

    public static boolean isEnabled(MirrorSettings m, BlockPos startPos) {
        if (m == null || !m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return false;

        //within mirror distance
        if (startPos.getX() + 0.5 < m.position.x - m.radius || startPos.getX() + 0.5 > m.position.x + m.radius ||
            startPos.getY() + 0.5 < m.position.y - m.radius || startPos.getY() + 0.5 > m.position.y + m.radius ||
            startPos.getZ() + 0.5 < m.position.z - m.radius || startPos.getZ() + 0.5 > m.position.z + m.radius)
            return false;

        return true;
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
}
