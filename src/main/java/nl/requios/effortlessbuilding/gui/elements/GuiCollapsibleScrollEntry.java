package nl.requios.effortlessbuilding.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class GuiCollapsibleScrollEntry implements GuiScrollPane.IScrollEntry {

    public GuiScrollPane scrollPane;
    protected FontRenderer fontRenderer;
    protected Minecraft mc;

    protected boolean isCollapsed = true;
    protected int left, right, top, bottom;

    public GuiCollapsibleScrollEntry(GuiScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        this.fontRenderer = scrollPane.fontRenderer;
        this.mc = scrollPane.parent.mc;
    }

    @Override
    public int initGui(int id, List<GuiButton> buttonList) {

        left = scrollPane.width / 2 - 140;
        right = scrollPane.width / 2 + 140;
        top = scrollPane.height / 2 - 100;
        bottom = scrollPane.height / 2 + 100;

        return id;
    }

    @Override
    public void updateScreen() {
    }

    @Override
    public void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY) {
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

    }

    @Override
    public boolean charTyped(char eventChar, int eventKey) {
        return false;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

    }

    @Override
    public void onGuiClosed() {
    }

    @Override
    public int getHeight() {
        return isCollapsed ? getCollapsedHeight() : getExpandedHeight();
    }

    public void setCollapsed(boolean collapsed) {
        this.isCollapsed = collapsed;
    }

    protected String getName() {
        return "Collapsible scroll entry";
    }

    protected int getCollapsedHeight() {
        return 24;
    }

    protected int getExpandedHeight() {
        return 100;
    }
}
