package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import java.util.ArrayList;
import java.util.List;

public class Array {

    public static class ArraySettings{
        public boolean enabled = false;
        public BlockPos offset = BlockPos.ORIGIN;
        public int count = 5;

        public ArraySettings() {
        }

        public ArraySettings(boolean enabled, BlockPos offset, int count) {
            this.enabled = enabled;
            this.offset = offset;
            this.count = count;
        }

        public int getReach() {
            //find largest offset
            int x = Math.abs(offset.getX());
            int y = Math.abs(offset.getY());
            int z = Math.abs(offset.getZ());
            int largestOffset = Math.max(Math.max(x, y), z);

            return largestOffset * count;
        }
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        //find arraysettings for the player
        ArraySettings a = BuildSettingsManager.getBuildSettings(player).getArraySettings();
        if (!isEnabled(a)) return coordinates;

        BlockPos pos = startPos;
        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());

        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);
            coordinates.add(pos);
        }

        return coordinates;
    }

    public static List<IBlockState> findBlockStates(EntityPlayer player, BlockPos startPos, IBlockState blockState, ItemStack itemStack, List<ItemStack> itemStacks) {
        List<IBlockState> blockStates = new ArrayList<>();

        //find arraysettings for the player that placed the block
        ArraySettings a = BuildSettingsManager.getBuildSettings(player).getArraySettings();
        if (!isEnabled(a)) return blockStates;

        BlockPos pos = startPos;
        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(itemStack);
        }

        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);

            //Randomizer bag synergy
            if (bagInventory != null) {
                itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
                blockState = BuildModifiers
                        .getBlockStateFromItem(itemStack, player, startPos, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            }

            //blockState = blockState.getBlock().getStateForPlacement(player.world, pos, )
            blockStates.add(blockState);
            itemStacks.add(itemStack);
        }

        return blockStates;
    }

    public static boolean isEnabled(ArraySettings a) {
        if (a == null || !a.enabled) return false;

        if (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0) return false;

        return true;
    }

}
