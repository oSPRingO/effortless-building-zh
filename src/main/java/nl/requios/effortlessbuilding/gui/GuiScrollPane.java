package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiScrollPane extends GuiListExtended {

    public GuiScreen parent;
    public FontRenderer fontRenderer;
    public List<IScrollEntry> listEntries;

    public GuiScrollPane(GuiScreen parent, FontRenderer fontRenderer, int top, int bottom) {
        super(parent.mc, parent.width, parent.height, top, bottom, 100);
        this.parent = parent;
        this.fontRenderer = fontRenderer;
        this.setShowSelectionBox(false);
        listEntries = new ArrayList<>();
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return listEntries.get(index);
    }

    @Override
    protected int getSize() {
        return listEntries.size();
    }

    @Override
    protected int getContentHeight() {
        //TODO add every entry height
        return this.getSize() * this.slotHeight + this.headerPadding;
    }

    @Override
    protected int getScrollBarX() {
        //return width / 2 + 140 + 10;
        return width - 15;
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();

            //Own code: clipping



            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }

            //All entries
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);
            GlStateManager.disableDepth();
            //Dirt overlays on top and bottom
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            //top fade
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.left, (double)(this.top + 5), 0.0D).tex(0.0D, 1.0D).color(100, 100, 100, 0).endVertex();
            bufferbuilder.pos((double)this.right, (double)(this.top + 5), 0.0D).tex(1.0D, 1.0D).color(100, 100, 100, 0).endVertex();
            bufferbuilder.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(100, 100, 100, 100).endVertex();
            bufferbuilder.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(100, 100, 100, 100).endVertex();
            tessellator.draw();
            //top line
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 1.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 1.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.right, (double)(this.top - 1), 0.0D).tex(1.0D, 0.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.left, (double)(this.top - 1), 0.0D).tex(0.0D, 0.0D).color(20, 20, 20, 255).endVertex();
            tessellator.draw();
            //bottom fade
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(10, 10, 10, 100).endVertex();
            bufferbuilder.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(10, 10, 10, 100).endVertex();
            bufferbuilder.pos((double)this.right, (double)(this.bottom - 5), 0.0D).tex(1.0D, 0.0D).color(10, 10, 10, 0).endVertex();
            bufferbuilder.pos((double)this.left, (double)(this.bottom - 5), 0.0D).tex(0.0D, 0.0D).color(10, 10, 10, 0).endVertex();
            tessellator.draw();
            //bottom line
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.left, (double)(this.bottom + 1), 0.0D).tex(0.0D, 1.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.right, (double)(this.bottom + 1), 0.0D).tex(1.0D, 1.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 0.0D).color(20, 20, 20, 255).endVertex();
            bufferbuilder.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 0.0D).color(20, 20, 20, 255).endVertex();
            tessellator.draw();
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    //PASSTHROUGHS
    public int initGui(int id, List<GuiButton> buttonList) {
        for (IScrollEntry entry : this.listEntries) {
            id = entry.initGui(id, buttonList);
        }
        registerScrollButtons(id++, id++);
        return id;
    }

    public void updateScreen() {
        for (IScrollEntry entry : this.listEntries)
            entry.updateScreen();
    }

    public void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY) {
        for (IScrollEntry entry : this.listEntries)
            entry.drawTooltip(guiScreen, mouseX, mouseY);
    }

    public void keyTyped(char eventChar, int eventKey) throws IOException {
        for (IScrollEntry entry : this.listEntries)
            entry.keyTyped(eventChar, eventKey);
    }

    public void actionPerformed(GuiButton button) {
        for (IScrollEntry entry : this.listEntries)
            entry.actionPerformed(button);
    }

    public void onGuiClosed() {
        for (IScrollEntry entry : this.listEntries)
            entry.onGuiClosed();
    }

    public interface IScrollEntry extends GuiListExtended.IGuiListEntry {
        int initGui(int id, List<GuiButton> buttonList);

        void updateScreen();

        void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY);

        void keyTyped(char eventChar, int eventKey) throws IOException;

        void actionPerformed(GuiButton button);

        void onGuiClosed();
    }
}
