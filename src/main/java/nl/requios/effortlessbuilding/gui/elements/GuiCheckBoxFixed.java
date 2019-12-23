package nl.requios.effortlessbuilding.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.fml.client.config.GuiUtils;

/**
 * This class provides a checkbox style control.
 */
public class GuiCheckBoxFixed extends Button
{
    private boolean isChecked;
    private int     boxWidth;

    public GuiCheckBoxFixed(int xPos, int yPos, String displayString, boolean isChecked)
    {
        super(xPos, yPos, Minecraft.getInstance().fontRenderer.getStringWidth(displayString) + 2 + 11, 11, displayString, b -> {});
        this.isChecked = isChecked;
        this.boxWidth = 11;
        this.height = 11;
        this.width = this.boxWidth + 2 + Minecraft.getInstance().fontRenderer.getStringWidth(displayString);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.boxWidth && mouseY < this.y + this.height;
            GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.x, this.y, 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, this.blitOffset);
            this.renderBg(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColor != 0)
            {
                color = packedFGColor;
            }
            else if (!this.active)
            {
                color = 10526880;
            }

            if (this.isChecked)
                this.drawCenteredString(mc.fontRenderer, "x", this.x + this.boxWidth / 2 + 1, this.y + 1, 14737632);

            this.drawString(mc.fontRenderer, getMessage(), this.x + this.boxWidth + 2, this.y + 2, color);
        }
    }

    @Override
    public void onPress()
    {
        this.isChecked = !this.isChecked;
    }

    public boolean isChecked()
    {
        return this.isChecked;
    }

    public void setIsChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
    }
}