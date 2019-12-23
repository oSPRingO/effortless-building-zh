package nl.requios.effortlessbuilding.network;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

import java.util.function.Supplier;

/***
 * Sends a message to the server indicating that a player wants to place a block.
 * Received clientside: server has placed blocks and its letting the client know.
 */
public class BlockPlacedMessage {

    private boolean blockHit;
    private BlockPos blockPos;
    private Direction sideHit;
    private Vec3d hitVec;
    private boolean placeStartPos; //prevent double placing in normal mode

    public BlockPlacedMessage() {
        this.blockHit = false;
        this.blockPos = BlockPos.ZERO;
        this.sideHit = Direction.UP;
        this.hitVec = new Vec3d(0, 0, 0);
        this.placeStartPos = true;
    }

    public BlockPlacedMessage(BlockRayTraceResult result, boolean placeStartPos) {
        this.blockHit = result.getType() == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getPos();
        this.sideHit = result.getFace();
        this.hitVec = result.getHitVec();
        this.placeStartPos = placeStartPos;
    }

    public BlockPlacedMessage(boolean blockHit, BlockPos blockPos, Direction sideHit, Vec3d hitVec, boolean placeStartPos) {
        this.blockHit = blockHit;
        this.blockPos = blockPos;
        this.sideHit = sideHit;
        this.hitVec = hitVec;
        this.placeStartPos = placeStartPos;
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

    public boolean getPlaceStartPos() {
        return placeStartPos;
    }

    public static void encode(BlockPlacedMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.blockHit);
        buf.writeInt(message.blockPos.getX());
        buf.writeInt(message.blockPos.getY());
        buf.writeInt(message.blockPos.getZ());
        buf.writeInt(message.sideHit.getIndex());
        buf.writeDouble(message.hitVec.x);
        buf.writeDouble(message.hitVec.y);
        buf.writeDouble(message.hitVec.z);
        buf.writeBoolean(message.placeStartPos);
    }

    public static BlockPlacedMessage decode(PacketBuffer buf) {
        boolean blockHit = buf.readBoolean();
        BlockPos blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        Direction sideHit = Direction.byIndex(buf.readInt());
        Vec3d hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        boolean placeStartPos = buf.readBoolean();
        return new BlockPlacedMessage(blockHit, blockPos, sideHit, hitVec, placeStartPos);
    }

    public static class Handler
    {
        public static void handle(BlockPlacedMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    //Received clientside
                    //Nod RenderHandler to do the dissolve shader effect
                    BlockPreviewRenderer.onBlocksPlaced();
                } else {
                    //Received serverside
                    BuildModes.onBlockPlacedMessage(ctx.get().getSender(), message);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
