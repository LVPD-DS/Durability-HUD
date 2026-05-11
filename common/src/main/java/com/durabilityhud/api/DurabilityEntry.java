package com.durabilityhud.api;

/**
 * Platform-agnostic representation of a single item's durability.
 * Populated by platform-specific code and consumed by the renderer.
 */
public class DurabilityEntry {

    public enum Slot { MAIN_HAND, OFF_HAND, HELMET, CHESTPLATE, LEGGINGS, BOOTS }

    public final String name;
    public final int    damage;
    public final int    maxDamage;
    public final Slot   slot;

    public DurabilityEntry(String name, int damage, int maxDamage, Slot slot) {
        this.name      = name;
        this.damage    = damage;
        this.maxDamage = maxDamage;
        this.slot      = slot;
    }

    /** 0.0 – 1.0, where 1.0 = brand new */
    public float ratio() {
        return maxDamage > 0 ? (float)(maxDamage - damage) / maxDamage : 1f;
    }

    public int percentInt() {
        return Math.round(ratio() * 100);
    }

    public String icon() {
        switch (slot) {
            case MAIN_HAND:  return "\u2694 ";  // ⚔
            case OFF_HAND:   return "\uD83D\uDEE1 "; // 🛡
            case HELMET:     return "\u26D1 ";   // ⛑
            case CHESTPLATE: return "\uD83D\uDC55 "; // 👕
            case LEGGINGS:   return "\uD83D\uDC56 "; // 👖
            case BOOTS:      return "\uD83D\uDC62 "; // 👢
            default:         return "? ";
        }
    }
}
