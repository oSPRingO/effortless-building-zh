package nl.requios.effortlessbuilding.gui.buildmodifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;
import nl.requios.effortlessbuilding.gui.elements.GuiScrollPane;
import nl.requios.effortlessbuilding.network.ModifierSettingsMessage;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.proxy.ClientProxy;

@OnlyIn(Dist.CLIENT)
public class ModifierSettingsGui extends Screen {

    private GuiScrollPane scrollPane;
    private Button buttonClose;

    private MirrorSettingsGui mirrorSettingsGui;
    private ArraySettingsGui arraySettingsGui;
    private RadialMirrorSettingsGui radialMirrorSettingsGui;

    @Override
    //Create buttons and labels and add them to buttonList/labelList
    public void initGui() {
        int id = 0;

        scrollPane = new GuiScrollPane(this, font, 8, height - 30);

        mirrorSettingsGui = new MirrorSettingsGui(scrollPane);
        scrollPane.AddListEntry(mirrorSettingsGui);

        arraySettingsGui = new ArraySettingsGui(scrollPane);
        scrollPane.AddListEntry(arraySettingsGui);

        radialMirrorSettingsGui = new RadialMirrorSettingsGui(scrollPane);
        scrollPane.AddListEntry(radialMirrorSettingsGui);

        id = scrollPane.initGui(id, buttons);

        //Close button
        int y = height - 26;
        buttonClose = new Button(width / 2 - 100, y, "Close") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                Minecraft.getInstance().player.closeScreen();
            }
        };
        buttons.add(buttonClose);

    }

    @Override
    //Process general logic, i.e. hide buttons
    public void tick() {
        scrollPane.updateScreen();

        handleMouseInput();
    }

    @Override
    //Set colors using GL11, use the fontObj field to display text
    //Use drawTexturedModalRect() to transfers areas of a texture resource to the screen
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        scrollPane.drawScreen(mouseX, mouseY, partialTicks);

        buttonClose.render(mouseX, mouseY, partialTicks);

        scrollPane.drawTooltip(this, mouseX, mouseY);
    }


    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        scrollPane.charTyped(typedChar, keyCode);
        if (keyCode == ClientProxy.keyBindings[0].getKey().getKeyCode()) {
            mc.player.closeScreen();
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
        return scrollPane.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (state != 0 || !scrollPane.mouseReleased(mouseX, mouseY, state))
        {
            return super.mouseReleased(mouseX, mouseY, state);
        }
        return false;
    }

    public void handleMouseInput() {
        //super.handleMouseInput();
        scrollPane.handleMouseInput();

        //Scrolling numbers
//        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
//        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//        numberFieldList.forEach(numberField -> numberField.handleMouseInput(mouseX, mouseY));
    }

    @Override
    public void onGuiClosed() {
        scrollPane.onGuiClosed();

        //save everything
        Mirror.MirrorSettings m = mirrorSettingsGui.getMirrorSettings();
        Array.ArraySettings a = arraySettingsGui.getArraySettings();
        RadialMirror.RadialMirrorSettings r = radialMirrorSettingsGui.getRadialMirrorSettings();

        ModifierSettingsManager.ModifierSettings modifierSettings = ModifierSettingsManager.getModifierSettings(mc.player);
        if (modifierSettings == null) modifierSettings = new ModifierSettingsManager.ModifierSettings();
        modifierSettings.setMirrorSettings(m);
        modifierSettings.setArraySettings(a);
        modifierSettings.setRadialMirrorSettings(r);

        //Sanitize
        String error = ModifierSettingsManager.sanitize(modifierSettings, mc.player);
        if (!error.isEmpty()) EffortlessBuilding.log(mc.player, error);

        ModifierSettingsManager.setModifierSettings(mc.player, modifierSettings);

        //Send to server
        PacketHandler.INSTANCE.sendToServer(new ModifierSettingsMessage(modifierSettings));

        //TODO fix not being able to scroll after this gui has opened
        Minecraft.getInstance().mouseHelper.grabMouse();
    }

}
