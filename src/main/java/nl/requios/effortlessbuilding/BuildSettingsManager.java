package nl.requios.effortlessbuilding;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import nl.requios.effortlessbuilding.capability.BuildModifierCapability;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;

@Mod.EventBusSubscriber
public class BuildSettingsManager {

    //Retrieves the buildsettings of a player through the buildModifier capability
    //Never returns null
    public static BuildSettings getBuildSettings(EntityPlayer player){
        if (player.hasCapability(BuildModifierCapability.buildModifier, null)) {
            BuildModifierCapability.IBuildModifier capability = player.getCapability(BuildModifierCapability.buildModifier, null);
            if (capability.getBuildModifierData() == null) {
                capability.setBuildModifierData(new BuildSettings());
            }
            return capability.getBuildModifierData();
        }
        throw new IllegalArgumentException("Player does not have buildModifier capability");
    }

    public static void setBuildSettings(EntityPlayer player, BuildSettings buildSettings) {
        if (player == null) {
            EffortlessBuilding.log("cannot set buildsettings, player is null");
            return;
        }
        if (player.hasCapability(BuildModifierCapability.buildModifier, null)) {
            BuildModifierCapability.IBuildModifier capability = player.getCapability(BuildModifierCapability.buildModifier, null);
            capability.setBuildModifierData(buildSettings);
        } else {
            EffortlessBuilding.log(player, "Saving buildsettings failed.");
        }
    }

    public static class BuildSettings {
        private Mirror.MirrorSettings mirrorSettings;
        private Array.ArraySettings arraySettings;
        private boolean quickReplace = false;

        public BuildSettings() {
            mirrorSettings = new Mirror.MirrorSettings();
            arraySettings = new Array.ArraySettings();
        }

        public BuildSettings(Mirror.MirrorSettings mirrorSettings, Array.ArraySettings arraySettings, boolean quickReplace) {
            this.mirrorSettings = mirrorSettings;
            this.arraySettings = arraySettings;
            this.quickReplace = quickReplace;
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

