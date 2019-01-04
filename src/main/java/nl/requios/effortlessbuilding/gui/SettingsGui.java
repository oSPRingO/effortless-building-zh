package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import nl.requios.effortlessbuilding.*;
import nl.requios.effortlessbuilding.network.BuildSettingsMessage;
import nl.requios.effortlessbuilding.proxy.ClientProxy;

import java.io.IOException;

public class SettingsGui extends GuiScreen {

    private GuiScrollPane scrollPane;
    private GuiButton buttonClose;

    private MirrorSettingsGui mirrorSettingsGui;
    private ArraySettingsGui arraySettingsGui;
    private RadialMirrorSettingsGui radialMirrorSettingsGui;

    @Override
    //Create buttons and labels and add them to buttonList/labelList
    public void initGui() {
        int id = 0;

        scrollPane = new GuiScrollPane(this, fontRenderer, 8, height - 30);

        mirrorSettingsGui = new MirrorSettingsGui(scrollPane);
        scrollPane.listEntries.add(mirrorSettingsGui);

        arraySettingsGui = new ArraySettingsGui(scrollPane);
        scrollPane.listEntries.add(arraySettingsGui);

        radialMirrorSettingsGui = new RadialMirrorSettingsGui(scrollPane);
        scrollPane.listEntries.add(radialMirrorSettingsGui);

        id = scrollPane.initGui(id, buttonList);

        //Close button
        int y = height - 26;
        buttonClose = new GuiButton(id++, width / 2 - 100, y, "Close");
        buttonList.add(buttonClose);

    }

    @Override
    //Process general logic, i.e. hide buttons
    public void updateScreen() {
        scrollPane.updateScreen();
    }

    @Override
    //Set colors using GL11, use the fontRendererObj field to display text
    //Use drawTexturedModalRect() to transfers areas of a texture resource to the screen
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        scrollPane.drawScreen(mouseX, mouseY, partialTicks);

        buttonClose.drawButton(this.mc, mouseX, mouseY, partialTicks);

        scrollPane.drawTooltip(this, mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        scrollPane.keyTyped(typedChar, keyCode);
        if (keyCode == ClientProxy.keyBindings[0].getKeyCode()) {
            mc.player.closeScreen();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        scrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state != 0 || !scrollPane.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        scrollPane.handleMouseInput();

        //Scrolling numbers
//        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//        numberFieldList.forEach(numberField -> numberField.handleMouseInput(mouseX, mouseY));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        //check what button and action type (left/right click)
        if (button == buttonClose) {
            mc.player.closeScreen();
        }
        scrollPane.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        scrollPane.onGuiClosed();

        //save everything
        Mirror.MirrorSettings m = mirrorSettingsGui.getMirrorSettings();
        Array.ArraySettings a = arraySettingsGui.getArraySettings();
        RadialMirror.RadialMirrorSettings r = radialMirrorSettingsGui.getRadialMirrorSettings();

        BuildSettingsManager.BuildSettings buildSettings = BuildSettingsManager.getBuildSettings(mc.player);
        if (buildSettings == null) buildSettings = new BuildSettingsManager.BuildSettings();
        buildSettings.setMirrorSettings(m);
        buildSettings.setArraySettings(a);
        buildSettings.setRadialMirrorSettings(r);

        //Sanitize
        String error = BuildSettingsManager.sanitize(buildSettings, mc.player);
        if (!error.isEmpty()) EffortlessBuilding.log(mc.player, error);

        BuildSettingsManager.setBuildSettings(mc.player, buildSettings);

        //Send to server
        EffortlessBuilding.packetHandler.sendToServer(new BuildSettingsMessage(buildSettings));
    }

}
