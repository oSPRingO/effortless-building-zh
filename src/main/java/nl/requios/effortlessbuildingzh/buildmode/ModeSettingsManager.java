package nl.requios.effortlessbuildingzh.buildmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.capability.ModeCapabilityManager;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;
import nl.requios.effortlessbuildingzh.network.ModeSettingsMessage;
import nl.requios.effortlessbuildingzh.network.PacketHandler;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class ModeSettingsManager {

    //Retrieves the buildsettings of a player through the modifierCapability capability
    //Never returns null
    @Nonnull
    public static ModeSettings getModeSettings(PlayerEntity player) {
        LazyOptional<ModeCapabilityManager.IModeCapability> modeCapability =
                player.getCapability(ModeCapabilityManager.modeCapability, null);

        if (modeCapability.isPresent()) {
            ModeCapabilityManager.IModeCapability capability = modeCapability.orElse(null);
            if (capability.getModeData() == null) {
                capability.setModeData(new ModeSettings());
            }
            return capability.getModeData();
        }

        //Player does not have modeCapability capability
        //Return dummy settings
        return new ModeSettings();
//        throw new IllegalArgumentException("Player does not have modeCapability capability");
    }

    public static void setModeSettings(PlayerEntity player, ModeSettings modeSettings) {
        if (player == null) {
            EffortlessBuildingZh.log("Cannot set buildmode settings, player is null");
            return;
        }
        LazyOptional<ModeCapabilityManager.IModeCapability> modeCapability =
                player.getCapability(ModeCapabilityManager.modeCapability, null);

        modeCapability.ifPresent((capability) -> {
            capability.setModeData(modeSettings);

            BuildModes.initializeMode(player);
        });

        if (!modeCapability.isPresent()) {
            EffortlessBuildingZh.log(player, "Saving buildmode settings failed.");
        }
    }

    public static String sanitize(ModeSettings modeSettings, PlayerEntity player) {
        int maxReach = ReachHelper.getMaxReach(player);
        String error = "";

        //TODO sanitize

        return error;
    }

    public static class ModeSettings {
        private BuildModes.BuildModeEnum buildMode = BuildModes.BuildModeEnum.NORMAL;

        public ModeSettings() {
        }

        public ModeSettings(BuildModes.BuildModeEnum buildMode) {
            this.buildMode = buildMode;
        }

        public BuildModes.BuildModeEnum getBuildMode() {
            return this.buildMode;
        }

        public void setBuildMode(BuildModes.BuildModeEnum buildMode) {
            this.buildMode = buildMode;
        }
    }

    public static void handleNewPlayer(PlayerEntity player){
        //Makes sure player has mode settings (if it doesnt it will create it)
        getModeSettings(player);

        //Only on server
        if (!player.world.isRemote) {
            //Send to client
            ModeSettingsMessage msg = new ModeSettingsMessage(getModeSettings(player));
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), msg);
        }
    }
}
