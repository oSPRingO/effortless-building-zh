package nl.requios.effortlessbuilding.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.Array;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.Mirror;
import nl.requios.effortlessbuilding.SurvivalHelper;
import nl.requios.effortlessbuilding.capability.ItemHandlerCapabilityProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemRandomizerBag extends Item {
    public static final int INV_SIZE = 5;

    private static Random rand = new Random(1337);

    public ItemRandomizerBag() {
        this.setRegistryName(EffortlessBuilding.MODID, "randomizer_bag");
        this.setUnlocalizedName(this.getRegistryName().toString());

        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (player.isSneaking()) {
            if (world.isRemote) return EnumActionResult.SUCCESS;
            //Open inventory
            player.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, world, 0, 0, 0);
        } else {
            if (world.isRemote) return EnumActionResult.SUCCESS;
            //Use item
            //Get bag inventory
            ItemStack bag = player.getHeldItem(hand);
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return EnumActionResult.FAIL;

            ItemStack toPlace = pickRandomStack(bagInventory);
            if (toPlace.isEmpty()) return EnumActionResult.FAIL;

            //Previously: use onItemUse to place block (no synergy)
            //bag.setItemDamage(toPlace.getMetadata());
            //toPlace.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

            if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                pos = pos.offset(facing);
            }

//            if (!player.isCreative()) {
//                toPlace.shrink(1);
//            }

            IBlockState blockState = Block.getBlockFromItem(toPlace.getItem()).getStateForPlacement(world, pos, facing,
                    hitX, hitY, hitZ, toPlace.getMetadata(), player, hand);
            //world.setBlockState(pos, blockState);
            SurvivalHelper.placeBlock(world, player, pos, blockState, toPlace, facing, false, true);

            //Synergy
            //Works without calling
//            BlockSnapshot blockSnapshot = new BlockSnapshot(player.world, pos, blockState);
//            BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(blockSnapshot, blockState, player, hand);
//            Mirror.onBlockPlaced(placeEvent);
//            Array.onBlockPlaced(placeEvent);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        if (player.isSneaking()) {
            if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            //Open inventory
            player.openGui(EffortlessBuilding.instance, EffortlessBuilding.RANDOMIZER_BAG_GUI, world, 0, 0, 0);
        } else {
            //Use item
            //Get bag inventory
            ItemStack bag = player.getHeldItem(hand);
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));

            ItemStack toUse = pickRandomStack(bagInventory);
            if (toUse.isEmpty()) return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));

            return toUse.useItemRightClick(world, player, hand);
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    /**
     * Get the inventory of a randomizer bag by checking the capability.
     *
     * @param bag
     * @return
     */
    public static IItemHandler getBagInventory(ItemStack bag) {
        if (!bag.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) return null;
        return bag.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    /**
     * Pick a random slot from the bag. Empty slots will never get chosen.
     *
     * @param bagInventory
     * @return
     */
    public static ItemStack pickRandomStack(IItemHandler bagInventory) {
        //Find how many stacks are non-empty, and save them in a list
        int nonempty = 0;
        List<ItemStack> nonEmptyStacks = new ArrayList<>(INV_SIZE);
        List<Integer> originalSlots = new ArrayList<>(INV_SIZE);
        for (int i = 0; i < bagInventory.getSlots(); i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                nonempty++;
                nonEmptyStacks.add(stack);
                originalSlots.add(i);
            }
        }

        if (nonEmptyStacks.size() != originalSlots.size())
            throw new Error("NonEmptyStacks and OriginalSlots not same size");

        if (nonempty == 0) return ItemStack.EMPTY;

        //Pick random slot
        int randomSlot = rand.nextInt(nonempty);
        if (randomSlot < 0 || randomSlot > bagInventory.getSlots()) return ItemStack.EMPTY;

        int originalSlot = originalSlots.get(randomSlot);
        if (originalSlot < 0 || originalSlot > bagInventory.getSlots()) return ItemStack.EMPTY;

        return bagInventory.getStackInSlot(originalSlot);
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
        tooltip.add(TextFormatting.BLUE + "Rightclick" + TextFormatting.GRAY + " to place a random block");
        tooltip.add(TextFormatting.BLUE + "Sneak + rightclick" + TextFormatting.GRAY + " to open inventory");
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }
}
