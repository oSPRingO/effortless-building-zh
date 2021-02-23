package nl.requios.effortlessbuildingzh.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmode.ModeOptions;
import nl.requios.effortlessbuildingzh.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuildingzh.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;
import nl.requios.effortlessbuildingzh.network.ModeActionMessage;
import nl.requios.effortlessbuildingzh.network.ModeSettingsMessage;
import nl.requios.effortlessbuildingzh.network.PacketHandler;
import nl.requios.effortlessbuildingzh.proxy.ClientProxy;

/***
 * Main render class for Effortless Building
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler {

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        if(event.getPhase() != EventPriority.NORMAL)
            return;

        MatrixStack matrixStack = event.getMatrixStack();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.getImpl(bufferBuilder);

        PlayerEntity player = Minecraft.getInstance().player;
        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);

        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        matrixStack.push();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        //Mirror and radial mirror lines and areas
        ModifierRenderer.render(matrixStack, renderTypeBuffer, modifierSettings);

        //Render block previews
        BlockPreviewRenderer.render(matrixStack, renderTypeBuffer, player, modifierSettings, modeSettings);

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
                    EffortlessBuildingZh.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
                }
            } else {
                if (!RadialMenu.instance.actionUsed) {
                    ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);

                    if (RadialMenu.instance.switchTo != null) {
                        playRadialMenuSound();
                        modeSettings.setBuildMode(RadialMenu.instance.switchTo);
                        ModeSettingsManager.setModeSettings(player, modeSettings);
                        PacketHandler.INSTANCE.sendToServer(new ModeSettingsMessage(modeSettings));

                        EffortlessBuildingZh.log(player, I18n.format(modeSettings.getBuildMode().name), true);
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
                if (wasVisible &&
                    RadialMenu.instance.doAction != ModeOptions.ActionEnum.OPEN_MODIFIER_SETTINGS &&
                    RadialMenu.instance.doAction != ModeOptions.ActionEnum.OPEN_PLAYER_SETTINGS) {
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

    protected static IVertexBuilder beginLines(IRenderTypeBuffer.Impl renderTypeBuffer) {
        return renderTypeBuffer.getBuffer(BuildRenderTypes.LINES);
    }

    protected static void endLines(IRenderTypeBuffer.Impl renderTypeBuffer) {
        renderTypeBuffer.finish();
    }

    protected static IVertexBuilder beginPlanes(IRenderTypeBuffer.Impl renderTypeBuffer){
        return renderTypeBuffer.getBuffer(BuildRenderTypes.PLANES);
    }

    protected static void endPlanes(IRenderTypeBuffer.Impl renderTypeBuffer){
        renderTypeBuffer.finish();
    }

    protected static void renderBlockPreview(MatrixStack matrixStack, IRenderTypeBuffer.Impl renderTypeBuffer, BlockRendererDispatcher dispatcher,
                                             BlockPos blockPos, BlockState blockState, float dissolve, BlockPos firstPos, BlockPos secondPos, boolean red) {
        if (blockState == null) return;

        matrixStack.push();
        matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//        matrixStack.rotate(Vector3f.YP.rotationDegrees(-90f));
        matrixStack.translate(-0.01f, -0.01f, -0.01f);
        matrixStack.scale(1.02f, 1.02f, 1.02f);

        //Begin block preview rendering
        RenderType blockPreviewRenderType = BuildRenderTypes.getBlockPreviewRenderType(dissolve, blockPos, firstPos, secondPos, red);
        IVertexBuilder buffer = renderTypeBuffer.getBuffer(blockPreviewRenderType);

//        MinecraftServer server = Minecraft.getInstance().getIntegratedServer();
//        World world = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, true);

        try {
            IBakedModel model = dispatcher.getModelForState(blockState);
            dispatcher.getBlockModelRenderer().renderModelBrightnessColor(matrixStack.getLast(), buffer,
                    blockState, model, 1f, 1f, 1f, 0, OverlayTexture.NO_OVERLAY);
//        blockRendererDispatcher.getBlockModelRenderer().renderModel(world, blockRendererDispatcher.getModelForState(blockState),
//                blockState, logicPos, matrixStack, renderTypeBuffer.getBuffer(renderType), true, new Random(), blockState.getPositionRandom(logicPos), i);
        } catch (NullPointerException e) {
            EffortlessBuildingZh.logger.warn("RenderHandler::renderBlockPreview cannot render " + blockState.getBlock().toString());

            //Render outline as backup, escape out of the current renderstack
            matrixStack.pop();
            renderTypeBuffer.finish();
            IVertexBuilder lineBuffer = beginLines(renderTypeBuffer);
            renderBlockOutline(matrixStack, lineBuffer, blockPos, new Vec3d(1f, 1f, 1f));
            endLines(renderTypeBuffer);
            buffer = renderTypeBuffer.getBuffer(Atlases.getTranslucentBlockType()); //any type will do, as long as we have something on the stack
            matrixStack.push();
        }

        renderTypeBuffer.finish();
        matrixStack.pop();
    }

    protected static void renderBlockOutline(MatrixStack matrixStack, IVertexBuilder buffer, BlockPos pos, Vec3d color) {
        renderBlockOutline(matrixStack, buffer, pos, pos, color);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(MatrixStack matrixStack, IVertexBuilder buffer, BlockPos pos1, BlockPos pos2, Vec3d color) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        WorldRenderer.drawBoundingBox(matrixStack, buffer, aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
//        WorldRenderer.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(MatrixStack matrixStack, IVertexBuilder buffer, BlockPos pos, VoxelShape collisionShape, Vec3d color) {
//        WorldRenderer.drawShape(collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
        WorldRenderer.drawVoxelShapeParts(matrixStack, buffer, collisionShape, pos.getX(), pos.getY(), pos.getZ(), (float) color.x, (float) color.y, (float) color.z, 0.4f);
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
