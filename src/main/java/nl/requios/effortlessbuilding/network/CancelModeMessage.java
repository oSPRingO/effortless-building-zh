package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;

public class CancelModeMessage implements IMessage {

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<CancelModeMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(CancelModeMessage message, MessageContext ctx) {

            // Execute the action on the main server thread by adding it as a scheduled task
            IThreadListener threadListener = EffortlessBuilding.proxy.getThreadListenerFromContext(ctx);
            threadListener.addScheduledTask(() -> {
                EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                BuildModes.initializeMode(player);
            });
            // No response packet
            return null;
        }
    }
}
