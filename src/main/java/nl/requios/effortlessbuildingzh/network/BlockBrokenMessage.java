package nl.requios.effortlessbuildingzh.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuildingzh.buildmode.BuildModes;

import java.util.function.Supplier;

/***
 * Sends a message to the server indicating that a player wants to break a block
 */
public class BlockBrokenMessage {

    private boolean blockHit;
    private BlockPos blockPos;
    private Direction sideHit;
    private Vec3d hitVec;

    public BlockBrokenMessage() {
        this.blockHit = false;
        this.blockPos = BlockPos.ZERO;
        this.sideHit = Direction.UP;
        this.hitVec = new Vec3d(0, 0, 0);
    }

    public BlockBrokenMessage(BlockRayTraceResult result) {
        this.blockHit = result.getType() == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getPos();
        this.sideHit = result.getFace();
        this.hitVec = result.getHitVec();
    }

    public BlockBrokenMessage(boolean blockHit, BlockPos blockPos, Direction sideHit, Vec3d hitVec) {
        this.blockHit = blockHit;
        this.blockPos = blockPos;
        this.sideHit = sideHit;
        this.hitVec = hitVec;
    }

    public boolean isBlockHit() {
        return blockHit;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Direction getSideHit() {
        return sideHit;
    }

    public Vec3d getHitVec() {
        return hitVec;
    }

    public static void encode(BlockBrokenMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.blockHit);
        buf.writeInt(message.blockPos.getX());
        buf.writeInt(message.blockPos.getY());
        buf.writeInt(message.blockPos.getZ());
        buf.writeInt(message.sideHit.getIndex());
        buf.writeDouble(message.hitVec.x);
        buf.writeDouble(message.hitVec.y);
        buf.writeDouble(message.hitVec.z);
    }

    public static BlockBrokenMessage decode(PacketBuffer buf) {
        boolean blockHit = buf.readBoolean();
        BlockPos blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        Direction sideHit = Direction.byIndex(buf.readInt());
        Vec3d hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new BlockBrokenMessage(blockHit, blockPos, sideHit, hitVec);
    }

    public static class Handler
    {
        public static void handle(BlockBrokenMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                    //Received serverside
                    BuildModes.onBlockBrokenMessage(ctx.get().getSender(), message);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
