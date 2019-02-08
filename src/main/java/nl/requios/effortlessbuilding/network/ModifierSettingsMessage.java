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
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;

import static nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager.*;

/**
 * Shares modifier settings (see ModifierSettingsManager) between server and client
 */
public class ModifierSettingsMessage implements IMessage {

    private ModifierSettings modifierSettings;

    public ModifierSettingsMessage() {
    }

    public ModifierSettingsMessage(ModifierSettings modifierSettings) {
        this.modifierSettings = modifierSettings;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        //MIRROR
        Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
        buf.writeBoolean(m != null);
        if (m != null) {
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
        }

        //ARRAY
        Array.ArraySettings a = modifierSettings.getArraySettings();
        buf.writeBoolean(a != null);
        if (a != null) {
            buf.writeBoolean(a.enabled);
            buf.writeInt(a.offset.getX());
            buf.writeInt(a.offset.getY());
            buf.writeInt(a.offset.getZ());
            buf.writeInt(a.count);
        }

        buf.writeBoolean(modifierSettings.doQuickReplace());

        buf.writeInt(modifierSettings.getReachUpgrade());

        //RADIAL MIRROR
        RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
        buf.writeBoolean(r != null);
        if (r != null) {
            buf.writeBoolean(r.enabled);
            buf.writeDouble(r.position.x);
            buf.writeDouble(r.position.y);
            buf.writeDouble(r.position.z);
            buf.writeInt(r.slices);
            buf.writeBoolean(r.alternate);
            buf.writeInt(r.radius);
            buf.writeBoolean(r.drawLines);
            buf.writeBoolean(r.drawPlanes);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        //MIRROR
        Mirror.MirrorSettings m = new Mirror.MirrorSettings();
        if (buf.readBoolean()) {
            boolean mirrorEnabled = buf.readBoolean();
            Vec3d mirrorPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            boolean mirrorX = buf.readBoolean();
            boolean mirrorY = buf.readBoolean();
            boolean mirrorZ = buf.readBoolean();
            int mirrorRadius = buf.readInt();
            boolean mirrorDrawLines = buf.readBoolean();
            boolean mirrorDrawPlanes = buf.readBoolean();
            m = new Mirror.MirrorSettings(mirrorEnabled, mirrorPosition, mirrorX, mirrorY, mirrorZ, mirrorRadius,
                            mirrorDrawLines, mirrorDrawPlanes);
        }

        //ARRAY
        Array.ArraySettings a = new Array.ArraySettings();
        if (buf.readBoolean()) {
            boolean arrayEnabled = buf.readBoolean();
            BlockPos arrayOffset = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            int arrayCount = buf.readInt();
            a = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);
        }

        boolean quickReplace = buf.readBoolean();

        int reachUpgrade = buf.readInt();

        //RADIAL MIRROR
        RadialMirror.RadialMirrorSettings r = new RadialMirror.RadialMirrorSettings();
        if (buf.readBoolean()) {
            boolean radialMirrorEnabled = buf.readBoolean();
            Vec3d radialMirrorPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            int radialMirrorSlices = buf.readInt();
            boolean radialMirrorAlternate = buf.readBoolean();
            int radialMirrorRadius = buf.readInt();
            boolean radialMirrorDrawLines = buf.readBoolean();
            boolean radialMirrorDrawPlanes = buf.readBoolean();
            r = new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPosition, radialMirrorSlices,
                            radialMirrorAlternate, radialMirrorRadius, radialMirrorDrawLines, radialMirrorDrawPlanes);
        }

        modifierSettings = new ModifierSettings(m, a, r, quickReplace, reachUpgrade);
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<ModifierSettingsMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(ModifierSettingsMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            // The value that was sent
            ModifierSettings modifierSettings = message.modifierSettings;

            // Execute the action on the main server thread by adding it as a scheduled task
            IThreadListener threadListener = EffortlessBuilding.proxy.getThreadListenerFromContext(ctx);
            threadListener.addScheduledTask(() -> {
                EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                // Sanitize
                ModifierSettingsManager.sanitize(modifierSettings, player);

                ModifierSettingsManager.setModifierSettings(player, modifierSettings);
            });
            // No response packet
            return null;
        }
    }
}
