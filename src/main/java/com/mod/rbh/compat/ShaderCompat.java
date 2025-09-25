package com.mod.rbh.compat;

public final class ShaderCompat {
    private static final boolean OCULUS_PRESENT;
    static {
        boolean present;
        try {
            Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            present = true;
        } catch (ClassNotFoundException e) {
            present = false;
        }
        OCULUS_PRESENT = present;
    }

    public static boolean hasOculus() {
        return OCULUS_PRESENT;
    }

    public static boolean shadersEnabled() {
        if (!OCULUS_PRESENT) return false;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            return (Boolean) apiClass.getMethod("isShaderPackInUse").invoke(api);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
