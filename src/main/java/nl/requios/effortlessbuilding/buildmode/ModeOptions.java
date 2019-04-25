package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.UndoRedo;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.gui.buildmodifier.ModifierSettingsGui;

public class ModeOptions {

    public enum ActionEnum {
        UNDO("effortlessbuilding.action.undo"),
        REDO("effortlessbuilding.action.redo"),
        REPLACE("effortlessbuilding.action.replace"),
        OPEN_MODIFIER_SETTINGS("effortlessbuilding.action.open_modifier_settings"),

        NORMAL_SPEED("effortlessbuilding.action.normal_speed"),
        FAST_SPEED("effortlessbuilding.action.fast_speed"),

        FULL("effortlessbuilding.action.full"),
        HOLLOW("effortlessbuilding.action.hollow"),

        CUBE_FULL("effortlessbuilding.action.full"),
        CUBE_HOLLOW("effortlessbuilding.action.hollow"),
        CUBE_SKELETON("effortlessbuilding.action.skeleton"),

        SHORT_EDGE("effortlessbuilding.action.short_edge"),
        LONG_EDGE("effortlessbuilding.action.long_edge"),

        THICKNESS_1("effortlessbuilding.action.thickness_1"),
        THICKNESS_3("effortlessbuilding.action.thickness_3"),
        THICKNESS_5("effortlessbuilding.action.thickness_5");

        public String name;

        ActionEnum(String name) {
            this.name = name;
        }
    }

    private static ActionEnum buildSpeed = ActionEnum.NORMAL_SPEED;
    private static ActionEnum fill = ActionEnum.FULL;
    private static ActionEnum cubeFill = ActionEnum.CUBE_FULL;
    private static ActionEnum raisedEdge = ActionEnum.SHORT_EDGE;
    private static ActionEnum lineThickness = ActionEnum.THICKNESS_1;

    public static ActionEnum getBuildSpeed() {
        return buildSpeed;
    }

    public static ActionEnum getFill() {
        return fill;
    }

    public static ActionEnum getCubeFill() {
        return cubeFill;
    }

    public static ActionEnum getRaisedEdge() {
        return raisedEdge;
    }

    public static ActionEnum getLineThickness() {
        return lineThickness;
    }

    //Called on both client and server
    public static void performAction(EntityPlayer player, ActionEnum action) {
        if (action == null) return;
        BuildModes.BuildModeEnum currentBuildMode = ModeSettingsManager.getModeSettings(Minecraft.getMinecraft().player).getBuildMode();

        switch (action) {
            case UNDO:
                UndoRedo.undo(player);
                break;
            case REDO:
                UndoRedo.redo(player);
                break;
            case REPLACE:
                ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(player);
                modifierSettings.setQuickReplace(!modifierSettings.doQuickReplace());
                EffortlessBuilding.log(player, "Set " + TextFormatting.GOLD + "Quick Replace " + TextFormatting.RESET + (
                        modifierSettings.doQuickReplace() ? "on" : "off"), true);
                break;
            case OPEN_MODIFIER_SETTINGS:
                if (player.world.isRemote)
                    openModifierSettings();
                break;

            case NORMAL_SPEED:
                buildSpeed = ActionEnum.NORMAL_SPEED;
                break;
            case FAST_SPEED:
                buildSpeed = ActionEnum.FAST_SPEED;
                break;
            case FULL:
                fill = ActionEnum.FULL;
                break;
            case HOLLOW:
                fill = ActionEnum.HOLLOW;
                break;
            case CUBE_FULL:
                cubeFill = ActionEnum.CUBE_FULL;
                break;
            case CUBE_HOLLOW:
                cubeFill = ActionEnum.CUBE_HOLLOW;
                break;
            case CUBE_SKELETON:
                cubeFill = ActionEnum.CUBE_SKELETON;
                break;
            case SHORT_EDGE:
                raisedEdge = ActionEnum.SHORT_EDGE;
                break;
            case LONG_EDGE:
                raisedEdge = ActionEnum.LONG_EDGE;
                break;
            case THICKNESS_1:
                lineThickness = ActionEnum.THICKNESS_1;
                break;
            case THICKNESS_3:
                lineThickness = ActionEnum.THICKNESS_3;
                break;
            case THICKNESS_5:
                lineThickness = ActionEnum.THICKNESS_5;
                break;
        }

        if (player.world.isRemote && action != ActionEnum.REPLACE && action != ActionEnum.OPEN_MODIFIER_SETTINGS) {
            logAction(action);
        }
    }

    //TODO fix client class import giving error on server (nothing happens, it's just ugly)
    @SideOnly(Side.CLIENT)
    private static void openModifierSettings() {
        Minecraft.getMinecraft().displayGuiScreen(new ModifierSettingsGui());
        RadialMenu.instance.setVisibility(0f);
    }

    @SideOnly(Side.CLIENT)
    private static void logAction(ActionEnum action) {
        EffortlessBuilding.log(Minecraft.getMinecraft().player, I18n.format(action.name), true);
    }
}
