package nl.requios.effortlessbuilding.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.BuildModifiers;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.command.CommandReach;
import nl.requios.effortlessbuilding.gui.SettingsGui;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.RenderHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.network.BlockBrokenMessage;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements IProxy {
    public static KeyBinding[] keyBindings;
    public static RayTraceResult previousLookAt;
    public static RayTraceResult currentLookAt;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // register key bindings
        keyBindings = new KeyBinding[3];

        // instantiate the key bindings
        keyBindings[0] = new KeyBinding("key.effortlessbuilding.hud.desc", Keyboard.KEY_ADD, "key.effortlessbuilding.category");
        keyBindings[1] = new KeyBinding("key.effortlessbuilding.replace.desc", Keyboard.KEY_SUBTRACT, "key.effortlessbuilding.category");
        keyBindings[2] = new KeyBinding("key.effortlessbuilding.creative.desc", Keyboard.KEY_F4, "key.effortlessbuilding.category");

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
            event.getWorld().addEventListener(new RenderHelper());
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (!BuildModifiers.isEnabled(BuildSettingsManager.getBuildSettings(player), player.getPosition())) return;

        if (mc.gameSettings.keyBindUseItem.isPressed()) {
            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

            ItemStack currentItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (currentItemStack.getItem() instanceof ItemBlock || currentItemStack.getItem() instanceof ItemRandomizerBag) {
                //find position in distance
                RayTraceResult lookingAt = getLookingAt(player);
                if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) {
                    EffortlessBuilding.packetHandler.sendToServer(new BlockPlacedMessage(lookingAt));
                }
            }
        }

        if (mc.gameSettings.keyBindAttack.isKeyDown()) {
            //KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

            //Break block in distance in creative (or survival if enabled in config)
            if (ReachHelper.canBreakFar(player)) {
                RayTraceResult lookingAt = getLookingAt(player);
                if (lookingAt != null && lookingAt.typeOfHit == RayTraceResult.Type.BLOCK) {
                    EffortlessBuilding.packetHandler.sendToServer(new BlockBrokenMessage(lookingAt));

                    //play sound
                    BlockPos blockPos = lookingAt.getBlockPos();
                    IBlockState state = player.world.getBlockState(blockPos);
                    SoundType soundtype = state.getBlock().getSoundType(state, player.world, blockPos, player);
                    player.world.playSound(player, blockPos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    player.swingArm(EnumHand.MAIN_HAND);
                }
            }
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
                    Minecraft.getMinecraft().displayGuiScreen(new SettingsGui());
                } else {
                    player.closeScreen();
                }
            }
        }

        //QuickReplace toggle
        if (keyBindings[1].isPressed()) {
            BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
            buildSettings.setQuickReplace(!buildSettings.doQuickReplace());
            EffortlessBuilding.log(player, "Set "+ TextFormatting.GOLD + "Quick Replace " + TextFormatting.RESET + (buildSettings.doQuickReplace() ? "on" : "off"));
            EffortlessBuilding.packetHandler.sendToServer(new BuildSettingsMessage(buildSettings));
        }

        //Creative/survival mode toggle
        if (keyBindings[2].isPressed()) {
            if (player.isCreative()) {
                player.sendChatMessage("/gamemode 0");
            } else {
                player.sendChatMessage("/gamemode 1");
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

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
                if (currentLookAt.getBlockPos() != objectMouseOver.getBlockPos()){
                    previousLookAt = currentLookAt;
                    currentLookAt = objectMouseOver;
                }
            }
        }
    }

    public static RayTraceResult getLookingAt(EntityPlayer player) {
//        World world = player.world;

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getMaxReach(player) / 4f;

//        Vec3d look = player.getLookVec();
//        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
//        Vec3d end = new Vec3d(player.posX + look.x * raytraceRange, player.posY + player.getEyeHeight() + look.y * raytraceRange, player.posZ + look.z * raytraceRange);
        return player.rayTrace(raytraceRange, 1f);
//        return world.rayTraceBlocks(start, end, false, false, false);
    }
}
