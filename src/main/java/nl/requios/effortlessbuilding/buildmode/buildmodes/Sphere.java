package nl.requios.effortlessbuilding.buildmode.buildmodes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import nl.requios.effortlessbuilding.buildmode.ModeOptions;
import nl.requios.effortlessbuilding.buildmode.ThreeClicksBuildMode;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends ThreeClicksBuildMode {

    @Override
    public BlockPos findSecondPos(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        return Floor.findFloor(player, firstPos, skipRaytrace);
    }

    @Override
    public BlockPos findThirdPos(EntityPlayer player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace) {
        return findHeight(player, secondPos, skipRaytrace);
    }

    @Override
    public List<BlockPos> getIntermediateBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2) {
        return Circle.getCircleBlocks(player, x1, y1, z1, x2, y2, z2);
    }

    @Override
    public List<BlockPos> getFinalBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        return getSphereBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    public static List<BlockPos> getSphereBlocks(EntityPlayer player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

//        float centerX = x1;
//        float centerZ = z1;
//
//        //Adjust for CIRCLE_START
//        if (ModeOptions.getCircleStart() == ModeOptions.ActionEnum.CIRCLE_START_CORNER) {
//            centerX = x1 + (x2 - x1) / 2f;
//            centerZ = z1 + (z2 - z1) / 2f;
//        } else {
//            x1 = (int) (centerX - (x2 - centerX));
//            z1 = (int) (centerZ - (z2 - centerZ));
//        }
//
//        float radiusX = MathHelper.abs(x2 - centerX);
//        float radiusZ = MathHelper.abs(z2 - centerZ);
//
//        if (ModeOptions.getFill() == ModeOptions.ActionEnum.FULL)
//            addCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerX, centerZ, radiusX, radiusZ);
//        else
//            addHollowCircleBlocks(list, x1, y1, z1, x2, y2, z2, centerX, centerZ, radiusX, radiusZ, 1f);
//
//        return list;

        //Adjust for CIRCLE_START

        //Get center point

        //Get radius

        //Get blocks based on FILL

        return list;
    }
}