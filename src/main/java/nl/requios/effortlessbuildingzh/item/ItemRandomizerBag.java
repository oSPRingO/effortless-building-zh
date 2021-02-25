package nl.requios.effortlessbuildingzh.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmode.BuildModes;
import nl.requios.effortlessbuildingzh.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuildingzh.capability.ItemHandlerCapabilityProvider;
import nl.requios.effortlessbuildingzh.gui.RandomizerBagContainer;
import nl.requios.effortlessbuildingzh.helper.SurvivalHelper;

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

        this.setRegistryName(EffortlessBuildingZh.MODID, "randomizer_bag");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        Direction facing = ctx.getFace();
        ItemStack item = ctx.getItem();
        Vec3d hitVec = ctx.getHitVec();

        if (player == null) return ActionResultType.FAIL;

        if (ctx.func_225518_g_()) { //ctx.isPlacerSneaking()
            if (world.isRemote) return ActionResultType.SUCCESS;
            //Open inventory
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(item));
        } else {
            if (world.isRemote) return ActionResultType.SUCCESS;

            //Only place manually if in normal vanilla mode
            BuildModes.BuildModeEnum buildMode = ModeSettingsManager.getModeSettings(player).getBuildMode();
            ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
            if (buildMode != BuildModes.BuildModeEnum.NORMAL || modifierSettings.doQuickReplace()) {
                return ActionResultType.FAIL;
            }

            //Use item
            //Get bag inventory
            //TODO offhand support
            ItemStack bag = player.getHeldItem(Hand.MAIN_HAND);
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return ActionResultType.FAIL;

            ItemStack toPlace = pickRandomStack(bagInventory);
            if (toPlace.isEmpty()) return ActionResultType.FAIL;

            //Previously: use onItemUse to place block (no synergy)
            //bag.setItemDamage(toPlace.getMetadata());
            //toPlace.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

            //TODO replaceable
            if (!world.getBlockState(pos).getBlock().getMaterial(world.getBlockState(pos)).isReplaceable()) {
                pos = pos.offset(facing);
            }

            BlockItemUseContext blockItemUseContext = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(hitVec, facing, pos, false)));
            BlockState blockState = Block.getBlockFromItem(toPlace.getItem()).getStateForPlacement(blockItemUseContext);

            SurvivalHelper.placeBlock(world, player, pos, blockState, toPlace, facing, hitVec, false, false, true);

            //Synergy
            //Works without calling
//            BlockSnapshot blockSnapshot = new BlockSnapshot(player.world, pos, blockState);
//            BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(blockSnapshot, blockState, player, hand);
//            Mirror.onBlockPlaced(placeEvent);
//            Array.onBlockPlaced(placeEvent);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack bag = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (world.isRemote) return new ActionResult<>(ActionResultType.SUCCESS, bag);
            //Open inventory
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(bag));
        } else {
            //Use item
            //Get bag inventory
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return new ActionResult<>(ActionResultType.FAIL, bag);

            ItemStack toUse = pickRandomStack(bagInventory);
            if (toUse.isEmpty()) return new ActionResult<>(ActionResultType.FAIL, bag);

            return toUse.useItemRightClick(world, player, hand);
        }
        return new ActionResult<>(ActionResultType.PASS, bag);
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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ItemHandlerCapabilityProvider();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "右键" + TextFormatting.GRAY + "以放置一个随机方块"));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "潜行+右键" + TextFormatting.GRAY + "以打开袋子"));
        if (world != null && world.getPlayers().size() > 1) {
            tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "警告: 网络原因，物品可能会丢失"));
        }
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

    public static class ContainerProvider implements INamedContainerProvider{

        private ItemStack bag;

        public ContainerProvider(ItemStack bag) {
            this.bag = bag;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TranslationTextComponent("effortlessbuilding.screen.randomizer_bag");
        }

        @Nullable
        @Override
        public Container createMenu(int containerId, PlayerInventory playerInventory, PlayerEntity player) {
            return new RandomizerBagContainer(containerId, playerInventory, ItemRandomizerBag.getBagInventory(bag));
        }
    }
}
