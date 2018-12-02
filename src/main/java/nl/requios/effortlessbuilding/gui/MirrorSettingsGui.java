package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.Mirror;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MirrorSettingsGui extends GuiCollapsibleScrollEntry {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/building_icons.png");

    protected List<GuiButton> mirrorButtonList = new ArrayList<>();
    protected List<GuiIconButton> mirrorIconButtonList = new ArrayList<>();
    protected List<GuiNumberField> mirrorNumberFieldList = new ArrayList<>();

    private GuiNumberField textMirrorPosX, textMirrorPosY, textMirrorPosZ, textMirrorRadius;
    private GuiCheckBox buttonMirrorEnabled, buttonMirrorX, buttonMirrorY, buttonMirrorZ;
    private GuiIconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;

    public MirrorSettingsGui(GuiScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public int initGui(int id, List<GuiButton> buttonList) {
        id = super.initGui(id, buttonList);

        int y = top - 2;
        buttonMirrorEnabled = new GuiCheckBox(id++, left - 15 + 8, y, "", false);
        buttonList.add(buttonMirrorEnabled);

        y = top + 18;
        textMirrorPosX = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 58, y, 62, 18);
        textMirrorPosX.setNumber(0);
        textMirrorPosX.setTooltip(
                Arrays.asList("The position of the mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        mirrorNumberFieldList.add(textMirrorPosX);

        textMirrorPosY = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 138, y, 62, 18);
        textMirrorPosY.setNumber(64);
        textMirrorPosY.setTooltip(Arrays.asList("The position of the mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        mirrorNumberFieldList.add(textMirrorPosY);

        textMirrorPosZ = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 218, y, 62, 18);
        textMirrorPosZ.setNumber(0);
        textMirrorPosZ.setTooltip(Arrays.asList("The position of the mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        mirrorNumberFieldList.add(textMirrorPosZ);

        y = top + 50;
        buttonMirrorX = new GuiCheckBox(id++, left + 60, y, " X", true);
        mirrorButtonList.add(buttonMirrorX);

        buttonMirrorY = new GuiCheckBox(id++, left + 100, y, " Y", false);
        mirrorButtonList.add(buttonMirrorY);

        buttonMirrorZ = new GuiCheckBox(id++, left + 140, y, " Z", false);
        mirrorButtonList.add(buttonMirrorZ);

        y = top + 47;
        textMirrorRadius = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 218, y, 62, 18);
        textMirrorRadius.setNumber(50);
        //TODO change to diameter (remove /2)
        textMirrorRadius.setTooltip(Arrays.asList("How far the mirror reaches in any direction.",
                TextFormatting.GRAY + "Max: " + TextFormatting.GOLD + BuildSettingsManager.getMaxReach(mc.player) / 2,
                TextFormatting.GRAY + "Upgradeable in survival with reach upgrades."));
        mirrorNumberFieldList.add(textMirrorRadius);

        y = top + 72;
        buttonCurrentPosition = new GuiIconButton(id++, left + 5, y, 0, 0, BUILDING_ICONS);
        buttonCurrentPosition.setTooltip("Set mirror position to current player position");
        mirrorIconButtonList.add(buttonCurrentPosition);

        buttonToggleOdd = new GuiIconButton(id++, left + 35, y, 0, 20, BUILDING_ICONS);
        buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to middle of block", "for odd numbered builds"));
        mirrorIconButtonList.add(buttonToggleOdd);

        buttonDrawLines = new GuiIconButton(id++, left + 65, y, 0, 40, BUILDING_ICONS);
        buttonDrawLines.setTooltip("Show lines");
        mirrorIconButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new GuiIconButton(id++, left + 95, y, 0, 60, BUILDING_ICONS);
        buttonDrawPlanes.setTooltip("Show area");
        mirrorIconButtonList.add(buttonDrawPlanes);

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(mc.player);
        if (buildSettings != null) {
            Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
            buttonMirrorEnabled.setIsChecked(m.enabled);
            textMirrorPosX.setNumber(m.position.x);
            textMirrorPosY.setNumber(m.position.y);
            textMirrorPosZ.setNumber(m.position.z);
            buttonMirrorX.setIsChecked(m.mirrorX);
            buttonMirrorY.setIsChecked(m.mirrorY);
            buttonMirrorZ.setIsChecked(m.mirrorZ);
            textMirrorRadius.setNumber(m.radius);
            drawLines = m.drawLines;
            drawPlanes = m.drawPlanes;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawLines.setTooltip(drawLines ? "Hide lines" : "Show lines");
            buttonDrawPlanes.setTooltip(drawPlanes ? "Hide area" : "Show area");
            if (textMirrorPosX.getNumber() == Math.floor(textMirrorPosX.getNumber())) {
                toggleOdd = false;
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to middle of block", "for odd numbered builds"));
            } else {
                toggleOdd = true;
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to corner of block", "for even numbered builds"));
            }
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
        }

        buttonList.addAll(mirrorButtonList);
        buttonList.addAll(mirrorIconButtonList);

        return id;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        mirrorNumberFieldList.forEach(GuiNumberField::update);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);

        int yy = y;
        int offset = 8;

        buttonMirrorEnabled.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (buttonMirrorEnabled.isChecked()) {
            buttonMirrorEnabled.y = yy;
            fontRenderer.drawString("Mirror enabled", left + offset, yy + 2, 0xFFFFFF, true);

            yy = y + 18;
            fontRenderer.drawString("Position", left + offset, yy + 5, 0xFFFFFF, true);
            fontRenderer.drawString("X", left + 40 + offset, yy + 5, 0xFFFFFF, true);
            textMirrorPosX.y = yy;
            fontRenderer.drawString("Y", left + 120 + offset, yy + 5, 0xFFFFFF, true);
            textMirrorPosY.y = yy;
            fontRenderer.drawString("Z", left + 200 + offset, yy + 5, 0xFFFFFF, true);
            textMirrorPosZ.y = yy;

            yy = y + 50;
            fontRenderer.drawString("Direction", left + offset, yy + 2, 0xFFFFFF, true);
            buttonMirrorX.y = yy;
            buttonMirrorY.y = yy;
            buttonMirrorZ.y = yy;
            fontRenderer.drawString("Reach", left + 176 + offset, yy + 2, 0xFFFFFF, true);
            textMirrorRadius.y = yy - 3;

            yy = y + 72;
            buttonCurrentPosition.y = yy;
            buttonToggleOdd.y = yy;
            buttonDrawLines.y = yy;
            buttonDrawPlanes.y = yy;

            mirrorButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            mirrorIconButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawNumberField(this.mc, mouseX, mouseY, partialTicks));
        } else {
            buttonMirrorEnabled.y = yy;
            fontRenderer.drawString("Mirror disabled", left + offset, yy + 2, 0x999999, true);
        }

    }

    public void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonMirrorEnabled.isChecked())
        {
            mirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(scrollPane.parent, mouseX, mouseY));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        super.updatePosition(slotIndex, x, y, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (GuiNumberField numberField : mirrorNumberFieldList) {
            numberField.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
        mirrorNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideMirrorEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideMirrorEnabledLabel) {
            buttonMirrorEnabled.setIsChecked(!buttonMirrorEnabled.isChecked());
            buttonMirrorEnabled.playPressSound(this.mc.getSoundHandler());
            actionPerformed(buttonMirrorEnabled);
        }

        return true;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button == buttonCurrentPosition) {
            Vec3d pos = new Vec3d(Math.floor(mc.player.posX) + 0.5, Math.floor(mc.player.posY) + 0.5, Math.floor(mc.player.posZ) + 0.5);
            textMirrorPosX.setNumber(pos.x);
            textMirrorPosY.setNumber(pos.y);
            textMirrorPosZ.setNumber(pos.z);
        }
        if (button == buttonToggleOdd) {
            toggleOdd = !toggleOdd;
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
            if (toggleOdd) {
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to corner of block", "for even numbered builds"));
                textMirrorPosX.setNumber(textMirrorPosX.getNumber() + 0.5);
                textMirrorPosY.setNumber(textMirrorPosY.getNumber() + 0.5);
                textMirrorPosZ.setNumber(textMirrorPosZ.getNumber() + 0.5);
            } else {
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to middle of block", "for odd numbered builds"));
                textMirrorPosX.setNumber(Math.floor(textMirrorPosX.getNumber()));
                textMirrorPosY.setNumber(Math.floor(textMirrorPosY.getNumber()));
                textMirrorPosZ.setNumber(Math.floor(textMirrorPosZ.getNumber()));
            }
        }
        if (button == buttonDrawLines) {
            drawLines = !drawLines;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawLines.setTooltip(drawLines ? "Hide lines" : "Show lines");
        }
        if (button == buttonDrawPlanes) {
            drawPlanes = !drawPlanes;
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawPlanes.setTooltip(drawPlanes ? "Hide area" : "Show area");
        }
        mirrorNumberFieldList.forEach(numberField -> numberField.actionPerformed(button));
    }

    public Mirror.MirrorSettings getMirrorSettings() {
        boolean mirrorEnabled = buttonMirrorEnabled.isChecked();

        Vec3d mirrorPos = new Vec3d(0, 64, 0);
        try {
            mirrorPos = new Vec3d(textMirrorPosX.getNumber(), textMirrorPosY.getNumber(), textMirrorPosZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(mc.player, "Mirror position not valid.");
        }

        boolean mirrorX = buttonMirrorX.isChecked();
        boolean mirrorY = buttonMirrorY.isChecked();
        boolean mirrorZ = buttonMirrorZ.isChecked();

        int mirrorRadius = 50;
        try {
            mirrorRadius = (int) textMirrorRadius.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(mc.player, "Mirror radius not valid.");
        }

        return new Mirror.MirrorSettings(mirrorEnabled, mirrorPos, mirrorX, mirrorY, mirrorZ, mirrorRadius, drawLines, drawPlanes);
    }

    @Override
    protected String getName() {
        return "Mirror";
    }
}
