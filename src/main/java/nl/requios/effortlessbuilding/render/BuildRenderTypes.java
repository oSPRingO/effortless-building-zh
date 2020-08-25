package nl.requios.effortlessbuilding.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.*;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class BuildRenderTypes {
    public static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY;
    public static final RenderState.TransparencyState NO_TRANSPARENCY;

    public static final RenderState.DiffuseLightingState DIFFUSE_LIGHTING_ENABLED;
    public static final RenderState.DiffuseLightingState DIFFUSE_LIGHTING_DISABLED;

    public static final RenderState.LayerState PROJECTION_LAYERING;

    public static final RenderState.CullState CULL_DISABLED;

    public static final RenderState.AlphaState DEFAULT_ALPHA;

    public static final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR;
    public static final RenderState.WriteMaskState COLOR_WRITE;

    public static final RenderType LINES;
    public static final RenderType PLANES;

    private static final int primaryTextureUnit = 0;
    private static final int secondaryTextureUnit = 2;

    static {
        TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
        NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228510_b_");

        DIFFUSE_LIGHTING_ENABLED = new RenderState.DiffuseLightingState(true);
        DIFFUSE_LIGHTING_DISABLED = new RenderState.DiffuseLightingState(false);

        PROJECTION_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228500_J_");

        CULL_DISABLED = new RenderState.CullState(false);

        DEFAULT_ALPHA = new RenderState.AlphaState(0.003921569F);

        final boolean ENABLE_DEPTH_WRITING = true;
        final boolean ENABLE_COLOUR_COMPONENTS_WRITING = true;
        WRITE_TO_DEPTH_AND_COLOR = new RenderState.WriteMaskState(ENABLE_COLOUR_COMPONENTS_WRITING, ENABLE_DEPTH_WRITING);
        COLOR_WRITE = new RenderState.WriteMaskState(true, false);

        final int INITIAL_BUFFER_SIZE = 128;
        RenderType.State renderState;

        //LINES
//        RenderSystem.pushLightingAttributes();
//        RenderSystem.pushTextureAttributes();
//        RenderSystem.disableCull();
//        RenderSystem.disableLighting();
//        RenderSystem.disableTexture();
//
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//        RenderSystem.lineWidth(2);
        renderState = RenderType.State.getBuilder()
                .line(new RenderState.LineState(OptionalDouble.of(2)))
                .layer(PROJECTION_LAYERING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .writeMask(WRITE_TO_DEPTH_AND_COLOR)
                .cull(CULL_DISABLED)
                .build(false);
        LINES = RenderType.makeType("eb_lines",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, INITIAL_BUFFER_SIZE, renderState);

        renderState = RenderType.State.getBuilder()
                .line(new RenderState.LineState(OptionalDouble.of(2)))
                .layer(PROJECTION_LAYERING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .writeMask(COLOR_WRITE)
                .cull(CULL_DISABLED)
                .build(false);
        PLANES = RenderType.makeType("eb_planes",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLE_STRIP, INITIAL_BUFFER_SIZE, renderState);

    }

    public static RenderType getBlockPreviewRenderType(float dissolve, BlockPos blockPos, BlockPos firstPos,
                                                       BlockPos secondPos, boolean red) {
//        RenderSystem.pushLightingAttributes();
//        RenderSystem.pushTextureAttributes();
//        RenderSystem.enableCull();
//        RenderSystem.enableTexture();
//        Minecraft.getInstance().textureManager.bindTexture(ShaderHandler.shaderMaskTextureLocation);
//
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        RenderSystem.blendColor(1f, 1f, 1f, 0.8f);
        //end
//        ShaderHandler.releaseShader();

        //highjacking texturing state (which does nothing by default) to do my own things
        RenderState.TexturingState MY_TEXTURING = new RenderState.TexturingState("eb_texturing", () -> {
//            RenderSystem.pushLightingAttributes();
//            RenderSystem.pushTextureAttributes();
            ShaderHandler.useShader(ShaderHandler.dissolve, generateShaderCallback(dissolve, new Vec3d(blockPos), new Vec3d(firstPos), new Vec3d(secondPos), blockPos == secondPos, red));

            RenderSystem.blendColor(1f, 1f, 1f, 0.8f);
        }, () -> {
            ShaderHandler.releaseShader();
        });

        RenderType.State renderState = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(ShaderHandler.shaderMaskTextureLocation, false, false))
                .texturing(MY_TEXTURING)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .diffuseLighting(DIFFUSE_LIGHTING_DISABLED)
                .alpha(DEFAULT_ALPHA)
                .cull(new RenderState.CullState(true))
                .lightmap(new RenderState.LightmapState(false))
                .overlay(new RenderState.OverlayState(false))
                .build(true);
        return RenderType.makeType("eb_block_previews",
                DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256, true, true, renderState);
    }


    private static Consumer<Integer> generateShaderCallback(final float dissolve, final Vec3d blockpos,
                                                            final Vec3d firstpos, final Vec3d secondpos,
                                                            final boolean highlight, final boolean red) {
        Minecraft mc = Minecraft.getInstance();
        return (Integer shader) -> {
            int percentileUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "dissolve");
            int highlightUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "highlight");
            int redUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "red");
            int blockposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "blockpos");
            int firstposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "firstpos");
            int secondposUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "secondpos");
            int imageUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "image");
            int maskUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "mask");

            RenderSystem.enableTexture();
            GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            //mask
            ARBShaderObjects.glUniform1iARB(maskUniform, secondaryTextureUnit);
            glActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + secondaryTextureUnit);
            mc.getTextureManager().getTexture(ShaderHandler.shaderMaskTextureLocation).bindTexture();
            //GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getTextureManager().getTexture(ShaderHandler.shaderMaskTextureLocation).getGlTextureId());

            //image
            ARBShaderObjects.glUniform1iARB(imageUniform, primaryTextureUnit);
            glActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + primaryTextureUnit);
            mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).bindTexture();
            //GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getGlTextureId());

            //blockpos
            ARBShaderObjects.glUniform3fARB(blockposUniform, (float) blockpos.x, (float) blockpos.y, (float) blockpos.z);
            ARBShaderObjects.glUniform3fARB(firstposUniform, (float) firstpos.x, (float) firstpos.y, (float) firstpos.z);
            ARBShaderObjects.glUniform3fARB(secondposUniform, (float) secondpos.x, (float) secondpos.y, (float) secondpos.z);

            //dissolve
            ARBShaderObjects.glUniform1fARB(percentileUniform, dissolve);
            //highlight
            ARBShaderObjects.glUniform1iARB(highlightUniform, highlight ? 1 : 0);
            //red
            ARBShaderObjects.glUniform1iARB(redUniform, red ? 1 : 0);
        };
    }

    public static void glActiveTexture(int texture) {
        if (GL.getCapabilities().GL_ARB_multitexture && !GL.getCapabilities().OpenGL13) {
            ARBMultitexture.glActiveTextureARB(texture);
        } else {
            GL13.glActiveTexture(texture);
        }

    }
}
