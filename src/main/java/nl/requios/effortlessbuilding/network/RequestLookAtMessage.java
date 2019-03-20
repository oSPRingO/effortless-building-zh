package nl.requios.effortlessbuilding.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.BlockSet;
import nl.requios.effortlessbuilding.buildmodifier.UndoRedo;
import nl.requios.effortlessbuilding.proxy.ClientProxy;

import java.util.ArrayList;

/***
 * Sends a message to the client asking for its lookat (objectmouseover) data.
 * This is then sent back with a BlockPlacedMessage.
 */
public class RequestLookAtMessage implements IMessage {
    private BlockPos coordinate;
    private IBlockState blockState;

    public RequestLookAtMessage() {
        coordinate = BlockPos.ORIGIN;
        blockState = null;
    }

    public RequestLookAtMessage(BlockPos coordinate, IBlockState blockState) {
        this.coordinate = coordinate;
        this.blockState = blockState;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.coordinate.getX());
        buf.writeInt(this.coordinate.getY());
        buf.writeInt(this.coordinate.getZ());
        buf.writeInt(Block.getStateId(this.blockState));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        coordinate = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        blockState = Block.getStateById(buf.readInt());
    }

    // The params of the IMessageHandler are <REQ, REPLY>
    public static class MessageHandler implements IMessageHandler<RequestLookAtMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override
        public IMessage onMessage(RequestLookAtMessage message, MessageContext ctx) {
            //EffortlessBuilding.log("message received on " + ctx.side + " side");

            if (ctx.side == Side.CLIENT){
                //Received clientside
                //Send back your info

                EffortlessBuilding.proxy.getThreadListenerFromContext(ctx).addScheduledTask(() -> {
                    EntityPlayer player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);

                    //Add to undo stack clientside
                    UndoRedo.addUndo(player, new BlockSet(
                            new ArrayList<BlockPos>() {{add(message.getCoordinate());}},
                            new ArrayList<IBlockState>() {{add(message.getBlockState());}},
                            new Vec3d(0,0,0),
                            message.getCoordinate(), message.getCoordinate()));
                });

                //Prevent double placing in normal mode with placeStartPos false
                return new BlockPlacedMessage(ClientProxy.previousLookAt, false);
            }
            return null;
        }
    }
}
