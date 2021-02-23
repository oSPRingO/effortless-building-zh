package nl.requios.effortlessbuildingzh.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiIconButton extends Button {

    private final ResourceLocation resourceLocation;
    private final int iconX, iconY, iconWidth, iconHeight, iconAltX, iconAltY;
    List<String> tooltip = new ArrayList<>();
    private boolean useAltIcon = false;

    public GuiIconButton(int x, int y, int iconX, int iconY, ResourceLocation resourceLocation, Button.IPressable onPress) {
        this(x, y, 20, 20, iconX, iconY, 20, 20, 20, 0, resourceLocation, onPress);
    }

    public GuiIconButton(int x, int y, int width, int height, int iconX, int iconY, int iconWidth, int iconHeight, int iconAltX, int iconAltY, ResourceLocation resourceLocation, Button.IPressable onPress) {
        super(x, y, width, height, "", onPress);
        this.iconX = iconX;
        this.iconY = iconY;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.iconAltX = iconAltX;
        this.iconAltY = iconAltY;
        this.resourceLocation = resourceLocation;
    }

    public void setTooltip(String tooltip) {
        setTooltip(Arrays.asList(tooltip));
    }

    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }

    public void setUseAlternateIcon(boolean useAlternateIcon) {
        this.useAltIcon = useAlternateIcon;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (this.visible)
        {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            Minecraft.getInstance().getTextureManager().bindTexture(this.resourceLocation);
            int currentIconX = this.iconX;
            int currentIconY = this.iconY;

            if (useAltIcon)
            {
                currentIconX += iconAltX;
                currentIconY += iconAltY;
            }

            //Draws a textured rectangle at the current z-value. Used to be drawTexturedModalRect in Gui.
            this.blit(this.x, this.y, currentIconX, currentIconY, this.iconWidth, this.iconHeight);
        }
    }

    public void drawTooltip(Screen screen, int mouseX, int mouseY) {
        boolean flag = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (flag) {
            List<String> textLines = new ArrayList<>();
            textLines.addAll(tooltip);
            screen.renderTooltip(textLines, mouseX - 10, mouseY + 25);
        }
    }
}
