package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class Cube implements IBuildMode {
    //In singleplayer client and server variables are shared
    //Split everything that needs separate values and may not be called twice in one click
    private Dictionary<UUID, Integer> rightClickClientTable = new Hashtable<>();
    private Dictionary<UUID, Integer> rightClickServerTable = new Hashtable<>();
    private Dictionary<UUID, BlockPos> firstPosTable = new Hashtable<>();
    private Dictionary<UUID, BlockPos> secondPosTable = new Hashtable<>();
    private Dictionary<UUID, EnumFacing> sideHitTable = new Hashtable<>();
    private Dictionary<UUID, Vec3d> hitVecTable = new Hashtable<>();

    @Override
    public void initialize(EntityPlayer player) {
        rightClickClientTable.put(player.getUniqueID(), 0);
        rightClickServerTable.put(player.getUniqueID(), 0);
        firstPosTable.put(player.getUniqueID(), BlockPos.ORIGIN);
        sideHitTable.put(player.getUniqueID(), EnumFacing.UP);
        hitVecTable.put(player.getUniqueID(), Vec3d.ZERO);
    }

    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();

        Dictionary<UUID, Integer> rightClickTable = player.world.isRemote ? rightClickClientTable : rightClickServerTable;
        int rightClickNr = rightClickTable.get(player.getUniqueID());
        rightClickNr++;
        rightClickTable.put(player.getUniqueID(), rightClickNr);

        if (rightClickNr == 1) {
            //If clicking in air, reset and try again
            if (blockPos == null) {
                rightClickTable.put(player.getUniqueID(), 0);
                return list;
            }

            //First click, remember starting position
            firstPosTable.put(player.getUniqueID(), blockPos);
            sideHitTable.put(player.getUniqueID(), sideHit);
            hitVecTable.put(player.getUniqueID(), hitVec);
            //Keep list empty, dont place any blocks yet
        } else if (rightClickNr == 2) {
            //Second click, find other floor point
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = Floor.findFloor(player, firstPos, skipRaytrace);

            if (secondPos == null) {
                rightClickTable.put(player.getUniqueID(), 1);
                return list;
            }

            secondPosTable.put(player.getUniqueID(), secondPos);

        } else {
            //Third click, place cube with height
            list = findCoordinates(player, blockPos, skipRaytrace);
            rightClickTable.put(player.getUniqueID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        Dictionary<UUID, Integer> rightClickTable = player.world.isRemote ? rightClickClientTable : rightClickServerTable;
        int rightClickNr = rightClickTable.get(player.getUniqueID());

        if (rightClickNr == 0) {
            if (blockPos != null)
                list.add(blockPos);
        } else if (rightClickNr == 1) {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());

            BlockPos secondPos = Floor.findFloor(player, firstPos, skipRaytrace);
            if (secondPos == null) return list;

            //Add whole floor
            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y = firstPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            if (ModeOptions.getCubeFill() == ModeOptions.ActionEnum.CUBE_SKELETON) {
                //Hollow floor
                Line.addXLineBlocks(list, x1, x2, y, z1);
                Line.addXLineBlocks(list, x1, x2, y, z2);
                Line.addZLineBlocks(list, z1, z2, x1, y);
                Line.addZLineBlocks(list, z1, z2, x2, y);
            } else {
                //Filled floor
                for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

                    for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                        list.add(new BlockPos(l, y, n));
                    }
                }
            }

        } else {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = secondPosTable.get(player.getUniqueID());

            BlockPos thirdPos = DiagonalLine.findHeight(player, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Add whole cube
            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

            int x1 = firstPos.getX(), x2 = thirdPos.getX();
            int y1 = firstPos.getY(), y2 = thirdPos.getY();
            int z1 = firstPos.getZ(), z2 = thirdPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            switch (ModeOptions.getCubeFill()) {
                case CUBE_FULL:
                    addCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                    break;
                case CUBE_HOLLOW:
                    addHollowCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                    break;
                case CUBE_SKELETON:
                    addSkeletonCubeBlocks(list, x1, x2, y1, y2, z1, z2);
                    break;
            }

        }

        return list;
    }

    public static void addCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                    list.add(new BlockPos(l, m, n));
                }
            }
        }
    }

    public static void addHollowCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Wall.addXWallBlocks(list, x1, y1, y2, z1, z2);
        Wall.addXWallBlocks(list, x2, y1, y2, z1, z2);

        Wall.addZWallBlocks(list, x1, x2, y1, y2, z1);
        Wall.addZWallBlocks(list, x1, x2, y1, y2, z2);

        Floor.addFloorBlocks(list, x1, x2, y1, z1, z2);
        Floor.addFloorBlocks(list, x1, x2, y2, z1, z2);
    }

    public static void addSkeletonCubeBlocks(List<BlockPos> list, int x1, int x2, int y1, int y2, int z1, int z2) {
        Line.addXLineBlocks(list, x1, x2, y1, z1);
        Line.addXLineBlocks(list, x1, x2, y1, z2);
        Line.addXLineBlocks(list, x1, x2, y2, z1);
        Line.addXLineBlocks(list, x1, x2, y2, z2);

        Line.addYLineBlocks(list, y1, y2, x1, z1);
        Line.addYLineBlocks(list, y1, y2, x1, z2);
        Line.addYLineBlocks(list, y1, y2, x2, z1);
        Line.addYLineBlocks(list, y1, y2, x2, z2);

        Line.addZLineBlocks(list, z1, z2, x1, y1);
        Line.addZLineBlocks(list, z1, z2, x1, y2);
        Line.addZLineBlocks(list, z1, z2, x2, y1);
        Line.addZLineBlocks(list, z1, z2, x2, y2);
    }

    @Override
    public EnumFacing getSideHit(EntityPlayer player) {
        return sideHitTable.get(player.getUniqueID());
    }

    @Override
    public Vec3d getHitVec(EntityPlayer player) {
        return hitVecTable.get(player.getUniqueID());
    }

}