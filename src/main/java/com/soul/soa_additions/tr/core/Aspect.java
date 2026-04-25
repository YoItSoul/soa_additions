package com.soul.soa_additions.tr.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * One aspect in the Thaumic Remnants aspect tree (see THAUMIC_REMNANTS_DESIGN.md
 * → AspectTree.md). 48 instances total: 6 primal (no parents) + 42 compound (each
 * derived from exactly two parents).
 *
 * <p>This is the lightweight runtime object — just identity, display data, and the
 * parent tuple. Per-item/block aspect assignments, the adjacency BitSet for the
 * hex puzzle, and aura affinities are layered on top of this in later subsystems.
 *
 * <p>Aspects are registered via {@link Aspects} static initialization in
 * dependency order so compounds can resolve their parents at construction time.
 */
public final class Aspect {

    private final ResourceLocation id;
    private final String englishName;
    private final int color;
    private final int tier;
    @Nullable private final Aspect parentA;
    @Nullable private final Aspect parentB;

    Aspect(ResourceLocation id, String englishName, int color) {
        this(id, englishName, color, 0, null, null);
    }

    Aspect(ResourceLocation id, String englishName, Aspect parentA, Aspect parentB) {
        this(id, englishName, mixColors(parentA.color, parentB.color),
                Math.max(parentA.tier, parentB.tier) + 1, parentA, parentB);
    }

    private Aspect(ResourceLocation id, String englishName, int color, int tier,
                   @Nullable Aspect parentA, @Nullable Aspect parentB) {
        this.id = id;
        this.englishName = englishName;
        this.color = color;
        this.tier = tier;
        this.parentA = parentA;
        this.parentB = parentB;
    }

    public ResourceLocation id() { return id; }
    public String englishName() { return englishName; }
    public int color() { return color; }
    public int tier() { return tier; }
    public boolean isPrimal() { return parentA == null; }
    @Nullable public Aspect parentA() { return parentA; }
    @Nullable public Aspect parentB() { return parentB; }

    /** Translation key for the aspect's display name —
     *  {@code aspect.<namespace>.<path>}. Lang entries live in the lang file. */
    public String translationKey() {
        return "aspect." + id.getNamespace() + "." + id.getPath();
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }

    /** Average two RGB colors. Used to derive a compound aspect's tint when no
     *  explicit color is given — keeps the visual lineage of the aspect tree
     *  readable without a manually curated 48-color palette. */
    private static int mixColors(int a, int b) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((ar + br) / 2) << 16 | ((ag + bg) / 2) << 8 | ((ab + bb) / 2);
    }

    @Override
    public String toString() {
        return "Aspect[" + id + ", tier=" + tier + "]";
    }
}
