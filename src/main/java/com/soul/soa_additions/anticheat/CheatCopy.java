package com.soul.soa_additions.anticheat;

/**
 * Player-facing wording shared by every surface that reports a detection.
 *
 * <p>The disconnect reason, the blocked screen and the singleplayer choice
 * screen all make the same two promises, so each is written once. Three copies
 * of a sentence about what happens to your save is three chances for one of
 * them to quietly stop being true.
 *
 * <p>Written for a player who has never heard of a saved-data backend or an
 * advancement. What matters to them is whether their world is ruined, not which
 * five files record that it is.
 */
public final class CheatCopy {

    private CheatCopy() {}

    /** What "nothing is recorded" means, in one line. */
    public static final String NOTHING_RECORDED =
            "Nothing is saved and nothing is sent anywhere.";

    /** What happens if they keep the cheat instead. */
    public static final String CHEATER_WORLD =
            "The modpack can no longer be completed on it, and servers can see the mark.";

    /** The mistake everyone makes — the pack selector does not delete anything. */
    public static final String DELETE_NOT_DISABLE =
            "Turning it off in the pack selector is not enough — delete it.";

    /** Where to go looking. A detection can be a resource pack or a mod, so name both. */
    public static final String FILE_LOCATION =
            "It will be in your instance's resourcepacks or mods folder.";
}
