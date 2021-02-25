package nl.requios.effortlessbuildingzh.gui.buildmode;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.client.gui.widget.Slider;
import nl.requios.effortlessbuildingzh.EffortlessBuildingZh;

public class PlayerSettingsGui extends Screen {

    protected int left, right, top, bottom;

    private Button shaderTypeButton;
    private ShaderTypeList shaderTypeList;
    private Button closeButton;

    protected boolean showShaderList = false;

    public enum ShaderType {
        DISSOLVE_BLUE("Dissolve Blue"),
        DISSOLVE_ORANGE("Dissolve Orange");

        public String name;
        ShaderType(String name) {
            this.name = name;
        }
    }

    public PlayerSettingsGui() {
        super(new TranslationTextComponent("effortlessbuildingzh.screen.player_settings"));
    }

    @Override
    protected void init() {
        left = this.width / 2 - 140;
        right = this.width / 2 + 140;
        top = this.height / 2 - 100;
        bottom = this.height / 2 + 100;

        int yy = top;
        shaderTypeList = new ShaderTypeList(this.minecraft);
        this.children.add(shaderTypeList);
        //TODO set selected name
        String currentShaderName = ShaderType.DISSOLVE_BLUE.name;
        shaderTypeButton = new ExtendedButton(right - 180, yy, 180, 20, currentShaderName, (button) -> {
            showShaderList = !showShaderList;
        });
        addButton(shaderTypeButton);

        yy += 50;
        Slider slider = new Slider(right - 200, yy, 200, 20, "", "", 0.5, 2.0, 1.0, true, true, (button) -> {

        });
        addButton(slider);

        closeButton = new ExtendedButton(left + 50, bottom - 20, 180, 20, "Done", (button) -> {
            this.minecraft.player.closeScreen();
        });
        addButton(closeButton);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();

        int yy = top;
        font.drawString("Shader type", left, yy + 5, 0xFFFFFF);

        yy += 50;
        font.drawString("Shader speed", left, yy + 5, 0xFFFFFF);

        super.render(mouseX, mouseY, partialTicks);

        if (showShaderList)
            this.shaderTypeList.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (showShaderList) {
            if (!shaderTypeList.isMouseOver(mouseX, mouseY) && !shaderTypeButton.isMouseOver(mouseX, mouseY))
                showShaderList = false;
        }
        return true;
    }

    @Override
    public void removed() {
        ShaderTypeList.ShaderTypeEntry selectedShader = shaderTypeList.getSelected();
        //TODO save
    }

    //Inspired by LanguageScreen
    @OnlyIn(Dist.CLIENT)
    class ShaderTypeList extends ExtendedList<PlayerSettingsGui.ShaderTypeList.ShaderTypeEntry> {

        public ShaderTypeList(Minecraft mcIn) {
            super(mcIn, 180, 140, top + 20, top + 100, 18);
            this.setLeftPos(right - width);

            for (int i = 0; i < 40; i++) {

                for (ShaderType shaderType : ShaderType.values()) {
                    ShaderTypeEntry shaderTypeEntry = new ShaderTypeEntry(shaderType);
                    addEntry(shaderTypeEntry);
                    //TODO setSelected to this if appropriate
                }

            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        public void setSelected(PlayerSettingsGui.ShaderTypeList.ShaderTypeEntry selected) {
            super.setSelected(selected);
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            EffortlessBuildingZh.log("Selected shader " + selected.shaderType.name);
            shaderTypeButton.setMessage(selected.shaderType.name);
//            showShaderList = false;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            if (!showShaderList) return false;
            return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            if (!showShaderList) return false;
            return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        @Override
        public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
            if (!showShaderList) return false;
            return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }

        @Override
        public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
            if (!showShaderList) return false;
            return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
        }

        @Override
        public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
            if (!showShaderList) return false;
            return super.isMouseOver(p_isMouseOver_1_, p_isMouseOver_3_);
        }

        protected boolean isFocused() {
            return PlayerSettingsGui.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return right - 6;
        }

        @OnlyIn(Dist.CLIENT)
        public class ShaderTypeEntry extends ExtendedList.AbstractListEntry<ShaderTypeEntry> {
            private final ShaderType shaderType;

            public ShaderTypeEntry(ShaderType shaderType) {
                this.shaderType = shaderType;
            }

            public void render(int itemIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
                if (rowTop + 10 > ShaderTypeList.this.y0 && rowTop + rowHeight - 5 < ShaderTypeList.this.y1)
                    drawString(font, shaderType.name, ShaderTypeList.this.x0 + 8, rowTop + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                if (p_mouseClicked_5_ == 0) {
                    setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }
        }

        //From AbstractList, disabled parts
        public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
            this.renderBackground();
            int i = this.getScrollbarPosition();
            int j = i + 6;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
//            this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.pos((double)this.x1, (double)this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            bufferbuilder.pos((double)this.x0, (double)this.y0, 0.0D).color(20, 20, 20, 180).endVertex();
            tessellator.draw();
            int k = this.getRowLeft();
            int l = this.y0 + 4 - (int)this.getScrollAmount();
            if (this.renderHeader) {
                this.renderHeader(k, l, tessellator);
            }

            this.renderList(k, l, p_render_1_, p_render_2_, p_render_3_);
            RenderSystem.disableDepthTest();
//            this.renderHoleBackground(0, this.y0, 255, 255);
//            this.renderHoleBackground(this.y1, this.height, 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
//            int i1 = 4;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)(this.y0 + 4), 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y0 + 4), 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            tessellator.draw();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.pos((double)this.x1, (double)(this.y1 - 4), 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.pos((double)this.x0, (double)(this.y1 - 4), 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            tessellator.draw();

            //SCROLLBAR
            int j1 = this.getMaxScroll();
            if (j1 > 0) {
                int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
                int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
                if (l1 < this.y0) {
                    l1 = this.y0;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)j, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)i, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

//            this.renderDecorations(p_render_1_, p_render_2_);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        }

        private int getMaxScroll() {
            return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
        }
    }
}
