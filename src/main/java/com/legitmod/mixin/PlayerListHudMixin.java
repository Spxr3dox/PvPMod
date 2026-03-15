package com.legitmod.mixin;

import com.legitmod.feature.NameProtect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void filterTabName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (!NameProtect.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        String realName = mc.player.getGameProfile().getName();
        Text original = cir.getReturnValue();
        if (original == null) return;
        String raw = original.getString();
        if (raw.contains(realName)) {
            cir.setReturnValue(Text.literal(NameProtect.filter(raw, realName)));
        }
    }
}
