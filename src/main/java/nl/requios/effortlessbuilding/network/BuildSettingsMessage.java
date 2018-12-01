package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import nl.requios.effortlessbuilding.Array;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.BuildSettingsManager.BuildSettings;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.Mirror;

public class BuildSettingsMessage implements IMessage {

    private BuildSettings buildSettings;

    public BuildSettingsMessage() {
    }

    public BuildSettingsMessage(BuildSettings buildSettings) {
        this.buildSettings = buildSettings;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        //MIRROR
        Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
        buf.writeBoolean(m.enabled);
        buf.writeDouble(m.position.x);
        buf.writeDouble(m.position.y);
        buf.writeDouble(m.position.z);
        buf.writeBoolean(m.mirrorX);
        buf.writeBoolean(m.mirrorY);
        buf.writeBoolean(m.mirrorZ);
        buf.writeInt(m.radius);
        buf.writeBoolean(m.drawLines);
        buf.writeBoolean(m.drawPlanes);

        //ARRAY
        Array.ArraySettings a = buildSettings.getArraySettings();
        buf.writeBoolean(a.enabled);
        buf.writeInt(a.offset.getX());
        buf.writeInt(a.offset.getY());
        buf.writeInt(a.offset.getZ());
        buf.writeInt(a.count);

        buf.writeBoolean(buildSettings.doQuickReplace());

        buf.writeInt(buildSettings.getReachUpgrade());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        //MIRROR
        boolean mirrorEnabled = buf.readBoolean();
        Vec3d mirrorPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        boolean mirrorX = buf.readBoolean();
        boolean mirrorY = buf.readBoolean();
        boolean mirrorZ = buf.readBoolean();
        int mirrorRadius = buf.readInt();
        boolean mirrorDrawLines = buf.readBoolean();
        boolean mirrorDrawPlanes = buf.readBoolean();
        Mirror.MirrorSettings m = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius, mirrorDrawLines, mirrorDrawPlanes);

        //ARRAY
        boolean arrayEnabled = buf.readBoolean();
        BlockPos arrayOffset = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        int arrayCount = buf.readInt();
        Array.ArraySettings a = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);

        boolean quickReplace = buf.readBoolean();

        int reachUpgrade = buf.readInt();

        buildSettings = new BuildSettings(m, a, quickReplace, reachUpgrade);
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<BuildSettingsMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(BuildSettingsMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            // This is the player the packet was sent to the server from
            EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);
            // The value that was sent
            BuildSettings buildSettings = message.buildSettings;

            // Sanitize
            BuildSettingsManager.sanitize(buildSettings, player);

            // Execute the action on the main server thread by adding it as a scheduled task
            IThreadListener threadListener = EffortlessBuilding.proxy.getThreadListenerFromContext(ctx);
            threadListener.addScheduledTask(() -> {
                EntityPlayer p = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);
                BuildSettingsManager.setBuildSettings(p, buildSettings);
            });
            // No response packet
            return null;
        }
    }
}
