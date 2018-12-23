package nl.requios.effortlessbuilding.helper;

import net.minecraft.entity.player.EntityPlayer;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.BuildSettingsManager;

public class ReachHelper {
    public static int getMaxReach(EntityPlayer player) {
        if (player.isCreative()) return BuildConfig.reach.maxReachCreative;

        if (!BuildConfig.reach.enableReachUpgrades) return BuildConfig.reach.maxReachLevel3;

        //Check buildsettings for reachUpgrade
        int reachUpgrade = BuildSettingsManager.getBuildSettings(player).getReachUpgrade();
        switch (reachUpgrade) {
            case 0: return BuildConfig.reach.maxReachLevel0;
            case 1: return BuildConfig.reach.maxReachLevel1;
            case 2: return BuildConfig.reach.maxReachLevel2;
            case 3: return BuildConfig.reach.maxReachLevel3;
        }
        return BuildConfig.reach.maxReachLevel0;
    }

    public static boolean canBreakFar(EntityPlayer player) {
        return player.isCreative() || BuildConfig.survivalBalancers.breakFar;
    }
}
