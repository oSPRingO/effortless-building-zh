package nl.requios.effortlessbuilding.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;

import javax.annotation.Nullable;

public class RandomizerBagGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // Use the player's held item to create the container
            IItemHandler capability = player.getHeldItemMainhand().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ?
                    player.getHeldItemMainhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) :
                    player.getHeldItemOffhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            return new RandomizerBagContainer(player.inventory, capability);
        }
        return null;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // Use the player's held item to create the client-side gui container
            IItemHandler capability = player.getHeldItemMainhand().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ?
                    player.getHeldItemMainhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) :
                    player.getHeldItemOffhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            return new RandomizerBagGuiContainer(player.inventory, capability);
        }
        return null;
    }
}
