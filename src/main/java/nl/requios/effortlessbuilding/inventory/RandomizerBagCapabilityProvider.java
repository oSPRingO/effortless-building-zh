package nl.requios.effortlessbuilding.inventory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RandomizerBagCapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
    IItemHandler itemHandler = new ItemStackHandler(5);

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) itemHandler;
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return ((ItemStackHandler)itemHandler).serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ((ItemStackHandler)itemHandler).deserializeNBT(nbt);
    }
}
