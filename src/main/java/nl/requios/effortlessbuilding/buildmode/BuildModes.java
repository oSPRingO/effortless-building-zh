package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.*;
import nl.requios.effortlessbuilding.helper.ReachHelper;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.network.BlockBrokenMessage;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;

import java.util.ArrayList;
import java.util.List;

public class BuildModes {

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
        public final IBuildMode instance;

        BuildModeEnum(String name, IBuildMode instance) {
            this.name = name;
            this.instance = instance;
        }
    }

    //Uses a network message to get the previous raytraceresult from the player
    //The server could keep track of all raytraceresults but this might lag with many players
    //Raytraceresult is needed for sideHit and hitVec
    public static void onBlockPlacedMessage(EntityPlayer player, BlockPlacedMessage message) {

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
        List<BlockPos> posList = buildMode.instance.onRightClick(player, startPos, message.getSideHit(), message.getHitVec());

        //Limit number of blocks you can place
        int limit = ReachHelper.getMaxBlocksPlacedAtOnce(player);
        if (posList.size() > limit) {
            posList = posList.subList(0, limit);
        }

        EnumFacing sideHit = buildMode.instance.getSideHit(player);
        if (sideHit == null) sideHit = message.getSideHit();

        Vec3d hitVec = buildMode.instance.getHitVec(player);
        if (hitVec == null) hitVec = message.getHitVec();

        BuildModifiers.onBlockPlaced(player, posList, sideHit, hitVec);

    }


    //Use a network message to break blocks in the distance using clientside mouse input
    public static void onBlockBrokenMessage(EntityPlayer player, BlockBrokenMessage message) {
        BlockPos blockPos = message.getBlockPos();
        if (ReachHelper.canBreakFar(player) && message.isBlockHit()) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(player.world, blockPos, player.world.getBlockState(blockPos), player);
            onBlockBroken(event);
        }
    }

    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        EntityPlayer player = event.getPlayer();
        BlockPos pos = event.getPos();

        if (world.isRemote) return;

        //get coordinates
        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        BuildModeEnum buildMode = modeSettings.getBuildMode();
        List<BlockPos> coordinates = buildMode.instance.onRightClick(player, pos, EnumFacing.UP, Vec3d.ZERO);

        //let buildmodifiers break blocks
        BuildModifiers.onBlockBroken(player, coordinates);
    }

    public static List<BlockPos> findCoordinates(EntityPlayer player, BlockPos startPos) {
        List<BlockPos> coordinates = new ArrayList<>();

        ModeSettingsManager.ModeSettings modeSettings = ModeSettingsManager.getModeSettings(player);
        coordinates.addAll(modeSettings.getBuildMode().instance.findCoordinates(player, startPos));

        return coordinates;
    }

    public static void initializeMode(EntityPlayer player) {
        ModeSettingsManager.getModeSettings(player).getBuildMode().instance.initialize(player);
    }
}
