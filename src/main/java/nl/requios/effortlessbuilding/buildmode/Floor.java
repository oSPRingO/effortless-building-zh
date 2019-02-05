package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Floor implements IBuildMode {
    @Override
    public void initialize(EntityPlayer player) {

    }

    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec) {
        return new ArrayList<>();
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos) {
        return new ArrayList<>();
    }

    @Override
    public EnumFacing getSideHit(EntityPlayer player) {
        return null;
    }

    @Override
    public Vec3d getHitVec(EntityPlayer player) {
        return null;
    }
}
