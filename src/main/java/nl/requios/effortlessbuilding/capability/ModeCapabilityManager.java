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
import nl.requios.effortlessbuilding.buildmode.BuildModes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static nl.requios.effortlessbuilding.buildmode.ModeSettingsManager.*;

@Mod.EventBusSubscriber
public class ModeCapabilityManager {

    @CapabilityInject(IModeCapability.class)
    public final static Capability<IModeCapability> modeCapability = null;

    public interface IModeCapability {
        ModeSettings getModeData();

        void setModeData(ModeSettings modeSettings);
    }

    public static class ModeCapability implements IModeCapability {
        private ModeSettings modeSettings;

        @Override
        public ModeSettings getModeData() {
            return modeSettings;
        }

        @Override
        public void setModeData(ModeSettings modeSettings) {
            this.modeSettings = modeSettings;
        }
    }

    public static class Storage implements Capability.IStorage<IModeCapability> {
        @Override
        public NBTBase writeNBT(Capability<IModeCapability> capability, IModeCapability instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            ModeSettings modeSettings = instance.getModeData();
            if (modeSettings == null) modeSettings = new ModeSettings();

            //compound.setInteger("buildMode", modeSettings.getBuildMode().ordinal());

            //TODO add mode settings

            return compound;
        }

        @Override
        public void readNBT(Capability<IModeCapability> capability, IModeCapability instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            //BuildModes.BuildModeEnum buildMode = BuildModes.BuildModeEnum.values()[compound.getInteger("buildMode")];

            //TODO add mode settings

            ModeSettings modeSettings = new ModeSettings(BuildModes.BuildModeEnum.Normal);
            instance.setModeData(modeSettings);
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTBase> {
        IModeCapability inst = modeCapability.getDefaultInstance();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == modeCapability;
        }

        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == modeCapability) return modeCapability.<T>cast(inst);
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return modeCapability.getStorage().writeNBT(modeCapability, inst, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            modeCapability.getStorage().readNBT(modeCapability, inst, null, nbt);
        }
    }

    // Allows for the capability to persist after death.
    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        IModeCapability original = event.getOriginal().getCapability(modeCapability, null);
        IModeCapability clone = event.getEntity().getCapability(modeCapability, null);
        clone.setModeData(original.getModeData());
    }
}
