package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Random;

public class TriggerBot {

    public static boolean enabled = false;
    public static final float RANGE = 3.0f;

    private static final Random RNG = new Random();
    private static float threshold = nextThreshold();
    private static boolean firedThisCycle = false;
    private static float lastProgress = 0f;

    // Crit tracking
    private static boolean scheduledCrit = false;  // waiting to land a crit
    private static boolean wasInAir = false;

    private static float nextThreshold() {
        return 0.980f + RNG.nextFloat() * 0.019f;
    }

    public static void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof SwordItem)) return;

        LivingEntity target = getCrosshairTarget(mc);
        if (target == null) {
            firedThisCycle = false;
            lastProgress = 0f;
            scheduledCrit = false;
            wasInAir = false;
            return;
        }

        float progress = mc.player.getAttackCooldownProgress(0f);

        // Detect new cooldown cycle
        if (lastProgress > 0.5f && progress < 0.5f) {
            firedThisCycle = false;
            threshold = nextThreshold();
        }
        lastProgress = progress;

        if (firedThisCycle) return;

        boolean onGround = mc.player.isOnGround();
        boolean falling  = mc.player.getVelocity().y < -0.08;   // falling = crit window
        boolean inAir    = !onGround;

        // ── SMART CRITS ────────────────────────────────────────────────────
        if (inAir) {
            // In the air: wait until falling (peak passed) for a crit
            if (falling && progress >= threshold) {
                attack(mc, target);
            }
        } else {
            // On ground: attack at normal threshold (no crit, that's fine)
            if (progress >= threshold) {
                attack(mc, target);
            }
        }

        wasInAir = inAir;
    }

    private static void attack(MinecraftClient mc, LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        firedThisCycle = true;
    }

    private static LivingEntity getCrosshairTarget(MinecraftClient mc) {
        if (mc.crosshairTarget instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof LivingEntity le
                    && le != mc.player
                    && le.isAlive()
                    && mc.player.distanceTo(le) <= RANGE) {
                return le;
            }
        }
        return null;
    }
}
