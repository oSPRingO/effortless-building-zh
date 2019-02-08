package nl.requios.effortlessbuilding.gui.buildmode;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;

import static nl.requios.effortlessbuilding.buildmode.BuildModes.*;

/**
 * From Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */
public class RadialMenu extends GuiScreen {

    private final float TIME_SCALE = 0.01f;
    public static final RadialMenu instance = new RadialMenu();

    private float visibility = 0.0f;
    private Stopwatch lastChange = Stopwatch.createStarted();
    public BuildModeEnum switchTo = null;
    //public ButtonAction doAction = null;
    public boolean actionUsed = false;

    private float clampVis(final float f) {
        return Math.max( 0.0f, Math.min( 1.0f, f ) );
    }

    public void raiseVisibility() {
        visibility = clampVis( visibility + lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
        lastChange = Stopwatch.createStarted();
    }

    public void decreaseVisibility() {
        visibility = clampVis( visibility - lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
        lastChange = Stopwatch.createStarted();
    }

    public boolean isVisible() {
        return visibility > 0.001;
    }

    public void configure(final int scaledWidth, final int scaledHeight ) {
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        width = scaledWidth;
        height = scaledHeight;
    }

    private static class MenuButton {

        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        //public final ButtonAction action;
        public TextureAtlasSprite icon;
        public int color;
        public String name;
        public EnumFacing textSide;

        public MenuButton(final String name, /*final ButtonAction action,*/ final double x, final double y,
                final TextureAtlasSprite ico, final EnumFacing textSide) {
            this.name = name;
            //this.action = action;
            x1 = x;
            x2 = x + 18;
            y1 = y;
            y2 = y + 18;
            icon = ico;
            color = 0xffffff;
            this.textSide = textSide;
        }

        public MenuButton(final String name, /*final ButtonAction action,*/ final double x, final double y,
                final int col, final EnumFacing textSide) {
            this.name = name;
            //this.action = action;
            x1 = x;
            x2 = x + 18;
            y1 = y;
            y2 = y + 18;
            color = col;
            this.textSide = textSide;
        }

    }

    static class MenuRegion {

        public final BuildModeEnum mode;
        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        public MenuRegion(final BuildModeEnum mode) {
            this.mode = mode;
        }

    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        //TODO chisels compat
//        final ChiselToolType tool = ClientSide.instance.getHeldToolType( EnumHand.MAIN_HAND );
//
//        if ( tool != null )
//        {
//            return;
//        }

        GlStateManager.pushMatrix();
        GlStateManager.translate( 0.0F, 0.0F, 200.0F );

        final int startColor = (int) ( visibility * 98 ) << 24;
        final int endColor = (int) ( visibility * 128 ) << 24;

        drawGradientRect(0, 0, width, height, startColor, endColor);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        final double middleX = width / 2.0;
        final double middleY = height / 2.0;

        final double mouseXCenter = mouseX - middleX;
        final double mouseYCenter = mouseY - middleY;
        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        final double ringInnerEdge = 20;
        final double ringOuterEdge = 50;
        final double textDistance = 65;
        final double quarterCircle = Math.PI / 2.0;

        if ( mouseRadians < -quarterCircle ) {
            mouseRadians = mouseRadians + Math.PI * 2;
        }

        final ArrayList<MenuRegion> modes = new ArrayList<MenuRegion>();
        final ArrayList<MenuButton> buttons = new ArrayList<MenuButton>();

//        buttons.add( new MenuButton( "mod.chiselsandbits.other.undo", ButtonAction.UNDO, textDistance, -20, ClientSide.undoIcon, EnumFacing.EAST ) );
//        buttons.add( new MenuButton( "mod.chiselsandbits.other.redo", ButtonAction.REDO, textDistance, 4, ClientSide.redoIcon, EnumFacing.EAST ) );

        for (final BuildModeEnum mode : BuildModeEnum.values()) {
            modes.add(new MenuRegion(mode));
        }

        switchTo = null;
        //doAction = null;

        if (!modes.isEmpty()) {
            final int totalModes = Math.max( 3, modes.size() );
            int currentMode = 0;
            final double fragment = Math.PI * 0.005;
            final double fragment2 = Math.PI * 0.0025;
            final double perObject = 2.0 * Math.PI / totalModes;

            for (int i = 0; i < modes.size(); i++) {
                MenuRegion menuRegion = modes.get(i);
                final double beginRadians = currentMode * perObject - quarterCircle;
                final double endRadians = (currentMode + 1) * perObject - quarterCircle;

                menuRegion.x1 = Math.cos(beginRadians);
                menuRegion.x2 = Math.cos(endRadians);
                menuRegion.y1 = Math.sin(beginRadians);
                menuRegion.y2 = Math.sin(endRadians);

                final double x1m1 = Math.cos(beginRadians + fragment) * ringInnerEdge;
                final double x2m1 = Math.cos(endRadians - fragment) * ringInnerEdge;
                final double y1m1 = Math.sin(beginRadians + fragment) * ringInnerEdge;
                final double y2m1 = Math.sin(endRadians - fragment) * ringInnerEdge;

                final double x1m2 = Math.cos(beginRadians + fragment2) * ringOuterEdge;
                final double x2m2 = Math.cos(endRadians - fragment2) * ringOuterEdge;
                final double y1m2 = Math.sin(beginRadians + fragment2) * ringOuterEdge;
                final double y2m2 = Math.sin(endRadians - fragment2) * ringOuterEdge;

                float r = 0.0f;
                float g = 0.0f;
                float b = 0.0f;
                float a = 0.5f;

                //check if current mode
                int buildMode = ModeSettingsManager.getModeSettings(Minecraft.getMinecraft().player).getBuildMode().ordinal();
                if (buildMode == i) {
                    r = 0f;
                    g = 0.5f;
                    b = 1f;
                    a = 0.5f;
                    //menuRegion.highlighted = true; //draw text
                }

                //check if mouse is over this region
                final boolean isMouseInQuad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, mouseXCenter, mouseYCenter)
                                              || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, mouseXCenter, mouseYCenter);

                if (beginRadians <= mouseRadians && mouseRadians <= endRadians && isMouseInQuad) {
                    r = 0.6f;//0.6f;
                    g = 0.8f;//0.3f;
                    b = 1f;//0.0f;
                    a = 0.6f;
                    menuRegion.highlighted = true;
                    switchTo = menuRegion.mode;
                }

                buffer.pos(middleX + x1m1, middleY + y1m1, zLevel).color(r, g, b, a).endVertex();
                buffer.pos(middleX + x2m1, middleY + y2m1, zLevel).color(r, g, b, a).endVertex();
                buffer.pos(middleX + x2m2, middleY + y2m2, zLevel).color(r, g, b, a).endVertex();
                buffer.pos(middleX + x1m2, middleY + y1m2, zLevel).color(r, g, b, a).endVertex();

                currentMode++;
            }
        }

        for (final MenuButton btn : buttons) {
            final float a = 0.5f;
            float f = 0f;

            if (btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter) {
                f = 1;
                btn.highlighted = true;
                //doAction = btn.action;
            }

            buffer.pos(middleX + btn.x1, middleY + btn.y1, zLevel).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btn.x1, middleY + btn.y2, zLevel).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btn.x2, middleY + btn.y2, zLevel).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btn.x2, middleY + btn.y1, zLevel).color(f, f, f, a).endVertex();
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.translate(0f, 0f, 5f);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (final MenuRegion mnuRgn : modes) {

            final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (ringOuterEdge * 0.6 + 0.4 * ringInnerEdge);
            final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (ringOuterEdge * 0.6 + 0.4 * ringInnerEdge);

            final TextureAtlasSprite sprite = ClientProxy.getBuildModeIcon(mnuRgn.mode);

            final double scalex = 16 * 0.5;
            final double scaley = 16 * 0.5;
            final double x1 = x - scalex;
            final double x2 = x + scalex;
            final double y1 = y - scaley;
            final double y2 = y + scaley;

            final float f = 1f;
            final float a = 1f;

            final double u1 = 0f;
            final double u2 = 16f;
            final double v1 = 0f;
            final double v2 = 16f;

            buffer.pos(middleX + x1, middleY + y1, zLevel).tex( sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x1, middleY + y2, zLevel).tex( sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x2, middleY + y2, zLevel).tex( sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x2, middleY + y1, zLevel).tex( sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
        }

        for (final MenuButton btn : buttons) {

            final float f = switchTo == null ? 1.0f : 0.5f;
            final float a = 1.0f;

            final double u1 = 0;
            final double u2 = 16;
            final double v1 = 0;
            final double v2 = 16;

            final TextureAtlasSprite sprite = btn.icon == null ? Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(ModelLoader.White.LOCATION.toString()) : btn.icon;

            final double btnx1 = btn.x1 + 1;
            final double btnx2 = btn.x2 - 1;
            final double btny1 = btn.y1 + 1;
            final double btny2 = btn.y2 - 1;

            final float red = f * ((btn.color >> 16 & 0xff) / 255.0f);
            final float green = f * ((btn.color >> 8 & 0xff) / 255.0f);
            final float blue = f * ((btn.color & 0xff) / 255.0f);

            buffer.pos(middleX + btnx1, middleY + btny1, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
            buffer.pos(middleX + btnx1, middleY + btny2, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
            buffer.pos(middleX + btnx2, middleY + btny2, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
            buffer.pos(middleX + btnx2, middleY + btny1, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
        }

        tessellator.draw();

        for (final MenuRegion mnuRgn : modes) {

            if (mnuRgn.highlighted) {
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5;
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5;

                int fixed_x = (int) (x * textDistance);
                final int fixed_y = (int) (y * textDistance);
                final String text = mnuRgn.mode.name;

                if ( x <= -0.2 ) {
                    fixed_x -= fontRenderer.getStringWidth(text);
                } else if ( -0.2 <= x && x <= 0.2 ) {
                    fixed_x -= fontRenderer.getStringWidth(text) / 2;
                }

                fontRenderer.drawStringWithShadow(text, (int) middleX + fixed_x, (int) middleY + fixed_y, 0xffffffff);
            }
        }

        for (final MenuButton btn : buttons) {
            if (btn.highlighted) {
                final String text = btn.name;

                if (btn.textSide == EnumFacing.WEST) {

                    fontRenderer.drawStringWithShadow( text, (int) ( middleX + btn.x1 - 8 ) - fontRenderer.getStringWidth( text ),
                            (int) ( middleY + btn.y1 + 6 ), 0xffffffff );

                } else if (btn.textSide == EnumFacing.EAST) {

                    fontRenderer.drawStringWithShadow( text, (int) ( middleX + btn.x2 + 8 ),
                            (int) ( middleY + btn.y1 + 6 ), 0xffffffff );

                } else if (btn.textSide == EnumFacing.UP) {

                    fontRenderer.drawStringWithShadow( text, (int) ( middleX + ( btn.x1 + btn.x2 ) * 0.5 - fontRenderer.getStringWidth( text ) * 0.5 ),
                            (int) ( middleY + btn.y1 - 14 ), 0xffffffff );

                } else if (btn.textSide == EnumFacing.DOWN) {

                    fontRenderer.drawStringWithShadow( text, (int) ( middleX + ( btn.x1 + btn.x2 ) * 0.5 - fontRenderer.getStringWidth( text ) * 0.5 ),
                            (int) ( middleY + btn.y1 + 24 ), 0xffffffff );

                }

            }
        }

        GlStateManager.popMatrix();
    }

    private boolean inTriangle(final double x1, final double y1, final double x2, final double y2,
                               final double x3, final double y3, final double x, final double y ) {
        final double ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        final double bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        final double ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return sign(ab) == sign(bc) && sign(bc) == sign(ca);
    }

    private int sign(final double n) {
        return n > 0 ? 1 : -1;
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton ) {
        if (mouseButton == 0) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
    }
}

