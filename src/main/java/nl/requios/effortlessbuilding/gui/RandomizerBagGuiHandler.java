package nl.requios.effortlessbuilding.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;

import javax.annotation.Nullable;

public class RandomizerBagGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // Use the player's held item to create the container
            return new RandomizerBagContainer(player.inventory,
                    player.getHeldItem(EnumHand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // Use the player's held item to create the client-side gui container
            return new RandomizerBagGuiContainer(player.inventory,
                    player.getHeldItem(EnumHand.MAIN_HAND).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
        }
        return null;
    }
}
