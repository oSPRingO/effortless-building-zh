package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class BlockSet {
	private final List<BlockPos> coordinates;
	private final List<BlockState> previousBlockStates;
	private final List<BlockState> newBlockStates;
	private final Vector3d hitVec;
	private final BlockPos firstPos;
	private final BlockPos secondPos;

	public BlockSet(List<BlockPos> coordinates, List<BlockState> previousBlockStates, List<BlockState> newBlockStates, Vector3d hitVec,
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

	public Vector3d getHitVec() {
		return hitVec;
	}

	public BlockPos getFirstPos() {
		return firstPos;
	}

	public BlockPos getSecondPos() {
		return secondPos;
	}
}
