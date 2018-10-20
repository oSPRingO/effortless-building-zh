package nl.requios.effortlessbuilding;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

@Mod.EventBusSubscriber
public class Mirror {

    //TODO config file
    public static final int MAX_RADIUS = 200;

    private static final Color colorX = new Color(255, 72, 52);
    private static final Color colorY = new Color(67, 204, 51);
    private static final Color colorZ = new Color(52, 247, 255);
    private static final int lineAlpha = 200;
    private static final int planeAlpha = 75;
    private static final Vec3d epsilon = new Vec3d(0.001, 0.001, 0.001); //prevents z-fighting


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
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(event.getWorld(), newBlockPos, event.getState(), event.getPlayer());
        Array.onBlockBroken(breakEvent);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRender(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        if (buildSettings == null) return;
        MirrorSettings m = buildSettings.getMirrorSettings();

        if (m == null || !m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return;

        RenderHelper.begin(event.getPartialTicks());

        Vec3d pos = m.position.add(epsilon);
        int radius = m.radius;

        if (m.mirrorX) {
            Vec3d posA = new Vec3d(pos.x, pos.y - radius, pos.z - radius);
            Vec3d posB = new Vec3d(pos.x, pos.y + radius, pos.z + radius);

            drawMirrorPlane(posA, posB, colorX, m.drawLines, m.drawPlanes);
        }
        if (m.mirrorY) {
            Vec3d posA = new Vec3d(pos.x - radius, pos.y, pos.z - radius);
            Vec3d posB = new Vec3d(pos.x + radius, pos.y, pos.z + radius);

            drawMirrorPlaneY(posA, posB, colorY, m.drawLines, m.drawPlanes);
        }
        if (m.mirrorZ) {
            Vec3d posA = new Vec3d(pos.x - radius, pos.y - radius, pos.z);
            Vec3d posB = new Vec3d(pos.x + radius, pos.y + radius, pos.z);

            drawMirrorPlane(posA, posB, colorZ, m.drawLines, m.drawPlanes);
        }

        //Draw axis coordinated colors if two or more axes are enabled
        //(If only one is enabled the lines are that planes color)
        if (m.drawLines && ((m.mirrorX && m.mirrorY) || (m.mirrorX && m.mirrorZ) || (m.mirrorY && m.mirrorZ))) {
            drawMirrorLines(m);
        }

        //Render block outlines
        RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
        if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockPos = objectMouseOver.getBlockPos();
            if (!buildSettings.doQuickReplace()) blockPos = blockPos.offset(objectMouseOver.sideHit);

            //RenderHelper.renderBlockOutline(blockPos);
            if (m.mirrorX) drawBlockOutlineX(buildSettings, blockPos);
            if (m.mirrorY) drawBlockOutlineY(buildSettings, blockPos);
            if (m.mirrorZ) drawBlockOutlineZ(buildSettings, blockPos);
        }

        RenderHelper.end();
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorPlane(Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes) {

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (drawPlanes) {
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posA.x, posB.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posB.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            tessellator.draw();
        }

        if (drawLines) {
            Vec3d middle = posA.add(posB).scale(0.5);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, middle.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posB.x, middle.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(middle.x, posA.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(middle.x, posB.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();

            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorPlaneY(Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes) {

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        if (drawPlanes) {
            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posA.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
            bufferBuilder.pos(posB.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

            tessellator.draw();
        }

        if (drawLines) {
            Vec3d middle = posA.add(posB).scale(0.5);
            bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            bufferBuilder.pos(middle.x, middle.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(middle.x, middle.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posA.x, middle.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();
            bufferBuilder.pos(posB.x, middle.y, middle.z).color(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha).endVertex();

            tessellator.draw();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorLines(MirrorSettings m) {

        Vec3d pos = m.position.add(epsilon);

        GL11.glColor4d(100, 100, 100, 255);
        GL11.glLineWidth(2);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        bufferBuilder.pos(pos.x - m.radius, pos.y, pos.z).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x + m.radius, pos.y, pos.z).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y - m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y + m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z - m.radius).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z + m.radius).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();

        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public static void drawBlockOutlineX(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        Array.drawBlockOutlines(buildSettings.getArraySettings(), newBlockPos);

        if (m.mirrorY) drawBlockOutlineY(buildSettings, newBlockPos);
        if (m.mirrorZ) drawBlockOutlineZ(buildSettings, newBlockPos);
    }

    @SideOnly(Side.CLIENT)
    public static void drawBlockOutlineY(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        Array.drawBlockOutlines(buildSettings.getArraySettings(), newBlockPos);

        if (m.mirrorZ) drawBlockOutlineZ(buildSettings, newBlockPos);
    }

    @SideOnly(Side.CLIENT)
    public static void drawBlockOutlineZ(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        Array.drawBlockOutlines(buildSettings.getArraySettings(), newBlockPos);
    }

}
