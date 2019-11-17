package nl.requios.effortlessbuilding.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

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
    protected int getScrollBarX() {
        //return width / 2 + 140 + 10;
        return width - 15;
    }

    @Override
    public int getListWidth() {
        return 280;
    }

    //Removed background
    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            int scrollBarLeft = this.getScrollBarX();
            int scrollBarRight = scrollBarLeft + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int insideLeft = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int insideTop = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader) {
                this.drawListHeader(insideLeft, insideTop, tessellator);
            }

            //All entries
            this.drawSelectionBox(insideLeft, insideTop, mouseXIn, mouseYIn, partialTicks);
            GlStateManager.disableDepth();

            //Dirt overlays on top and bottom
//            this.overlayBackground(0, this.top, 255, 255);
//            this.overlayBackground(this.bottom, this.height, 255, 255);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();

//            //top fade
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.left, (double)(this.top + 5), 0.0D).tex(0.0D, 1.0D).color(100, 100, 100, 0).endVertex();
//            bufferbuilder.pos((double)this.right, (double)(this.top + 5), 0.0D).tex(1.0D, 1.0D).color(100, 100, 100, 0).endVertex();
//            bufferbuilder.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(100, 100, 100, 100).endVertex();
//            bufferbuilder.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(100, 100, 100, 100).endVertex();
//            tessellator.draw();

//            //top line
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 1.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 1.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.right, (double)(this.top - 1), 0.0D).tex(1.0D, 0.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.left, (double)(this.top - 1), 0.0D).tex(0.0D, 0.0D).color(20, 20, 20, 255).endVertex();
//            tessellator.draw();

//            //bottom fade
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(10, 10, 10, 100).endVertex();
//            bufferbuilder.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(10, 10, 10, 100).endVertex();
//            bufferbuilder.pos((double)this.right, (double)(this.bottom - 5), 0.0D).tex(1.0D, 0.0D).color(10, 10, 10, 0).endVertex();
//            bufferbuilder.pos((double)this.left, (double)(this.bottom - 5), 0.0D).tex(0.0D, 0.0D).color(10, 10, 10, 0).endVertex();
//            tessellator.draw();

//            //bottom line
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.left, (double)(this.bottom + 1), 0.0D).tex(0.0D, 1.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.right, (double)(this.bottom + 1), 0.0D).tex(1.0D, 1.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 0.0D).color(20, 20, 20, 255).endVertex();
//            bufferbuilder.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 0.0D).color(20, 20, 20, 255).endVertex();
//            tessellator.draw();

            //Draw scrollbar
            int maxScroll = this.getMaxScroll();
            if (maxScroll > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / maxScroll + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollBarLeft, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollBarRight, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollBarRight, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollBarLeft, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollBarLeft, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollBarRight, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollBarRight, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollBarLeft, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollBarLeft, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(scrollBarRight - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(scrollBarRight - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)scrollBarLeft, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    //SLOTHEIGHT MODIFICATIONS
    //SlotHeight is still relied on for determining how much to scroll
    @Override
    protected int getContentHeight() {
        //Add every entry height
        int height = this.headerPadding;
        for (IScrollEntry entry : listEntries) {
            height += entry.getHeight();
        }
        return height;
    }

    public int getContentHeight(int count) {
        //Add all count entry heights
        int height = this.headerPadding;
        for (int i = 0; i < count; i++) {
            IScrollEntry entry = listEntries.get(i);
            height += entry.getHeight();
        }
        return height;
    }

    @Override
    public int getSlotIndexFromScreenCoords(int posX, int posY) {
        int left = this.left + (this.width - this.getListWidth()) / 2;
        int right = this.left + (this.width + this.getListWidth()) / 2;
        int relativeMouseY = getRelativeMouseY(mouseY, 0);

        //Iterate over every entry until relativeMouseY falls within its height
        for (int i = 0; i < listEntries.size(); i++) {
            IScrollEntry entry = listEntries.get(i);
            if (relativeMouseY <= entry.getHeight())
                return posX < this.getScrollBarX() && posX >= left && posX <= right && i >= 0 &&
                       relativeMouseY >= 0 && i < this.getSize() ? i : -1;
            relativeMouseY -= entry.getHeight();
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
    {
        int selectedSlot = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        int relativeX = getRelativeMouseX(mouseX);

        //Always pass through mouseclicked, to be able to unfocus textfields
        for (int i = 0; i < this.listEntries.size(); i++) {
            int relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mousePressed(selectedSlot, mouseX, mouseY, mouseEvent, relativeX, relativeY);
        }


//        if (this.isMouseYWithinSlotBounds(mouseY))
//        {
//            int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
//
//            if (i >= 0)
//            {
//                int relativeX = getRelativeMouseX(mouseX);
//                int relativeY = getRelativeMouseY(mouseY, i);
//
//                if (this.getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, relativeX, relativeY))
//                {
//                    this.setEnabled(false);
//                    return true;
//                }
//            }
//        }

        return false;
    }

    @Override
    public boolean mouseReleased(int x, int y, int mouseEvent)
    {
        for (int i = 0; i < this.getSize(); ++i)
        {
            int relativeX = getRelativeMouseX(mouseX);
            int relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mouseReleased(i, x, y, mouseEvent, relativeX, relativeY);
        }

        this.setEnabled(true);
        return false;
    }

    @Override
    public void handleMouseInput() {
        if (this.isMouseYWithinSlotBounds(this.mouseY)) {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top &&
                this.mouseY <= this.bottom) {
                int i = this.left + (this.width - this.getListWidth()) / 2;
                int j = this.left + (this.width + this.getListWidth()) / 2;
                int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                int relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                if (slotIndex > -1) {
                    this.elementClicked(slotIndex, false, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (this.mouseX >= i && this.mouseX <= j && relativeMouseY < 0) {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }

            if (Mouse.isButtonDown(0) && this.getEnabled()) {
                if (this.initialClickY == -1) {
                    boolean flag1 = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                        int i2 = this.left + (this.width - this.getListWidth()) / 2;
                        int j2 = this.left + (this.width + this.getListWidth()) / 2;
                        int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                        int relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                        if (slotIndex > -1) {
                            boolean flag = slotIndex == this.selectedElement &&
                                           Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(slotIndex, flag, this.mouseX, this.mouseY);
                            this.selectedElement = slotIndex;
                            this.lastClicked = Minecraft.getSystemTime();
                        } else if (this.mouseX >= i2 && this.mouseX <= j2 && relativeMouseY < 0) {
                            this.clickedHeader(this.mouseX - i2,
                                    this.mouseY - this.top + (int) this.amountScrolled - 4);
                            flag1 = false;
                        }

                        int i3 = this.getScrollBarX();
                        int j1 = i3 + 6;

                        if (this.mouseX >= i3 && this.mouseX <= j1) {
                            this.scrollMultiplier = -1.0F;
                            int maxScroll = this.getMaxScroll();

                            if (maxScroll < 1) {
                                maxScroll = 1;
                            }

                            int l1 = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) /
                                            (float) this.getContentHeight());
                            l1 = MathHelper.clamp(l1, 32, this.bottom - this.top - 8);
                            this.scrollMultiplier /= (float) (this.bottom - this.top - l1) / (float) maxScroll;
                        } else {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (flag1) {
                            this.initialClickY = this.mouseY;
                        } else {
                            this.initialClickY = -2;
                        }
                    } else {
                        this.initialClickY = -2;
                    }
                } else if (this.initialClickY >= 0) {
                    this.amountScrolled -= (float) (this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            } else {
                this.initialClickY = -1;
            }

            int i2 = Mouse.getEventDWheel();

            if (i2 != 0) {
                if (i2 > 0) {
                    i2 = -1;
                } else if (i2 < 0) {
                    i2 = 1;
                }

                this.amountScrolled += (float) (i2 * this.slotHeight / 2);
            }
        }
    }

    //Draw in center if it fits
    @Override
    protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks)
    {
        int size = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        //Find y to start with
        int y = this.headerPadding + insideTop;
        int contentHeight = getContentHeight();
        int insideHeight = this.bottom - this.top - 4;

        if (contentHeight < insideHeight) {
            //it fits, so we can center it vertically
            y += (insideHeight - contentHeight) / 2;
        }

        //Draw all entries
        for (int i = 0; i < size; ++i)
        {
            int entryHeight = listEntries.get(i).getHeight();
            int entryHeight2 = entryHeight - 4;

            if (y > this.bottom || y + entryHeight2 < this.top)
            {
                this.updateItemPos(i, insideLeft, y, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(i))
            {
                int i1 = this.left + this.width / 2 - this.getListWidth() / 2;
                int j1 = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i1, (double)(y + entryHeight2 + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(y + entryHeight2 + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(y - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i1, (double)(y - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(y + entryHeight2 + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(y + entryHeight2 + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(y - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(y - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(i, insideLeft, y, entryHeight2, mouseXIn, mouseYIn, partialTicks);
            y += entryHeight;
        }
    }

    private int getRelativeMouseX(int mouseX) {
        int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
        return mouseX - j;
    }

    private int getRelativeMouseY(int mouseY, int contentIndex) {
        int k = this.top + 4 - this.getAmountScrolled() + getContentHeight(contentIndex) + this.headerPadding;
        int relativeMouseY = mouseY - k;

        //Content might be centered, adjust relative mouse y accordingly
        int contentHeight = getContentHeight();
        int insideHeight = this.bottom - this.top - 4;

        if (contentHeight < insideHeight) {
            //it fits, so we can center it vertically
            relativeMouseY -= (insideHeight - contentHeight) / 2;
        }
        return relativeMouseY;
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

        int getHeight();
    }
}
