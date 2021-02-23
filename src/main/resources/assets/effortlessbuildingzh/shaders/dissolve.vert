#version 120

varying vec4 position;
varying vec3 normal;

void main() {
    gl_Position = ftransform();//gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
    gl_TexCoord[0]  = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
    //gl_BackColor = gl_Color;

    position = gl_Vertex;
    normal = gl_Normal;
}