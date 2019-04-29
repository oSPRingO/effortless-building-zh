package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class DiagonalLine implements IBuildMode {
    //In singleplayer client and server variables are shared
    //Split everything that needs separate values and may not be called twice in one click
    private Dictionary<UUID, Integer> rightClickClientTable = new Hashtable<>();
    private Dictionary<UUID, Integer> rightClickServerTable = new Hashtable<>();
    private Dictionary<UUID, BlockPos> firstPosTable = new Hashtable<>();
    private Dictionary<UUID, BlockPos> secondPosTable = new Hashtable<>();
    private Dictionary<UUID, EnumFacing> sideHitTable = new Hashtable<>();
    private Dictionary<UUID, Vec3d> hitVecTable = new Hashtable<>();

    static class HeightCriteria {
        Vec3d planeBound;
        Vec3d lineBound;
        double distToLineSq;
        double distToPlayerSq;

        HeightCriteria(Vec3d planeBound, BlockPos secondPos, Vec3d start) {
            this.planeBound = planeBound;
            this.lineBound = toLongestLine(this.planeBound, secondPos);
            this.distToLineSq = this.lineBound.subtract(this.planeBound).lengthSquared();
            this.distToPlayerSq = this.planeBound.subtract(start).lengthSquared();
        }

        //Make it from a plane into a line, on y axis only
        private Vec3d toLongestLine(Vec3d boundVec, BlockPos secondPos) {
            BlockPos bound = new BlockPos(boundVec);
            return new Vec3d(secondPos.getX(), bound.getY(), secondPos.getZ());
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3d start, Vec3d look, int reach, EntityPlayer player, boolean skipRaytrace) {

            boolean intersects = false;
            if (!skipRaytrace) {
                //collision within a 1 block radius to selected is fine
                RayTraceResult rayTraceResult = player.world.rayTraceBlocks(start, lineBound, false, true, false);
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
        } else if (rightClickNr == 2) {
            //Second click, find other floor point
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = Floor.findFloor(player, firstPos, true);

            if (secondPos == null) {
                rightClickTable.put(player.getUniqueID(), 1);
                return list;
            }

            secondPosTable.put(player.getUniqueID(), secondPos);

        } else {
            //Third click, place diagonal line with height
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

            BlockPos secondPos = Floor.findFloor(player, firstPos, true);
            if (secondPos == null) return list;

            //Add diagonal line from first to second
            list.addAll(getDiagonalLineBlocks(player , firstPos, secondPos, 10));

        } else {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = secondPosTable.get(player.getUniqueID());

            BlockPos thirdPos = findHeight(player, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Add diagonal line from first to third
            list.addAll(getDiagonalLineBlocks(player , firstPos, thirdPos, 10));
        }

        return list;
    }
    
    //Finds height after floor has been chosen in buildmodes with 3 clicks
    public static BlockPos findHeight(EntityPlayer player, BlockPos secondPos, boolean skipRaytrace) {
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        List<HeightCriteria> criteriaList = new ArrayList<>(3);

        //X
        Vec3d xBound = BuildModes.findXBound(secondPos.getX(), start, look);
        criteriaList.add(new HeightCriteria(xBound, secondPos, start));

        //Z
        Vec3d zBound = BuildModes.findZBound(secondPos.getZ(), start, look);
        criteriaList.add(new HeightCriteria(zBound, secondPos, start));

        //Remove invalid criteria
        int reach = ReachHelper.getPlacementReach(player) * 4; //4 times as much as normal placement reach
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        HeightCriteria selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                HeightCriteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }
        }
        return new BlockPos(selected.lineBound);
    }

    //Add diagonal line from first to second
    public static List<BlockPos> getDiagonalLineBlocks(EntityPlayer player, BlockPos firstPos, BlockPos secondPos, float sampleMultiplier) {
        List<BlockPos> list = new ArrayList<>();

        int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

        int x1 = firstPos.getX(), x2 = secondPos.getX();
        int y1 = firstPos.getY(), y2 = secondPos.getY();
        int z1 = firstPos.getZ(), z2 = secondPos.getZ();

        //limit axis
        if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
        if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
        if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
        if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
        if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
        if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

        Vec3d first = new Vec3d(x1, y1, z1).add(0.5, 0.5, 0.5);
        Vec3d second = new Vec3d(x2, y2, z2).add(0.5, 0.5, 0.5);

        int iterations = (int) Math.ceil(first.distanceTo(second) * sampleMultiplier);
        for (double t = 0; t <= 1.0; t += 1.0/iterations) {
            Vec3d lerp = first.add(second.subtract(first).scale(t));
            BlockPos candidate = new BlockPos(lerp);
            //Only add if not equal to the last in the list
            if (list.isEmpty() || !list.get(list.size() - 1).equals(candidate))
                list.add(candidate);
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