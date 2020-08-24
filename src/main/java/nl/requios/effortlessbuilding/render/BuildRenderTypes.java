package nl.requios.effortlessbuilding.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class BuildRenderTypes {
    public static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY;
    public static final RenderState.TransparencyState NO_TRANSPARENCY;

    public static final RenderState.LayerState PROJECTION_LAYERING;

    public static final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR;
    public static final RenderState.WriteMaskState COLOR_WRITE;

    public static final RenderState.TransparencyState MY_TRANSPARENCY;

    public static final RenderType LINES;

    public static final RenderType PLANES;

    static {
        TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
        NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228510_b_");
        MY_TRANSPARENCY = new RenderState.TransparencyState("eb_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        PROJECTION_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228500_J_");

        final boolean ENABLE_DEPTH_WRITING = true;
        final boolean ENABLE_COLOUR_COMPONENTS_WRITING = true;
        WRITE_TO_DEPTH_AND_COLOR = new RenderState.WriteMaskState(ENABLE_COLOUR_COMPONENTS_WRITING, ENABLE_DEPTH_WRITING);
        COLOR_WRITE = new RenderState.WriteMaskState(true, false);

        final int INITIAL_BUFFER_SIZE = 128;
        RenderType.State renderState;
        renderState = RenderType.State.getBuilder()
                .line(new RenderState.LineState(OptionalDouble.of(2)))
                .layer(PROJECTION_LAYERING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .writeMask(WRITE_TO_DEPTH_AND_COLOR)
                .cull(new RenderState.CullState(false))
                .build(false);
        LINES = RenderType.makeType("eb_lines",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, INITIAL_BUFFER_SIZE, renderState);

        renderState = RenderType.State.getBuilder()
                .line(new RenderState.LineState(OptionalDouble.of(2)))
                .layer(PROJECTION_LAYERING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .writeMask(COLOR_WRITE)
                .cull(new RenderState.CullState(false))
                .build(false);
        PLANES = RenderType.makeType("eb_planes",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_STRIP, INITIAL_BUFFER_SIZE, renderState);
    }

}
