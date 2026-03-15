package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AimBot {

    public static boolean enabled = false;
    public static final float RANGE = 6.0f;

    // Faster smoothness
    private static final float SMOOTH_CLOSE = 0.55f;
    private static final float SMOOTH_FAR   = 0.85f;

    public static PlayerEntity lockedTarget = null;
    private static final Random RNG = new Random();
    private static float predX = 0, predY = 0;
    private static int   predTimer = 0;

    public static void onTick(MinecraftClient mc) {
        if (!enabled) return;
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        if (lockedTarget != null &&
                (!lockedTarget.isAlive() || mc.player.distanceTo(lockedTarget) > 20.0)) {
            lockedTarget = null;
        }
        if (lockedTarget == null) lockedTarget = findClosestTarget(mc);
        if (lockedTarget == null) return;

        float dist = (float) mc.player.distanceTo(lockedTarget);
        if (dist > RANGE) return;

        float t = MathHelper.clamp((dist - 1f) / (RANGE - 1f), 0f, 1f);
        float smooth = SMOOTH_CLOSE + (SMOOTH_FAR - SMOOTH_CLOSE) * t;

        // Micro-jitter every 3-6 ticks
        if (--predTimer <= 0) {
            predTimer = 3 + RNG.nextInt(4);
            predX = (RNG.nextFloat() - 0.5f) * 0.4f;
            predY = (RNG.nextFloat() - 0.5f) * 0.3f;
        }

        Vec3d eye = mc.player.getEyePos();
        // AIM AT CHEST = getY() + 0.9 (mid torso, works regardless of TargetStrafe)
        Vec3d chest = new Vec3d(
            lockedTarget.getX(),
            lockedTarget.getY() + 0.9,
            lockedTarget.getZ()
        );

        double dx = chest.x - eye.x;
        double dy = chest.y - eye.y;
        double dz = chest.z - eye.z;
        double d2 = Math.sqrt(dx*dx + dz*dz);

        float tYaw   = (float) Math.toDegrees(Math.atan2(-dx, dz)) + predX;
        float tPitch = (float) Math.toDegrees(-Math.atan2(dy, d2)) + predY;

        mc.player.setYaw(lerpAngle(mc.player.getYaw(),   tYaw,   smooth));
        mc.player.setPitch(MathHelper.clamp(
            lerpAngle(mc.player.getPitch(), tPitch, smooth), -90f, 90f));
    }

    public static PlayerEntity findClosestTarget(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return null;
        List<PlayerEntity> list = mc.world.getEntitiesByClass(
            PlayerEntity.class,
            mc.player.getBoundingBox().expand(RANGE),
            e -> e != mc.player && e.isAlive() && mc.player.distanceTo(e) <= RANGE
        );
        if (list.isEmpty()) return null;
        list.sort(Comparator
            .comparingDouble((PlayerEntity e) -> (double) e.getHealth())
            .thenComparingDouble(e -> mc.player.distanceTo(e)));
        return list.get(0);
    }

    public static void reset() { lockedTarget = null; }

    private static float lerpAngle(float cur, float target, float t) {
        return cur + MathHelper.wrapDegrees(target - cur) * t;
    }
}
