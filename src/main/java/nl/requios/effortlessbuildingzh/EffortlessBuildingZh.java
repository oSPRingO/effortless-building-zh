package nl.requios.effortlessbuildingzh;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.requios.effortlessbuildingzh.capability.ModeCapabilityManager;
import nl.requios.effortlessbuildingzh.capability.ModifierCapabilityManager;
import nl.requios.effortlessbuildingzh.command.CommandReach;
import nl.requios.effortlessbuildingzh.compatibility.CompatHelper;
import nl.requios.effortlessbuildingzh.gui.RandomizerBagContainer;
import nl.requios.effortlessbuildingzh.item.ItemRandomizerBag;
import nl.requios.effortlessbuildingzh.item.ItemReachUpgrade1;
import nl.requios.effortlessbuildingzh.item.ItemReachUpgrade2;
import nl.requios.effortlessbuildingzh.item.ItemReachUpgrade3;
import nl.requios.effortlessbuildingzh.network.PacketHandler;
import nl.requios.effortlessbuildingzh.proxy.ClientProxy;
import nl.requios.effortlessbuildingzh.proxy.IProxy;
import nl.requios.effortlessbuildingzh.proxy.ServerProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EffortlessBuildingZh.MODID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class EffortlessBuildingZh
{
    public static final String MODID = "effortlessbuildingzh";
    public static final String NAME = "Effortless Building 汉化版";
    public static final String VERSION = "1.15.2-2.21";

    public static EffortlessBuildingZh instance;

    public static final Logger logger = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

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

    public static final ContainerType<RandomizerBagContainer> RANDOMIZER_BAG_CONTAINER = register("randomizer_bag", RandomizerBagContainer::new);
    public static final ResourceLocation RANDOMIZER_BAG_GUI = new ResourceLocation(EffortlessBuildingZh.MODID, "randomizer_bag");

    public EffortlessBuildingZh() {
        instance = this;

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the clientSetup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        //Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BuildConfig.spec);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(ModifierCapabilityManager.IModifierCapability.class, new ModifierCapabilityManager.Storage(), ModifierCapabilityManager.ModifierCapability::new);
        CapabilityManager.INSTANCE.register(ModeCapabilityManager.IModeCapability.class, new ModeCapabilityManager.Storage(), ModeCapabilityManager.ModeCapability::new);

        PacketHandler.register();

        //TODO 1.13 config
//        ConfigManager.sync(MODID, Config.Type.INSTANCE);

        proxy.setup(event);

        CompatHelper.setup();
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {

        proxy.clientSetup(event);
    }

    @SubscribeEvent
    public void enqueueIMC(final InterModEnqueueEvent event) {

        // some example code to dispatch IMC to another mod
//        InterModComms.sendTo("examplemod", "helloworld", () -> { logger.info("Hello world from the MDK"); return "Hello world";});
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event) {

        // some example code to receive and process InterModComms from other mods
//        logger.info("Got IMC {}", event.getIMCStream().
//                map(m->m.getMessageSupplier().get()).
//                collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        CommandReach.register(event.getCommandDispatcher());
    }

    private static <T extends Container> ContainerType<T> register(String key, ContainerType.IFactory<T> factory) {
        return Registry.register(Registry.MENU, key, new ContainerType<>(factory));
    }

    public static void log(String msg){
        logger.info(msg);
    }

    public static void log(PlayerEntity player, String msg){
        log(player, msg, false);
    }

    public static void log(PlayerEntity player, String msg, boolean actionBar){
        player.sendStatusMessage(new StringTextComponent(msg), actionBar);
    }

    //Log with translation supported, call either on client or server (which then sends a message)
    public static void logTranslate(PlayerEntity player, String prefix, String translationKey, String suffix, boolean actionBar){
        proxy.logTranslate(player, prefix, translationKey, suffix, actionBar);
    }
}
