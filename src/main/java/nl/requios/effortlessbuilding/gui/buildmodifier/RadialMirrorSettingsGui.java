package nl.requios.effortlessbuilding.gui.buildmodifier;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;
import nl.requios.effortlessbuilding.gui.elements.GuiCollapsibleScrollEntry;
import nl.requios.effortlessbuilding.gui.elements.GuiIconButton;
import nl.requios.effortlessbuilding.gui.elements.GuiNumberField;
import nl.requios.effortlessbuilding.gui.elements.GuiScrollPane;
import nl.requios.effortlessbuilding.helper.ReachHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadialMirrorSettingsGui extends GuiCollapsibleScrollEntry {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/building_icons.png");

    protected List<GuiButton> radialMirrorButtonList = new ArrayList<>();
    protected List<GuiIconButton> radialMirrorIconButtonList = new ArrayList<>();
    protected List<GuiNumberField> radialMirrorNumberFieldList = new ArrayList<>();

    private GuiNumberField textRadialMirrorPosX, textRadialMirrorPosY, textRadialMirrorPosZ, textRadialMirrorSlices, textRadialMirrorRadius;
    private GuiCheckBox buttonRadialMirrorEnabled, buttonRadialMirrorAlternate;
    private GuiIconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;

    public RadialMirrorSettingsGui(GuiScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public int initGui(int id, List<GuiButton> buttonList) {
        id = super.initGui(id, buttonList);

        int y = top - 2;
        buttonRadialMirrorEnabled = new GuiCheckBox(id++, left - 15 + 8, y, "", false);
        buttonList.add(buttonRadialMirrorEnabled);

        y = top + 18;
        textRadialMirrorPosX = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 58, y, 62, 18);
        textRadialMirrorPosX.setNumber(0);
        textRadialMirrorPosX.setTooltip(
                Arrays.asList("The position of the radial mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        radialMirrorNumberFieldList.add(textRadialMirrorPosX);

        textRadialMirrorPosY = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 138, y, 62, 18);
        textRadialMirrorPosY.setNumber(64);
        textRadialMirrorPosY.setTooltip(Arrays.asList("The position of the radial mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        radialMirrorNumberFieldList.add(textRadialMirrorPosY);

        textRadialMirrorPosZ = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 218, y, 62, 18);
        textRadialMirrorPosZ.setNumber(0);
        textRadialMirrorPosZ.setTooltip(Arrays.asList("The position of the radial mirror.", TextFormatting.GRAY + "For odd numbered builds add 0.5."));
        radialMirrorNumberFieldList.add(textRadialMirrorPosZ);

        y = top + 47;
        textRadialMirrorSlices = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 55, y, 50, 18);
        textRadialMirrorSlices.setNumber(4);
        textRadialMirrorSlices.setTooltip(Arrays.asList("The number of repeating slices.", TextFormatting.GRAY + "Minimally 2."));
        radialMirrorNumberFieldList.add(textRadialMirrorSlices);

        textRadialMirrorRadius = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 218, y, 62, 18);
        textRadialMirrorRadius.setNumber(50);
        //TODO change to diameter (remove /2)
        textRadialMirrorRadius.setTooltip(Arrays.asList("How far the radial mirror reaches from its center position.",
                TextFormatting.GRAY + "Max: " + TextFormatting.GOLD + ReachHelper.getMaxReach(mc.player) / 2,
                TextFormatting.GRAY + "Upgradeable in survival with reach upgrades."));
        radialMirrorNumberFieldList.add(textRadialMirrorRadius);


        y = top + 72;
        buttonCurrentPosition = new GuiIconButton(id++, left + 5, y, 0, 0, BUILDING_ICONS);
        buttonCurrentPosition.setTooltip("Set radial mirror position to current player position");
        radialMirrorIconButtonList.add(buttonCurrentPosition);

        buttonToggleOdd = new GuiIconButton(id++, left + 35, y, 0, 20, BUILDING_ICONS);
        buttonToggleOdd.setTooltip(Arrays.asList("Set radial mirror position to middle of block", "for odd numbered builds"));
        radialMirrorIconButtonList.add(buttonToggleOdd);

        buttonDrawLines = new GuiIconButton(id++, left + 65, y, 0, 40, BUILDING_ICONS);
        buttonDrawLines.setTooltip("Show lines");
        radialMirrorIconButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new GuiIconButton(id++, left + 95, y, 0, 60, BUILDING_ICONS);
        buttonDrawPlanes.setTooltip("Show area");
        radialMirrorIconButtonList.add(buttonDrawPlanes);

        y = top + 76;
        buttonRadialMirrorAlternate = new GuiCheckBox(id++, left + 140, y, " Alternate", false);
        radialMirrorButtonList.add(buttonRadialMirrorAlternate);

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
        if (modifierSettings != null) {
            RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
            buttonRadialMirrorEnabled.setIsChecked(r.enabled);
            textRadialMirrorPosX.setNumber(r.position.x);
            textRadialMirrorPosY.setNumber(r.position.y);
            textRadialMirrorPosZ.setNumber(r.position.z);
            textRadialMirrorSlices.setNumber(r.slices);
            buttonRadialMirrorAlternate.setIsChecked(r.alternate);
            textRadialMirrorRadius.setNumber(r.radius);
            drawLines = r.drawLines;
            drawPlanes = r.drawPlanes;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawLines.setTooltip(drawLines ? "Hide lines" : "Show lines");
            buttonDrawPlanes.setTooltip(drawPlanes ? "Hide area" : "Show area");
            if (textRadialMirrorPosX.getNumber() == Math.floor(textRadialMirrorPosX.getNumber())) {
                toggleOdd = false;
                buttonToggleOdd.setTooltip(Arrays.asList("Set radial mirror position to middle of block", "for odd numbered builds"));
            } else {
                toggleOdd = true;
                buttonToggleOdd.setTooltip(Arrays.asList("Set radial mirror position to corner of block", "for even numbered builds"));
            }
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
        }

        buttonList.addAll(radialMirrorButtonList);
        buttonList.addAll(radialMirrorIconButtonList);

        setCollapsed(!buttonRadialMirrorEnabled.isChecked());

        return id;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        radialMirrorNumberFieldList.forEach(GuiNumberField::update);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);

        int yy = y;
        int offset = 8;

        buttonRadialMirrorEnabled.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (buttonRadialMirrorEnabled.isChecked()) {
            buttonRadialMirrorEnabled.y = yy;
            fontRenderer.drawString("Radial mirror enabled", left + offset, yy + 2, 0xFFFFFF, true);

            yy = y + 18;
            fontRenderer.drawString("Position", left + offset, yy + 5, 0xFFFFFF, true);
            fontRenderer.drawString("X", left + 40 + offset, yy + 5, 0xFFFFFF, true);
            textRadialMirrorPosX.y = yy;
            fontRenderer.drawString("Y", left + 120 + offset, yy + 5, 0xFFFFFF, true);
            textRadialMirrorPosY.y = yy;
            fontRenderer.drawString("Z", left + 200 + offset, yy + 5, 0xFFFFFF, true);
            textRadialMirrorPosZ.y = yy;

            yy = y + 50;
            fontRenderer.drawString("Slices", left + offset, yy + 2, 0xFFFFFF, true);
            textRadialMirrorSlices.y = yy - 3;
            fontRenderer.drawString("Radius", left + 176 + offset, yy + 2, 0xFFFFFF, true);
            textRadialMirrorRadius.y = yy - 3;

            yy = y + 72;
            buttonCurrentPosition.y = yy;
            buttonToggleOdd.y = yy;
            buttonDrawLines.y = yy;
            buttonDrawPlanes.y = yy;

            yy = y + 76;
            buttonRadialMirrorAlternate.y = yy;

            radialMirrorButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            radialMirrorIconButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            radialMirrorNumberFieldList
                    .forEach(numberField -> numberField.drawNumberField(this.mc, mouseX, mouseY, partialTicks));
        } else {
            buttonRadialMirrorEnabled.y = yy;
            fontRenderer.drawString("Radial mirror disabled", left + offset, yy + 2, 0x999999, true);
        }

    }

    public void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonRadialMirrorEnabled.isChecked())
        {
            radialMirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(scrollPane.parent, mouseX, mouseY));
            radialMirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        super.updatePosition(slotIndex, x, y, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (GuiNumberField numberField : radialMirrorNumberFieldList) {
            numberField.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
        radialMirrorNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideRadialMirrorEnabledLabel = mouseX >= left && mouseX < right && relativeY >= -2 && relativeY < 12;

        if (insideRadialMirrorEnabledLabel) {
            buttonRadialMirrorEnabled.setIsChecked(!buttonRadialMirrorEnabled.isChecked());
            buttonRadialMirrorEnabled.playPressSound(this.mc.getSoundHandler());
            actionPerformed(buttonRadialMirrorEnabled);
        }

        return true;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button == buttonRadialMirrorEnabled) {
            setCollapsed(!buttonRadialMirrorEnabled.isChecked());
        }
        if (button == buttonCurrentPosition) {
            Vec3d pos = new Vec3d(Math.floor(mc.player.posX) + 0.5, Math.floor(mc.player.posY) + 0.5, Math.floor(mc.player.posZ) + 0.5);
            textRadialMirrorPosX.setNumber(pos.x);
            textRadialMirrorPosY.setNumber(pos.y);
            textRadialMirrorPosZ.setNumber(pos.z);
        }
        if (button == buttonToggleOdd) {
            toggleOdd = !toggleOdd;
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
            if (toggleOdd) {
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to corner of block", "for even numbered builds"));
                textRadialMirrorPosX.setNumber(textRadialMirrorPosX.getNumber() + 0.5);
                textRadialMirrorPosY.setNumber(textRadialMirrorPosY.getNumber() + 0.5);
                textRadialMirrorPosZ.setNumber(textRadialMirrorPosZ.getNumber() + 0.5);
            } else {
                buttonToggleOdd.setTooltip(Arrays.asList("Set mirror position to middle of block", "for odd numbered builds"));
                textRadialMirrorPosX.setNumber(Math.floor(textRadialMirrorPosX.getNumber()));
                textRadialMirrorPosY.setNumber(Math.floor(textRadialMirrorPosY.getNumber()));
                textRadialMirrorPosZ.setNumber(Math.floor(textRadialMirrorPosZ.getNumber()));
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
        radialMirrorNumberFieldList.forEach(numberField -> numberField.actionPerformed(button));
    }

    public RadialMirror.RadialMirrorSettings getRadialMirrorSettings() {
        boolean radialMirrorEnabled = buttonRadialMirrorEnabled.isChecked();

        Vec3d radialMirrorPos = new Vec3d(0, 64, 0);
        try {
            radialMirrorPos = new Vec3d(textRadialMirrorPosX.getNumber(), textRadialMirrorPosY.getNumber(), textRadialMirrorPosZ
                    .getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(mc.player, "Radial mirror position not a valid number.");
        }

        int radialMirrorSlices = 4;
        try {
            radialMirrorSlices = (int) textRadialMirrorSlices.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(mc.player, "Radial mirror slices not a valid number.");
        }

        boolean radialMirrorAlternate = buttonRadialMirrorAlternate.isChecked();

        int radialMirrorRadius = 50;
        try {
            radialMirrorRadius = (int) textRadialMirrorRadius.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(mc.player, "Mirror radius not a valid number.");
        }

        return new RadialMirror.RadialMirrorSettings(radialMirrorEnabled, radialMirrorPos, radialMirrorSlices, radialMirrorAlternate, radialMirrorRadius, drawLines, drawPlanes);
    }

    @Override
    protected String getName() {
        return "Radial mirror";
    }

    @Override
    protected int getExpandedHeight() {
        return 100;
    }
}
