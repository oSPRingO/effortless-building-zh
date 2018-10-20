package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

@Mod.EventBusSubscriber
public class Array {
    //TODO config file
    public static final int MAX_COUNT = 100;

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
    }

    //Called from EventHandler
    public static boolean onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return false;

        //find arraysettings for the player that placed the block
        ArraySettings a = BuildSettingsManager.getBuildSettings(event.getPlayer()).getArraySettings();
        if (a == null || !a.enabled) return false;

        if (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0) return false;

        BlockPos pos = event.getPos();
        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());

        //Randomizer bag synergy
        IItemHandler bagInventory = null;
        if (event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemRandomizerBag) {
            bagInventory = ItemRandomizerBag.getBagInventory(event.getPlayer().getHeldItemMainhand());
        }

        //Get itemstack
        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());

        for (int i = 0; i < a.count; i++) {
            pos = pos.add(offset);
            if (event.getWorld().isBlockLoaded(pos, true)) {
                if (itemStack.isEmpty()) break;

                IBlockState blockState = event.getPlacedBlock();

                //Randomizer bag synergy
                if (bagInventory != null) {
                    itemStack = ItemRandomizerBag.pickRandomStack(bagInventory);
                    if (itemStack.isEmpty()) continue;
                    blockState = getBlockStateFromRandomizerBag(bagInventory, event.getWorld(), event.getPlayer(), event.getPos(), itemStack);
                    if (blockState == null) continue;
                }

                //TODO check if can place (ItemBlock) and if can break replaced

                SurvivalHelper.placeBlock(event.getWorld(), event.getPlayer(), pos, blockState, itemStack, EnumFacing.NORTH, true, false);
            }
        }

        return true;
    }

    private static IBlockState getBlockStateFromRandomizerBag(IItemHandler bagInventory, World world, EntityPlayer player, BlockPos pos, ItemStack itemStack) {
        //TODO get facing from getPlacedAgainst and getPlacedBlock
        return Block.getBlockFromItem(itemStack.getItem()).getStateForPlacement(world, pos, EnumFacing.NORTH, 0, 0, 0, itemStack.getMetadata(), player, EnumHand.MAIN_HAND);
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
            SurvivalHelper.breakBlock(event.getWorld(), event.getPlayer(), pos);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRender(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        if (buildSettings == null) return;
        ArraySettings a = buildSettings.getArraySettings();

        if (a == null || !a.enabled || (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0)) return;

        RenderHelper.begin(event.getPartialTicks());

        //Render block outlines
        RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
        if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockPos = objectMouseOver.getBlockPos();
            if (!buildSettings.doQuickReplace()) blockPos = blockPos.offset(objectMouseOver.sideHit);

            drawBlockOutlines(a, blockPos);
        }

        RenderHelper.end();
    }

    @SideOnly(Side.CLIENT)
    public static void drawBlockOutlines(ArraySettings a, BlockPos pos) {
        if (a == null || !a.enabled || (a.offset.getX() == 0 && a.offset.getY() == 0 && a.offset.getZ() == 0)) return;

        Vec3i offset = new Vec3i(a.offset.getX(), a.offset.getY(), a.offset.getZ());

        //RenderHelper.renderBlockOutline(blockPos);
        for (int i = 0; i < a.count; i++)
        {
            pos = pos.add(offset);
            RenderHelper.renderBlockOutline(pos);
        }
    }
}
