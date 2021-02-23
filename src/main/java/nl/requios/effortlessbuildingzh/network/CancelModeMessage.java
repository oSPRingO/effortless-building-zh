package nl.requios.effortlessbuildingzh.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmode.BuildModes;

import java.util.function.Supplier;

/**
 * Sends a message to the server indicating that a buildmode needs to be canceled for a player
 */
public class CancelModeMessage {

    public static void encode(CancelModeMessage message, PacketBuffer buf) {
    }

    public static CancelModeMessage decode(PacketBuffer buf) {
        return new CancelModeMessage();
    }

    public static class Handler
    {
        public static void handle(CancelModeMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = EffortlessBuildingZh.proxy.getPlayerEntityFromContext(ctx);

                BuildModes.initializeMode(player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
