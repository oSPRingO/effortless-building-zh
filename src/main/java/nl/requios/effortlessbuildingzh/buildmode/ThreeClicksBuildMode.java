package nl.requios.effortlessbuildingzh.buildmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;

import java.util.*;

public abstract class ThreeClicksBuildMode extends BaseBuildMode {
    protected Dictionary<UUID, BlockPos> secondPosTable = new Hashtable<>();

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
        public boolean isValid(Vec3d start, Vec3d look, int reach, PlayerEntity player, boolean skipRaytrace) {

            return BuildModes.isCriteriaValid(start, look, reach, player, skipRaytrace, lineBound, planeBound, distToPlayerSq);
        }
    }

    @Override
    public void initialize(PlayerEntity player) {
        super.initialize(player);
        secondPosTable.put(player.getUniqueID(), BlockPos.ZERO);
    }

    @Override
    public List<BlockPos> onRightClick(PlayerEntity player, BlockPos blockPos, Direction sideHit, Vec3d hitVec, boolean skipRaytrace) {
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
            BlockPos secondPos = findSecondPos(player, firstPos, true);

            if (secondPos == null) {
                rightClickTable.put(player.getUniqueID(), 1);
                return list;
            }

            secondPosTable.put(player.getUniqueID(), secondPos);

        } else {
            //Third click, place diagonal wall with height
            list = findCoordinates(player, blockPos, skipRaytrace);
            rightClickTable.put(player.getUniqueID(), 0);
        }

        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(PlayerEntity player, BlockPos blockPos, boolean skipRaytrace) {
        List<BlockPos> list = new ArrayList<>();
        Dictionary<UUID, Integer> rightClickTable = player.world.isRemote ? rightClickClientTable : rightClickServerTable;
        int rightClickNr = rightClickTable.get(player.getUniqueID());

        if (rightClickNr == 0) {
            if (blockPos != null)
                list.add(blockPos);
        } else if (rightClickNr == 1) {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());

            BlockPos secondPos = findSecondPos(player, firstPos, true);
            if (secondPos == null) return list;

            //Limit amount of blocks you can place per row
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

            //Add diagonal line from first to second
            list.addAll(getIntermediateBlocks(player, x1, y1, z1, x2, y2, z2));

        } else {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = secondPosTable.get(player.getUniqueID());

            BlockPos thirdPos = findThirdPos(player, firstPos, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Limit amount of blocks you can place per row
            int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

            int x1 = firstPos.getX(), x2 = secondPos.getX(), x3 = thirdPos.getX();
            int y1 = firstPos.getY(), y2 = secondPos.getY(), y3 = thirdPos.getY();
            int z1 = firstPos.getZ(), z2 = secondPos.getZ(), z3 = thirdPos.getZ();

            //limit axis
            if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
            if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
            if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
            if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
            if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
            if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

            if (x3 - x1 >= axisLimit) x3 = x1 + axisLimit - 1;
            if (x1 - x3 >= axisLimit) x3 = x1 - axisLimit + 1;
            if (y3 - y1 >= axisLimit) y3 = y1 + axisLimit - 1;
            if (y1 - y3 >= axisLimit) y3 = y1 - axisLimit + 1;
            if (z3 - z1 >= axisLimit) z3 = z1 + axisLimit - 1;
            if (z1 - z3 >= axisLimit) z3 = z1 - axisLimit + 1;

            //Add diagonal line from first to third
            list.addAll(getFinalBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3));
        }

        return list;
    }

    //Finds the place of the second block pos
    protected abstract BlockPos findSecondPos(PlayerEntity player, BlockPos firstPos, boolean skipRaytrace);

    //Finds the place of the third block pos
    protected abstract BlockPos findThirdPos(PlayerEntity player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace);

    //After first and second pos are known, we want to visualize the blocks in a way (like floor for cube)
    protected abstract List<BlockPos> getIntermediateBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2);

    //After first, second and third pos are known, we want all the blocks
    protected abstract List<BlockPos> getFinalBlocks(PlayerEntity player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3);

    //Finds height after floor has been chosen in buildmodes with 3 clicks
    public static BlockPos findHeight(PlayerEntity player, BlockPos secondPos, boolean skipRaytrace) {
        Vec3d look = BuildModes.getPlayerLookVec(player);
        Vec3d start = new Vec3d(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());

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
}
