package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class Line implements IBuildMode {
    //In singleplayer client and server variables are shared
    //Split everything that needs separate values and may not be called twice in one click
    Dictionary<UUID, Integer> rightClickClientTable = new Hashtable<>();
    Dictionary<UUID, Integer> rightClickServerTable = new Hashtable<>();
    Dictionary<UUID, BlockPos> firstPosTable = new Hashtable<>();
    Dictionary<UUID, EnumFacing> sideHitTable = new Hashtable<>();
    Dictionary<UUID, Vec3d> hitVecTable = new Hashtable<>();

    @Override
    public void initialize(EntityPlayer player) {
        rightClickClientTable.put(player.getUniqueID(), 0);
        rightClickServerTable.put(player.getUniqueID(), 0);
        firstPosTable.put(player.getUniqueID(), BlockPos.ORIGIN);
        sideHitTable.put(player.getUniqueID(), EnumFacing.UP);
        hitVecTable.put(player.getUniqueID(), Vec3d.ZERO);
    }

    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec) {
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
        } else {
            //Second click, place wall

            list = findCoordinates(player, blockPos);
            rightClickTable.put(player.getUniqueID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos) {
        List<BlockPos> list = new ArrayList<>();
        Dictionary<UUID, Integer> rightClickTable = player.world.isRemote ? rightClickClientTable : rightClickServerTable;
        int rightClickNr = rightClickTable.get(player.getUniqueID());
        BlockPos firstPos = firstPosTable.get(player.getUniqueID());

        if (rightClickNr == 0) {
            if (blockPos != null)
                list.add(blockPos);
        } else {
            Vec3d look = player.getLookVec();
            Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);



            //try on x axis
            double x = firstPos.getX();

            //then y and z are
            double y = (x - start.x) / look.x * look.y + start.y;
            double z = (x - start.x) / look.x * look.z + start.z;

            Vec3d xBound = new Vec3d(x, y, z);
            Vec3d xBoundLine = toLongestLine(xBound, firstPos);
            double xDistToLine = xBoundLine.subtract(xBound).lengthSquared();

            //distance to player
            double xDistSquared = xBound.subtract(start).lengthSquared();



            //try on y axis
            y = firstPos.getY();

            //then x and z are
            x = (y - start.y) / look.y * look.x + start.x;
            z = (y - start.y) / look.y * look.z + start.z;

            Vec3d yBound = new Vec3d(x, y, z);
            Vec3d yBoundLine = toLongestLine(yBound, firstPos);
            double yDistToLine = yBoundLine.subtract(yBound).lengthSquared();

            //distance to player
            double yDistSquared = yBound.subtract(start).lengthSquared();



            //try on z axis
            z = firstPos.getZ();

            //then x and y are
            x = (z - start.z) / look.z * look.x + start.x;
            y = (z - start.z) / look.z * look.y + start.y;

            Vec3d zBound = new Vec3d(x, y, z);
            Vec3d zBoundLine = toLongestLine(zBound, firstPos);
            double zDistToLine = zBoundLine.subtract(zBound).lengthSquared();

            //distance to player
            double zDistSquared = zBound.subtract(start).lengthSquared();



            int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach

            //check if its not behind the player and its not too close and not too far
            boolean xValid = xBound.subtract(start).dotProduct(look) > 0 &&
                             xDistSquared > 4 && xDistSquared < reach * reach;

            boolean yValid = yBound.subtract(start).dotProduct(look) > 0 &&
                             yDistSquared > 4 && yDistSquared < reach * reach;

            boolean zValid = zBound.subtract(start).dotProduct(look) > 0 &&
                             zDistSquared > 4 && zDistSquared < reach * reach;

            //select the one that is closest (from wall position to its line counterpart) and is valid
            Vec3d selected = null;
            double selectedDistToLine = 0;

            if (xValid) {
                selected = xBoundLine;
                selectedDistToLine = xDistToLine;
            } else if (yValid) {
                selected = yBoundLine;
                selectedDistToLine = yDistToLine;
            } else if (zValid) {
                selected = zBoundLine;
                selectedDistToLine = yDistToLine;
            }

            if (yValid && yDistToLine < selectedDistToLine) selected = yBoundLine;
            if (zValid && zDistToLine < selectedDistToLine) selected = zBoundLine;

            if (selected == null) return list;

            //check if it doesnt go through blocks
            RayTraceResult rayTraceResult = player.world.rayTraceBlocks(start, selected, false, true, false);
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                //return empty list
                return list;
            }

            BlockPos secondPos = new BlockPos(selected);

            //Add whole line

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

                for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                    for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                        list.add(new BlockPos(l, m, n));
                    }
                }
            }
        }

        return list;
    }

    //Make it into a line
    //Select the axis that is longest
    private Vec3d toLongestLine(Vec3d boundVec, BlockPos firstPos) {
        BlockPos bound = new BlockPos(boundVec);

        BlockPos firstToSecond = bound.subtract(firstPos);
        firstToSecond = new BlockPos(Math.abs(firstToSecond.getX()), Math.abs(firstToSecond.getY()), Math.abs(firstToSecond.getZ()));
        int longest = Math.max(firstToSecond.getX(), Math.max(firstToSecond.getY(), firstToSecond.getZ()));
        if (longest == firstToSecond.getX()) {
            return new Vec3d(bound.getX(), firstPos.getY(), firstPos.getZ());
        }
        if (longest == firstToSecond.getY()) {
            return new Vec3d(firstPos.getX(), bound.getY(), firstPos.getZ());
        }
        if (longest == firstToSecond.getZ()) {
            return new Vec3d(firstPos.getX(), firstPos.getY(), bound.getZ());
        }
        return null;
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
