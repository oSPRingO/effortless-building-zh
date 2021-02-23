package nl.requios.effortlessbuildingzh.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmode.BuildModes;
import nl.requios.effortlessbuildingzh.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuildingzh.buildmode.ModeSettingsManager.ModeSettings;

import java.util.function.Supplier;

/**
 * Shares mode settings (see ModeSettingsManager) between server and client
 */
public class ModeSettingsMessage {

    private ModeSettings modeSettings;

    public ModeSettingsMessage() {
    }

    public ModeSettingsMessage(ModeSettings modeSettings) {
        this.modeSettings = modeSettings;
    }

    public static void encode(ModeSettingsMessage message, PacketBuffer buf) {
        buf.writeInt(message.modeSettings.getBuildMode().ordinal());
    }

    public static ModeSettingsMessage decode(PacketBuffer buf) {
        BuildModes.BuildModeEnum buildMode = BuildModes.BuildModeEnum.values()[buf.readInt()];

        return new ModeSettingsMessage(new ModeSettings(buildMode));
    }

    public static class Handler
    {
        public static void handle(ModeSettingsMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = EffortlessBuildingZh.proxy.getPlayerEntityFromContext(ctx);

                // Sanitize
                ModeSettingsManager.sanitize(message.modeSettings, player);

                ModeSettingsManager.setModeSettings(player, message.modeSettings);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
