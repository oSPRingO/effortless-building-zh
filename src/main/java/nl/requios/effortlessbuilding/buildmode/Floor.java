package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class Floor implements IBuildMode {
    //In singleplayer client and server variables are shared
    //Split everything that needs separate values and may not be called twice in one click
    Dictionary<UUID, Integer> rightClickClientTable = new Hashtable<>();
    Dictionary<UUID, Integer> rightClickServerTable = new Hashtable<>();
    Dictionary<UUID, BlockPos> firstPosTable = new Hashtable<>();
    Dictionary<UUID, EnumFacing> sideHitTable = new Hashtable<>();
    Dictionary<UUID, Vec3d> hitVecTable = new Hashtable<>();

    class Criteria {
        Vec3d planeBound;
        double distToPlayerSq;

        Criteria(Vec3d planeBound, Vec3d start) {
            this.planeBound = planeBound;
            this.distToPlayerSq = this.planeBound.subtract(start).lengthSquared();
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3d start, Vec3d look, int reach, EntityPlayer player, boolean skipRaytrace) {

            boolean intersects = false;
            if (!skipRaytrace) {
                //collision within a 1 block radius to selected is fine
                RayTraceResult rayTraceResult = player.world.rayTraceBlocks(start, planeBound, false, true, false);
                intersects = rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK &&
                    planeBound.subtract(rayTraceResult.hitVec).lengthSquared() > 4;
            }

            return planeBound.subtract(start).dotProduct(look) > 0 &&
                   distToPlayerSq > 2 && distToPlayerSq < reach * reach &&
                   !intersects;
        }
    }

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
            BlockPos secondPos = findFloor(player, firstPos, skipRaytrace);
            if (secondPos == null) return list;

            //Add whole floor
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

    public BlockPos findFloor(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        List<Criteria> criteriaList = new ArrayList<>(3);

        //Y
        Vec3d yBound = BuildModes.findYBound(firstPos.getY(), start, look);
        criteriaList.add(new Criteria(yBound, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //Then only 1 can be valid, return that one
        Criteria selected = criteriaList.get(0);

        return new BlockPos(selected.planeBound);
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
