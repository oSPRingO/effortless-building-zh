#version 120

uniform int time; // Passed in, see ShaderHelper.java

uniform float dissolve; // Passed in via Callback
uniform int highlight;
uniform vec3 blockpos;
uniform vec3 firstpos;
uniform vec3 secondpos;
uniform sampler2D image;
uniform sampler2D mask;

varying vec4 position;
varying vec3 normal;

void main() {

    vec3 worldpos = blockpos + position.xyz;
    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 texcolor = texture2D(image, texcoord);
    vec4 color = texcolor;
    vec3 firstposc = firstpos + 0.51; //center in block
    vec3 secondposc = secondpos + 0.5;

    //find place in between first and second pos
    float firstToSecond = length(secondposc - firstposc);
    float place = 0.0;
    if (firstToSecond > 0.5) {
        float placeFromFirst = length(worldpos - firstposc) / firstToSecond;
        float placeFromSecond = length(worldpos - secondposc) / firstToSecond;
        place = (placeFromFirst + (1.0 - placeFromSecond)) / 2.0;
    } else {
        //only one block
    }

    //find 2d texture coordinate for noise texture based on world position
    vec2 maskcoord = vec2(worldpos.y, worldpos.z);
    if (abs(normal.y) > 0.5)
        maskcoord = vec2(worldpos.x, worldpos.z);
    if (abs(normal.z) > 0.5)
        maskcoord = vec2(worldpos.x, worldpos.y);

    maskcoord /= 20.0;
    vec4 maskColor = texture2D(mask, maskcoord);
    float maskgs = maskColor.r;

    color.rgb *= gl_Color.rgb;

    //desaturate
    color.rgb *= vec3(0.8);

    //add blueish hue
    color.rgb += vec3(-0.1, 0.0, 0.2);

    //add pulsing blue
    float pulse = (sin(time / 5.0) + 1.0) / 2.0;
    color.rgb += 0.4 * vec3(-0.5, 0.2, 0.6) * pulse;

    //add diagonal highlights
    float diagTime = mod(time / 40.0, 1.4) - 0.2;
    float diag = smoothstep(diagTime - 0.2, diagTime, place) - smoothstep(diagTime, diagTime + 0.2, place);
    color.rgb += 0.2 * diag * vec3(0.0, 0.2, 0.4);

    float diagTime2 = mod(time / 70.0, 1.4) - 0.2;
    float diag2 = smoothstep(diagTime2 - 0.2, diagTime2, place) - smoothstep(diagTime2, diagTime2 + 0.2, place);
    color.rgb += 0.2 * diag2 * vec3(0.0, 0.4, 0.8);

    //add edge shading
//    vec3 p1;
//    //if (firstpos.x < secondpos.x)
//
//    vec3 wmf = worldpos - firstposc;
//    vec3 wms = worldpos - (secondposc + vec3(0.0, 1.0, 1.0));
//    float distToEdge1 = min(length(wmf.xy), length(wmf.xz));
//    float distToEdge2 = min(length(wmf.yz), length(wms.xy));
//    float distToEdge3 = min(length(wms.xz), length(wms.yz));
//    float distToEdge = min(min(distToEdge1, distToEdge2), distToEdge3);
//    color.rgb += vec3(0.5 - smoothstep(0, 0.5, distToEdge)) * 0.5;

    //add flat shading
    if (abs(normal.x) > 0.5)
        color.rgb -= 0.0;
    if (abs(normal.y) > 0.5)
        color.rgb += 0.05;
    if (abs(normal.z) > 0.5)
        color.rgb -= 0.05;


    if(highlight == 1 && dissolve == 0.0) {
        color.r += 0.0;
        color.g += 0.1;
        color.b -= 0.2;
    }

    color.r = max(0, min(1, color.r));
    color.g = max(0, min(1, color.g));
    color.b = max(0, min(1, color.b));

    if (maskgs * 0.3 + place * 0.7 <= dissolve)
    	gl_FragColor = vec4(texcolor.rgb, 0.0);
    else gl_FragColor = color;
}