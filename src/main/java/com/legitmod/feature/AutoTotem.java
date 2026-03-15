package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem {

    // Always active unless UnHook is on — no toggle key
    public static boolean enabled = true;

    private static final float HP_THRESHOLD = 4.5f;

    public static void onTick(MinecraftClient mc) {
        if (!enabled) return;
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        float hp = mc.player.getHealth();
        if (!offhand.isEmpty() && hp > HP_THRESHOLD) return;

        int totemSlot = -1;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot < 0) return;

        int containerSlot = totemSlot < 9 ? 36 + totemSlot : totemSlot;
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            containerSlot, 40,
            SlotActionType.SWAP,
            mc.player
        );
    }
}
