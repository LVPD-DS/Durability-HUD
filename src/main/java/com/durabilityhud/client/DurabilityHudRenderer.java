package com.durabilityhud.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders item durability indicators on the HUD — версия для Minecraft 1.16.5.
 *
 * Ключевые отличия от 1.21:
 *  - HudRenderCallback принимает (MatrixStack, tickDelta) вместо (DrawContext, RenderTickCounter)
 *  - Нет DrawContext — рисуем через MinecraftClient.getInstance().inGameHud / textRenderer напрямую
 *  - fill() и drawString() вызываются через MatrixStack + RenderSystem
 *  - ItemStack.isDamageable() → ItemStack.isDamageable() (то же, но getMaxDamage() было всегда)
 *  - Нет getScaledWindowWidth() у DrawContext — берём из Window
 *
 * Цветовая индикация (ARGB):
 *  🟢 >50%  — 0xFF55FF55
 *  🟡 25-50% — 0xFFFFFF55
 *  🟠 10-25% — 0xFFFFAA00
 *  🔴 <10%  — 0xFFFF5555
 */
@Environment(EnvType.CLIENT)
public class DurabilityHudRenderer implements HudRenderCallback {

    private static final int PADDING_X  = 5;
    private static final int PADDING_Y  = 5;
    private static final int ROW_HEIGHT = 10;

    // ---------------------------------------------------------------
    // HudRenderCallback — сигнатура для 1.16.5
    // ---------------------------------------------------------------
    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;   // скрываем в меню

        PlayerEntity player = client.player;

        List<DurabilityEntry> entries = new ArrayList<>();

        // --- Основная рука ---
        ItemStack mainHand = player.getMainHandStack();
        if (isDamageable(mainHand)) {
            entries.add(new DurabilityEntry(getName(mainHand), mainHand.getDamage(), mainHand.getMaxDamage(), "\u2694 "));
        }

        // --- Второй рука ---
        ItemStack offHand = player.getOffHandStack();
        if (isDamageable(offHand)) {
            entries.add(new DurabilityEntry(getName(offHand), offHand.getDamage(), offHand.getMaxDamage(), "\uD83D\uDEE1 "));
        }

        // --- Броня (хранится [0]=boots [1]=legs [2]=chest [3]=helmet) ---
        // Рисуем сверху вниз: шлем → ботинки
        String[] armorIcons = {"\uD83D\uDC62 ", "\uD83D\uDC56 ", "\uD83D\uDC55 ", "\u26D1 "};
        ItemStack[] armorArr = new ItemStack[4];
        int ai = 0;
        for (ItemStack a : player.getArmorItems()) armorArr[ai++] = a;
        for (int i = 3; i >= 0; i--) {
            if (isDamageable(armorArr[i])) {
                entries.add(new DurabilityEntry(getName(armorArr[i]), armorArr[i].getDamage(), armorArr[i].getMaxDamage(), armorIcons[i]));
            }
        }

        if (entries.isEmpty()) return;

        // Размеры окна — в 1.16.5 берём через client.getWindow()
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        boolean leftArm = player.getMainArm() == Arm.LEFT;
        int startX = leftArm
                ? screenW - PADDING_X - 144
                : PADDING_X;

        int hotbarH = 22;
        int totalH  = entries.size() * ROW_HEIGHT + 4;
        int startY  = screenH - hotbarH - PADDING_Y - totalH;

        TextRenderer tr = client.textRenderer;

        // Фон
        fillRect(matrixStack, startX - 2, startY - 2, startX + 144, startY + totalH, 0xAA000000);

        for (int i = 0; i < entries.size(); i++) {
            int y = startY + 2 + i * ROW_HEIGHT;
            renderRow(matrixStack, tr, startX + 3, y, entries.get(i));
        }
    }

    // ---------------------------------------------------------------
    // Отрисовка одной строки
    // ---------------------------------------------------------------
    private void renderRow(MatrixStack ms, TextRenderer tr, int x, int y, DurabilityEntry e) {
        int current = e.maxDamage - e.damage;
        float ratio = e.maxDamage > 0 ? (float) current / e.maxDamage : 1f;
        int color   = colorFor(ratio);

        // Текст: иконка + название (обрезаем если длинное)
        String label = e.icon + truncate(e.name, 9);
        tr.drawWithShadow(ms, label, x, y, 0xFFFFFFFF);

        // Полоска прочности
        int barX = x + 65;
        int barW = 50;
        int barY = y + 2;

        fillRect(ms, barX, barY, barX + barW, barY + 5, 0xFF333333);  // фон
        int filled = (int)(barW * ratio);
        if (filled > 0) {
            fillRect(ms, barX, barY, barX + filled, barY + 5, color);
        }

        // Процент справа от полоски
        String pct = (int)(ratio * 100) + "%";
        tr.drawWithShadow(ms, pct, barX + barW + 3, y, color);
    }

    // ---------------------------------------------------------------
    // Вспомогательный fill через Tessellator (аналог DrawContext.fill в 1.16.5)
    // ---------------------------------------------------------------
    private void fillRect(MatrixStack ms, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.render.Tessellator tess = net.minecraft.client.render.Tessellator.getInstance();
        net.minecraft.client.render.BufferBuilder buf = tess.getBuffer();

        net.minecraft.client.render.RenderSystem.enableBlend();
        net.minecraft.client.render.RenderSystem.disableTexture();
        net.minecraft.client.render.RenderSystem.defaultBlendFunc();

        float a = (float)(color >> 24 & 0xFF) / 255f;
        float r = (float)(color >> 16 & 0xFF) / 255f;
        float g = (float)(color >>  8 & 0xFF) / 255f;
        float b = (float)(color       & 0xFF) / 255f;

        net.minecraft.client.render.RenderSystem.color4f(r, g, b, a);

        org.lwjgl.opengl.GL11.glColor4f(r, g, b, a);

        net.minecraft.util.math.Matrix4f mat = ms.peek().getModel();
        buf.begin(org.lwjgl.opengl.GL11.GL_QUADS, net.minecraft.client.render.VertexFormats.POSITION);
        buf.vertex(mat, x1, y2, 0).next();
        buf.vertex(mat, x2, y2, 0).next();
        buf.vertex(mat, x2, y1, 0).next();
        buf.vertex(mat, x1, y1, 0).next();
        tess.draw();

        net.minecraft.client.render.RenderSystem.enableTexture();
        net.minecraft.client.render.RenderSystem.disableBlend();
    }

    // ---------------------------------------------------------------
    // Утилиты
    // ---------------------------------------------------------------
    private int colorFor(float ratio) {
        if (ratio > 0.50f) return 0xFF55FF55;
        if (ratio > 0.25f) return 0xFFFFFF55;
        if (ratio > 0.10f) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private boolean isDamageable(ItemStack s) {
        return !s.isEmpty() && s.isDamageable();
    }

    private String getName(ItemStack s) {
        return s.getName().getString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }

    // ---------------------------------------------------------------
    // Data holder
    // ---------------------------------------------------------------
    private static class DurabilityEntry {
        final String name;
        final int damage, maxDamage;
        final String icon;

        DurabilityEntry(String name, int damage, int maxDamage, String icon) {
            this.name      = name;
            this.damage    = damage;
            this.maxDamage = maxDamage;
            this.icon      = icon;
        }
    }
}
