package com.legitmod.mixin;

import com.legitmod.feature.HitboxPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 2000)
public abstract class EntityMixin {

    // Only hitbox expansion - nothing that touches nametag rendering
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true, require = 0)
    private void expandHitbox(CallbackInfoReturnable<Box> cir) {
        if (!HitboxPlus.enabled) return;
        float e = HitboxPlus.expand;
        if (e <= 0f) return;
        Entity self = (Entity)(Object)this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && self == mc.player) return;
        Box b = cir.getReturnValue();
        cir.setReturnValue(new Box(b.minX-e, b.minY, b.minZ-e, b.maxX+e, b.maxY, b.maxZ+e));
    }
}
