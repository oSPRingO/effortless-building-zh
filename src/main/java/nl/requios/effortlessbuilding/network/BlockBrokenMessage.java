package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.BuildModifiers;
import nl.requios.effortlessbuilding.EffortlessBuilding;

public class BlockBrokenMessage implements IMessage {

    private boolean blockHit;
    private BlockPos blockPos;
    private EnumFacing sideHit;
    private Vec3d hitVec;

    public BlockBrokenMessage() {
        this.blockHit = false;
        this.blockPos = BlockPos.ORIGIN;
        this.sideHit = EnumFacing.UP;
        this.hitVec = new Vec3d(0, 0, 0);
    }

    public BlockBrokenMessage(RayTraceResult result) {
        this.blockHit = result.typeOfHit == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getBlockPos();
        this.sideHit = result.sideHit;
        this.hitVec = result.hitVec;
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

    public Vec3d getHitVec() {
        return hitVec;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(blockHit);
        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());
        buf.writeInt(sideHit.getIndex());
        buf.writeDouble(hitVec.x);
        buf.writeDouble(hitVec.y);
        buf.writeDouble(hitVec.z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockHit = buf.readBoolean();
        blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        sideHit = EnumFacing.byIndex(buf.readInt());
        hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<BlockBrokenMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(BlockBrokenMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            if (ctx.side == Side.SERVER) {
                //Received serverside
                EffortlessBuilding.proxy.getThreadListenerFromContext(ctx).addScheduledTask(() -> {
                    BuildModifiers.onBlockBrokenMessage(ctx.getServerHandler().player, message);
                });
            }
            // No response packet
            return null;
        }
    }

}
