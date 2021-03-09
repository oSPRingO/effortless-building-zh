package nl.requios.effortlessbuildingzh.buildmodifier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import nl.requios.effortlessbuildingzh.BuildConfig;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.helper.FixedStack;
import nl.requios.effortlessbuildingzh.helper.InventoryHelper;
import nl.requios.effortlessbuildingzh.helper.SurvivalHelper;
import nl.requios.effortlessbuildingzh.render.BlockPreviewRenderer;

import java.util.*;

public class UndoRedo {

    //Undo and redo stacks per player
    //Gets added to twice in singleplayer (server and client) if not careful. So separate stacks.
    private static Map<UUID, FixedStack<BlockSet>> undoStacksClient = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> undoStacksServer = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> redoStacksClient = new HashMap<>();
    private static Map<UUID, FixedStack<BlockSet>> redoStacksServer = new HashMap<>();

    //add to undo stack
    public static void addUndo(PlayerEntity player, BlockSet blockSet) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;

        //Assert coordinates is as long as previous and new blockstate lists
        if (blockSet.getCoordinates().size() != blockSet.getPreviousBlockStates().size() ||
                blockSet.getCoordinates().size() != blockSet.getNewBlockStates().size()) {
            EffortlessBuildingZh.logger.error("Coordinates and blockstate lists are not equal length. Coordinates: {}. Previous blockstates: {}. New blockstates: {}.",
                    blockSet.getCoordinates().size(), blockSet.getPreviousBlockStates().size(), blockSet.getNewBlockStates().size());
        }

        //Warn if previous and new blockstate are equal
        //Can happen in a lot of valid cases
//        for (int i = 0; i < blockSet.getCoordinates().size(); i++) {
//            if (blockSet.getPreviousBlockStates().get(i).equals(blockSet.getNewBlockStates().get(i))) {
//                EffortlessBuilding.logger.warn("Previous and new blockstates are equal at index {}. Blockstate: {}.",
//                        i, blockSet.getPreviousBlockStates().get(i));
//            }
//        }

        //If no stack exists, make one
        if (!undoStacks.containsKey(player.getUniqueID())) {
            undoStacks.put(player.getUniqueID(), new FixedStack<>(new BlockSet[BuildConfig.survivalBalancers.undoStackSize.get()]));
        }

        undoStacks.get(player.getUniqueID()).push(blockSet);
    }

    private static void addRedo(PlayerEntity player, BlockSet blockSet) {
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;

        //(No asserts necessary, it's private)

        //If no stack exists, make one
        if (!redoStacks.containsKey(player.getUniqueID())) {
            redoStacks.put(player.getUniqueID(), new FixedStack<>(new BlockSet[BuildConfig.survivalBalancers.undoStackSize.get()]));
        }

        redoStacks.get(player.getUniqueID()).push(blockSet);
    }

    public static boolean undo(PlayerEntity player) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;

        if (!undoStacks.containsKey(player.getUniqueID())) return false;

        FixedStack<BlockSet> undoStack = undoStacks.get(player.getUniqueID());

        if (undoStack.isEmpty()) return false;

        BlockSet blockSet = undoStack.pop();
        List<BlockPos> coordinates = blockSet.getCoordinates();
        List<BlockState> previousBlockStates = blockSet.getPreviousBlockStates();
        List<BlockState> newBlockStates = blockSet.getNewBlockStates();
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

                if (previousBlockStates.get(i).equals(newBlockStates.get(i))) continue;

                //get blockstate from itemstack
                BlockState previousBlockState = Blocks.AIR.getDefaultState();
                if (itemStack.getItem() instanceof BlockItem) {
                    previousBlockState = ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
                }

                if (player.world.isBlockPresent(coordinate)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        itemStack = findItemStackInInventory(player, previousBlockStates.get(i));
                        //get blockstate from new itemstack
                        if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem) {
                            previousBlockState = ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
                        } else {
                            if (previousBlockStates.get(i).getBlock() != Blocks.AIR)
                                EffortlessBuildingZh.logTranslate(player, "", previousBlockStates.get(i).getBlock().getTranslationKey(), "在背包中不存在", true);
                            previousBlockState = Blocks.AIR.getDefaultState();
                        }
                    }
                    if (itemStack.isEmpty()) SurvivalHelper.breakBlock(player.world, player, coordinate, true);
                    //if previousBlockState is air, placeBlock will set it to air
                    SurvivalHelper.placeBlock(player.world, player, coordinate, previousBlockState, itemStack, Direction.UP, hitVec, true, false, false);
                }
            }
        }

        //add to redo
        addRedo(player, blockSet);

        return true;
    }

    public static boolean redo(PlayerEntity player) {
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;

        if (!redoStacks.containsKey(player.getUniqueID())) return false;

        FixedStack<BlockSet> redoStack = redoStacks.get(player.getUniqueID());

        if (redoStack.isEmpty()) return false;

        BlockSet blockSet = redoStack.pop();
        List<BlockPos> coordinates = blockSet.getCoordinates();
        List<BlockState> previousBlockStates = blockSet.getPreviousBlockStates();
        List<BlockState> newBlockStates = blockSet.getNewBlockStates();
        Vec3d hitVec = blockSet.getHitVec();

        //Find up to date itemstacks in player inventory
        List<ItemStack> itemStacks = findItemStacksInInventory(player, newBlockStates);

        if (player.world.isRemote) {
            BlockPreviewRenderer.onBlocksPlaced(coordinates, itemStacks, newBlockStates, blockSet.getFirstPos(), blockSet.getSecondPos());
        } else {
            //place blocks
            for (int i = 0; i < coordinates.size(); i++) {
                BlockPos coordinate = coordinates.get(i);
                ItemStack itemStack = itemStacks.get(i);

                if (previousBlockStates.get(i).equals(newBlockStates.get(i))) continue;

                //get blockstate from itemstack
                BlockState newBlockState = Blocks.AIR.getDefaultState();
                if (itemStack.getItem() instanceof BlockItem) {
                    newBlockState = ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
                }

                if (player.world.isBlockPresent(coordinate)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) {
                        itemStack = findItemStackInInventory(player, newBlockStates.get(i));
                        //get blockstate from new itemstack
                        if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem) {
                            newBlockState = ((BlockItem) itemStack.getItem()).getBlock().getDefaultState();
                        } else {
                            if (newBlockStates.get(i).getBlock() != Blocks.AIR)
                                EffortlessBuildingZh.logTranslate(player, "", newBlockStates.get(i).getBlock().getTranslationKey(), "在背包中不存在", true);
                            newBlockState = Blocks.AIR.getDefaultState();
                        }
                    }
                    if (itemStack.isEmpty()) SurvivalHelper.breakBlock(player.world, player, coordinate, true);
                    SurvivalHelper.placeBlock(player.world, player, coordinate, newBlockState, itemStack, Direction.UP, hitVec, true, false, false);
                }
            }
        }

        //add to undo
        addUndo(player, blockSet);

        return true;
    }

    public static void clear(PlayerEntity player) {
        Map<UUID, FixedStack<BlockSet>> undoStacks = player.world.isRemote ? undoStacksClient : undoStacksServer;
        Map<UUID, FixedStack<BlockSet>> redoStacks = player.world.isRemote ? redoStacksClient : redoStacksServer;
        if (undoStacks.containsKey(player.getUniqueID())) {
            undoStacks.get(player.getUniqueID()).clear();
        }
        if (redoStacks.containsKey(player.getUniqueID())) {
            redoStacks.get(player.getUniqueID()).clear();
        }
    }

    private static List<ItemStack> findItemStacksInInventory(PlayerEntity player, List<BlockState> blockStates) {
        List<ItemStack> itemStacks = new ArrayList<>(blockStates.size());
        for (BlockState blockState : blockStates) {
            itemStacks.add(findItemStackInInventory(player, blockState));
        }
        return itemStacks;
    }

    private static ItemStack findItemStackInInventory(PlayerEntity player, BlockState blockState) {
        ItemStack itemStack = ItemStack.EMPTY;
        if (blockState == null) return itemStack;

        //First try previousBlockStates
        //TODO try to find itemstack with right blockstate first
        // then change line 103 back (get state from item)
        itemStack = InventoryHelper.findItemStackInInventory(player, blockState.getBlock());


        //then anything it drops
        if (itemStack.isEmpty()) {
            //Cannot check drops on clientside because loot tables are server only
            if (!player.world.isRemote)
            {
                List<ItemStack> itemsDropped = Block.getDrops(blockState, (ServerWorld) player.world, BlockPos.ZERO, null);
                for (ItemStack itemStackDropped : itemsDropped) {
                    if (itemStackDropped.getItem() instanceof BlockItem) {
                        Block block = ((BlockItem) itemStackDropped.getItem()).getBlock();
                        itemStack = InventoryHelper.findItemStackInInventory(player, block);
                    }
                }
            }
        }

        //then air
        //(already empty)

        return itemStack;
    }
}
