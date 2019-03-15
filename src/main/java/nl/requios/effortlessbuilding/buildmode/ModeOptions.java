package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.client.Minecraft;
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
        UNDO,
        REDO,
        REPLACE,
        OPEN_MODIFIER_SETTINGS
    }

    //Called on both client and server
    public static void performAction(EntityPlayer player, ActionEnum action) {

        switch (action) {
            case UNDO:
                UndoRedo.undo(player);
                EffortlessBuilding.log(player, "Undo", true);
                break;
            case REDO:
                UndoRedo.redo(player);
                EffortlessBuilding.log(player, "Redo", true);
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
        }
    }

    @SideOnly(Side.CLIENT)
    private static void openModifierSettings() {
        Minecraft.getMinecraft().displayGuiScreen(new ModifierSettingsGui());
        RadialMenu.instance.setVisibility(0f);
    }

}
