package nl.requios.effortlessbuilding.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.*;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.Color;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderHelper {

    private static final Color colorX = new Color(255, 72, 52);
    private static final Color colorY = new Color(67, 204, 51);
    private static final Color colorZ = new Color(52, 247, 255);
    private static final int lineAlpha = 200;
    private static final int planeAlpha = 75;
    private static final Vec3d epsilon = new Vec3d(0.001, 0.001, 0.001); //prevents z-fighting

    public static void begin(float partialTicks) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

        GL11.glLineWidth(2);
        GL11.glDepthMask(false);
    }

    public static void end() {
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void renderBlockOutline(BlockPos pos) {
        renderBlockOutline(pos, pos);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    public static void renderBlockOutline(BlockPos pos1, BlockPos pos2) {
        GL11.glLineWidth(2);

        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        RenderGlobal.drawSelectionBoundingBox(aabb, 1f, 1f, 1f, 0.6f);
    }

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        if (buildSettings == null) return;
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        Array.ArraySettings a = buildSettings.getArraySettings();

        begin(event.getPartialTicks());

        //Mirror
        if (m != null && m.enabled && (m.mirrorX || m.mirrorY || m.mirrorZ))
        {
            Vec3d pos = m.position.add(epsilon);
            int radius = m.radius;

            if (m.mirrorX)
            {
                Vec3d posA = new Vec3d(pos.x, pos.y - radius, pos.z - radius);
                Vec3d posB = new Vec3d(pos.x, pos.y + radius, pos.z + radius);

                drawMirrorPlane(posA, posB, colorX, m.drawLines, m.drawPlanes);
            }
            if (m.mirrorY)
            {
                Vec3d posA = new Vec3d(pos.x - radius, pos.y, pos.z - radius);
                Vec3d posB = new Vec3d(pos.x + radius, pos.y, pos.z + radius);

                drawMirrorPlaneY(posA, posB, colorY, m.drawLines, m.drawPlanes);
            }
            if (m.mirrorZ)
            {
                Vec3d posA = new Vec3d(pos.x - radius, pos.y - radius, pos.z);
                Vec3d posB = new Vec3d(pos.x + radius, pos.y + radius, pos.z);

                drawMirrorPlane(posA, posB, colorZ, m.drawLines, m.drawPlanes);
            }

            //Draw axis coordinated colors if two or more axes are enabled
            //(If only one is enabled the lines are that planes color)
            if (m.drawLines && ((m.mirrorX && m.mirrorY) || (m.mirrorX && m.mirrorZ) || (m.mirrorY && m.mirrorZ)))
            {
                drawMirrorLines(m);
            }

        }

        //Render block previews
        RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockPos = objectMouseOver.getBlockPos();
            ItemStack stack = ItemStack.EMPTY;
            ItemStack mainhand = player.getHeldItemMainhand();
            IBlockState blockState = null;
            if (mainhand.getItem() instanceof ItemBlock) {
                stack = mainhand;
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                Vec3d hitVec = objectMouseOver.hitVec;
                hitVec = new Vec3d(Math.abs(hitVec.x - ((int) hitVec.x)), Math.abs(hitVec.y - ((int) hitVec.y)), Math.abs(hitVec.z - ((int) hitVec.z)));
                blockState = block.getStateForPlacement(player.world, blockPos, objectMouseOver.sideHit,
                        ((float) hitVec.x), ((float) hitVec.y), ((float) hitVec.z), stack.getMetadata(), player, EnumHand.MAIN_HAND);
            }
            if (mainhand.getItem() instanceof ItemRandomizerBag) {
                //TODO figure this out
            }
            //TODO check offhand

            //Check if tool (or none) in hand
            boolean toolInHand = !mainhand.isEmpty() && mainhand.getItem() instanceof ItemTool;
            if (!buildSettings.doQuickReplace() && !toolInHand) {
                blockPos = blockPos.offset(objectMouseOver.sideHit);
            }

            if (buildSettings.doQuickReplace() && !toolInHand) {
                //Get under tall grass and other replaceable blocks
                if (player.world.getBlockState(blockPos).getBlock().isReplaceable(player.world, blockPos)) {
                    blockPos = blockPos.down();
                }
            }

            //Render current block outline based on config, or when QuickReplace is enabled
            if (buildSettings.doQuickReplace() || BuildConfig.visuals.showOutlineOnCurrentBlock) {
                RenderHelper.renderBlockOutline(blockPos);
            }

            //TODO testing
            if (blockState != null) {
                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
                GlStateManager.pushMatrix();//Push matrix again just because
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
                GlStateManager.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.005f, -0.005f, 0.005f);
                GlStateManager.scale(1.01f, 1.01f, 1.01f);
                GL14.glBlendColor(1F, 1F, 1F, 0.6f);
                dispatcher.renderBlockBrightness(blockState, 1f);
                GlStateManager.popMatrix();
                GL11.glPopAttrib();
            }

            //Mirror
            if (m != null && m.enabled && (m.mirrorX || m.mirrorY || m.mirrorZ) &&
                    !(blockPos.getX() + 0.5 < m.position.x - m.radius) && !(blockPos.getX() + 0.5 > m.position.x + m.radius) &&
                    !(blockPos.getY() + 0.5 < m.position.y - m.radius) && !(blockPos.getY() + 0.5 > m.position.y + m.radius) &&
                    !(blockPos.getZ() + 0.5 < m.position.z - m.radius) && !(blockPos.getZ() + 0.5 > m.position.z + m.radius))
            {
                if (m.mirrorX) drawMirrorBlockOutlineX(buildSettings, blockPos);
                if (m.mirrorY) drawMirrorBlockOutlineY(buildSettings, blockPos);
                if (m.mirrorZ) drawMirrorBlockOutlineZ(buildSettings, blockPos);
            }

            //Array
            if (a != null && a.enabled && (a.offset.getX() != 0 || a.offset.getY() != 0 || a.offset.getZ() != 0)) {
                drawArrayBlockOutlines(a, blockPos);
            }
        }

        end();
    }


    //----Mirror----

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

    public static void drawMirrorLines(Mirror.MirrorSettings m) {

        Vec3d pos = m.position.add(epsilon);

        GL11.glColor4d(100, 100, 100, 255);
        GL11.glLineWidth(2);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        bufferBuilder.pos(pos.x - m.radius, pos.y, pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x + m.radius, pos.y, pos.z).color(colorX.getRed(), colorX.getGreen(), colorX.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y - m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y + m.radius, pos.z).color(colorY.getRed(), colorY.getGreen(), colorY.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z - m.radius).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();
        bufferBuilder.pos(pos.x, pos.y, pos.z + m.radius).color(colorZ.getRed(), colorZ.getGreen(), colorZ.getBlue(), lineAlpha).endVertex();

        tessellator.draw();
    }

    public static void drawMirrorBlockOutlineX(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double x = m.position.x + (m.position.x - oldBlockPos.getX() - 0.5);
        BlockPos newBlockPos = new BlockPos(x, oldBlockPos.getY(), oldBlockPos.getZ());

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        drawArrayBlockOutlines(buildSettings.getArraySettings(), newBlockPos);

        if (m.mirrorY) drawMirrorBlockOutlineY(buildSettings, newBlockPos);
        if (m.mirrorZ) drawMirrorBlockOutlineZ(buildSettings, newBlockPos);
    }

    public static void drawMirrorBlockOutlineY(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double y = m.position.y + (m.position.y - oldBlockPos.getY() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), y, oldBlockPos.getZ());

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        drawArrayBlockOutlines(buildSettings.getArraySettings(), newBlockPos);

        if (m.mirrorZ) drawMirrorBlockOutlineZ(buildSettings, newBlockPos);
    }

    public static void drawMirrorBlockOutlineZ(BuildSettingsManager.BuildSettings buildSettings, BlockPos oldBlockPos) {
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        //find mirror position
        double z = m.position.z + (m.position.z - oldBlockPos.getZ() - 0.5);
        BlockPos newBlockPos = new BlockPos(oldBlockPos.getX(), oldBlockPos.getY(), z);

        RenderHelper.renderBlockOutline(newBlockPos);

        //Array synergy
        drawArrayBlockOutlines(buildSettings.getArraySettings(), newBlockPos);
    }


    //----Array----

    public static void drawArrayBlockOutlines(Array.ArraySettings a, BlockPos pos) {
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
