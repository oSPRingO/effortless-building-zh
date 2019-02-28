package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeOptions;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;

import static nl.requios.effortlessbuilding.buildmode.ModeSettingsManager.*;

/**
 * Shares mode settings (see ModeSettingsManager) between server and client
 */
public class ModeActionMessage implements IMessage {

    private ModeOptions.ActionEnum action;

    public ModeActionMessage() {
    }

    public ModeActionMessage(ModeOptions.ActionEnum action) {
        this.action = action;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = ModeOptions.ActionEnum.values()[buf.readInt()];
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<ModeActionMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(ModeActionMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            // The value that was sent
            ModeOptions.ActionEnum action = message.action;

            // Execute the action on the main server thread by adding it as a scheduled task
            IThreadListener threadListener = EffortlessBuilding.proxy.getThreadListenerFromContext(ctx);
            threadListener.addScheduledTask(() -> {
                EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                ModeOptions.performAction(player, action);
            });
            // No response packet
            return null;
        }
    }
}
