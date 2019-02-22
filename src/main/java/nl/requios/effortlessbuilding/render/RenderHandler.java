package nl.requios.effortlessbuilding.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.network.ModeSettingsMessage;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.util.List;

/***
 * Main render class for Effortless Building
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderHandler implements IWorldEventListener {

    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
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
    public static void onRenderGameOverlay(final RenderGameOverlayEvent.Post event ) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        //check if chisel and bits tool in hand (and has menu)
        final boolean hasChiselInHand = CompatHelper.chiselsAndBitsProxy.isHoldingChiselTool(EnumHand.MAIN_HAND);

        final RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL && !hasChiselInHand) {
            final boolean wasVisible = RadialMenu.instance.isVisible();

            if (ClientProxy.keyBindings[3].isKeyDown()) {
                if (ReachHelper.getMaxReach(player) > 0) {
                    RadialMenu.instance.actionUsed = false;
                    RadialMenu.instance.raiseVisibility();
                } else if (ClientProxy.keyBindings[3].isPressed()) {
                    EffortlessBuilding.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
                }
            } else {
                if ( !RadialMenu.instance.actionUsed ) {
                    ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);

                    if ( RadialMenu.instance.switchTo != null ) {
                        playRadialMenuSound();
                        modeSettings.setBuildMode(RadialMenu.instance.switchTo);
                        ModeSettingsManager.setModeSettings(player, modeSettings);
                        EffortlessBuilding.packetHandler.sendToServer(new ModeSettingsMessage(modeSettings));

                        EffortlessBuilding.log(player, modeSettings.getBuildMode().name, true);
                    }

                    //TODO change buildmode settings

                    playRadialMenuSound();
                }

                RadialMenu.instance.actionUsed = true;
                RadialMenu.instance.decreaseVisibility();
            }

            if (RadialMenu.instance.isVisible()) {

                final ScaledResolution res = event.getResolution();
                RadialMenu.instance.configure(res.getScaledWidth(), res.getScaledHeight());

                if (!wasVisible) {
                    mc.inGameHasFocus = false;
                    mc.mouseHelper.ungrabMouseCursor();
                }

                if (mc.inGameHasFocus) {
                    KeyBinding.unPressAllKeys();
                }

                final int k1 = Mouse.getX() * res.getScaledWidth() / mc.displayWidth;
                final int l1 = res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1;

                net.minecraftforge.client.ForgeHooksClient.drawScreen(RadialMenu.instance, k1, l1, event.getPartialTicks());
            } else {
                if (wasVisible) {
                    mc.setIngameFocus();
                }
            }
        }
    }

    public static void playRadialMenuSound() {
        final float volume = 0.1f;
        if (volume >= 0.0001f) {
            final PositionedSoundRecord psr = new PositionedSoundRecord(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER,
                    volume, 1.0f, Minecraft.getMinecraft().player.getPosition());
            Minecraft.getMinecraft().getSoundHandler().playSound(psr);
        }
    }

    private static void begin(float partialTicks) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        Vec3d playerPos = new Vec3d(playerX, playerY, playerZ);

        GL11.glPushMatrix();
        GL11.glTranslated(-playerPos.x, -playerPos.y, -playerPos.z);

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
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(EffortlessBuilding.MODID, "textures/shader_mask.png"));

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, 0.8f);
    }

    protected static void endBlockPreviews() {
        ShaderHandler.releaseShader();
        GlStateManager.disableBlend();
        GL11.glPopAttrib();
    }

    private static void end() {
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    protected static void renderBlockPreview(BlockRendererDispatcher dispatcher, BlockPos blockPos, IBlockState blockState) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.01f, -0.01f, 0.01f);
        GlStateManager.scale(1.02f, 1.02f, 1.02f);
        dispatcher.renderBlockBrightness(blockState, 0.85f);
        GlStateManager.popMatrix();
    }

    protected static void renderBlockOutline(BlockPos pos, Vec3d color) {
        renderBlockOutline(pos, pos, color);
    }

    //Renders outline. Pos1 has to be minimal x,y,z and pos2 maximal x,y,z
    protected static void renderBlockOutline(BlockPos pos1, BlockPos pos2, Vec3d color) {
        GL11.glLineWidth(2);

        AxisAlignedBB aabb = new AxisAlignedBB(pos1, pos2.add(1, 1, 1)).grow(0.0020000000949949026);

        RenderGlobal.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }

    //Renders outline with given bounding box
    protected static void renderBlockOutline(BlockPos pos, AxisAlignedBB boundingBox, Vec3d color) {
        GL11.glLineWidth(2);

        AxisAlignedBB aabb = boundingBox.offset(pos).grow(0.0020000000949949026);

        RenderGlobal.drawSelectionBoundingBox(aabb, (float) color.x, (float) color.y, (float) color.z, 0.4f);
    }



    //IWORLDEVENTLISTENER IMPLEMENTATION
    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {

    }

    @Override
    public void notifyLightSet(BlockPos pos) {

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category,
                                         double x, double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
                              double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void onEntityAdded(Entity entityIn) {

    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

    }

    //Sends breaking progress for all coordinates to renderglobal, so all blocks get visually broken
    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        Minecraft mc = Minecraft.getMinecraft();

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
        if (!BuildModifiers.isEnabled(modifierSettings, pos)) return;

        List<BlockPos> coordinates = BuildModifiers.findCoordinates(mc.player, pos);
        for (int i = 1; i < coordinates.size(); i++) {
            BlockPos coordinate = coordinates.get(i);
            if (SurvivalHelper.canBreak(mc.world, mc.player, coordinate)) {
                //Send i as entity id because only one block can be broken per id
                //Unless i happens to be the player id, then take something else
                int fakeId = mc.player.getEntityId() != i ? i : coordinates.size();
                mc.renderGlobal.sendBlockBreakProgress(fakeId, coordinate, progress);
            }
        }
    }
}
