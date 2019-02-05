package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;

import static nl.requios.effortlessbuilding.buildmode.ModeSettingsManager.*;

public class ModeSettingsMessage implements IMessage {

    private ModeSettings modeSettings;

    public ModeSettingsMessage() {
    }

    public ModeSettingsMessage(ModeSettings modeSettings) {
        this.modeSettings = modeSettings;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(modeSettings.getBuildMode().ordinal());

        //TODO add mode settings
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        BuildModes.BuildModeEnum buildMode = BuildModes.BuildModeEnum.values()[buf.readInt()];

        //TODO add mode settings

        modeSettings = new ModeSettings(buildMode);
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<ModeSettingsMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(ModeSettingsMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            // The value that was sent
            ModeSettings modeSettings = message.modeSettings;

            // Execute the action on the main server thread by adding it as a scheduled task
            IThreadListener threadListener = EffortlessBuilding.proxy.getThreadListenerFromContext(ctx);
            threadListener.addScheduledTask(() -> {
                EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                // Sanitize
                ModeSettingsManager.sanitize(modeSettings, player);

                ModeSettingsManager.setModeSettings(player, modeSettings);
            });
            // No response packet
            return null;
        }
    }
}
