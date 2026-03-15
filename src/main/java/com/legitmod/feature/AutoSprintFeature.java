package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;

public class AutoSprintFeature {

    public static boolean enabled = false;

    private static int wtapTicks = 0;
    private static final int WTAP_TICKS = 3;

    /** Called by AttackEntityCallback. */
    public static void onAttack() {
        if (!enabled) return;
        wtapTicks = WTAP_TICKS;
    }

    public static void onTick(MinecraftClient mc) {
        if (mc.player == null) return;

        // WTap window: force-cancel sprint
        if (wtapTicks > 0) {
            mc.player.setSprinting(false);
            mc.options.sprintKey.setPressed(false);
            wtapTicks--;
            return;            // don't re-enable sprint until window closes
        }

        // Auto-sprint: resume as soon as W is held and conditions met
        if (mc.options.forwardKey.isPressed()
                && !mc.player.isSneaking()
                && !mc.player.isUsingItem()
                && mc.player.getHungerManager().getFoodLevel() > 6
                && !mc.player.isSubmergedInWater()) {
            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
                mc.options.sprintKey.setPressed(true);
            }
        } else {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
                mc.options.sprintKey.setPressed(false);
            }
        }
    }
}
