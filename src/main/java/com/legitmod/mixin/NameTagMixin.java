package com.legitmod.mixin;

import com.legitmod.feature.Esp;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class NameTagMixin {

    /** Return null label when ESP is on — hides vanilla nametag */
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
    private void hideVanillaLabel(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (!Esp.enabled) return;
        if (!(entity instanceof AbstractClientPlayerEntity)) return;
        net.minecraft.client.MinecraftClient mc =
                net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null && entity == mc.player) return;
        cir.setReturnValue(null);
    }
}
