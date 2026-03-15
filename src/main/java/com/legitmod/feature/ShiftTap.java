package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import java.util.Random;

public class ShiftTap {

    public static boolean enabled = false;

    private static final Random RNG = new Random();
    private static int sneakTicksLeft = 0;
    private static boolean wasForcingSneaking = false;

    public static void onAttack() {
        if (!enabled) return;
        // Randomise between 2 and 3 ticks
        sneakTicksLeft = 2 + RNG.nextInt(2); // 2 or 3
    }

    public static void onTick(MinecraftClient mc) {
        if (mc.player == null) return;

        if (sneakTicksLeft > 0) {
            mc.options.sneakKey.setPressed(true);
            mc.player.setSneaking(true);
            wasForcingSneaking = true;
            sneakTicksLeft--;
        } else if (wasForcingSneaking) {
            mc.options.sneakKey.setPressed(false);
            mc.player.setSneaking(false);
            wasForcingSneaking = false;
        }
    }
}
