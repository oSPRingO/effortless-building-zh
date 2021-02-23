package nl.requios.effortlessbuildingzh.gui.buildmodifier;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;
import nl.requios.effortlessbuildingzh.buildmodifier.Array;
import nl.requios.effortlessbuildingzh.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuildingzh.gui.elements.GuiCheckBoxFixed;
import nl.requios.effortlessbuildingzh.gui.elements.GuiCollapsibleScrollEntry;
import nl.requios.effortlessbuildingzh.gui.elements.GuiNumberField;
import nl.requios.effortlessbuildingzh.gui.elements.GuiScrollPane;
import nl.requios.effortlessbuildingzh.helper.ReachHelper;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ArraySettingsGui extends GuiCollapsibleScrollEntry {

    protected List<GuiNumberField> arrayNumberFieldList = new ArrayList<>();

    private GuiCheckBoxFixed buttonArrayEnabled;
    private GuiNumberField textArrayOffsetX, textArrayOffsetY, textArrayOffsetZ, textArrayCount;

    public ArraySettingsGui(GuiScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Widget> buttons) {
        super.init(buttons);

        int y = top;
        buttonArrayEnabled = new GuiCheckBoxFixed(left - 15 + 8, y, "", false) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                setCollapsed(!buttonArrayEnabled.isChecked());
            }
        };
        buttons.add(buttonArrayEnabled);

        y = top + 20;
        textArrayOffsetX = new GuiNumberField(font, buttons, left + 70, y, 50, 18);
        textArrayOffsetX.setNumber(0);
        textArrayOffsetX.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetX);

        textArrayOffsetY = new GuiNumberField(font, buttons, left + 140, y, 50, 18);
        textArrayOffsetY.setNumber(0);
        textArrayOffsetY.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetY);

        textArrayOffsetZ = new GuiNumberField(font, buttons, left + 210, y, 50, 18);
        textArrayOffsetZ.setNumber(0);
        textArrayOffsetZ.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetZ);

        y = top + 50;
        textArrayCount = new GuiNumberField(font, buttons, left + 55, y, 50, 18);
        textArrayCount.setNumber(5);
        textArrayCount.setTooltip("How many copies should be made.");
        arrayNumberFieldList.add(textArrayCount);

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
        if (modifierSettings != null) {
            Array.ArraySettings a = modifierSettings.getArraySettings();
            buttonArrayEnabled.setIsChecked(a.enabled);
            textArrayOffsetX.setNumber(a.offset.getX());
            textArrayOffsetY.setNumber(a.offset.getY());
            textArrayOffsetZ.setNumber(a.offset.getZ());
            textArrayCount.setNumber(a.count);
        }

        setCollapsed(!buttonArrayEnabled.isChecked());
    }

    public void updateScreen() {
        arrayNumberFieldList.forEach(GuiNumberField::update);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {
        int yy = y;
        int offset = 8;

        buttonArrayEnabled.render(mouseX, mouseY, partialTicks);
        if (buttonArrayEnabled.isChecked()) {
            buttonArrayEnabled.y = yy;
            font.drawString("Array enabled", left + offset, yy + 2, 0xFFFFFF);

            yy = y + 20;
            font.drawString("Offset", left + offset, yy + 5, 0xFFFFFF);
            font.drawString("X", left + 50 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetX.y = yy;
            font.drawString("Y", left + 120 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetY.y = yy;
            font.drawString("Z", left + 190 + offset, yy + 5, 0xFFFFFF);
            textArrayOffsetZ.y = yy;

            yy = y + 50;
            font.drawString("Count", left + offset, yy + 5, 0xFFFFFF);
            textArrayCount.y = yy;

            int currentReach = Math.max(-1, getArrayReach());
            int maxReach = ReachHelper.getMaxReach(mc.player);
            TextFormatting reachColor = isCurrentReachValid(currentReach, maxReach) ? TextFormatting.GRAY : TextFormatting.RED;
            String reachText = "Reach: " + reachColor + currentReach + TextFormatting.GRAY + "/" + TextFormatting.GRAY + maxReach;
            font.drawString(reachText, left + 176 + offset, yy + 5, 0xFFFFFF);

            arrayNumberFieldList.forEach(numberField -> numberField.drawNumberField(mouseX, mouseY, partialTicks));
        } else {
            buttonArrayEnabled.y = yy;
            font.drawString("Array disabled", left + offset, yy + 2, 0x999999);
        }

    }

    public void drawTooltip(Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonArrayEnabled.isChecked())
        {
            arrayNumberFieldList.forEach(numberField -> numberField.drawTooltip(scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        for (GuiNumberField numberField : arrayNumberFieldList) {
            numberField.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        arrayNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideArrayEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideArrayEnabledLabel) {
            buttonArrayEnabled.playDownSound(this.mc.getSoundHandler());
            buttonArrayEnabled.onClick(mouseX, mouseY);
        }

        return true;
    }

    public Array.ArraySettings getArraySettings() {
        boolean arrayEnabled = buttonArrayEnabled.isChecked();
        BlockPos arrayOffset = new BlockPos(0, 0, 0);
        try {
            arrayOffset = new BlockPos(textArrayOffsetX.getNumber(), textArrayOffsetY.getNumber(), textArrayOffsetZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuildingZh.log(mc.player, "Array offset not a valid number.");
        }

        int arrayCount = 5;
        try {
            arrayCount = (int) textArrayCount.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuildingZh.log(mc.player, "Array count not a valid number.");
        }

        return new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);
    }

    @Override
    protected String getName() {
        return "Array";
    }

    @Override
    protected int getExpandedHeight() {
        return 80;
    }

    private int getArrayReach() {
        try
        {
            //find largest offset
            double x = Math.abs(textArrayOffsetX.getNumber());
            double y = Math.abs(textArrayOffsetY.getNumber());
            double z = Math.abs(textArrayOffsetZ.getNumber());
            double largestOffset = Math.max(Math.max(x, y), z);
            return (int) (largestOffset * textArrayCount.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            return -1;
        }
    }

    private boolean isCurrentReachValid(int currentReach, int maxReach) {
        return currentReach <= maxReach && currentReach > -1;
    }
}
