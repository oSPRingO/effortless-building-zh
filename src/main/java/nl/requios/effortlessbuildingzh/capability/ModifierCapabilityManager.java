package nl.requios.effortlessbuildingzh.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.requios.effortlessbuildingzh.buildmodifier.Array;
import nl.requios.effortlessbuildingzh.buildmodifier.Mirror;
import nl.requios.effortlessbuildingzh.buildmodifier.RadialMirror;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager.*;

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
        public INBT writeNBT(Capability<IModifierCapability> capability, IModifierCapability instance, Direction side) {
            CompoundNBT compound = new CompoundNBT();
            ModifierSettings modifierSettings = instance.getModifierData();
            if (modifierSettings == null) modifierSettings = new ModifierSettings();

            //MIRROR
            Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
            if (m == null) m = new Mirror.MirrorSettings();
            compound.putBoolean("mirrorEnabled", m.enabled);
            compound.putDouble("mirrorPosX", m.position.x);
            compound.putDouble("mirrorPosY", m.position.y);
            compound.putDouble("mirrorPosZ", m.position.z);
            compound.putBoolean("mirrorX", m.mirrorX);
            compound.putBoolean("mirrorY", m.mirrorY);
            compound.putBoolean("mirrorZ", m.mirrorZ);
            compound.putInt("mirrorRadius", m.radius);
            compound.putBoolean("mirrorDrawLines", m.drawLines);
            compound.putBoolean("mirrorDrawPlanes", m.drawPlanes);

            //ARRAY
            Array.ArraySettings a = modifierSettings.getArraySettings();
            if (a == null) a = new Array.ArraySettings();
            compound.putBoolean("arrayEnabled", a.enabled);
            compound.putInt("arrayOffsetX", a.offset.getX());
            compound.putInt("arrayOffsetY", a.offset.getY());
            compound.putInt("arrayOffsetZ", a.offset.getZ());
            compound.putInt("arrayCount", a.count);

            compound.putInt("reachUpgrade", modifierSettings.getReachUpgrade());

            //compound.putBoolean("quickReplace", buildSettings.doQuickReplace()); dont save quickreplace

            //RADIAL MIRROR
            RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
            if (r == null) r = new RadialMirror.RadialMirrorSettings();
            compound.putBoolean("radialMirrorEnabled", r.enabled);
            compound.putDouble("radialMirrorPosX", r.position.x);
            compound.putDouble("radialMirrorPosY", r.position.y);
            compound.putDouble("radialMirrorPosZ", r.position.z);
            compound.putInt("radialMirrorSlices", r.slices);
            compound.putBoolean("radialMirrorAlternate", r.alternate);
            compound.putInt("radialMirrorRadius", r.radius);
            compound.putBoolean("radialMirrorDrawLines", r.drawLines);
            compound.putBoolean("radialMirrorDrawPlanes", r.drawPlanes);

            return compound;
        }

        @Override
        public void readNBT(Capability<IModifierCapability> capability, IModifierCapability instance, Direction side, INBT nbt) {
            CompoundNBT compound = (CompoundNBT) nbt;

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

    public static class Provider implements ICapabilitySerializable<INBT> {

        IModifierCapability inst = modifierCapability.getDefaultInstance();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return modifierCapability.orEmpty(cap, LazyOptional.of(() -> inst));
        }

        @Override
        public INBT serializeNBT() {
            return modifierCapability.getStorage().writeNBT(modifierCapability, inst, null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
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
