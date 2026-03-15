package com.legitmod.hud;

import com.legitmod.feature.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class HudOverlay {

    public static boolean unhooked = false;
    private static final int LINE_H = 11;

    public static void render(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        int screenW = mc.getWindow().getScaledWidth();

        // ── Feature list (top-right) ─────────────────────────────────────
        String[] lines = buildLines();
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, tr.getWidth(strip(l)));
        int x = screenW - maxW - 6, y = 5;
        for (String line : lines) {
            ctx.drawText(tr, line, x+1, y+1, 0x55000000, false);
            ctx.drawText(tr, line, x,   y,   0xFFFFFFFF, false);
            y += LINE_H;
        }

        // ── Server info mini-HUD (bottom-right) ──────────────────────────
        if (mc.player != null) {
            int    ping = ServerStats.getPing();
            float  tps  = ServerStats.getTps();

            int pingColor = ping < 80  ? 0xFF55FF55 :
                            ping < 150 ? 0xFFFFFF55 : 0xFFFF5555;
            int tpsColor  = tps  > 18f ? 0xFF55FF55 :
                            tps  > 14f ? 0xFFFFFF55 : 0xFFFF5555;

            String pingStr = "§7Ping: " + colorCode(pingColor) + ping + "§7ms";
            String tpsStr  = "§7TPS:  " + colorCode(tpsColor)  + String.format("%.1f", tps);

            int sh = mc.getWindow().getScaledHeight();
            int bx = screenW - tr.getWidth(strip(pingStr)) - 6;
            int by = sh - 22;

            ctx.drawText(tr, pingStr, bx+1, by+1,   0x55000000, false);
            ctx.drawText(tr, pingStr, bx,   by,     0xFFFFFFFF, false);
            ctx.drawText(tr, tpsStr,  bx+1, by+12,  0x55000000, false);
            ctx.drawText(tr, tpsStr,  bx,   by+11,  0xFFFFFFFF, false);

            if (com.legitmod.feature.TargetStrafe.enabled) {
                String bpsStr = "§7BPS:  §f" + String.format("%.1f", com.legitmod.feature.TargetStrafe.currentBps);
                int bxb = screenW - tr.getWidth(strip(bpsStr)) - 6;
                ctx.drawText(tr, bpsStr, bxb+1, by+23, 0x55000000, false);
                ctx.drawText(tr, bpsStr, bxb,   by+22, 0xFFFFFFFF, false);
            }
        }
    }

    private static String[] buildLines() {
        if (unhooked) {
            return new String[]{
                label("AutoSprint",   AutoSprintFeature.enabled),
                label("ShiftTap",     ShiftTap.enabled),
                label("NameProtect",  NameProtect.enabled)
            };
        }
        return new String[]{
            label("AimBot",       AimBot.enabled),
            label("TargetStrafe", TargetStrafe.enabled),
            label("ESP",          Esp.enabled),
            label("TriggerBot",   TriggerBot.enabled),
            label("Hitbox",       HitboxPlus.enabled),
            label("AutoSprint",   AutoSprintFeature.enabled),
            label("ShiftTap",     ShiftTap.enabled),
            label("NameProtect",  NameProtect.enabled),
            label("AutoAuth",     AutoAuth.enabled),
            label("AutoReg",      AutoReg.enabled)
        };
    }

    private static String label(String name, boolean on) {
        return (on ? "§a" : "§c") + name + " " + (on ? "On" : "Off");
    }

    private static String strip(String s) { return s.replaceAll("§.", ""); }

    private static String colorCode(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b = argb & 0xFF;
        if (g > 200) return "§a";
        if (r > 200 && g > 200) return "§e";
        return "§c";
    }
}
