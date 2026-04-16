package com.soul.soa_additions.quest;

/**
 * Single-slot holder for a pack mode selected on the create-world screen.
 * The client writes into this from {@code CreateWorldPackModeButton}; the
 * server consumes it on first access in {@link PackModeData#get} so the
 * brand-new world starts in the chosen mode instead of the default ADVENTURE.
 *
 * <p>Common class (not client-only) so {@code PackModeData} can consume it
 * without a dist-split import. Dedicated servers simply never set the field.</p>
 */
public final class PendingPackMode {

    private static volatile PackMode pending;

    private PendingPackMode() {}

    public static void set(PackMode mode) {
        pending = mode;
    }

    public static PackMode get() {
        return pending;
    }

    /** Return the pending mode and clear it atomically. */
    public static PackMode consume() {
        PackMode p = pending;
        pending = null;
        return p;
    }
}
