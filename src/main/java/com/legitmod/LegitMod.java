package com.legitmod;

import com.legitmod.feature.*;
import com.legitmod.hud.HudOverlay;
import com.legitmod.render.EspRenderer;
import com.legitmod.render.TargetMarker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class LegitMod implements ClientModInitializer {

    private static final int K_TRIGGERBOT = GLFW.GLFW_KEY_Y;
    private static final int K_HITBOX     = GLFW.GLFW_KEY_B;
    private static final int K_AUTOSPRINT = GLFW.GLFW_KEY_I;
    private static final int K_SHIFTTAP   = GLFW.GLFW_KEY_H;
    private static final int K_UNHOOK     = GLFW.GLFW_KEY_LEFT;
    private static final int K_HITBOX_INC = GLFW.GLFW_KEY_EQUAL;
    private static final int K_HITBOX_DEC = GLFW.GLFW_KEY_MINUS;
    private static final int K_AUTOAUTH   = GLFW.GLFW_KEY_RIGHT;
    private static final int K_AUTOREG    = GLFW.GLFW_KEY_UP;
    private static final int K_AIMBOT     = GLFW.GLFW_KEY_G;
    private static final int K_COMBO_R    = GLFW.GLFW_KEY_R;
    private static final int K_TARGET     = GLFW.GLFW_KEY_Z;
    private static final int K_ELYTRA     = GLFW.GLFW_KEY_V;
    private static final int K_ESP        = GLFW.GLFW_KEY_LEFT_BRACKET;
    private static final int K_NAMEPROTECT= GLFW.GLFW_KEY_N;

    private static final Map<Integer, Boolean> prev = new HashMap<>();

    @Override
    public void onInitializeClient() {

        EspRenderer.init();
        TargetMarker.init();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) return;

            // Always-on feature ticks
            if (AutoTotem.enabled) AutoTotem.onTick(mc);
            if (Esp.enabled && mc.world != null && mc.player != null) {
                // Track visible players within 400 blocks
                java.util.Set<java.util.UUID> seen = new java.util.HashSet<>();
                mc.world.getEntitiesByClass(
                    net.minecraft.entity.player.PlayerEntity.class,
                    mc.player.getBoundingBox().expand(400),
                    e -> e != mc.player && e.isAlive()
                ).forEach(p -> { Esp.track(p); seen.add(p.getUuid()); });
                // Remove players that are gone (left server or >400 blocks away)
                Esp.trackedPlayers.keySet().removeIf(uuid -> !seen.contains(uuid));
            }
            ServerStats.onTick(mc);

            if (mc.currentScreen != null) return;

            long win = mc.getWindow().getHandle();

            boolean unhook     = edge(win, K_UNHOOK);
            boolean triggerKey = edge(win, K_TRIGGERBOT);
            boolean hitboxKey  = edge(win, K_HITBOX);
            boolean sprintKey  = edge(win, K_AUTOSPRINT);
            boolean shiftKey   = edge(win, K_SHIFTTAP);
            boolean authKey    = edge(win, K_AUTOAUTH);
            boolean regKey     = edge(win, K_AUTOREG);
            boolean incKey     = edge(win, K_HITBOX_INC);
            boolean decKey     = edge(win, K_HITBOX_DEC);
            boolean aimKey     = edge(win, K_AIMBOT);
            boolean comboR     = edge(win, K_COMBO_R);
            boolean targetKey  = edge(win, K_TARGET);
            boolean elytraKey  = edge(win, K_ELYTRA);
            boolean espKey     = edge(win, K_ESP);
            boolean nameKey    = edge(win, K_NAMEPROTECT);

            // ── UnHook ────────────────────────────────────────────────────
            if (unhook) {
                HudOverlay.unhooked = !HudOverlay.unhooked;
                if (HudOverlay.unhooked) {
                    TriggerBot.enabled    = false;
                    HitboxPlus.enabled    = false;
                    AutoTotem.enabled     = false;
                    AimBot.enabled        = false;
                    TargetStrafe.enabled  = false;
                    Esp.enabled           = false;
                } else {
                    AutoTotem.enabled     = true;
                    // ESP stays OFF after unhook — user must press [ again manually
                }
            }

            // ── Blocked while unhooked ────────────────────────────────────
            if (!HudOverlay.unhooked) {
                if (triggerKey) TriggerBot.enabled = !TriggerBot.enabled;
                if (hitboxKey)  HitboxPlus.enabled = !HitboxPlus.enabled;
                if (aimKey) {
                    AimBot.enabled = !AimBot.enabled;
                    if (!AimBot.enabled) { AimBot.reset(); TargetStrafe.reset(); }
                }
                if (targetKey) {
                    TargetStrafe.enabled = !TargetStrafe.enabled;
                    if (!TargetStrafe.enabled) TargetStrafe.reset();
                }
                if (espKey)   Esp.enabled   = !Esp.enabled;
                // R key combo: AimBot off+TriggerBot off → enable both
                //              AimBot on+TriggerBot off → enable TriggerBot
                //              both on → disable both
                if (comboR) {
                    if (!AimBot.enabled && !TriggerBot.enabled) {
                        AimBot.enabled = true;
                        TriggerBot.enabled = true;
                    } else if (AimBot.enabled && !TriggerBot.enabled) {
                        TriggerBot.enabled = true;
                    } else {
                        AimBot.enabled = false;
                        TriggerBot.enabled = false;
                        AimBot.reset();
                    }
                }
            }

            // ── Always available ──────────────────────────────────────────
            if (sprintKey) AutoSprintFeature.enabled = !AutoSprintFeature.enabled;
            if (shiftKey)  ShiftTap.enabled = !ShiftTap.enabled;
            if (authKey)   AutoAuth.enabled = !AutoAuth.enabled;
            if (regKey)    AutoReg.enabled  = !AutoReg.enabled;
            if (elytraKey)  ElytraSwap.onPress(mc);
            if (nameKey)    NameProtect.enabled = !NameProtect.enabled;
            if (incKey)    HitboxPlus.increase();
            if (decKey)    HitboxPlus.decrease();

            // ── Feature ticks ─────────────────────────────────────────────
            if (TargetStrafe.enabled)                    TargetStrafe.onTick(mc);
            if (AimBot.enabled)                          AimBot.onTick(mc);
            if (TriggerBot.enabled)                      TriggerBot.onTick(mc);
            if (AutoSprintFeature.enabled)               AutoSprintFeature.onTick(mc);
            if (ShiftTap.enabled)                        ShiftTap.onTick(mc);
            AutoAuth.onTick(mc);
            AutoReg.onTick(mc);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            AutoSprintFeature.onAttack();
            ShiftTap.onAttack();
            return ActionResult.PASS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AutoAuth.reset();
            AutoReg.reset();
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            HudOverlay.render(drawContext, mc);
        });
    }

    private static boolean edge(long window, int key) {
        boolean cur  = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        boolean p    = prev.getOrDefault(key, false);
        prev.put(key, cur);
        return cur && !p;
    }
}
