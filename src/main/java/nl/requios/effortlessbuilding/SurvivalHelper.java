package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

    //Used for all placing of blocks in this mod.
    //Checks if area is loaded, if player has the right permissions, if existing block can be replaced (drops it if so) and consumes an item from the stack.
    //Based on ItemBlock#onItemUse
    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState blockState, ItemStack itemstack, EnumFacing facing, boolean skipCollisionCheck, boolean playSound) {
        if (!world.isBlockLoaded(pos, true)) return false;

        //Randomizer bag synergy
        //Find itemstack that belongs to the blockstate
        if (itemstack.getItem() == EffortlessBuilding.ITEM_RANDOMIZER_BAG) {
            IItemHandler bagInventory = ItemRandomizerBag.getBagInventory(itemstack);
            itemstack = ItemRandomizerBag.findStack(bagInventory, Item.getItemFromBlock(blockState.getBlock()));
        }

        //Check if itemstack is correct
        if (!(itemstack.getItem() instanceof ItemBlock) || Block.getBlockFromItem(itemstack.getItem()) != blockState.getBlock()) {
            EffortlessBuilding.log(player, "Cannot replace block", true);
            EffortlessBuilding.log("SurvivalHelper#placeBlock: itemstack " + itemstack.toString() + " does not match blockstate " + blockState.toString());
            return false;
        }

        Block block = ((ItemBlock) itemstack.getItem()).getBlock();

        if (!itemstack.isEmpty() && canPlayerEdit(player, world, pos, itemstack) &&
            mayPlace(world, block, blockState, pos, skipCollisionCheck, facing.getOpposite(), player) &&
            canReplace(world, player, pos))
        {
            //Drop existing block
            //TODO check if can replace
            dropBlock(world, player, pos);

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

    //Used for all breaking of blocks in this mod.
    //Checks if area is loaded, if appropriate tool is used in survival mode, and drops the block directly into the players inventory
    public static boolean breakBlock(World world, EntityPlayer player, BlockPos pos) {
        if (!world.isBlockLoaded(pos, false)) return false;

        //Check if can break
        if (canBreak(world, player, pos))
        {
            //Drop existing block
            dropBlock(world, player, pos);

            //Damage tool
            player.getHeldItemMainhand().onBlockDestroyed(world, world.getBlockState(pos), pos, player);

            world.setBlockToAir(pos);
            return true;
        }
        return false;
    }

    //Can break using held tool? (or in creative)
    public static boolean canBreak(World world, EntityPlayer player, BlockPos pos) {
        if (player.isCreative()) return true;

        IBlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock().canHarvestBlock(world, pos, player)) return true;

        return false;
    }

    //Can be harvested with hand? (or in creative)
    public static boolean canReplace(World world, EntityPlayer player, BlockPos pos){
        if (player.isCreative()) return true;

        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        if (state.getMaterial().isToolNotRequired()) return true;

        return false;
    }

    //Gives items directly to player
    public static void dropBlock(World world, EntityPlayer player, BlockPos pos){
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
    private static boolean mayPlace(World world, Block blockIn, IBlockState newBlockState, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer)
    {
        IBlockState iblockstate1 = world.getBlockState(pos);
        AxisAlignedBB axisalignedbb = skipCollisionCheck ? null : blockIn.getDefaultState().getCollisionBoundingBox(world, pos);

        if (axisalignedbb != Block.NULL_AABB && !world.checkNoEntityCollision(axisalignedbb.offset(pos), placer))
        {
            return false;
        }

        //Check if same block
        //Necessary otherwise extra items will be dropped
        if (iblockstate1 == newBlockState) {
            return false;
        }

        if (iblockstate1.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL)
        {
            return true;
        }

        //TODO check config for allow to replace
        return true;
        //TODO fix check canPlaceBlockOnSide
        //return /*iblockstate1.getBlock().isReplaceable(world, pos) &&*/ blockIn.canPlaceBlockOnSide(world, pos, sidePlacedOn);
    }
}
