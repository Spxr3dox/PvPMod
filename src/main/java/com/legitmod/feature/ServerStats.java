package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class ServerStats {

    private static long   lastPingUpdate = 0;
    private static int    cachedPing     = 0;
    private static float  cachedTps      = 20.0f;

    // TPS estimation via server tick time (from network handler)
    private static long lastTickTime   = 0;
    private static int  tickCount      = 0;
    private static long tickWindowStart = 0;
    private static int  ticksInWindow  = 0;

    public static void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();

        // Ping: read from player list entry
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) cachedPing = entry.getLatency();

        // TPS: count server ticks received (each call = 1 server tick)
        ticksInWindow++;
        if (tickWindowStart == 0) tickWindowStart = now;
        long elapsed = now - tickWindowStart;
        if (elapsed >= 1000) {
            cachedTps = Math.min(20.0f, ticksInWindow * 1000f / elapsed);
            ticksInWindow  = 0;
            tickWindowStart = now;
        }
    }

    public static int   getPing() { return cachedPing; }
    public static float getTps()  { return cachedTps;  }
}
