package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.capability.ModeCapabilityManager;
import nl.requios.effortlessbuilding.capability.ModifierCapabilityManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;
import nl.requios.effortlessbuilding.network.RequestLookAtMessage;

import java.util.List;

@Mod.EventBusSubscriber
public class EventHandler
{

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(EffortlessBuilding.BLOCKS);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(EffortlessBuilding.ITEMS);

        for (Block block : EffortlessBuilding.BLOCKS)
        {
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation(EffortlessBuilding.MODID, "BuildModifier"), new ModifierCapabilityManager.Provider());
            event.addCapability(new ResourceLocation(EffortlessBuilding.MODID, "BuildMode"), new ModeCapabilityManager.Provider());
        }
    }

    @SubscribeEvent
    public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(EffortlessBuilding.MODID))
        {
            ConfigManager.sync(EffortlessBuilding.MODID, Config.Type.INSTANCE);
        }
    }

//    @SubscribeEvent
//    public static void onServerTick(TickEvent.ServerTickEvent event) {
//
//    }

    @SubscribeEvent
    //Only called serverside (except with lilypads...)
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;

        //Cancel event if necessary
        EntityPlayer player = event.getPlayer();
        BuildModes.BuildModeEnum buildMode = ModeSettingsManager.getModeSettings(player).getBuildMode();
        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);

        if (buildMode != BuildModes.BuildModeEnum.Normal || modifierSettings.doQuickReplace()) {
            event.setCanceled(true);
        } else {
            //Normal mode, let vanilla handle block placing
            //But modifiers and QuickReplace should still work

            //Send message to client, which sends message back with raytrace info
            EffortlessBuilding.packetHandler.sendTo(new RequestLookAtMessage(), (EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;

        //Cancel event if necessary
        //If cant break far then dont cancel event ever
        BuildModes.BuildModeEnum buildMode = ModeSettingsManager.getModeSettings(event.getPlayer()).getBuildMode();
        if (buildMode != BuildModes.BuildModeEnum.Normal && ReachHelper.canBreakFar(event.getPlayer())) {
            event.setCanceled(true);
        } else {
            //Normal mode, let vanilla handle block breaking
            //But modifiers and QuickReplace should still work
            //Dont break the original block yourself, otherwise Tinkers Hammer and Veinminer won't work
            BuildModes.onBlockBroken(event.getPlayer(), event.getPos(), false);
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        //Disable if config says so
        if (!BuildConfig.survivalBalancers.increasedMiningTime) return;

        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;
        BlockPos pos = event.getPos();

        //EffortlessBuilding.log(player, String.valueOf(event.getNewSpeed()));

        float originalBlockHardness = event.getState().getBlockHardness(world, pos);
        if (originalBlockHardness < 0) return; //Dont break bedrock
        float totalBlockHardness = 0;
        //get coordinates
        List<BlockPos> coordinates = BuildModifiers.findCoordinates(player, pos);
        for (int i = 1; i < coordinates.size(); i++) {
            BlockPos coordinate = coordinates.get(i);
            //get existing blockstates at those coordinates
            IBlockState blockState = world.getBlockState(coordinate);
            //add hardness for each blockstate, if can break
            if (SurvivalHelper.canBreak(world, player, coordinate))
                totalBlockHardness += blockState.getBlockHardness(world, coordinate);
        }

        //Grabbing percentage from config
        float percentage = (float) BuildConfig.survivalBalancers.miningTimePercentage / 100;
        totalBlockHardness *= percentage;
        totalBlockHardness += originalBlockHardness;

        float newSpeed = event.getOriginalSpeed() / totalBlockHardness * originalBlockHardness;
        if (Float.isNaN(newSpeed) || newSpeed == 0f) newSpeed = 1f;
        event.setNewSpeed(newSpeed);

        //EffortlessBuilding.log(player, String.valueOf(event.getNewSpeed()));
    }
}
