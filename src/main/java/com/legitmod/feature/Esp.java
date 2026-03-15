package com.legitmod.feature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ESP — tracks player positions for world-space box rendering.
 * Does NOT touch any rendering hooks or nametag logic.
 * Vanilla nametags remain completely untouched.
 */
public class Esp {
    public static boolean enabled = false;

    public static final Map<UUID, Vec3d> trackedPlayers = new ConcurrentHashMap<>();

    public static void track(PlayerEntity p) {
        trackedPlayers.put(p.getUuid(), new Vec3d(p.getX(), p.getY(), p.getZ()));
    }

    public static void clear() {
        trackedPlayers.clear();
    }
}
