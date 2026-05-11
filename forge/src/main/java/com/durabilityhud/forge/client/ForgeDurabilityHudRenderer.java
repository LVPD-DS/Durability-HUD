package com.durabilityhud.forge.client;

import com.durabilityhud.api.DurabilityEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 1.21 HUD renderer.
 *
 * Key API differences from Fabric 1.16.5:
 *  - Uses GuiGraphics (replaces PoseStack + raw GL calls)
 *  - GuiGraphics.fill()  → draws a colored quad (no Tessellator needed)
 *  - GuiGraphics.drawString() → renders text with optional drop-shadow
 *  - IGuiOverlay is the Forge equivalent of HudRenderCallback
 */
public class ForgeDurabilityHudRenderer implements IGuiOverlay {

    private static final int PADDING_X  = 5;
    private static final int PADDING_Y  = 5;
    private static final int ROW_HEIGHT = 10;

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui,
                       GuiGraphics guiGraphics,
                       float partialTick,
                       int screenWidth,
                       int screenHeight) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;           // hide when a GUI is open

        Player player = mc.player;
        List<DurabilityEntry> entries = collectEntries(player);
        if (entries.isEmpty()) return;

        boolean leftArm = player.getMainArm() == HumanoidArm.LEFT;
        int startX = leftArm ? screenWidth - PADDING_X - 144 : PADDING_X;
        int totalH  = entries.size() * ROW_HEIGHT + 4;
        int startY  = screenHeight - 22 - PADDING_Y - totalH;

        // Background panel
        guiGraphics.fill(startX - 2, startY - 2, startX + 144, startY + totalH, 0xAA000000);

        Font font = mc.font;
        for (int i = 0; i < entries.size(); i++) {
            renderRow(guiGraphics, font, startX + 3, startY + 2 + i * ROW_HEIGHT, entries.get(i));
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<DurabilityEntry> collectEntries(Player player) {
        List<DurabilityEntry> list = new ArrayList<>();

        addIfDamageable(list, player.getMainHandItem(),   DurabilityEntry.Slot.MAIN_HAND);
        addIfDamageable(list, player.getOffhandItem(),    DurabilityEntry.Slot.OFF_HAND);

        // getArmorSlots() order in Forge 1.21: feet, legs, chest, head
        DurabilityEntry.Slot[] armorSlots = {
            DurabilityEntry.Slot.BOOTS,
            DurabilityEntry.Slot.LEGGINGS,
            DurabilityEntry.Slot.CHESTPLATE,
            DurabilityEntry.Slot.HELMET
        };
        int ai = 0;
        for (net.minecraft.world.item.ItemStack a : player.getArmorSlots()) {
            addIfDamageable(list, a, armorSlots[ai++]);
        }
        return list;
    }

    private void addIfDamageable(List<DurabilityEntry> list,
                                  net.minecraft.world.item.ItemStack stack,
                                  DurabilityEntry.Slot slot) {
        if (!stack.isEmpty() && stack.isDamageableItem()) {
            list.add(new DurabilityEntry(
                stack.getHoverName().getString(),
                stack.getDamageValue(),
                stack.getMaxDamage(),
                slot
            ));
        }
    }

    private void renderRow(GuiGraphics g, Font font, int x, int y, DurabilityEntry e) {
        float ratio = e.ratio();
        int   color = colorFor(ratio);

        // Icon + name (shadow = true)
        g.drawString(font, e.icon() + truncate(e.name, 9), x, y, 0xFFFFFFFF, true);

        // Progress bar
        int barX = x + 65;
        int barW = 50;
        int barY = y + 2;

        g.fill(barX, barY, barX + barW, barY + 5, 0xFF333333);
        int filled = (int)(barW * ratio);
        if (filled > 0) g.fill(barX, barY, barX + filled, barY + 5, color);

        // Percentage text
        g.drawString(font, e.percentInt() + "%", barX + barW + 3, y, color, true);
    }

    private int colorFor(float ratio) {
        if (ratio > 0.50f) return 0xFF55FF55;
        if (ratio > 0.25f) return 0xFFFFFF55;
        if (ratio > 0.10f) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }
}
