package nl.requios.effortlessbuilding.compatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

public class CompatHelper {
    // Get a handle to the dank null item instance. This will remain null if the mod doesn't load
    // and all checks will fail, so it works.
    @GameRegistry.ObjectHolder("danknull:dank_null")
    public static final Item dankNullItem = null;

    public static IChiselsAndBitsProxy chiselsAndBitsProxy;

    public static void postInit() {
        if (Loader.isModLoaded("chiselsandbits")) {
            // reflection to avoid hard dependency
            try {
                chiselsAndBitsProxy = Class.forName("nl.requios.effortlessbuilding.compatibility.ActiveChiselsAndBitsProxy").asSubclass(ActiveChiselsAndBitsProxy.class).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            chiselsAndBitsProxy = new DummyChiselsAndBitsProxy();
        }
    }

    // Check if the item given is a proxy for blocks. For now, we check for the randomizer bag,
    // /dank/null, or plain old blocks.
    public static boolean isItemBlockProxy(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock)
            return true;
        if ((item instanceof ItemRandomizerBag))
            return true;
        if (item == dankNullItem)
            return true;
        return false;
    }

    // Get the block to be placed by this proxy. For the /dank/null, it's the slot stack
    // pointed to by nbt integer selectedIndex.
    public static ItemStack getItemBlockFromStack(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ItemBlock)
            return stack;

        if (item instanceof ItemRandomizerBag)
            return ItemRandomizerBag.pickRandomStack(ItemRandomizerBag.getBagInventory(stack));

        if(item == dankNullItem) {
            int index = 0;
            if(stack.hasTagCompound() && stack.getTagCompound().hasKey("selectedIndex"))
                index = stack.getTagCompound().getInteger("selectedIndex");
            return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(index);
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getItemBlockByState(ItemStack stack, IBlockState state) {
        Item blockItem = Item.getItemFromBlock(state.getBlock());
        if(stack.getItem() instanceof ItemBlock)
            return stack;
        else if(stack.getItem() instanceof ItemRandomizerBag) {
            IItemHandler bagInventory = ItemRandomizerBag.getBagInventory(stack);
            return ItemRandomizerBag.findStack(bagInventory, blockItem);
        } else if(stack.getItem() == dankNullItem) {
            int index = itemHandlerSlotForItem(stack, blockItem);
            if(index >= 0)
                return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(index);
        }
        return ItemStack.EMPTY;
    }

    // Handle IItemHandler slot stacks not being modifiable. We must call IItemHandler#extractItem,
    // because the ItemStack returned by IItemHandler#getStackInSlot isn't modifiable.
    public static void shrinkStack(ItemStack origStack, ItemStack curStack) {
        if(origStack.getItem() == dankNullItem) {
            int index = itemHandlerSlotForItem(origStack, curStack.getItem());
            origStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(index, 1, false);
        } else
            curStack.shrink(1);
    }

    private static int itemHandlerSlotForItem(ItemStack stack, Item blockItem) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for(int i = 0; i < handler.getSlots(); i++) {
            ItemStack ref = handler.getStackInSlot(i);
            if(ref.getItem() instanceof ItemBlock)
                if(ref.getItem() == blockItem)
                    return i;
        }
        return -1;
    }

}
