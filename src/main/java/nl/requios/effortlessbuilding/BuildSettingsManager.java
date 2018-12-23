package nl.requios.effortlessbuilding;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import nl.requios.effortlessbuilding.capability.BuildModifierCapabilityManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;

@Mod.EventBusSubscriber
public class BuildSettingsManager {

    //Retrieves the buildsettings of a player through the buildModifierCapability capability
    //Never returns null
    public static BuildSettings getBuildSettings(EntityPlayer player){
        if (player.hasCapability(BuildModifierCapabilityManager.buildModifierCapability, null)) {
            BuildModifierCapabilityManager.IBuildModifierCapability capability = player.getCapability(
                    BuildModifierCapabilityManager.buildModifierCapability, null);
            if (capability.getBuildModifierData() == null) {
                capability.setBuildModifierData(new BuildSettings());
            }
            return capability.getBuildModifierData();
        }
        throw new IllegalArgumentException("Player does not have buildModifierCapability capability");
    }

    public static void setBuildSettings(EntityPlayer player, BuildSettings buildSettings) {
        if (player == null) {
            EffortlessBuilding.log("Cannot set buildsettings, player is null");
            return;
        }
        if (player.hasCapability(BuildModifierCapabilityManager.buildModifierCapability, null)) {
            BuildModifierCapabilityManager.IBuildModifierCapability capability = player.getCapability(
                    BuildModifierCapabilityManager.buildModifierCapability, null);
            capability.setBuildModifierData(buildSettings);
        } else {
            EffortlessBuilding.log(player, "Saving buildsettings failed.");
        }
    }

    public static String sanitize(BuildSettings buildSettings, EntityPlayer player) {
        int maxReach = ReachHelper.getMaxReach(player);
        String error = "";

        //Mirror settings
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        if (m.radius < 1) {
            m.radius = 1;
            error += "Mirror size is too small. Size has been set to 1. ";
        }
        if (m.getReach() > maxReach) {
            m.radius = maxReach / 2;
            error += "Mirror exceeds your maximum reach. Reach has been set to max. ";
        }

        //Array settings
        Array.ArraySettings a = buildSettings.getArraySettings();
        if (a.count < 0) {
            a.count = 0;
            error += "Array count cannot be negative. Count has been set to 0. ";
        }

        if (a.getReach() > maxReach) {
            a.count = 0;
            error += "Array exceeds your maximum reach. Count has been set to 0. ";
        }

        //Other
        if (buildSettings.reachUpgrade < 0) {
            buildSettings.reachUpgrade = 0;
        }
        if (buildSettings.reachUpgrade > 3) {
            buildSettings.reachUpgrade = 3;
        }

        return error;
    }

    public static class BuildSettings {
        private Mirror.MirrorSettings mirrorSettings;
        private Array.ArraySettings arraySettings;
        private boolean quickReplace = false;
        private int reachUpgrade = 0;

        public BuildSettings() {
            mirrorSettings = new Mirror.MirrorSettings();
            arraySettings = new Array.ArraySettings();
        }

        public BuildSettings(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings, boolean quickReplace, int reachUpgrade) {
            this.mirrorSettings = mirrorSettings;
            this.arraySettings = arraySettings;
            this.quickReplace = quickReplace;
            this.reachUpgrade = reachUpgrade;
        }

        public Mirror.MirrorSettings getMirrorSettings() {
            return mirrorSettings;
        }

        public void setMirrorSettings(Mirror.MirrorSettings mirrorSettings) {
            this.mirrorSettings = mirrorSettings;
        }

        public Array.ArraySettings getArraySettings() {
            return arraySettings;
        }

        public void setArraySettings(Array.ArraySettings arraySettings) {
            this.arraySettings = arraySettings;
        }

        public boolean doQuickReplace() {
            return quickReplace;
        }

        public void setQuickReplace(boolean quickReplace) {
            this.quickReplace = quickReplace;
        }

        public int getReachUpgrade() {
            return reachUpgrade;
        }

        public void setReachUpgrade(int reachUpgrade) {
            this.reachUpgrade = reachUpgrade;
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
        if (getBuildSettings(player) == null) {
            setBuildSettings(player, new BuildSettings());
        }

        //Only on server
        if (!player.world.isRemote) {
            //Send to client
            BuildSettingsMessage msg = new BuildSettingsMessage(getBuildSettings(player));
            EffortlessBuilding.packetHandler.sendTo(msg, (EntityPlayerMP) player);
        }
    }
}

