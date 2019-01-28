package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.entity.player.EntityPlayer;
import nl.requios.effortlessbuilding.EffortlessBuilding;

public class BuildModes {

    public enum BuildModeEnum {
        Normal ("Normal", new Normal()),
        NormalPlus ("Normal+", new NormalPlus()),
        Line ("Line", new Line()),
        Wall ("Wall", new Wall()),
        Floor ("Floor", new Floor())
        ;
//        DiagonalLine,
//        DiagonalWall,
//        SlopedFloor,
//        Cube;

        public String name;
        public final BuildMode instance;

        BuildModeEnum(String name, BuildMode instance) {
            this.name = name;
            this.instance = instance;
        }
    }

    protected static BuildModeEnum buildMode = BuildModeEnum.Normal;

    public static BuildModeEnum getBuildMode() {
        return buildMode;
    }

    public static void setBuildMode(EntityPlayer player, BuildModeEnum buildMode) {
        if (player.world.isRemote) {
            //TODO send to server
            BuildModes.buildMode = buildMode;
            EffortlessBuilding.log(player, BuildModes.buildMode.name, true);
        } else {
            //TODO cancel previous mode's action
            BuildModes.buildMode = buildMode;
        }
    }
}
