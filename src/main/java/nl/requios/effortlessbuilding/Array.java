package nl.requios.effortlessbuilding;

import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;

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
        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);
            if (event.getWorld().isBlockLoaded(pos, true)) {
                event.getWorld().setBlockState(pos, event.getPlacedBlock());

                //Mirror synergy
                BlockSnapshot blockSnapshot = new BlockSnapshot(event.getWorld(), pos, event.getPlacedBlock());
                BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(blockSnapshot, event.getPlacedBlock(), event.getPlayer(), EnumHand.MAIN_HAND);
                Mirror.onBlockPlaced(placeEvent);
            }
        }
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

                //Mirror synergy
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(event.getWorld(), pos, event.getState(), event.getPlayer());
                Mirror.onBlockBroken(breakEvent);
            }
        }
    }

//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void onRender(RenderWorldLastEvent event) {
//
//    }
}
