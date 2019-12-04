package nl.requios.effortlessbuilding.buildmode.buildmodes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import nl.requios.effortlessbuilding.buildmode.ThreeClicksBuildMode;

import java.util.ArrayList;
import java.util.List;

public class Cylinder extends ThreeClicksBuildMode {

    @Override
    public BlockPos findSecondPos(PlayerEntity player, BlockPos firstPos, boolean skipRaytrace) {
        return Floor.findFloor(player, firstPos, skipRaytrace);
    }

    @Override
    public BlockPos findThirdPos(PlayerEntity player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        return findHeight(player, secondPos, skipRaytrace);
    }

    @Override
    public List<BlockPos> getIntermediateBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return Circle.getCircleBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    @Override
    public List<BlockPos> getFinalBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getCylinderBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    public static List<BlockPos> getCylinderBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        //Get circle blocks (using CIRCLE_START and FILL options built-in)
        List<BlockPos> circleBlocks = Circle.getCircleBlocks(player, x1, y1, z1, x2, y2, z2);

        int lowest = Math.min(y1, y3);
        int highest = Math.max(y1, y3);

        //Copy circle on y axis
        for (int y = lowest; y <= highest; y++) {
            for (BlockPos blockPos : circleBlocks) {
                list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
            }
        }

        return list;
    }
}