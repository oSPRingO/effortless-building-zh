package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends GuiScreen {

    protected List<GuiTextField> textFieldList = new ArrayList<>();

    protected List<GuiButton> mirrorButtonList = new ArrayList<>();
    protected List<GuiTextField> mirrorTextFieldList = new ArrayList<>();
    protected List<GuiTextField> arrayTextFieldList = new ArrayList<>();

    private GuiTextField textMirrorPosX, textMirrorPosY, textMirrorPosZ, textMirrorRadius;
    private GuiCheckBox buttonMirrorEnabled, buttonMirrorX, buttonMirrorY, buttonMirrorZ;
    private GuiButton buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines;
    private GuiButton buttonCurrentPosition, buttonClose;

    private GuiCheckBox buttonArrayEnabled;
    private GuiTextField textArrayOffsetX, textArrayOffsetY, textArrayOffsetZ, textArrayCount;

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
        textMirrorPosX = new GuiTextField(id++, fontRenderer, left + 70, y, 50, 18);
        textMirrorPosX.setText("0.5");
        mirrorTextFieldList.add(textMirrorPosX);

        textMirrorPosY = new GuiTextField(id++, fontRenderer, left + 140, y, 50, 18);
        textMirrorPosY.setText("64.5");
        mirrorTextFieldList.add(textMirrorPosY);

        textMirrorPosZ = new GuiTextField(id++, fontRenderer, left + 210, y, 50, 18);
        textMirrorPosZ.setText("0.5");
        mirrorTextFieldList.add(textMirrorPosZ);

        y = top + 50;
        buttonMirrorX = new GuiCheckBox(id++, left + 60, y, " X", true);
        mirrorButtonList.add(buttonMirrorX);

        buttonMirrorY = new GuiCheckBox(id++, left + 100, y, " Y", false);
        mirrorButtonList.add(buttonMirrorY);

        buttonMirrorZ = new GuiCheckBox(id++, left + 140, y, " Z", false);
        mirrorButtonList.add(buttonMirrorZ);

        y = top + 47;
        textMirrorRadius = new GuiTextField(id++, fontRenderer, left + 220, y, 60, 18);
        textMirrorRadius.setText("50");
        mirrorTextFieldList.add(textMirrorRadius);

        y = top + 72;
        buttonCurrentPosition = new GuiButton(id++, left + 5, y, 110, 20, "Set to current pos");
        mirrorButtonList.add(buttonCurrentPosition);

        buttonDrawLines = new GuiButton(id++, left + 127, y, 70, 20, "Show lines");
        mirrorButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new GuiButton(id++, left + 209, y, 75, 20, "Show area");
        mirrorButtonList.add(buttonDrawPlanes);

        //ARRAY
        y = top + 100;
        buttonArrayEnabled = new GuiCheckBox(id++, left - 15 + 8, y, "", false);
        buttonList.add(buttonArrayEnabled);

        y = top + 120;
        textArrayOffsetX = new GuiTextField(id++, fontRenderer, left + 70, y, 50, 18);
        textArrayOffsetX.setText("0");
        arrayTextFieldList.add(textArrayOffsetX);

        textArrayOffsetY = new GuiTextField(id++, fontRenderer, left + 140, y, 50, 18);
        textArrayOffsetY.setText("0");
        arrayTextFieldList.add(textArrayOffsetY);

        textArrayOffsetZ = new GuiTextField(id++, fontRenderer, left + 210, y, 50, 18);
        textArrayOffsetZ.setText("0");
        arrayTextFieldList.add(textArrayOffsetZ);

        y = top + 150;
        textArrayCount = new GuiTextField(id++, fontRenderer, left + 55, y, 50, 18);
        textArrayCount.setText("5");
        arrayTextFieldList.add(textArrayCount);

        //CLOSE
        y = height - 40;
        buttonClose = new GuiButton(id++, width / 2 - 100, y, "Close");
        buttonList.add(buttonClose);

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(mc.player);
        if (buildSettings != null) {
            //MIRROR
            Mirror.MirrorSettings m = buildSettings.getMirrorSettings();
            buttonMirrorEnabled.setIsChecked(m.enabled);
            textMirrorPosX.setText(String.valueOf(m.position.x));
            textMirrorPosY.setText(String.valueOf(m.position.y));
            textMirrorPosZ.setText(String.valueOf(m.position.z));
            buttonMirrorX.setIsChecked(m.mirrorX);
            buttonMirrorY.setIsChecked(m.mirrorY);
            buttonMirrorZ.setIsChecked(m.mirrorZ);
            textMirrorRadius.setText(String.valueOf(m.radius));
            drawLines = m.drawLines;
            drawPlanes = m.drawPlanes;
            buttonDrawLines.displayString = drawLines ? "Hide lines" : "Show lines";
            buttonDrawPlanes.displayString = drawPlanes ? "Hide area" : "Show area";

            //ARRAY
            Array.ArraySettings a = buildSettings.getArraySettings();
            buttonArrayEnabled.setIsChecked(a.enabled);
            textArrayOffsetX.setText(String.valueOf(a.offset.getX()));
            textArrayOffsetY.setText(String.valueOf(a.offset.getY()));
            textArrayOffsetZ.setText(String.valueOf(a.offset.getZ()));
            textArrayCount.setText(String.valueOf(a.count));
        }

        buttonList.addAll(mirrorButtonList);
        textFieldList.addAll(mirrorTextFieldList);
        textFieldList.addAll(arrayTextFieldList);
    }

    @Override
    //Process general logic, i.e. hide buttons
    public void updateScreen() {
        textFieldList.forEach(GuiTextField::updateCursorCounter);
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
            fontRenderer.drawString("X", left + 50 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Y", left + 120 + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Z", left + 190 + offset, y, 0xFFFFFF, true);

            y = top + 52;
            fontRenderer.drawString("Direction", left + offset, y, 0xFFFFFF, true);
            fontRenderer.drawString("Size", left + 190, y, 0xFFFFFF, true);

            mirrorButtonList.forEach(button -> button.drawButton(this.mc, mouseX, mouseY, partialTicks));
            mirrorTextFieldList.forEach(textField -> textField.drawTextBox());
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

            arrayTextFieldList.forEach(textField -> textField.drawTextBox());
        } else {
            fontRenderer.drawString("Array disabled", left + offset, y, 0x999999, true);
        }

        buttonClose.drawButton(this.mc, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (Character.isDigit(typedChar) || typedChar == '.' || typedChar == '-' || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
            for (GuiTextField textField : textFieldList) {
                if (textField.isFocused()) {
                    textField.textboxKeyTyped(typedChar, keyCode);
                }
            }
        }
        if (keyCode == ClientProxy.keyBindings[0].getKeyCode()) {
            Minecraft.getMinecraft().player.closeScreen();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldList.forEach(textField -> textField.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        //check what button and action type (left/right click)
        if (button == buttonClose) {
            mc.player.closeScreen();
        }
        if (button == buttonCurrentPosition) {
            Vec3d pos = new Vec3d(Math.floor(mc.player.posX) + 0.5, Math.floor(mc.player.posY) + 0.5, Math.floor(mc.player.posZ) + 0.5);
            textMirrorPosX.setText(String.valueOf(pos.x));
            textMirrorPosY.setText(String.valueOf(pos.y));
            textMirrorPosZ.setText(String.valueOf(pos.z));
        }
        if (button == buttonDrawLines) {
            drawLines = !drawLines;
            buttonDrawLines.displayString = drawLines ? "Hide lines" : "Show lines";
        }
        if (button == buttonDrawPlanes) {
            drawPlanes = !drawPlanes;
            buttonDrawPlanes.displayString = drawPlanes ? "Hide area" : "Show area";
        }
    }

    @Override
    public void onGuiClosed() {
        //save everything

        //MIRROR
        boolean mirrorEnabled = buttonMirrorEnabled.isChecked();

        Vec3d mirrorPos = new Vec3d(0.5, 64.5, 0.5);
        try {
            mirrorPos = new Vec3d(Double.parseDouble(textMirrorPosX.getText()), Double.parseDouble(textMirrorPosY.getText()), Double.parseDouble(textMirrorPosZ.getText()));
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Mirror position not valid.", true);
            EffortlessBuilding.log("Mirror position not valid. Resetting to default.");
        }

        boolean mirrorX = buttonMirrorX.isChecked();
        boolean mirrorY = buttonMirrorY.isChecked();
        boolean mirrorZ = buttonMirrorZ.isChecked();

        int mirrorRadius = 50;
        try {
            mirrorRadius = Math.min(Integer.parseInt(textMirrorRadius.getText()), Mirror.MAX_RADIUS);
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
            arrayOffset = new BlockPos(Integer.parseInt(textArrayOffsetX.getText()), Integer.parseInt(textArrayOffsetY.getText()), Integer.parseInt(textArrayOffsetZ.getText()));
        } catch (NumberFormatException | NullPointerException ex) {
            EffortlessBuilding.log(Minecraft.getMinecraft().player, "Array offset not valid.", true);
            EffortlessBuilding.log("Array offset not valid. Resetting to default.");
        }

        int arrayCount = 5;
        try {
            arrayCount = Integer.parseInt(textArrayCount.getText());
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
