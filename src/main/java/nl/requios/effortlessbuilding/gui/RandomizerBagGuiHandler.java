package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import javax.annotation.Nullable;
TODO 1.14 GUI
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
    public static Screen openGui(FMLPlayMessages.OpenContainer openContainer) {
        if (openContainer.getId().equals(EffortlessBuilding.RANDOMIZER_BAG_GUI)) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof ItemRandomizerBag) {
                IItemHandler itemHandler = player.getHeldItem(Hand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                if (itemHandler != null) {
                    return new RandomizerBagGuiContainer(player.inventory, itemHandler);
                }
            }
        }
        return null;
    }

    @Override
    public Container createContainer(PlayerInventory inventory, PlayerEntity player) {
        if (player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof ItemRandomizerBag) {
            IItemHandler itemHandler = player.getHeldItem(Hand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
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
        return new StringTextComponent("Randomizer Bag");
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Randomizer Bag");
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}
