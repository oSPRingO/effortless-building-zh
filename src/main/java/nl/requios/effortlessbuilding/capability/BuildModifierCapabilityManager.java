package nl.requios.effortlessbuilding.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.requios.effortlessbuilding.BuildSettingsManager.BuildSettings;
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class BuildModifierCapabilityManager {

    @CapabilityInject(IBuildModifierCapability.class)
    public final static Capability<IBuildModifierCapability> buildModifierCapability = null;

    public interface IBuildModifierCapability {
        BuildSettings getBuildModifierData();

        void setBuildModifierData(BuildSettings buildSettings);
    }

    public static class BuildModifierCapability implements IBuildModifierCapability {
        private BuildSettings buildSettings;

        @Override
        public BuildSettings getBuildModifierData() {
            return buildSettings;
        }

        @Override
        public void setBuildModifierData(BuildSettings buildSettings) {
            this.buildSettings = buildSettings;
        }
    }

    public static class Storage implements Capability.IStorage<IBuildModifierCapability> {
        @Override
        public NBTBase writeNBT(Capability<IBuildModifierCapability> capability, IBuildModifierCapability instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            BuildSettings buildSettings = instance.getBuildModifierData();
            if (buildSettings == null) buildSettings = new BuildSettings();

            //MIRROR
            Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
            if (m == null) m = new Mirror.MirrorSettings();
            compound.setBoolean("mirrorEnabled", m.enabled);
            compound.setDouble("mirrorPosX", m.position.x);
            compound.setDouble("mirrorPosY", m.position.y);
            compound.setDouble("mirrorPosZ", m.position.z);
            compound.setBoolean("mirrorX", m.mirrorX);
            compound.setBoolean("mirrorY", m.mirrorY);
            compound.setBoolean("mirrorZ", m.mirrorZ);
            compound.setInteger("mirrorRadius", m.radius);
            compound.setBoolean("mirrorDrawLines", m.drawLines);
            compound.setBoolean("mirrorDrawPlanes", m.drawPlanes);

            //ARRAY
            Array.ArraySettings a = buildSettings.getArraySettings();
            if (a == null) a = new Array.ArraySettings();
            compound.setBoolean("arrayEnabled", a.enabled);
            compound.setInteger("arrayOffsetX", a.offset.getX());
            compound.setInteger("arrayOffsetY", a.offset.getY());
            compound.setInteger("arrayOffsetZ", a.offset.getZ());
            compound.setInteger("arrayCount", a.count);

            compound.setInteger("reachUpgrade", buildSettings.getReachUpgrade());

            //compound.setBoolean("quickReplace", buildSettings.doQuickReplace()); dont save quickreplace

            //RADIAL MIRROR
            RadialMirror.RadialMirrorSettings r = buildSettings.getRadialMirrorSettings();
            if (r == null) r = new RadialMirror.RadialMirrorSettings();
            compound.setBoolean("radialMirrorEnabled", r.enabled);
            compound.setDouble("radialMirrorPosX", r.position.x);
            compound.setDouble("radialMirrorPosY", r.position.y);
            compound.setDouble("radialMirrorPosZ", r.position.z);
            compound.setInteger("radialMirrorSlices", r.slices);
            compound.setBoolean("radialMirrorAlternate", r.alternate);
            compound.setInteger("radialMirrorRadius", r.radius);
            compound.setBoolean("radialMirrorDrawLines", r.drawLines);
            compound.setBoolean("radialMirrorDrawPlanes", r.drawPlanes);

            return compound;
        }

        @Override
        public void readNBT(Capability<IBuildModifierCapability> capability, IBuildModifierCapability instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            //MIRROR
            boolean mirrorEnabled = compound.getBoolean("mirrorEnabled");
            Vec3d mirrorPosition = new Vec3d(compound.getDouble("mirrorPosX"), compound.getDouble("mirrorPosY"), compound.getDouble("mirrorPosZ"));
            boolean mirrorX = compound.getBoolean("mirrorX");
            boolean mirrorY = compound.getBoolean("mirrorY");
            boolean mirrorZ = compound.getBoolean("mirrorZ");
            int mirrorRadius = compound.getInteger("mirrorRadius");
            boolean mirrorDrawLines = compound.getBoolean("mirrorDrawLines");
            boolean mirrorDrawPlanes = compound.getBoolean("mirrorDrawPlanes");
            Mirror.MirrorSettings mirrorSettings = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius, mirrorDrawLines, mirrorDrawPlanes);

            //ARRAY
            boolean arrayEnabled = compound.getBoolean("arrayEnabled");
            BlockPos arrayOffset = new BlockPos(compound.getInteger("arrayOffsetX"), compound.getInteger("arrayOffsetY"), compound.getInteger("arrayOffsetZ"));
            int arrayCount = compound.getInteger("arrayCount");
            Array.ArraySettings arraySettings = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);

            int reachUpgrade = compound.getInteger("reachUpgrade");

            //boolean quickReplace = compound.getBoolean("quickReplace"); //dont load quickreplace

            //RADIAL MIRROR
            boolean radialMirrorEnabled = compound.getBoolean("radialMirrorEnabled");
            Vec3d radialMirrorPosition = new Vec3d(compound.getDouble("radialMirrorPosX"), compound.getDouble("radialMirrorPosY"), compound.getDouble("radialMirrorPosZ"));
            int radialMirrorSlices = compound.getInteger("radialMirrorSlices");
            boolean radialMirrorAlternate = compound.getBoolean("radialMirrorAlternate");
            int radialMirrorRadius = compound.getInteger("radialMirrorRadius");
            boolean radialMirrorDrawLines = compound.getBoolean("radialMirrorDrawLines");
            boolean radialMirrorDrawPlanes = compound.getBoolean("radialMirrorDrawPlanes");
            RadialMirror.RadialMirrorSettings radialMirrorSettings = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition,
                    radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);

            BuildSettings buildSettings = new BuildSettings(mirrorSettings, arraySettings, radialMirrorSettings, false, reachUpgrade);
            instance.setBuildModifierData(buildSettings);
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTBase> {
        IBuildModifierCapability inst = buildModifierCapability.getDefaultInstance();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == buildModifierCapability;
        }

        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == buildModifierCapability) return buildModifierCapability.<T>cast(inst);
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return buildModifierCapability.getStorage().writeNBT(buildModifierCapability, inst, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            buildModifierCapability.getStorage().readNBT(buildModifierCapability, inst, null, nbt);
        }
    }

    // Allows for the capability to persist after death.
    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        IBuildModifierCapability original = event.getOriginal().getCapability(buildModifierCapability, null);
        IBuildModifierCapability clone = event.getEntity().getCapability(buildModifierCapability, null);
        clone.setBuildModifierData(original.getBuildModifierData());
    }
}
