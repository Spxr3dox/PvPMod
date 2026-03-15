package com.legitmod.mixin;

import com.legitmod.feature.NameProtect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
                    at = @At("HEAD"), argsOnly = true, index = 1, require = 0)
    private Text filterChatMessage(Text message) {
        if (!NameProtect.enabled || message == null) return message;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return message;
        String realName = mc.player.getGameProfile().getName();
        String raw = message.getString();
        if (!raw.contains(realName)) return message;
        // Replace in the serialized form and reconstruct as literal
        return Text.literal(NameProtect.filter(raw, realName));
    }
}
