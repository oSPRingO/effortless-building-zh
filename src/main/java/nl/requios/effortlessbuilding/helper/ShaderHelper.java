/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Modified by Requios
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 9, 2014, 11:20:26 PM (GMT)]
 */
package nl.requios.effortlessbuilding.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class ShaderHelper {

    private static final int VERT_ST = ARBVertexShader.GL_VERTEX_SHADER_ARB;
    private static final int FRAG_ST = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;

    private static final int VERT = 1;
    private static final int FRAG = 2;

    private static final String VERT_EXTENSION = ".vert";
    private static final String FRAG_EXTENSION = ".frag";

//    public static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
//    public static final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16﻿);

    public static int rawColor;
    public static int dissolve;

    public static void init() {
        if(!doUseShaders())
            return;

        rawColor = createProgram("/assets/effortlessbuilding/shaders/raw_color", FRAG);
        dissolve = createProgram("/assets/effortlessbuilding/shaders/dissolve", FRAG);
    }

    public static void useShader(int shader, Consumer<Integer> callback) {
        if(!doUseShaders())
            return;

        ARBShaderObjects.glUseProgramObjectARB(shader);

        if(shader != 0) {
            int time = ARBShaderObjects.glGetUniformLocationARB(shader, "time");
            ARBShaderObjects.glUniform1iARB(time, ClientProxy.ticksInGame);

//            GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projection);
//            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelview);

//            int inverse_view_proj = ARBShaderObjects.glGetUniformLocationARB(shader, "inverse_view_proj");
            int screen_width = ARBShaderObjects.glGetUniformLocationARB(shader, "screen_width");
            int screen_height = ARBShaderObjects.glGetUniformLocationARB(shader, "screen_height");

//            ARBShaderObjects.glUniformMatrix4ARB(inverse_view_proj, true, modelview);
            ARBShaderObjects.glUniform1fARB(screen_width, Minecraft.getMinecraft().displayWidth);
            ARBShaderObjects.glUniform1fARB(screen_height, Minecraft.getMinecraft().displayHeight);

            if(callback != null)
                callback.accept(shader);
        }
    }

    public static void useShader(int shader) {
        useShader(shader, null);
    }

    public static void releaseShader() {
        useShader(0);
    }

    public static boolean doUseShaders() {
        return BuildConfig.visuals.useShaders && OpenGlHelper.shadersSupported;
    }

    private static int createProgram(String s, int sides) {
        boolean vert = (sides & VERT) != 0;
        boolean frag = (sides & FRAG) != 0;

        return createProgram(vert ? s + VERT_EXTENSION : null, frag ? s + FRAG_EXTENSION : null);
    }

    // Most of the code taken from the LWJGL wiki
    // http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL

    private static int createProgram(String vert, String frag) {
        int vertId = 0, fragId = 0, program;
        if(vert != null)
            vertId = createShader(vert, VERT_ST);
        if(frag != null)
            fragId = createShader(frag, FRAG_ST);

        program = ARBShaderObjects.glCreateProgramObjectARB();
        if(program == 0)
            return 0;

        if(vert != null)
            ARBShaderObjects.glAttachObjectARB(program, vertId);
        if(frag != null)
            ARBShaderObjects.glAttachObjectARB(program, fragId);

        ARBShaderObjects.glLinkProgramARB(program);
        if(ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            EffortlessBuilding.logger.log(Level.ERROR, getLogInfo(program));
            return 0;
        }

        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            EffortlessBuilding.logger.log(Level.ERROR, getLogInfo(program));
            return 0;
        }

        return program;
    }

    private static int createShader(String filename, int shaderType){
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if(shader == 0)
                return 0;

            ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

            return shader;
        }
        catch(Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            e.printStackTrace();
            return -1;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private static String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();
        InputStream in = ShaderHelper.class.getResourceAsStream(filename);
        Exception exception = null;
        BufferedReader reader;

        if(in == null)
            return "";

        try {
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            Exception innerExc= null;
            try {
                String line;
                while((line = reader.readLine()) != null)
                    source.append(line).append('\n');
            } catch(Exception exc) {
                exception = exc;
            } finally {
                try {
                    reader.close();
                } catch(Exception exc) {
                    innerExc = exc;
                }
            }

            if(innerExc != null)
                throw innerExc;
        } catch(Exception exc) {
            exception = exc;
        } finally {
            try {
                in.close();
            } catch(Exception exc) {
                if(exception == null)
                    exception = exc;
                else exc.printStackTrace();
            }

            if(exception != null)
                throw exception;
        }

        return source.toString();
    }

}
