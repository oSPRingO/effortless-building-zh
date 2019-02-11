package nl.requios.effortlessbuilding.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.gui.buildmodifier.ModifierSettingsGui;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.render.RenderHandler;
import nl.requios.effortlessbuilding.render.ShaderHandler;
import nl.requios.effortlessbuilding.network.*;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements IProxy {
    public static KeyBinding[] keyBindings;
    public static RayTraceResult previousLookAt;
    public static RayTraceResult currentLookAt;
    private static int breakCooldown = 0;

    public static int ticksInGame = 0;

    private static final HashMap<BuildModes.BuildModeEnum, TextureAtlasSprite> buildModeIcons = new HashMap<>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ShaderHandler.init();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // register key bindings
        keyBindings = new KeyBinding[5];

        // instantiate the key bindings
        keyBindings[0] = new KeyBinding("key.effortlessbuilding.hud.desc", Keyboard.KEY_ADD, "key.effortlessbuilding.category");
        keyBindings[1] = new KeyBinding("key.effortlessbuilding.replace.desc", Keyboard.KEY_SUBTRACT, "key.effortlessbuilding.category");
        keyBindings[2] = new KeyBinding("key.effortlessbuilding.creative.desc", Keyboard.KEY_F4, "key.effortlessbuilding.category");
        keyBindings[3] = new KeyBinding("key.effortlessbuilding.mode.desc", Keyboard.KEY_LMENU, "key.effortlessbuilding.category");
        keyBindings[4] = new KeyBinding("Reload shaders", Keyboard.KEY_TAB, "key.effortlessbuilding.category");

        // register all the key bindings
        for (int i = 0; i < keyBindings.length; ++i) {
            ClientRegistry.registerKeyBinding(keyBindings[i]);
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft().player : ctx.getServerHandler().player);
    }

    @Override
    public IThreadListener getThreadListenerFromContext(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft() : ((EntityPlayerMP) getPlayerEntityFromContext(ctx)).getServerWorld());
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        //This will get called clientside
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block block : EffortlessBuilding.BLOCKS) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }

        for (Item item : EffortlessBuilding.ITEMS) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() == Minecraft.getMinecraft().player) {
            event.getWorld().addEventListener(new RenderHandler());
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre event) {
        //register icon textures
        final TextureMap map = event.getMap();

        for ( final BuildModes.BuildModeEnum mode : BuildModes.BuildModeEnum.values() )
        {
            final ResourceLocation sprite = new ResourceLocation("effortlessbuilding", "icons/" + mode.name().toLowerCase());
            buildModeIcons.put( mode, map.registerSprite(sprite));
        }
    }

    public static TextureAtlasSprite getBuildModeIcon(BuildModes.BuildModeEnum mode) {
        return buildModeIcons.get(mode);
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (Minecraft.getMinecraft().currentScreen != null ||
            ModeSettingsManager.getModeSettings(player).getBuildMode() == BuildModes.BuildModeEnum.Normal ||
            RadialMenu.instance.isVisible()) {
            return;
        }

        if (mc.gameSettings.keyBindUseItem.isPressed()) {
            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

            ItemStack currentItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (CompatHelper.isItemBlockProxy(currentItemStack) && !player.isSneaking()) {

                //find position in distance
                RayTraceResult lookingAt = getLookingAt(player);
                EffortlessBuilding.packetHandler.sendToServer(new BlockPlacedMessage(lookingAt));

                //play sound if further than normal
                if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK &&
                    (lookingAt.hitVec.subtract(player.getPositionEyes(1f))).lengthSquared() > 25f) {

                    BlockPos blockPos = lookingAt.getBlockPos();
                    IBlockState state = player.world.getBlockState(blockPos);
                    SoundType soundtype = state.getBlock().getSoundType(state, player.world, blockPos, player);
                    player.world.playSound(player, blockPos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                            (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    player.swingArm(EnumHand.MAIN_HAND);
                }
            }
        }

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

            //Break block in distance in creative (or survival if enabled in config)
            if (ReachHelper.canBreakFar(player)) {
                if (breakCooldown <= 0) {
                    breakCooldown = 6;
                    RayTraceResult lookingAt = getLookingAt(player);
                    if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) {
                        EffortlessBuilding.packetHandler.sendToServer(new BlockBrokenMessage(lookingAt));

                        //play sound
                        BlockPos blockPos = lookingAt.getBlockPos();
                        IBlockState state = player.world.getBlockState(blockPos);
                        SoundType soundtype = state.getBlock().getSoundType(state, player.world, blockPos, player);
                        player.world.playSound(player, blockPos, soundtype.getBreakSound(), SoundCategory.BLOCKS,
                                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        player.swingArm(EnumHand.MAIN_HAND);
                    }
                } else {
                    breakCooldown--;
                }
            }

            //EffortlessBuilding.packetHandler.sendToServer(new CancelModeMessage());

        } else {
            breakCooldown = 0;
        }
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        //Remember to send packet to server if necessary
        //Show HUD
        if (keyBindings[0].isPressed()) {
            //Disabled if max reach is 0, might be set in the config that way.
            if (ReachHelper.getMaxReach(player) == 0) {
                EffortlessBuilding.log(player, "Effortless Building is disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
            } else {
                if (Minecraft.getMinecraft().currentScreen == null) {
                    Minecraft.getMinecraft().displayGuiScreen(new ModifierSettingsGui());
                } else {
                    player.closeScreen();
                }
            }
        }

        //QuickReplace toggle
        if (keyBindings[1].isPressed()) {
            ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
            modifierSettings.setQuickReplace(!modifierSettings.doQuickReplace());
            EffortlessBuilding.log(player, "Set "+ TextFormatting.GOLD + "Quick Replace " + TextFormatting.RESET + (
                    modifierSettings.doQuickReplace() ? "on" : "off"));
            EffortlessBuilding.packetHandler.sendToServer(new ModifierSettingsMessage(modifierSettings));
        }

        //Creative/survival mode toggle
        if (keyBindings[2].isPressed()) {
            if (player.isCreative()) {
                player.sendChatMessage("/gamemode 0");
            } else {
                player.sendChatMessage("/gamemode 1");
            }
        }

        if (keyBindings[4].isPressed()) {
            //TODO remove
            ShaderHandler.init();
            EffortlessBuilding.log(player, "Reloaded shaders");
            //player.playSound(SoundEvents.UI_BUTTON_CLICK, 1f, 1f);
            player.playSound(EffortlessBuilding.SOUND_BUILD_CLICK, 1f, 1f);
        }

    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            RayTraceResult objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
            //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
            if (objectMouseOver == null) return;

            if (currentLookAt == null) {
                currentLookAt = objectMouseOver;
                previousLookAt = objectMouseOver;
                return;
            }

            if (objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                if (currentLookAt.typeOfHit != RayTraceResult.Type.BLOCK) {
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
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if(gui == null || !gui.doesGuiPauseGame()) {
                ticksInGame++;
            }
        }

    }

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            BuildModes.initializeMode(player);
        }
    }

    public static RayTraceResult getLookingAt(EntityPlayer player) {
//        World world = player.world;

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getPlacementReach(player);

//        Vec3d look = player.getLookVec();
//        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
//        Vec3d end = new Vec3d(player.posX + look.x * raytraceRange, player.posY + player.getEyeHeight() + look.y * raytraceRange, player.posZ + look.z * raytraceRange);
        return player.rayTrace(raytraceRange, 1f);
//        return world.rayTraceBlocks(start, end, false, false, false);
    }
}
