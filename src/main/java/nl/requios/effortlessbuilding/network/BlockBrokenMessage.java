package nl.requios.effortlessbuilding.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;

import java.util.function.Supplier;

/***
 * Sends a message to the server indicating that a player wants to break a block
 */
public class BlockBrokenMessage {

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
        this.blockHit = result.type == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getBlockPos();
        this.sideHit = result.sideHit;
        this.hitVec = result.hitVec;
    }

    public BlockBrokenMessage(boolean blockHit, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec) {
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

    public EnumFacing getSideHit() {
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
        EnumFacing sideHit = EnumFacing.byIndex(buf.readInt());
        Vec3d hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new BlockBrokenMessage(blockHit, blockPos, sideHit, hitVec);
    }

    public static class Handler
    {
        public static void handle(BlockBrokenMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            EffortlessBuilding.log("BlockBrokenMessage");
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
