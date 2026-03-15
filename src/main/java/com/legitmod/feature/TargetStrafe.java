package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TargetStrafe {

    public static boolean enabled = false;

    private static PlayerEntity lockedTarget = null;
    private static int   dir        = 1;
    private static int   dirTimer   = 0;
    private static int   dirEvery   = 130;

    public static float currentBps  = 0f;
    private static Vec3d lastPos    = null;
    private static int   bpsTick    = 0;
    private static float bpsAccum   = 0f;

    private static final float ORBIT_RADIUS = 0.5f;
    private static final float BASE_SPEED   = 0.28f;
    private static final float MAX_SPEED    = 0.65f;
    private static final float ORBIT_SPEED  = 16f;
    private static final Random RNG = new Random();

    private static boolean firedThisCycle = false;
    private static float   lastCooldown   = 0f;

    // Critical hit: jump timing
    private static boolean critJumped    = false;
    private static boolean waitingFall   = false;

    // Head bob while attacking (pitch oscillation)
    private static float   headBobAngle  = 0f;
    private static boolean isBobbing     = false;
    private static int     bobTicks      = 0;

    public static PlayerEntity getLockedTarget() { return lockedTarget; }

    public static void onTick(MinecraftClient mc) {
        if (!enabled) return;
        if (mc.player == null || mc.world == null) return;

        if (lockedTarget == null || !lockedTarget.isAlive()
                || mc.player.distanceTo(lockedTarget) > 24.0) {
            lockedTarget = findTarget(mc);
            if (lockedTarget == null) return;
        }

        // BPS
        if (lastPos != null) {
            Vec3d cur = mc.player.getPos();
            bpsAccum += (float) lastPos.distanceTo(cur) * 20f;
            if (++bpsTick >= 20) { currentBps = bpsAccum / 20f; bpsAccum = 0; bpsTick = 0; }
        }
        lastPos = mc.player.getPos();

        // Dir flip
        if (++dirTimer >= dirEvery) {
            dirTimer = 0; dirEvery = 80 + RNG.nextInt(100);
            if (RNG.nextFloat() < 0.45f) dir = -dir;
        }

        Vec3d tp = lockedTarget.getPos();
        Vec3d pp = mc.player.getPos();
        double dist = mc.player.distanceTo(lockedTarget);

        double dxT = tp.x - pp.x, dzT = tp.z - pp.z;
        double fLen = Math.sqrt(dxT*dxT + dzT*dzT);
        if (fLen < 0.01) return;

        double fwdX = dxT/fLen, fwdZ = dzT/fLen;
        double strafeX = fwdZ * dir, strafeZ = -fwdX * dir;

        float speed = BASE_SPEED + Math.min(MAX_SPEED-BASE_SPEED, (float)(dist-ORBIT_RADIUS)*0.18f);
        speed = Math.max(BASE_SPEED, speed);

        double moveX, moveZ;
        if (dist > ORBIT_RADIUS + 1.0) {
            moveX = fwdX*0.75 + strafeX*0.25; moveZ = fwdZ*0.75 + strafeZ*0.25;
            speed = Math.min(MAX_SPEED, speed * 1.5f);
        } else if (dist < ORBIT_RADIUS - 0.25) {
            moveX = -fwdX*0.5 + strafeX*0.5; moveZ = -fwdZ*0.5 + strafeZ*0.5;
        } else {
            moveX = strafeX; moveZ = strafeZ;
        }

        double mLen = Math.sqrt(moveX*moveX + moveZ*moveZ);
        if (mLen > 0.01) { moveX /= mLen; moveZ /= mLen; }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(moveX*speed, vel.y, moveZ*speed);
        mc.player.setSprinting(true);

        // ── Body/head face target ─────────────────────────────────────
        float bodyYaw = (float) Math.toDegrees(Math.atan2(-dxT, dzT));
        mc.player.bodyYaw = bodyYaw; mc.player.prevBodyYaw = bodyYaw;
        mc.player.headYaw = bodyYaw;

        // ── Crit jump timing ──────────────────────────────────────────
        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (lastCooldown > 0.5f && cooldown < 0.5f) { firedThisCycle = false; critJumped = false; }
        lastCooldown = cooldown;

        // Jump just before attack to get crit
        if (!critJumped && cooldown > 0.85f && mc.player.isOnGround()) {
            mc.player.jump();
            critJumped  = true;
            waitingFall = true;
        }

        // ── Head pitch bob while attacking ────────────────────────────
        if (isBobbing) {
            bobTicks++;
            headBobAngle += 18f; // 20 ticks = full cycle
            if (bobTicks >= 12) { isBobbing = false; bobTicks = 0; headBobAngle = 0f; }
        }

        // ── Aim + attack ──────────────────────────────────────────────
        Vec3d eye   = mc.player.getEyePos();
        Vec3d chest = new Vec3d(lockedTarget.getX(), lockedTarget.getY()+0.9, lockedTarget.getZ());

        double adx = chest.x-eye.x, ady = chest.y-eye.y, adz = chest.z-eye.z;
        double ad2 = Math.sqrt(adx*adx + adz*adz);

        float tYaw   = (float) Math.toDegrees(Math.atan2(-adx, adz));
        float tPitch = MathHelper.clamp((float) Math.toDegrees(-Math.atan2(ady, ad2)), -90f, 90f);

        // Apply pitch bob (up-down while hitting)
        float finalPitch = tPitch + (float)(Math.sin(Math.toRadians(headBobAngle)) * 12.0);

        float savedYaw = mc.player.getYaw(), savedPitch = mc.player.getPitch();
        mc.player.setYaw(tYaw);
        mc.player.setPitch(finalPitch);

        // Fire when falling (crit window) and cooldown ready
        boolean falling = mc.player.getVelocity().y < -0.05;
        if (!firedThisCycle && cooldown >= 0.985f && dist <= AimBot.RANGE
                && (falling || mc.player.isOnGround())) {
            mc.interactionManager.attackEntity(mc.player, lockedTarget);
            mc.player.swingHand(Hand.MAIN_HAND);
            firedThisCycle = true;
            isBobbing = true;
            bobTicks  = 0;
        }

        mc.player.setYaw(savedYaw);
        mc.player.setPitch(savedPitch);
    }

    private static PlayerEntity findTarget(MinecraftClient mc) {
        List<PlayerEntity> list = mc.world.getEntitiesByClass(
            PlayerEntity.class, mc.player.getBoundingBox().expand(24),
            e -> e != mc.player && e.isAlive()
        );
        if (list.isEmpty()) return null;
        list.sort(Comparator
            .comparingDouble((PlayerEntity e) -> (double) e.getHealth())
            .thenComparingDouble(e -> mc.player.distanceTo(e)));
        return list.get(0);
    }

    public static void reset() {
        lockedTarget = null; dir = 1; dirTimer = 0;
        firedThisCycle = false; lastCooldown = 0f;
        critJumped = false; waitingFall = false;
        isBobbing = false; bobTicks = 0; headBobAngle = 0f;
        currentBps = 0f; lastPos = null; bpsTick = 0; bpsAccum = 0f;
    }
}
