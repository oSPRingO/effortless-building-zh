package nl.requios.effortlessbuilding.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class RandomizerBagInventory implements IItemHandler {

    //Reference to NBT data
    private final ItemStack invItem;

    public static final int INV_SIZE = 5;

    private ItemStack[] inventory = new ItemStack[INV_SIZE];

    public RandomizerBagInventory(ItemStack invItem) {
        this.invItem = invItem;

        if (!invItem.hasTagCompound()) {
            invItem.setTagCompound(new NBTTagCompound());
        }

        readFromNBT(invItem.getTagCompound());
    }

    @Override
    public int getSlots() {
        return INV_SIZE;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack slotStack = getStackInSlot(slot);
        if (slotStack.getCount() == 0) {
            setInventorySlotContents(slot, stack);
            return null;
        }
        if (getSlotLimit(slot) - slotStack.getCount() < stack.getCount()) {
            //Not enough place remaining, split stack
            slotStack.setCount(getSlotLimit(slot));
            onInventoryChanged();
            stack.copy().shrink(getSlotLimit(slot) - slotStack.getCount());
            //TODO make proper
            return stack;
        } else {
            slotStack.grow(stack.getCount());
            onInventoryChanged();
            return null;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return stack;

        if (stack.getCount() > amount) {
            stack = stack.splitStack(amount);
            onInventoryChanged();
        } else {
            setInventorySlotContents(slot, null);
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    public void onInventoryChanged() {
        for (int i = 0; i < getSlotLimit(0); ++i) {
            if (getStackInSlot(i) != null && getStackInSlot(i).getCount() == 0) {
                inventory[i] = null;
            }
        }

        writeToNBT(invItem.getTagCompound());
    }

    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;

        if (stack != null && stack.getCount() > getSlotLimit(slot)) {
            stack.setCount(getSlotLimit(slot));
        }

        // Don't forget this line or your inventory will not be saved!
        onInventoryChanged();
    }

    public void readFromNBT(NBTTagCompound compound) {
        // Gets the custom taglist we wrote to this compound, if any
        // 1.7.2+ change to compound.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
        NBTTagList items = compound.getTagList("ItemInventory", 0);

        for (int i = 0; i < items.tagCount(); ++i) {
            // 1.7.2+ change to items.getCompoundTagAt(i)
            NBTTagCompound item = items.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");

            // Just double-checking that the saved slot index is within our inventory array bounds
            if (slot >= 0 && slot < getSlots()) {
                inventory[slot] = new ItemStack(item);
            }
        }
    }

    /**
     * A custom method to write our inventory to an ItemStack's NBT compound
     */
    public void writeToNBT(NBTTagCompound tagcompound) {
        // Create a new NBT Tag List to store itemstacks as NBT Tags
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSlots(); ++i) {
            // Only write stacks that contain items
            if (getStackInSlot(i) != null) {
                // Make a new NBT Tag Compound to write the itemstack and slot index to
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger("Slot", i);
                // Writes the itemstack in slot(i) to the Tag Compound we just made
                getStackInSlot(i).writeToNBT(item);

                // add the tag compound to our tag list
                items.appendTag(item);
            }
        }
        // Add the TagList to the ItemStack's Tag Compound with the name "ItemInventory"
        tagcompound.setTag("ItemInventory", items);
    }

}
