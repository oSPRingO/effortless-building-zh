package nl.requios.effortlessbuilding.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;

public class ReachHelper {
	public static int getMaxReach(PlayerEntity player) {
		if (player.isCreative()) return BuildConfig.reach.maxReachCreative.get();

		if (!BuildConfig.reach.enableReachUpgrades.get()) return BuildConfig.reach.maxReachLevel3.get();

		//Check buildsettings for reachUpgrade
		int reachUpgrade = ModifierSettingsManager.getModifierSettings(player).getReachUpgrade();
		switch (reachUpgrade) {
			case 0:
				return BuildConfig.reach.maxReachLevel0.get();
			case 1:
				return BuildConfig.reach.maxReachLevel1.get();
			case 2:
				return BuildConfig.reach.maxReachLevel2.get();
			case 3:
				return BuildConfig.reach.maxReachLevel3.get();
		}
		return BuildConfig.reach.maxReachLevel0.get();
	}

	public static int getPlacementReach(PlayerEntity player) {
		return getMaxReach(player) / 4;
	}

	public static int getMaxBlocksPlacedAtOnce(PlayerEntity player) {
		if (player.isCreative()) return 1000000;
		return MathHelper.ceil(Math.pow(getMaxReach(player), 1.6));
		//Level 0: 121
		//Level 1: 523
		//Level 2: 1585
		//Level 3: 4805
	}

	public static int getMaxBlocksPerAxis(PlayerEntity player) {
		if (player.isCreative()) return 2000;
		return MathHelper.ceil(getMaxReach(player) * 0.3);
		//Level 0: 6
		//Level 1: 15
		//Level 2: 30
		//Level 3: 60
	}

	public static boolean canBreakFar(PlayerEntity player) {
		return player.isCreative() || BuildConfig.survivalBalancers.breakFar.get();
	}
}
