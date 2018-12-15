package nl.requios.effortlessbuilding.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.gui.SettingsGui;
import nl.requios.effortlessbuilding.helper.RenderHelper;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;
import org.lwjgl.input.Keyboard;
import scala.collection.parallel.ParIterableLike;

import javax.annotation.Nullable;

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
        // This will never get called on client side
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

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        //Remember to send packet to server if necessary
        //Show HUD
        if (keyBindings[0].isPressed()) {
            //Disabled if max reach is 0, might be set in the config that way.
            if (BuildSettingsManager.getMaxReach(player) == 0) {
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

        //TODO increase range using getLookingAt
        // Would also need other trigger than onBlockPlaced
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

    private static float rayTraceRange = 32f;
    public static RayTraceResult getLookingAt(EntityPlayer player) {
        World world = player.world;
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, false, false, false);
    }
}
