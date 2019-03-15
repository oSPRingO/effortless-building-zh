package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlockSet {
    private List<BlockPos> coordinates;
    private List<IBlockState> blockStates;
    private Vec3d hitVec;
    private BlockPos firstPos;
    private BlockPos secondPos;

    public BlockSet(List<BlockPos> coordinates, List<IBlockState> blockStates, Vec3d hitVec,
                    BlockPos firstPos, BlockPos secondPos) {
        this.coordinates = coordinates;
        this.blockStates = blockStates;
        this.hitVec = hitVec;
        this.firstPos = firstPos;
        this.secondPos = secondPos;
    }

    public List<BlockPos> getCoordinates() {
        return coordinates;
    }

    public List<IBlockState> getBlockStates() {
        return blockStates;
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
