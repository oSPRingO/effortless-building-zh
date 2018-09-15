package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.requios.effortlessbuilding.capability.BuildModifierCapability;

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
            event.addCapability(new ResourceLocation(EffortlessBuilding.MODID, "BuildModifier"), new BuildModifierCapability.Provider());
        }
    }


    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        QuickReplace.onBlockPlaced(event);
        if (event.isCanceled()) return;
        Array.onBlockPlaced(event);
        Mirror.onBlockPlaced(event);
    }


    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        Array.onBlockBroken(event);
        Mirror.onBlockBroken(event);
    }

}
