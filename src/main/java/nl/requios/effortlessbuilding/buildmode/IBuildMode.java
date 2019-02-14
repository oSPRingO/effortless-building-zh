package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface IBuildMode {

    //Fired when a player selects a buildmode and when it needs to initializeMode
    void initialize(EntityPlayer player);

    //Fired when a block would be placed
    //Return a list of coordinates where you want to place blocks
    List<BlockPos> onRightClick(EntityPlayer player, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec, boolean skipRaytrace);

    //Fired continuously for visualization purposes
    List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos, boolean skipRaytrace);

    EnumFacing getSideHit(EntityPlayer player);

    Vec3d getHitVec(EntityPlayer player);
}
