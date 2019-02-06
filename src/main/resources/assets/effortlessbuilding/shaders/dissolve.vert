#version 120

uniform int time; // Passed in, see ShaderHelper.java

uniform float percentile; // Passed in via Callback
uniform int highlight;
uniform vec3 blockpos;
uniform vec3 firstpos;
uniform vec3 secondpos;
uniform sampler2D image;
uniform sampler2D mask;

varying vec4 position;

void main() {
    gl_Position = ftransform();//gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
    gl_TexCoord[0]  = gl_TextureMatrix[0] * gl_MultiTexCoord0 + 0.01;

    position = gl_Vertex;
}