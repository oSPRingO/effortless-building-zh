package nl.requios.effortlessbuilding.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.ItemHandlerCapabilityProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemRandomizerBag extends Item {
    public static final int INV_SIZE = 5;

    private Random rand;

    public ItemRandomizerBag() {
        this.setRegistryName(EffortlessBuilding.MODID, "randomizer_bag");
        this.setUnlocalizedName(this.getRegistryName().toString());

        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        rand = new Random(1337);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (player.isSneaking()) {
            if (world.isRemote) return EnumActionResult.SUCCESS;
            //Open inventory
            player.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, world, 0, 0, 0);
        } else {
            //Place block
            return placeRandomBlockFromBag(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult placeRandomBlockFromBag(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        //Get bag inventory
        ItemStack bag = player.getHeldItem(hand);
        if (!bag.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) return EnumActionResult.FAIL;
        IItemHandler bagInventory = bag.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        //Find how many stacks are non-empty, and save them in a list
        int nonempty = 0;
        List<ItemStack> nonEmptyStacks = new ArrayList<>(5);
        List<Integer> originalSlots = new ArrayList<>(5);
        for (int i = 0; i < bagInventory.getSlots(); i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                nonempty++;
                nonEmptyStacks.add(stack);
                originalSlots.add(i);
            }
        }

        if (nonEmptyStacks.size() != originalSlots.size()) throw new Error("NonEmptyStacks and OriginalSlots not same size");

        if (nonempty == 0) return EnumActionResult.FAIL;

        //Pick random slot
        int randomSlot = rand.nextInt(nonempty);

        ItemStack toPlace = nonEmptyStacks.get(randomSlot);
        if (toPlace.isEmpty()) return EnumActionResult.FAIL;

        if (toPlace.getItem() instanceof ItemBlock) {
            IBlockState existingBlockState = world.getBlockState(pos);
            Block existingBlock = existingBlockState.getBlock();

            if (!existingBlock.isReplaceable(world, pos))
            {
                pos = pos.offset(facing);
            }

            Block block = Block.getBlockFromItem(toPlace.getItem());
            if (!toPlace.isEmpty() && player.canPlayerEdit(pos, facing, toPlace) && world.mayPlace(block, pos, false, facing, (Entity)null)) {
                IBlockState blockState = block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, toPlace.getMetadata(), player, hand);
                if (world.setBlockState(pos, blockState)){
                    SoundType soundType = block.getSoundType(blockState, world, pos, player);
                    world.playSound(player, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

                    if (!player.isCreative())
                        bagInventory.extractItem(originalSlots.get(randomSlot), 1, false);
                }

                //((ItemBlock) toPlace.getItem()).placeBlockAt(toPlace, player, world, pos, facing, hitX, hitY, hitZ, blockState);
                //return ((ItemBlock) toPlace.getItem()).onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));

        //Open inventory
        player.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, world, 0, 0, 0);

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemHandlerCapabilityProvider();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("Sneak + rightclick to open inventory");
        tooltip.add("Rightclick to place a random block");
    }
}
