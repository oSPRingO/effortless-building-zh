package nl.requios.effortlessbuildingzh.buildmodifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import nl.requios.effortlessbuildingzh.BuildConfig;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.capability.ModifierCapabilityManager;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;
import nl.requios.effortlessbuildingzh.network.ModifierSettingsMessage;
import nl.requios.effortlessbuildingzh.network.PacketHandler;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class ModifierSettingsManager {

    //Retrieves the buildsettings of a player through the modifierCapability capability
    //Never returns null
    @Nonnull
    public static ModifierSettings getModifierSettings(PlayerEntity player){
        LazyOptional<ModifierCapabilityManager.IModifierCapability> modifierCapability =
                player.getCapability(ModifierCapabilityManager.modifierCapability, null);

        if (modifierCapability.isPresent()) {
            ModifierCapabilityManager.IModifierCapability capability = modifierCapability.orElse(null);
            if (capability.getModifierData() == null) {
                capability.setModifierData(new ModifierSettings());
            }
            return capability.getModifierData();
        }

        //Player does not have modifierCapability capability
        //Return dummy settings
        return new ModifierSettings();
//        throw new IllegalArgumentException("Player does not have modifierCapability capability");
    }

    public static void setModifierSettings(PlayerEntity player, ModifierSettings modifierSettings) {
        if (player == null) {
            EffortlessBuildingZh.log("Cannot set buildsettings, player is null");
            return;
        }

        LazyOptional<ModifierCapabilityManager.IModifierCapability> modifierCapability =
                player.getCapability(ModifierCapabilityManager.modifierCapability, null);

        modifierCapability.ifPresent((capability) -> {
            capability.setModifierData(modifierSettings);
        });

        if (!modifierCapability.isPresent()) {
            EffortlessBuildingZh.log(player, "Saving buildsettings failed.");
        }
    }

    public static String sanitize(ModifierSettings modifierSettings, PlayerEntity player) {
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
                case 0: reach = BuildConfig.reach.maxReachLevel0.get(); break;
                case 1: reach =  BuildConfig.reach.maxReachLevel1.get(); break;
                case 2: reach =  BuildConfig.reach.maxReachLevel2.get(); break;
                case 3: reach =  BuildConfig.reach.maxReachLevel3.get(); break;
            }

            if (this.mirrorSettings != null)
                this.mirrorSettings.radius = reach / 2;
            if (this.radialMirrorSettings != null)
                this.radialMirrorSettings.radius = reach / 2;
        }
    }

    public static void handleNewPlayer(PlayerEntity player){
        //Makes sure player has modifier settings (if it doesnt it will create it)
        getModifierSettings(player);

        //Only on server
        if (!player.world.isRemote) {
            //Send to client
            ModifierSettingsMessage msg = new ModifierSettingsMessage(getModifierSettings(player));
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), msg);
        }
    }
}

