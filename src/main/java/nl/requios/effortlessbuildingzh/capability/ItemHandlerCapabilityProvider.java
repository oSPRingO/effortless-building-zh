package nl.requios.effortlessbuildingzh.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import nl.requios.effortlessbuildingzh.item.ItemRandomizerBag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemHandlerCapabilityProvider implements ICapabilitySerializable<CompoundNBT> {
    IItemHandler itemHandler = new ItemStackHandler(ItemRandomizerBag.INV_SIZE);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> itemHandler));
    }

    @Override
    public CompoundNBT serializeNBT() {
        return ((ItemStackHandler) itemHandler).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ((ItemStackHandler) itemHandler).deserializeNBT(nbt);
    }
}
