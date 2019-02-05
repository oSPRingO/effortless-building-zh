package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
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
        private BuildModes.BuildModeEnum buildMode = BuildModes.BuildModeEnum.Normal;

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

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        handleNewPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        handleNewPlayer(player);
    }

    private static void handleNewPlayer(EntityPlayer player){
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
