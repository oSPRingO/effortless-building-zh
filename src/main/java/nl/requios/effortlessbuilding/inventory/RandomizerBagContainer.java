package nl.requios.effortlessbuilding.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RandomizerBagContainer extends Container {

    private final IItemHandler bagInventory;
    private final int sizeInventory;

    public RandomizerBagContainer(InventoryPlayer parInventoryPlayer, IItemHandler parIInventory) {
        bagInventory = parIInventory;
        sizeInventory = bagInventory.getSlots();
        for (int i = 0; i < sizeInventory; ++i) {
            this.addSlotToContainer(new SlotItemHandler(bagInventory, i, 80 + (18 * (i / 4)), 8 + (18 * (i % 4))));
        }

        // add player inventory slots
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(parInventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // add hotbar slots
        for (i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(parInventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn,
                                         int slotIndex) {
        ItemStack itemStack1 = null;
        Slot slot = inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack1 = itemStack2.copy();

            if (slotIndex == 0) {
                if (!mergeItemStack(itemStack2, sizeInventory,
                        sizeInventory + 36, true)) {
                    return null;
                }

                slot.onSlotChange(itemStack2, itemStack1);
            } else if (slotIndex != 1) {
                if (slotIndex >= sizeInventory
                        && slotIndex < sizeInventory + 27) // player inventory slots
                {
                    if (!mergeItemStack(itemStack2, sizeInventory + 27,
                            sizeInventory + 36, false)) {
                        return null;
                    }
                } else if (slotIndex >= sizeInventory + 27
                        && slotIndex < sizeInventory + 36
                        && !mergeItemStack(itemStack2, sizeInventory + 1,
                        sizeInventory + 27, false)) // hotbar slots
                {
                    return null;
                }
            } else if (!mergeItemStack(itemStack2, sizeInventory,
                    sizeInventory + 36, false)) {
                return null;
            }

            if (itemStack2.getCount() == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemStack2.getCount() == itemStack1.getCount()) {
                return null;
            }

            slot.onTake(playerIn, itemStack2);
        }

        return itemStack1;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        // this will prevent the player from interacting with the item that opened the inventory:
        if (slot >= 0 && getSlot(slot) != null && getSlot(slot).getStack() == player.getHeldItem(EnumHand.MAIN_HAND)) {
            return null;
        }
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }
}
