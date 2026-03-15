package com.legitmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class ElytraSwap {

    private static int     savedChestSlot = -1;
    private static boolean elytraEquipped = false;

    public static void onPress(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (!elytraEquipped) {
            int elytraInvSlot = findElytra(mc);
            if (elytraInvSlot < 0) return;
            savedChestSlot = elytraInvSlot;
            tripleSwap(mc, elytraInvSlot);
            elytraEquipped = true;
        } else {
            int returnSlot = savedChestSlot >= 0 ? savedChestSlot : findEmptySlot(mc);
            if (returnSlot < 0) return;
            tripleSwap(mc, returnSlot);
            elytraEquipped  = false;
            savedChestSlot = -1;
        }
    }

    private static void tripleSwap(MinecraftClient mc, int invSlot) {
        int syncId        = mc.player.currentScreenHandler.syncId;
        int containerSlot = invSlot < 9 ? 36 + invSlot : invSlot;
        int chestSlot     = 6; // chest armor slot in player inventory container
        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, chestSlot,     0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, mc.player);
    }

    private static int findElytra(MinecraftClient mc) {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.ELYTRA)) return i;
        }
        return -1;
    }

    private static int findEmptySlot(MinecraftClient mc) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }
}
