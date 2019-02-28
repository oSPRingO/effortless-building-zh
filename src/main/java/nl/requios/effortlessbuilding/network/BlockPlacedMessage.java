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
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

/***
 * Sends a message to the server indicating that a player wants to place a block.
 * Received clientside: server has placed blocks and its letting the client know.
 */
public class BlockPlacedMessage implements IMessage {

    private boolean blockHit;
    private BlockPos blockPos;
    private EnumFacing sideHit;
    private Vec3d hitVec;
    private boolean placeStartPos; //prevent double placing in normal mode

    public BlockPlacedMessage() {
        this.blockHit = false;
        this.blockPos = BlockPos.ORIGIN;
        this.sideHit = EnumFacing.UP;
        this.hitVec = new Vec3d(0, 0, 0);
        this.placeStartPos = true;
    }

    public BlockPlacedMessage(RayTraceResult result, boolean placeStartPos) {
        this.blockHit = result.typeOfHit == RayTraceResult.Type.BLOCK;
        this.blockPos = result.getBlockPos();
        this.sideHit = result.sideHit;
        this.hitVec = result.hitVec;
        this.placeStartPos = placeStartPos;
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

    public boolean getPlaceStartPos() {
        return placeStartPos;
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
        buf.writeBoolean(placeStartPos);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        blockHit = buf.readBoolean();
        blockPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        sideHit = EnumFacing.byIndex(buf.readInt());
        hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        placeStartPos = buf.readBoolean();
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<BlockPlacedMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(BlockPlacedMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            if (ctx.side == Side.CLIENT){
                //Received clientside
                EffortlessBuilding.proxy.getThreadListenerFromContext(ctx).addScheduledTask(() -> {
                    //Nod RenderHandler to do the dissolve shader effect
                    BlockPreviewRenderer.onBlocksPlaced();
                });
                return null;
            } else {
                //Received serverside
                EffortlessBuilding.proxy.getThreadListenerFromContext(ctx).addScheduledTask(() -> {
                    BuildModes.onBlockPlacedMessage(ctx.getServerHandler().player, message);
                });
                // No response packet
                return null;
            }
        }
    }
}
