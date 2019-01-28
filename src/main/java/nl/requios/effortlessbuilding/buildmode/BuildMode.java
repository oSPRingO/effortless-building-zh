package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface BuildMode {

    List<BlockPos> onRightClick(EntityPlayer player, BlockPos startPos);

    List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos);
}
