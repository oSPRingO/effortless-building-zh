package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import nl.requios.effortlessbuilding.capability.BuildModifierCapability;

@Mod.EventBusSubscriber
public class EventHandler
{
    private static boolean placedBlock = false;
    private static BlockEvent.PlaceEvent placeEvent = null;

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
            event.addCapability(new ResourceLocation(EffortlessBuilding.MODID, "BuildModifier"), new BuildModifierCapability.Provider());
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(EffortlessBuilding.MODID))
        {
            ConfigManager.sync(EffortlessBuilding.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (placedBlock) {
            placedBlock = false;

            Mirror.onBlockPlaced(placeEvent);
            Array.onBlockPlaced(placeEvent);
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (QuickReplace.onBlockPlaced(event)) {
            event.setCanceled(true);
            return;
        }

        //Delay mirror and array by a tick so we can edit the held itemstack
        //Otherwise the itemstack count would be overridden by ItemBlock#onItemUse
        placedBlock = true;
        placeEvent = event;
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        Mirror.onBlockBroken(event);
        Array.onBlockBroken(event);
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
        float totalBlockHardness = 0;
        totalBlockHardness += Mirror.getTotalBlockHardness(world, player, pos);
        totalBlockHardness += Array.getTotalBlockHardness(world, player, pos);

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
