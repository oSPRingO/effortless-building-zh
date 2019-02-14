package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.requios.effortlessbuilding.capability.BuildModifierCapabilityManager;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;

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
            event.addCapability(new ResourceLocation(EffortlessBuilding.MODID, "BuildModifier"), new BuildModifierCapabilityManager.Provider());
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
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        //Still call it to cancel event
        BuildModifiers.onBlockPlaced(event);
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        BuildModifiers.onBlockBroken(event);
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
