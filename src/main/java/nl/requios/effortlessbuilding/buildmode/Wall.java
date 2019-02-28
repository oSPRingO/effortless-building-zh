package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class Wall implements IBuildMode {
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
        } else {
            //Second click, place wall

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

            //distance to player
            double xDistSquared = xBound.subtract(start).lengthSquared();

            //angle to look
            //double xAngle = xBound.subtract(new Vec3d(firstPos)).normalize().dotProduct(look);

            //try on z axis
            z = firstPos.getZ();

            //then x and y are
            x = (z - start.z) / look.z * look.x + start.x;
            y = (z - start.z) / look.z * look.y + start.y;

            Vec3d zBound = new Vec3d(x, y, z);

            //distance to player
            double zDistSquared = zBound.subtract(start).lengthSquared();

            //angle to look
            //double zAngle = zBound.subtract(new Vec3d(firstPos)).normalize().dotProduct(look);

            int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
            
            //check if its not behind the player and its not too close and not too far
            boolean xValid = xBound.subtract(start).dotProduct(look) > 0 &&
                             xDistSquared > 4 && xDistSquared < reach * reach;
            
            boolean zValid = zBound.subtract(start).dotProduct(look) > 0 &&
                             zDistSquared > 4 && zDistSquared < reach * reach;
            
            //select the one that is closest and is valid
            Vec3d selected = null;
            if (xValid)
                selected = xBound;
            else if (zValid)
                selected = zBound;
            if (zValid && zDistSquared < xDistSquared/*Math.abs(zAngle) < Math.abs(xAngle)*/) selected = zBound;

            if (selected == null) return list;

            //check if it doesnt go through blocks
            //TODO collision within a 1 block radius to selected is fine
            if (!skipRaytrace) {
                RayTraceResult rayTraceResult = player.world.rayTraceBlocks(start, selected, false, true, false);
                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    //return empty list
                    return list;
                }
            }
            
            BlockPos secondPos = new BlockPos(selected);

            //Add whole wall
            //Limit amount of blocks you can place per row
            int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);
            int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

            //limit axis
            if (x2 - x1 > axisLimit) x2 = x1 + axisLimit;
            if (x1 - x2 > axisLimit) x2 = x1 - axisLimit;
            if (y2 - y1 > axisLimit) y2 = y1 + axisLimit;
            if (y1 - y2 > axisLimit) y2 = y1 - axisLimit;
            if (z2 - z1 > axisLimit) z2 = z1 + axisLimit;
            if (z1 - z2 > axisLimit) z2 = z1 - axisLimit;

            for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {

                for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {

                    //check if whole row fits within limit
                    if (Math.abs(y1 - y2) < limit - list.size()) {

                        for (int m = y1; y1 < y2 ? m <= y2 : m >= y2; m += y1 < y2 ? 1 : -1) {
                            list.add(new BlockPos(l, m, n));
                        }
                    }
                }
            }
        }

        return list;
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
