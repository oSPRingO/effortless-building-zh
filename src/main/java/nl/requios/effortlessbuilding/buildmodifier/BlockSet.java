package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlockSet {
    private List<BlockPos> coordinates;
    private List<BlockState> previousBlockStates;
    private List<BlockState> newBlockStates;
    private Vec3d hitVec;
    private BlockPos firstPos;
    private BlockPos secondPos;

    public BlockSet(List<BlockPos> coordinates, List<BlockState> previousBlockStates, List<BlockState> newBlockStates, Vec3d hitVec,
                    BlockPos firstPos, BlockPos secondPos) {
        this.coordinates = coordinates;
        this.previousBlockStates = previousBlockStates;
        this.newBlockStates = newBlockStates;
        this.hitVec = hitVec;
        this.firstPos = firstPos;
        this.secondPos = secondPos;
    }

    public List<BlockPos> getCoordinates() {
        return coordinates;
    }

    public List<BlockState> getPreviousBlockStates() {
        return previousBlockStates;
    }

    public List<BlockState> getNewBlockStates() {
        return newBlockStates;
    }

    public Vec3d getHitVec() {
        return hitVec;
    }

    public BlockPos getFirstPos() {
        return firstPos;
    }

    public BlockPos getSecondPos() {
        return secondPos;
    }
}
