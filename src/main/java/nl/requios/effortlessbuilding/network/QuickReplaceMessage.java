package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.QuickReplace;
import nl.requios.effortlessbuilding.proxy.ClientProxy;

public class QuickReplaceMessage implements IMessage {

    private boolean blockHit;
    private BlockPos blockPos;
    private EnumFacing sideHit;

    public QuickReplaceMessage() {
        this.blockHit = false;
        this.blockPos = BlockPos.ORIGIN;
        this.sideHit = EnumFacing.UP;
    }

    public QuickReplaceMessage(RayTraceResult result) {
        this.blockHit = result.typeOfHit == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getBlockPos();
        this.sideHit = result.sideHit;
    }

    public boolean isBlockHit() {
        return blockHit;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public EnumFacing getSideHit() {
        return sideHit;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(blockHit);
        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());
        buf.writeInt(sideHit.getIndex());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockHit = buf.readBoolean();
        blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        sideHit = EnumFacing.getFront(buf.readInt());
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<QuickReplaceMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(QuickReplaceMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            if (ctx.side == Side.CLIENT){
                //Received clientside
                //Send back your info
                return new QuickReplaceMessage(ClientProxy.previousLookAt);

//                Minecraft.getMinecraft().addScheduledTask(() -> {
//                    EffortlessBuilding.packetHandler.sendToServer(new QuickReplaceMessage(Minecraft.getMinecraft().objectMouseOver));
//                });

            } else {
                //Received serverside
                EffortlessBuilding.proxy.getThreadListenerFromContext(ctx).addScheduledTask(() -> {
                    QuickReplace.onMessageReceived(ctx.getServerHandler().player, message);
                });
            }
            // No response packet
            return null;
        }
    }
}
