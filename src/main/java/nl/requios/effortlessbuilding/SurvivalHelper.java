package nl.requios.effortlessbuilding;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import nl.requios.effortlessbuilding.item.ItemRandomizerBag;

import javax.annotation.Nullable;
import java.util.List;

public class SurvivalHelper {

    //From ItemBlock#onItemUse
    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState blockState, ItemStack itemstack, EnumFacing facing, boolean skipCollisionCheck, boolean playSound) {

        //Randomizer bag synergy
        //Find itemstack that belongs to the blockstate
        if (itemstack.getItem() == EffortlessBuilding.ITEM_RANDOMIZER_BAG) {
            IItemHandler bagInventory = ItemRandomizerBag.getBagInventory(itemstack);
            itemstack = ItemRandomizerBag.findStack(bagInventory, Item.getItemFromBlock(blockState.getBlock()));
        }

        //Check if itemstack is correct
        if (!(itemstack.getItem() instanceof ItemBlock) || Block.getBlockFromItem(itemstack.getItem()) != blockState.getBlock()) {
            EffortlessBuilding.log(player, "Cannot place block", true);
            EffortlessBuilding.log("SurvivalHelper#placeBlock: itemstack " + itemstack.toString() + " does not match blockstate " + blockState.toString());
            return false;
        }

        Block block = ((ItemBlock) itemstack.getItem()).getBlock();

        if (!itemstack.isEmpty() && canPlayerEdit(player, world, pos, itemstack) && mayPlace(world, block, pos, skipCollisionCheck, facing.getOpposite(), player))
        {
            //Drop existing block
            //TODO check if can replace
            dropBlock(world, pos, player);

            //From ItemBlock#placeBlockAt
            if (!world.setBlockState(pos, blockState, 11)) return false;

            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == block)
            {
                ((ItemBlock) itemstack.getItem()).setTileEntityNBT(world, player, pos, itemstack);
                block.onBlockPlacedBy(world, pos, state, player, itemstack);

//                if (player instanceof EntityPlayerMP)
//                    CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, itemstack);
            }

            if (playSound) {
                SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }

            if (!player.isCreative() && Block.getBlockFromItem(itemstack.getItem()) == block) {
                itemstack.shrink(1);
            }

            return true;
        }
        return false;
    }

    public static boolean canBreak(){
        //Can break using held tool? (or in creative)
        return true;
    }

    public static boolean canReplace(){
        //Can be harvested with hand? (or in creative)
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

    //From EntityPlayer#canPlayerEdit
    private static boolean canPlayerEdit(EntityPlayer player, World world, BlockPos pos, ItemStack stack)
    {
        if (player.capabilities.allowEdit)
        {
            return true;
        }
        else
        {
            Block block = world.getBlockState(pos).getBlock();
            return stack.canPlaceOn(block) || stack.canEditBlocks();
        }
    }

    //From World#mayPlace
    private static boolean mayPlace(World world, Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer)
    {
        IBlockState iblockstate1 = world.getBlockState(pos);
        AxisAlignedBB axisalignedbb = skipCollisionCheck ? null : blockIn.getDefaultState().getCollisionBoundingBox(world, pos);

        if (axisalignedbb != Block.NULL_AABB && !world.checkNoEntityCollision(axisalignedbb.offset(pos), placer))
        {
            return false;
        }
        else if (iblockstate1.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL)
        {
            return true;
        }
        else
        {
            //TODO check config for allow to replace
            return true;
            //TODO fix check canPlaceBlockOnSide
            //return /*iblockstate1.getBlock().isReplaceable(world, pos) &&*/ blockIn.canPlaceBlockOnSide(world, pos, sidePlacedOn);
        }
    }
}
