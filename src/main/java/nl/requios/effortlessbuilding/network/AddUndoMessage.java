package nl.requios.effortlessbuilding.network;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.BlockSet;
import nl.requios.effortlessbuilding.buildmodifier.UndoRedo;

import java.util.ArrayList;
import java.util.function.Supplier;

/***
 * Sends a message to the client asking to add a block to the undo stack.
 */
public class AddUndoMessage {
    private BlockPos coordinate;
    private BlockState previousBlockState;
    private BlockState newBlockState;

    public AddUndoMessage() {
        coordinate = BlockPos.ZERO;
        previousBlockState = null;
        newBlockState = null;
    }

    public AddUndoMessage(BlockPos coordinate, BlockState previousBlockState, BlockState newBlockState) {
        this.coordinate = coordinate;
        this.previousBlockState = previousBlockState;
        this.newBlockState = newBlockState;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    public BlockState getPreviousBlockState() {
        return previousBlockState;
    }

    public BlockState getNewBlockState() {
        return newBlockState;
    }

    public static void encode(AddUndoMessage message, PacketBuffer buf) {
        buf.writeInt(message.coordinate.getX());
        buf.writeInt(message.coordinate.getY());
        buf.writeInt(message.coordinate.getZ());
        buf.writeInt(Block.getStateId(message.previousBlockState));
        buf.writeInt(Block.getStateId(message.newBlockState));
    }

    public static AddUndoMessage decode(PacketBuffer buf) {
        BlockPos coordinate = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        BlockState previousBlockState = Block.getStateById(buf.readInt());
        BlockState newBlockState = Block.getStateById(buf.readInt());
        return new AddUndoMessage(coordinate, previousBlockState, newBlockState);
    }

    public static class Handler
    {
        public static void handle(AddUndoMessage message, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    //Received clientside

                    PlayerEntity player = EffortlessBuilding.proxy.getPlayerEntityFromContext(ctx);
                    //Add to undo stack clientside
                    //Only the appropriate player that needs to add this to the undo stack gets this message
                    UndoRedo.addUndo(player, new BlockSet(
                            new ArrayList<BlockPos>() {{add(message.getCoordinate());}},
                            new ArrayList<BlockState>() {{add(message.getPreviousBlockState());}},
                            new ArrayList<BlockState>() {{add(message.getNewBlockState());}},
                            new Vec3d(0,0,0),
                            message.getCoordinate(), message.getCoordinate()));
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
