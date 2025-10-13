package com.mod.rbh.utils.math;

// g(x) = (1 - exp(-k * ((x^p) / (x^p + (1 - x)^q))) * (1 + a*((x^p) / (x^p + (1 - x)^q)) + b*((x^p) / (x^p + (1 - x)^q))^2)) / (1 - exp(-k) * (1 + a + b))
// for GeoGebra
public final class ShootAnimFunction {

    private ShootAnimFunction() {}

    /**
     * g(x) = (1 - exp(-k*w) * (1 + a*w + b*w^2)) / (1 - exp(-k) * (1 + a + b)),
     * where w = x^p / (x^p + (1-x)^q), x in [0,1].
     */
    public static float value(float x, float a, float b, float k, float p, float q) {
        // Clamp x to [0,1] to avoid NaNs when pow(neg, frac)
        if (x <= 0.0) return 0.0f;
        if (x >= 1.0) return 1.0f;

        // Warp w(x) with separate left/right thickness (p,q)
        final float xp = (float) Math.pow(x, p);
        final float omx = 1.0f - x;
        final float omxq = (float) Math.pow(omx, q);
        final float denomWarp = xp + omxq;
        final float w = xp / denomWarp;

        // Core easing with overshoot
        final float ew = (float) Math.exp(-k * w);
        final float w2 = w * w;                    // cheaper than pow(w,2)
        final float num = 1.0f - ew * (1.0f + a * w + b * w2);

        // Normalization so g(1)=1 (and g(0)=0 automatically)
        final float normDen = (float) (1.0f - Math.exp(-k) * (1.0f + a + b));

        // Avoid division by ~0 when parameters make normDen tiny
        if (Math.abs(normDen) < 1e-12) {
            // Fallback: return unnormalized (keeps shape) to avoid blow-ups
            return num;
        }
        return num / normDen;
    }

    /**
     * Faster when evaluating many x's with fixed parameters (precomputes constants).
     */
    public static final class Compiled {
        private final float a, b, k, p, q, normDen;

        public Compiled(float a, float b, float k, float p, float q) {
            this.a = a; this.b = b; this.k = k; this.p = p; this.q = q;
            this.normDen = (float) (1.0f - Math.exp(-k) * (1.0f + a + b));
        }

        public float value(float x) {
            if (x <= 0.0) return 0.0f;
            if (x >= 1.0) return 1.0f;

            final float xp = (float) Math.pow(x, p);
            final float omxq = (float) Math.pow(1.0 - x, q);
            final float w = xp / (xp + omxq);

            final float ew = (float) Math.exp(-k * w);
            final float w2 = w * w;
            final float num = 1.0f - ew * (1.0f + a * w + b * w2);

            if (Math.abs(normDen) < 1e-12) return num;
            return num / normDen;
        }
    }
}
