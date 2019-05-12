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
package nl.requios.effortlessbuilding.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import nl.requios.effortlessbuilding.BuildConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class ShaderHandler {


    private static final int VERT_ST = GL20.GL_VERTEX_SHADER;
    private static final int FRAG_ST = GL20.GL_FRAGMENT_SHADER;

    private static final int VERT = 1;
    private static final int FRAG = 2;

    private static final String VERT_EXTENSION = ".vert";
    private static final String FRAG_EXTENSION = ".frag";

    public static int rawColor;
    public static int dissolve;

    public static ResourceLocation shaderMaskTextureLocation = new ResourceLocation(EffortlessBuilding.MODID, "textures/shader_mask.png");

    public static void init() {
        if(!doUseShaders())
            return;

        rawColor = createProgram("/assets/effortlessbuilding/shaders/raw_color", FRAG);
        dissolve = createProgram("/assets/effortlessbuilding/shaders/dissolve", VERT + FRAG);
    }

    public static void useShader(int shader, Consumer<Integer> callback) {
        if(!doUseShaders())
            return;

        GL20.glUseProgram(shader);

        if(shader != 0) {
            int time = GL20.glGetUniformLocation(shader, "time");
            GL20.glUniform1i(time, ClientProxy.ticksInGame);

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
        return BuildConfig.visuals.useShaders && OpenGlHelper.shadersSupported && OpenGlHelper.openGL21; //Only GL2.1 shaders
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

        program = GL20.glCreateProgram();
        if(program == 0)
            return 0;

        if(vert != null)
            GL20.glAttachShader(program, vertId);
        if(frag != null)
            GL20.glAttachShader(program, fragId);

        GL20.glLinkProgram(program);
        if(GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            EffortlessBuilding.logger.log(Level.ERROR, getProgramLogInfo(program));
            return 0;
        }

        GL20.glValidateProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            EffortlessBuilding.logger.log(Level.ERROR, getProgramLogInfo(program));
            return 0;
        }

        return program;
    }

    private static int createShader(String filename, int shaderType){
        int shader = 0;
        try {
            shader = GL20.glCreateShader(shaderType);

            if(shader == 0)
                return 0;

            GL20.glShaderSource(shader, readFileAsString(filename));
            GL20.glCompileShader(shader);

            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader: " + getShaderLogInfo(shader));

            return shader;
        }
        catch(Exception e) {
            GL20.glDeleteShader(shader);
            e.printStackTrace();
            return -1;
        }
    }

    private static String getProgramLogInfo(int program) {
        return GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH));
    }

    private static String getShaderLogInfo(int shader) {
        return GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH));
    }

    private static String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();
        InputStream in = ShaderHandler.class.getResourceAsStream(filename);
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
