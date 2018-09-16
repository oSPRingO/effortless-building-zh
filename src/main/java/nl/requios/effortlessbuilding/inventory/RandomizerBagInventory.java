package nl.requios.effortlessbuilding.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class RandomizerBagInventory implements IInventory {

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
    public int getSizeInventory() {
        return INV_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null)
        {
            ItemStack itemstack;

            if (inventory[slot].getCount() <= amount)
            {
                itemstack = inventory[slot];
                inventory[slot] = null;
                return itemstack;
            }
            else
            {
                itemstack = inventory[slot].splitStack(amount);

                if (inventory[slot].getCount() == 0)
                {
                    inventory[slot] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    public void onInventoryChanged() {
        for (int i = 0; i < getFieldCount(); ++i) {
            if (getStackInSlot(i) != null && getStackInSlot(i).getCount() == 0) {
                inventory[i] = null;
            }
        }

        writeToNBT(invItem.getTagCompound());
    }

    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;

        if (stack != null && stack.getCount() > getInventoryStackLimit())
        {
            stack.setCount(getInventoryStackLimit());
        }

        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        //variables
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < inventory.length; ++i)
        {
            inventory[i] = null;
        }
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
            if (slot >= 0 && slot < getFieldCount()) {
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

        for (int i = 0; i < getSizeInventory(); ++i) {
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

    @Override
    public String getName() {
        return "Testname";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("Testname");
    }
}
