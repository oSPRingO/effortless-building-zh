package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.helper.FixedStack;
import nl.requios.effortlessbuilding.helper.InventoryHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.render.BlockPreviewRenderer;

import java.util.*;

public class UndoRedo {

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
        List<IBlockState> blockStates = blockSet.getBlockStates();

        //Find up to date itemstacks in player inventory
        List<ItemStack> itemStacks = findItemStacksInInventory(player, blockStates);

        if (player.world.isRemote) {
            BlockPreviewRenderer.onBlocksBroken(coordinates, itemStacks, blockStates, blockSet.getSecondPos(), blockSet.getFirstPos());
        } else {
            //break all those blocks
            for (int i = 0; i < coordinates.size(); i++) {
                BlockPos coordinate = coordinates.get(i);
                if (player.world.isBlockLoaded(coordinate, false)) {
                    SurvivalHelper.breakBlock(player.world, player, coordinate);
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
        List<IBlockState> blockStates = blockSet.getBlockStates();
        Vec3d hitVec = blockSet.getHitVec();

        //Find up to date itemstacks in player inventory
        List<ItemStack> itemStacks = findItemStacksInInventory(player, blockStates);

        if (player.world.isRemote) {
            BlockPreviewRenderer.onBlocksPlaced(coordinates, itemStacks, blockStates, blockSet.getFirstPos(), blockSet.getSecondPos());
        } else {
            //place blocks
            for (int i = 0; i < coordinates.size(); i++) {
                BlockPos blockPos = coordinates.get(i);
                IBlockState blockState = blockStates.get(i);
                ItemStack itemStack = itemStacks.get(i);

                if (player.world.isBlockLoaded(blockPos, true)) {
                    //check itemstack empty
                    if (itemStack.isEmpty()) continue;
                    SurvivalHelper.placeBlock(player.world, player, blockPos, blockState, itemStack, EnumFacing.UP, hitVec, false, false);
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
            itemStacks.add(InventoryHelper.findItemStackInInventory(player, blockState));
        }
        return itemStacks;
    }
}
