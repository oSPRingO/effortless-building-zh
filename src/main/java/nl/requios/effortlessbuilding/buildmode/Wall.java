package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class Wall implements IBuildMode {
    Dictionary<UUID, Integer> rightClickNrTable = new Hashtable<>();
    Dictionary<UUID, BlockPos> firstPosTable = new Hashtable<>();
    Dictionary<UUID, EnumFacing> sideHitTable = new Hashtable<>();
    Dictionary<UUID, Vec3d> hitVecTable = new Hashtable<>();

    @Override
    public void initialize(EntityPlayer player) {
        rightClickNrTable.put(player.getUniqueID(), 0);
        firstPosTable.put(player.getUniqueID(), BlockPos.ORIGIN);
        sideHitTable.put(player.getUniqueID(), EnumFacing.UP);
        hitVecTable.put(player.getUniqueID(), Vec3d.ZERO);
    }

    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec) {
        List<BlockPos> list = new ArrayList<>();

        int rightClickNr = rightClickNrTable.get(player.getUniqueID());
        rightClickNr++;
        rightClickNrTable.put(player.getUniqueID(), rightClickNr);

        if (rightClickNr == 1) {
            //If clicking in air, reset and try again
            if (blockPos == null) {
                rightClickNrTable.put(player.getUniqueID(), 0);
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
            rightClickNrTable.put(player.getUniqueID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos blockPos) {
        List<BlockPos> list = new ArrayList<>();
        int rightClickNr = rightClickNrTable.get(player.getUniqueID());
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

            //try on z axis
            z = firstPos.getZ();

            //then x and y are
            x = (z - start.z) / look.z * look.x + start.x;
            y = (z - start.z) / look.z * look.y + start.y;

            Vec3d zBound = new Vec3d(x, y, z);

            //distance to player
            double zDistSquared = zBound.subtract(start).lengthSquared();

            int reach = ReachHelper.getMaxReach(player); //4 times as much as normal placement reach
            
            //check if its not behind the player and its not too close and not too far
            boolean xValid = xBound.subtract(start).dotProduct(look) > 0 &&
                             xDistSquared > 4 && xDistSquared < reach * reach;
            
            boolean zValid = zBound.subtract(start).dotProduct(look) > 0 &&
                             zDistSquared > 4 && zDistSquared < reach * reach;
            
            //select the one that is closest to the player and is valid
            Vec3d selected = null;
            if (xValid) selected = xBound;
            if (zValid && zDistSquared < xDistSquared) selected = zBound;

            if (selected == null) return list;

            //check if it doesnt go through blocks
            RayTraceResult rayTraceResult = player.world.rayTraceBlocks(start, selected, false, false, false);
            if (rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
                //return empty list
                return list;
            }
            
            BlockPos secondPos = new BlockPos(selected);

            //Add whole wall
            //Limit amount of blocks you can place per row
            int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ();

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
