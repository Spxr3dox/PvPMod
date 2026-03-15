package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;

public class AutoAuth {

    public static boolean enabled = false;
    private static boolean sent    = false;
    private static int     delayTicks = 0;
    private static final int DELAY = 40; // 2 seconds after join

    public static void onTick(MinecraftClient mc) {
        if (!enabled) return;
        if (mc.player == null || mc.getNetworkHandler() == null) {
            sent = false;
            delayTicks = 0;
            return;
        }
        if (sent) return;

        delayTicks++;
        if (delayTicks < DELAY) return;

        try {
            mc.player.networkHandler.sendChatCommand("login IRBBY123");
        } catch (Exception ignored) {}
        sent = true;
    }

    public static void reset() { sent = false; delayTicks = 0; }
}
