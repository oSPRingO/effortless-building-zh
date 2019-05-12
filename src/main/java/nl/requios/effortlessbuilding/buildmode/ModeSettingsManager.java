package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.ModeCapabilityManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.ModeSettingsMessage;

@Mod.EventBusSubscriber
public class ModeSettingsManager {

    //Retrieves the buildsettings of a player through the modifierCapability capability
    //Never returns null
    public static ModeSettings getModeSettings(EntityPlayer player) {
        if (player.hasCapability(ModeCapabilityManager.modeCapability, null)) {
            ModeCapabilityManager.IModeCapability capability = player.getCapability(
                    ModeCapabilityManager.modeCapability, null);
            if (capability.getModeData() == null) {
                capability.setModeData(new ModeSettings());
            }
            return capability.getModeData();
        }
        throw new IllegalArgumentException("Player does not have modeCapability capability");
    }

    public static void setModeSettings(EntityPlayer player, ModeSettings modeSettings) {
        if (player == null) {
            EffortlessBuilding.log("Cannot set buildsettings, player is null");
            return;
        }
        if (player.hasCapability(ModeCapabilityManager.modeCapability, null)) {
            ModeCapabilityManager.IModeCapability capability = player.getCapability(
                    ModeCapabilityManager.modeCapability, null);

            capability.setModeData(modeSettings);

            //Initialize new mode
            BuildModes.initializeMode(player);
        } else {
            EffortlessBuilding.log(player, "Saving buildsettings failed.");
        }
    }

    public static String sanitize(ModeSettings modeSettings, EntityPlayer player) {
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

    public static void handleNewPlayer(EntityPlayer player){
        if (getModeSettings(player) == null) {
            setModeSettings(player, new ModeSettings());
        }

        //Only on server
        if (!player.world.isRemote) {
            //Send to client
            ModeSettingsMessage msg = new ModeSettingsMessage(getModeSettings(player));
            EffortlessBuilding.packetHandler.sendTo(msg, (EntityPlayerMP) player);
        }
    }
}
