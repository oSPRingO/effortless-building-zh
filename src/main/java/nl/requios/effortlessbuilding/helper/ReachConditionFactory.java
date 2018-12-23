package nl.requios.effortlessbuilding.helper;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import nl.requios.effortlessbuilding.BuildConfig;

import java.util.function.BooleanSupplier;

public class ReachConditionFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> BuildConfig.reach.enableReachUpgrades;
    }
}
