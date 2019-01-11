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
            error += "Mirror size has to be at least 1. This has been corrected. ";
        }
        if (m.getReach() > maxReach) {
            m.radius = maxReach / 2;
            error += "Mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to "+ (maxReach / 2) + ". ";
        }

        //Array settings
        Array.ArraySettings a = buildSettings.getArraySettings();
        if (a.count < 0) {
            a.count = 0;
            error += "Array count may not be negative. It has been reset to 0.";
        }

        if (a.getReach() > maxReach) {
            a.count = 0;
            error += "Array exceeds your maximum reach of " + maxReach + ". Array count has been reset to 0. ";
        }

        //Radial mirror settings
        RadialMirror.RadialMirrorSettings r = buildSettings.getRadialMirrorSettings();
        if (r.slices < 2) {
            r.slices = 2;
            error += "Radial mirror needs to have at least 2 slices. Slices has been set to 2.";
        }

        if (r.radius < 1) {
            r.radius = 1;
            error += "Radial mirror radius has to be at least 1. This has been corrected. ";
        }
        if (r.getReach() > maxReach) {
            r.radius = maxReach / 2;
            error += "Radial mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to "+ (maxReach / 2) + ". ";
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
        private RadialMirror.RadialMirrorSettings radialMirrorSettings;
        private boolean quickReplace = false;
        private int reachUpgrade = 0;

        public BuildSettings() {
            mirrorSettings = new Mirror.MirrorSettings();
            arraySettings = new Array.ArraySettings();
            radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
        }

        public BuildSettings(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings,
                             RadialMirror.RadialMirrorSettings radialMirrorSettings, boolean quickReplace, int reachUpgrade) {
            this.mirrorSettings = mirrorSettings;
            this.arraySettings = arraySettings;
            this.radialMirrorSettings = radialMirrorSettings;
            this.quickReplace = quickReplace;
            this.reachUpgrade = reachUpgrade;
        }

        public Mirror.MirrorSettings getMirrorSettings() {
            if (this.mirrorSettings == null) this.mirrorSettings = new Mirror.MirrorSettings();
            return this.mirrorSettings;
        }

        public void setMirrorSettings(Mirror.MirrorSettings mirrorSettings) {
            if (mirrorSettings == null) return;
            this.mirrorSettings = mirrorSettings;
        }

        public Array.ArraySettings getArraySettings() {
            if (this.arraySettings == null) this.arraySettings = new Array.ArraySettings();
            return this.arraySettings;
        }

        public void setArraySettings(Array.ArraySettings arraySettings) {
            if (arraySettings == null) return;
            this.arraySettings = arraySettings;
        }

        public RadialMirror.RadialMirrorSettings getRadialMirrorSettings() {
            if (this.radialMirrorSettings == null) this.radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
            return this.radialMirrorSettings;
        }

        public void setRadialMirrorSettings(RadialMirror.RadialMirrorSettings radialMirrorSettings) {
            if (radialMirrorSettings == null) return;
            this.radialMirrorSettings = radialMirrorSettings;
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

