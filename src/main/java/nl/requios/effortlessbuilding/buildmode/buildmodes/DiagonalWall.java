package nl.requios.effortlessbuilding.buildmode.buildmodes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import nl.requios.effortlessbuilding.buildmode.ThreeClicksBuildMode;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.ArrayList;
import java.util.List;

public class DiagonalWall extends ThreeClicksBuildMode {

    @Override
    protected BlockPos findSecondPos(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        return Floor.findFloor(player, firstPos, skipRaytrace);
    }

    @Override
    protected BlockPos findThirdPos(EntityPlayer player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        return findHeight(player, secondPos, skipRaytrace);
    }

    @Override
    protected List<BlockPos> getIntermediateBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return DiagonalLine.getDiagonalLineBlocks(player, x1, y1, z1, x2, y2, z2, 1);
    }

    @Override
    protected List<BlockPos> getFinalBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getDiagonalWallBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    //Add diagonal wall from first to second
    public static List<BlockPos> getDiagonalWallBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        //Get diagonal line blocks
        List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(player, x1, y1, z1, x2, y2, z2, 1);

        int lowest = Math.min(y1, y3);
        int highest = Math.max(y1, y3);

        //Copy diagonal line on y axis
        for (int y = lowest; y <= highest; y++) {
            for (BlockPos blockPos : diagonalLineBlocks) {
                list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
            }
        }

        return list;
    }
}