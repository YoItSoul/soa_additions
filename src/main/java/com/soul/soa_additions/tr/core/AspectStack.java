package com.soul.soa_additions.tr.core;

/**
 * One slot in an item or block's aspect composition: the aspect itself plus
 * how much of it the target contributes when scanned/decomposed.
 *
 * <p>Amounts are integers in the design's "essentia points" scale — same
 * units the Grimoire/infusion systems will draw against later. A sane range
 * is 1–8 for vanilla content; powerful magical items can go higher.
 */
public record AspectStack(Aspect aspect, int amount) {

    public AspectStack {
        if (amount <= 0) throw new IllegalArgumentException("AspectStack amount must be positive, got " + amount);
        if (aspect == null) throw new IllegalArgumentException("AspectStack aspect must not be null");
    }

    public static AspectStack of(Aspect aspect, int amount) {
        return new AspectStack(aspect, amount);
    }
}
