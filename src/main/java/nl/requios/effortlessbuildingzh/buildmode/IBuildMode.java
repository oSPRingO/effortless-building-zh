package nl.requios.effortlessbuildingzh.buildmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface IBuildMode {

    //Fired when a player selects a buildmode and when it needs to initializeMode
    void initialize(PlayerEntity player);

    //Fired when a block would be placed
    //Return a list of coordinates where you want to place blocks
    List<BlockPos> onRightClick(PlayerEntity player, BlockPos blockPos, Direction sideHit, Vec3d hitVec, boolean skipRaytrace);

    //Fired continuously for visualization purposes
    List<BlockPos> findCoordinates(PlayerEntity player, BlockPos blockPos, boolean skipRaytrace);

    Direction getSideHit(PlayerEntity player);

    Vec3d getHitVec(PlayerEntity player);
}
