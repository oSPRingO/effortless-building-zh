package nl.requios.effortlessbuilding.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.inventory.RandomizerBagContainer;

import javax.annotation.Nullable;

public class RandomizerGuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // Use the player's held item to create the inventory
            return new RandomizerBagContainer(player.inventory, new RandomizerBagInventory(player.getHeldItem(EnumHand.MAIN_HAND)));
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == EffortlessBuilding.RANDOMIZER_BAG_GUI) {
            // We have to cast the new container as our custom class
            // and pass in currently held item for the inventory
            return new RandomizerBagGuiContainer(player.inventory, new RandomizerBagInventory(player.getHeldItem(EnumHand.MAIN_HAND)));
        }
        return null;
    }
}
