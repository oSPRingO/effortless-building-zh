#version 120

uniform mat4 gl_ProjectionMatrix;
uniform mat4 gl_ModelViewMatrix;

//uniform mat4 inverse_view_proj;
uniform float screen_width;
uniform float screen_height;

uniform int time; // Passed in, see ShaderHelper.java

uniform float percentile; // Passed in via Callback
uniform int highlight;
uniform vec3 blockpos;
uniform vec3 firstpos;
uniform vec3 secondpos;
uniform sampler2D image;
uniform sampler2D mask;

varying vec4 position;

//	Simplex 3D Noise
//	by Ian McEwan, Ashima Arts
//
vec4 permute(vec4 x) {return mod(((x*34.0)+1.0)*x, 289.0);}
vec4 taylorInvSqrt(vec4 r) {return 1.79284291400159 - 0.85373472095314 * r;}

float snoise(vec3 v){
    const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
    vec3 i  = floor(v + dot(v, C.yyy) );
    vec3 x0 =   v - i + dot(i, C.xxx) ;

    // Other corners
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min( g.xyz, l.zxy );
    vec3 i2 = max( g.xyz, l.zxy );

    //  x0 = x0 - 0. + 0.0 * C
    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    // Permutations
    i = mod(i, 289.0 );
    vec4 p = permute( permute( permute(
             i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
           + i.y + vec4(0.0, i1.y, i2.y, 1.0 ))
           + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));

    // Gradients
    // ( N*N points uniformly over a square, mapped onto an octahedron.)
    float n_ = 1.0/7.0; // N=7
    vec3  ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z *ns.z);  //  mod(p,N*N)

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_ );    // mod(j,N)

    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4( x.xy, y.xy );
    vec4 b1 = vec4( x.zw, y.zw );

    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;

    vec3 p0 = vec3(a0.xy,h.x);
    vec3 p1 = vec3(a0.zw,h.y);
    vec3 p2 = vec3(a1.xy,h.z);
    vec3 p3 = vec3(a1.zw,h.w);

    //Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
    m = m * m;
    return 42.0 * dot( m*m, vec4( dot(p0,x0), dot(p1,x1),
                                dot(p2,x2), dot(p3,x3) ) );
}

vec3 getWorldPosition() {
    // Convert screen coordinates to normalized device coordinates (NDC)
    vec4 ndc = vec4(
        (gl_FragCoord.x / screen_width - 0.5) * 2.0,
        (gl_FragCoord.y / screen_height - 0.5) * 2.0,
        (gl_FragCoord.z - 0.5) * 2.0,
        1.0);

    // Convert NDC throuch inverse clip coordinates to view coordinates
    vec4 clip = transpose(gl_ModelViewMatrix) * ndc;
    vec3 vertex = (clip / clip.w).xyz;
    return vertex;
}

void main() {

    vec3 worldpos = getWorldPosition();

    float noise = worldpos.x / 300.0;//snoise(worldpos);

    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 color = texture2D(image, texcoord);

    vec3 relBlockPos = mod(blockpos, 32.0) / 32.0;
    vec2 maskcoord = texcoord + vec2(relBlockPos.x + relBlockPos.y, relBlockPos.z + relBlockPos.y);
    vec4 maskColor = texture2D(mask, maskcoord);
    float maskgs = maskColor.r;
    maskgs = noise;

    float r = color.r * gl_Color.r;
    float g = color.g * gl_Color.g;
    float b = color.b * gl_Color.b;
    float a = color.a; // Ignore gl_Color.a as we don't want to make use of that for the dissolve effect

    r = noise;
    g = noise;
    b = noise;

//    r -= 0.1;
//    g += 0.0;
//    b += 0.1;
//
//    float pulse = (sin(time / 5.0) + 1.0) / 2.0;
//    pulse = 1.0;//pulse / 2.0 + 0.5;
//    vec4 pulseColor = texture2D(mask, maskcoord + time / 700.0);
//    vec4 pulseColor2 = texture2D(mask, vec2(maskcoord.x + time / 600.0, maskcoord.y - time / 600.0));
//    float pulseGreyScale = pulseColor.r + pulseColor2.r / 2.0;
//
//    r -= r * pulseColor.r * pulse * 0.2;
//    g += (1.0 - g) * pulseColor.r * pulse * 0.2;
//    b += (1.0 - b) * pulseColor.r * pulse * 0.8;
//
//    r -= r * pulseColor2.r * pulse * 0.4;
//    g += (1.0 - g) * pulseColor2.r * pulse * 0.2;
//    b += (1.0 - b) * pulseColor2.r * pulse;
        
    if(highlight == 1) {
        r += 0.0;
        g += 0.1;
        b -= 0.2;
    }

    r = max(0, min(1, r));
    g = max(0, min(1, g));
    b = max(0, min(1, b));

    if(maskgs <= percentile)
    	gl_FragColor = vec4(r, g, b, a);
    else gl_FragColor = vec4(r, g, b, 0);
}