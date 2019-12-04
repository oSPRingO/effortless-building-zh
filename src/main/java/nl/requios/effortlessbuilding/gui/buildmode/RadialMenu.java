package nl.requios.effortlessbuilding.gui.buildmode;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TextFormatting;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.buildmode.ModeSettingsManager;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import static nl.requios.effortlessbuilding.buildmode.BuildModes.*;
import static nl.requios.effortlessbuilding.buildmode.ModeOptions.*;

/**
 * From Chisels and Bits by AlgorithmX2
 * https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/client/gui/ChiselsAndBitsMenu.java
 */
public class RadialMenu extends Screen {

    private final float TIME_SCALE = 0.01f;
    public static final RadialMenu instance = new RadialMenu();

    private float visibility = 0.0f;
    private Stopwatch lastChange = Stopwatch.createStarted();
    public BuildModeEnum switchTo = null;
    public ActionEnum doAction = null;
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

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }

    public boolean isVisible() {
        return visibility > 0.001;
    }

    public void configure(final int scaledWidth, final int scaledHeight ) {
        Minecraft mc = Minecraft.getInstance();
        font = mc.fontRenderer;
        width = scaledWidth;
        height = scaledHeight;
    }

    private static class MenuButton {

        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        public final ActionEnum action;
        public String name;
        public Direction textSide;

        public MenuButton(final String name, final ActionEnum action, final double x, final double y,
                final Direction textSide) {
            this.name = I18n.format(name);
            this.action = action;
            x1 = x - 10;
            x2 = x + 10;
            y1 = y - 10;
            y2 = y + 10;
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
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (!isVisible()) return;

        BuildModeEnum currentBuildMode = ModeSettingsManager.getModeSettings(Minecraft.getInstance().player).getBuildMode();

        GlStateManager.pushMatrix();
        GlStateManager.translatef( 0.0F, 0.0F, 200.0F );

        final int startColor = (int) ( visibility * 98 ) << 24;
        final int endColor = (int) ( visibility * 128 ) << 24;

        drawGradientRect(0, 0, width, height, startColor, endColor);

        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        final double middleX = width / 2.0;
        final double middleY = height / 2.0;

        final double mouseXCenter = mouseX - middleX;
        final double mouseYCenter = -mouseY + middleY;
        double mouseRadians = Math.atan2(mouseYCenter, mouseXCenter);

        final double ringInnerEdge = 30;
        final double ringOuterEdge = 65;
        final double textDistance = 75;
        final double buttonDistance = 105;
        final double quarterCircle = Math.PI / 2.0;

        if ( mouseRadians < -quarterCircle ) {
            mouseRadians = mouseRadians + Math.PI * 2;
        }

        final ArrayList<MenuRegion> modes = new ArrayList<MenuRegion>();
        final ArrayList<MenuButton> buttons = new ArrayList<MenuButton>();

        //Add build modes
        for (final BuildModeEnum mode : BuildModeEnum.values()) {
            modes.add(new MenuRegion(mode));
        }

        //Add actions
        buttons.add(new MenuButton(ActionEnum.UNDO.name, ActionEnum.UNDO, -buttonDistance - 26, -13, Direction.UP));
        buttons.add(new MenuButton(ActionEnum.REDO.name, ActionEnum.REDO, -buttonDistance, -13, Direction.UP));
        buttons.add(new MenuButton(ActionEnum.OPEN_MODIFIER_SETTINGS.name, ActionEnum.OPEN_MODIFIER_SETTINGS, -buttonDistance - 26, 13, Direction.DOWN));
        buttons.add(new MenuButton(ActionEnum.REPLACE.name, ActionEnum.REPLACE, -buttonDistance, 13, Direction.DOWN));

        //Add buildmode dependent options
        OptionEnum[] options = currentBuildMode.options;
        for (int i = 0; i < options.length; i++) {
            for (int j = 0; j < options[i].actions.length; j++) {
                ActionEnum action = options[i].actions[j];
                buttons.add(new MenuButton(action.name, action, buttonDistance + j * 26, -13 + i * 39, Direction.DOWN));
            }
        }

        switchTo = null;
        doAction = null;

        //Draw buildmode backgrounds
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
                int buildMode = currentBuildMode.ordinal();
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
                    r = 0.6f;
                    g = 0.8f;
                    b = 1f;
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

        //Draw action backgrounds
        for (final MenuButton btn : buttons) {
            float r = 0.5f;
            float g = 0.5f;
            float b = 0.5f;
            float a = 0.5f;

            //highlight when active option
            if (btn.action == getBuildSpeed() ||
                btn.action == getFill() ||
                btn.action == getCubeFill() ||
                btn.action == getRaisedEdge() ||
                btn.action == getLineThickness() ||
                btn.action == getCircleStart()) {
                r = 0.0f;
                g = 0.5f;
                b = 1f;
                a = 0.6f;
            }

            //highlight when mouse over
            if (btn.x1 <= mouseXCenter && btn.x2 >= mouseXCenter && btn.y1 <= mouseYCenter && btn.y2 >= mouseYCenter) {
                r = 0.6f;
                g = 0.8f;
                b = 1f;
                a = 0.6f;
                btn.highlighted = true;
                doAction = btn.action;
            }

            buffer.pos(middleX + btn.x1, middleY + btn.y1, zLevel).color(r, g, b, a).endVertex();
            buffer.pos(middleX + btn.x1, middleY + btn.y2, zLevel).color(r, g, b, a).endVertex();
            buffer.pos(middleX + btn.x2, middleY + btn.y2, zLevel).color(r, g, b, a).endVertex();
            buffer.pos(middleX + btn.x2, middleY + btn.y1, zLevel).color(r, g, b, a).endVertex();
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.translatef(0f, 0f, 5f);
        GlStateManager.enableTexture2D();
        GlStateManager.color3f(1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.bindTexture(Minecraft.getInstance().getTextureMap().getGlTextureId());

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        //Draw buildmode icons
        for (final MenuRegion menuRegion : modes) {

            final double x = (menuRegion.x1 + menuRegion.x2) * 0.5 * (ringOuterEdge * 0.6 + 0.4 * ringInnerEdge);
            final double y = (menuRegion.y1 + menuRegion.y2) * 0.5 * (ringOuterEdge * 0.6 + 0.4 * ringInnerEdge);

            final TextureAtlasSprite sprite = ClientProxy.getBuildModeIcon(menuRegion.mode);

            final double x1 = x - 8;
            final double x2 = x + 8;
            final double y1 = y - 8;
            final double y2 = y + 8;

            final float f = 1f;
            final float a = 1f;

            final double u1 = 0;
            final double u2 = 16;
            final double v1 = 0;
            final double v2 = 16;

            buffer.pos(middleX + x1, middleY + y1, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x1, middleY + y2, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x2, middleY + y2, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + x2, middleY + y1, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
        }

        //Draw action icons
        for (final MenuButton button : buttons) {

            final float f = 1f;
            final float a = 1f;

            final double u1 = 0;
            final double u2 = 16;
            final double v1 = 0;
            final double v2 = 16;

            final TextureAtlasSprite sprite = ClientProxy.getModeOptionIcon(button.action);

            final double btnmiddleX = (button.x1 + button.x2) / 2 + 0.01;
            final double btnmiddleY = (button.y1 + button.y2) / 2 + 0.01;
            final double btnx1 = btnmiddleX - 8;
            final double btnx2 = btnmiddleX + 8;
            final double btny1 = btnmiddleY - 8;
            final double btny2 = btnmiddleY + 8;

            buffer.pos(middleX + btnx1, middleY + btny1, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btnx1, middleY + btny2, zLevel).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btnx2, middleY + btny2, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middleX + btnx2, middleY + btny1, zLevel).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
        }

        tessellator.draw();

        //Draw strings
        //font.drawStringWithShadow("Actions", (int) (middleX - buttonDistance - 13) - font.getStringWidth("Actions") * 0.5f, (int) middleY - 38, 0xffffffff);

        //Draw option strings
        for (int i = 0; i < currentBuildMode.options.length; i++) {
            OptionEnum option = options[i];
            font.drawStringWithShadow(I18n.format(option.name), (int) (middleX + buttonDistance - 9), (int) middleY - 37 + i * 39, 0xeeeeeeff);
        }

        String credits = "Effortless Building";
        font.drawStringWithShadow(credits, width - font.getStringWidth(credits) - 4, height - 10, 0x88888888);

        //Draw buildmode text
        for (final MenuRegion menuRegion : modes) {

            if (menuRegion.highlighted) {
                final double x = (menuRegion.x1 + menuRegion.x2) * 0.5;
                final double y = (menuRegion.y1 + menuRegion.y2) * 0.5;

                int fixed_x = (int) (x * textDistance);
                final int fixed_y = (int) (y * textDistance) - font.FONT_HEIGHT / 2;
                final String text = I18n.format(menuRegion.mode.name);

                if ( x <= -0.2 ) {
                    fixed_x -= font.getStringWidth(text);
                } else if ( -0.2 <= x && x <= 0.2 ) {
                    fixed_x -= font.getStringWidth(text) / 2;
                }

                font.drawStringWithShadow(text, (int) middleX + fixed_x, (int) middleY + fixed_y, 0xffffffff);
            }
        }

        //Draw action text
        for (final MenuButton button : buttons) {
            if (button.highlighted) {
                String text = TextFormatting.AQUA + button.name;
                int wrap = 120;
                String keybind = "";
                String keybindFormatted = "";

                //Add keybind in brackets
                if (button.action == ActionEnum.UNDO) {
                    keybind = ClientProxy.keyBindings[4].getKey().getName();
                }
                if (button.action == ActionEnum.REDO) {
                    keybind = ClientProxy.keyBindings[5].getKey().getName();
                }
                if (button.action == ActionEnum.REPLACE) {
                    keybind = ClientProxy.keyBindings[1].getKey().getName();
                }
                if (button.action == ActionEnum.OPEN_MODIFIER_SETTINGS) {
                    keybind = ClientProxy.keyBindings[0].getKey().getName();
                }
                if (currentBuildMode.options.length > 0) {
                    //Add (ctrl) to first two actions of first option
                    if (button.action == currentBuildMode.options[0].actions[0]
                        || button.action == currentBuildMode.options[0].actions[1]) {
                        keybind = ClientProxy.keyBindings[6].getKey().getName();
                        if (keybind.equals("Left Control")) keybind = "Ctrl";
                    }
                }
                if (!keybind.isEmpty()) keybindFormatted = TextFormatting.GRAY + "(" + WordUtils.capitalizeFully(keybind) + ")";

                if (button.textSide == Direction.WEST) {

                    font.drawSplitString( text, (int) (middleX + button.x1 - 8 ) - font.getStringWidth(text),
                            (int) (middleY + button.y1 + 6), wrap, 0xffffffff);

                } else if (button.textSide == Direction.EAST) {

                    font.drawSplitString(text, (int) (middleX + button.x2 + 8),
                            (int) (middleY + button.y1 + 6 ), wrap, 0xffffffff);

                } else if (button.textSide == Direction.UP || button.textSide == Direction.NORTH) {

                    font.drawSplitString( keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.getStringWidth(keybindFormatted) * 0.5),
                            (int) (middleY + button.y1 - 26), wrap,0xffffffff);

                    font.drawSplitString( text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.getStringWidth(text) * 0.5),
                            (int) (middleY + button.y1 - 14), wrap,0xffffffff);

                } else if (button.textSide == Direction.DOWN || button.textSide == Direction.SOUTH) {

                    font.drawSplitString(text, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.getStringWidth(text) * 0.5),
                            (int) (middleY + button.y1 + 26), wrap, 0xffffffff);

                    font.drawSplitString(keybindFormatted, (int) (middleX + (button.x1 + button.x2) * 0.5 - font.getStringWidth(keybindFormatted) * 0.5),
                            (int) (middleY + button.y1 + 38), wrap, 0xffffffff);

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
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        EffortlessBuilding.log("mouse clicked");

//        KeyBinding.updateKeyBindState();
//        KeyBinding.setKeyBindState(ClientProxy.keyBindings[3].getKeyCode(), true);

//        if (mouseButton == 0) {
//            this.mc.displayGuiScreen(null);
//
//            if (this.mc.currentScreen == null) {
//                this.mc.setIngameFocus();
//            }
//        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}

