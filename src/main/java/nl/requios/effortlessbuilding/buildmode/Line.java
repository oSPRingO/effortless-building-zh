package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class Line implements BuildMode {
    @Override
    public List<BlockPos> onRightClick(EntityPlayer player, BlockPos startPos) {
        return null;
    }

    @Override
    public List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        return null;
    }
}
