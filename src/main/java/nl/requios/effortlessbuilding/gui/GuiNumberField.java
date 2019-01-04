package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiNumberField extends Gui {

    int x, y, width, height;
    int buttonWidth = 10;

    protected GuiTextField textField;
    protected GuiButton minusButton, plusButton;

    List<String> tooltip = new ArrayList<>();

    public GuiNumberField(int id1, int id2, int id3, FontRenderer fontRenderer,
                          List<GuiButton> buttonList, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        textField = new GuiTextField(id1, fontRenderer, x + buttonWidth + 1, y + 1, width - 2 * buttonWidth - 2, height - 2);
        minusButton = new GuiButton(id2, x, y - 1, buttonWidth, height + 2, "-");
        plusButton  = new GuiButton(id3, x + width - buttonWidth, y - 1, buttonWidth, height + 2, "+");

        buttonList.add(minusButton);
        buttonList.add(plusButton);
    }

    public void setNumber(double number) {
        DecimalFormat format = new DecimalFormat("0.#");
        textField.setText(format.format(number));
    }

    public double getNumber() {
        if (textField.getText().isEmpty()) return 0;
        return Double.parseDouble(textField.getText());
    }

    public void setTooltip(String tooltip) {
        setTooltip(Arrays.asList(tooltip));
    }

    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean result = textField.mouseClicked(mouseX, mouseY, mouseButton);

        //Check if clicked inside textfield
        boolean flag = mouseX >= x + buttonWidth && mouseX < x + width - buttonWidth && mouseY >= y && mouseY < y + height;

        //Rightclicked inside textfield
        if (flag && mouseButton == 1) {
            textField.setText("");
            textField.setFocused(true);
            result = true;
        }

        return result;
    }

    public void drawNumberField(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        textField.y = y + 1;
        minusButton.y = y - 1;
        plusButton.y = y - 1;

        textField.drawTextBox();
        minusButton.drawButton(mc, mouseX, mouseY, partialTicks);
        plusButton.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    public void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY) {
        boolean insideTextField = mouseX >= x + buttonWidth && mouseX < x + width - buttonWidth && mouseY >= y && mouseY < y + height;
        boolean insideMinusButton = mouseX >= x && mouseX < x + buttonWidth && mouseY >= y && mouseY < y + height;
        boolean insidePlusButton = mouseX >= x + width - buttonWidth && mouseX < x + width && mouseY >= y && mouseY < y + height;

        List<String> textLines = new ArrayList<>();

        if (insideTextField) {
            if (!tooltip.isEmpty())
                textLines.addAll(tooltip);
//            textLines.add(TextFormatting.GRAY + "Tip: try scrolling.");
        }

        if (insideMinusButton) {
            textLines.add("Hold " + TextFormatting.AQUA + "shift " + TextFormatting.RESET + "for " + TextFormatting.RED + "10");
            textLines.add("Hold " + TextFormatting.AQUA + "ctrl " + TextFormatting.RESET + "for " + TextFormatting.RED + "5");
        }

        if (insidePlusButton) {
            textLines.add("Hold " + TextFormatting.AQUA + "shift " + TextFormatting.RESET + "for " + TextFormatting.DARK_GREEN + "10");
            textLines.add("Hold " + TextFormatting.AQUA + "ctrl " + TextFormatting.RESET + "for " + TextFormatting.DARK_GREEN + "5");
        }

        guiScreen.drawHoveringText(textLines, mouseX - 10, mouseY + 25);

    }

    protected void actionPerformed(GuiButton button) {
        float valueChanged = 1f;
        if (GuiScreen.isCtrlKeyDown()) valueChanged = 5f;
        if (GuiScreen.isShiftKeyDown()) valueChanged = 10f;

        if (button == minusButton) {
            setNumber(getNumber() - valueChanged);
        }
        if (button == plusButton) {
            setNumber(getNumber() + valueChanged);
        }
    }

    public void update() {
        textField.updateCursorCounter();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!textField.isFocused()) return;
//        if (Character.isDigit(typedChar) || typedChar == '.' || typedChar == '-' || keyCode == Keyboard.KEY_BACK
//            || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT
//            || keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_DOWN) {
            textField.textboxKeyTyped(typedChar, keyCode);
//        }
    }

    //Scroll inside textfield to change number
//    public void handleMouseInput(int mouseX, int mouseY) {
//        boolean insideTextField = mouseX >= x + buttonWidth && mouseX < x + width - buttonWidth && mouseY >= y && mouseY < y + height;
//
//        if (insideTextField)
//        {
//            int valueChanged = 0;
//            if (Mouse.getEventDWheel() > 0)
//                valueChanged = 1;
//            if (Mouse.getEventDWheel() < 0)
//                valueChanged = -1;
//
//            if (valueChanged != 0)
//                setNumber(getNumber() + valueChanged);
//        }
//    }
}
