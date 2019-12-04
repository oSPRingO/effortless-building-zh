package nl.requios.effortlessbuilding.network;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.BlockSet;
import nl.requios.effortlessbuilding.buildmodifier.UndoRedo;
import nl.requios.effortlessbuilding.proxy.ClientProxy;

import java.util.ArrayList;
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
                EffortlessBuilding.log("RequestLookAtMessage");

                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    //Received clientside
                    //Send back your info
                    EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                    //Prevent double placing in normal mode with placeStartPos false
                    //Unless QuickReplace is on, then we do need to place start pos.
                    PacketHandler.INSTANCE.sendToServer(new BlockPlacedMessage(ClientProxy.previousLookAt, message.getPlaceStartPos()));
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
