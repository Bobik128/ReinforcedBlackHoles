#version 150

in vec4 Position; // full-screen quad positions
uniform mat4 ProjMat;
uniform mat4 InverseProjection;
uniform vec2 OutSize;

out vec2 texCoord;
out vec3 rayDir; // normalized ray direction in view space

void main() {
    // Standard fullscreen quad position
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.0, 1.0);

    // Texture coordinate
    texCoord = Position.xy / OutSize;

    // Precompute normalized ray direction in view space
//    vec2 ndc = texCoord * 2.0 - 1.0;            // -1..1 range
//    vec4 clip = vec4(ndc, -1.0, 1.0);
//    vec4 viewPos4 = InverseProjection * clip;
//    viewPos4 /= viewPos4.w;
//    rayDir = normalize(viewPos4.xyz);
    rayDir = vec3(0.0);
}