package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.helper.FixedStack;
import nl.requios.effortlessbuilding.helper.InventoryHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

import java.util.*;

public class UndoRedo {

    //Undo and redo stacks per player
    //Gets added to twice in singleplayer (server and client) if not careful. So separate stacks.
    private static Map<UUID, FixedStack<BlockSet>> undoStacksClient = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> undoStacksServer = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> redoStacksClient = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> redoStacksServer = new HashMap<>();

    //add to undo stack
    public static void addUndo(EntityPlayer player, BlockSet blockSet) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;

        //If no stack exists, make one
        if (!undoStacks.containsKey(player.getUniqueID())) {
            undoStacks.put(player.getUniqueID(), new FixedStack<>(new BlockSet[BuildConfig.survivalBalancers.undoStackSize]));
        }

        undoStacks.get(player.getUniqueID()).push(blockSet);
    }

    private static void addRedo(EntityPlayer player, BlockSet blockSet) {
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;

        //If no stack exists, make one
        if (!redoStacks.containsKey(player.getUniqueID())) {
            redoStacks.put(player.getUniqueID(), new FixedStack<>(new BlockSet[BuildConfig.survivalBalancers.undoStackSize]));
        }

        redoStacks.get(player.getUniqueID()).push(blockSet);
    }

    public static boolean undo(EntityPlayer player) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;

        if (!undoStacks.containsKey(player.getUniqueID())) return false;

        FixedStack<BlockSet> undoStack = undoStacks.get(player.getUniqueID());

        if (undoStack.isEmpty()) return false;

        BlockSet blockSet = undoStack.pop();
        List<BlockPos> coordinates = blockSet.getCoordinates();
        List<IBlockState> previousBlockStates = blockSet.getPreviousBlockStates();
        List<IBlockState> newBlockStates = blockSet.getNewBlockStates();
        Vec3d hitVec = blockSet.getHitVec();

        //Find up to date itemstacks in player inventory
        List<ItemStack> itemStacks = findItemStacksInInventory(player, previousBlockStates);

        if (player.world.isRemote) {
            BlockPreviewRenderer.onBlocksBroken(coordinates, itemStacks, newBlockStates, blockSet.getSecondPos(), blockSet.getFirstPos());
        } else {
            //break all those blocks, reset to what they were
            for (int i = 0; i < coordinates.size(); i++) {
                BlockPos coordinate = coordinates.get(i);
                ItemStack itemStack = itemStacks.get(i);

                //get blockstate from itemstack
                IBlockState previousBlockState = Block.getBlockById(0).getDefaultState();
                if (itemStack.getItem() instanceof ItemBlock) {
                    previousBlockState = ((ItemBlock) itemStack.getItem()).getBlock().getDefaultState();
                }

                if (player.world.isBlockLoaded(coordinate, true)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        itemStack = findItemStackInInventory(player, previousBlockStates.get(i));
                        //get blockstate from new itemstack
                        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                            previousBlockState = ((ItemBlock) itemStack.getItem()).getBlock().getDefaultState();
                        }
                    }
                    if (itemStack.isEmpty()) SurvivalHelper.breakBlock(player.world, player, coordinate, true);
                    //if previousBlockState is air, placeBlock will set it to air
                    SurvivalHelper.placeBlock(player.world, player, coordinate, previousBlockState, itemStack, EnumFacing.UP, hitVec, true, false, false);
                }
            }
        }

        //add to redo
        addRedo(player, blockSet);

        return true;
    }

    public static boolean redo(EntityPlayer player) {
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;

        if (!redoStacks.containsKey(player.getUniqueID())) return false;

        FixedStack<BlockSet> redoStack = redoStacks.get(player.getUniqueID());

        if (redoStack.isEmpty()) return false;

        BlockSet blockSet = redoStack.pop();
        List<BlockPos> coordinates = blockSet.getCoordinates();
        List<IBlockState> newBlockStates = blockSet.getNewBlockStates();
        Vec3d hitVec = blockSet.getHitVec();

        //Find up to date itemstacks in player inventory
        List<ItemStack> itemStacks = findItemStacksInInventory(player, newBlockStates);

        if (player.world.isRemote) {
            BlockPreviewRenderer.onBlocksPlaced(coordinates, itemStacks, newBlockStates, blockSet.getFirstPos(), blockSet.getSecondPos());
        } else {
            //place blocks
            for (int i = 0; i < coordinates.size(); i++) {
                BlockPos coordinate = coordinates.get(i);
                IBlockState newBlockState = newBlockStates.get(i);
                ItemStack itemStack = itemStacks.get(i);

                if (player.world.isBlockLoaded(coordinate, true)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        itemStack = findItemStackInInventory(player, newBlockStates.get(i));
                        //get blockstate from new itemstack
                        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                            newBlockState = ((ItemBlock) itemStack.getItem()).getBlock().getDefaultState();
                        }
                    }
                    if (itemStack.isEmpty()) SurvivalHelper.breakBlock(player.world, player, coordinate, true);
                    SurvivalHelper.placeBlock(player.world, player, coordinate, newBlockState, itemStack, EnumFacing.UP, hitVec, true, false, false);
                }
            }
        }

        //add to undo
        addUndo(player, blockSet);

        return true;
    }

    public static void clear(EntityPlayer player) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;
        if (undoStacks.containsKey(player.getUniqueID())) {
            undoStacks.get(player.getUniqueID()).clear();
        }
        if (redoStacks.containsKey(player.getUniqueID())) {
            redoStacks.get(player.getUniqueID()).clear();
        }
    }

    private static List<ItemStack> findItemStacksInInventory(EntityPlayer player, List<IBlockState> blockStates) {
        List<ItemStack> itemStacks = new ArrayList<>(blockStates.size());
        for (IBlockState blockState : blockStates) {
            itemStacks.add(findItemStackInInventory(player, blockState));
        }
        return itemStacks;
    }

    private static ItemStack findItemStackInInventory(EntityPlayer player, IBlockState blockState) {
        ItemStack itemStack = ItemStack.EMPTY;

        //First try previousBlockStates
        itemStack = InventoryHelper.findItemStackInInventory(player, blockState.getBlock());

        //then anything it drops
        if (itemStack.isEmpty()) {
            Item itemDropped = blockState.getBlock().getItemDropped(blockState, player.world.rand, 10);
            if (itemDropped instanceof ItemBlock) {
                Block block = ((ItemBlock) itemDropped).getBlock();
                itemStack = InventoryHelper.findItemStackInInventory(player, block);
            }
        }

        //then air
        //(already empty)

        return itemStack;
    }
}
