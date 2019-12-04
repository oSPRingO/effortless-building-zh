package nl.requios.effortlessbuilding.buildmode.buildmodes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.buildmode.IBuildMode;

import java.util.ArrayList;
import java.util.List;

public class Normal implements IBuildMode {
    @Override
    public void initialize(PlayerEntity player) {

    }

    @Override
    public List<BlockPos> onRightClick(PlayerEntity player, BlockPos blockPos, Direction sideHit, Vec3d hitVec, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        if (blockPos != null) list.add(blockPos);
        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(PlayerEntity player, BlockPos blockPos, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        if (blockPos != null) list.add(blockPos);
        return list;
    }

    @Override
    public Direction getSideHit(PlayerEntity player) {
        return null;
    }

    @Override
    public Vec3d getHitVec(PlayerEntity player) {
        return null;
    }
}
