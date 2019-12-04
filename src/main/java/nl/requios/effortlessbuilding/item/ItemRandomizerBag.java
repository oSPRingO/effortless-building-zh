package nl.requios.effortlessbuilding.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.capability.ItemHandlerCapabilityProvider;
import nl.requios.effortlessbuilding.gui.RandomizerBagContainer;
import nl.requios.effortlessbuilding.gui.RandomizerBagGuiHandler;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class ItemRandomizerBag extends Item {
    public static final int INV_SIZE = 5;

    private static long currentSeed = 1337;
    private static Random rand = new Random(currentSeed);

    public ItemRandomizerBag() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));

        this.setRegistryName(EffortlessBuilding.MODID, "randomizer_bag");
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        EntityPlayer player = ctx.getPlayer();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        EnumFacing facing = ctx.getFace();
        ItemStack item = ctx.getItem();

        if (player == null) return EnumActionResult.FAIL;

        if (ctx.isPlacerSneaking()) {
            if (world.isRemote) return EnumActionResult.SUCCESS;
            //Open inventory
            NetworkHooks.openGui((EntityPlayerMP) player, new RandomizerBagGuiHandler());
        } else {
            if (world.isRemote) return EnumActionResult.SUCCESS;

            //Only place manually if in normal vanilla mode
            BuildModes.BuildModeEnum buildMode = ModeSettingsManager.getModeSettings(player).getBuildMode();
            ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
            if (buildMode != BuildModes.BuildModeEnum.NORMAL || modifierSettings.doQuickReplace()) {
                return EnumActionResult.FAIL;
            }

            //Use item
            //Get bag inventory
            //TODO offhand support
            ItemStack bag = player.getHeldItem(EnumHand.MAIN_HAND);
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return EnumActionResult.FAIL;

            ItemStack toPlace = pickRandomStack(bagInventory);
            if (toPlace.isEmpty()) return EnumActionResult.FAIL;

            //Previously: use onItemUse to place block (no synergy)
            //bag.setItemDamage(toPlace.getMetadata());
            //toPlace.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

            //TODO replaceable
            if (!world.getBlockState(pos).getBlock().getMaterial(world.getBlockState(pos)).isReplaceable()) {
                pos = pos.offset(facing);
            }

            BlockItemUseContext blockItemUseContext = new BlockItemUseContext(world, player, item, pos, facing, ctx.getHitX(), ctx.getHitY(), ctx.getHitZ());
            IBlockState blockState = Block.getBlockFromItem(toPlace.getItem()).getStateForPlacement(blockItemUseContext);

            SurvivalHelper.placeBlock(world, player, pos, blockState, toPlace, facing, new Vec3d(ctx.getHitX(), ctx.getHitY(), ctx.getHitZ()), false, false, true);

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
        ItemStack bag = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, bag);
            //Open inventory
            NetworkHooks.openGui((EntityPlayerMP) player, new RandomizerBagGuiHandler());
        } else {
            //Use item
            //Get bag inventory
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return new ActionResult<>(EnumActionResult.FAIL, bag);

            ItemStack toUse = pickRandomStack(bagInventory);
            if (toUse.isEmpty()) return new ActionResult<>(EnumActionResult.FAIL, bag);

            return toUse.useItemRightClick(world, player, hand);
        }
        return new ActionResult<>(EnumActionResult.PASS, bag);
    }

    /**
     * Get the inventory of a randomizer bag by checking the capability.
     *
     * @param bag
     * @return
     */
    public static IItemHandler getBagInventory(ItemStack bag) {
        return bag.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
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

    public static ItemStack findStack(IItemHandler bagInventory, Item item) {
        for (int i = 0; i < bagInventory.getSlots(); i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {
        return 1;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ItemHandlerCapabilityProvider();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TextComponentString(TextFormatting.BLUE + "Rightclick" + TextFormatting.GRAY + " to place a random block"));
        tooltip.add(new TextComponentString(TextFormatting.BLUE + "Sneak + rightclick" + TextFormatting.GRAY + " to open inventory"));
    }

    @Override
    public String getTranslationKey() {
        return this.getRegistryName().toString();
    }

    public static void resetRandomness() {
        rand.setSeed(currentSeed);
    }

    public static void renewRandomness() {
        currentSeed = Calendar.getInstance().getTimeInMillis();
        rand.setSeed(currentSeed);
    }
}
