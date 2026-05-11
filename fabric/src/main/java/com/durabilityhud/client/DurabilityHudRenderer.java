package com.durabilityhud.client;

import com.durabilityhud.api.DurabilityEntry;
import com.durabilityhud.api.DurabilityEntry.Slot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DurabilityHudRenderer implements HudRenderCallback {

    private static final int PADDING_X  = 5;
    private static final int PADDING_Y  = 5;
    private static final int ROW_HEIGHT = 10;

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;

        List<DurabilityEntry> entries = collectEntries(client.player);
        if (entries.isEmpty()) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        boolean leftArm = client.player.getMainArm() == Arm.LEFT;
        int startX = leftArm ? screenW - PADDING_X - 144 : PADDING_X;
        int totalH  = entries.size() * ROW_HEIGHT + 4;
        int startY  = screenH - 22 - PADDING_Y - totalH;

        TextRenderer tr = client.textRenderer;
        fillRect(matrixStack, startX - 2, startY - 2, startX + 144, startY + totalH, 0xAA000000);

        for (int i = 0; i < entries.size(); i++) {
            renderRow(matrixStack, tr, startX + 3, startY + 2 + i * ROW_HEIGHT, entries.get(i));
        }
    }

    // ── shared helpers ────────────────────────────────────────────────────────

    static List<DurabilityEntry> collectEntries(PlayerEntity player) {
        List<DurabilityEntry> list = new ArrayList<>();

        addIfDamageable(list, player.getMainHandStack(),  Slot.MAIN_HAND);
        addIfDamageable(list, player.getOffHandStack(),   Slot.OFF_HAND);

        // getArmorItems() order: feet → legs → chest → head (index 0-3)
        Slot[] armorSlots = { Slot.BOOTS, Slot.LEGGINGS, Slot.CHESTPLATE, Slot.HELMET };
        int ai = 0;
        for (ItemStack a : player.getArmorItems()) {
            addIfDamageable(list, a, armorSlots[ai++]);
        }
        return list;
    }

    private static void addIfDamageable(List<DurabilityEntry> list, ItemStack stack, Slot slot) {
        if (!stack.isEmpty() && stack.isDamageable()) {
            list.add(new DurabilityEntry(stack.getName().getString(), stack.getDamage(), stack.getMaxDamage(), slot));
        }
    }

    private void renderRow(MatrixStack ms, TextRenderer tr, int x, int y, DurabilityEntry e) {
        float ratio = e.ratio();
        int   color = colorFor(ratio);

        tr.drawWithShadow(ms, e.icon() + truncate(e.name, 9), x, y, 0xFFFFFFFF);

        int barX = x + 65;
        int barW = 50;
        int barY = y + 2;

        fillRect(ms, barX, barY, barX + barW, barY + 5, 0xFF333333);
        int filled = (int)(barW * ratio);
        if (filled > 0) fillRect(ms, barX, barY, barX + filled, barY + 5, color);

        tr.drawWithShadow(ms, e.percentInt() + "%", barX + barW + 3, y, color);
    }

    private void fillRect(MatrixStack ms, int x1, int y1, int x2, int y2, int color) {
        Tessellator   tess = Tessellator.getInstance();
        BufferBuilder buf  = tess.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >>  8 & 0xFF) / 255f;
        float b = (color       & 0xFF) / 255f;
        RenderSystem.color4f(r, g, b, a);

        Matrix4f mat = ms.peek().getModel();
        buf.begin(GL11.GL_QUADS, VertexFormats.POSITION);
        buf.vertex(mat, x1, y2, 0).next();
        buf.vertex(mat, x2, y2, 0).next();
        buf.vertex(mat, x2, y1, 0).next();
        buf.vertex(mat, x1, y1, 0).next();
        tess.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
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
