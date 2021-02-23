package nl.requios.effortlessbuildingzh.buildmode.buildmodes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import nl.requios.effortlessbuildingzh.buildmode.ModeOptions;
import nl.requios.effortlessbuildingzh.buildmode.ThreeClicksBuildMode;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends ThreeClicksBuildMode {

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
        return getSphereBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    public static List<BlockPos> getSphereBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        List<BlockPos> list = new ArrayList<>();

        float centerX = x1;
        float centerY = y1;
        float centerZ = z1;

        //Adjust for CIRCLE_START
        if (ModeOptions.getCircleStart() == ModeOptions.ActionEnum.CIRCLE_START_CORNER) {
            centerX = x1 + (x2 - x1) / 2f;
            centerY = y1 + (y3 - y1) / 2f;
            centerZ = z1 + (z2 - z1) / 2f;
        } else {
            x1 = (int) (centerX - (x2 - centerX));
            y1 = (int) (centerY - (y3 - centerY));
            z1 = (int) (centerZ - (z2 - centerZ));
        }

        float radiusX = MathHelper.abs(x2 - centerX);
        float radiusY = MathHelper.abs(y3 - centerY);
        float radiusZ = MathHelper.abs(z2 - centerZ);

        if (ModeOptions.getFill() == ModeOptions.ActionEnum.FULL)
            addSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);
        else
            addHollowSphereBlocks(list, x1, y1, z1, x3, y3, z3, centerX, centerY, centerZ, radiusX, radiusY, radiusZ);

        return list;
    }

    public static void addSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                       float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowSphereBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2,
                                             float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {

                    float distance = distance(l, m, n, centerX, centerY, centerZ);
                    float radius = calculateSpheroidRadius(centerX, centerY, centerZ, radiusX, radiusY, radiusZ, l, m, n);
                    if (distance < radius + 0.4f && distance > radius - 0.6f)
                        list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    private static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return MathHelper.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
    }

    public static float calculateSpheroidRadius(float centerX, float centerY, float centerZ, float radiusX, float radiusY, float radiusZ, int x, int y, int z) {
        //Twice ellipse radius
        float radiusXZ = Circle.calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, x, z);

        //TODO project x to plane

        return Circle.calculateEllipseRadius(centerX, centerY, radiusXZ, radiusY, x, y);
    }
}