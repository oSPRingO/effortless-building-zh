package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Normal implements BuildMode {
    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> list = new ArrayList<>();
        list.add(startPos);
        return list;
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> list = new ArrayList<>();
        list.add(startPos);
        return list;
    }
}
