package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

public class Array {
    //TODO config file
    public static final int MAX_COUNT = 100;

    public static class ArraySettings{
        public boolean enabled = false;
        public BlockPos offset = BlockPos.ORIGIN;
        public int count = 5;

        public ArraySettings(){
        }

        public ArraySettings(boolean enabled, BlockPos offset, int count) {
            this.enabled = enabled;
            this.offset = offset;
            this.count = count;
        }
    }

    //Called from EventHandler
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;

        //find arraysettings for the player that placed the block
        ArraySettings a = BuildSettingsManager.getBuildSettings(event.getPlayer()).getArraySettings();
        if (a == null || !a.enabled) return;

        if (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0) return;

        BlockPos pos = event.getPos();
        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(event.getPlayer().getHeldItemMainhand());
        }

        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);
            if (event.getWorld().isBlockLoaded(pos, true)) {
                IBlockState blockState = bagInventory == null ? event.getPlacedBlock() :
                        getBlockStateFromRandomizerBag(bagInventory, event.getWorld(), event.getPlayer(), event.getPos());
                event.getWorld().setBlockState(pos, blockState);
            }
        }
    }

    private static IBlockState getBlockStateFromRandomizerBag(IItemHandler bagInventory, World world, EntityPlayer player, BlockPos pos) {
        int randomSlot = ItemRandomizerBag.pickRandomSlot(bagInventory);
        ItemStack stack = bagInventory.getStackInSlot(randomSlot);
        //TODO get facing from getPlacedAgainst and getPlacedBlock
        return Block.getBlockFromItem(stack.getItem()).getStateForPlacement(world, pos, EnumFacing.NORTH, 0, 0, 0, stack.getMetadata(), player, EnumHand.MAIN_HAND);
    }

    //Called from EventHandler
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;

        //find arraysettings for the player that placed the block
        ArraySettings a = BuildSettingsManager.getBuildSettings(event.getPlayer()).getArraySettings();
        if (a == null || !a.enabled) return;

        if (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0) return;

        BlockPos pos = event.getPos();
        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());
        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);
            if (event.getWorld().isBlockLoaded(pos, false)) {
                event.getWorld().setBlockToAir(pos);
            }
        }
    }
}
