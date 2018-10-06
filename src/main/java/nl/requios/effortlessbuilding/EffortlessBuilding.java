package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
import nl.requios.effortlessbuilding.capability.*;
import nl.requios.effortlessbuilding.gui.RandomizerBagGuiHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;
import nl.requios.effortlessbuilding.network.QuickReplaceMessage;
import nl.requios.effortlessbuilding.proxy.IProxy;
import org.apache.logging.log4j.Logger;

@Mod(modid = EffortlessBuilding.MODID, name = EffortlessBuilding.NAME, version = EffortlessBuilding.VERSION)
@Mod.EventBusSubscriber
public class EffortlessBuilding
{
    public static final String MODID = "effortlessbuilding";
    public static final String NAME = "Effortless Building";
    public static final String VERSION = "0.2";

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

    public static final Block[] BLOCKS = {
    };

    public static final Item[] ITEMS = {
            ITEM_RANDOMIZER_BAG
    };

    public static final int RANDOMIZER_BAG_GUI = 0;

    @EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        CapabilityManager.INSTANCE.register(BuildModifierCapability.IBuildModifier.class, new BuildModifierCapability.Storage(), BuildModifierCapability.BuildModifier.class);

        EffortlessBuilding.packetHandler.registerMessage(BuildSettingsMessage.MessageHandler.class, BuildSettingsMessage.class, 0, Side.SERVER);
        EffortlessBuilding.packetHandler.registerMessage(BuildSettingsMessage.MessageHandler.class, BuildSettingsMessage.class, 0, Side.CLIENT);

        EffortlessBuilding.packetHandler.registerMessage(QuickReplaceMessage.MessageHandler.class, QuickReplaceMessage.class, 1, Side.SERVER);
        EffortlessBuilding.packetHandler.registerMessage(QuickReplaceMessage.MessageHandler.class, QuickReplaceMessage.class, 1, Side.CLIENT);

        proxy.preInit(event);
    }

    @EventHandler
    // Do your mod setup. Build whatever data structures you care about.
    // Register network handlers
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(EffortlessBuilding.instance, new RandomizerBagGuiHandler());
        proxy.init(event);
    }

    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
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
