package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import nl.requios.effortlessbuilding.Array;
import nl.requios.effortlessbuilding.BuildSettingsManager;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.Mirror;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsGui extends GuiScreen {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/building_icons.png");

    protected List<GuiNumberField> numberFieldList = new ArrayList<>();

    protected List<GuiButton> mirrorButtonList = new ArrayList<>();
    protected List<GuiIconButton> mirrorIconButtonList = new ArrayList<>();
    protected List<GuiNumberField> mirrorNumberFieldList = new ArrayList<>();
    protected List<GuiNumberField> arrayNumberFieldList = new ArrayList<>();

    private GuiNumberField textMirrorPosX, textMirrorPosY, textMirrorPosZ, textMirrorRadius;
    private GuiCheckBox buttonMirrorEnabled, buttonMirrorX, buttonMirrorY, buttonMirrorZ;
    private GuiIconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;
    private GuiButton buttonClose;

    private GuiCheckBox buttonArrayEnabled;
    private GuiNumberField textArrayOffsetX, textArrayOffsetY, textArrayOffsetZ, textArrayCount;

    private int left, right, top, bottom;

    @Override
    //Create buttons and labels and add them to buttonList/labelList
    public void initGui() {
        int id = 0;
        left = width / 2 - 140;
        right = width / 2 + 140;
        top = height / 2 - 100;
        bottom = height / 2 + 100;

        //MIRROR
        int y = top - 2;
        buttonMirrorEnabled = new GuiCheckBox(id++, left - 15 + 8, y, "", false);
        buttonList.add(buttonMirrorEnabled);

        y = top + 18;
        textMirrorPosX = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 58, y, 62, 18);
        textMirrorPosX.setNumber(0);
        textMirrorPosX.setTooltip(Arrays.asList("The position of the mirror.", "For odd numbered builds add 0.5."));
        mirrorNumberFieldList.add(textMirrorPosX);

        textMirrorPosY = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 138, y, 62, 18);
        textMirrorPosY.setNumber(64);
        textMirrorPosY.setTooltip(Arrays.asList("The position of the mirror.", "For odd numbered builds add 0.5."));
        mirrorNumberFieldList.add(textMirrorPosY);

        textMirrorPosZ = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 218, y, 62, 18);
        textMirrorPosZ.setNumber(0);
        textMirrorPosZ.setTooltip(Arrays.asList("The position of the mirror.", "For odd numbered builds add 0.5."));
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
        textMirrorRadius.setTooltip("How far the mirror reaches in any direction.");
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

        //ARRAY
        y = top + 100;
        buttonArrayEnabled = new GuiCheckBox(id++, left - 15 + 8, y, "", false);
        buttonList.add(buttonArrayEnabled);

        y = top + 120;
        textArrayOffsetX = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 70, y, 50, 18);
        textArrayOffsetX.setNumber(0);
        textArrayOffsetX.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetX);

        textArrayOffsetY = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 140, y, 50, 18);
        textArrayOffsetY.setNumber(0);
        textArrayOffsetY.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetY);

        textArrayOffsetZ = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 210, y, 50, 18);
        textArrayOffsetZ.setNumber(0);
        textArrayOffsetZ.setTooltip("How much each copy is shifted.");
        arrayNumberFieldList.add(textArrayOffsetZ);

        y = top + 150;
        textArrayCount = new GuiNumberField(id++, id++, id++, fontRenderer, buttonList, left + 55, y, 50, 18);
        textArrayCount.setNumber(5);
        textArrayCount.setTooltip("How many copies should be made.");
        arrayNumberFieldList.add(textArrayCount);

        //CLOSE
        y = height - 40;
        buttonClose = new GuiButton(id++, width / 2 - 100, y, "Close");
        buttonList.add(buttonClose);

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(mc.player);
        if (buildSettings != null) {
            //MIRROR
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

            //ARRAY
            Array.ArraySettings a = buildSettings.getArraySettings();
            buttonArrayEnabled.setIsChecked(a.enabled);
            textArrayOffsetX.setNumber(a.offset.getX());
            textArrayOffsetY.setNumber(a.offset.getY());
            textArrayOffsetZ.setNumber(a.offset.getZ());
            textArrayCount.setNumber(a.count);
        }

        buttonList.addAll(mirrorButtonList);
        buttonList.addAll(mirrorIconButtonList);
        numberFieldList.addAll(mirrorNumberFieldList);
        numberFieldList.addAll(arrayNumberFieldList);
    }

    @Override
    //Process general logic, i.e. hide buttons
    public void updateScreen() {
        numberFieldList.forEach(GuiNumberField::update);
    }

    @Override
    //Set colors using GL11, use the fontRendererObj field to display text
    //Use drawTexturedModalRect() to transfers areas of a texture resource to the screen
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int y = top;
        int offset = 8;

        buttonMirrorEnabled.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (buttonMirrorEnabled.isChecked()) {
            fontRenderer.drawString("Mirror enabled", left + offset, y, 0xFFFFFF, true);

            y = top + 18 + 5;
            fontRenderer.drawString("Position", left + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("X", left + 40 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Y", left + 120 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Z", left + 200 + offset, y, 0xFFFFFF, true);

            y = top + 52;
            fontRenderer.drawString("Direction", left + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Size", left + 190, y, 0xFFFFFF, true);

            mirrorButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            mirrorIconButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawNumberField(this.mc, mouseX, mouseY, partialTicks));
        } else {
            fontRenderer.drawString("Mirror disabled", left + offset, y, 0x999999, true);
        }

        y = top + 100 + 2;
        buttonArrayEnabled.drawButton(this.mc, mouseX, mouseY, partialTicks);
        if (buttonArrayEnabled.isChecked()) {
            fontRenderer.drawString("Array enabled", left + offset, y, 0xFFFFFF, true);

            y = top + 120 + 5;
            fontRenderer.drawString("Offset", left + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("X", left + 50 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Y", left + 120 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Z", left + 190 + offset, y, 0xFFFFFF, true);

            y = top + 150 + 5;
            fontRenderer.drawString("Count", left + offset, y, 0xFFFFFF, true);

            arrayNumberFieldList.forEach(numberField -> numberField.drawNumberField(this.mc, mouseX, mouseY, partialTicks));
        } else {
            fontRenderer.drawString("Array disabled", left + offset, y, 0x999999, true);
        }

        buttonClose.drawButton(this.mc, mouseX, mouseY, partialTicks);

        //Draw tooltips last
        if (buttonMirrorEnabled.isChecked())
        {
            mirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(this, mouseX, mouseY));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(this, mouseX, mouseY));
        }
        if (buttonArrayEnabled.isChecked())
        {
            arrayNumberFieldList.forEach(numberField -> numberField.drawTooltip(this, mouseX, mouseY));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (GuiNumberField numberField : numberFieldList) {
            numberField.keyTyped(typedChar, keyCode);
        }
        if (keyCode == ClientProxy.keyBindings[0].getKeyCode()) {
            Minecraft.getMinecraft().player.closeScreen();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        numberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseButton));

        boolean insideMirrorEnabledLabel = mouseX >= left && mouseX < right && mouseY >= top - 2 && mouseY < top + 10;
        boolean insideArrayEnabledLabel = mouseX >= left && mouseX < right && mouseY >= top + 100 && mouseY < top + 112;

        if (insideMirrorEnabledLabel) {
            buttonMirrorEnabled.setIsChecked(!buttonMirrorEnabled.isChecked());
            buttonMirrorEnabled.playPressSound(this.mc.getSoundHandler());
            actionPerformed(buttonMirrorEnabled);
        }

        if (insideArrayEnabledLabel) {
            buttonArrayEnabled.setIsChecked(!buttonArrayEnabled.isChecked());
            buttonArrayEnabled.playPressSound(this.mc.getSoundHandler());
            actionPerformed(buttonArrayEnabled);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        numberFieldList.forEach(numberField -> numberField.handleMouseInput(mouseX, mouseY));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        //check what button and action type (left/right click)
        if (button == buttonClose) {
            mc.player.closeScreen();
        }
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
        numberFieldList.forEach(numberField -> numberField.actionPerformed(button));
    }

    @Override
    public void onGuiClosed() {
        //save everything

        //MIRROR
        boolean mirrorEnabled = buttonMirrorEnabled.isChecked();

        Vec3d mirrorPos = new Vec3d(0, 64, 0);
        try {
            mirrorPos = new Vec3d(textMirrorPosX.getNumber(), textMirrorPosY.getNumber(), textMirrorPosZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Mirror position not valid.", true);
            EffortlessBuilding.log("Mirror position not valid. Resetting to default.");
        }

        boolean mirrorX = buttonMirrorX.isChecked();
        boolean mirrorY = buttonMirrorY.isChecked();
        boolean mirrorZ = buttonMirrorZ.isChecked();

        int mirrorRadius = 50;
        try {
            mirrorRadius = Math.min((int) textMirrorRadius.getNumber(), Mirror.MAX_RADIUS);
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Mirror radius not valid.", true);
            EffortlessBuilding.log("Mirror radius not valid. Resetting to default.");
        }
        mirrorRadius = Math.max(1, mirrorRadius);
        mirrorRadius = Math.min(Mirror.MAX_RADIUS, mirrorRadius);

        Mirror.MirrorSettings m = new Mirror.MirrorSettings(mirrorEnabled, mirrorPos, mirrorX, mirrorY, mirrorZ, mirrorRadius, drawLines, drawPlanes);

        //ARRAY
        boolean arrayEnabled = buttonArrayEnabled.isChecked();
        BlockPos arrayOffset = new BlockPos(0, 0, 0);
        try {
            arrayOffset = new BlockPos(textArrayOffsetX.getNumber(), textArrayOffsetY.getNumber(), textArrayOffsetZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Array offset not valid.", true);
            EffortlessBuilding.log("Array offset not valid. Resetting to default.");
        }

        int arrayCount = 5;
        try {
            arrayCount = (int) textArrayCount.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Array count not valid.", true);
            EffortlessBuilding.log("Array count not valid. Resetting to default.");
        }
        arrayCount = Math.max(1, arrayCount);
        arrayCount = Math.min(Array.MAX_COUNT, arrayCount);

        Array.ArraySettings a = new Array.ArraySettings(arrayEnabled, arrayOffset, arrayCount);

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(mc.player);
        if (buildSettings == null) buildSettings = new BuildSettingsManager.BuildSettings();
        buildSettings.setMirrorSettings(m);
        buildSettings.setArraySettings(a);
        BuildSettingsManager.setBuildSettings(mc.player, buildSettings);

        //Send to server
        EffortlessBuilding.packetHandler.sendToServer(new BuildSettingsMessage(buildSettings));
    }
}
