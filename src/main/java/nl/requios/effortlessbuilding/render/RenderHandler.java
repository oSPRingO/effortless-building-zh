package nl.requios.effortlessbuilding.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

/***
 * Main render class for Effortless Building
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler {

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);

        begin(event.getPartialTicks());

        //Mirror and radial mirror lines and areas
        ModifierRenderer.render(modifierSettings);

        //Render block previews
        BlockPreviewRenderer.render(player, modifierSettings, modeSettings);

        end();
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

    private static void begin(float partialTicks) {
//        PlayerEntity player = Minecraft.getInstance().player;
//        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
//        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
//        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
//        Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

        GL11.glPushMatrix();
//        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);
        //TODO 1.15
        //RenderSystem.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        GL11.glDepthMask(false);
    }

    protected static void beginLines() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(2);
    }

    protected static void endLines() {
        GL11.glPopAttrib();
    }

    protected static void beginBlockPreviews() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().textureManager.bindTexture(ShaderHandler.shaderMaskTextureLocation);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, 0.8f);
    }

    protected static void endBlockPreviews() {
        ShaderHandler.releaseShader();
        RenderSystem.disableBlend();

        GL11.glPopAttrib();
    }

    private static void end() {
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    protected static void renderBlockPreview(BlockRendererDispatcher dispatcher, BlockPos blockPos, BlockState blockState) {
        if (blockState == null) return;

        RenderSystem.pushMatrix();
        RenderSystem.translatef(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(-0.01f, -0.01f, 0.01f);
        RenderSystem.scalef(1.02f, 1.02f, 1.02f);

        //TODO 1.15
//        try {
//            dispatcher.renderBlockBrightness(blockState, 0.85f);
//        } catch (NullPointerException e) {
//            EffortlessBuilding.logger.warn("RenderHandler::renderBlockPreview cannot render " + blockState.getBlock().toString());
//
//            //Render outline as backup
//            RenderSystem.popMatrix();
////            ShaderHandler.releaseShader();
//            GL11.glDisable(GL11.GL_LIGHTING);
//            renderBlockOutline(blockPos, new Vec3d(1f, 1f, 1f));
//            GL11.glEnable(GL11.GL_LIGHTING);
//            RenderSystem.pushMatrix();
//        }

        RenderSystem.popMatrix();
    }

    protected static void renderBlockOutline(BlockPos pos, Vec3d color) {
        renderBlockOutline(pos, pos, color);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(BlockPos pos1, BlockPos pos2, Vec3d color) {
        GL11.glLineWidth(2);

        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        //TODO 1.15
//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(BlockPos pos, VoxelShape collisionShape, Vec3d color) {
        GL11.glLineWidth(2);

//        AxisAlignedBB aabb = boundingBox.offset(pos).grow(0.0020000000949949026);
//        VoxelShape voxelShape = collisionShape.withOffset(pos.getX(), pos.getY(), pos.getZ());

//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
        //TODO 1.15
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
