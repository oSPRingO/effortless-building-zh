package nl.requios.effortlessbuilding.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.ModeOptions;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.ModeActionMessage;
import nl.requios.effortlessbuilding.network.ModeSettingsMessage;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.OptionalDouble;
import java.util.Random;

/***
 * Main render class for Effortless Building
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler {

    protected static final RenderState.LayerState PROJECTION_LAYERING = new RenderState.LayerState("projection_layering", () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.0F, 1.0F, 0.999F);
        RenderSystem.matrixMode(5888);
    }, () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
    });
    protected static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, () -> {
        RenderSystem.disableBlend();
    });
    protected static final RenderState.WriteMaskState COLOR_WRITE = new RenderState.WriteMaskState(true, false);

    public static final RenderType PLANES = RenderType.makeType("planes", DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_STRIP, 256, RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.empty())).layer(PROJECTION_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).writeMask(COLOR_WRITE).build(false));

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        if(event.getPhase() != EventPriority.NORMAL)
            return;

        // Get instances of the classes required for a block render.
        MinecraftServer server = Minecraft.getInstance().getIntegratedServer();
        World world = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, true);
        MatrixStack matrixStack = event.getMatrixStack();

        // Get the projected view coordinates.
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        // Choose obsidian as the arbitrary block.
        BlockState blockState = Blocks.BIRCH_LOG.getDefaultState();

        // Begin rendering the block.
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.getImpl(bufferBuilder);

        renderBlock(matrixStack, renderTypeBuffer, world, blockState, new BlockPos(0, 128, 0), projectedView, new Vec3d(0.0, 128.0, 0.0));

        renderTypeBuffer.finish();

//TEST lines

        matrixStack.push();
        Vec3d pos = new Vec3d(0, 0, 0);
        matrixStack.translate(-projectedView.x + pos.x, -projectedView.y + pos.y, -projectedView.z + pos.z);
        IVertexBuilder buffer = renderTypeBuffer.getBuffer(PLANES);
//        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        Vec3d posA = new Vec3d(-10, 128, -10);
        Vec3d posB = new Vec3d(10, 120, 10);
        Color c = new Color(255, 72, 52);
        int planeAlpha = 75;
        buffer.pos(posA.x, posA.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
        buffer.pos(posA.x, posB.y, posA.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
        buffer.pos(posB.x, posA.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();
        buffer.pos(posB.x, posB.y, posB.z).color(c.getRed(), c.getGreen(), c.getBlue(), planeAlpha).endVertex();

        matrixStack.pop();
        renderTypeBuffer.finish();


//        PlayerEntity player = Minecraft.getInstance().player;
//        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
//        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
//
//        begin(event.getPartialTicks(), event.getMatrixStack());
//
//        //Mirror and radial mirror lines and areas
//        ModifierRenderer.render(modifierSettings, event.getMatrixStack());
//
//        //Render block previews
//        BlockPreviewRenderer.render(player, modifierSettings, modeSettings, event.getMatrixStack());
//
//        end(event.getMatrixStack());
    }

    public static void renderBlock(MatrixStack matrixStack, IRenderTypeBuffer.Impl renderTypeBuffer, World world, BlockState blockState, BlockPos logicPos, Vec3d projectedView, Vec3d renderCoordinates)
    {
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        int i = OverlayTexture.NO_OVERLAY;

        matrixStack.push();
        matrixStack.translate(-projectedView.x + renderCoordinates.x, -projectedView.y + renderCoordinates.y, -projectedView.z + renderCoordinates.z);

        RenderType renderType = RenderTypeLookup.getRenderType(blockState);
        renderType = Atlases.getTranslucentBlockType();
        IBakedModel model = blockRendererDispatcher.getModelForState(blockState);
        blockRendererDispatcher.getBlockModelRenderer().renderModelBrightnessColor(matrixStack.getLast(), renderTypeBuffer.getBuffer(renderType),
                blockState, model, 1f, 1f, 1f, 1000, OverlayTexture.NO_OVERLAY);
//        blockRendererDispatcher.getBlockModelRenderer().renderModel(world, blockRendererDispatcher.getModelForState(blockState),
//                blockState, logicPos, matrixStack, renderTypeBuffer.getBuffer(renderType), true, new Random(), blockState.getPositionRandom(logicPos), i);

        matrixStack.pop();
    }

    @SubscribeEvent
    //Display Radial Menu
    public static void onRenderGameOverlay(final RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;

        //check if chisel and bits tool in hand (and has menu)
//        final boolean hasChiselInHand = CompatHelper.chiselsAndBitsProxy.isHoldingChiselTool(EnumHand.MAIN_HAND);

        final RenderGameOverlayEvent.ElementType type = event.getType();
        //TODO 1.13 compatibility
        if (type == RenderGameOverlayEvent.ElementType.ALL /*&& !hasChiselInHand*/) {
            final boolean wasVisible = RadialMenu.instance.isVisible();

            if (ClientProxy.keyBindings[3].isKeyDown()) {
                if (ReachHelper.getMaxReach(player) > 0) {
                    RadialMenu.instance.actionUsed = false;
                    RadialMenu.instance.raiseVisibility();
                } else if (ClientProxy.keyBindings[3].isPressed()) {
                    EffortlessBuilding.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
                }
            } else {
                if (!RadialMenu.instance.actionUsed) {
                    ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);

                    if (RadialMenu.instance.switchTo != null) {
                        playRadialMenuSound();
                        modeSettings.setBuildMode(RadialMenu.instance.switchTo);
                        ModeSettingsManager.setModeSettings(player, modeSettings);
                        PacketHandler.INSTANCE.sendToServer(new ModeSettingsMessage(modeSettings));

                        EffortlessBuilding.log(player, I18n.format(modeSettings.getBuildMode().name), true);
                    }

                    //Perform button action
                    ModeOptions.ActionEnum action = RadialMenu.instance.doAction;
                    if (action != null) {
                        ModeOptions.performAction(player, action);
                        PacketHandler.INSTANCE.sendToServer(new ModeActionMessage(action));
                    }

                    playRadialMenuSound();
                }

                RadialMenu.instance.actionUsed = true;
                RadialMenu.instance.decreaseVisibility();
            }

            if (RadialMenu.instance.isVisible()) {

                int scaledWidth = mc.getMainWindow().getScaledWidth();
                int scaledHeight = mc.getMainWindow().getScaledHeight();
                RadialMenu.instance.configure(scaledWidth, scaledHeight);

                if (!wasVisible) {
                    mc.mouseHelper.ungrabMouse();
                }

                if (mc.mouseHelper.isMouseGrabbed()) {
                    KeyBinding.unPressAllKeys();
                }

                final int mouseX = ((int) mc.mouseHelper.getMouseX()) * scaledWidth / mc.getMainWindow().getFramebufferWidth();
                final int mouseY = scaledHeight - ((int) mc.mouseHelper.getMouseY()) * scaledHeight / mc.getMainWindow().getFramebufferHeight() - 1;

                net.minecraftforge.client.ForgeHooksClient.drawScreen(RadialMenu.instance, mouseX, mouseY, event.getPartialTicks());
            } else {
                if (wasVisible && RadialMenu.instance.doAction != ModeOptions.ActionEnum.OPEN_MODIFIER_SETTINGS) {
                    mc.mouseHelper.grabMouse();
                }
            }
        }
    }

    public static void playRadialMenuSound() {
        final float volume = 0.1f;
        if (volume >= 0.0001f) {
            SimpleSound sound = new SimpleSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, volume, 1.0f, Minecraft.getInstance().player.getPosition());
            Minecraft.getInstance().getSoundHandler().play(sound);
        }
    }

    private static void begin(float partialTicks, MatrixStack matrixStack) {
//        PlayerEntity player = Minecraft.getInstance().player;
//        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
//        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
//        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
//        Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

        matrixStack.push();
//        RenderSystem.pushMatrix();
//        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);
        //TODO 1.15
//        Vec3d projectedView = Minecraft.getInstance().getRenderManager().info.getProjectedView();
//        RenderSystem.translated(-projectedView.x, -projectedView.y, -projectedView.z);

        RenderSystem.depthMask(false);
    }

    protected static void beginLines() {
        RenderSystem.pushLightingAttributes(); //GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        RenderSystem.pushTextureAttributes();
        RenderSystem.disableCull();
        RenderSystem.disableLighting();
        RenderSystem.disableTexture();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.lineWidth(2);
    }

    protected static void endLines() {
        RenderSystem.popAttributes();
    }

    protected static void beginBlockPreviews() {
        RenderSystem.pushLightingAttributes(); //GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        RenderSystem.pushTextureAttributes();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
//        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().textureManager.bindTexture(ShaderHandler.shaderMaskTextureLocation);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.blendColor(1f, 1f, 1f, 0.8f);
    }

    protected static void endBlockPreviews() {
        ShaderHandler.releaseShader();
        RenderSystem.disableBlend();

        RenderSystem.popAttributes();
    }

    private static void end(MatrixStack matrixStack) {
        RenderSystem.depthMask(true);
        matrixStack.pop();
    }

    protected static void renderBlockPreview(BlockRendererDispatcher dispatcher, BlockPos blockPos, BlockState blockState, MatrixStack matrixStack) {
        if (blockState == null) return;

        RenderSystem.pushMatrix();
        RenderSystem.translatef(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(-0.01f, -0.01f, 0.01f);
        RenderSystem.scalef(1.02f, 1.02f, 1.02f);

        //TODO 1.15
        try {
            IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            dispatcher.renderBlock(blockState, matrixStack, bufferSource, 1, 1);
            bufferSource.finish();
//            dispatcher.renderBlockBrightness(blockState, 0.85f);
        } catch (NullPointerException e) {
            EffortlessBuilding.logger.warn("RenderHandler::renderBlockPreview cannot render " + blockState.getBlock().toString());

            //Render outline as backup
            RenderSystem.popMatrix();
//            ShaderHandler.releaseShader();
            GL11.glDisable(GL11.GL_LIGHTING);
            renderBlockOutline(blockPos, new Vec3d(1f, 1f, 1f), matrixStack);
            GL11.glEnable(GL11.GL_LIGHTING);
            RenderSystem.pushMatrix();
        }

        RenderSystem.popMatrix();
    }

    protected static void renderBlockOutline(BlockPos pos, Vec3d color, MatrixStack matrixStack) {
        renderBlockOutline(pos, pos, color, matrixStack);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(BlockPos pos1, BlockPos pos2, Vec3d color, MatrixStack matrixStack) {
        RenderSystem.lineWidth(2);

        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        //TODO 1.15
        IVertexBuilder buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.LINES);
        WorldRenderer.drawBoundingBox(matrixStack, buffer, aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(BlockPos pos, VoxelShape collisionShape, Vec3d color, MatrixStack matrixStack) {
        RenderSystem.lineWidth(2);

//        AxisAlignedBB aabb = boundingBox.offset(pos).grow(0.0020000000949949026);
//        VoxelShape voxelShape = collisionShape.withOffset(pos.getX(), pos.getY(), pos.getZ());

//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
        //TODO 1.15
        IVertexBuilder buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.LINES);
        WorldRenderer.drawVoxelShapeParts(matrixStack, buffer, collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
//        WorldRenderer.drawShape(collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //TODO 1.14
    //Sends breaking progress for all coordinates to renderglobal, so all blocks get visually broken
//    @Override
//    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
//        Minecraft mc = Minecraft.getInstance();
//
//        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
//        if (!BuildModifiers.isEnabled(modifierSettings, pos)) return;
//
//        List<BlockPos> coordinates = BuildModifiers.findCoordinates(mc.player, pos);
//        for (int i = 1; i < coordinates.size(); i++) {
//            BlockPos coordinate = coordinates.get(i);
//            if (SurvivalHelper.canBreak(mc.world, mc.player, coordinate)) {
//                //Send i as entity id because only one block can be broken per id
//                //Unless i happens to be the player id, then take something else
//                int fakeId = mc.player.getEntityId() != i ? i : coordinates.size();
//                mc.renderGlobal.sendBlockBreakProgress(fakeId, coordinate, progress);
//            }
//        }
//    }
}
