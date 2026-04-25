package com.soul.soa_additions.tr.core;

import com.soul.soa_additions.tr.ThaumicRemnants;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The full Aetherium aspect tree — 48 aspects, registered in dependency order
 * (every compound's parents already exist before the compound is constructed).
 *
 * <p>The static initializer order matches the AspectTree.md table verbatim:
 * 6 primals, then tier-1 compounds (two primals each), tier-2, tier-3, tier-4.
 * Touching this class via {@link #bootstrap()} forces JVM class init and
 * populates {@link #BY_ID}.
 *
 * <p>Primal colors are explicit and roughly track the design doc's color words
 * ("light blue", "cyan", "red-orange", "green", "silver-white", "dark gray").
 * Compound aspects derive their color by averaging the two parent colors —
 * cheap, deterministic, and produces a visually-readable lineage; if a specific
 * compound needs a curated tint later, override via the {@link Aspect} ctor that
 * takes an explicit color.
 */
public final class Aspects {

    private static final Map<ResourceLocation, Aspect> BY_ID = new LinkedHashMap<>();

    private Aspects() {}

    private static Aspect primal(String path, String englishName, int color) {
        Aspect a = new Aspect(new ResourceLocation(ThaumicRemnants.MODID, path), englishName, color);
        BY_ID.put(a.id(), a);
        return a;
    }

    private static Aspect compound(String path, String englishName, Aspect parentA, Aspect parentB) {
        Aspect a = new Aspect(new ResourceLocation(ThaumicRemnants.MODID, path), englishName, parentA, parentB);
        BY_ID.put(a.id(), a);
        return a;
    }

    // ===== Primals (6) =====
    public static final Aspect AER      = primal("aer",      "Air",     0xA0D0FF);
    public static final Aspect AQUA     = primal("aqua",     "Water",   0x33B5E5);
    public static final Aspect IGNIS    = primal("ignis",    "Fire",    0xFF5A01);
    public static final Aspect TERRA    = primal("terra",    "Earth",   0x56C00C);
    public static final Aspect ORDO     = primal("ordo",     "Order",   0xE0E0F0);
    public static final Aspect PERDITIO = primal("perditio", "Entropy", 0x404040);

    // ===== Tier 1 — two primals (10) =====
    public static final Aspect VACUOS     = compound("vacuos",     "Void",     AER,      PERDITIO);
    public static final Aspect LUX        = compound("lux",        "Light",    AER,      IGNIS);
    public static final Aspect POTENTIA   = compound("potentia",   "Power",    ORDO,     IGNIS);
    public static final Aspect MOTUS      = compound("motus",      "Motion",   AER,      ORDO);
    public static final Aspect GELUM      = compound("gelum",      "Cold",     IGNIS,    PERDITIO);
    public static final Aspect VITREUS    = compound("vitreus",    "Crystal",  TERRA,    ORDO);
    public static final Aspect VICTUS     = compound("victus",     "Life",     AQUA,     TERRA);
    public static final Aspect VENENUM    = compound("venenum",    "Poison",   AQUA,     PERDITIO);
    public static final Aspect PERMUTATIO = compound("permutatio", "Exchange", PERDITIO, ORDO);
    public static final Aspect TEMPESTAS  = compound("tempestas",  "Weather",  AER,      AQUA);

    // ===== Tier 2 — primal + tier-1, or two tier-1s (14) =====
    public static final Aspect METALLUM      = compound("metallum",      "Metal",   TERRA,         VITREUS);
    public static final Aspect MORTUUS       = compound("mortuus",       "Death",   VICTUS,        PERDITIO);
    public static final Aspect VOLATUS       = compound("volatus",       "Flight",  AER,           MOTUS);
    public static final Aspect TENEBRAE      = compound("tenebrae",      "Darkness",VACUOS,        LUX);
    public static final Aspect SPIRITUS      = compound("spiritus",      "Spirit",  VICTUS,        MORTUUS);
    public static final Aspect SANO          = compound("sano",          "Healing", ORDO,          VICTUS);
    public static final Aspect ITER          = compound("iter",          "Journey", MOTUS,         TERRA);
    public static final Aspect PRAECANTATIO  = compound("praecantatio",  "Magic",   VACUOS,        POTENTIA);
    public static final Aspect HERBA         = compound("herba",         "Plant",   VICTUS,        TERRA);
    public static final Aspect LIMUS         = compound("limus",         "Slime",   VICTUS,        AQUA);
    public static final Aspect BESTIA        = compound("bestia",        "Beast",   MOTUS,         VICTUS);
    public static final Aspect FAMES         = compound("fames",         "Hunger",  VICTUS,        VACUOS);
    public static final Aspect VINCULUM      = compound("vinculum",      "Trap",    MOTUS,         PERDITIO);
    public static final Aspect ALIENIS       = compound("alienis",       "Eldritch",VACUOS,        TENEBRAE);

    // ===== Tier 3 — higher compounds (11) =====
    public static final Aspect AURAM    = compound("auram",    "Aura",    PRAECANTATIO, AER);
    public static final Aspect VITIUM   = compound("vitium",   "Taint",   PRAECANTATIO, PERDITIO);
    public static final Aspect ARBOR    = compound("arbor",    "Tree",    AER,          HERBA);
    public static final Aspect CORPUS   = compound("corpus",   "Flesh",   MORTUUS,      BESTIA);
    public static final Aspect EXANIMIS = compound("exanimis", "Undead",  MOTUS,        MORTUUS);
    public static final Aspect COGNITIO = compound("cognitio", "Mind",    IGNIS,        SPIRITUS);
    public static final Aspect SENSUS   = compound("sensus",   "Sense",   AER,          SPIRITUS);
    public static final Aspect HUMANUS  = compound("humanus",  "Human",   BESTIA,       COGNITIO);
    public static final Aspect MESSIS   = compound("messis",   "Crop",    HERBA,        HUMANUS);
    public static final Aspect LUCRUM   = compound("lucrum",   "Greed",   HUMANUS,      FAMES);
    public static final Aspect PERFODIO = compound("perfodio", "Mining",  HUMANUS,      TERRA);

    // ===== Tier 4 — highest compounds (7) =====
    public static final Aspect INSTRUMENTUM = compound("instrumentum", "Tool",    HUMANUS,      ORDO);
    public static final Aspect FABRICA      = compound("fabrica",      "Craft",   HUMANUS,      INSTRUMENTUM);
    public static final Aspect MACHINA      = compound("machina",      "Machine", MOTUS,        INSTRUMENTUM);
    public static final Aspect TELUM        = compound("telum",        "Weapon",  INSTRUMENTUM, IGNIS);
    public static final Aspect TUTAMEN      = compound("tutamen",      "Armor",   INSTRUMENTUM, TERRA);
    public static final Aspect PANNUS       = compound("pannus",       "Cloth",   INSTRUMENTUM, BESTIA);
    public static final Aspect METO         = compound("meto",         "Harvest", MESSIS,       INSTRUMENTUM);

    /** All 48 aspects in registration order (primals first, compounds in tier
     *  order). Iteration order is stable thanks to LinkedHashMap. */
    public static Collection<Aspect> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static Aspect byId(ResourceLocation id) {
        Aspect a = BY_ID.get(id);
        if (a == null) throw new IllegalArgumentException("Unknown aspect: " + id);
        return a;
    }

    /** Force class-load of {@link Aspects} so the static fields populate
     *  {@link #BY_ID} before anything else (item registration, etc.) reads it. */
    public static void bootstrap() {}
}
