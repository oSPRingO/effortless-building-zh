package nl.requios.effortlessbuilding.helper;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionSerializer;
import nl.requios.effortlessbuilding.BuildConfig;

import java.util.function.BooleanSupplier;

public class ReachConditionFactory implements IConditionSerializer {
    @Override
    public BooleanSupplier parse(JsonObject json) {
        return () -> BuildConfig.reach.enableReachUpgrades.get();
    }
}
