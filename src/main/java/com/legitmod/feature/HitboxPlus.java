package com.legitmod.feature;

public class HitboxPlus {

    public static boolean enabled = false;
    public static float expand = 0.10f;
    private static final float STEP = 0.05f;

    public static void increase() { expand += STEP; }
    public static void decrease() { expand = Math.max(0f, expand - STEP); }
}
