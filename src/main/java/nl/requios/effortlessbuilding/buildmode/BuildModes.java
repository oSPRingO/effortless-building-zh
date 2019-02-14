package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.*;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.network.BlockBrokenMessage;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class BuildModes {

    //Static variables are shared between client and server in singleplayer
    //We need them separate
    public static Dictionary<EntityPlayer, Boolean> currentlyBreakingClient = new Hashtable<>();
    public static Dictionary<EntityPlayer, Boolean> currentlyBreakingServer = new Hashtable<>();

    public enum BuildModeEnum {
        Normal ("Normal", new Normal()),
        NormalPlus ("Normal+", new NormalPlus()),
        Line ("Line", new Line()),
        Wall ("Wall", new Wall()),
        Floor ("Floor", new Floor()),
        DiagonalLine ("Diagonal Line", new DiagonalLine()),
        DiagonalWall ("Diagonal Wall", new DiagonalWall()),
        SlopeFloor ("Slope Floor", new SlopeFloor()),
        Cube ("Cube", new Cube());

        public String name;
        public IBuildMode instance;

        BuildModeEnum(String name, IBuildMode instance) {
            this.name = name;
            this.instance = instance;
        }
    }

    //Uses a network message to get the previous raytraceresult from the player
    //The server could keep track of all raytraceresults but this might lag with many players
    //Raytraceresult is needed for sideHit and hitVec
    public static void onBlockPlacedMessage(EntityPlayer player, BlockPlacedMessage message) {

        //Check if not in the middle of breaking
        Dictionary<EntityPlayer, Boolean> currentlyBreaking = player.world.isRemote ? currentlyBreakingClient : currentlyBreakingServer;
        if (currentlyBreaking.get(player) != null && currentlyBreaking.get(player)) {
            //Cancel breaking
            initializeMode(player);
            return;
        }

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        BuildModeEnum buildMode = modeSettings.getBuildMode();
        int maxReach = ReachHelper.getMaxReach(player);

        BlockPos startPos = null;

        if (message.isBlockHit() && message.getBlockPos() != null) {
            startPos = message.getBlockPos();

            //Offset in direction of sidehit if not quickreplace and not replaceable
            boolean replaceable = player.world.getBlockState(startPos).getBlock().isReplaceable(player.world, startPos);
            if (!modifierSettings.doQuickReplace() && !replaceable) {
                startPos = startPos.offset(message.getSideHit());
            }

            //Get under tall grass and other replaceable blocks
            if (modifierSettings.doQuickReplace() && replaceable) {
                startPos = startPos.down();
            }

            //Check if player reach does not exceed startpos
            if (player.getPosition().distanceSq(startPos) > maxReach * maxReach) {
                EffortlessBuilding.log(player, "Placement exceeds your reach.");
                return;
            }
        }

        //Even when no starting block is found, call buildmode instance
        //We might want to place things in the air
        List<BlockPos> coordinates = buildMode.instance.onRightClick(player, startPos, message.getSideHit(), message.getHitVec(), modifierSettings.doQuickReplace());

        if (coordinates.isEmpty()) {
            currentlyBreaking.put(player, false);
            return;
        }

        //Limit number of blocks you can place
        int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);
        if (coordinates.size() > limit) {
            coordinates = coordinates.subList(0, limit);
        }

        EnumFacing sideHit = buildMode.instance.getSideHit(player);
        if (sideHit == null) sideHit = message.getSideHit();

        Vec3d hitVec = buildMode.instance.getHitVec(player);
        if (hitVec == null) hitVec = message.getHitVec();

        BuildModifiers.onBlockPlaced(player, coordinates, sideHit, hitVec);

        //Only works when finishing a buildmode is equal to placing some blocks
        //No intermediate blocks allowed
        currentlyBreaking.remove(player);

    }

    //Use a network message to break blocks in the distance using clientside mouse input
    public static void onBlockBrokenMessage(EntityPlayer player, BlockBrokenMessage message) {
        BlockPos blockPos = message.getBlockPos();

        if (ReachHelper.canBreakFar(player) && message.isBlockHit() &&
            !CompatHelper.chiselsAndBitsProxy.isHoldingChiselTool(EnumHand.MAIN_HAND)) {

            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(player.world, blockPos, player.world.getBlockState(blockPos), player);
            onBlockBroken(event);
        }
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        BlockPos pos = event.getPos();

        //Check if not in the middle of placing
        Dictionary<EntityPlayer, Boolean> currentlyBreaking = player.world.isRemote ? currentlyBreakingClient : currentlyBreakingServer;
        if (currentlyBreaking.get(player) != null && !currentlyBreaking.get(player)) {
            //Cancel placing
            initializeMode(player);
            return;
        }

        //get coordinates
        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        BuildModeEnum buildMode = modeSettings.getBuildMode();
        List<BlockPos> coordinates = buildMode.instance.onRightClick(player, pos, EnumFacing.UP, Vec3d.ZERO, true);

        if (coordinates.isEmpty()) {
            currentlyBreaking.put(player, true);
            return;
        }

        //let buildmodifiers break blocks
        BuildModifiers.onBlockBroken(player, coordinates);
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos, boolean skipRaytrace) {
        List<BlockPos> coordinates = new ArrayList<>();

        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        coordinates.addAll(modeSettings.getBuildMode().instance.findCoordinates(player, startPos, skipRaytrace));

        return coordinates;
    }

    public static void initializeMode(EntityPlayer player) {
        //Resetting mode, so not placing or breaking
        Dictionary<EntityPlayer, Boolean> currentlyBreaking = player.world.isRemote ? currentlyBreakingClient : currentlyBreakingServer;
        currentlyBreaking.remove(player);

        ModeSettingsManager.getModeSettings(player).getBuildMode().instance.initialize(player);
    }
}
