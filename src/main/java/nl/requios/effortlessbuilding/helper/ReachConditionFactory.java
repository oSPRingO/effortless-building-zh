package nl.requios.effortlessbuilding.helper;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import nl.requios.effortlessbuilding.BuildConfig;

public class ReachConditionFactory implements IIngredientSerializer {
    @Override
    public Ingredient parse(PacketBuffer buffer) {
        if (BuildConfig.reach.enableReachUpgrades.get())
            return Ingredient.read(buffer);
        return Ingredient.EMPTY;
    }

    @Override
    public Ingredient parse(JsonObject json) {
        if (BuildConfig.reach.enableReachUpgrades.get())
            return Ingredient.deserialize(json);
        return Ingredient.EMPTY;
    }

    @Override
    public void write(PacketBuffer buffer, Ingredient ingredient) {
        ingredient.write(buffer);
    }
}
