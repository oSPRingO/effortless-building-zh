package nl.requios.effortlessbuilding;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.network.QuickReplaceMessage;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

public class QuickReplace {
    //Dilemma in getting blockstate from event to when message is received:
    // 1) send via client. Then hacking makes it possible to place any block.
    // 2) save serverside. Messages may not be received chronologically so data could get switched.
    //Solution for now: save data serverside and per player. Messages from 1 player will rarely come unchronologically
    //and players will rarely switch between blocks that quickly.

    private static Dictionary<UUID, IBlockState> blockStates = new Hashtable<>();
    private static Dictionary<UUID, ItemStack> itemStacks = new Hashtable<>();

    public static boolean onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return true;
        //Only serverside

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(event.getPlayer());
        if (!buildSettings.doQuickReplace()) return false;

        //TODO base on player facing instead, no more messages (or break block clientside)

        blockStates.put(event.getPlayer().getUniqueID(), event.getPlacedBlock());
        itemStacks.put(event.getPlayer().getUniqueID(), event.getPlayer().getHeldItem(event.getHand()));

        //RayTraceResult result = event.getWorld().rayTraceBlocks(event.getPlayer().getPositionEyes(1f), event.getPlayer().getLookVec());
        EffortlessBuilding.packetHandler.sendTo(new QuickReplaceMessage(), (EntityPlayerMP) event.getPlayer());

        return true;
    }

    public static void onMessageReceived(EntityPlayer player, QuickReplaceMessage message) {
        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(player);
        if (!buildSettings.doQuickReplace()) return;

        //TODO check for bedrock, end portal etc in survival

        if (!message.isBlockHit() || message.getBlockPos() == null) return;

        BlockPos placedAgainstBlockPos = message.getBlockPos();

        //Get under tall grass and other replaceable blocks
        if (player.world.getBlockState(placedAgainstBlockPos).getBlock().isReplaceable(player.world, placedAgainstBlockPos)) {
            placedAgainstBlockPos = placedAgainstBlockPos.down();
        }

        IBlockState blockState = blockStates.get(player.getUniqueID());
        ItemStack itemStack = itemStacks.get(player.getUniqueID());

        SurvivalHelper.placeBlock(player.world, player, placedAgainstBlockPos, blockState, itemStack, message.getSideHit(), true, false);

        //Mirror and Array synergy
        BlockSnapshot blockSnapshot = new BlockSnapshot(player.world, placedAgainstBlockPos, blockState);
        BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(blockSnapshot, blockState, player, EnumHand.MAIN_HAND);
        Mirror.onBlockPlaced(placeEvent);
        Array.onBlockPlaced(placeEvent);
    }
}
