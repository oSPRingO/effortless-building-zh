package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.ModifierCapabilityManager;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.ModifierSettingsMessage;

@Mod.EventBusSubscriber
public class ModifierSettingsManager {

    //Retrieves the buildsettings of a player through the modifierCapability capability
    //Never returns null
    public static ModifierSettings getModifierSettings(EntityPlayer player){
        if (player.hasCapability(ModifierCapabilityManager.modifierCapability, null)) {
            ModifierCapabilityManager.IModifierCapability capability = player.getCapability(
                    ModifierCapabilityManager.modifierCapability, null);
            if (capability.getModifierData() == null) {
                capability.setModifierData(new ModifierSettings());
            }
            return capability.getModifierData();
        }
        throw new IllegalArgumentException("Player does not have modifierCapability capability");
    }

    public static void setModifierSettings(EntityPlayer player, ModifierSettings modifierSettings) {
        if (player == null) {
            EffortlessBuilding.log("Cannot set buildsettings, player is null");
            return;
        }
        if (player.hasCapability(ModifierCapabilityManager.modifierCapability, null)) {
            ModifierCapabilityManager.IModifierCapability capability = player.getCapability(
                    ModifierCapabilityManager.modifierCapability, null);
            capability.setModifierData(modifierSettings);
        } else {
            EffortlessBuilding.log(player, "Saving buildsettings failed.");
        }
    }

    public static String sanitize(ModifierSettings modifierSettings, EntityPlayer player) {
        int maxReach = ReachHelper.getMaxReach(player);
        String error = "";

        //Mirror settings
        Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
        if (m.radius < 1) {
            m.radius = 1;
            error += "Mirror size has to be at least 1. This has been corrected. ";
        }
        if (m.getReach() > maxReach) {
            m.radius = maxReach / 2;
            error += "Mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to "+ (maxReach / 2) + ". ";
        }

        //Array settings
        Array.ArraySettings a = modifierSettings.getArraySettings();
        if (a.count < 0) {
            a.count = 0;
            error += "Array count may not be negative. It has been reset to 0.";
        }

        if (a.getReach() > maxReach) {
            a.count = 0;
            error += "Array exceeds your maximum reach of " + maxReach + ". Array count has been reset to 0. ";
        }

        //Radial mirror settings
        RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
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
        if (modifierSettings.reachUpgrade < 0) {
            modifierSettings.reachUpgrade = 0;
        }
        if (modifierSettings.reachUpgrade > 3) {
            modifierSettings.reachUpgrade = 3;
        }

        return error;
    }

    public static class ModifierSettings {
        private Mirror.MirrorSettings mirrorSettings;
        private Array.ArraySettings arraySettings;
        private RadialMirror.RadialMirrorSettings radialMirrorSettings;
        private boolean quickReplace = false;
        private int reachUpgrade = 0;

        public ModifierSettings() {
            mirrorSettings = new Mirror.MirrorSettings();
            arraySettings = new Array.ArraySettings();
            radialMirrorSettings = new RadialMirror.RadialMirrorSettings();
        }

        public ModifierSettings(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings,
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
            //Set mirror radius to max
            int reach = 10;
            switch (reachUpgrade) {
                case 0: reach = BuildConfig.reach.maxReachLevel0; break;
                case 1: reach =  BuildConfig.reach.maxReachLevel1; break;
                case 2: reach =  BuildConfig.reach.maxReachLevel2; break;
                case 3: reach =  BuildConfig.reach.maxReachLevel3; break;
            }

            EffortlessBuilding.log("before "+this.mirrorSettings.radius);

            if (this.mirrorSettings != null)
                this.mirrorSettings.radius = reach / 2;
            if (this.radialMirrorSettings != null)
                this.radialMirrorSettings.radius = reach / 2;

            EffortlessBuilding.log("after "+this.mirrorSettings.radius);
        }
    }

    public static void handleNewPlayer(EntityPlayer player){
        if (getModifierSettings(player) == null) {
            setModifierSettings(player, new ModifierSettings());
        }

        //Only on server
        if (!player.world.isRemote) {
            //Send to client
            ModifierSettingsMessage msg = new ModifierSettingsMessage(getModifierSettings(player));
            EffortlessBuilding.packetHandler.sendTo(msg, (EntityPlayerMP) player);
        }
    }
}

