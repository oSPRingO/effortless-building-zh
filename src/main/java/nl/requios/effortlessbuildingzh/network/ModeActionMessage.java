package nl.requios.effortlessbuildingzh.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmode.ModeOptions;

import java.util.function.Supplier;

/**
 * Shares mode settings (see ModeSettingsManager) between server and client
 */
public class ModeActionMessage {

    private ModeOptions.ActionEnum action;

    public ModeActionMessage() {
    }

    public ModeActionMessage(ModeOptions.ActionEnum action) {
        this.action = action;
    }

    public static void encode(ModeActionMessage message, PacketBuffer buf) {
        buf.writeInt(message.action.ordinal());
    }

    public static ModeActionMessage decode(PacketBuffer buf) {
        ModeOptions.ActionEnum action = ModeOptions.ActionEnum.values()[buf.readInt()];
        return new ModeActionMessage(action);
    }

    public static class Handler
    {
        public static void handle(ModeActionMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = EffortlessBuildingZh.proxy.getPlayerEntityFromContext(ctx);

                ModeOptions.performAction(player, message.action);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
