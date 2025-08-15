#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainSampler;

in vec2 texCoord;
in vec3 rayDir;

uniform vec3 HoleCenter;
uniform vec2 HoleScreenCenter;
uniform vec4 HoleColor;
uniform float HoleRadius;
uniform float Radius;
uniform mat4 InverseProjection;
uniform float HoleRadius2; // HoleRadius^2 (precomputed)
uniform float Radius2;     // Radius^2 (precomputed)
uniform float EffectFraction; // precomputed Radius / (tan(fov/2)*DistFromCam)

out vec4 fragColor;

vec4 raycast(vec3 ro, vec3 rd, vec3 sphereOrigin, float radius2) {
    vec3 L = sphereOrigin - ro;
    float tca = dot(L, rd);
    float d2 = dot(L, L) - tca * tca;
    if (d2 > radius2) return vec4(0.0);
    float thc = sqrt(radius2 - d2);
    float t0 = tca - thc;
    float t1 = tca + thc;
    float t = t0;
    if (t < 0.0) t = t1; // inside sphere case
    if (t < 0.0) return vec4(0.0); // both behind
    vec3 hitPos = ro + rd * t;
    return vec4(1.0, hitPos);
}

void main() {
    vec4 sampleValue = texture(DiffuseSampler, texCoord);

    if (sampleValue.a > 0.1) {
        vec2 ndc = texCoord * 2.0 - 1.0;

        // Clip-space position on near plane (z = -1 typical for GL)
        vec4 clip = vec4(ndc, -1.0, 1.0);
        vec4 viewPos4 = InverseProjection * clip;
        viewPos4 /= viewPos4.w;

        vec3 rd = normalize(viewPos4.xyz); // camera -> pixel ray

        // Raycast in view space
        vec4 hit = raycast(vec3(0.0), rd, HoleCenter, HoleRadius2);

        if (hit.x == 1.0) {
            vec3 hitPos = hit.yzw;                  // view-space hit
            vec3 N = normalize(hitPos - HoleCenter);// view-space normal
            vec3 V = rd;                // view-space camera-to-hit

            if (dot(V, N) < 0.0) N = -N; // inside-sphere flip

            float fresnel = pow(1.0 - max(dot(V, N), 0.0), 3.0);

            fragColor = vec4(HoleColor.rgb * fresnel, 1.0);
        } else {
            vec4 hit2 = raycast(vec3(0.0), rd, HoleCenter, Radius2 + 0.01);
            if (hit2.x == 1.0) {
                vec3 hitPos = hit2.yzw;
                vec3 N = normalize(hitPos - HoleCenter);// view-space normal
                vec3 V = rd;// view-space camera-to-hit

                if (dot(V, N) < 0.0) N = -N;// inside-sphere flip

                float fresnel = pow(dot(V, N), 1.0);
                float linearFresnel = 1.0 - acos(fresnel);

                linearFresnel *= 1.0 + pow(HoleRadius / Radius, 1.1);

                float k = 5.0;
                float expFresnel = (exp(k*linearFresnel)-1.0)/(exp(k)-1.0);

                vec2 dirVec = normalize(texCoord - HoleScreenCenter) * 0.4 * EffectFraction * expFresnel;

                vec2 newCoord = vec2(dirVec + texCoord);
                newCoord = clamp(newCoord, 0.0, 1.0);
                fragColor = texture(MainSampler, newCoord);
            } else {
                fragColor = vec4(0.0);
            }
        }
    } else {
        fragColor = vec4(0.0);
    }
}