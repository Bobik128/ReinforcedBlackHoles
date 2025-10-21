#version 150

uniform sampler2D DiffuseSampler;   // color from scene
uniform sampler2D MainSampler;      // color to sample after distortion

in vec2 texCoord;
in vec3 rayDir; // (unused here)

uniform vec3 HoleCenter;            // view-space center of the hole
uniform vec2 HoleScreenCenter;      // kept for compatibility (not used below)
uniform vec4 HoleColor;

uniform float HoleRadius;
uniform float Radius;
uniform float Exponent;
uniform float EffectOffset;
uniform mat4  InverseProjection;    // to build a view ray from texCoord
uniform mat4  Projection;           // to compute correct screen direction

uniform float HoleRadius2;          // precomputed HoleRadius^2
uniform float Radius2;              // precomputed Radius^2
uniform float EffectFraction;       // precomputed Radius / (tan(fov/2)*DistFromCam)

// Ellipse controls (VIEW-SPACE)
uniform vec3  StretchDir;           // axis of elongation (view-space)
uniform float StretchStrength;      // 0 = sphere, >0 = stretch equally along +/-axis (scale = 1 + StretchStrength)

out vec4 fragColor;

/* ===================== helpers ===================== */

// Scale only the component of v parallel to unit axis u by factor k.
vec3 scaleAlong(vec3 v, vec3 u, float k) {
    float p = dot(v, u);
    return v + (k - 1.0) * p * u;
}

// Affine helpers that scale relative to the hole center (positions vs. directions)
vec3 toSpherePoint(vec3 p, vec3 c, vec3 u, float invStretch) {
    return c + scaleAlong(p - c, u, invStretch);
}
vec3 toSphereDir(vec3 d, vec3 u, float invStretch) {
    return scaleAlong(d, u, invStretch); // DO NOT normalize afterward
}
vec3 fromSpherePoint(vec3 p, vec3 c, vec3 u, float stretch) {
    return c + scaleAlong(p - c, u, stretch);
}

// General ray-sphere intersection (rd need NOT be unit length)
vec4 raycast_general(vec3 ro, vec3 rd, vec3 center, float radius2) {
    vec3 oc = ro - center;
    float a = dot(rd, rd);
    float b = 2.0 * dot(oc, rd);
    float c = dot(oc, oc) - radius2;
    float disc = b*b - 4.0*a*c;
    if (disc < 0.0) return vec4(0.0);
    float sdisc = sqrt(disc);
    float inv2a = 0.5 / a;
    float t0 = (-b - sdisc) * inv2a;
    float t1 = (-b + sdisc) * inv2a;
    float t = t0;
    if (t < 0.0) t = t1;             // inside-surface case
    if (t < 0.0) return vec4(0.0);
    vec3 hitPos = ro + rd * t;
    return vec4(1.0, hitPos);
}

/* ===================== main ===================== */

void main() {
    vec4 sampleValue = textureLod(DiffuseSampler, texCoord, 0.0);
    if (sampleValue.a <= 0.1) {
        fragColor = vec4(0.0);
        return;
    }

    // Build a view ray from texCoord using the inverse projection
    vec2 ndc = texCoord * 2.0 - 1.0;               // [0,1] -> [-1,1]
    vec4 clip = vec4(ndc, -1.0, 1.0);              // near plane in GL (-1)
    vec4 viewPos4 = InverseProjection * clip;
    viewPos4 /= viewPos4.w;
    vec3 rd_view = normalize(viewPos4.xyz);         // camera -> pixel ray (view-space)

    // === Ellipse setup (VIEW-SPACE) ===
    vec3 u = normalize(StretchDir);                 // unit axis in view-space
    float s    = max(1.0 + StretchStrength, 1e-6);  // position scale along u
    float invS = 1.0 / s;

    // Transform camera origin (0) and ray dir into "sphere space" (squashed along u)
    vec3 ro_s = toSpherePoint(vec3(0.0), HoleCenter, u, invS);
    vec3 rd_s = toSphereDir(rd_view, u, invS);   // IMPORTANT: not normalized
    vec3 c_s  = HoleCenter;

    // ---------- inner (event horizon) ----------
    vec4 hit_inner_s = raycast_general(ro_s, rd_s, c_s, HoleRadius2);
    if (hit_inner_s.x == 1.0) {
        // Map hit + normal back into view space for shading
        vec3 hit_inner_view = fromSpherePoint(hit_inner_s.yzw, HoleCenter, u, s);

        // Sphere-space normal -> view-space ellipsoid normal via inverse-transpose of stretch
        vec3 N_sphere = normalize(hit_inner_s.yzw - HoleCenter);
        vec3 N_view   = normalize(scaleAlong(N_sphere, u, invS));

        vec3 V = rd_view;
        if (dot(V, N_view) < 0.0) N_view = -N_view;

        float fresnel = pow(1.0 - max(dot(V, N_view), 0.0), 3.0);
        fragColor = vec4(HoleColor.rgb * fresnel, 1.0);
        return;
    }

    // Only the hole center is scaled — the camera stays at origin.
    ro_s = vec3(0.0);
    c_s  = scaleAlong(HoleCenter, u, invS); // scaled hole center

    // ---------- outer (distortion shell) ----------
    vec4 hit_outer_s = raycast_general(ro_s, rd_s, c_s, Radius2 * 1.001);
    if (hit_outer_s.x == 1.0) {
        vec3 hit_outer_view = fromSpherePoint(hit_outer_s.yzw, HoleCenter, u, s);

        vec3 N_sphere = normalize(hit_outer_s.yzw - HoleCenter);
        vec3 N_view   = normalize(scaleAlong(N_sphere, u, invS));

        vec3 V = rd_view;
        if (dot(V, N_view) < 0.0) N_view = -N_view;

        // Your fresnel-ish shaping
        float f = dot(V, N_view);
        float r = sqrt(max(1.0 - f*f, 0.0));
        float linearFresnel = 1.0 - r;

        float denom = max(1.0 - EffectOffset, 1e-5);
        float expFresnel = max(pow(linearFresnel / denom, Exponent), 0.0);

        // Screen-space direction based on true projected positions (stable at wide FOV / edges)
        vec4 centerClip = Projection * vec4(HoleCenter,     1.0);
        vec2 centerNDC  = centerClip.xy / centerClip.w;          // [-1,1]

        vec4 hitClip    = Projection * vec4(hit_outer_view, 1.0);
        vec2 hitNDC     = hitClip.xy / hitClip.w;                // [-1,1]

        vec2 dirNDC = normalize(hitNDC - centerNDC);             // on-screen radial from center to hit
        vec2 dirTex = dirNDC * 0.5;                              // NDC -> texcoords

        vec2 newCoord = texCoord + dirTex * 0.4 * EffectFraction * expFresnel;
        newCoord = clamp(newCoord, 0.0, 1.0);

        vec4 col = textureLod(MainSampler, newCoord, 0.0);
        fragColor = vec4(col.rgb, 1.0);

        return;
    }

    fragColor = vec4(0.0);
}