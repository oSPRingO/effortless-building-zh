package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.util.*;

public class DiagonalWall implements IBuildMode {
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
            BlockPos secondPos = Floor.findFloor(player, firstPos, true);

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

            //Add diagonal line
            list.addAll(DiagonalLine.getDiagonalLineBlocks(player, firstPos, secondPos, 1));

        } else {
            BlockPos firstPos = firstPosTable.get(player.getUniqueID());
            BlockPos secondPos = secondPosTable.get(player.getUniqueID());

            BlockPos thirdPos = DiagonalLine.findHeight(player, secondPos, skipRaytrace);
            if (thirdPos == null) return list;

            //Add diagonal wall
            list.addAll(getDiagonalWallBlocks(player, firstPos, secondPos, thirdPos));
        }

        return list;
    }

    //Add diagonal wall from first to second
    public static List<BlockPos> getDiagonalWallBlocks(EntityPlayer player, BlockPos firstPos, BlockPos secondPos, BlockPos thirdPos) {
        List<BlockPos> list = new ArrayList<>();

        int axisLimit = ReachHelper.getMaxBlocksPerAxis(player);

        //Get diagonal line blocks
        List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(player, firstPos, secondPos, 1);

        //Limit amount of blocks we can place
        int lowest = Math.min(firstPos.getY(), thirdPos.getY());
        int highest = Math.max(firstPos.getY(), thirdPos.getY());

        if (highest - lowest >= axisLimit) highest = lowest + axisLimit - 1;

        //Copy diagonal line on y axis
        for (int y = lowest; y <= highest; y++) {
            for (BlockPos blockPos : diagonalLineBlocks) {
                list.add(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
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