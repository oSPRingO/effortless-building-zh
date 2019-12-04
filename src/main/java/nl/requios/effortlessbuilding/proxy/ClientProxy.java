package nl.requios.effortlessbuilding.proxy;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeOptions;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.gui.buildmodifier.ModifierSettingsGui;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.*;
import nl.requios.effortlessbuilding.render.RenderHandler;
import nl.requios.effortlessbuilding.render.ShaderHandler;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = {Dist.CLIENT})
public class ClientProxy implements IProxy {
    public static KeyBinding[] keyBindings;
    public static RayTraceResult previousLookAt;
    public static RayTraceResult currentLookAt;
    private static int placeCooldown = 0;
    private static int breakCooldown = 0;
    private static boolean shadersInitialized = false;

    public static int ticksInGame = 0;

    private static final HashMap<BuildModes.BuildModeEnum, ResourceLocation> buildModeIcons = new HashMap<>();
    private static final HashMap<ModeOptions.ActionEnum, ResourceLocation> modeOptionIcons = new HashMap<>();

    @Override
    public void setup(FMLCommonSetupEvent event) {}

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        //Crashes in 1.13, do it elsewhere
//        ShaderHandler.init();

        // register key bindings
        keyBindings = new KeyBinding[7];

        // instantiate the key bindings
        keyBindings[0] = new KeyBinding("key.effortlessbuilding.hud.desc", KeyConflictContext.UNIVERSAL, InputMappings.getInputByCode(GLFW.GLFW_KEY_KP_ADD, 0), "key.effortlessbuilding.category");
        keyBindings[1] = new KeyBinding("key.effortlessbuilding.replace.desc", KeyConflictContext.IN_GAME, InputMappings.getInputByCode(GLFW.GLFW_KEY_KP_SUBTRACT, 0), "key.effortlessbuilding.category");
        keyBindings[2] = new KeyBinding("key.effortlessbuilding.creative.desc", KeyConflictContext.IN_GAME, InputMappings.getInputByCode(GLFW.GLFW_KEY_F4, 0), "key.effortlessbuilding.category");
        keyBindings[3] = new KeyBinding("key.effortlessbuilding.mode.desc", KeyConflictContext.IN_GAME, InputMappings.getInputByCode(GLFW.GLFW_KEY_LEFT_ALT, 0), "key.effortlessbuilding.category") {
            @Override
            public boolean func_197983_b(KeyBinding other) {
                //Does not conflict with Chisels and Bits radial menu
                if (other.getKey().getKeyCode() == getKey().getKeyCode() && other.getKeyDescription().equals("mod.chiselsandbits.other.mode")) return false;
                return super.func_197983_b(other);
            }
        };
        keyBindings[4] = new KeyBinding("key.effortlessbuilding.undo.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputMappings.getInputByCode(GLFW.GLFW_KEY_Z, 0), "key.effortlessbuilding.category");
        keyBindings[5] = new KeyBinding("key.effortlessbuilding.redo.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputMappings.getInputByCode(GLFW.GLFW_KEY_Y, 0), "key.effortlessbuilding.category");
        keyBindings[6] = new KeyBinding("key.effortlessbuilding.altplacement.desc", KeyConflictContext.IN_GAME, InputMappings.getInputByCode(GLFW.GLFW_KEY_LEFT_CONTROL, 0), "key.effortlessbuilding.category");
        //keyBindings[7] = new KeyBinding("Reload shaders", KeyConflictContext.UNIVERSAL, InputMappings.getInputByCode(GLFW.GLFW_KEY_TAB, 0), "key.effortlessbuilding.category");

        // register all the key bindings
        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
        }

    }

    public EntityPlayer getPlayerEntityFromContext(Supplier<NetworkEvent.Context> ctx){
        return (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT ? Minecraft.getInstance().player : ctx.get().getSender());
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            event.getWorld().addEventListener(new RenderHandler());
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre event) {
        //register icon textures
        final TextureMap map = event.getMap();

        for ( final BuildModes.BuildModeEnum mode : BuildModes.BuildModeEnum.values() )
        {
            final ResourceLocation sprite = new ResourceLocation(EffortlessBuilding.MODID, "icons/" + mode.name().toLowerCase());
            map.registerSprite(Minecraft.getInstance().getResourceManager(), sprite);
            buildModeIcons.put(mode, sprite);
        }

        for ( final ModeOptions.ActionEnum action : ModeOptions.ActionEnum.values() )
        {
            final ResourceLocation sprite = new ResourceLocation(EffortlessBuilding.MODID, "icons/" + action.name().toLowerCase());
            map.registerSprite(Minecraft.getInstance().getResourceManager(), sprite);
            modeOptionIcons.put(action, sprite);
        }
    }

    public static TextureAtlasSprite getBuildModeIcon(BuildModes.BuildModeEnum mode) {
        return Minecraft.getInstance().getTextureMap().getSprite(buildModeIcons.get(mode));
    }

    public static TextureAtlasSprite getModeOptionIcon(ModeOptions.ActionEnum action) {
        return Minecraft.getInstance().getTextureMap().getSprite(modeOptionIcons.get(action));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase == TickEvent.Phase.START) {
            onMouseInput();

            //Update previousLookAt
            RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
            //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
            if (objectMouseOver == null) return;

            if (currentLookAt == null) {
                currentLookAt = objectMouseOver;
                previousLookAt = objectMouseOver;
                return;
            }

            if (objectMouseOver.type == RayTraceResult.Type.BLOCK) {
                if (currentLookAt.type != RayTraceResult.Type.BLOCK) {
                    currentLookAt = objectMouseOver;
                    previousLookAt = objectMouseOver;
                } else {
                    if (currentLookAt.getBlockPos() != objectMouseOver.getBlockPos()) {
                        previousLookAt = currentLookAt;
                        currentLookAt = objectMouseOver;
                    }
                }
            }

        } else if (event.phase == TickEvent.Phase.END){
            GuiScreen gui = Minecraft.getInstance().currentScreen;
            if(gui == null || !gui.doesGuiPauseGame()) {
                ticksInGame++;
            }

            //Init shaders in the first tick. Doing it anywhere before this seems to crash the game.
            if (!shadersInitialized) {
                ShaderHandler.init();
                shadersInitialized = true;
            }
        }

    }

    private static void onMouseInput() {
        Minecraft mc = Minecraft.getInstance();
        EntityPlayerSP player = mc.player;
        if (player == null) return;
        BuildModes.BuildModeEnum buildMode = ModeSettingsManager.getModeSettings(player).getBuildMode();

        if (Minecraft.getInstance().currentScreen != null ||
            buildMode == BuildModes.BuildModeEnum.NORMAL ||
            RadialMenu.instance.isVisible()) {
            return;
        }

        if (mc.gameSettings.keyBindUseItem.isKeyDown()) {

            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

            if (placeCooldown <= 0) {
                placeCooldown = 4;

                ItemStack currentItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
                if (currentItemStack.getItem() instanceof ItemBlock ||
                    (CompatHelper.isItemBlockProxy(currentItemStack) && !player.isSneaking())) {

                    ItemStack itemStack = CompatHelper.getItemBlockFromStack(currentItemStack);

                    //find position in distance
                    RayTraceResult lookingAt = getLookingAt(player);
                    BuildModes.onBlockPlacedMessage(player, lookingAt == null ? new BlockPlacedMessage() : new BlockPlacedMessage(lookingAt, true));
                    PacketHandler.INSTANCE.sendToServer(lookingAt == null ? new BlockPlacedMessage() : new BlockPlacedMessage(lookingAt, true));

                    //play sound if further than normal
                    if (lookingAt != null && lookingAt.type == RayTraceResult.Type.BLOCK &&
                        (lookingAt.hitVec.subtract(player.getEyePosition(1f))).lengthSquared() > 25f &&
                        itemStack.getItem() instanceof ItemBlock) {

                        IBlockState state = ((ItemBlock) itemStack.getItem()).getBlock().getDefaultState();
                        BlockPos blockPos = lookingAt.getBlockPos();
                        SoundType soundType = state.getBlock().getSoundType(state, player.world, blockPos, player);
                        player.world.playSound(player, player.getPosition(), soundType.getPlaceSound(), SoundCategory.BLOCKS,
                                0.4f, soundType.getPitch() * 1f);
                        player.swingArm(EnumHand.MAIN_HAND);
                    }
                }
            }
            else if (buildMode == BuildModes.BuildModeEnum.NORMAL_PLUS) {
                placeCooldown--;
                if (ModeOptions.getBuildSpeed() == ModeOptions.ActionEnum.FAST_SPEED) placeCooldown = 0;
            }
        } else {
            placeCooldown = 0;
        }

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {

            //Break block in distance in creative (or survival if enabled in config)
            if (breakCooldown <= 0) {
                breakCooldown = 4;

                //Early out if cant break far, coming from own mouse event (not block broken event)
                //To make breaking blocks in survival possible like array
                //TODO this causes not being able to cancel placement in survival
                //  moving it to after buildmodes fixes that, but introduces this bug
                if (!ReachHelper.canBreakFar(player)) return;

                RayTraceResult lookingAt = getLookingAt(player);
                BuildModes.onBlockBrokenMessage(player, lookingAt == null ? new BlockBrokenMessage() : new BlockBrokenMessage(lookingAt));
                PacketHandler.INSTANCE.sendToServer(lookingAt == null ? new BlockBrokenMessage() : new BlockBrokenMessage(lookingAt));

                //play sound if further than normal
                if (lookingAt != null && lookingAt.type == RayTraceResult.Type.BLOCK &&
                    (lookingAt.hitVec.subtract(player.getEyePosition(1f))).lengthSquared() > 25f) {

                    BlockPos blockPos = lookingAt.getBlockPos();
                    IBlockState state = player.world.getBlockState(blockPos);
                    SoundType soundtype = state.getBlock().getSoundType(state, player.world, blockPos, player);
                    player.world.playSound(player, player.getPosition(), soundtype.getBreakSound(), SoundCategory.BLOCKS,
                            0.4f, soundtype.getPitch() * 1f);
                    player.swingArm(EnumHand.MAIN_HAND);
                }
            }
            else if (buildMode == BuildModes.BuildModeEnum.NORMAL_PLUS) {
                breakCooldown--;
                if (ModeOptions.getBuildSpeed() == ModeOptions.ActionEnum.FAST_SPEED) breakCooldown = 0;
            }

            //EffortlessBuilding.packetHandler.sendToServer(new CancelModeMessage());

        } else {
            breakCooldown = 0;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getInstance().player;

        //Remember to send packet to server if necessary
        //Show Modifier Settings GUI
        if (keyBindings[0].isPressed()) {
            openModifierSettings();
        }

        //QuickReplace toggle
        if (keyBindings[1].isPressed()) {
            ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
            modifierSettings.setQuickReplace(!modifierSettings.doQuickReplace());
            EffortlessBuilding.log(player, "Set " + TextFormatting.GOLD + "Quick Replace " + TextFormatting.RESET + (
                    modifierSettings.doQuickReplace() ? "on" : "off"));
            PacketHandler.INSTANCE.sendToServer(new ModifierSettingsMessage(modifierSettings));
        }

        //Creative/survival mode toggle
        if (keyBindings[2].isPressed()) {
            if (player.isCreative()) {
                player.sendChatMessage("/gamemode survival");
            } else {
                player.sendChatMessage("/gamemode creative");
            }
        }

        //Undo (Ctrl+Z)
        if (keyBindings[4].isPressed()) {
            ModeOptions.ActionEnum action = ModeOptions.ActionEnum.UNDO;
            ModeOptions.performAction(player, action);
            PacketHandler.INSTANCE.sendToServer(new ModeActionMessage(action));
        }

        //Redo (Ctrl+Y)
        if (keyBindings[5].isPressed()) {
            ModeOptions.ActionEnum action = ModeOptions.ActionEnum.REDO;
            ModeOptions.performAction(player, action);
            PacketHandler.INSTANCE.sendToServer(new ModeActionMessage(action));
        }

        //Change placement mode
        if (keyBindings[6].isPressed()) {
            //Toggle between first two actions of the first option of the current build mode
            BuildModes.BuildModeEnum currentBuildMode = ModeSettingsManager.getModeSettings(player).getBuildMode();
            if (currentBuildMode.options.length > 0) {
                ModeOptions.OptionEnum option = currentBuildMode.options[0];
                if (option.actions.length >= 2) {
                    if (ModeOptions.getOptionSetting(option) == option.actions[0]) {
                        ModeOptions.performAction(player, option.actions[1]);
                        PacketHandler.INSTANCE.sendToServer(new ModeActionMessage(option.actions[1]));
                    } else {
                        ModeOptions.performAction(player, option.actions[0]);
                        PacketHandler.INSTANCE.sendToServer(new ModeActionMessage(option.actions[0]));
                    }
                }
            }
        }

        //For shader development
        if (keyBindings.length >= 8 && keyBindings[7].isPressed()) {
            ShaderHandler.init();
            EffortlessBuilding.log(player, "Reloaded shaders");
        }

    }

    public static void openModifierSettings() {
        Minecraft mc = Minecraft.getInstance();
        EntityPlayerSP player = mc.player;

        RadialMenu.instance.setVisibility(0f);

        //Disabled if max reach is 0, might be set in the config that way.
        if (ReachHelper.getMaxReach(player) == 0) {
            EffortlessBuilding.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
        } else {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ModifierSettingsGui());
            } else {
                player.closeScreen();
            }
        }
    }

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        EntityPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            BuildModes.initializeMode(player);
        }
    }

    @Nullable
    public static RayTraceResult getLookingAt(EntityPlayer player) {
//        World world = player.world;

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getPlacementReach(player);

//        Vec3d look = player.getLookVec();
//        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
//        Vec3d end = new Vec3d(player.posX + look.x * raytraceRange, player.posY + player.getEyeHeight() + look.y * raytraceRange, player.posZ + look.z * raytraceRange);
        return player.rayTrace(raytraceRange, 1f, RayTraceFluidMode.NEVER);
//        return world.rayTraceBlocks(start, end, false, false, false);
    }

    public static void logTranslate(String key) {
        EffortlessBuilding.log(Minecraft.getInstance().player, I18n.format(key), true);
    }
}
