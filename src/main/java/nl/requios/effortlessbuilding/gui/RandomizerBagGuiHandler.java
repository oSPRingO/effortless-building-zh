package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.ItemHandlerCapabilityProvider;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import javax.annotation.Nullable;

public class RandomizerBagGuiHandler implements /*IGuiHandler, */IInteractionObject {
//    @Nullable
//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
//            // Use the player's held item to create the container
//            LazyOptional<IItemHandler> capabilityOptional = player.getHeldItemMainhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//
//            if (!capabilityOptional.isPresent()) {
//                capabilityOptional = player.getHeldItemOffhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//            }
//
//            return new RandomizerBagContainer(player.inventory, capabilityOptional.orElse(null));
//        }
//        return null;
//    }
//
//    @Nullable
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
//            // Use the player's held item to create the client-side gui container
//            LazyOptional<IItemHandler> capabilityOptional = player.getHeldItemMainhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//
//            if (!capabilityOptional.isPresent()) {
//                capabilityOptional = player.getHeldItemOffhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//            }
//
//            return new RandomizerBagGuiContainer(player.inventory, capabilityOptional.orElse(null));
//        }
//        return null;
//    }

    @OnlyIn(Dist.CLIENT)
    public static GuiScreen openGui(FMLPlayMessages.OpenContainer openContainer) {
        if (openContainer.getId().equals(EffortlessBuilding.RANDOMIZER_BAG_GUI)) {
            EntityPlayerSP player = Minecraft.getInstance().player;
            if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemRandomizerBag) {
                IItemHandler itemHandler = player.getHeldItem(EnumHand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                if (itemHandler != null) {
                    return new RandomizerBagGuiContainer(player.inventory, itemHandler);
                }
            }
        }
        return null;
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemRandomizerBag) {
            IItemHandler itemHandler = player.getHeldItem(EnumHand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            if (itemHandler != null) {
                return new RandomizerBagContainer(inventory, itemHandler);
            }
        }
        return null;
    }

    @Override
    public String getGuiID() {
        return EffortlessBuilding.RANDOMIZER_BAG_GUI.toString();
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentString("Randomizer Bag");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("Randomizer Bag");
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}
