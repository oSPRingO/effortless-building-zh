package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class SurvivalHelper {

    public static boolean canBreak(){
        return true;
    }

    public static boolean canPlace(){
        return true;
    }

    //Gives items directly to player
    public static void dropBlock(World world, BlockPos pos, EntityPlayer player){
        if (player.isCreative()) return;

        IBlockState blockState = world.getBlockState(pos);

        List<ItemStack> drops = blockState.getBlock().getDrops(world, pos, blockState, 0);
        for (ItemStack drop : drops)
        {
            ItemHandlerHelper.giveItemToPlayer(player, drop);
        }
    }
}
