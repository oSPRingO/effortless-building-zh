package nl.requios.effortlessbuilding;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;

        //find mirrorsettings for the player that placed the block
        MirrorSettings m = BuildSettingsManager.getBuildSettings(event.getPlayer()).getMirrorSettings();
        if (m == null) return;

        if (!m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return;

        //if within mirror distance, mirror
        BlockPos oldBlockPos = event.getPos();

        if (oldBlockPos.getX() + 0.5 < m.position.x - m.radius || oldBlockPos.getX() + 0.5 > m.position.x + m.radius ||
                oldBlockPos.getY() + 0.5 < m.position.y - m.radius || oldBlockPos.getY() + 0.5 > m.position.y + m.radius ||
                oldBlockPos.getZ() + 0.5 < m.position.z - m.radius || oldBlockPos.getZ() + 0.5 > m.position.z+ m.radius)
            return;

        if (m.mirrorX) {
            placeMirrorX(event.getWorld(), m, oldBlockPos, event.getPlacedBlock());
        }

        if (m.mirrorY) {
            placeMirrorY(event.getWorld(), m, oldBlockPos, event.getPlacedBlock());
        }

        if (m.mirrorZ) {
            placeMirrorZ(event.getWorld(), m, oldBlockPos, event.getPlacedBlock());
        }
    }

    private static void placeMirrorX(World world, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState) {
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());
        IBlockState newBlockState = oldBlockState;
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            newBlockState = oldBlockState.withMirror(net.minecraft.util.Mirror.FRONT_BACK);

            world.setBlockState(newBlockPos, newBlockState);
        }
        if (m.mirrorY) placeMirrorY(world, m, newBlockPos, newBlockState);
        if (m.mirrorZ) placeMirrorZ(world, m, newBlockPos, newBlockState);
    }

    private static void placeMirrorY(World world, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());
        IBlockState newBlockState = oldBlockState;
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            newBlockState = getVerticalMirror(oldBlockState);

            world.setBlockState(newBlockPos, newBlockState);
        }
        if (m.mirrorZ) placeMirrorZ(world, m, newBlockPos, newBlockState);
    }

    private static void placeMirrorZ(World world, MirrorSettings m, BlockPos oldBlockPos, IBlockState oldBlockState) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);
        IBlockState newBlockState = oldBlockState;
        //place block
        if (world.isBlockLoaded(newBlockPos, true)) {
            newBlockState = oldBlockState.withMirror(net.minecraft.util.Mirror.LEFT_RIGHT);

            world.setBlockState(newBlockPos, newBlockState);
        }
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
                oldBlockPos.getZ() + 0.5 < m.position.z - m.radius || oldBlockPos.getZ() + 0.5 > m.position.z+ m.radius)
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
        if (event.getWorld().isBlockLoaded(newBlockPos, true)) {
            event.getWorld().setBlockToAir(newBlockPos);
        }
        if (m.mirrorY) breakMirrorY(event, m, newBlockPos);
        if (m.mirrorZ) breakMirrorZ(event, m, newBlockPos);
    }

    private static void breakMirrorY(BlockEvent.BreakEvent event, MirrorSettings m, BlockPos oldBlockPos) {
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());
        //place block
        if (event.getWorld().isBlockLoaded(newBlockPos, true)) {
            event.getWorld().setBlockToAir(newBlockPos);
        }
        if (m.mirrorZ) breakMirrorZ(event, m, newBlockPos);
    }

    private static void breakMirrorZ(BlockEvent.BreakEvent event, MirrorSettings m, BlockPos oldBlockPos) {
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);
        //place block
        if (event.getWorld().isBlockLoaded(newBlockPos, true)) {
            event.getWorld().setBlockToAir(newBlockPos);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRender(RenderWorldLastEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        if (buildSettings == null) return;
        MirrorSettings m = buildSettings.getMirrorSettings();

        if (m == null || !m.enabled || (!m.mirrorX && !m.mirrorY && !m.mirrorZ)) return;

        Vec3d playerPos = new Vec3d(player.posX, player.posY, player.posZ);
        Vec3d pos = m.position.add(epsilon);
        int radius = m.radius;

        if (m.mirrorX) {
            Vec3d posA = new Vec3d(pos.x, pos.y - radius, pos.z - radius);
            Vec3d posB = new Vec3d(pos.x, pos.y + radius, pos.z + radius);

            drawMirrorPlane(playerPos, posA, posB, colorX, m.drawLines, m.drawPlanes);
        }
        if (m.mirrorY) {
            Vec3d posA = new Vec3d(pos.x - radius, pos.y, pos.z - radius);
            Vec3d posB = new Vec3d(pos.x + radius, pos.y, pos.z + radius);

            drawMirrorPlaneY(playerPos, posA, posB, colorY, m.drawLines, m.drawPlanes);
        }
        if (m.mirrorZ) {
            Vec3d posA = new Vec3d(pos.x - radius, pos.y - radius, pos.z);
            Vec3d posB = new Vec3d(pos.x + radius, pos.y + radius, pos.z);

            drawMirrorPlane(playerPos, posA, posB, colorZ, m.drawLines, m.drawPlanes);
        }

        //Draw axis coordinated colors if two or more axes are enabled
        //(If only one is enabled the lines are that planes color)
        if (m.drawLines && ((m.mirrorX && m.mirrorY) || (m.mirrorX && m.mirrorZ) || (m.mirrorY && m.mirrorZ))) {
            drawMirrorLines(playerPos, m);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorPlane(Vec3d playerPos, Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes) {

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha);
        GL11.glLineWidth(2);
        GL11.glDepthMask(false);
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

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorPlaneY(Vec3d playerPos, Vec3d posA, Vec3d posB, Color c, boolean drawLines, boolean drawPlanes) {

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        GL11.glLineWidth(2);
        GL11.glDepthMask(false);
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

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @SideOnly(Side.CLIENT)
    public static void drawMirrorLines(Vec3d playerPos, MirrorSettings m) {

        Vec3d pos = m.position.add(epsilon);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

        GL11.glColor4d(100, 100, 100, 255);
        GL11.glLineWidth(2);
        GL11.glDepthMask(false);
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

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }


}
