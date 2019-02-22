package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.capability.ModeCapabilityManager;
import nl.requios.effortlessbuilding.capability.ModifierCapabilityManager;
import nl.requios.effortlessbuilding.command.CommandReach;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.gui.RandomizerBagGuiHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.item.ItemReachUpgrade1;
import nl.requios.effortlessbuilding.item.ItemReachUpgrade2;
import nl.requios.effortlessbuilding.item.ItemReachUpgrade3;
import nl.requios.effortlessbuilding.network.*;
import nl.requios.effortlessbuilding.proxy.IProxy;
import org.apache.logging.log4j.Logger;

@Mod(modid = EffortlessBuilding.MODID, name = EffortlessBuilding.NAME, version = EffortlessBuilding.VERSION)
@Mod.EventBusSubscriber
public class EffortlessBuilding
{
    public static final String MODID = "effortlessbuilding";
    public static final String NAME = "Effortless Building";
    public static final String VERSION = "1.12.2-2.2";

    @Mod.Instance(EffortlessBuilding.MODID)
    public static EffortlessBuilding instance;

    public static Logger logger;

    @SidedProxy(
        clientSide="nl.requios.effortlessbuilding.proxy.ClientProxy",
        serverSide="nl.requios.effortlessbuilding.proxy.ServerProxy"
    )
    public static IProxy proxy;

    public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(EffortlessBuilding.MODID);

    public static final ItemRandomizerBag ITEM_RANDOMIZER_BAG = new ItemRandomizerBag();
    public static final ItemReachUpgrade1 ITEM_REACH_UPGRADE_1 = new ItemReachUpgrade1();
    public static final ItemReachUpgrade2 ITEM_REACH_UPGRADE_2 = new ItemReachUpgrade2();
    public static final ItemReachUpgrade3 ITEM_REACH_UPGRADE_3 = new ItemReachUpgrade3();

    public static final Block[] BLOCKS = {
    };

    public static final Item[] ITEMS = {
            ITEM_RANDOMIZER_BAG,
            ITEM_REACH_UPGRADE_1,
            ITEM_REACH_UPGRADE_2,
            ITEM_REACH_UPGRADE_3
    };

    public static final int RANDOMIZER_BAG_GUI = 0;

    @EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        CapabilityManager.INSTANCE.register(ModifierCapabilityManager.IModifierCapability.class, new ModifierCapabilityManager.Storage(), ModifierCapabilityManager.ModifierCapability.class);
        CapabilityManager.INSTANCE.register(ModeCapabilityManager.IModeCapability.class, new ModeCapabilityManager.Storage(), ModeCapabilityManager.ModeCapability.class);

        EffortlessBuilding.packetHandler.registerMessage(ModifierSettingsMessage.MessageHandler.class, ModifierSettingsMessage.class, 0, Side.SERVER);
        EffortlessBuilding.packetHandler.registerMessage(ModifierSettingsMessage.MessageHandler.class, ModifierSettingsMessage.class, 0, Side.CLIENT);

        EffortlessBuilding.packetHandler.registerMessage(ModeSettingsMessage.MessageHandler.class, ModeSettingsMessage.class, 1, Side.SERVER);
        EffortlessBuilding.packetHandler.registerMessage(ModeSettingsMessage.MessageHandler.class, ModeSettingsMessage.class, 1, Side.CLIENT);

        EffortlessBuilding.packetHandler.registerMessage(BlockPlacedMessage.MessageHandler.class, BlockPlacedMessage.class, 2, Side.SERVER);
        EffortlessBuilding.packetHandler.registerMessage(BlockPlacedMessage.MessageHandler.class, BlockPlacedMessage.class, 2, Side.CLIENT);

        EffortlessBuilding.packetHandler.registerMessage(BlockBrokenMessage.MessageHandler.class, BlockBrokenMessage.class, 3, Side.SERVER);

        EffortlessBuilding.packetHandler.registerMessage(CancelModeMessage.MessageHandler.class, CancelModeMessage.class, 4, Side.SERVER);

        EffortlessBuilding.packetHandler.registerMessage(RequestLookAtMessage.MessageHandler.class, RequestLookAtMessage.class, 5, Side.CLIENT);

        proxy.preInit(event);
    }

    @EventHandler
    // Do your mod setup. Build whatever data structures you care about.
    // Register network handlers
    public void init(FMLInitializationEvent event)
    {
        ConfigManager.sync(MODID, Config.Type.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(EffortlessBuilding.instance, new RandomizerBagGuiHandler());

        proxy.init(event);
    }

    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
        CompatHelper.postInit();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandReach());
        proxy.serverStarting(event);
    }

    public static void log(String msg){
        logger.info(msg);
    }

    public static void log(EntityPlayer player, String msg){
        log(player, msg, false);
    }

    public static void log(EntityPlayer player, String msg, boolean actionBar){
        player.sendStatusMessage(new TextComponentString(msg), actionBar);
    }
}
