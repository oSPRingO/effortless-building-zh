package nl.requios.effortlessbuilding.capability;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.Callable;

import static nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager.*;

@Mod.EventBusSubscriber
public class ModifierCapabilityManager {

    @CapabilityInject(IModifierCapability.class)
    public final static Capability<IModifierCapability> modifierCapability = null;

    public interface IModifierCapability {
        ModifierSettings getModifierData();

        void setModifierData(ModifierSettings modifierSettings);
    }

    public static class ModifierCapability implements IModifierCapability {
        private ModifierSettings modifierSettings;

        @Override
        public ModifierSettings getModifierData() {
            return modifierSettings;
        }

        @Override
        public void setModifierData(ModifierSettings modifierSettings) {
            this.modifierSettings = modifierSettings;
        }
    }

    public static class Storage implements Capability.IStorage<IModifierCapability> {
        @Override
        public INBTBase writeNBT(Capability<IModifierCapability> capability, IModifierCapability instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            ModifierSettings modifierSettings = instance.getModifierData();
            if (modifierSettings == null) modifierSettings = new ModifierSettings();

            //MIRROR
            Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
            if (m == null) m = new Mirror.MirrorSettings();
            compound.setBoolean("mirrorEnabled", m.enabled);
            compound.setDouble("mirrorPosX", m.position.x);
            compound.setDouble("mirrorPosY", m.position.y);
            compound.setDouble("mirrorPosZ", m.position.z);
            compound.setBoolean("mirrorX", m.mirrorX);
            compound.setBoolean("mirrorY", m.mirrorY);
            compound.setBoolean("mirrorZ", m.mirrorZ);
            compound.setInt("mirrorRadius", m.radius);
            compound.setBoolean("mirrorDrawLines", m.drawLines);
            compound.setBoolean("mirrorDrawPlanes", m.drawPlanes);

            //ARRAY
            Array.ArraySettings a = modifierSettings.getArraySettings();
            if (a == null) a = new Array.ArraySettings();
            compound.setBoolean("arrayEnabled", a.enabled);
            compound.setInt("arrayOffsetX", a.offset.getX());
            compound.setInt("arrayOffsetY", a.offset.getY());
            compound.setInt("arrayOffsetZ", a.offset.getZ());
            compound.setInt("arrayCount", a.count);

            compound.setInt("reachUpgrade", modifierSettings.getReachUpgrade());

            //compound.setBoolean("quickReplace", buildSettings.doQuickReplace()); dont save quickreplace

            //RADIAL MIRROR
            RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
            if (r == null) r = new RadialMirror.RadialMirrorSettings();
            compound.setBoolean("radialMirrorEnabled", r.enabled);
            compound.setDouble("radialMirrorPosX", r.position.x);
            compound.setDouble("radialMirrorPosY", r.position.y);
            compound.setDouble("radialMirrorPosZ", r.position.z);
            compound.setInt("radialMirrorSlices", r.slices);
            compound.setBoolean("radialMirrorAlternate", r.alternate);
            compound.setInt("radialMirrorRadius", r.radius);
            compound.setBoolean("radialMirrorDrawLines", r.drawLines);
            compound.setBoolean("radialMirrorDrawPlanes", r.drawPlanes);

            return compound;
        }

        @Override
        public void readNBT(Capability<IModifierCapability> capability, IModifierCapability instance, EnumFacing side, INBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            //MIRROR
            boolean mirrorEnabled = compound.getBoolean("mirrorEnabled");
            Vec3d mirrorPosition = new Vec3d(
                    compound.getDouble("mirrorPosX"),
                    compound.getDouble("mirrorPosY"),
                    compound.getDouble("mirrorPosZ"));
            boolean mirrorX = compound.getBoolean("mirrorX");
            boolean mirrorY = compound.getBoolean("mirrorY");
            boolean mirrorZ = compound.getBoolean("mirrorZ");
            int mirrorRadius = compound.getInt("mirrorRadius");
            boolean mirrorDrawLines = compound.getBoolean("mirrorDrawLines");
            boolean mirrorDrawPlanes = compound.getBoolean("mirrorDrawPlanes");
            Mirror.MirrorSettings mirrorSettings = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius, mirrorDrawLines, mirrorDrawPlanes);

            //ARRAY
            boolean arrayEnabled = compound.getBoolean("arrayEnabled");
            BlockPos arrayOffset = new BlockPos(
                    compound.getInt("arrayOffsetX"),
                    compound.getInt("arrayOffsetY"),
                    compound.getInt("arrayOffsetZ"));
            int arrayCount = compound.getInt("arrayCount");
            Array.ArraySettings arraySettings = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);

            int reachUpgrade = compound.getInt("reachUpgrade");

            //boolean quickReplace = compound.getBoolean("quickReplace"); //dont load quickreplace

            //RADIAL MIRROR
            boolean radialMirrorEnabled = compound.getBoolean("radialMirrorEnabled");
            Vec3d radialMirrorPosition = new Vec3d(
                    compound.getDouble("radialMirrorPosX"),
                    compound.getDouble("radialMirrorPosY"),
                    compound.getDouble("radialMirrorPosZ"));
            int radialMirrorSlices = compound.getInt("radialMirrorSlices");
            boolean radialMirrorAlternate = compound.getBoolean("radialMirrorAlternate");
            int radialMirrorRadius = compound.getInt("radialMirrorRadius");
            boolean radialMirrorDrawLines = compound.getBoolean("radialMirrorDrawLines");
            boolean radialMirrorDrawPlanes = compound.getBoolean("radialMirrorDrawPlanes");
            RadialMirror.RadialMirrorSettings radialMirrorSettings = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition,
                    radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);

            ModifierSettings modifierSettings = new ModifierSettings(mirrorSettings, arraySettings, radialMirrorSettings, false, reachUpgrade);
            instance.setModifierData(modifierSettings);
        }
    }

    public static class Provider implements ICapabilitySerializable<INBTBase> {

        IModifierCapability inst = modifierCapability.getDefaultInstance();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
            return modifierCapability.orEmpty(cap, LazyOptional.of(() -> inst));
        }

        @Override
        public INBTBase serializeNBT() {
            return modifierCapability.getStorage().writeNBT(modifierCapability, inst, null);
        }

        @Override
        public void deserializeNBT(INBTBase nbt) {
            modifierCapability.getStorage().readNBT(modifierCapability, inst, null, nbt);
        }

    }


    // Allows for the capability to persist after death.
    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        LazyOptional<IModifierCapability> original = event.getOriginal().getCapability(modifierCapability, null);
        LazyOptional<IModifierCapability> clone = event.getEntity().getCapability(modifierCapability, null);
        clone.ifPresent(cloneModifierCapability ->
                original.ifPresent(originalModifierCapability ->
                        cloneModifierCapability.setModifierData(originalModifierCapability.getModifierData())));
    }
}
