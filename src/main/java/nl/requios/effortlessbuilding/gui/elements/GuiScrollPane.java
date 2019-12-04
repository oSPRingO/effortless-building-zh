package nl.requios.effortlessbuilding.gui.elements;

import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.glfw.GLFW;
import sun.security.ssl.Debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiScrollPane extends GuiSlot {

    public GuiScreen parent;
    public FontRenderer fontRenderer;
    private List<IScrollEntry> listEntries;
    private float scrollMultiplier = 1f;

    private int mouseX;
    private int mouseY;

    public GuiScrollPane(GuiScreen parent, FontRenderer fontRenderer, int top, int bottom) {
        super(parent.mc, parent.width, parent.height, top, bottom, 100);
        this.parent = parent;
        this.fontRenderer = fontRenderer;
        this.setShowSelectionBox(false);
        listEntries = new ArrayList<>();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public IScrollEntry getListEntry(int index) {
        return listEntries.get(index);
    }

    public void AddListEntry(IScrollEntry listEntry){
        listEntries.add(listEntry);
    }

    @Override
    protected int getSize() {
        return listEntries.size();
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
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
            GlStateManager.disableDepthTest();

            //Dirt overlays on top and bottom
//            this.overlayBackground(0, this.top, 255, 255);
//            this.overlayBackground(this.bottom, this.height, 255, 255);

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlphaTest();
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
            GlStateManager.enableAlphaTest();
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

    @Override
    protected void drawBackground() {

    }

    @Override
    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        this.getListEntry(slotIndex).drawEntry(slotIndex, xPos, yPos, this.getListWidth(), heightIn, mouseXIn, mouseYIn,
                this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == slotIndex, partialTicks);
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

    public int getSlotIndexFromScreenCoords(double posX, double posY) {
        int left = this.left + (this.width - this.getListWidth()) / 2;
        int right = this.left + (this.width + this.getListWidth()) / 2;
        double relativeMouseY = getRelativeMouseY(mouseY, 0);

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
    protected boolean mouseClicked(int index, int button, double mouseX, double mouseY) {
        int selectedSlot = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
        double relativeX = getRelativeMouseX(mouseX);

        //Always pass through mouseclicked, to be able to unfocus textfields
        for (int i = 0; i < this.listEntries.size(); i++) {
            double relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mousePressed(selectedSlot, (int) mouseX, (int) mouseY, button, (int) relativeX, (int) relativeY);
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
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        for (int i = 0; i < this.getSize(); ++i)
        {
            double relativeX = getRelativeMouseX(mouseX);
            double relativeY = getRelativeMouseY(mouseY, i);
            this.getListEntry(i).mouseReleased(i, (int) p_mouseReleased_1_, (int) p_mouseReleased_3_, p_mouseReleased_5_, (int) relativeX, (int) relativeY);
        }

        this.visible = true;
        return false;
    }

    public void handleMouseInput() {
        if (this.isMouseInList(this.mouseX, this.mouseY)) {
            if (mc.mouseHelper.isLeftDown() && this.mouseY >= this.top &&
                this.mouseY <= this.bottom) {
                int i = this.left + (this.width - this.getListWidth()) / 2;
                int j = this.left + (this.width + this.getListWidth()) / 2;
                int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                double relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                if (slotIndex > -1) {
                    this.mouseClicked(slotIndex, 0, this.mouseX, this.mouseY);
                    this.selectedElement = slotIndex;
                } else if (this.mouseX >= i && this.mouseX <= j && relativeMouseY < 0) {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int) this.amountScrolled - 4);
                }
            }

            if (mc.mouseHelper.isLeftDown() && this.isVisible()) {
                if (this.initialClickY == -1) {
                    boolean flag1 = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                        int i2 = this.left + (this.width - this.getListWidth()) / 2;
                        int j2 = this.left + (this.width + this.getListWidth()) / 2;
                        int slotIndex = getSlotIndexFromScreenCoords(this.mouseX, this.mouseY);
                        double relativeMouseY = getRelativeMouseY(mouseY, slotIndex);

                        if (slotIndex > -1) {
                            //TODO 1.13 use flag
                            boolean flag = slotIndex == this.selectedElement &&
                                           Util.milliTime() - this.lastClicked < 250L;
                            this.mouseClicked(slotIndex, this.mouseX, this.mouseY);
                            this.selectedElement = slotIndex;
                            this.lastClicked = Util.milliTime();
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

        }
    }

    //Through forge event instead of through the parent, because the parent no longer has scroll functionality in 1.13
    @SubscribeEvent
    public void mouseScrolled(GuiScreenEvent.MouseScrollEvent.Pre event) {
        double scrollDelta = event.getScrollDelta();
        if (scrollDelta != 0) {
            if (scrollDelta > 0) {
                scrollDelta = -1;
            } else if (scrollDelta < 0) {
                scrollDelta = 1;
            }

            this.amountScrolled += (float) (scrollDelta * this.slotHeight / 2);
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
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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

    private double getRelativeMouseX(double mouseX) {
        int j = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
        return mouseX - j;
    }

    private double getRelativeMouseY(double mouseY, int contentIndex) {
        int k = this.top + 4 - this.getAmountScrolled() + getContentHeight(contentIndex) + this.headerPadding;
        double relativeMouseY = mouseY - k;

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

    @Override
    public boolean charTyped(char eventChar, int eventKey) {
        for (IScrollEntry entry : this.listEntries)
            entry.charTyped(eventChar, eventKey);
        return false;
    }

    public void onGuiClosed() {
        for (IScrollEntry entry : this.listEntries)
            entry.onGuiClosed();
    }

    public interface IScrollEntry {
        int initGui(int id, List<GuiButton> buttonList);

        void updateScreen();

        void drawTooltip(GuiScreen guiScreen, int mouseX, int mouseY);

        boolean charTyped(char eventChar, int eventKey);

        void onGuiClosed();

        int getHeight();

        void updatePosition(int slotIndex, int x, int y, float partialTicks);

        void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks);

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY);

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY);
    }
}
