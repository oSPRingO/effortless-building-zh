package nl.requios.effortlessbuildingzh.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.proxy.ClientProxy;

import java.util.function.Supplier;

/***
 * Sends a message to the client asking for its lookat (objectmouseover) data.
 * This is then sent back with a BlockPlacedMessage.
 */
public class RequestLookAtMessage {
    private boolean placeStartPos;

    public RequestLookAtMessage() {
        placeStartPos = false;
    }

    public RequestLookAtMessage(boolean placeStartPos) {
        this.placeStartPos = placeStartPos;
    }

    public boolean getPlaceStartPos() {
        return placeStartPos;
    }

    public static void encode(RequestLookAtMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.placeStartPos);
    }

    public static RequestLookAtMessage decode(PacketBuffer buf) {
        boolean placeStartPos = buf.readBoolean();
        return new RequestLookAtMessage(placeStartPos);
    }

    public static class Handler
    {
        public static void handle(RequestLookAtMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    //Received clientside
                    //Send back your info
                    PlayerEntity player = EffortlessBuildingZh.proxy.getPlayerEntityFromContext(ctx);

                    //Prevent double placing in normal mode with placeStartPos false
                    //Unless QuickReplace is on, then we do need to place start pos.
                    if (ClientProxy.previousLookAt.getType() == RayTraceResult.Type.BLOCK) {
                        PacketHandler.INSTANCE.sendToServer(new BlockPlacedMessage((BlockRayTraceResult) ClientProxy.previousLookAt, message.getPlaceStartPos()));
                    } else {
                        PacketHandler.INSTANCE.sendToServer(new BlockPlacedMessage());
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
