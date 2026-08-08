package com.soul.soa_additions.smithery;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.material.MaterialStats;
import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.content.SmitheryPartTypes;
import com.soul.smithery.content.SmitheryToolTypes;
import com.soul.smithery.api.tool.ToolType;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public final class SoaSmitheryMaterials {

    private static final Logger LOG = LogManager.getLogger("SOA-SmitheryMaterials");

    public static ResourceLocation ABYSSALNITE;
    public static ResourceLocation ABYSSUM;
    public static ResourceLocation ADAMANT;
    public static ResourceLocation ADAMINITE;
    public static ResourceLocation ADVANCED_ALLOY;
    public static ResourceLocation AEONSTEEL;
    public static ResourceLocation AEROITE;
    public static ResourceLocation AETHIUM;
    public static ResourceLocation SILENTGEMSAGATE;
    public static ResourceLocation SILENTGEMSAGATE_SUPER;
    public static ResourceLocation SILENTGEMSALEXANDRITE;
    public static ResourceLocation SILENTGEMSALEXANDRITE_SUPER;
    public static ResourceLocation ALPHA_FUR;
    public static ResourceLocation ALUBRASS;
    public static ResourceLocation ALUMINIUM;
    public static ResourceLocation ALUMITE;
    public static ResourceLocation AMBER;
    public static ResourceLocation SILENTGEMSAMBER;
    public static ResourceLocation SILENTGEMSAMBER_SUPER;
    public static ResourceLocation SILENTGEMSAMETHYST;
    public static ResourceLocation SILENTGEMSAMETHYST_SUPER;
    public static ResourceLocation SILENTGEMSAMETRINE;
    public static ResourceLocation SILENTGEMSAMETRINE_SUPER;
    public static ResourceLocation SILENTGEMSAMMOLITE;
    public static ResourceLocation SILENTGEMSAMMOLITE_SUPER;
    public static ResourceLocation FEATHERAMPHITHERE;
    public static ResourceLocation APATITE;
    public static ResourceLocation SILENTGEMSAPATITE;
    public static ResourceLocation SILENTGEMSAPATITE_SUPER;
    public static ResourceLocation AQUALITE;
    public static ResourceLocation AQUAMARINE;
    public static ResourceLocation SILENTGEMSAQUAMARINE;
    public static ResourceLocation SILENTGEMSAQUAMARINE_SUPER;
    public static ResourceLocation ARDITE;
    public static ResourceLocation SAPPHIRE_AOA;
    public static ResourceLocation ASGARDIUM;
    public static ResourceLocation ASTRAL_METAL;
    public static ResourceLocation ASTRIUM;
    public static ResourceLocation AURORIUM;
    public static ResourceLocation AWAKENED_PLUSTIC;
    public static ResourceLocation BARONYTE;
    // GC TCon entity-melting fluids (cow->milk, enderman->ender, snowman->water)
    public static ResourceLocation MILK;
    public static ResourceLocation ENDER;
    public static ResourceLocation WATER;
    public static ResourceLocation BASALT;
    public static ResourceLocation MA_BASE_ESSENCE;
    public static ResourceLocation SILENTGEMSBERYL;
    public static ResourceLocation SILENTGEMSBERYL_SUPER;
    public static ResourceLocation SILENTGEMSBLACK_DIAMOND;
    public static ResourceLocation SILENTGEMSBLACK_DIAMOND_SUPER;
    public static ResourceLocation BLACKQUARTZ_PLUSTIC;
    public static ResourceLocation BLACK_QUARTZ;
    public static ResourceLocation BLAZIUM;
    public static ResourceLocation BLOOD_INFUSED_IRON;
    public static ResourceLocation BLOOD_INFUSED_WOOD;
    public static ResourceLocation BLOODSTONE;
    public static ResourceLocation BLOODWOOD_PLUSTIC;
    public static ResourceLocation BLOODWOOD;
    public static ResourceLocation AERCLOUD_BLUE;
    public static ResourceLocation SCALEBLUEDRAGON;
    public static ResourceLocation SCALEBLUESEASERPENT;
    public static ResourceLocation BLUESLIME;
    public static ResourceLocation SILENTGEMSBLUE_TOPAZ;
    public static ResourceLocation SILENTGEMSBLUE_TOPAZ_SUPER;
    public static ResourceLocation BONE;
    public static ResourceLocation BOUND_METAL;
    public static ResourceLocation BRONZE;
    public static ResourceLocation SCALEBRONZEDRAGON;
    public static ResourceLocation SCALEBRONZESEASERPENT;
    public static ResourceLocation CHITINBROWNDEATHWORM;
    public static ResourceLocation CACTUS;
    public static ResourceLocation CANDY_CANE;
    public static ResourceLocation CARBON_FIBER;
    public static ResourceLocation SILENTGEMSCARNELIAN;
    public static ResourceLocation SILENTGEMSCARNELIAN_SUPER;
    public static ResourceLocation SILENTGEMSCATS_EYE;
    public static ResourceLocation SILENTGEMSCATS_EYE_SUPER;
    public static ResourceLocation CERTUSQUARTZ_PLUSTIC;
    public static ResourceLocation CERTUS_QUARTZ;
    public static ResourceLocation CHAOTIC_PLUSTIC;
    public static ResourceLocation CHAOTIC_METAL;
    public static ResourceLocation CHARGER;
    public static ResourceLocation CHEESE;
    public static ResourceLocation CHOCOLATE;
    public static ResourceLocation CHROMASTEEL;
    public static ResourceLocation CHROMIUM;
    public static ResourceLocation SILENTGEMSCHRYSOPRASE;
    public static ResourceLocation SILENTGEMSCHRYSOPRASE_SUPER;
    public static ResourceLocation CINCINNASITE;
    public static ResourceLocation SILENTGEMSCITRINE;
    public static ResourceLocation SILENTGEMSCITRINE_SUPER;
    public static ResourceLocation CLOUD;
    public static ResourceLocation COAGULATED_BLOOD_SAND;
    public static ResourceLocation COAL;
    public static ResourceLocation FUSION_MATRIX;
    public static ResourceLocation COBALT;
    public static ResourceLocation AERCLOUD_COLD;
    public static ResourceLocation CONDUCTIVE_IRON;
    public static ResourceLocation CONSTANTAN;
    public static ResourceLocation SILENTGEMSCORAL;
    public static ResourceLocation SILENTGEMSCORAL_SUPER;
    public static ResourceLocation CORRUPTED;
    public static ResourceLocation CORUNDUM;
    public static ResourceLocation COSMILITE;
    public static ResourceLocation CRIMSONITE;
    public static ResourceLocation CRYONIUM;
    public static ResourceLocation CRYSTAL_LEAF;
    public static ResourceLocation CRYSTAL_MATRIX;
    public static ResourceLocation CRYSTALLITE;
    public static ResourceLocation CYTOSINITE;
    public static ResourceLocation DARKMATTER;
    public static ResourceLocation DARK_MATTER;
    public static ResourceLocation DARK_STEEL;
    public static ResourceLocation DARKWOOD_PLUSTIC;
    public static ResourceLocation DARKWOOD;
    public static ResourceLocation SCALEDEEPBLUESEASERPENT;
    public static ResourceLocation XU_DEMONIC_METAL;
    public static ResourceLocation DESERT_MYRMEX;
    public static ResourceLocation CHITINDESERTMYRMEX;
    public static ResourceLocation DESH;
    public static ResourceLocation DIAMATINE_ACTADD_PLUSTIC;
    public static ResourceLocation DIAMANTINE_CRYSTAL;
    public static ResourceLocation DILITHIUM;
    public static ResourceLocation DIMENSIONAL_SHARD;
    public static ResourceLocation DOGBEARIUM;
    public static ResourceLocation DRACONIC_METAL;
    public static ResourceLocation DRACONIUM;
    public static ResourceLocation BONEDRAGON;
    public static ResourceLocation DRAGONBONE;
    public static ResourceLocation DRAGONSTONE;
    public static ResourceLocation DREADIUM;
    public static ResourceLocation DREAMWOOD;
    public static ResourceLocation DURANITE;
    public static ResourceLocation DURASTEEL;
    public static ResourceLocation DYONITE;
    public static ResourceLocation EEZO;
    public static ResourceLocation ELECANIUM;
    public static ResourceLocation ELECTRICAL_STEEL;
    public static ResourceLocation ELECTRONIUM;
    public static ResourceLocation ELECTRUM;
    public static ResourceLocation ELEMENTIUM;
    public static ResourceLocation EMBERSTONE;
    public static ResourceLocation EMERADIC_ACTADD_PLUSTIC;
    public static ResourceLocation EMERALDIC_CRYSTAL;
    public static ResourceLocation EMERALD_PLUSTIC;
    public static ResourceLocation XU_ENCHANTED_METAL;
    public static ResourceLocation ENDSTONE;
    public static ResourceLocation END_STEEL;
    public static ResourceLocation ENDER_BIOTITE;
    public static ResourceLocation ENDER_CRYSTAL;
    public static ResourceLocation ENDER_DRAGON_SQUAMA;
    public static ResourceLocation ENDERIUM_PLUSTIC;
    public static ResourceLocation ENDERIUM;
    public static ResourceLocation ENDORIUM;
    public static ResourceLocation ENDROD;
    public static ResourceLocation ENERGETIC_ALLOY;
    public static ResourceLocation ENERGETIC_METAL;
    public static ResourceLocation ENORI_ACTADD_PLUSTIC;
    public static ResourceLocation ENORI_CRYSTAL;
    public static ResourceLocation ERODIUM;
    public static ResourceLocation ESSENCE_METAL;
    public static ResourceLocation ETHAXIUM;
    public static ResourceLocation XU_EVIL_METAL;
    public static ResourceLocation FEATHER;
    public static ResourceLocation FIERYMETAL;
    public static ResourceLocation FIERY;
    public static ResourceLocation DRAGONSTEEL_FIRE;
    public static ResourceLocation FIREWOOD;
    public static ResourceLocation FLAMESTRING_PLUSTIC;
    public static ResourceLocation FLUIX;
    public static ResourceLocation FLUIXCRYSTAL_PLUSTIC;
    public static ResourceLocation FLUIX_STEEL;
    public static ResourceLocation SILENTGEMSFLUORITE;
    public static ResourceLocation SILENTGEMSFLUORITE_SUPER;
    public static ResourceLocation FLUX_CRYSTAL;
    public static ResourceLocation FLUXED_STRING;
    public static ResourceLocation FLUXED_ELECTRUM;
    public static ResourceLocation LEATHERFORESTTROLL;
    public static ResourceLocation FRACTUM;
    public static ResourceLocation FRISCION;
    public static ResourceLocation LEATHERFROSTTROLL;
    public static ResourceLocation FROSTSTEEL;
    public static ResourceLocation FULGURITE;
    public static ResourceLocation FUSEWOOD_PLUSTIC;
    public static ResourceLocation FUSEWOOD;
    public static ResourceLocation GAIA;
    public static ResourceLocation GAIASTEEL;
    public static ResourceLocation GARFAX;
    public static ResourceLocation SILENTGEMSGARNET;
    public static ResourceLocation SILENTGEMSGARNET_SUPER;
    public static ResourceLocation GELID_ENDERIUM;
    public static ResourceLocation GELID_GEM;
    public static ResourceLocation GHASTLY;
    public static ResourceLocation GHOSTLYSTONE;
    public static ResourceLocation GHOSTWOOD_PLUSTIC;
    public static ResourceLocation GHOSTWOOD;
    public static ResourceLocation GHOULISH;
    public static ResourceLocation AERCLOUD_GOLD;
    public static ResourceLocation GOLDEN_AMBER;
    public static ResourceLocation SILENTGEMSGOLDEN_BERYL;
    public static ResourceLocation SILENTGEMSGOLDEN_BERYL_SUPER;
    public static ResourceLocation GOLDEN_FEATHER;
    public static ResourceLocation GOLDEN_OAK_LEAF;
    public static ResourceLocation GRAIN;
    public static ResourceLocation GRAVITITE;
    public static ResourceLocation SCALEGRAYDRAGON;
    public static ResourceLocation SCALEGREENDRAGON;
    public static ResourceLocation SCALEGREENSEASERPENT;
    public static ResourceLocation GUARDIANSCALE;
    public static ResourceLocation HARD_SPIDER_LEG;
    public static ResourceLocation POLYETHYLENE;
    public static ResourceLocation HEART_CRYSTAL;
    public static ResourceLocation SILENTGEMSHELIODOR;
    public static ResourceLocation SILENTGEMSHELIODOR_SUPER;
    public static ResourceLocation HEPHAESTITE;
    public static ResourceLocation STRONG_TOFU_GEM;
    public static ResourceLocation HOLIDAY_LEAF;
    public static ResourceLocation HOLYSTONE;
    public static ResourceLocation ICE;
    public static ResourceLocation DRAGONSTEEL_ICE;
    public static ResourceLocation ICESTONE;
    public static ResourceLocation IGNITZ;
    public static ResourceLocation IMPEROMITE;
    public static ResourceLocation SILENTGEMSINDICOLITE;
    public static ResourceLocation SILENTGEMSINDICOLITE_SUPER;
    public static ResourceLocation INERT_WITHERIUM;
    public static ResourceLocation MA_INFERIUM;
    public static ResourceLocation INFERNIUM;
    public static ResourceLocation INFINITY_AVARITIA_PLUSTIC;
    public static ResourceLocation INFINITY_METAL;
    public static ResourceLocation INSANIUM;
    public static ResourceLocation MA_INTERMEDIUM;
    public static ResourceLocation INVAR;
    public static ResourceLocation SILENTGEMSIOLITE;
    public static ResourceLocation SILENTGEMSIOLITE_SUPER;
    public static ResourceLocation IONITE;
    public static ResourceLocation IOX;
    public static ResourceLocation IRIDIUM;
    public static ResourceLocation CONSTRUCTION_ALLOY;
    public static ResourceLocation JADE;
    public static ResourceLocation SILENTGEMSJADE;
    public static ResourceLocation SILENTGEMSJADE_SUPER;
    public static ResourceLocation SILENTGEMSJASPER;
    public static ResourceLocation SILENTGEMSJASPER_SUPER;
    public static ResourceLocation JAUXUM;
    public static ResourceLocation JUNGLE_MYRMEX;
    public static ResourceLocation CHITINJUNGLEMYRMEX;
    public static ResourceLocation KARMESINE;
    public static ResourceLocation KELLINE;
    public static ResourceLocation KNIGHTMETAL;
    public static ResourceLocation EXPERIENCE;
    public static ResourceLocation SILENTGEMSKUNZITE;
    public static ResourceLocation SILENTGEMSKUNZITE_SUPER;
    public static ResourceLocation SILENTGEMSKYANITE;
    public static ResourceLocation SILENTGEMSKYANITE_SUPER;
    public static ResourceLocation KYRONITE;
    public static ResourceLocation LANDIUM;
    public static ResourceLocation LEAD;
    public static ResourceLocation LEAF;
    public static ResourceLocation SILENTGEMSLEPIDOLITE;
    public static ResourceLocation SILENTGEMSLEPIDOLITE_SUPER;
    public static ResourceLocation LIMONITE;
    public static ResourceLocation LITHERITE;
    public static ResourceLocation LIVINGROCK;
    public static ResourceLocation LIVINGWOOD_PLUSTIC;
    public static ResourceLocation LIVINGWOOD;
    public static ResourceLocation LONSDALEITE;
    public static ResourceLocation LUMIUM_PLUSTIC;
    public static ResourceLocation LUMIUM;
    public static ResourceLocation LUMIX;
    public static ResourceLocation LUNAR;
    public static ResourceLocation LYON;
    public static ResourceLocation XU_MAGICAL_WOOD;
    public static ResourceLocation MAGMA;
    public static ResourceLocation MAGMASLIME;
    public static ResourceLocation MALACHITE_GEM;
    public static ResourceLocation SILENTGEMSMALACHITE;
    public static ResourceLocation SILENTGEMSMALACHITE_SUPER;
    public static ResourceLocation MANA_DIAMOND;
    public static ResourceLocation MANA_PEARL;
    public static ResourceLocation MANA_STRING;
    public static ResourceLocation MANASTEEL;
    public static ResourceLocation MANGANESE;
    public static ResourceLocation MANGANESE_STEEL;
    public static ResourceLocation MANYULLYN;
    public static ResourceLocation SLIMELEAF_ORANGE;
    public static ResourceLocation SLIMELEAF_PURPLE;
    public static ResourceLocation SLIMEVINE_ORANGE;
    public static ResourceLocation SLIMEVINE_PURPLE;
    public static ResourceLocation MEAT_METAL;
    public static ResourceLocation METEOR;
    public static ResourceLocation METEORITE;
    public static ResourceLocation MICA;
    public static ResourceLocation MIRION;
    public static ResourceLocation MITHMINITE;
    public static ResourceLocation MITHRIL;
    public static ResourceLocation MITHRILLIUM;
    public static ResourceLocation MODULARIUM;
    public static ResourceLocation SILENTGEMSMOLDAVITE;
    public static ResourceLocation SILENTGEMSMOLDAVITE_SUPER;
    public static ResourceLocation SILENTGEMSMOONSTONE;
    public static ResourceLocation SILENTGEMSMOONSTONE_SUPER;
    public static ResourceLocation MORGANINE;
    public static ResourceLocation SILENTGEMSMORGANITE;
    public static ResourceLocation SILENTGEMSMORGANITE_SUPER;
    public static ResourceLocation LEATHERMOUNTAINTROLL;
    public static ResourceLocation MUD;
    public static ResourceLocation MYSTITE;
    public static ResourceLocation NAGASCALE;
    public static ResourceLocation NETHERRACK;
    public static ResourceLocation NEUTRONIUM;
    public static ResourceLocation NICKEL;
    public static ResourceLocation NIHILITE;
    public static ResourceLocation NIOB;
    public static ResourceLocation NITRONITE;
    public static ResourceLocation NUCLEUM;
    public static ResourceLocation NYLON_CLOTH;
    public static ResourceLocation NYLON_STRING;
    public static ResourceLocation OBSIDIAN;
    public static ResourceLocation OBDISIAN;
    public static ResourceLocation OBSIDIORITE;
    public static ResourceLocation SILENTGEMSONYX;
    public static ResourceLocation SILENTGEMSONYX_SUPER;
    public static ResourceLocation SILENTGEMSOPAL;
    public static ResourceLocation SILENTGEMSOPAL_SUPER;
    public static ResourceLocation ORICHALCOS;
    public static ResourceLocation ORICHALCUM;
    public static ResourceLocation OSGLOGLAS;
    public static ResourceLocation OSMIRIDIUM;
    public static ResourceLocation OSMIUM;
    public static ResourceLocation OSRAM;
    public static ResourceLocation OVIUM;
    public static ResourceLocation PALIS_ACTADD_PLUSTIC;
    public static ResourceLocation PALIS_CRYSTAL;
    public static ResourceLocation PALLADIUM;
    public static ResourceLocation PANDORIUM;
    public static ResourceLocation PAPER;
    public static ResourceLocation SILENTGEMSPEARL;
    public static ResourceLocation SILENTGEMSPEARL_SUPER;
    public static ResourceLocation PERFECT;
    public static ResourceLocation PERIDOT;
    public static ResourceLocation SILENTGEMSPERIDOT;
    public static ResourceLocation SILENTGEMSPERIDOT_SUPER;
    public static ResourceLocation PHOENIXITE;
    public static ResourceLocation PIGIRON;
    public static ResourceLocation PINK_METAL;
    public static ResourceLocation PINK_SLIME;
    public static ResourceLocation PLADIUM;
    public static ResourceLocation PLATINUM_PLUSTIC;
    public static ResourceLocation PLATINUM;
    public static ResourceLocation POOP;
    public static ResourceLocation PRIMAL_METAL;
    public static ResourceLocation PROMETHEUM;
    public static ResourceLocation MA_PROSPERITY;
    public static ResourceLocation PROTONIUM;
    public static ResourceLocation PROUSTITE;
    public static ResourceLocation PROXII;
    public static ResourceLocation MA_PRUDENTIUM;
    public static ResourceLocation PSIGEM;
    public static ResourceLocation PSIMETAL;
    public static ResourceLocation PULSATING_CRYSTAL;
    public static ResourceLocation PULSATING_IRON;
    public static ResourceLocation PUMPKIN;
    public static ResourceLocation SCALEPURPLESEASERPENT;
    public static ResourceLocation SILENTGEMSPYROPE;
    public static ResourceLocation SILENTGEMSPYROPE_SUPER;
    public static ResourceLocation QUEENSGOLD;
    public static ResourceLocation QUICKSILVER;
    public static ResourceLocation RACHELINE;
    public static ResourceLocation RAVAGING;
    public static ResourceLocation RAVEN_FEATHER;
    public static ResourceLocation SCALEREDDRAGON;
    public static ResourceLocation REDMATTER;
    public static ResourceLocation RED_MATTER;
    public static ResourceLocation SCALEREDSEASERPENT;
    public static ResourceLocation REDINS;
    public static ResourceLocation REDSTONE_ALLOY;
    public static ResourceLocation REED;
    public static ResourceLocation REFINED_CORALIUM;
    public static ResourceLocation REFINEDGLOWSTONE;
    public static ResourceLocation REFINED_GLOWSTONE;
    public static ResourceLocation REFINEDOBSIDIAN;
    public static ResourceLocation REFINED_OBSIDIAN;
    public static ResourceLocation REMORSEFUL;
    public static ResourceLocation RESTONIA_ACTADD_PLUSTIC;
    public static ResourceLocation RESTONIA_CRYSTAL;
    public static ResourceLocation RIME;
    public static ResourceLocation SILENTGEMSROSE_QUARTZ;
    public static ResourceLocation SILENTGEMSROSE_QUARTZ_SUPER;
    public static ResourceLocation ROSEGOLD;
    public static ResourceLocation ROSITE;
    public static ResourceLocation RUBBER;
    public static ResourceLocation RUBBER_BAND;
    public static ResourceLocation RUBY;
    public static ResourceLocation SILENTGEMSRUBY;
    public static ResourceLocation SILENTGEMSRUBY_SUPER;
    public static ResourceLocation SAKURA_DIAMOND;
    public static ResourceLocation SAPPHIRE;
    public static ResourceLocation SILENTGEMSSAPPHIRE;
    public static ResourceLocation SILENTGEMSSAPPHIRE_SUPER;
    public static ResourceLocation SCALESAPPHIREDRAGON;
    public static ResourceLocation SCARLITE;
    public static ResourceLocation SCORCHED;
    public static ResourceLocation SEARED;
    public static ResourceLocation SEISMUM;
    public static ResourceLocation SENTIENT_METAL;
    public static ResourceLocation SHADOWIUM;
    public static ResourceLocation SCALESHINY;
    public static ResourceLocation SHYRESTONE;
    public static ResourceLocation SIGNALUM_PLUSTIC;
    public static ResourceLocation SIGNALUM;
    public static ResourceLocation SILVER;
    public static ResourceLocation SCALESILVERDRAGON;
    public static ResourceLocation SINISTERIUM;
    public static ResourceLocation SKELETAL;
    public static ResourceLocation SKY_STONE;
    public static ResourceLocation SKYROOT;
    public static ResourceLocation SKYROOT_LEAF;
    public static ResourceLocation SLIMELEAF_BLUE;
    public static ResourceLocation SLIMEVINE_BLUE;
    public static ResourceLocation SILENTGEMSSODALITE;
    public static ResourceLocation SILENTGEMSSODALITE_SUPER;
    public static ResourceLocation SOLARIUM;
    public static ResourceLocation SOULARIUM;
    public static ResourceLocation MA_SOULIUM;
    public static ResourceLocation SPECTRE;
    public static ResourceLocation SPECTRE_STRING;
    public static ResourceLocation SPIDER_LEG;
    public static ResourceLocation SILENTGEMSSPINEL;
    public static ResourceLocation SILENTGEMSSPINEL_SUPER;
    public static ResourceLocation SPONGE;
    public static ResourceLocation STAINLESS_STEEL;
    public static ResourceLocation STARMETAL;
    public static ResourceLocation STEEL;
    public static ResourceLocation STEELEAF;
    public static ResourceLocation STELLAR_ALLOY;
    public static ResourceLocation STELLAR_ALLOY_GC;
    public static ResourceLocation STELLARIUM;
    public static ResourceLocation STELLARIUM_OBSIDIAN;
    public static ResourceLocation FEATHERSTYMPHALIANBIRD;
    public static ResourceLocation SUNNARIUM;
    public static ResourceLocation SILENTGEMSSUNSTONE;
    public static ResourceLocation SILENTGEMSSUNSTONE_SUPER;
    public static ResourceLocation MA_SUPERIUM;
    public static ResourceLocation MA_SUPREMIUM;
    public static ResourceLocation SWET;
    public static ResourceLocation CHITINTANDEATHWORM;
    public static ResourceLocation TANZANITE;
    public static ResourceLocation SILENTGEMSTANZANITE;
    public static ResourceLocation SILENTGEMSTANZANITE_SUPER;
    public static ResourceLocation SCALETEALSEASERPENT;
    public static ResourceLocation SILENTGEMSTEKTITE;
    public static ResourceLocation SILENTGEMSTEKTITE_SUPER;
    public static ResourceLocation TERRA_ALLOY;
    public static ResourceLocation TERRASTEEL;
    public static ResourceLocation TERRAX;
    public static ResourceLocation TERRESTRIAL;
    public static ResourceLocation THAUMIUM;
    public static ResourceLocation THORIUM;
    public static ResourceLocation TIBERIUM;
    public static ResourceLocation TIN;
    public static ResourceLocation TITANIUM;
    public static ResourceLocation TOFU_GEM;
    public static ResourceLocation TOFUDIAMOND;
    public static ResourceLocation TOFUMETAL;
    public static ResourceLocation TOPAZ;
    public static ResourceLocation SILENTGEMSTOPAZ;
    public static ResourceLocation SILENTGEMSTOPAZ_SUPER;
    public static ResourceLocation TREATEDWOOD;
    public static ResourceLocation TRIBERIUM;
    public static ResourceLocation TRITANIUM;
    public static ResourceLocation TRITONITE;
    public static ResourceLocation TUNGSTEN;
    public static ResourceLocation SILENTGEMSTURQUOISE;
    public static ResourceLocation SILENTGEMSTURQUOISE_SUPER;
    public static ResourceLocation UMBRIUM;
    public static ResourceLocation URU;
    public static ResourceLocation UNIVERSAL_METAL;
    public static ResourceLocation VALKYRIE;
    public static ResourceLocation VALYRIANSTEEL;
    public static ResourceLocation VALYRIUM;
    public static ResourceLocation VARSIUM;
    public static ResourceLocation VIBRANIUM;
    public static ResourceLocation VIBRANT_ALLOY;
    public static ResourceLocation VIBRANT_CRYSTAL;
    public static ResourceLocation VINE;
    public static ResourceLocation SILENTGEMSVIOLET_SAPPHIRE;
    public static ResourceLocation SILENTGEMSVIOLET_SAPPHIRE_SUPER;
    public static ResourceLocation VIOLIUM;
    public static ResourceLocation VOID_ACTADD_PLUSTIC;
    public static ResourceLocation VOID_CRYSTAL;
    public static ResourceLocation VOID_METAL;
    public static ResourceLocation WEATHER_CRYSTAL;
    public static ResourceLocation WEEZER;
    public static ResourceLocation CHITINWHITEDEATHWORM;
    public static ResourceLocation SCALEWHITEDRAGON;
    public static ResourceLocation WITHERBONE;
    public static ResourceLocation BONEWITHER;
    public static ResourceLocation XU_WITHERING;
    public static ResourceLocation WITHERIUM;
    public static ResourceLocation WOLFRAMIUM;
    public static ResourceLocation WYVERN_PLUSTIC;
    public static ResourceLocation WYVERN_METAL;
    public static ResourceLocation YELLORIUM;
    public static ResourceLocation YRDEEN;
    public static ResourceLocation ZANITE;
    public static ResourceLocation SILENTGEMSZIRCON;
    public static ResourceLocation SILENTGEMSZIRCON_SUPER;

    public static void register() {
        registerAbyssalcraft();
        registerTaiga();
        registerGreedycraftCustom();
        registerTinkersEvolution();
        registerPlustic();
        registerSilentsGems();
        registerTwilightForest();
        registerIceAndFireCompat();
        registerTinkersConstructBase();
        registerMysticalAgriculture();
        registerBloodMagic();
        registerTinkersAether();
        registerDraconicEvolution();
        registerEnderio();
        registerThermalFoundation();
        registerProjecteEe2();
        registerExtraUtilities();
        registerBotania();
        registerMekores();
        registerTofucraft();
        registerDefiledLands();
        registerMalumValoria();
        registerAlloyFluids();
        retuneBuiltinMaterials();
    }

    /**
     * GreedyCraft's tuning for the 14 materials Smithery already ships.
     *
     * <p>These were registered a second time under {@code soa_additions}, which put two
     * identical-looking part sets in the game for wood, stone, flint, iron, copper, gold, slime,
     * amethyst, blaze, diamond, netherite, prismarine, string and bedrock — one carrying
     * GreedyCraft's numbers, one carrying Smithery's, and no way for a player to tell which
     * blade they had picked up. The duplicates are gone; what GreedyCraft actually contributed
     * is layered onto the surviving {@code smithery:} material here.</p>
     *
     * <p>{@link SmitheryAPI#retuneMaterial} derives from the existing stats, so Smithery's own
     * traits and modifier slots survive a retune that only restates numbers — Magnetized on iron,
     * Bouncy on slime boots and the rest stay put. Only the materials below had anything worth
     * keeping: the other eight were strictly poorer copies (SOA's {@code string} in particular
     * generated 26 all-zero tool parts it had no business having) and simply went away.</p>
     */
    private static void retuneBuiltinMaterials() {
        // GreedyCraft endgame curve. Smithery's defaults are vanilla-tier, which on bedrock and
        // netherite is a several-fold nerf to the top of the progression.
        retune("bedrock", b -> binderSlots(b, 5)
                .harvestLevel(10).miningSpeed(30.4f).attackDamage(28.2f).durabilityPerIngot(8400)
                .meltingTemp(8000.0f).binderMultiplier(3.4f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_INFINITUM)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_INFINITUM), armorPieces())
                .armor(10000.0f, 58.2f, 666.0f, 30.0f, 6.0f, 666.0f));

        retune("netherite", b -> binderSlots(b, 5)
                .harvestLevel(6).miningSpeed(15.2f).attackDamage(12.8f).durabilityPerIngot(3600)
                .meltingTemp(3000.0f).binderMultiplier(3.6f)
                .addUniversalModifier(SoaSmitheryModifiers.ARIDICULOUS)
                .addUniversalModifier(SoaSmitheryModifiers.HELLISH)
                .addUniversalModifier(SoaSmitheryModifiers.RELIABLETRAIT)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INFERNAL_ARMOR), armorPieces())
                .armor(360.0f, 34.4f, 120.0f, 3.6f, 5.0f, 80.0f));

        retune("amethyst", b -> binderSlots(b, 4)
                .harvestLevel(4).miningSpeed(6.0f).attackDamage(8.0f).durabilityPerIngot(1100)
                .meltingTemp(2000.0f).binderMultiplier(1.5f)
                .addUniversalModifier(SoaSmitheryModifiers.APOCALYPSE)
                .armor(250.0f, 20.0f, 11.2f, 1.5f, 3.0f, 11.2f));

        retune("diamond", b -> binderSlots(b, 3)
                .harvestLevel(3).miningSpeed(6.2f).attackDamage(5.7f).durabilityPerIngot(800)
                .meltingTemp(1500.0f).binderMultiplier(1.3f)
                .addUniversalModifier(SoaSmitheryModifiers.CRYSTALTRAIT)
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .armor(90.0f, 16.0f, 30.0f, 1.3f, 1.5f, 20.0f));

        // GC gold is a glass cannon — fastest mining in the low tiers, almost no durability —
        // where Smithery's is a luck/XP material. Both trait sets survive; the numbers are GC's.
        retune("gold", b -> binderSlots(b, 2)
                .harvestLevel(2).miningSpeed(20.5f).attackDamage(6.4f).durabilityPerIngot(20)
                .meltingTemp(1000.0f).binderMultiplier(0.2f)
                .addUniversalModifier(SoaSmitheryModifiers.GAMBLE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1))
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GAMBLE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1,
                        java.util.Map.of("bonus_slots", 1)), armorPieces())
                .armor(40.0f, 18.0f, 8.0f, 0.2f, 0.75f, 10.0f));

        // Traits only — Smithery's stat blocks for these two are equal or better, and its slime
        // is the one the bowstring allow-list knows about.
        retune("prismarine", b -> b
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUASPEED_ARMOR), armorPieces()));
        retune("slime", b -> b
                .addUniversalModifier(SoaSmitheryModifiers.SLIMEY));

        // Knightslime was the same duplication one name removed: Smithery's slimeknightium — which
        // its own alloy file calls a tribute alloy — already carried GC's knightslime numbers
        // exactly (harvest 3, 5.8 speed, 5.1 damage, 850/ingot, 0.5 binder, 3 slots). Only the melt
        // temperature and the trait pair differed, so those are all that need layering. Smithery's
        // Bouncy-on-boots and its armour stat block survive the retune; SOA's knightslime defined
        // neither, so merging into slimeknightium strictly gains armour support.
        retune("slimeknightium", b -> b
                .meltingTemp(1500.0f)
                .addUniversalModifier(SoaSmitheryModifiers.UNNATURAL)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CRUMBLING),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD,
                        SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD,
                        SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD));
    }

    /** Retunes a {@code smithery:} material, warning rather than failing silently if it is absent. */
    private static void retune(String path, java.util.function.UnaryOperator<MaterialStats.Builder> edit) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("smithery", path);
        if (!SmitheryAPI.retuneMaterial(id, edit)) {
            LOG.warn("Cannot retune {} - Smithery does not register it (renamed or removed?)", id);
        }
    }

    /**
     * Malum + Valoria metals — SOA originals (neither mod existed in 1.12 GC, so
     * stats are first-pass mid/late-game placements, traits themed per material).
     * soul_stained_steel carries {@code soul_stained}: Smithery tools of it harvest
     * arcane spirits on kill like Malum's own scythes.
     */
    private static void registerMalumValoria() {
        malumValoria("soul_stained_steel", 3, 7.5f, 6.5f, 950, 950.0f, 0xFF4E4867, 4,
                SoaSmitheryModifiers.SOUL_STAINED);
        malumValoria("hallowed_gold",      2, 7.0f, 4.0f, 500, 800.0f, 0xFFE7BD64, 3,
                SoaSmitheryModifiers.ARCANE);
        malumValoria("cthonic_gold",       3, 8.0f, 6.0f, 650, 1000.0f, 0xFF8A6B3C, 3,
                SoaSmitheryModifiers.DARK);
        malumValoria("malignant_pewter",   3, 6.5f, 7.0f, 1100, 900.0f, 0xFF5B4A6B, 3,
                SoaSmitheryModifiers.CURSED);
        malumValoria("soulstone",          2, 5.0f, 5.5f, 400, 700.0f, 0xFF9BB2D4, 3,
                SoaSmitheryModifiers.SOULEATER);
        malumValoria("blazing_quartz",     2, 7.0f, 5.0f, 350, 1100.0f, 0xFFE8663B, 3,
                SoaSmitheryModifiers.MELTING);

        malumValoria("ancient",     4, 9.0f, 8.0f, 1300, 1400.0f, 0xFFB8A88A, 5,
                SoaSmitheryModifiers.ANALYSING);
        malumValoria("aquarius",    3, 8.0f, 6.0f, 800, 900.0f, 0xFF4FA9C9, 3,
                SoaSmitheryModifiers.WHIRL);
        malumValoria("black_gold",  3, 9.0f, 5.5f, 700, 900.0f, 0xFF3A3A2E, 3,
                SoaSmitheryModifiers.GLIMMER);
        malumValoria("crimtane",    3, 7.5f, 7.0f, 900, 1000.0f, 0xFFA0262B, 3,
                SoaSmitheryModifiers.BERSERK);
        malumValoria("infernal",    3, 8.0f, 7.0f, 850, 1300.0f, 0xFFD35B26, 3,
                SoaSmitheryModifiers.MELTING);
        malumValoria("nature",      3, 7.0f, 5.5f, 750, 800.0f, 0xFF4C8A3F, 3,
                SoaSmitheryModifiers.NATUREBOUND);
        malumValoria("pearlium",    3, 8.5f, 6.0f, 800, 900.0f, 0xFFD8E4E8, 3,
                SoaSmitheryModifiers.RESONANCE);
        malumValoria("void",        4, 9.5f, 8.5f, 1200, 1500.0f, 0xFF241B33, 5,
                SoaSmitheryModifiers.DARK);
        // valoria ruby/sapphire gems feed the existing Silent's Gems materials (melting only)
    }

    private static void malumValoria(String name, int harvest, float speed, float damage,
                                     int durability, float meltTemp, int color, int binders,
                                     ResourceLocation trait) {
        SmitheryAPI.registerMaterial(id(name), binderSlots(MaterialStats.builder()
                        .harvestLevel(harvest)
                        .miningSpeed(speed)
                        .attackDamage(damage)
                        .durabilityPerIngot(durability)
                        .meltingTemp(meltTemp)
                        .partColor(color)
                        .binderMultiplier(1.0f + binders * 0.25f)
                , binders)
                .addModifier(ModifierEffect.of(trait), allToolTypes())
                .build());
    }

    /**
     * Fluid-only materials backing the GC alloy recipes (TC 1.12 had these as smeltery
     * fluids with no tool material). Zero combat/mining stats, like magma; meltingTemp
     * must be &gt; 0 or SmitheryFluids.bootstrap() skips the fluid entirely.
     */
    private static void registerAlloyFluids() {
        SmitheryAPI.registerMaterial(id("clay"), MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(700.0f)
                        .partColor(0xFFA0A7B4)
                        .binderMultiplier(1.0f)
                .build());

        SmitheryAPI.registerMaterial(id("glowstone"), MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(500.0f)
                        .partColor(0xFFFFBC5E)
                        .binderMultiplier(1.0f)
                .build());

        // "ender" is NOT registered here — it already exists as an entity-melting
        // fluid material (see ENDER above, with moltenColor + castOnly).

        SmitheryAPI.registerMaterial(id("lava"), MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFD96415)
                        .binderMultiplier(1.0f)
                .build());
    }

    private static MaterialStats.Builder binderSlots(MaterialStats.Builder b, int count) {
        return b.modifierSlots(SmitheryPartTypes.BINDER, count)
                .modifierSlots(SmitheryPartTypes.ARMOR_PLATES, count);
    }

    private static ToolType[] allToolTypes() {
        return new ToolType[]{ SmitheryToolTypes.SWORD, SmitheryToolTypes.PICKAXE,
                SmitheryToolTypes.AXE, SmitheryToolTypes.SHOVEL, SmitheryToolTypes.HOE,
                SmitheryToolTypes.SPEAR, SmitheryToolTypes.BROADSWORD, SmitheryToolTypes.RAPIER,
                SmitheryToolTypes.PAXEL, SmitheryToolTypes.MINING_HAMMER, SmitheryToolTypes.KAMA,
                SmitheryToolTypes.CLEAVER, SmitheryToolTypes.LUMBERAXE, SmitheryToolTypes.EXCAVATOR,
                SmitheryToolTypes.SHURIKEN, SmitheryToolTypes.TRIDENT, SmitheryToolTypes.BATTLESIGN,
                SmitheryToolTypes.SCYTHE, SmitheryToolTypes.SCEPTRE };
    }

    private static ToolType[] armorPieces() {
        return new ToolType[]{ SmitheryToolTypes.HELMET, SmitheryToolTypes.CHESTPLATE,
                SmitheryToolTypes.LEGGINGS, SmitheryToolTypes.BOOTS };
    }

    private static void registerAbyssalcraft() {
        ABYSSALNITE = id("abyssalnite");
        SmitheryAPI.registerMaterial(ABYSSALNITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(5.5f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(1280)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF431A7A)
                        .binderMultiplier(0.9f)
                , 3)
                .armor(256.0f, 23.0f, 70.0f, 2.0f, 2.5f, 20.0f)
                .addUniversalModifier(SoaSmitheryModifiers.DREADPURITY)
                .build());

        DREADIUM = id("dreadium");
        SmitheryAPI.registerMaterial(DREADIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(7.0f)
                        .attackDamage(8.8f)
                        .durabilityPerIngot(1000)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFB80202)
                        .binderMultiplier(1.0f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPLAGUE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPURITY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPLAGUE), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPURITY), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STRONGVACCINETRAIT_ARMOR), armorPieces())
                .armor(240.0f, 28.0f, 60.0f, 2.7f, 3.5f, 40.0f)
                .build());

        ETHAXIUM = id("ethaxium");
        SmitheryAPI.registerMaterial(ETHAXIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(15.5f)
                        .attackDamage(14.2f)
                        .durabilityPerIngot(2800)
                        .meltingTemp(3000.0f)
                        .partColor(0xFF5F5845)
                        .binderMultiplier(1.2f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPLAGUE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GIANTSLAYER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_OVERWHELM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPURITY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPLAGUE), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FIRSTGUARDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DREADPURITY), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STRONGVACCINETRAIT_ARMOR), armorPieces())
                .armor(420.0f, 42.6f, 76.8f, 4.6f, 4.0f, 76.8f)
                .bow(0.7407f, 1.3f, 12.2f)
                .build());

        REFINED_CORALIUM = id("refined_coralium");
        SmitheryAPI.registerMaterial(REFINED_CORALIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(6.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF139A69)
                        .binderMultiplier(0.95f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CORALIUMPLAGUE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CORALIUMPLAGUE), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VACCINETRAIT_ARMOR), armorPieces())
                .armor(200.0f, 25.0f, 32.0f, 1.8f, 3.0f, 12.0f)
                .build());

    }

    private static void registerTaiga() {
        ABYSSUM = id("abyssum");
        SmitheryAPI.registerMaterial(ABYSSUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(4.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF1B546A)
                        .binderMultiplier(0.85f)
                , 4)
                .build());

        ADAMANT = id("adamant");
        SmitheryAPI.registerMaterial(ADAMANT, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(18.2f)
                        .attackDamage(18.6f)
                        .durabilityPerIngot(1800)
                        .meltingTemp(4000.0f)
                        .partColor(0xFFFC8FF3)
                        .binderMultiplier(1.75f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.APOCALYPSE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ELEMENTAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESPOWER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INVIGORATING_ARMOR), armorPieces())
                .armor(440.0f, 41.0f, 30.0f, 4.2f, 3.5f, 70.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BERSERK), allToolTypes())
                .bow(0.35f, 1.85f, 9.4f)
                .build());

        ASTRIUM = id("astrium");
        SmitheryAPI.registerMaterial(ASTRIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.35f)
                        .attackDamage(5.4f)
                        .durabilityPerIngot(750)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF4D1B50)
                        .binderMultiplier(0.95f)
                , 4)
                .armor(210.0f, 23.0f, 30.0f, 2.2f, 2.5f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PORTED), allToolTypes())
                .bow(0.7f, 0.8f, 2.0f)
                .build());

        AURORIUM = id("aurorium");
        SmitheryAPI.registerMaterial(AURORIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.5f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(700)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFDC6263)
                        .binderMultiplier(0.8f)
                , 4)
                .armor(110.0f, 32.0f, 110.0f, 2.1f, 1.5f, 83.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), allToolTypes())
                .bow(0.45f, 1.0f, 1.0f)
                .build());

        // GC TCon entity melting fluids — fluid-only materials (pattern: smithery BLOOD).
        // Water-base fluids render as the bare name ("Milk", not "Molten Milk").
        MILK = id("milk");
        SmitheryAPI.registerMaterial(MILK, MaterialStats.builder()
                        .harvestLevel(0).miningSpeed(0f).attackDamage(0f).durabilityPerIngot(0)
                        .meltingTemp(50f)
                        .moltenColor(0xFFF3F3F0)
                        .partColor(0xFFF3F3F0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                        .fluidBase(MaterialStats.FluidBase.WATER)
                .build());

        ENDER = id("ender");
        SmitheryAPI.registerMaterial(ENDER, MaterialStats.builder()
                        .harvestLevel(0).miningSpeed(0f).attackDamage(0f).durabilityPerIngot(0)
                        .meltingTemp(500f)
                        .moltenColor(0xFF105E51)
                        .partColor(0xFF105E51)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .build());

        WATER = id("water");
        SmitheryAPI.registerMaterial(WATER, MaterialStats.builder()
                        .harvestLevel(0).miningSpeed(0f).attackDamage(0f).durabilityPerIngot(0)
                        .meltingTemp(20f)
                        .moltenColor(0xFF3F76E4)
                        .partColor(0xFF3F76E4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                        .fluidBase(MaterialStats.FluidBase.WATER)
                .build());

        BARONYTE = id("baronyte");
        SmitheryAPI.registerMaterial(BARONYTE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBFCF3E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 20.25f, 84.48f, 29.2235f, 0.0f, 84.48f)
                .build());

        BASALT = id("basalt");
        SmitheryAPI.registerMaterial(BASALT, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.5f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(180)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFC4C0B7)
                        .binderMultiplier(0.75f)
                , 2)
                .armor(50.0f, 10.0f, 110.0f, 0.5f, 1.5f, 30.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOFTY), allToolTypes())
                .build());

        BLAZIUM = id("blazium");
        SmitheryAPI.registerMaterial(BLAZIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8722E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(112.0f, 19.44f, 26.88f, 17.7882f, 0.0f, 26.88f)
                .build());

        CHARGER = id("charger");
        SmitheryAPI.registerMaterial(CHARGER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBEBEBE)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(15.0f, 13.5f, 6.912f, 12.7059f, 0.0f, 6.912f)
                .build());

        CLOUD = id("cloud");
        SmitheryAPI.registerMaterial(CLOUD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7A9DC3)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(2.0f, 0.0f, -57.6f, 0.0f, 0.0f, -57.6f)
                .build());

        CORRUPTED = id("corrupted");
        SmitheryAPI.registerMaterial(CORRUPTED, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF690305)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(9.0f, 18.0f, 5.6f, 18.0f, 0.0f, 5.6f)
                .build());

        CORUNDUM = id("corundum");
        SmitheryAPI.registerMaterial(CORUNDUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9E3B3B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(52.0f, 15.75f, 16.0f, 14.8235f, 0.0f, 16.0f)
                .build());

        DILITHIUM = id("dilithium");
        SmitheryAPI.registerMaterial(DILITHIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF305A61)
                        .binderMultiplier(1.0f)
                .build());

        DOGBEARIUM = id("dogbearium");
        SmitheryAPI.registerMaterial(DOGBEARIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8B6F47)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(7.0f, 40.5f, 8.4f, 18.0f, 0.0f, 8.4f)
                .build());

        DURANITE = id("duranite");
        SmitheryAPI.registerMaterial(DURANITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(6.5f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(1000)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFA5B9CD)
                        .binderMultiplier(0.8f)
                , 4)
                .armor(170.0f, 24.0f, 130.0f, 2.8f, 1.0f, 40.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ANALYSING), allToolTypes())
                .bow(0.3f, 1.4f, 2.0f)
                .build());

        DYONITE = id("dyonite");
        SmitheryAPI.registerMaterial(DYONITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(6.45f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(900)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF892717)
                        .binderMultiplier(0.66f)
                , 5)
                .armor(150.0f, 25.0f, 105.0f, 1.5f, 1.0f, 40.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TANTRUM), allToolTypes())
                .build());

        EEZO = id("eezo");
        SmitheryAPI.registerMaterial(EEZO, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(500)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF252729)
                        .binderMultiplier(0.7f)
                , 4)
                .armor(30.0f, 20.0f, 80.0f, 1.0f, 1.0f, 40.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DISSOLVING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAVY), allToolTypes())
                .build());

        ELECANIUM = id("elecanium");
        SmitheryAPI.registerMaterial(ELECANIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3EC8C8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(84.0f, 32.94f, 11.52f, 50.0f, 0.0f, 11.52f)
                .build());

        EMBERSTONE = id("emberstone");
        SmitheryAPI.registerMaterial(EMBERSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8542E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(90.0f, 20.52f, 76.8f, 16.5176f, 0.0f, 76.8f)
                .build());

        FRACTUM = id("fractum");
        SmitheryAPI.registerMaterial(FRACTUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.71f)
                        .attackDamage(6.93f)
                        .durabilityPerIngot(538)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF796A4B)
                        .binderMultiplier(0.88f)
                , 2)
                .armor(30.0f, 21.0f, 110.0f, 1.25f, 0.0f, 120.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FRACTURE), allToolTypes())
                .build());

        FROSTSTEEL = id("froststeel");
        SmitheryAPI.registerMaterial(FROSTSTEEL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FD8E8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(28.0f, 31.5f, 14.0f, 23.2941f, 0.0f, 14.0f)
                .build());

        FULGURITE = id("fulgurite");
        SmitheryAPI.registerMaterial(FULGURITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF837544)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(60.0f, 22.05f, 38.4f, 0.0f, 0.0f, 38.4f)
                .build());

        GRAIN = id("grain");
        SmitheryAPI.registerMaterial(GRAIN, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFA49A95)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(28.0f, 9.45f, 8.96f, 15.5647f, 0.0f, 8.96f)
                .build());

        IGNITZ = id("ignitz");
        SmitheryAPI.registerMaterial(IGNITZ, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(2.0f)
                        .attackDamage(6.66f)
                        .durabilityPerIngot(350)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFEE5D6B)
                        .binderMultiplier(0.85f)
                , 4)
                .armor(150.0f, 30.0f, 50.0f, 1.8f, 2.0f, 100.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MELTING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GARISHLY), allToolTypes())
                .bow(0.8f, 0.8f, 3.0f)
                .build());

        IMPEROMITE = id("imperomite");
        SmitheryAPI.registerMaterial(IMPEROMITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(4.65f)
                        .attackDamage(5.9f)
                        .durabilityPerIngot(1350)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF549C6C)
                        .binderMultiplier(1.15f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ZANY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ZANY), armorPieces())
                .armor(120.0f, 36.0f, 50.0f, 1.9f, 1.5f, 120.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLLOW), allToolTypes())
                .bow(1.2f, 1.8f, 2.0f)
                .build());

        INERT_WITHERIUM = id("inert_witherium");
        SmitheryAPI.registerMaterial(INERT_WITHERIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A4A52)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 9.45f, 57.6f, 15.5647f, 0.0f, 57.6f)
                .build());

        IOX = id("iox");
        SmitheryAPI.registerMaterial(IOX, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.5f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(900)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFC32023)
                        .binderMultiplier(0.75f)
                , 4)
                .build());

        JAUXUM = id("jauxum");
        SmitheryAPI.registerMaterial(JAUXUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(550)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF3FB53F)
                        .binderMultiplier(0.8f)
                , 3)
                .build());

        KARMESINE = id("karmesine");
        SmitheryAPI.registerMaterial(KARMESINE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(6.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFB3242C)
                        .binderMultiplier(0.8f)
                , 4)
                .build());

        LUMIX = id("lumix");
        SmitheryAPI.registerMaterial(LUMIX, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(3.84f)
                        .attackDamage(3.92f)
                        .durabilityPerIngot(666)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFFFCD7D)
                        .binderMultiplier(0.85f)
                , 4)
                .armor(140.0f, 24.0f, 110.0f, 0.8f, 1.25f, 20.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BRIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GLIMMER), allToolTypes())
                .bow(0.8f, 1.3f, 1.0f)
                .build());

        LUNAR = id("lunar");
        SmitheryAPI.registerMaterial(LUNAR, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF79498B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(93.0f, 10.8f, 49.92f, 22.8706f, 0.0f, 49.92f)
                .build());

        LYON = id("lyon");
        SmitheryAPI.registerMaterial(LYON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC8CAD8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(118.0f, 21.6f, 145.92f, 26.6824f, 0.0f, 145.92f)
                .build());

        METEORITE = id("meteorite");
        SmitheryAPI.registerMaterial(METEORITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(5.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(250)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF252527)
                        .binderMultiplier(0.8f)
                , 2)
                .armor(80.0f, 12.0f, 130.0f, 0.5f, 0.0f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRUMBLING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PULVERIZING), allToolTypes())
                .build());

        MYSTITE = id("mystite");
        SmitheryAPI.registerMaterial(MYSTITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FE8C8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(107.0f, 23.22f, 30.72f, 26.6824f, 0.0f, 30.72f)
                .build());

        NIHILITE = id("nihilite");
        SmitheryAPI.registerMaterial(NIHILITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(14.2f)
                        .attackDamage(16.4f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF686EA3)
                        .binderMultiplier(2.25f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MADNESSTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SUNDERING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING_ARMOR), armorPieces())
                .armor(360.0f, 43.0f, 35.0f, 4.0f, 2.4f, 25.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOULEATER), allToolTypes())
                .bow(1.5f, 0.8f, 14.4f)
                .build());

        NIOB = id("niob");
        SmitheryAPI.registerMaterial(NIOB, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(4.5f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(700)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF314357)
                        .binderMultiplier(2.0f)
                , 4)
                .armor(320.0f, 30.0f, 110.0f, 2.4f, 2.25f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.REVIVING), allToolTypes())
                .build());

        NITRONITE = id("nitronite");
        SmitheryAPI.registerMaterial(NITRONITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E82E)
                        .binderMultiplier(1.0f)
                .build());

        NUCLEUM = id("nucleum");
        SmitheryAPI.registerMaterial(NUCLEUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(17.5f)
                        .attackDamage(9.5f)
                        .durabilityPerIngot(505)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFE2F987)
                        .binderMultiplier(1.05f)
                , 5)
                .armor(130.0f, 23.0f, 130.0f, 1.2f, 1.0f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DECAY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MUTATE), allToolTypes())
                .build());

        OBSIDIORITE = id("obsidiorite");
        SmitheryAPI.registerMaterial(OBSIDIORITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(6.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(350)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF1B2532)
                        .binderMultiplier(0.85f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .armor(80.0f, 24.0f, 130.0f, 2.0f, 0.0f, 81.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ALIEN), allToolTypes())
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .build());

        OSRAM = id("osram");
        SmitheryAPI.registerMaterial(OSRAM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE09063)
                        .binderMultiplier(1.0f)
                .build());

        OVIUM = id("ovium");
        SmitheryAPI.registerMaterial(OVIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF2F31C5)
                        .binderMultiplier(0.75f)
                , 2)
                .build());

        PALLADIUM = id("palladium");
        SmitheryAPI.registerMaterial(PALLADIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(900)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFC95319)
                        .binderMultiplier(0.8f)
                , 4)
                .armor(220.0f, 24.0f, 90.0f, 2.0f, 2.25f, 110.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DARK), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CURSED), allToolTypes())
                .bow(0.5f, 0.2f, 3.0f)
                .build());

        PANDORIUM = id("pandorium");
        SmitheryAPI.registerMaterial(PANDORIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F2EA8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(47.0f, 39.24f, 11.088f, 8.4706f, 0.0f, 11.088f)
                .build());

        PROMETHEUM = id("prometheum");
        SmitheryAPI.registerMaterial(PROMETHEUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(6.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(800)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF332D35)
                        .binderMultiplier(0.9f)
                , 3)
                .armor(110.0f, 17.0f, 120.0f, 1.2f, 0.0f, 30.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLIND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CATCHER), allToolTypes())
                .bow(0.2f, 0.6f, 3.0f)
                .build());

        PROUSTITE = id("proustite");
        SmitheryAPI.registerMaterial(PROUSTITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB03A4A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 12.6f, 11.264f, 12.6f, 0.0f, 11.264f)
                .build());

        QUEENSGOLD = id("queensgold");
        SmitheryAPI.registerMaterial(QUEENSGOLD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8C22E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(3.0f, 13.5f, 2.24f, 22.2353f, 0.0f, 2.24f)
                .build());

        REDINS = id("redins");
        SmitheryAPI.registerMaterial(REDINS, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC23A2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 22.05f, 35.2f, 12.6f, 0.0f, 35.2f)
                .build());

        ROSEGOLD = id("rosegold");
        SmitheryAPI.registerMaterial(ROSEGOLD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFFF9E13)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(4.0f, 4.5f, -10.08f, 31.7647f, 0.0f, -10.08f)
                .build());

        ROSITE = id("rosite");
        SmitheryAPI.registerMaterial(ROSITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE85A7A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(56.0f, 18.36f, 21.12f, 24.1412f, 0.0f, 21.12f)
                .build());

        SEISMUM = id("seismum");
        SmitheryAPI.registerMaterial(SEISMUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(3.66f)
                        .attackDamage(6.05f)
                        .durabilityPerIngot(780)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF56282D)
                        .binderMultiplier(0.95f)
                , 4)
                .armor(230.0f, 20.0f, 140.0f, 1.2f, 2.0f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CASCADE), allToolTypes())
                .build());

        SHYRESTONE = id("shyrestone");
        SmitheryAPI.registerMaterial(SHYRESTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FE8E8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(90.0f, 19.44f, 120.96f, 25.4118f, 0.0f, 120.96f)
                .build());

        SINISTERIUM = id("sinisterium");
        SmitheryAPI.registerMaterial(SINISTERIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6A2E38)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(10.0f, 22.5f, 11.088f, 15.8824f, 0.0f, 11.088f)
                .build());

        SOLARIUM = id("solarium");
        SmitheryAPI.registerMaterial(SOLARIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(24.0f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1100)
                        .meltingTemp(4000.0f)
                        .partColor(0xFFFF6C00)
                        .binderMultiplier(1.25f)
                , 5)
                .armor(200.0f, 33.0f, 130.0f, 1.0f, 1.75f, 140.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAVY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRUSHING), allToolTypes())
                .bow(0.8f, 1.5f, 5.0f)
                .build());

        STELLARIUM = id("stellarium");
        SmitheryAPI.registerMaterial(STELLARIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A5AA8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 25.2f, 0.0f, 0.0f, 0.0f, 0.0f)
                .build());

        STELLARIUM_OBSIDIAN = id("stellarium_obsidian");
        SmitheryAPI.registerMaterial(STELLARIUM_OBSIDIAN, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E2A45)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(63.0f, 22.05f, 6.4f, 14.8235f, 0.0f, 6.4f)
                .build());

        TERRAX = id("terrax");
        SmitheryAPI.registerMaterial(TERRAX, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(4.77f)
                        .attackDamage(2.9f)
                        .durabilityPerIngot(444)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF594251)
                        .binderMultiplier(0.8f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SACRIFICIALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN_ARMOR), armorPieces())
                .armor(130.0f, 28.0f, 170.0f, 2.1f, 2.0f, 110.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SLAUGHTERING), allToolTypes())
                .build());

        TIBERIUM = id("tiberium");
        SmitheryAPI.registerMaterial(TIBERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(8.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF587004)
                        .binderMultiplier(1.0f)
                , 4)
                .armor(120.0f, 26.0f, 140.0f, 2.1f, 2.5f, 60.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.UNSTABLE), allToolTypes())
                .build());

        TITANIUM = id("titanium");
        SmitheryAPI.registerMaterial(TITANIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(24.6f)
                        .attackDamage(31.2f)
                        .durabilityPerIngot(8000)
                        .meltingTemp(8000.0f)
                        .partColor(0xFFBABABA)
                        .binderMultiplier(2.8f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTICORROSION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_FOOT_FLEET), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.THUNDERING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FIRSTGUARDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .armor(800.0f, 77.6f, 300.0f, 15.0f, 6.0f, 200.0f)
                .bow(0.8333f, 1.3f, 23.3f)
                .arrowShaft(2.4f, 32)
                .fletching(1.0f, 1.3f)
                .build());

        TRIBERIUM = id("triberium");
        SmitheryAPI.registerMaterial(TRIBERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.2f)
                        .attackDamage(8.35f)
                        .durabilityPerIngot(223)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF3D5F0E)
                        .binderMultiplier(0.63f)
                , 2)
                .armor(20.0f, 20.0f, 120.0f, 1.2f, 0.0f, 50.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FRAGILE), allToolTypes())
                .build());

        TRITONITE = id("tritonite");
        SmitheryAPI.registerMaterial(TRITONITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(3.3f)
                        .durabilityPerIngot(780)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF014986)
                        .binderMultiplier(1.45f)
                , 4)
                .armor(120.0f, 27.0f, 120.0f, 1.7f, 0.0f, 100.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WHIRL), allToolTypes())
                .build());

        URU = id("uru");
        SmitheryAPI.registerMaterial(URU, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(6.5f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF837490)
                        .binderMultiplier(0.85f)
                , 4)
                .armor(120.0f, 26.0f, 90.0f, 2.0f, 1.25f, 30.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DIFFUSE), allToolTypes())
                .bow(1.3f, 0.8f, 6.0f)
                .build());

        VALYRIANSTEEL = id("valyriansteel");
        SmitheryAPI.registerMaterial(VALYRIANSTEEL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6A7A8A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(28.0f, 31.5f, 14.0f, 19.0588f, 0.0f, 14.0f)
                .build());

        VALYRIUM = id("valyrium");
        SmitheryAPI.registerMaterial(VALYRIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(8.0f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1100)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF891C45)
                        .binderMultiplier(0.85f)
                , 4)
                .armor(220.0f, 37.0f, 120.0f, 3.0f, 3.0f, 30.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CONGENIAL), allToolTypes())
                .bow(1.1f, 1.2f, 4.0f)
                .build());

        VARSIUM = id("varsium");
        SmitheryAPI.registerMaterial(VARSIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8FA84A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 21.06f, 96.0f, 25.4118f, 0.0f, 96.0f)
                .build());

        VIBRANIUM = id("vibranium");
        SmitheryAPI.registerMaterial(VIBRANIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(9.0f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFD6DCE0)
                        .binderMultiplier(0.85f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GAMBLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GAMBLE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT_ARMOR), armorPieces())
                .armor(250.0f, 39.0f, 170.0f, 3.2f, 2.5f, 50.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RESONANCE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEROIC), allToolTypes())
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.1f, 1.8f, 4.0f)
                .build());

        VIOLIUM = id("violium");
        SmitheryAPI.registerMaterial(VIOLIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(3.8f)
                        .attackDamage(3.75f)
                        .durabilityPerIngot(925)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF43546B)
                        .binderMultiplier(0.9f)
                , 4)
                .armor(140.0f, 24.0f, 110.0f, 1.5f, 1.0f, 40.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), allToolTypes())
                .bow(0.45f, 0.95f, 1.0f)
                .build());

        WEEZER = id("weezer");
        SmitheryAPI.registerMaterial(WEEZER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFFFFFFF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 50.0f, 192.0f, 38.1176f, 0.0f, 192.0f)
                .build());

        WITHERIUM = id("witherium");
        SmitheryAPI.registerMaterial(WITHERIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E2E38)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(81.0f, 28.35f, 12.8f, 14.0824f, 0.0f, 12.8f)
                .build());

        WOLFRAMIUM = id("wolframium");
        SmitheryAPI.registerMaterial(WOLFRAMIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF5A5A62)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(60.0f, 20.475f, 0.0f, 20.7529f, 0.0f, 0.0f)
                .build());

        YRDEEN = id("yrdeen");
        SmitheryAPI.registerMaterial(YRDEEN, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.0f)
                        .attackDamage(9.5f)
                        .durabilityPerIngot(1800)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF401736)
                        .binderMultiplier(0.9f)
                , 5)
                .armor(150.0f, 24.0f, 150.0f, 2.2f, 1.0f, 80.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATUREBOUND), allToolTypes())
                .build());

    }

    private static void registerGreedycraftCustom() {
        ADAMINITE = id("adaminite");
        SmitheryAPI.registerMaterial(ADAMINITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(17.2f)
                        .attackDamage(25.2f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFA72A3C)
                        .colorCycle(10, 0xFFDA374E, 0xFF741E2A) // [auto-color]
                        .binderMultiplier(1.6f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COLDBLOODED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(400.0f, 42.0f, 100.0f, 5.2f, 3.0f, 120.0f)
                .bow(1.4286f, 1.2f, 12.0f)
                .build());

        AEONSTEEL = id("aeonsteel");
        SmitheryAPI.registerMaterial(AEONSTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(22.3f)
                        .attackDamage(23.5f)
                        .durabilityPerIngot(18000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF00544D)
                        .binderMultiplier(2.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1)), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PENETRATIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INDOMITABLE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TRUEDEFENSETRAIT_ARMOR), armorPieces())
                .armor(480.0f, 62.0f, 250.0f, 6.0f, 5.25f, 150.0f)
                .bow(0.4545f, 1.75f, 14.0f)
                .arrowShaft(2.25f, 2)
                .build());

        AEROITE = id("aeroite");
        SmitheryAPI.registerMaterial(AEROITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(9.2f)
                        .attackDamage(7.9f)
                        .durabilityPerIngot(200)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF12E6FF)
                        .binderMultiplier(1.3f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTIGRAV), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LAUNCHING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FEATHERWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_REACTIVE_ARMOR), armorPieces())
                .armor(200.0f, 23.4f, 25.6f, 3.0f, 2.25f, 25.6f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.4286f, 6.0f, 2.0f)
                .build());

        ALUBRASS = id("alubrass");
        SmitheryAPI.registerMaterial(ALUBRASS, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(200)
                        .meltingTemp(600.0f)
                        .partColor(0xFFDDB94C)
                        .binderMultiplier(1.2f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOMENTUM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FEATHERWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN_ARMOR), armorPieces())
                .armor(50.0f, 14.6f, 13.0f, 1.1f, 0.5f, 11.0f)
                .bow(0.6061f, 1.3f, 6.2f)
                .build());

        AQUALITE = id("aqualite");
        SmitheryAPI.registerMaterial(AQUALITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(10.8f)
                        .attackDamage(10.2f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF10D0BF)
                        .binderMultiplier(1.75f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUADYNAMIC), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TIDALFORCETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ABSORBENT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUASPEED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TIDALFORCETRAIT_ARMOR), armorPieces())
                .armor(280.0f, 26.5f, 80.0f, 3.0f, 3.75f, 70.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TIDAL_FORCE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.6667f, 1.5f, 2.6f)
                .build());

        ASGARDIUM = id("asgardium");
        SmitheryAPI.registerMaterial(ASGARDIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.3f)
                        .attackDamage(8.8f)
                        .durabilityPerIngot(600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFFFDF18)
                        .binderMultiplier(1.5f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BANE_OF_NIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTNING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GILDED), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRIDEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT_ARMOR), armorPieces())
                .armor(240.0f, 25.2f, 76.8f, 3.2f, 3.25f, 76.8f)
                .bow(0.6667f, 1.2f, 8.0f)
                .build());

        ASTRAL_METAL = id("astral_metal");
        SmitheryAPI.registerMaterial(ASTRAL_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(17.6f)
                        .attackDamage(23.0f)
                        .durabilityPerIngot(2600)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFD300A3)
                        .binderMultiplier(2.0f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BANE_OF_NIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOMENTUM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA2), armorPieces())
                .armor(700.0f, 58.0f, 800.0f, 6.4f, 6.0f, 600.0f)
                .bow(1.0f, 1.4f, 11.0f)
                .build());

        BLOODSTONE = id("bloodstone");
        SmitheryAPI.registerMaterial(BLOODSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF77231D)
                        .binderMultiplier(1.0f)
                .build());

        CHEESE = id("cheese");
        SmitheryAPI.registerMaterial(CHEESE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.5f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(200)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF4BD3A)
                        .binderMultiplier(0.2f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MILKYTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SQUEAKY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MILKYTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PURIFYINGTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), armorPieces())
                .armor(80.0f, 4.0f, 10.0f, 0.25f, 0.25f, 20.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.8333f, 1.0f, 0.2f)
                .build());

        CHOCOLATE = id("chocolate");
        SmitheryAPI.registerMaterial(CHOCOLATE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(2.6f)
                        .attackDamage(1.2f)
                        .durabilityPerIngot(20)
                        .meltingTemp(0.0f)
                        .partColor(0xFF75401F)
                        .binderMultiplier(0.2f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1)), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), armorPieces())
                .armor(40.0f, 6.0f, 5.0f, 1.0f, 0.5f, 3.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.3333f, 1.0f, 1.0f)
                .build());

        CHROMASTEEL = id("chromasteel");
        SmitheryAPI.registerMaterial(CHROMASTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(30.2f)
                        .attackDamage(38.2f)
                        .durabilityPerIngot(8400)
                        .meltingTemp(8000.0f)
                        .partColor(0xFF9F9F9F)
                        .colorCycle(18, 0xFF5082EF, 0xFFBD50EF, 0xFFEF5082, 0xFFEFBD50, 0xFF82EF50, 0xFF50EFBD) // [auto-color]
                        .binderMultiplier(3.2f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COTLIFESTEAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.EXECUTIONERTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE2, java.util.Map.of("bonus_slots", 1)), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FIRSTGUARDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INVIGORATING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .armor(800.0f, 92.0f, 400.0f, 8.0f, 4.0f, 560.0f)
                .bow(2.0f, 1.25f, 4.5f)
                .arrowShaft(4.0f, 1)
                .build());

        CHROMIUM = id("chromium");
        SmitheryAPI.registerMaterial(CHROMIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(9.2f)
                        .attackDamage(6.8f)
                        .durabilityPerIngot(720)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF80DEEA)
                        .binderMultiplier(1.25f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.POISONOUS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .armor(120.0f, 21.0f, 25.6f, 1.8f, 1.25f, 25.6f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7143f, 1.2f, 1.6f)
                .build());

        CINCINNASITE = id("cincinnasite");
        SmitheryAPI.registerMaterial(CINCINNASITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(240)
                        .meltingTemp(600.0f)
                        .partColor(0xFF533313)
                        .binderMultiplier(1.05f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RAGING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN_ARMOR), armorPieces())
                .armor(40.0f, 13.0f, 5.0f, 0.5f, 0.5f, 2.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.5556f, 1.4f, 1.5f)
                .build());

        COAL = id("coal");
        SmitheryAPI.registerMaterial(COAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.5f)
                        .attackDamage(1.2f)
                        .durabilityPerIngot(10)
                        .meltingTemp(0.0f)
                        .partColor(0xFF323233)
                        .binderMultiplier(0.1f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEPTHDIGGER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUBTERRANEAN_ARMOR), armorPieces())
                .armor(10.0f, 4.0f, 1.92f, 0.3f, 0.0f, 1.92f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.5556f, 1.0f, 0.1f)
                .build());

        FUSION_MATRIX = id("fusion_matrix");
        SmitheryAPI.registerMaterial(FUSION_MATRIX, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(13.6f)
                        .attackDamage(18.9f)
                        .durabilityPerIngot(12000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF87879D)
                        .binderMultiplier(1.8f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOVERING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_OVERWHELM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FIRSTGUARDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(240.0f, 50.0f, -3.84f, 3.6f, 4.25f, -3.84f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.8333f, 1.2f, 9.6f)
                .arrowShaft(1.75f, 40)
                .build());

        COSMILITE = id("cosmilite");
        SmitheryAPI.registerMaterial(COSMILITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(12)
                        .miningSpeed(40.0f)
                        .attackDamage(128.0f)
                        .durabilityPerIngot(12800)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFA9AAA9)
                        .colorCycle(16, 0xFF898C8A, 0xFFA1A3A2, 0xFFB4B5B4, 0xFFC3C4C3, 0xFFB4B5B4, 0xFFA1A3A2) // [auto-color]
                        .binderMultiplier(3.0f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.EXECUTIONERTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_INFINITUM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_REND3), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CELESTIAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GALE_FORCE_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_INFINITUM), armorPieces())
                .armor(6400.0f, 144.0f, 33554.43f, 28.0f, 15.0f, 33554.43f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(3.125f, 3.0f, 38.0f)
                .build());

        CRIMSONITE = id("crimsonite");
        SmitheryAPI.registerMaterial(CRIMSONITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(23.6f)
                        .attackDamage(11.6f)
                        .durabilityPerIngot(3600)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFA60000)
                        .binderMultiplier(1.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_BLOODBOUND_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COTLIFESTEAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CRYSTALYS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIVING2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIVING2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SENTIENT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_GUARD_ARMOR), armorPieces())
                .armor(280.0f, 30.0f, 200.0f, 3.0f, 4.25f, 140.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_BLOODBOUND),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.3333f, 1.25f, 4.5f)
                .build());

        CRYONIUM = id("cryonium");
        SmitheryAPI.registerMaterial(CRYONIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(14.6f)
                        .attackDamage(19.4f)
                        .durabilityPerIngot(8000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF46B4FD)
                        .binderMultiplier(1.6f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FREEZING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHOCKING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYONICTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .armor(300.0f, 52.8f, 75.0f, 5.0f, 4.0f, 40.0f)
                .bow(0.8333f, 1.3f, 8.9f)
                .arrowShaft(1.5f, 32)
                .build());

        CRYSTAL_LEAF = id("crystal_leaf");
        SmitheryAPI.registerMaterial(CRYSTAL_LEAF, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF65C3D4)
                        .binderMultiplier(1.0f)
                .fletching(0.8f, 2.0f)
                .build());

        CRYSTALLITE = id("crystallite");
        SmitheryAPI.registerMaterial(CRYSTALLITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8E8F0)
                        .binderMultiplier(1.0f)
                .build());

        CYTOSINITE = id("cytosinite");
        SmitheryAPI.registerMaterial(CYTOSINITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(14.6f)
                        .attackDamage(14.2f)
                        .durabilityPerIngot(6000)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFCAF925)
                        .binderMultiplier(1.3f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_FERTILIZING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESBLESSING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESPOWER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESWRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VENGEFUL_ARMOR), armorPieces())
                .armor(300.0f, 44.2f, 70.0f, 4.2f, 2.5f, 50.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.25f, 1.1f, 7.8f)
                .build());

        DIMENSIONAL_SHARD = id("dimensional_shard");
        SmitheryAPI.registerMaterial(DIMENSIONAL_SHARD, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.4f)
                        .attackDamage(7.8f)
                        .durabilityPerIngot(500)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF83CDCB)
                        .binderMultiplier(1.3f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CURVATURE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SPECTRAL_ARMOR), armorPieces())
                .armor(100.0f, 23.0f, 23.0f, 1.3f, 2.0f, 12.0f)
                .bow(0.8333f, 1.2f, 6.0f)
                .build());

        DURASTEEL = id("durasteel");
        SmitheryAPI.registerMaterial(DURASTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(15.3f)
                        .attackDamage(11.5f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF3B4EC1)
                        .binderMultiplier(2.0f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COLDBLOODED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDURANCE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(320.0f, 32.0f, 160.0f, 3.2f, 4.0f, 120.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARROW_SHAFT)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOW_LIMB)
                .bow(1.0f, 1.5f, 6.0f)
                .arrowShaft(1.5f, 4)
                .build());

        ELECTRONIUM = id("electronium");
        SmitheryAPI.registerMaterial(ELECTRONIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(25.6f)
                        .attackDamage(24.0f)
                        .durabilityPerIngot(20000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF41299E)
                        .binderMultiplier(2.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ENERGIZED_ARMOR2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTNING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ENERGIZED_ARMOR2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SUPERDENSE_ARMOR), armorPieces())
                .armor(500.0f, 63.0f, 537.6f, 10.0f, 8.0f, 537.6f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ENERGIZED)
                .build());

        ENDER_BIOTITE = id("ender_biotite");
        SmitheryAPI.registerMaterial(ENDER_BIOTITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(11.5f)
                        .attackDamage(11.2f)
                        .durabilityPerIngot(720)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF090D13)
                        .binderMultiplier(1.35f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERFERENCE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHOCKING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERPORT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PURIFYINGTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .armor(180.0f, 30.0f, 12.0f, 2.3f, 2.0f, 4.0f)
                .bow(0.7143f, 1.5f, 7.8f)
                .build());

        GHOSTLYSTONE = id("ghostlystone");
        SmitheryAPI.registerMaterial(GHOSTLYSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB8C8C8)
                        .binderMultiplier(1.0f)
                .build());

        HEART_CRYSTAL = id("heart_crystal");
        SmitheryAPI.registerMaterial(HEART_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.4f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFB60000)
                        .binderMultiplier(1.25f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLOODLUSTTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COTLIFESTEAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLOODLUSTTRAIT), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FIRSTGUARDTRAIT_ARMOR), armorPieces())
                .armor(88.0f, 7.0f, 12.0f, 1.8f, 3.5f, 4.0f)
                .bow(0.9091f, 1.2f, 4.0f)
                .build());

        HEPHAESTITE = id("hephaestite");
        SmitheryAPI.registerMaterial(HEPHAESTITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(3.5f)
                        .attackDamage(2.2f)
                        .durabilityPerIngot(100)
                        .meltingTemp(600.0f)
                        .partColor(0xFF7B2600)
                        .binderMultiplier(0.8f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FLAMMABLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHOT_ARMOR), armorPieces())
                .armor(40.0f, 13.6f, 40.0f, 0.6f, 0.5f, 10.0f)
                .bow(0.7692f, 1.0f, 1.5f)
                .build());

        STRONG_TOFU_GEM = id("strong_tofu_gem");
        SmitheryAPI.registerMaterial(STRONG_TOFU_GEM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.5f)
                        .attackDamage(6.3f)
                        .durabilityPerIngot(2300)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFFEE0C8)
                        .binderMultiplier(1.35f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RAGING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), armorPieces())
                .armor(150.0f, 22.0f, 32.0f, 1.7f, 1.5f, 12.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY), armorPieces())
                .bow(0.8333f, 1.2f, 2.8f)
                .build());

        INFERNIUM = id("infernium");
        SmitheryAPI.registerMaterial(INFERNIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(9)
                        .miningSpeed(20.6f)
                        .attackDamage(25.7f)
                        .durabilityPerIngot(16000)
                        .meltingTemp(6000.0f)
                        .partColor(0xFF5D2214)
                        .binderMultiplier(2.2f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FLAMMABLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HELLISH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NAPHTHA), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AUTOFORGE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INFERNOTRAIT_ARMOR), armorPieces())
                .armor(120.0f, 70.2f, 131.072f, 8.8f, 5.0f, 131.072f)
                .bow(0.8333f, 1.2f, 12.4f)
                .arrowShaft(2.0f, 30)
                .build());

        INSANIUM = id("insanium");
        SmitheryAPI.registerMaterial(INSANIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(15.6f)
                        .attackDamage(22.2f)
                        .durabilityPerIngot(1800)
                        .meltingTemp(3000.0f)
                        .partColor(0xFF7B03C3)
                        .binderMultiplier(2.4f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHUNKY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GIANTSLAYER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PROSPEROUS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDURANCE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PROSPEROUS), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TRUEDEFENSETRAIT_ARMOR), armorPieces())
                .armor(300.0f, 52.2f, 100.0f, 4.5f, 5.0f, 70.0f)
                .bow(1.1364f, 2.5f, 8.2f)
                .arrowShaft(2.25f, 10)
                .build());

        EXPERIENCE = id("experience");
        SmitheryAPI.registerMaterial(EXPERIENCE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(4.0f)
                        .attackDamage(4.2f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFB2FF59)
                        .binderMultiplier(1.2f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ESTABLISHED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WRITABLE1, java.util.Map.of("bonus_slots", 1)), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WRITABLE2, java.util.Map.of("bonus_slots", 1)), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AMBITIOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WRITABLE1, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WRITABLE2, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .armor(12.0f, 16.2f, 10.0f, 1.2f, 1.0f, 8.0f)
                .bow(1.3889f, 1.1f, 1.0f)
                .build());

        LIMONITE = id("limonite");
        SmitheryAPI.registerMaterial(LIMONITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC8923E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(35.0f, 15.12f, 28.8f, 15.2471f, 0.0f, 28.8f)
                .build());

        MANGANESE = id("manganese");
        SmitheryAPI.registerMaterial(MANGANESE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF546E7A)
                        .binderMultiplier(1.2f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KUNGFU_ARMOR), armorPieces())
                .armor(100.0f, 16.0f, 100.0f, 1.5f, 1.75f, 70.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.4545f, 1.5f, 3.6f)
                .build());

        MANGANESE_STEEL = id("manganese_steel");
        SmitheryAPI.registerMaterial(MANGANESE_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.0f)
                        .attackDamage(8.2f)
                        .durabilityPerIngot(2200)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF687377)
                        .binderMultiplier(1.4f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .armor(200.0f, 19.0f, 200.0f, 2.0f, 2.5f, 140.0f)
                .bow(0.3448f, 1.8f, 7.6f)
                .build());

        SLIMELEAF_ORANGE = id("slimeleaf_orange");
        SmitheryAPI.registerMaterial(SLIMELEAF_ORANGE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8923E)
                        .binderMultiplier(1.0f)
                .fletching(0.8f, 1.25f)
                .build());

        SLIMELEAF_PURPLE = id("slimeleaf_purple");
        SmitheryAPI.registerMaterial(SLIMELEAF_PURPLE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFA86FD8)
                        .binderMultiplier(1.0f)
                .fletching(0.8f, 1.25f)
                .build());

        SLIMEVINE_ORANGE = id("slimevine_orange");
        SmitheryAPI.registerMaterial(SLIMEVINE_ORANGE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8823E)
                        .binderMultiplier(1.0f)
                .build());

        SLIMEVINE_PURPLE = id("slimevine_purple");
        SmitheryAPI.registerMaterial(SLIMEVINE_PURPLE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF986FC8)
                        .binderMultiplier(1.0f)
                .build());

        METEOR = id("meteor");
        SmitheryAPI.registerMaterial(METEOR, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.3f)
                        .attackDamage(8.5f)
                        .durabilityPerIngot(1220)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF85100B)
                        .binderMultiplier(1.2f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_BLASTING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FLAMMABLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERKNOCKBACK), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SKELETAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VISIONTRAIT_ARMOR), armorPieces())
                .armor(200.0f, 24.0f, 76.8f, 2.1f, 2.25f, 76.8f)
                .bow(0.2778f, 2.8f, 22.0f)
                .arrowShaft(1.5f, 5)
                .build());

        MITHMINITE = id("mithminite");
        SmitheryAPI.registerMaterial(MITHMINITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(20.5f)
                        .attackDamage(30.4f)
                        .durabilityPerIngot(3400)
                        .meltingTemp(4000.0f)
                        .partColor(0xFFA04E7C)
                        .colorCycle(40, 0xFFD59DBF, 0xFF6B003A) // [auto-color]
                        .binderMultiplier(2.4f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INSATIABLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.REACH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE2, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(500.0f, 52.0f, 120.0f, 8.0f, 4.5f, 140.0f)
                .bow(1.5385f, 1.4f, 18.0f)
                .build());

        MITHRILLIUM = id("mithrillium");
        SmitheryAPI.registerMaterial(MITHRILLIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(14.3f)
                        .attackDamage(20.6f)
                        .durabilityPerIngot(9800)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF124356)
                        .colorCycle(10, 0xFF0E4D65, 0xFF163A48) // [auto-color]
                        .binderMultiplier(1.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARCANE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MODIFIABLE1, java.util.Map.of("bonus_slots", 1)), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(340.0f, 36.0f, 80.0f, 4.0f, 2.0f, 100.0f)
                .bow(1.25f, 1.0f, 10.0f)
                .build());

        MODULARIUM = id("modularium");
        SmitheryAPI.registerMaterial(MODULARIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.1f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(420)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFBABABA)
                        .binderMultiplier(1.2f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTICORROSION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INVIGORATING_ARMOR), armorPieces())
                .armor(128.0f, 15.5f, 32.0f, 1.4f, 1.0f, 20.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.8333f, 1.2f, 3.0f)
                .build());

        MUD = id("mud");
        SmitheryAPI.registerMaterial(MUD, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.4f)
                        .attackDamage(1.4f)
                        .durabilityPerIngot(20)
                        .meltingTemp(0.0f)
                        .partColor(0xFF333139)
                        .binderMultiplier(0.12f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOFTY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOFTY), armorPieces())
                .armor(20.0f, 4.5f, 1.28f, 0.2f, 0.25f, 1.28f)
                .bow(0.6667f, 1.0f, 1.0f)
                .build());

        NYLON_CLOTH = id("nylon_cloth");
        SmitheryAPI.registerMaterial(NYLON_CLOTH, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3949AB)
                        .binderMultiplier(1.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NAPHTHA), allToolTypes())
                .fletching(1.0f, 2.5f)
                .build());

        NYLON_STRING = id("nylon_string");
        SmitheryAPI.registerMaterial(NYLON_STRING, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3949AB)
                        .binderMultiplier(1.0f)
                .bowstring(2.25f)
                .build());

        PERFECT = id("perfect");
        SmitheryAPI.registerMaterial(PERFECT, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.0f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1000)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF4CAF50)
                        .binderMultiplier(1.0f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PERFECTIONISTTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LUCKYTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PERFECTIONISTTRAIT_ARMOR), armorPieces())
                .armor(500.0f, 25.0f, 100.0f, 4.0f, 5.0f, 100.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.PERFECTIONIST),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.0f, 2.0f, 5.0f)
                .build());

        PHOENIXITE = id("phoenixite");
        SmitheryAPI.registerMaterial(PHOENIXITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8862E)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.GETLUCKY)
                .build());

        POOP = id("poop");
        SmitheryAPI.registerMaterial(POOP, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.5f)
                        .attackDamage(1.4f)
                        .durabilityPerIngot(11)
                        .meltingTemp(0.0f)
                        .partColor(0xFF795548)
                        .binderMultiplier(0.19f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.POISONOUS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.POOPY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOFTY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.POOPY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SOFTY), armorPieces())
                .armor(11.0f, 4.5f, 19.0f, 1.4f, 1.9f, 810.0f)
                .bow(0.9091f, 4.5f, 1.4f)
                .build());

        PROTONIUM = id("protonium");
        SmitheryAPI.registerMaterial(PROTONIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(9)
                        .miningSpeed(13.6f)
                        .attackDamage(35.6f)
                        .durabilityPerIngot(32000)
                        .meltingTemp(6000.0f)
                        .partColor(0xFFF080E4)
                        .binderMultiplier(3.6f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MATTERTRAIT12), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TRUEDEFENSETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ULTRADENSE_ARMOR), armorPieces())
                .armor(800.0f, 78.0f, 819.2f, 14.0f, 10.0f, 819.2f)
                .addUniversalModifier(SoaSmitheryModifiers.MATTERTRAIT2)
                .bow(0.0833f, 5.0f, 80.0f)
                .build());

        PROXII = id("proxii");
        SmitheryAPI.registerMaterial(PROXII, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(6.8f)
                        .attackDamage(4.21f)
                        .durabilityPerIngot(625)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFBAD4B2)
                        .binderMultiplier(1.25f)
                , 5)
                .armor(150.0f, 31.0f, 100.0f, 1.1f, 2.0f, 30.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CURVATURE), allToolTypes())
                .bow(0.35f, 0.5f, 3.0f)
                .build());

        PUMPKIN = id("pumpkin");
        SmitheryAPI.registerMaterial(PUMPKIN, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(3.0f)
                        .attackDamage(4.6f)
                        .durabilityPerIngot(200)
                        .meltingTemp(600.0f)
                        .partColor(0xFFE38A1D)
                        .binderMultiplier(0.6f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HALLOWEENTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LUCKYTRAIT_ARMOR), armorPieces())
                .armor(40.0f, 12.0f, 16.64f, 0.8f, 0.5f, 16.64f)
                .bow(0.7143f, 1.2f, 1.4f)
                .build());

        RAVAGING = id("ravaging");
        SmitheryAPI.registerMaterial(RAVAGING, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(32.0f)
                        .attackDamage(8.4f)
                        .durabilityPerIngot(400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFE5E5E5)
                        .binderMultiplier(1.5f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRUMBLING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PULVERIZING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_RELENTLESS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INFERNOTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INVIGORATING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PULVERIZING), armorPieces())
                .armor(240.0f, 25.2f, 50.0f, 3.0f, 1.5f, 38.0f)
                .bow(4.0f, 1.2f, 0.0f)
                .build());

        REMORSEFUL = id("remorseful");
        SmitheryAPI.registerMaterial(REMORSEFUL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(14.5f)
                        .attackDamage(10.0f)
                        .durabilityPerIngot(540)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF343434)
                        .binderMultiplier(1.4f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.APOCALYPSE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEFILED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SACRIFICIALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEFILED), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL_ARMOR), armorPieces())
                .armor(230.0f, 27.5f, 64.0f, 3.2f, 2.5f, 32.0f)
                .bow(0.1667f, 5.0f, 23.5f)
                .build());

        RIME = id("rime");
        SmitheryAPI.registerMaterial(RIME, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.5f)
                        .attackDamage(9.2f)
                        .durabilityPerIngot(540)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF737EEA)
                        .binderMultiplier(1.25f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FREEZING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYONICTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .armor(180.0f, 22.0f, 36.0f, 2.5f, 2.0f, 32.0f)
                .bow(0.8333f, 2.0f, 3.5f)
                .build());

        RUBBER_BAND = id("rubber_band");
        SmitheryAPI.registerMaterial(RUBBER_BAND, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFEF6C00)
                        .binderMultiplier(1.0f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_LUMINIFEROUS), allToolTypes())
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT), SmitheryPartTypes.BOWSTRING)
                .bowstring(2.5f)
                .build());

        SAKURA_DIAMOND = id("sakura_diamond");
        SmitheryAPI.registerMaterial(SAKURA_DIAMOND, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.3f)
                        .attackDamage(6.5f)
                        .durabilityPerIngot(1700)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFD42669)
                        .binderMultiplier(1.25f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLOODLUSTTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PINKYTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AMBITIOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .armor(100.0f, 18.0f, 40.0f, 1.5f, 2.0f, 24.0f)
                .bow(0.9091f, 1.2f, 4.0f)
                .build());

        SCARLITE = id("scarlite");
        SmitheryAPI.registerMaterial(SCARLITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(3.5f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(140)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFFAACA9)
                        .binderMultiplier(1.2f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COTLIFESTEAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEFILED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SACRIFICIALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEFILED), armorPieces())
                .armor(150.0f, 21.2f, 23.0f, 1.8f, 4.0f, 12.0f)
                .bow(0.6667f, 1.0f, 2.5f)
                .build());

        SCORCHED = id("scorched");
        SmitheryAPI.registerMaterial(SCORCHED, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.2f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(260)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF5A4638)
                        .binderMultiplier(1.1f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AUTOSMELT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.THRONY_ARMOR), armorPieces())
                .armor(20.0f, 13.0f, 28.16f, 1.3f, 1.0f, 28.16f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7143f, 1.2f, 2.0f)
                .build());

        SEARED = id("seared");
        SmitheryAPI.registerMaterial(SEARED, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.2f)
                        .attackDamage(4.7f)
                        .durabilityPerIngot(100)
                        .meltingTemp(600.0f)
                        .partColor(0xFF4A4440)
                        .binderMultiplier(0.8f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUBTERRANEAN_ARMOR), armorPieces())
                .armor(30.0f, 9.0f, 15.36f, 1.1f, 0.25f, 15.36f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.625f, 1.1f, 1.2f)
                .build());

        SHADOWIUM = id("shadowium");
        SmitheryAPI.registerMaterial(SHADOWIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(16.5f)
                        .attackDamage(15.6f)
                        .durabilityPerIngot(700)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF65307B)
                        .binderMultiplier(1.6f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DARK), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEPTHDIGGER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MADNESSTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_STIFLING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUBTERRANEAN_ARMOR), armorPieces())
                .armor(300.0f, 47.2f, 70.0f, 5.0f, 4.0f, 50.0f)
                .bow(2.2222f, 1.5f, 4.0f)
                .build());

        SKELETAL = id("skeletal");
        SmitheryAPI.registerMaterial(SKELETAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC8C4B0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(86.0f, 23.22f, 111.36f, 19.0588f, 0.0f, 111.36f)
                .build());

        SLIMELEAF_BLUE = id("slimeleaf_blue");
        SmitheryAPI.registerMaterial(SLIMELEAF_BLUE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FB8D8)
                        .binderMultiplier(1.0f)
                .fletching(0.8f, 1.25f)
                .build());

        SLIMEVINE_BLUE = id("slimevine_blue");
        SmitheryAPI.registerMaterial(SLIMEVINE_BLUE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF5FA8C8)
                        .binderMultiplier(1.0f)
                .build());

        SPECTRE = id("spectre");
        SmitheryAPI.registerMaterial(SPECTRE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.2f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF6B909F)
                        .binderMultiplier(1.2f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_REND1), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SPECTRAL_ARMOR), armorPieces())
                .armor(200.0f, 23.3f, 100.0f, 1.6f, 2.0f, 70.0f)
                .bow(0.6667f, 1.0f, 2.5f)
                .build());

        SPECTRE_STRING = id("spectre_string");
        SmitheryAPI.registerMaterial(SPECTRE_STRING, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBDE0DD)
                        .binderMultiplier(1.0f)
                .bowstring(2.0f)
                .build());

        STAINLESS_STEEL = id("stainless_steel");
        SmitheryAPI.registerMaterial(STAINLESS_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(11.0f)
                        .attackDamage(8.5f)
                        .durabilityPerIngot(3200)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF7C7C7C)
                        .binderMultiplier(2.3f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTICORROSION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPLITTING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.FORTIFIEDTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INDOMITABLE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .armor(256.0f, 27.0f, 100.0f, 2.7f, 3.5f, 60.0f)
                .bow(0.3846f, 2.4f, 15.6f)
                .arrowShaft(2.0f, 10)
                .build());

        STELLAR_ALLOY = id("stellar_alloy");
        SmitheryAPI.registerMaterial(STELLAR_ALLOY, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFDCE8F0)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.SPARTAN)
                .addUniversalModifier(SoaSmitheryModifiers.MOMENTUM)
                .build());

        TERRA_ALLOY = id("terra_alloy");
        SmitheryAPI.registerMaterial(TERRA_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(21.6f)
                        .attackDamage(33.6f)
                        .durabilityPerIngot(6000)
                        .meltingTemp(8000.0f)
                        .partColor(0xFF598190)
                        .binderMultiplier(2.25f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESBLESSING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESWRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUASPEED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHOT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VENGEFUL_ARMOR), armorPieces())
                .armor(800.0f, 82.0f, 120.0f, 8.0f, 8.0f, 80.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL),
                        SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL), armorPieces())
                .bow(0.25f, 2.0f, 34.0f)
                .build());

        TERRESTRIAL = id("terrestrial");
        SmitheryAPI.registerMaterial(TERRESTRIAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(24.0f)
                        .attackDamage(17.6f)
                        .durabilityPerIngot(4200)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF7A8F5A)
                        .binderMultiplier(2.6f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESBLESSING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESPOWER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.NATURESWRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WARPDRAINTRAIT_ARMOR), armorPieces())
                .armor(300.0f, 50.0f, 120.0f, 5.0f, 4.0f, 70.0f)
                .bow(0.8333f, 3.2f, 16.4f)
                .build());

        TOFU_GEM = id("tofu_gem");
        SmitheryAPI.registerMaterial(TOFU_GEM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(3.5f)
                        .attackDamage(4.2f)
                        .durabilityPerIngot(200)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFF3F3E8)
                        .binderMultiplier(1.15f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .armor(100.0f, 15.0f, 12.0f, 1.6f, 1.0f, 4.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7143f, 1.2f, 1.8f)
                .build());

        TRITANIUM = id("tritanium");
        SmitheryAPI.registerMaterial(TRITANIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF324143)
                        .binderMultiplier(1.0f)
                .build());

        WITHERBONE = id("witherbone");
        SmitheryAPI.registerMaterial(WITHERBONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A4A45)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.APOCALYPSE)
                .build());

        YELLORIUM = id("yellorium");
        SmitheryAPI.registerMaterial(YELLORIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.6f)
                        .attackDamage(9.2f)
                        .durabilityPerIngot(200)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFE3E485)
                        .binderMultiplier(1.1f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BRIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DECAY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GLIMMER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_LUMINIFEROUS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DECAY), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_RADIANT_ARMOR), armorPieces())
                .armor(160.0f, 21.0f, 10.0f, 2.2f, 1.0f, 8.0f)
                .bow(0.6667f, 1.2f, 3.0f)
                .build());

    }

    private static void registerTinkersEvolution() {
        ADVANCED_ALLOY = id("advanced_alloy");
        SmitheryAPI.registerMaterial(ADVANCED_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(800)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF8F9AA5)
                        .binderMultiplier(1.0f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_IMPACT_FORCE)
                .bow(0.85f, 1.25f, 2.0f)
                .build());

        ALUMINIUM = id("aluminium");
        SmitheryAPI.registerMaterial(ALUMINIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(10.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(225)
                        .meltingTemp(600.0f)
                        .partColor(0xFFBFC8D0)
                        .binderMultiplier(0.9f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .bow(1.0f, 1.1f, 1.0f)
                .build());

        APATITE = id("apatite");
        SmitheryAPI.registerMaterial(APATITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(375)
                        .meltingTemp(600.0f)
                        .partColor(0xFF3495D4)
                        .binderMultiplier(0.8f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.CHEAPSKATE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_FERTILIZING)
                .build());

        BLACK_QUARTZ = id("black_quartz");
        SmitheryAPI.registerMaterial(BLACK_QUARTZ, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.5f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(280)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF121110)
                        .binderMultiplier(0.8f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DEPTHDIGGER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), armorPieces())
                .bow(1.2f, 1.0f, 0.0f)
                .build());

        BLOODWOOD = id("bloodwood");
        SmitheryAPI.registerMaterial(BLOODWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(350)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF6E1B10)
                        .binderMultiplier(0.75f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ECOLOGICAL_ARMOR), armorPieces())
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(0.8f, 1.2f, 2.5f)
                .arrowShaft(1.5f, 0)
                .build());

        CARBON_FIBER = id("carbon_fiber");
        SmitheryAPI.registerMaterial(CARBON_FIBER, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF33363A)
                        .binderMultiplier(1.3f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_RELENTLESS)
                .bow(1.6f, 1.0f, 1.5f)
                .build());

        CRYSTAL_MATRIX = id("crystal_matrix");
        SmitheryAPI.registerMaterial(CRYSTAL_MATRIX, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(13.0f)
                        .attackDamage(9.5f)
                        .durabilityPerIngot(3200)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF418A86)
                        .binderMultiplier(1.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK3), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.INSATIABLE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRIDEFUL_ARMOR), armorPieces())
                .armor(600.0f, 38.0f, 400.0f, 4.0f, 3.0f, 300.0f)
                .bow(1.5f, 1.0f, 2.0f)
                .build());

        DARKWOOD = id("darkwood");
        SmitheryAPI.registerMaterial(DARKWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(112)
                        .meltingTemp(600.0f)
                        .partColor(0xFF1E3972)
                        .binderMultiplier(0.9f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_SUNDERING)
                .bow(0.9f, 1.1f, 1.0f)
                .arrowShaft(1.0f, 10)
                .build());

        DIAMANTINE_CRYSTAL = id("diamantine_crystal");
        SmitheryAPI.registerMaterial(DIAMANTINE_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.5f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(960)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF787BD6)
                        .binderMultiplier(1.25f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .bow(1.0f, 1.2f, 4.0f)
                .build());

        DRAGONSTONE = id("dragonstone");
        SmitheryAPI.registerMaterial(DRAGONSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE771B9)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_FAE_VOICE),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .build());

        DREAMWOOD = id("dreamwood");
        SmitheryAPI.registerMaterial(DREAMWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(110)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F807B)
                        .binderMultiplier(1.1f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_AURA_SIPHON)
                .bow(1.1f, 1.1f, 1.0f)
                .arrowShaft(1.25f, 25)
                .build());

        EMERALDIC_CRYSTAL = id("emeraldic_crystal");
        SmitheryAPI.registerMaterial(EMERALDIC_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1130)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF07CD03)
                        .binderMultiplier(1.25f)
                , 4)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.COLDBLOODED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.MOMENTUM)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .bow(0.85f, 1.3f, 6.0f)
                .build());

        ENDERIUM = id("enderium");
        SmitheryAPI.registerMaterial(ENDERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(8.0f)
                        .attackDamage(9.0f)
                        .durabilityPerIngot(1700)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF0B494A)
                        .binderMultiplier(1.25f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.ENDERFERENCE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MORTAL_WOUNDS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(0.75f, 1.5f, 7.0f)
                .build());

        ENERGETIC_METAL = id("energetic_metal");
        SmitheryAPI.registerMaterial(ENERGETIC_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.5f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(512)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFA61B1B)
                        .binderMultiplier(0.8f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ELECTRIC)
                .bow(0.75f, 1.0f, 3.5f)
                .build());

        ENORI_CRYSTAL = id("enori_crystal");
        SmitheryAPI.registerMaterial(ENORI_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(160)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFD2D2D2)
                        .binderMultiplier(1.0f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .bow(0.7f, 1.35f, 2.0f)
                .build());

        ESSENCE_METAL = id("essence_metal");
        SmitheryAPI.registerMaterial(ESSENCE_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF679365)
                        .binderMultiplier(1.5f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.ESTABLISHED)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_SUNDERING)
                .bow(1.5f, 0.7f, 0.0f)
                .build());

        FLUIX_STEEL = id("fluix_steel");
        SmitheryAPI.registerMaterial(FLUIX_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.5f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(450)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF875D93)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_PIEZOELECTRIC),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(0.7f, 1.3f, 6.0f)
                .build());

        FLUX_CRYSTAL = id("flux_crystal");
        SmitheryAPI.registerMaterial(FLUX_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(500)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF860303)
                        .colorCycle(32, 0xFF6A0305, 0xFF670204, 0xFF960403, 0xFF950403, 0xFF940403, 0xFF950403) // [auto-color]
                        .binderMultiplier(0.9f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ENERGIZED)
                .bow(1.2f, 1.0f, 0.0f)
                .build());

        FLUXED_STRING = id("fluxed_string");
        SmitheryAPI.registerMaterial(FLUXED_STRING, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF7F7F7)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK),
                        SmitheryPartTypes.BOWSTRING)
                .bowstring(1.25f)
                .build());

        FLUXED_ELECTRUM = id("fluxed_electrum");
        SmitheryAPI.registerMaterial(FLUXED_ELECTRUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFC8523E)
                        .binderMultiplier(0.6f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ENERGIZED)
                .bow(0.8f, 1.25f, 2.0f)
                .build());

        FUSEWOOD = id("fusewood");
        SmitheryAPI.registerMaterial(FUSEWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.5f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(24)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF0D352C)
                        .binderMultiplier(1.0f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.BLASTING)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(0.75f, 1.25f, 4.0f)
                .arrowShaft(1.25f, 4)
                .build());

        GELID_ENDERIUM = id("gelid_enderium");
        SmitheryAPI.registerMaterial(GELID_ENDERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.6f)
                        .attackDamage(9.0f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF177C95)
                        .colorCycle(152, 0xFF2AAFF9, 0xFF0E6363, 0xFF0E6363, 0xFF2AAFF9, 0xFF0E6363, 0xFF0E6363) // [auto-color]
                        .binderMultiplier(1.35f)
                , 5)
                .armor(250.0f, 25.5f, 0.0f, 2.5f, 0.0f, 0.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ENERGIZED)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_JUGGERNAUT)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CHILLING_TOUCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ENERGIZED_ARMOR), armorPieces())
                .bow(0.75f, 1.5f, 6.5f)
                .build());

        GELID_GEM = id("gelid_gem");
        SmitheryAPI.registerMaterial(GELID_GEM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(11.6f)
                        .attackDamage(8.7f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF29ACBE)
                        .binderMultiplier(1.25f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_ENERGIZED)
                .bow(1.5f, 1.1f, 7.2f)
                .build());

        GHOSTWOOD = id("ghostwood");
        SmitheryAPI.registerMaterial(GHOSTWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.0f)
                        .attackDamage(2.5f)
                        .durabilityPerIngot(24)
                        .meltingTemp(0.0f)
                        .partColor(0xFF282828)
                        .binderMultiplier(0.9f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_FOOT_FLEET)
                .bow(1.5f, 1.0f, 0.0f)
                .arrowShaft(0.9f, 12)
                .fletching(1.0f, 0.9f)
                .build());

        POLYETHYLENE = id("polyethylene");
        SmitheryAPI.registerMaterial(POLYETHYLENE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(5.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(220)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E8E4)
                        .binderMultiplier(0.5f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.CHEAP)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_FOOT_FLEET)
                .bow(1.75f, 0.6f, 0.0f)
                .arrowShaft(0.75f, 75)
                .build());

        INFINITY_METAL = id("infinity_metal");
        SmitheryAPI.registerMaterial(INFINITY_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(127)
                        .miningSpeed(666.0f)
                        .attackDamage(32767.0f)
                        .durabilityPerIngot(16384)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFE5C4C4)
                        .colorCycle(233, 0xFFFFBEBF, 0xFFE9B8B8, 0xFFE2C6C7, 0xFFE2DFDD, 0xFFD8D0CB, 0xFFDEB2B3) // [auto-color]
                        .binderMultiplier(4.5f)
                , 5)
                .armor(24000.0f, 4096.0f, 1337.0f, 666.0f, 128.0f, 10000.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_INFINITUM)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_OMNIPOTENCE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CELESTIAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_ETERNITY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GALE_FORCE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_NULL_ALMIGHTY_ARMOR), armorPieces())
                .bow(100.0f, 3.0f, 9999.0f)
                .build());

        LIVINGROCK = id("livingrock");
        SmitheryAPI.registerMaterial(LIVINGROCK, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(170)
                        .meltingTemp(600.0f)
                        .partColor(0xFFC9C2B1)
                        .binderMultiplier(0.9f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.STONEBOUND)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SUNDERING),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(0.6f, 0.9f, 0.0f)
                .build());

        LIVINGWOOD = id("livingwood");
        SmitheryAPI.registerMaterial(LIVINGWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(2.5f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(80)
                        .meltingTemp(0.0f)
                        .partColor(0xFF421909)
                        .binderMultiplier(1.0f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE, java.util.Map.of("bonus_slots", 1))
                .bow(1.2f, 1.0f, 0.0f)
                .arrowShaft(1.0f, 0)
                .build());

        LONSDALEITE = id("lonsdaleite");
        SmitheryAPI.registerMaterial(LONSDALEITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(840)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFBFE8E4)
                        .binderMultiplier(1.2f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE, java.util.Map.of("bonus_slots", 1))
                .bow(0.9f, 1.2f, 1.5f)
                .build());

        LUMIUM = id("lumium");
        SmitheryAPI.registerMaterial(LUMIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(9.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(250)
                        .meltingTemp(600.0f)
                        .partColor(0xFFCE7F2D)
                        .binderMultiplier(0.8f)
                , 1)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_LUMINIFEROUS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_OPPORTUNIST)
                .bow(1.0f, 1.15f, 2.0f)
                .build());

        MANA_DIAMOND = id("mana_diamond");
        SmitheryAPI.registerMaterial(MANA_DIAMOND, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF00869B)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .build());

        MANA_PEARL = id("mana_pearl");
        SmitheryAPI.registerMaterial(MANA_PEARL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF00E4E5)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDSPEED), SmitheryPartTypes.ARCANE_FOCUS)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .build());

        MANA_STRING = id("mana_string");
        SmitheryAPI.registerMaterial(MANA_STRING, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9DFFF4)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOWSTRING)
                .bowstring(1.0f)
                .build());

        MEAT_METAL = id("meat_metal");
        SmitheryAPI.registerMaterial(MEAT_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.25f)
                        .attackDamage(2.5f)
                        .durabilityPerIngot(135)
                        .meltingTemp(0.0f)
                        .partColor(0xFF392717)
                        .binderMultiplier(2.5f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.TASTY)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_REJUVENATING)
                .build());

        NEUTRONIUM = id("neutronium");
        SmitheryAPI.registerMaterial(NEUTRONIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(13.0f)
                        .attackDamage(16.0f)
                        .durabilityPerIngot(11111)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF2E3030)
                        .binderMultiplier(1.0f)
                , 5)
                .armor(1000.0f, 60.0f, 800.0f, 10.0f, 6.0f, 600.0f)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addUniversalModifier(SoaSmitheryModifiers.HEAVY)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CONDENSING),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_REACTIVE_ARMOR), armorPieces())
                .bow(0.25f, 4.0f, 20.0f)
                .build());

        OBDISIAN = id("obdisian");
        SmitheryAPI.registerMaterial(OBDISIAN, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(0.0f)
                        .attackDamage(4.2f)
                        .durabilityPerIngot(0)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF3A2E4A)
                        .binderMultiplier(1.0f)
                , 3)
                .build());

        PALIS_CRYSTAL = id("palis_crystal");
        SmitheryAPI.registerMaterial(PALIS_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(4.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(150)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF0D0E7F)
                        .binderMultiplier(1.1f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.ESTABLISHED)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .bow(0.75f, 1.1f, 0.0f)
                .build());

        PINK_METAL = id("pink_metal");
        SmitheryAPI.registerMaterial(PINK_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.5f)
                        .attackDamage(8.5f)
                        .durabilityPerIngot(1789)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFF6BCEE)
                        .binderMultiplier(1.0f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MORTAL_WOUNDS)
                .addUniversalModifier(SoaSmitheryModifiers.UNNATURAL)
                .bow(1.1f, 1.0f, 4.0f)
                .build());

        PLATINUM = id("platinum");
        SmitheryAPI.registerMaterial(PLATINUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.0f)
                        .attackDamage(6.5f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF4771C0)
                        .binderMultiplier(0.8f)
                , 4)
                .armor(4.0f, 18.0f, -10.08f, 31.7647f, 0.0f, -10.08f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.COLDBLOODED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRIDEFUL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DIVINE_GRACE_ARMOR), armorPieces())
                .bow(1.0f, 0.8f, 8.0f)
                .build());

        QUICKSILVER = id("quicksilver");
        SmitheryAPI.registerMaterial(QUICKSILVER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF323130)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE, java.util.Map.of("bonus_slots", 1))
                .build());

        REFINED_GLOWSTONE = id("refined_glowstone");
        SmitheryAPI.registerMaterial(REFINED_GLOWSTONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(10.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFFFEA86)
                        .binderMultiplier(0.8f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.SHARP)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_LUMINIFEROUS)
                .bow(1.0f, 1.25f, 4.0f)
                .build());

        REFINED_OBSIDIAN = id("refined_obsidian");
        SmitheryAPI.registerMaterial(REFINED_OBSIDIAN, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.5f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1100)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF7861A2)
                        .binderMultiplier(1.25f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DURITOS_RANCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .bow(0.65f, 1.35f, 7.0f)
                .build());

        RESTONIA_CRYSTAL = id("restonia_crystal");
        SmitheryAPI.registerMaterial(RESTONIA_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(7.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(150)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFF10000)
                        .binderMultiplier(0.75f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_PIEZOELECTRIC),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(1.5f, 0.8f, 0.0f)
                .build());

        RUBBER = id("rubber");
        SmitheryAPI.registerMaterial(RUBBER, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.0f)
                        .attackDamage(1.5f)
                        .durabilityPerIngot(180)
                        .meltingTemp(600.0f)
                        .partColor(0xFF3E3A36)
                        .binderMultiplier(0.5f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SQUEAKY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(2.0f, 0.5f, 0.0f)
                .build());

        SIGNALUM = id("signalum");
        SmitheryAPI.registerMaterial(SIGNALUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(13.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(150)
                        .meltingTemp(600.0f)
                        .partColor(0xFFCF4606)
                        .binderMultiplier(0.7f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_AFTERSHOCK)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_RELENTLESS)
                .build());

        SUNNARIUM = id("sunnarium");
        SmitheryAPI.registerMaterial(SUNNARIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(12.0f)
                        .attackDamage(12.0f)
                        .durabilityPerIngot(580)
                        .meltingTemp(4000.0f)
                        .partColor(0xFFE8D22E)
                        .binderMultiplier(1.25f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_LUMINIFEROUS)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_PHOTOSYNTHETIC)
                .bow(2.0f, 1.1f, 4.0f)
                .build());

        TIN = id("tin");
        SmitheryAPI.registerMaterial(TIN, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(150)
                        .meltingTemp(600.0f)
                        .partColor(0xFF9C9C9C)
                        .binderMultiplier(0.8f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .bow(0.9f, 1.25f, 0.0f)
                .build());

        UNIVERSAL_METAL = id("universal_metal");
        SmitheryAPI.registerMaterial(UNIVERSAL_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(15.0f)
                        .attackDamage(10.0f)
                        .durabilityPerIngot(17)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF781E74)
                        .binderMultiplier(2.0f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.CRUMBLING)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_RUINATION)
                .bow(1.25f, 1.25f, 0.0f)
                .build());

        VOID_CRYSTAL = id("void_crystal");
        SmitheryAPI.registerMaterial(VOID_CRYSTAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(3.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(170)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF080808)
                        .binderMultiplier(0.8f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.CHEAPSKATE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CRYSTALLINE)
                .bow(1.25f, 0.6f, 0.0f)
                .build());

        VOID_METAL = id("void_metal");
        SmitheryAPI.registerMaterial(VOID_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(14.6f)
                        .attackDamage(11.0f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF2E164C)
                        .binderMultiplier(1.4f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DARK), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MADNESSTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CHILLING_TOUCH_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING_ARMOR), armorPieces())
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.4f, 0.8f, 8.0f)
                .build());

        WEATHER_CRYSTAL = id("weather_crystal");
        SmitheryAPI.registerMaterial(WEATHER_CRYSTAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC9A8D3)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_THUNDERGOD_WRATH),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .build());

    }

    private static void registerPlustic() {
        AETHIUM = id("aethium");
        SmitheryAPI.registerMaterial(AETHIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(14.2f)
                        .attackDamage(13.5f)
                        .durabilityPerIngot(2200)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFE8B25A)
                        .binderMultiplier(0.9f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.MUSICOFTHESPHERES)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CORRUPTING)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_EXECUTOR)
                .bow(0.38f, 3.0f, 12.0f)
                .build());

        ALUMITE = id("alumite");
        SmitheryAPI.registerMaterial(ALUMITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(10.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(700)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFDE8CCC)
                        .binderMultiplier(1.1f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.GLOBAL)
                .bow(0.65f, 1.6f, 7.0f)
                .build());

        AMBER = id("amber");
        SmitheryAPI.registerMaterial(AMBER, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(4.6f)
                        .attackDamage(5.7f)
                        .durabilityPerIngot(730)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFF09D34)
                        .binderMultiplier(1.0f)
                , 3)
                .armor(100.0f, 16.5f, 11.2f, 1.6f, 1.5f, 11.2f)
                .addUniversalModifier(SoaSmitheryModifiers.SHOCKING)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_OPPORTUNIST),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.THUNDERING),
                        SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE, SmitheryPartTypes.ARROW_SHAFT)
                .arrowShaft(1.0f, 5)
                .build());

        AQUAMARINE = id("aquamarine");
        SmitheryAPI.registerMaterial(AQUAMARINE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(7.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(175)
                        .meltingTemp(600.0f)
                        .partColor(0xFF006FC1)
                        .binderMultiplier(0.8f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUADYNAMIC), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ABSORBENT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .bow(0.75f, 1.0f, 0.0f)
                .build());

        SAPPHIRE_AOA = id("sapphire_aoa");
        SmitheryAPI.registerMaterial(SAPPHIRE_AOA, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(6.4f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF3993C2)
                        .binderMultiplier(1.25f)
                , 4)
                .armor(120.0f, 34.56f, 46.08f, 31.7647f, 0.0f, 46.08f)
                .addUniversalModifier(SoaSmitheryModifiers.AQUADYNAMIC)
                .bow(1.3f, 1.5f, 4.0f)
                .build());

        AWAKENED_PLUSTIC = id("awakened_plustic");
        SmitheryAPI.registerMaterial(AWAKENED_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(9.0f)
                        .attackDamage(35.0f)
                        .durabilityPerIngot(5000)
                        .meltingTemp(8000.0f)
                        .partColor(0xFFD8622E)
                        .binderMultiplier(1.8f)
                , 5)
                .armor(120.0f, 50.0f, 56.0f, 38.1176f, 0.0f, 56.0f)
                .addUniversalModifier(SoaSmitheryModifiers.APOCALYPSE)
                .addUniversalModifier(SoaSmitheryModifiers.BLINDBANDIT)
                .addUniversalModifier(SoaSmitheryModifiers.GLOBAL)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.BROWNMAGIC),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.RUDEAWAKENING),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.9f, 2.8f, 20.0f)
                .build());

        BLACKQUARTZ_PLUSTIC = id("blackquartz_plustic");
        SmitheryAPI.registerMaterial(BLACKQUARTZ_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(380)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF121110)
                        .binderMultiplier(0.8f)
                , 2)
                .armor(44.0f, 16.2f, 16.0f, 13.5529f, 0.0f, 16.0f)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addUniversalModifier(SoaSmitheryModifiers.DEVILSSTRENGTH)
                .build());

        BLOODWOOD_PLUSTIC = id("bloodwood_plustic");
        SmitheryAPI.registerMaterial(BLOODWOOD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(550)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF6E1B10)
                        .binderMultiplier(1.4f)
                , 4)
                .armor(25.0f, 22.5f, 19.04f, 29.6471f, 0.0f, 19.04f)
                .addUniversalModifier(SoaSmitheryModifiers.BLOODYMARY)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(1.6f, 1.4f, 7.0f)
                .build());

        CERTUSQUARTZ_PLUSTIC = id("certusquartz_plustic");
        SmitheryAPI.registerMaterial(CERTUSQUARTZ_PLUSTIC, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBADBFF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(11.0f, 20.25f, 7.84f, 16.9412f, 0.0f, 7.84f)
                .build());

        CERTUS_QUARTZ = id("certus_quartz");
        SmitheryAPI.registerMaterial(CERTUS_QUARTZ, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(200)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFBADBFF)
                        .binderMultiplier(0.75f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .bow(1.15f, 1.0f, 0.0f)
                .build());

        CHAOTIC_PLUSTIC = id("chaotic_plustic");
        SmitheryAPI.registerMaterial(CHAOTIC_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(12)
                        .miningSpeed(12.0f)
                        .attackDamage(48.0f)
                        .durabilityPerIngot(10000)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF2F3236)
                        .colorCycle(48, 0xFF53585E, 0xFF212326, 0xFF292C30, 0xFF212326) // [auto-color]
                        .binderMultiplier(2.3f)
                , 5)
                .armor(120.0f, 50.0f, 89.6f, 48.7059f, 0.0f, 89.6f)
                .addUniversalModifier(SoaSmitheryModifiers.DARKTRAVELER)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.HAILHYDRA),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.VINDICTIVE),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(2.3f, 4.0f, 24.0f)
                .build());

        DARKMATTER = id("darkmatter");
        SmitheryAPI.registerMaterial(DARKMATTER, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(14.0f)
                        .attackDamage(10.0f)
                        .durabilityPerIngot(4096)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF0F0029)
                        .binderMultiplier(1.5f)
                , 5)
                .armor(81.0f, 47.25f, 12.432f, 36.0f, 0.0f, 12.432f)
                .addUniversalModifier(SoaSmitheryModifiers.DARKTRAVELER)
                .addHeadModifier(SoaSmitheryModifiers.IGNOBLE)
                .bow(0.85f, 1.5f, 4.0f)
                .build());

        DARKWOOD_PLUSTIC = id("darkwood_plustic");
        SmitheryAPI.registerMaterial(DARKWOOD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(5.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(350)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF1E3972)
                        .binderMultiplier(1.3f)
                , 4)
                .armor(16.0f, 13.5f, 10.08f, 27.5294f, 0.0f, 10.08f)
                .addUniversalModifier(SoaSmitheryModifiers.DARKTRAVELER)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(1.2f, 1.3f, 3.0f)
                .build());

        DESH = id("desh");
        SmitheryAPI.registerMaterial(DESH, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.54f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1729)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF8F8A82)
                        .binderMultiplier(2.0f)
                , 4)
                .armor(81.0f, 36.0f, 28.784f, 34.2642f, 0.0f, 28.784f)
                .addUniversalModifier(SoaSmitheryModifiers.ALIEN)
                .addUniversalModifier(SoaSmitheryModifiers.BROWNMAGIC)
                .addUniversalModifier(SoaSmitheryModifiers.ENDERFERENCE)
                .bow(0.3679f, 2.72f, 12.0f)
                .build());

        DIAMATINE_ACTADD_PLUSTIC = id("diamatine_actadd_plustic");
        SmitheryAPI.registerMaterial(DIAMATINE_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(6.3f)
                        .durabilityPerIngot(1700)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFB8D8E8)
                        .binderMultiplier(1.2f)
                , 4)
                .armor(120.0f, 22.68f, 28.16f, 20.3294f, 0.0f, 28.16f)
                .addUniversalModifier(SoaSmitheryModifiers.MORGANLEFAY)
                .bow(0.7f, 2.0f, 11.0f)
                .build());

        ELEMENTIUM = id("elementium");
        SmitheryAPI.registerMaterial(ELEMENTIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.5f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFE084A5)
                        .binderMultiplier(0.8f)
                , 3)
                .armor(25.0f, 27.0f, 6.72f, 26.4706f, 0.0f, 6.72f)
                .addUniversalModifier(SoaSmitheryModifiers.MANA)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CASCADING)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_FAE_VOICE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_OPPORTUNIST)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ELEMENTAL),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DIVINE_GRACE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_FAE_VOICE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_AFFINITY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .bow(0.75f, 1.25f, 7.0f)
                .build());

        EMERADIC_ACTADD_PLUSTIC = id("emeradic_actadd_plustic");
        SmitheryAPI.registerMaterial(EMERADIC_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(7.7f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF3EC85A)
                        .binderMultiplier(1.1f)
                , 4)
                .armor(120.0f, 27.72f, 24.704f, 18.6353f, 0.0f, 24.704f)
                .addUniversalModifier(SoaSmitheryModifiers.VINDICTIVE)
                .bow(1.1f, 2.0f, 7.0f)
                .build());

        EMERALD_PLUSTIC = id("emerald_plustic");
        SmitheryAPI.registerMaterial(EMERALD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(6.5f)
                        .durabilityPerIngot(1222)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF17DD62)
                        .binderMultiplier(1.1f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ESTABLISHED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GOODFRIDAYAGREEMENT_ARMOR), armorPieces())
                .armor(100.0f, 18.0f, 45.0f, 1.4f, 1.75f, 30.0f)
                .bow(1.1f, 1.0f, 0.9f)
                .build());

        ENDERIUM_PLUSTIC = id("enderium_plustic");
        SmitheryAPI.registerMaterial(ENDERIUM_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.5f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF0B494A)
                        .binderMultiplier(1.05f)
                , 4)
                .armor(37.0f, 31.5f, 7.28f, 22.2353f, 0.0f, 7.28f)
                .addUniversalModifier(SoaSmitheryModifiers.ENDERFERENCE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDSPEED),
                        SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE, SmitheryPartTypes.ARROW_SHAFT)
                .addUniversalModifier(SoaSmitheryModifiers.GLOBAL)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.PORTLY),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.9f, 1.9f, 8.0f)
                .arrowShaft(1.0f, 12)
                .build());

        ENORI_ACTADD_PLUSTIC = id("enori_actadd_plustic");
        SmitheryAPI.registerMaterial(ENORI_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.2f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFD2D2D2)
                        .binderMultiplier(1.2f)
                , 3)
                .armor(70.0f, 18.72f, 24.064f, 20.3294f, 0.0f, 24.064f)
                .addUniversalModifier(SoaSmitheryModifiers.ANTICORROSION)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.STARFISHY),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.2f, 1.2f, 5.1f)
                .build());

        ERODIUM = id("erodium");
        SmitheryAPI.registerMaterial(ERODIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(9.0f)
                        .attackDamage(7.5f)
                        .durabilityPerIngot(1000)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF3E9FDF)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.DEPTHDIGGER)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .addUniversalModifier(SoaSmitheryModifiers.NATURESWRATH)
                .addUniversalModifier(SoaSmitheryModifiers.VINDICTIVE)
                .bow(0.85f, 1.2f, 1.5f)
                .build());

        FLAMESTRING_PLUSTIC = id("flamestring_plustic");
        SmitheryAPI.registerMaterial(FLAMESTRING_PLUSTIC, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB15E31)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.NAPHTHA)
                .bowstring(1.2f)
                .build());

        FLUIX = id("fluix");
        SmitheryAPI.registerMaterial(FLUIX, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(275)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF1D2547)
                        .binderMultiplier(1.0f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PENETRATIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHOCKING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PURIFYINGTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.VOLTAIC_ARMOR), armorPieces())
                .bow(0.9f, 1.25f, 4.0f)
                .build());

        FLUIXCRYSTAL_PLUSTIC = id("fluixcrystal_plustic");
        SmitheryAPI.registerMaterial(FLUIXCRYSTAL_PLUSTIC, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF1D2547)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(32.0f, 27.9f, 8.4f, 21.1765f, 0.0f, 8.4f)
                .build());

        FRISCION = id("friscion");
        SmitheryAPI.registerMaterial(FRISCION, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(1100)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF8FC8E8)
                        .binderMultiplier(0.8f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.TRASH)
                .build());

        FUSEWOOD_PLUSTIC = id("fusewood_plustic");
        SmitheryAPI.registerMaterial(FUSEWOOD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(6.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(430)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF0D352C)
                        .binderMultiplier(1.0f)
                , 4)
                .armor(20.0f, 18.0f, 5.6f, 21.1765f, 0.0f, 5.6f)
                .addUniversalModifier(SoaSmitheryModifiers.DPRK)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(0.7f, 2.0f, 7.0f)
                .build());

        GARFAX = id("garfax");
        SmitheryAPI.registerMaterial(GARFAX, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(7.3f)
                        .attackDamage(6.5f)
                        .durabilityPerIngot(1300)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF4A7A4A)
                        .binderMultiplier(0.9f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.BARRETT)
                .build());

        GHOSTWOOD_PLUSTIC = id("ghostwood_plustic");
        SmitheryAPI.registerMaterial(GHOSTWOOD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(4.9f)
                        .attackDamage(2.5f)
                        .durabilityPerIngot(300)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF282828)
                        .binderMultiplier(1.1f)
                , 4)
                .armor(14.0f, 11.25f, 11.2f, 23.2941f, 0.0f, 11.2f)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addUniversalModifier(SoaSmitheryModifiers.GHASTLY)
                .bow(1.6f, 1.1f, 2.0f)
                .build());

        INFINITY_AVARITIA_PLUSTIC = id("infinity_avaritia_plustic");
        SmitheryAPI.registerMaterial(INFINITY_AVARITIA_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(18.0f)
                        .attackDamage(40.0f)
                        .durabilityPerIngot(7000)
                        .meltingTemp(8000.0f)
                        .partColor(0xFFE5C4C4)
                        .colorCycle(233, 0xFFFFBEBF, 0xFFE9B8B8, 0xFFE2C6C7, 0xFFE2DFDD, 0xFFD8D0CB, 0xFFDEB2B3) // [auto-color]
                        .binderMultiplier(2.0f)
                , 5)
                .armor(120.0f, 50.0f, 156.8f, 42.3529f, 0.0f, 156.8f)
                .addUniversalModifier(SoaSmitheryModifiers.APOCALYPSE)
                .addUniversalModifier(SoaSmitheryModifiers.BROWNMAGIC)
                .addUniversalModifier(SoaSmitheryModifiers.GLOBAL)
                .bow(2.0f, 2.8f, 16.0f)
                .build());

        INVAR = id("invar");
        SmitheryAPI.registerMaterial(INVAR, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.5f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(320)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF5C7C7B)
                        .binderMultiplier(0.9f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.DEVILSSTRENGTH)
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.STIFF),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .bow(0.5f, 1.75f, 6.0f)
                .fletching(1.0f, 1.15f)
                .build());

        IONITE = id("ionite");
        SmitheryAPI.registerMaterial(IONITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.9f)
                        .attackDamage(12.0f)
                        .durabilityPerIngot(1900)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFB86FE8)
                        .binderMultiplier(0.9f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.ILLUMINATI)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_CHAIN_LIGHTNING)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHADTHUNDER),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.38f, 3.0f, 8.0f)
                .build());

        IRIDIUM = id("iridium");
        SmitheryAPI.registerMaterial(IRIDIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(6.5f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFC0BFD9)
                        .binderMultiplier(0.8f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addUniversalModifier(SoaSmitheryModifiers.MOMENTUM)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_OVERWHELM)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ALIEN),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.6f, 2.0f, 5.5f)
                .build());

        JADE = id("jade");
        SmitheryAPI.registerMaterial(JADE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF4FBC94)
                        .binderMultiplier(1.35f)
                , 4)
                .armor(75.0f, 22.5f, 5.6f, 28.5882f, 0.0f, 5.6f)
                .addUniversalModifier(SoaSmitheryModifiers.JADED)
                .bow(1.5f, 1.3f, 4.0f)
                .build());

        KELLINE = id("kelline");
        SmitheryAPI.registerMaterial(KELLINE, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(9.0f)
                        .attackDamage(9.5f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF4CBB17)
                        .binderMultiplier(1.3f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.HAILHYDRA)
                .bow(0.8f, 1.0f, 14.0f)
                .build());

        KYRONITE = id("kyronite");
        SmitheryAPI.registerMaterial(KYRONITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.3f)
                        .attackDamage(9.0f)
                        .durabilityPerIngot(1300)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFE8B8C8)
                        .binderMultiplier(0.9f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.NATURESBLESSING)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_BATTLE_FUROR)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.FRUITSALAD),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.6f, 1.6f, 5.0f)
                .build());

        LANDIUM = id("landium");
        SmitheryAPI.registerMaterial(LANDIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.7f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(1500)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF9A7B4F)
                        .binderMultiplier(1.5f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.PORTLY)
                .bow(1.2f, 1.6f, 7.0f)
                .build());

        LITHERITE = id("litherite");
        SmitheryAPI.registerMaterial(LITHERITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.7f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(700)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFCADF3E)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addUniversalModifier(SoaSmitheryModifiers.JAGGED)
                .addUniversalModifier(SoaSmitheryModifiers.PETRAMOR)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.STONEBOUND),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.75f, 1.25f, 2.0f)
                .build());

        LIVINGWOOD_PLUSTIC = id("livingwood_plustic");
        SmitheryAPI.registerMaterial(LIVINGWOOD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.1f)
                        .attackDamage(2.8f)
                        .durabilityPerIngot(50)
                        .meltingTemp(600.0f)
                        .partColor(0xFF421909)
                        .binderMultiplier(1.15f)
                , 1)
                .armor(2.0f, 12.6f, 2.24f, 24.3529f, 0.0f, 2.24f)
                .addUniversalModifier(SoaSmitheryModifiers.BOTANICAL)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .bow(1.1f, 1.1f, 1.8f)
                .arrowShaft(1.0f, 6)
                .build());

        LUMIUM_PLUSTIC = id("lumium_plustic");
        SmitheryAPI.registerMaterial(LUMIUM_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(6.5f)
                        .durabilityPerIngot(830)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFCE7F2D)
                        .binderMultiplier(1.1f)
                , 4)
                .armor(39.0f, 29.25f, 6.72f, 23.2941f, 0.0f, 6.72f)
                .addUniversalModifier(SoaSmitheryModifiers.GLOWING)
                .addUniversalModifier(SoaSmitheryModifiers.ILLUMINATI)
                .bow(1.5f, 1.8f, 4.0f)
                .build());

        MALACHITE_GEM = id("malachite_gem");
        SmitheryAPI.registerMaterial(MALACHITE_GEM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(3.0f)
                        .attackDamage(6.1f)
                        .durabilityPerIngot(640)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF2F9E5B)
                        .binderMultiplier(1.3f)
                , 3)
                .armor(260.0f, 20.0f, 100.0f, 2.0f, 2.25f, 80.0f)
                .addUniversalModifier(SoaSmitheryModifiers.NATURESWRATH)
                .bow(1.4f, 1.4f, 4.0f)
                .build());

        MANASTEEL = id("manasteel");
        SmitheryAPI.registerMaterial(MANASTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(540)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFCAFFFD)
                        .binderMultiplier(0.9f)
                , 3)
                .armor(25.0f, 27.0f, 6.72f, 26.4706f, 0.0f, 6.72f)
                .addUniversalModifier(SoaSmitheryModifiers.MANA)
                .addUniversalModifier(SoaSmitheryModifiers.MOMENTUM)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_AFFINITY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .bow(0.5f, 1.5f, 7.0f)
                .build());

        MICA = id("mica");
        SmitheryAPI.registerMaterial(MICA, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(5.5f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(680)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFC9C2A8)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.SLASHING)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE, java.util.Map.of("bonus_slots", 1))
                .bow(1.75f, 0.75f, 0.0f)
                .build());

        MIRION = id("mirion");
        SmitheryAPI.registerMaterial(MIRION, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(20.2f)
                        .attackDamage(10.4f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFD1E485)
                        .binderMultiplier(3.0f)
                , 5)
                .armor(250.0f, 30.0f, 40.0f, 3.0f, 3.0f, 32.0f)
                .addUniversalModifier(SoaSmitheryModifiers.MANA)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.MIRABILE),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.8f, 1.5f, 8.7f)
                .build());

        MORGANINE = id("morganine");
        SmitheryAPI.registerMaterial(MORGANINE, binderSlots(MaterialStats.builder()
                        .harvestLevel(6)
                        .miningSpeed(8.6f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(3000.0f)
                        .partColor(0xFFE8A0B8)
                        .binderMultiplier(1.2f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.MORGANLEFAY)
                .bow(1.2f, 1.2f, 6.0f)
                .build());

        NICKEL = id("nickel");
        SmitheryAPI.registerMaterial(NICKEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFC1C1B4)
                        .binderMultiplier(0.85f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR1), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT_ARMOR), armorPieces())
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .addUniversalModifier(SoaSmitheryModifiers.MAGNETIC)
                .bow(0.65f, 1.5f, 2.0f)
                .fletching(0.95f, 1.05f)
                .build());

        OSGLOGLAS = id("osgloglas");
        SmitheryAPI.registerMaterial(OSGLOGLAS, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(10.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(2000)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF15D41B)
                        .binderMultiplier(1.25f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GLOBAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GLOBAL), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING_ARMOR), armorPieces())
                .armor(500.0f, 36.0f, 11.2f, 7.2f, 3.5f, 11.2f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WARPING),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.6f, 1.8f, 8.2f)
                .build());

        OSMIRIDIUM = id("osmiridium");
        SmitheryAPI.registerMaterial(OSMIRIDIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(1500)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF6F75CD)
                        .binderMultiplier(1.0f)
                , 4)
                .armor(400.0f, 28.0f, 8.96f, 5.2f, 3.5f, 8.96f)
                .addUniversalModifier(SoaSmitheryModifiers.DEVILSSTRENGTH)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTICORROSION),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.38f, 2.05f, 10.0f)
                .build());

        OSMIUM = id("osmium");
        SmitheryAPI.registerMaterial(OSMIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(500)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF767686)
                        .binderMultiplier(1.0f)
                , 2)
                .armor(23.0f, 26.1f, 4.48f, 25.4118f, 0.0f, 4.48f)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addUniversalModifier(SoaSmitheryModifiers.ESTABLISHED)
                .addUniversalModifier(SoaSmitheryModifiers.STIFF)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .bow(0.65f, 1.3f, 5.7f)
                .build());

        PALIS_ACTADD_PLUSTIC = id("palis_actadd_plustic");
        SmitheryAPI.registerMaterial(PALIS_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.5f)
                        .attackDamage(5.8f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF0D0E7F)
                        .binderMultiplier(1.3f)
                , 4)
                .armor(94.0f, 20.88f, 32.0f, 22.0235f, 0.0f, 32.0f)
                .addUniversalModifier(SoaSmitheryModifiers.SPADES)
                .build());

        PERIDOT = id("peridot");
        SmitheryAPI.registerMaterial(PERIDOT, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(4.0f)
                        .attackDamage(6.1f)
                        .durabilityPerIngot(640)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF314419)
                        .binderMultiplier(1.3f)
                , 3)
                .armor(230.0f, 20.0f, 100.0f, 2.0f, 2.0f, 80.0f)
                .addUniversalModifier(SoaSmitheryModifiers.NATURESBLESSING)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TOM_AND_JERRY_ARMOR), armorPieces())
                .bow(1.4f, 1.4f, 4.0f)
                .build());

        PINK_SLIME = id("pink_slime");
        SmitheryAPI.registerMaterial(PINK_SLIME, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.0f)
                        .attackDamage(2.0f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9F1761)
                        .binderMultiplier(1.25f)
                , 1)
                .armor(84.0f, 8.1f, 27.216f, 50.0f, 0.0f, 27.216f)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BOUNCY_ARMOR), armorPieces())
                .addUniversalModifier(SoaSmitheryModifiers.SLIMEY)
                .bow(1.3f, 0.85f, 0.0f)
                .build());

        PLADIUM = id("pladium");
        SmitheryAPI.registerMaterial(PLADIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(11.6f)
                        .attackDamage(10.5f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF6FD8A8)
                        .binderMultiplier(0.9f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.STOPBEINGSELFISH)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_RELENTLESS)
                .bow(0.49f, 2.2f, 6.5f)
                .build());

        PLATINUM_PLUSTIC = id("platinum_plustic");
        SmitheryAPI.registerMaterial(PLATINUM_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(720)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF4771C0)
                        .binderMultiplier(1.05f)
                , 4)
                .armor(33.0f, 27.0f, 6.72f, 22.2353f, 0.0f, 6.72f)
                .addUniversalModifier(SoaSmitheryModifiers.ANTICORROSION)
                .addUniversalModifier(SoaSmitheryModifiers.HEAVY_METAL)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.GLOBAL),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.85f, 1.8f, 8.0f)
                .build());

        PSIGEM = id("psigem");
        SmitheryAPI.registerMaterial(PSIGEM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF7AB8D8)
                        .binderMultiplier(0.9f)
                , 3)
                .armor(58.0f, 27.0f, 7.68f, 33.0353f, 0.0f, 7.68f)
                .addUniversalModifier(SoaSmitheryModifiers.PORTLY)
                .addUniversalModifier(SoaSmitheryModifiers.PSICOLOGICAL)
                .bow(1.0f, 1.6f, 4.0f)
                .build());

        PSIMETAL = id("psimetal");
        SmitheryAPI.registerMaterial(PSIMETAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.5f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(250)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFE0DCD0)
                        .binderMultiplier(0.85f)
                , 2)
                .armor(58.0f, 27.0f, 7.68f, 33.0353f, 0.0f, 7.68f)
                .addUniversalModifier(SoaSmitheryModifiers.GLOBAL)
                .addUniversalModifier(SoaSmitheryModifiers.PSICOLOGICAL)
                .bow(1.0f, 1.6f, 4.0f)
                .build());

        RACHELINE = id("racheline");
        SmitheryAPI.registerMaterial(RACHELINE, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(8.3f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(2000)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF9A6FD0)
                        .binderMultiplier(1.0f)
                , 5)
                .addUniversalModifier(SoaSmitheryModifiers.BLINDBANDIT)
                .bow(1.3f, 0.7f, 15.0f)
                .build());

        REDMATTER = id("redmatter");
        SmitheryAPI.registerMaterial(REDMATTER, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(18.0f)
                        .attackDamage(12.0f)
                        .durabilityPerIngot(8192)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFFEEC57)
                        .binderMultiplier(2.0f)
                , 5)
                .armor(94.0f, 50.0f, 11.76f, 42.3529f, 0.0f, 11.76f)
                .addUniversalModifier(SoaSmitheryModifiers.DPRK)
                .bow(0.75f, 2.0f, 10.0f)
                .build());

        REFINEDGLOWSTONE = id("refinedglowstone");
        SmitheryAPI.registerMaterial(REFINEDGLOWSTONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(8.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFFFEA86)
                        .binderMultiplier(0.9f)
                , 2)
                .armor(21.0f, 45.0f, 11.2f, 19.0588f, 0.0f, 11.2f)
                .addUniversalModifier(SoaSmitheryModifiers.ILLUMINATI)
                .bow(1.0f, 1.25f, 4.0f)
                .build());

        REFINEDOBSIDIAN = id("refinedobsidian");
        SmitheryAPI.registerMaterial(REFINEDOBSIDIAN, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(2500)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF7861A2)
                        .binderMultiplier(1.3f)
                , 4)
                .armor(117.0f, 49.5f, 17.92f, 31.7647f, 0.0f, 17.92f)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .bow(0.65f, 1.35f, 7.0f)
                .build());

        RESTONIA_ACTADD_PLUSTIC = id("restonia_actadd_plustic");
        SmitheryAPI.registerMaterial(RESTONIA_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.8f)
                        .attackDamage(6.1f)
                        .durabilityPerIngot(640)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFF10000)
                        .binderMultiplier(1.1f)
                , 3)
                .armor(75.0f, 21.96f, 28.8f, 18.6353f, 0.0f, 28.8f)
                .addUniversalModifier(SoaSmitheryModifiers.HEARTS)
                .bow(1.4f, 1.7f, 5.7f)
                .build());

        RUBY = id("ruby");
        SmitheryAPI.registerMaterial(RUBY, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(4.6f)
                        .attackDamage(6.4f)
                        .durabilityPerIngot(660)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF6F0735)
                        .binderMultiplier(1.2f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLOODLUSTTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RAGING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BLOODLUSTTRAIT), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LUCKYTRAIT_ARMOR), armorPieces())
                .armor(220.0f, 18.0f, 100.0f, 2.0f, 2.0f, 80.0f)
                .bow(1.5f, 1.4f, 4.0f)
                .build());

        SAPPHIRE = id("sapphire");
        SmitheryAPI.registerMaterial(SAPPHIRE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(5.0f)
                        .attackDamage(6.4f)
                        .durabilityPerIngot(700)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF3993C2)
                        .binderMultiplier(1.0f)
                , 3)
                .armor(240.0f, 19.0f, 100.0f, 2.0f, 2.0f, 80.0f)
                .addUniversalModifier(SoaSmitheryModifiers.AQUADYNAMIC)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AQUASPEED_ARMOR), armorPieces())
                .bow(1.0f, 1.5f, 4.0f)
                .build());

        SIGNALUM_PLUSTIC = id("signalum_plustic");
        SmitheryAPI.registerMaterial(SIGNALUM_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.5f)
                        .attackDamage(5.2f)
                        .durabilityPerIngot(690)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFCF4606)
                        .binderMultiplier(1.2f)
                , 3)
                .armor(32.0f, 23.4f, 6.16f, 25.4118f, 0.0f, 6.16f)
                .addUniversalModifier(SoaSmitheryModifiers.BLOODYMARY)
                .bow(1.2f, 1.6f, 4.4f)
                .build());

        SKY_STONE = id("sky_stone");
        SmitheryAPI.registerMaterial(SKY_STONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(340)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF2C2F2E)
                        .binderMultiplier(1.0f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE_ARMOR), allToolTypes())
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE), SmitheryPartTypes.FLETCHING)
                .bow(0.5f, 1.25f, 2.0f)
                .build());

        STARMETAL = id("starmetal");
        SmitheryAPI.registerMaterial(STARMETAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(14.6f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF000617)
                        .binderMultiplier(1.75f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BANE_OF_NIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRIDEFUL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.UNNATURAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRIDEFUL_ARMOR), armorPieces())
                .armor(100.0f, 18.4f, 50.0f, 1.2f, 1.0f, 40.0f)
                .bow(0.5f, 1.5f, 7.5f)
                .build());

        TANZANITE = id("tanzanite");
        SmitheryAPI.registerMaterial(TANZANITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(3.0f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(650)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF4F196E)
                        .binderMultiplier(0.7f)
                , 3)
                .armor(280.0f, 22.0f, 100.0f, 2.0f, 2.5f, 80.0f)
                .addUniversalModifier(SoaSmitheryModifiers.FREEZING)
                .build());

        TERRASTEEL = id("terrasteel");
        SmitheryAPI.registerMaterial(TERRASTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.0f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFDCFFCC)
                        .binderMultiplier(1.2f)
                , 4)
                .armor(73.0f, 29.25f, 1.12f, 29.6471f, 0.0f, 1.12f)
                .addUniversalModifier(SoaSmitheryModifiers.MANA)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_GAIA_WRATH)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MORTAL_WOUNDS)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_STAGGERING),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TERRAFIRMA1)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA2),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_AFFINITY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SECOND_WIND_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILL_STRENGTH_ARMOR), armorPieces())
                .bow(0.4f, 1.75f, 9.0f)
                .build());

        THAUMIUM = id("thaumium");
        SmitheryAPI.registerMaterial(THAUMIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(500)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF50437A)
                        .binderMultiplier(1.0f)
                , 3)
                .armor(18.0f, 19.125f, 12.432f, 21.1765f, 0.0f, 12.432f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_MODIFIABLE, java.util.Map.of("bonus_slots", 1))
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_OPPORTUNIST)
                .addUniversalModifier(SoaSmitheryModifiers.THAUMIC)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .bow(0.7f, 1.3f, 7.0f)
                .build());

        THORIUM = id("thorium");
        SmitheryAPI.registerMaterial(THORIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.5f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(500)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF2D2D2D)
                        .binderMultiplier(0.7f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.FLAMMABLE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.APOCALYPSE),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        TOPAZ = id("topaz");
        SmitheryAPI.registerMaterial(TOPAZ, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(690)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFF7C5A8)
                        .binderMultiplier(0.8f)
                , 3)
                .armor(200.0f, 14.0f, 100.0f, 2.0f, 1.75f, 80.0f)
                .addUniversalModifier(SoaSmitheryModifiers.NATURESPOWER)
                .bow(0.4f, 1.4f, 7.0f)
                .build());

        TUNGSTEN = id("tungsten");
        SmitheryAPI.registerMaterial(TUNGSTEN, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(9.0f)
                        .attackDamage(4.75f)
                        .durabilityPerIngot(700)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF4D4D4D)
                        .binderMultiplier(1.4f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.HEAVY_METAL)
                .build());

        VOID_ACTADD_PLUSTIC = id("void_actadd_plustic");
        SmitheryAPI.registerMaterial(VOID_ACTADD_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(4.4f)
                        .durabilityPerIngot(480)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF520CA2)
                        .binderMultiplier(1.0f)
                , 3)
                .armor(56.0f, 15.84f, 44.8f, 16.9412f, 0.0f, 44.8f)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.UNNAMED),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.0f, 1.3f, 3.5f)
                .build());

        WYVERN_PLUSTIC = id("wyvern_plustic");
        SmitheryAPI.registerMaterial(WYVERN_PLUSTIC, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(8.0f)
                        .attackDamage(15.0f)
                        .durabilityPerIngot(2000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF5B4273)
                        .binderMultiplier(1.6f)
                , 5)
                .armor(94.0f, 50.0f, 26.88f, 33.8824f, 0.0f, 26.88f)
                .addUniversalModifier(SoaSmitheryModifiers.PORTLY)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.BLINDBANDIT),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.BROWNMAGIC),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.6f, 2.0f, 11.0f)
                .build());

    }

    private static void registerSilentsGems() {
        SILENTGEMSAGATE = ResourceLocation.fromNamespaceAndPath("silentgems", "agate");
        SmitheryAPI.registerMaterial(SILENTGEMSAGATE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC97F6B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 16.2f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSAGATE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "agate_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAGATE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC97F6B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 32.4f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSALEXANDRITE = ResourceLocation.fromNamespaceAndPath("silentgems", "alexandrite");
        SmitheryAPI.registerMaterial(SILENTGEMSALEXANDRITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FBF8F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 16.2f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSALEXANDRITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "alexandrite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSALEXANDRITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FBF8F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSAMBER = ResourceLocation.fromNamespaceAndPath("silentgems", "amber");
        SmitheryAPI.registerMaterial(SILENTGEMSAMBER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF09D34)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(18.0f, 10.8f, 6.144f, 22.2353f, 0.0f, 6.144f)
                .build());

        SILENTGEMSAMBER_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "amber_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAMBER_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF09D34)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 27.0f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSAMETHYST = ResourceLocation.fromNamespaceAndPath("silentgems", "amethyst");
        SmitheryAPI.registerMaterial(SILENTGEMSAMETHYST, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4B207F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 16.2f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSAMETHYST_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "amethyst_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAMETHYST_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4B207F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSAMETRINE = ResourceLocation.fromNamespaceAndPath("silentgems", "ametrine");
        SmitheryAPI.registerMaterial(SILENTGEMSAMETRINE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB585D0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSAMETRINE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "ametrine_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAMETRINE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB585D0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSAMMOLITE = ResourceLocation.fromNamespaceAndPath("silentgems", "ammolite");
        SmitheryAPI.registerMaterial(SILENTGEMSAMMOLITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4FA88A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 16.2f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSAMMOLITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "ammolite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAMMOLITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4FA88A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 32.4f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSAPATITE = ResourceLocation.fromNamespaceAndPath("silentgems", "apatite");
        SmitheryAPI.registerMaterial(SILENTGEMSAPATITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3495D4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 10.8f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSAPATITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "apatite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAPATITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3495D4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 27.0f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSAQUAMARINE = ResourceLocation.fromNamespaceAndPath("silentgems", "aquamarine");
        SmitheryAPI.registerMaterial(SILENTGEMSAQUAMARINE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF006FC1)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 16.2f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSAQUAMARINE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "aquamarine_super");
        SmitheryAPI.registerMaterial(SILENTGEMSAQUAMARINE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF006FC1)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSBERYL = ResourceLocation.fromNamespaceAndPath("silentgems", "beryl");
        SmitheryAPI.registerMaterial(SILENTGEMSBERYL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7FD3A8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSBERYL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "beryl_super");
        SmitheryAPI.registerMaterial(SILENTGEMSBERYL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7FD3A8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSBLACK_DIAMOND = ResourceLocation.fromNamespaceAndPath("silentgems", "black_diamond");
        SmitheryAPI.registerMaterial(SILENTGEMSBLACK_DIAMOND, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3B3844)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 16.2f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSBLACK_DIAMOND_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "black_diamond_super");
        SmitheryAPI.registerMaterial(SILENTGEMSBLACK_DIAMOND_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3B3844)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 131.072f, 22.2353f, 0.0f, 131.072f)
                .build());

        SILENTGEMSBLUE_TOPAZ = ResourceLocation.fromNamespaceAndPath("silentgems", "blue_topaz");
        SmitheryAPI.registerMaterial(SILENTGEMSBLUE_TOPAZ, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3E8FD8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 16.2f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSBLUE_TOPAZ_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "blue_topaz_super");
        SmitheryAPI.registerMaterial(SILENTGEMSBLUE_TOPAZ_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3E8FD8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSCARNELIAN = ResourceLocation.fromNamespaceAndPath("silentgems", "carnelian");
        SmitheryAPI.registerMaterial(SILENTGEMSCARNELIAN, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC24E33)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 10.8f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSCARNELIAN_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "carnelian_super");
        SmitheryAPI.registerMaterial(SILENTGEMSCARNELIAN_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC24E33)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 27.0f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSCATS_EYE = ResourceLocation.fromNamespaceAndPath("silentgems", "cats_eye");
        SmitheryAPI.registerMaterial(SILENTGEMSCATS_EYE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC7B24A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 16.2f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSCATS_EYE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "cats_eye_super");
        SmitheryAPI.registerMaterial(SILENTGEMSCATS_EYE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC7B24A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSCHRYSOPRASE = ResourceLocation.fromNamespaceAndPath("silentgems", "chrysoprase");
        SmitheryAPI.registerMaterial(SILENTGEMSCHRYSOPRASE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF57C785)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 16.2f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSCHRYSOPRASE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "chrysoprase_super");
        SmitheryAPI.registerMaterial(SILENTGEMSCHRYSOPRASE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF57C785)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSCITRINE = ResourceLocation.fromNamespaceAndPath("silentgems", "citrine");
        SmitheryAPI.registerMaterial(SILENTGEMSCITRINE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE0A62B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSCITRINE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "citrine_super");
        SmitheryAPI.registerMaterial(SILENTGEMSCITRINE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE0A62B)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSCORAL = ResourceLocation.fromNamespaceAndPath("silentgems", "coral");
        SmitheryAPI.registerMaterial(SILENTGEMSCORAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8735A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 16.2f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSCORAL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "coral_super");
        SmitheryAPI.registerMaterial(SILENTGEMSCORAL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8735A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 32.4f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSFLUORITE = ResourceLocation.fromNamespaceAndPath("silentgems", "fluorite");
        SmitheryAPI.registerMaterial(SILENTGEMSFLUORITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC1D5DE)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 10.8f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSFLUORITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "fluorite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSFLUORITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC1D5DE)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 27.0f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSGARNET = ResourceLocation.fromNamespaceAndPath("silentgems", "garnet");
        SmitheryAPI.registerMaterial(SILENTGEMSGARNET, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9A1F2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSGARNET_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "garnet_super");
        SmitheryAPI.registerMaterial(SILENTGEMSGARNET_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9A1F2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSGOLDEN_BERYL = ResourceLocation.fromNamespaceAndPath("silentgems", "golden_beryl");
        SmitheryAPI.registerMaterial(SILENTGEMSGOLDEN_BERYL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8C84A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 10.8f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSGOLDEN_BERYL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "golden_beryl_super");
        SmitheryAPI.registerMaterial(SILENTGEMSGOLDEN_BERYL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8C84A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 27.0f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSHELIODOR = ResourceLocation.fromNamespaceAndPath("silentgems", "heliodor");
        SmitheryAPI.registerMaterial(SILENTGEMSHELIODOR, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE3D060)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 21.6f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSHELIODOR_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "heliodor_super");
        SmitheryAPI.registerMaterial(SILENTGEMSHELIODOR_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE3D060)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSINDICOLITE = ResourceLocation.fromNamespaceAndPath("silentgems", "indicolite");
        SmitheryAPI.registerMaterial(SILENTGEMSINDICOLITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E7FA8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 10.8f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSINDICOLITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "indicolite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSINDICOLITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E7FA8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 27.0f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSIOLITE = ResourceLocation.fromNamespaceAndPath("silentgems", "iolite");
        SmitheryAPI.registerMaterial(SILENTGEMSIOLITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF5A5FB8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 10.8f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSIOLITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "iolite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSIOLITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF5A5FB8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 27.0f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSJADE = ResourceLocation.fromNamespaceAndPath("silentgems", "jade");
        SmitheryAPI.registerMaterial(SILENTGEMSJADE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4FBC94)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 21.6f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSJADE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "jade_super");
        SmitheryAPI.registerMaterial(SILENTGEMSJADE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4FBC94)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSJASPER = ResourceLocation.fromNamespaceAndPath("silentgems", "jasper");
        SmitheryAPI.registerMaterial(SILENTGEMSJASPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF724134)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 16.2f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSJASPER_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "jasper_super");
        SmitheryAPI.registerMaterial(SILENTGEMSJASPER_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF724134)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSKUNZITE = ResourceLocation.fromNamespaceAndPath("silentgems", "kunzite");
        SmitheryAPI.registerMaterial(SILENTGEMSKUNZITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE3A0C8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 27.0f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSKUNZITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "kunzite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSKUNZITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE3A0C8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 43.2f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSKYANITE = ResourceLocation.fromNamespaceAndPath("silentgems", "kyanite");
        SmitheryAPI.registerMaterial(SILENTGEMSKYANITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3F6FC4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 21.6f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSKYANITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "kyanite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSKYANITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3F6FC4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSLEPIDOLITE = ResourceLocation.fromNamespaceAndPath("silentgems", "lepidolite");
        SmitheryAPI.registerMaterial(SILENTGEMSLEPIDOLITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB08BC0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(18.0f, 16.2f, 6.144f, 22.2353f, 0.0f, 6.144f)
                .build());

        SILENTGEMSLEPIDOLITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "lepidolite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSLEPIDOLITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB08BC0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 32.4f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSMALACHITE = ResourceLocation.fromNamespaceAndPath("silentgems", "malachite");
        SmitheryAPI.registerMaterial(SILENTGEMSMALACHITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF16A17A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 21.6f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSMALACHITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "malachite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSMALACHITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF16A17A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 37.8f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSMOLDAVITE = ResourceLocation.fromNamespaceAndPath("silentgems", "moldavite");
        SmitheryAPI.registerMaterial(SILENTGEMSMOLDAVITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F8F2F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 27.0f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSMOLDAVITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "moldavite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSMOLDAVITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F8F2F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 43.2f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSMOONSTONE = ResourceLocation.fromNamespaceAndPath("silentgems", "moonstone");
        SmitheryAPI.registerMaterial(SILENTGEMSMOONSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD2ECF6)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 16.2f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSMOONSTONE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "moonstone_super");
        SmitheryAPI.registerMaterial(SILENTGEMSMOONSTONE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD2ECF6)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSMORGANITE = ResourceLocation.fromNamespaceAndPath("silentgems", "morganite");
        SmitheryAPI.registerMaterial(SILENTGEMSMORGANITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFEFA8A0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 21.6f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSMORGANITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "morganite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSMORGANITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFEFA8A0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSONYX = ResourceLocation.fromNamespaceAndPath("silentgems", "onyx");
        SmitheryAPI.registerMaterial(SILENTGEMSONYX, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF373A3A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(18.0f, 37.8f, 6.144f, 22.2353f, 0.0f, 6.144f)
                .build());

        SILENTGEMSONYX_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "onyx_super");
        SmitheryAPI.registerMaterial(SILENTGEMSONYX_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF373A3A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 50.0f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSOPAL = ResourceLocation.fromNamespaceAndPath("silentgems", "opal");
        SmitheryAPI.registerMaterial(SILENTGEMSOPAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8E8E0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 16.2f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSOPAL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "opal_super");
        SmitheryAPI.registerMaterial(SILENTGEMSOPAL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8E8E0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 32.4f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSPEARL = ResourceLocation.fromNamespaceAndPath("silentgems", "pearl");
        SmitheryAPI.registerMaterial(SILENTGEMSPEARL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6AA7AD)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(24.0f, 16.2f, 8.192f, 22.2353f, 0.0f, 8.192f)
                .build());

        SILENTGEMSPEARL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "pearl_super");
        SmitheryAPI.registerMaterial(SILENTGEMSPEARL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6AA7AD)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(96.0f, 32.4f, 32.768f, 22.2353f, 0.0f, 32.768f)
                .build());

        SILENTGEMSPERIDOT = ResourceLocation.fromNamespaceAndPath("silentgems", "peridot");
        SmitheryAPI.registerMaterial(SILENTGEMSPERIDOT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF314419)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 21.6f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSPERIDOT_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "peridot_super");
        SmitheryAPI.registerMaterial(SILENTGEMSPERIDOT_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF314419)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSPYROPE = ResourceLocation.fromNamespaceAndPath("silentgems", "pyrope");
        SmitheryAPI.registerMaterial(SILENTGEMSPYROPE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBF2036)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 32.4f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSPYROPE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "pyrope_super");
        SmitheryAPI.registerMaterial(SILENTGEMSPYROPE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBF2036)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 48.6f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSROSE_QUARTZ = ResourceLocation.fromNamespaceAndPath("silentgems", "rose_quartz");
        SmitheryAPI.registerMaterial(SILENTGEMSROSE_QUARTZ, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9D3964)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSROSE_QUARTZ_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "rose_quartz_super");
        SmitheryAPI.registerMaterial(SILENTGEMSROSE_QUARTZ_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9D3964)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSRUBY = ResourceLocation.fromNamespaceAndPath("silentgems", "ruby");
        SmitheryAPI.registerMaterial(SILENTGEMSRUBY, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F0735)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 32.4f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSRUBY_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "ruby_super");
        SmitheryAPI.registerMaterial(SILENTGEMSRUBY_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F0735)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 48.6f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSSAPPHIRE = ResourceLocation.fromNamespaceAndPath("silentgems", "sapphire");
        SmitheryAPI.registerMaterial(SILENTGEMSSAPPHIRE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3993C2)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 21.6f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSSAPPHIRE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "sapphire_super");
        SmitheryAPI.registerMaterial(SILENTGEMSSAPPHIRE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3993C2)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSSODALITE = ResourceLocation.fromNamespaceAndPath("silentgems", "sodalite");
        SmitheryAPI.registerMaterial(SILENTGEMSSODALITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2F4C9E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 16.2f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSSODALITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "sodalite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSSODALITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2F4C9E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSSPINEL = ResourceLocation.fromNamespaceAndPath("silentgems", "spinel");
        SmitheryAPI.registerMaterial(SILENTGEMSSPINEL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD03A50)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 27.0f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSSPINEL_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "spinel_super");
        SmitheryAPI.registerMaterial(SILENTGEMSSPINEL_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD03A50)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 43.2f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSSUNSTONE = ResourceLocation.fromNamespaceAndPath("silentgems", "sunstone");
        SmitheryAPI.registerMaterial(SILENTGEMSSUNSTONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD14E2C)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 27.0f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSSUNSTONE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "sunstone_super");
        SmitheryAPI.registerMaterial(SILENTGEMSSUNSTONE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD14E2C)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 43.2f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSTANZANITE = ResourceLocation.fromNamespaceAndPath("silentgems", "tanzanite");
        SmitheryAPI.registerMaterial(SILENTGEMSTANZANITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4F196E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 16.2f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSTANZANITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "tanzanite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSTANZANITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4F196E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSTEKTITE = ResourceLocation.fromNamespaceAndPath("silentgems", "tektite");
        SmitheryAPI.registerMaterial(SILENTGEMSTEKTITE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A4238)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 21.6f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSTEKTITE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "tektite_super");
        SmitheryAPI.registerMaterial(SILENTGEMSTEKTITE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A4238)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSTOPAZ = ResourceLocation.fromNamespaceAndPath("silentgems", "topaz");
        SmitheryAPI.registerMaterial(SILENTGEMSTOPAZ, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF7C5A8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSTOPAZ_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "topaz_super");
        SmitheryAPI.registerMaterial(SILENTGEMSTOPAZ_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF7C5A8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

        SILENTGEMSTURQUOISE = ResourceLocation.fromNamespaceAndPath("silentgems", "turquoise");
        SmitheryAPI.registerMaterial(SILENTGEMSTURQUOISE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF40C8C0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 16.2f, 12.288f, 22.2353f, 0.0f, 12.288f)
                .build());

        SILENTGEMSTURQUOISE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "turquoise_super");
        SmitheryAPI.registerMaterial(SILENTGEMSTURQUOISE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF40C8C0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 32.4f, 49.152f, 22.2353f, 0.0f, 49.152f)
                .build());

        SILENTGEMSVIOLET_SAPPHIRE = ResourceLocation.fromNamespaceAndPath("silentgems", "violet_sapphire");
        SmitheryAPI.registerMaterial(SILENTGEMSVIOLET_SAPPHIRE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7A4FBF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(72.0f, 21.6f, 24.576f, 22.2353f, 0.0f, 24.576f)
                .build());

        SILENTGEMSVIOLET_SAPPHIRE_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "violet_sapphire_super");
        SmitheryAPI.registerMaterial(SILENTGEMSVIOLET_SAPPHIRE_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7A4FBF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 98.304f, 22.2353f, 0.0f, 98.304f)
                .build());

        SILENTGEMSZIRCON = ResourceLocation.fromNamespaceAndPath("silentgems", "zircon");
        SmitheryAPI.registerMaterial(SILENTGEMSZIRCON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FCADF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(48.0f, 21.6f, 16.384f, 22.2353f, 0.0f, 16.384f)
                .build());

        SILENTGEMSZIRCON_SUPER = ResourceLocation.fromNamespaceAndPath("silentgems", "zircon_super");
        SmitheryAPI.registerMaterial(SILENTGEMSZIRCON_SUPER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FCADF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 37.8f, 65.536f, 22.2353f, 0.0f, 65.536f)
                .build());

    }

    private static void registerTwilightForest() {
        ALPHA_FUR = id("alpha_fur");
        SmitheryAPI.registerMaterial(ALPHA_FUR, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(2.0f)
                        .attackDamage(1.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(600.0f)
                        .partColor(0xFF27436C)
                        .binderMultiplier(0.1f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SQUEAKY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KUNGFU_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MUNDANE_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WARMTRAIT_ARMOR), armorPieces())
                .armor(100.0f, 19.5f, 50.0f, 1.8f, 2.5f, 20.0f)
                .bow(1.0f, 0.3f, 1.0f)
                .build());

        FIERYMETAL = id("fierymetal");
        SmitheryAPI.registerMaterial(FIERYMETAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.2f)
                        .attackDamage(6.6f)
                        .durabilityPerIngot(720)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF301E1E)
                        .binderMultiplier(0.7f)
                , 3)
                .armor(120.0f, 19.0f, 30.0f, 2.0f, 2.25f, 10.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TWILIT)
                .addUniversalModifier(SoaSmitheryModifiers.FLAMMABLE)
                .addHeadModifier(SoaSmitheryModifiers.AUTOSMELT)
                .addHeadModifier(SoaSmitheryModifiers.SUPERHEAT)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHOT_ARMOR), armorPieces())
                .bow(1.0f, 0.9f, 4.0f)
                .arrowShaft(0.8f, 0)
                .build());

        FIERY = id("fiery");
        SmitheryAPI.registerMaterial(FIERY, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(720)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF512816)
                        .colorCycle(36, 0xFF3C2323, 0xFF662D09) // [auto-color]
                        .binderMultiplier(0.9f)
                , 3)
                .build());

        KNIGHTMETAL = id("knightmetal");
        SmitheryAPI.registerMaterial(KNIGHTMETAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(6.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF5D6256)
                        .binderMultiplier(0.9f)
                , 3)
                .armor(100.0f, 18.2f, 20.0f, 1.8f, 1.75f, 8.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TWILIT)
                .addUniversalModifier(SoaSmitheryModifiers.STALWART)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DRAMATIC_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .build());

        NAGASCALE = id("nagascale");
        SmitheryAPI.registerMaterial(NAGASCALE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.5f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF3B5A2D)
                        .binderMultiplier(0.85f)
                , 2)
                .armor(40.0f, 17.0f, 15.0f, 1.0f, 1.25f, 5.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TWILIT)
                .addUniversalModifier(SoaSmitheryModifiers.PRECIPITATE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .bow(0.6f, 2.0f, 0.0f)
                .arrowShaft(1.4f, 20)
                .build());

        RAVEN_FEATHER = id("raven_feather");
        SmitheryAPI.registerMaterial(RAVEN_FEATHER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF696D72)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TWILIT)
                .addUniversalModifier(SoaSmitheryModifiers.VEILED)
                .fletching(0.95f, 1.15f)
                .build());

        STEELEAF = id("steeleaf");
        SmitheryAPI.registerMaterial(STEELEAF, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.5f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(180)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF1E3214)
                        .binderMultiplier(1.0f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SYNERGY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TWILIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LUCKYTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SYNERGY), armorPieces())
                .armor(50.0f, 17.5f, 15.0f, 1.3f, 1.25f, 5.0f)
                .bow(1.2f, 1.5f, 2.0f)
                .arrowShaft(0.6f, 10)
                .fletching(1.0f, 0.8f)
                .build());

    }

    private static void registerIceAndFireCompat() {
        FEATHERAMPHITHERE = id("featheramphithere");
        SmitheryAPI.registerMaterial(FEATHERAMPHITHERE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3EA85A)
                        .binderMultiplier(1.0f)
                .build());

        SCALEBLUEDRAGON = id("scalebluedragon");
        SmitheryAPI.registerMaterial(SCALEBLUEDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3E6FC8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALEBLUESEASERPENT = id("scaleblueseaserpent");
        SmitheryAPI.registerMaterial(SCALEBLUESEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3E9FD8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        SCALEBRONZEDRAGON = id("scalebronzedragon");
        SmitheryAPI.registerMaterial(SCALEBRONZEDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB87A3E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALEBRONZESEASERPENT = id("scalebronzeseaserpent");
        SmitheryAPI.registerMaterial(SCALEBRONZESEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC8923E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        CHITINBROWNDEATHWORM = id("chitinbrowndeathworm");
        SmitheryAPI.registerMaterial(CHITINBROWNDEATHWORM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F6F4A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(15.0f, 14.0f, 15.0f, 0.625f, 2.25f, 1.875f)
                .build());

        SCALEDEEPBLUESEASERPENT = id("scaledeepblueseaserpent");
        SmitheryAPI.registerMaterial(SCALEDEEPBLUESEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E4A8F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        DESERT_MYRMEX = id("desert_myrmex");
        SmitheryAPI.registerMaterial(DESERT_MYRMEX, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8C28F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(56.0f, 29.7f, 57.6f, 17.7882f, 0.0f, 57.6f)
                .build());

        CHITINDESERTMYRMEX = id("chitindesertmyrmex");
        SmitheryAPI.registerMaterial(CHITINDESERTMYRMEX, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8C28F)
                        .binderMultiplier(1.0f)
                .build());

        BONEDRAGON = id("bonedragon");
        SmitheryAPI.registerMaterial(BONEDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE0D8C0)
                        .binderMultiplier(1.0f)
                .build());

        DRAGONBONE = id("dragonbone");
        SmitheryAPI.registerMaterial(DRAGONBONE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE0D8C0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(42.0f, 32.4f, 76.8f, 33.0353f, 0.0f, 76.8f)
                .build());

        ENDER_DRAGON_SQUAMA = id("ender_dragon_squama");
        SmitheryAPI.registerMaterial(ENDER_DRAGON_SQUAMA, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E2A38)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 18.9f, 38.4f, 15.5647f, 0.0f, 38.4f)
                .build());

        DRAGONSTEEL_FIRE = id("dragonsteel_fire");
        SmitheryAPI.registerMaterial(DRAGONSTEEL_FIRE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F2E2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 50.0f, 195.84f, 10.1647f, 0.0f, 195.84f)
                .build());

        LEATHERFORESTTROLL = id("leatherforesttroll");
        SmitheryAPI.registerMaterial(LEATHERFORESTTROLL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6F8F4A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(20.0f, 14.0f, 20.0f, 1.25f, 1.5f, 2.5f)
                .build());

        LEATHERFROSTTROLL = id("leatherfrosttroll");
        SmitheryAPI.registerMaterial(LEATHERFROSTTROLL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FB8C8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(20.0f, 14.0f, 20.0f, 1.25f, 1.5f, 2.5f)
                .build());

        GHASTLY = id("ghastly");
        SmitheryAPI.registerMaterial(GHASTLY, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E8E8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(103.0f, 24.3f, 19.2f, 22.8706f, 0.0f, 19.2f)
                .build());

        GHOULISH = id("ghoulish");
        SmitheryAPI.registerMaterial(GHOULISH, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB8D89F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 17.28f, 161.28f, 33.0353f, 0.0f, 161.28f)
                .build());

        SCALEGRAYDRAGON = id("scalegraydragon");
        SmitheryAPI.registerMaterial(SCALEGRAYDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F8F8F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALEGREENDRAGON = id("scalegreendragon");
        SmitheryAPI.registerMaterial(SCALEGREENDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3E8F3E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALEGREENSEASERPENT = id("scalegreenseaserpent");
        SmitheryAPI.registerMaterial(SCALEGREENSEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3EA86F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        GUARDIANSCALE = id("guardianscale");
        SmitheryAPI.registerMaterial(GUARDIANSCALE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FA89F)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.AQUADYNAMIC)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DIVINESHIELD),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        HARD_SPIDER_LEG = id("hard_spider_leg");
        SmitheryAPI.registerMaterial(HARD_SPIDER_LEG, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A3A32)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(32.0f, 12.6f, 0.0f, 3.7059f, 0.0f, 0.0f)
                .build());

        DRAGONSTEEL_ICE = id("dragonsteel_ice");
        SmitheryAPI.registerMaterial(DRAGONSTEEL_ICE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7AC8E8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 50.0f, 195.84f, 10.1647f, 0.0f, 195.84f)
                .build());

        JUNGLE_MYRMEX = id("jungle_myrmex");
        SmitheryAPI.registerMaterial(JUNGLE_MYRMEX, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FA83E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(56.0f, 29.7f, 57.6f, 17.7882f, 0.0f, 57.6f)
                .build());

        CHITINJUNGLEMYRMEX = id("chitinjunglemyrmex");
        SmitheryAPI.registerMaterial(CHITINJUNGLEMYRMEX, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF6FA83E)
                        .binderMultiplier(1.0f)
                .build());

        LEATHERMOUNTAINTROLL = id("leathermountaintroll");
        SmitheryAPI.registerMaterial(LEATHERMOUNTAINTROLL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F7A5A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(20.0f, 14.0f, 20.0f, 1.25f, 1.5f, 2.5f)
                .build());

        SCALEPURPLESEASERPENT = id("scalepurpleseaserpent");
        SmitheryAPI.registerMaterial(SCALEPURPLESEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F3EA8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        SCALEREDDRAGON = id("scalereddragon");
        SmitheryAPI.registerMaterial(SCALEREDDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC22E2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALEREDSEASERPENT = id("scaleredseaserpent");
        SmitheryAPI.registerMaterial(SCALEREDSEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD23E3E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        SCALESAPPHIREDRAGON = id("scalesapphiredragon");
        SmitheryAPI.registerMaterial(SCALESAPPHIREDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E5AC8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SCALESHINY = id("scaleshiny");
        SmitheryAPI.registerMaterial(SCALESHINY, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E8F0)
                        .binderMultiplier(1.0f)
                .build());

        SCALESILVERDRAGON = id("scalesilverdragon");
        SmitheryAPI.registerMaterial(SCALESILVERDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC8C8D0)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        SPIDER_LEG = id("spider_leg");
        SmitheryAPI.registerMaterial(SPIDER_LEG, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF4A3A32)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(5.0f, 9.45f, 0.0f, 0.0f, 0.0f, 0.0f)
                .build());

        FEATHERSTYMPHALIANBIRD = id("featherstymphalianbird");
        SmitheryAPI.registerMaterial(FEATHERSTYMPHALIANBIRD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB8A88F)
                        .binderMultiplier(1.0f)
                .build());

        CHITINTANDEATHWORM = id("chitintandeathworm");
        SmitheryAPI.registerMaterial(CHITINTANDEATHWORM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8C28F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(15.0f, 14.0f, 15.0f, 0.625f, 2.25f, 1.875f)
                .build());

        SCALETEALSEASERPENT = id("scaletealseaserpent");
        SmitheryAPI.registerMaterial(SCALETEALSEASERPENT, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2EA8A8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(30.0f, 14.0f, 30.0f, 3.125f, 3.75f, 3.75f)
                .build());

        CHITINWHITEDEATHWORM = id("chitinwhitedeathworm");
        SmitheryAPI.registerMaterial(CHITINWHITEDEATHWORM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E4D8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(15.0f, 14.0f, 15.0f, 0.625f, 2.25f, 1.875f)
                .build());

        SCALEWHITEDRAGON = id("scalewhitedragon");
        SmitheryAPI.registerMaterial(SCALEWHITEDRAGON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8E8E8)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(36.0f, 18.0f, 36.0f, 1.875f, 3.0f, 4.5f)
                .build());

        BONEWITHER = id("bonewither");
        SmitheryAPI.registerMaterial(BONEWITHER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3A3A38)
                        .binderMultiplier(1.0f)
                .build());

    }

    private static void registerTinkersConstructBase() {
        ARDITE = id("ardite");
        SmitheryAPI.registerMaterial(ARDITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(3.5f)
                        .attackDamage(3.6f)
                        .durabilityPerIngot(990)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFD15517)
                        .binderMultiplier(1.4f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.PETRAMOR)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.STONEBOUND),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        BLUESLIME = id("blueslime");
        SmitheryAPI.registerMaterial(BLUESLIME, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.0f)
                        .attackDamage(1.8f)
                        .durabilityPerIngot(780)
                        .meltingTemp(0.0f)
                        .partColor(0xFF71C8D2)
                        .binderMultiplier(1.3f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.SLIMEY)
                .build());

        BONE = id("bone");
        SmitheryAPI.registerMaterial(BONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.09f)
                        .attackDamage(2.5f)
                        .durabilityPerIngot(200)
                        .meltingTemp(600.0f)
                        .partColor(0xFF7B7E6B)
                        .binderMultiplier(1.1f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.FRACTURED)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SPLITTING), SmitheryPartTypes.ARROW_SHAFT)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SPLINTERING),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        BRONZE = id("bronze");
        SmitheryAPI.registerMaterial(BRONZE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(430)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF914321)
                        .binderMultiplier(1.1f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .build());

        CACTUS = id("cactus");
        SmitheryAPI.registerMaterial(CACTUS, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(3.4f)
                        .durabilityPerIngot(210)
                        .meltingTemp(600.0f)
                        .partColor(0xFF527D26)
                        .binderMultiplier(0.85f)
                , 1)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PENETRATIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PRICKLY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.THRONY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.THRONY_ARMOR), armorPieces())
                .build());

        COBALT = id("cobalt");
        SmitheryAPI.registerMaterial(COBALT, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(14.0f)
                        .attackDamage(4.1f)
                        .durabilityPerIngot(780)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFB8E8F9)
                        .binderMultiplier(0.9f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.MOMENTUM),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        ELECTRUM = id("electrum");
        SmitheryAPI.registerMaterial(ELECTRUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(12.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(50)
                        .meltingTemp(600.0f)
                        .partColor(0xFFE4AD55)
                        .binderMultiplier(1.1f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.SHOCKING)
                .build());

        ENDSTONE = id("endstone");
        SmitheryAPI.registerMaterial(ENDSTONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(3.23f)
                        .attackDamage(3.23f)
                        .durabilityPerIngot(420)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFD5DA94)
                        .binderMultiplier(0.85f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERFERENCE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERPORT_ARMOR), armorPieces())
                .build());

        ENDROD = id("endrod");
        SmitheryAPI.registerMaterial(ENDROD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9C9691)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.ENDSPEED)
                .arrowShaft(0.7f, 1)
                .build());

        FEATHER = id("feather");
        SmitheryAPI.registerMaterial(FEATHER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD3D3D3)
                        .binderMultiplier(1.0f)
                .fletching(1.0f, 1.0f)
                .build());

        FIREWOOD = id("firewood");
        SmitheryAPI.registerMaterial(FIREWOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(6.0f)
                        .attackDamage(5.5f)
                        .durabilityPerIngot(550)
                        .meltingTemp(600.0f)
                        .partColor(0xFF452629)
                        .binderMultiplier(1.0f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.AUTOSMELT)
                .build());

        ICE = id("ice");
        SmitheryAPI.registerMaterial(ICE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF45BEDC)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.FREEZING)
                .arrowShaft(0.95f, 3)
                .build());

        // knightslime is gone — it was a duplicate of Smithery's slimeknightium and is now
        // layered onto it in retuneBuiltinMaterials().

        LEAD = id("lead");
        SmitheryAPI.registerMaterial(LEAD, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.25f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(434)
                        .meltingTemp(600.0f)
                        .partColor(0xFF5F666D)
                        .binderMultiplier(0.7f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.HEAVY)
                .addUniversalModifier(SoaSmitheryModifiers.POISONOUS)
                .build());

        LEAF = id("leaf");
        SmitheryAPI.registerMaterial(LEAF, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8D8D8)
                        .binderMultiplier(1.0f)
                .fletching(0.5f, 1.5f)
                .build());

        MAGMA = id("magma");
        SmitheryAPI.registerMaterial(MAGMA, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF893E20)
                        .colorCycle(24, 0xFF4A1919, 0xFF652828, 0xFFEE7A21) // [auto-color]
                        .binderMultiplier(1.0f)
                .bowstring(0.85f)
                .build());

        MAGMASLIME = id("magmaslime");
        SmitheryAPI.registerMaterial(MAGMASLIME, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(2.1f)
                        .attackDamage(7.0f)
                        .durabilityPerIngot(600)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8622E)
                        .binderMultiplier(0.85f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.FLAMMABLE)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAT),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        MANYULLYN = id("manyullyn");
        SmitheryAPI.registerMaterial(MANYULLYN, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(7.02f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(820)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFA87DDE)
                        .binderMultiplier(0.5f)
                , 4)
                .addUniversalModifier(SoaSmitheryModifiers.COLDBLOODED)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.INSATIABLE),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        NETHERRACK = id("netherrack");
        SmitheryAPI.registerMaterial(NETHERRACK, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(270)
                        .meltingTemp(600.0f)
                        .partColor(0xFF652828)
                        .binderMultiplier(0.85f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.HELLISH)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        OBSIDIAN = id("obsidian");
        SmitheryAPI.registerMaterial(OBSIDIAN, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.07f)
                        .attackDamage(4.2f)
                        .durabilityPerIngot(139)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF3B2754)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.DURITOS)
                .build());

        PAPER = id("paper");
        SmitheryAPI.registerMaterial(PAPER, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.51f)
                        .attackDamage(0.05f)
                        .durabilityPerIngot(12)
                        .meltingTemp(0.0f)
                        .partColor(0xFFFCFCF2)
                        .binderMultiplier(0.1f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.WRITABLE, java.util.Map.of("bonus_slots", 1))
                .addHeadModifier(SoaSmitheryModifiers.WRITABLE2, java.util.Map.of("bonus_slots", 1))
                .arrowShaft(0.7f, 20)
                .build());

        PIGIRON = id("pigiron");
        SmitheryAPI.registerMaterial(PIGIRON, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.2f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(380)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF874E52)
                        .binderMultiplier(1.2f)
                , 2)
                .addUniversalModifier(SoaSmitheryModifiers.TASTY)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.BACONLICIOUS),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        REED = id("reed");
        SmitheryAPI.registerMaterial(REED, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF744728)
                        .binderMultiplier(1.0f)
                .addUniversalModifier(SoaSmitheryModifiers.BREAKABLE)
                .arrowShaft(1.5f, 3)
                .build());

        SILVER = id("silver");
        SmitheryAPI.registerMaterial(SILVER, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(250)
                        .meltingTemp(600.0f)
                        .partColor(0xFFD0D0D0)
                        .binderMultiplier(0.95f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.HOLY)
                .build());

        SPONGE = id("sponge");
        SmitheryAPI.registerMaterial(SPONGE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.02f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(1050)
                        .meltingTemp(0.0f)
                        .partColor(0xFFCDCE4A)
                        .binderMultiplier(1.2f)
                , 1)
                .addUniversalModifier(SoaSmitheryModifiers.SQUEAKY)
                .build());

        STEEL = id("steel");
        SmitheryAPI.registerMaterial(STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(6.0f)
                        .durabilityPerIngot(540)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF585858)
                        .binderMultiplier(0.9f)
                , 3)
                .addUniversalModifier(SoaSmitheryModifiers.STIFF)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SHARP),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.4f, 1.5f, 5.0f)
                .build());

        TREATEDWOOD = id("treatedwood");
        SmitheryAPI.registerMaterial(TREATEDWOOD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7A5B3A)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(3.0f, 10.8f, 7.68f, 25.4118f, 0.0f, 7.68f)
                .build());

        VINE = id("vine");
        SmitheryAPI.registerMaterial(VINE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF838383)
                        .binderMultiplier(1.0f)
                .bowstring(1.0f)
                .build());

    }

    private static void registerMysticalAgriculture() {
        MA_BASE_ESSENCE = id("ma.base_essence");
        SmitheryAPI.registerMaterial(MA_BASE_ESSENCE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.0f)
                        .attackDamage(2.8f)
                        .durabilityPerIngot(75)
                        .meltingTemp(0.0f)
                        .partColor(0xFF404040)
                        .binderMultiplier(0.6f)
                , 1)
                .armor(40.0f, 4.0f, 10.0f, 1.1f, 0.25f, 12.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .build());

        MA_INFERIUM = id("ma.inferium");
        SmitheryAPI.registerMaterial(MA_INFERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(3.4f)
                        .durabilityPerIngot(150)
                        .meltingTemp(600.0f)
                        .partColor(0xFF9BBA00)
                        .binderMultiplier(0.8f)
                , 1)
                .armor(50.0f, 8.0f, 15.0f, 1.3f, 0.75f, 15.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .build());

        MA_INTERMEDIUM = id("ma.intermedium");
        SmitheryAPI.registerMaterial(MA_INTERMEDIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(7.1f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFC94D00)
                        .binderMultiplier(1.4f)
                , 3)
                .armor(70.0f, 18.0f, 40.0f, 2.0f, 1.5f, 45.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .build());

        MA_PROSPERITY = id("ma.prosperity");
        SmitheryAPI.registerMaterial(MA_PROSPERITY, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE9FEFE)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(100.0f, 2.0f, 60.0f, 0.6f, 1.25f, 50.0f)
                .build());

        MA_PRUDENTIUM = id("ma.prudentium");
        SmitheryAPI.registerMaterial(MA_PRUDENTIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.5f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF005411)
                        .binderMultiplier(1.1f)
                , 2)
                .armor(60.0f, 11.0f, 20.0f, 1.6f, 1.0f, 25.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .build());

        MA_SOULIUM = id("ma.soulium");
        SmitheryAPI.registerMaterial(MA_SOULIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF965C40)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHUNKY), allToolTypes())
                .armor(14.0f, 20.7f, 5.376f, 16.9412f, 0.0f, 5.376f)
                .build());

        MA_SUPERIUM = id("ma.superium");
        SmitheryAPI.registerMaterial(MA_SUPERIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.0f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF0090F7)
                        .binderMultiplier(1.7f)
                , 4)
                .armor(80.0f, 23.0f, 60.0f, 2.7f, 2.0f, 75.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .build());

        MA_SUPREMIUM = id("ma.supremium");
        SmitheryAPI.registerMaterial(MA_SUPREMIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.0f)
                        .attackDamage(9.4f)
                        .durabilityPerIngot(2400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFDF0000)
                        .binderMultiplier(2.2f)
                , 5)
                .armor(120.0f, 27.0f, 80.0f, 3.2f, 4.0f, 74.0f)
                .addUniversalModifier(SoaSmitheryModifiers.PROSPEROUS)
                .addHeadModifier(SoaSmitheryModifiers.DURITOS)
                .build());

    }

    private static void registerBloodMagic() {
        BLOOD_INFUSED_IRON = id("blood_infused_iron");
        SmitheryAPI.registerMaterial(BLOOD_INFUSED_IRON, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF932B24)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.COTLIFESTEAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIVING2), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PENETRATIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_BLOODBOUND_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIVING2), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_GUARD_ARMOR), armorPieces())
                .armor(240.0f, 20.0f, 80.0f, 1.6f, 2.0f, 100.0f)
                .build());

        BLOOD_INFUSED_WOOD = id("blood_infused_wood");
        SmitheryAPI.registerMaterial(BLOOD_INFUSED_WOOD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7E281C)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(80.0f, 17.0f, 100.0f, 1.1f, 1.0f, 80.0f)
                .build());

        BOUND_METAL = id("bound_metal");
        SmitheryAPI.registerMaterial(BOUND_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(17.2f)
                        .attackDamage(9.3f)
                        .durabilityPerIngot(2400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF401919)
                        .binderMultiplier(1.6f)
                , 4)
                .armor(260.0f, 24.0f, 120.0f, 2.8f, 3.0f, 100.0f)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_BLOODBOUND)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CRYSTALYS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_BLOODBOUND_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_GUARD_ARMOR), armorPieces())
                .bow(0.6f, 1.4f, 7.2f)
                .build());

        COAGULATED_BLOOD_SAND = id("coagulated_blood_sand");
        SmitheryAPI.registerMaterial(COAGULATED_BLOOD_SAND, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF7A2E2E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(5.0f, 18.9f, -18.176f, 0.0f, 0.0f, -18.176f)
                .build());

        SENTIENT_METAL = id("sentient_metal");
        SmitheryAPI.registerMaterial(SENTIENT_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(16.0f)
                        .attackDamage(8.2f)
                        .durabilityPerIngot(1800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFBADEE0)
                        .binderMultiplier(1.4f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SENTIENT_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SENTIENT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL_ARMOR), armorPieces())
                .armor(220.0f, 21.0f, 80.0f, 2.2f, 2.0f, 40.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_WILLFUL),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SENTIENT),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.75f, 1.25f, 6.0f)
                .build());

    }

    private static void registerTinkersAether() {
        AERCLOUD_BLUE = id("aercloud_blue");
        SmitheryAPI.registerMaterial(AERCLOUD_BLUE, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFC3F4F4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(94.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f)
                .build());

        CANDY_CANE = id("candy_cane");
        SmitheryAPI.registerMaterial(CANDY_CANE, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.5f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(100)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB52E2E)
                        .binderMultiplier(0.9f)
                , 1)
                .armor(80.0f, 20.0f, 13.44f, 0.8f, 1.0f, 13.44f)
                .addUniversalModifier(SoaSmitheryModifiers.FESTIVE)
                .addUniversalModifier(SoaSmitheryModifiers.TASTY)
                .build());

        AERCLOUD_COLD = id("aercloud_cold");
        SmitheryAPI.registerMaterial(AERCLOUD_COLD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF4F4F4)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(94.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f)
                .build());

        AERCLOUD_GOLD = id("aercloud_gold");
        SmitheryAPI.registerMaterial(AERCLOUD_GOLD, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE8C23E)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(117.0f, 0.45f, 2.24f, 0.7f, 0.0f, 2.24f)
                .build());

        GOLDEN_AMBER = id("golden_amber");
        SmitheryAPI.registerMaterial(GOLDEN_AMBER, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(3.0f)
                        .attackDamage(2.5f)
                        .durabilityPerIngot(60)
                        .meltingTemp(600.0f)
                        .partColor(0xFFD27817)
                        .binderMultiplier(0.8f)
                , 1)
                .armor(70.0f, 4.2f, 12.0f, 1.2f, 3.25f, 8.0f)
                .addUniversalModifier(SoaSmitheryModifiers.GILDED)
                .build());

        GOLDEN_FEATHER = id("golden_feather");
        SmitheryAPI.registerMaterial(GOLDEN_FEATHER, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFF5C825)
                        .binderMultiplier(1.0f)
                .fletching(1.0f, 2.0f)
                .build());

        GOLDEN_OAK_LEAF = id("golden_oak_leaf");
        SmitheryAPI.registerMaterial(GOLDEN_OAK_LEAF, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFD8A82E)
                        .binderMultiplier(1.0f)
                .fletching(0.7f, 1.7f)
                .build());

        GRAVITITE = id("gravitite");
        SmitheryAPI.registerMaterial(GRAVITITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(8.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(568)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF802377)
                        .binderMultiplier(0.8f)
                , 3)
                .armor(240.0f, 23.0f, 10.08f, 2.0f, 1.5f, 10.08f)
                .addUniversalModifier(SoaSmitheryModifiers.LAUNCHING)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ANTIGRAV),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.GILDED),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        HOLIDAY_LEAF = id("holiday_leaf");
        SmitheryAPI.registerMaterial(HOLIDAY_LEAF, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2E8F3E)
                        .binderMultiplier(1.0f)
                .fletching(0.9f, 1.8f)
                .build());

        HOLYSTONE = id("holystone");
        SmitheryAPI.registerMaterial(HOLYSTONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(131)
                        .meltingTemp(600.0f)
                        .partColor(0xFF969696)
                        .binderMultiplier(0.75f)
                , 1)
                .armor(100.0f, 13.0f, 2.8f, 0.7f, 0.5f, 2.8f)
                .addUniversalModifier(SoaSmitheryModifiers.CHEAP)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAPSKATE),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ENLIGHTENED),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        ICESTONE = id("icestone");
        SmitheryAPI.registerMaterial(ICESTONE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(131)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFF1F1F1)
                        .binderMultiplier(0.75f)
                , 2)
                .armor(60.0f, 12.0f, 4.48f, 1.8f, 0.25f, 4.48f)
                .addUniversalModifier(SoaSmitheryModifiers.REFRIGERATION)
                .build());

        SKYROOT = id("skyroot");
        SmitheryAPI.registerMaterial(SKYROOT, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(2.0f)
                        .attackDamage(2.0f)
                        .durabilityPerIngot(35)
                        .meltingTemp(0.0f)
                        .partColor(0xFF58573E)
                        .binderMultiplier(1.0f)
                , 1)
                .armor(1.0f, 9.0f, 2.24f, 23.2941f, 0.0f, 2.24f)
                .addUniversalModifier(SoaSmitheryModifiers.ECOLOGICAL)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.SKYROOTED),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(1.05f, 1.05f, 0.05f)
                .arrowShaft(1.05f, 0)
                .build());

        SKYROOT_LEAF = id("skyroot_leaf");
        SmitheryAPI.registerMaterial(SKYROOT_LEAF, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF9FC970)
                        .binderMultiplier(1.0f)
                .fletching(0.5f, 1.6f)
                .build());

        SWET = id("swet");
        SmitheryAPI.registerMaterial(SWET, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(4.0f)
                        .attackDamage(2.0f)
                        .durabilityPerIngot(400)
                        .meltingTemp(0.0f)
                        .partColor(0xFF195975)
                        .binderMultiplier(0.8f)
                , 1)
                .armor(200.0f, 16.0f, 40.32f, 1.4f, 0.0f, 40.32f)
                .addUniversalModifier(SoaSmitheryModifiers.SWETTY)
                .bow(1.0f, 1.5f, 0.5f)
                .build());

        VALKYRIE = id("valkyrie");
        SmitheryAPI.registerMaterial(VALKYRIE, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(9.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(1561)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFE6F6F6)
                        .binderMultiplier(0.9f)
                , 4)
                .armor(160.0f, 26.8f, 30.0f, 2.5f, 2.75f, 20.0f)
                .addUniversalModifier(SoaSmitheryModifiers.REACH)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.GILDED),
                        SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        ZANITE = id("zanite");
        SmitheryAPI.registerMaterial(ZANITE, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(8.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(250)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF7A36E0)
                        .binderMultiplier(0.9f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ASSASSINTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GILDED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CRYSTALTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLD_GROUND_ARMOR), armorPieces())
                .armor(150.0f, 21.0f, 5.6f, 2.2f, 1.5f, 5.6f)
                .build());

    }

    private static void registerDraconicEvolution() {
        CHAOTIC_METAL = id("chaotic_metal");
        SmitheryAPI.registerMaterial(CHAOTIC_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(11)
                        .miningSpeed(24.0f)
                        .attackDamage(64.0f)
                        .durabilityPerIngot(6400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF2F3236)
                        .colorCycle(48, 0xFF53585E, 0xFF212326, 0xFF292C30, 0xFF212326) // [auto-color]
                        .binderMultiplier(3.75f)
                , 5)
                .armor(720.0f, 108.0f, 160.0f, 24.0f, 8.0f, 256.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_SOUL_REND)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CELESTIAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GALE_FORCE_ARMOR), armorPieces())
                .bow(1.2f, 2.0f, 28.6f)
                .build());

        DRACONIC_METAL = id("draconic_metal");
        SmitheryAPI.registerMaterial(DRACONIC_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(10)
                        .miningSpeed(18.2f)
                        .attackDamage(32.0f)
                        .durabilityPerIngot(7500)
                        .meltingTemp(8000.0f)
                        .partColor(0xFFD59F85)
                        .colorCycle(48, 0xFFB2593A, 0xFFFAEDDB, 0xFFB04A26, 0xFFFAEDDB) // [auto-color]
                        .binderMultiplier(2.5f)
                , 5)
                .armor(480.0f, 74.0f, 128.0f, 16.0f, 3.6f, 144.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_SOUL_REND)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CELESTIAL_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GALE_FORCE_ARMOR), armorPieces())
                .bow(0.85f, 1.6f, 17.2f)
                .build());

        DRACONIUM = id("draconium");
        SmitheryAPI.registerMaterial(DRACONIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(12.6f)
                        .attackDamage(15.6f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF7A45AB)
                        .binderMultiplier(1.25f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GIANTSLAYER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_REND1), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SUPERDENSE_ARMOR), armorPieces())
                .armor(124.0f, 46.0f, 54.0f, 5.2f, 2.5f, 40.0f)
                .bow(0.95f, 1.1f, 8.0f)
                .build());

        PRIMAL_METAL = id("primal_metal");
        SmitheryAPI.registerMaterial(PRIMAL_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(8)
                        .miningSpeed(12.2f)
                        .attackDamage(28.2f)
                        .durabilityPerIngot(15000)
                        .meltingTemp(5000.0f)
                        .partColor(0xFF82A37C)
                        .binderMultiplier(2.8f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BANE_OF_NIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CORRUPTING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CULLING), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GALE_FORCE_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.WARPDRAINTRAIT_ARMOR), armorPieces())
                .armor(400.0f, 64.0f, 99.0f, 5.5f, 3.0f, 125.0f)
                .bow(1.5f, 1.0f, 19.2f)
                .build());

        WYVERN_METAL = id("wyvern_metal");
        SmitheryAPI.registerMaterial(WYVERN_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(9)
                        .miningSpeed(15.6f)
                        .attackDamage(20.4f)
                        .durabilityPerIngot(10400)
                        .meltingTemp(6000.0f)
                        .partColor(0xFF5B4273)
                        .binderMultiplier(1.75f)
                , 5)
                .armor(340.0f, 56.0f, 96.0f, 10.0f, 2.4f, 108.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.TCONEVO_SOUL_REND)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_EVOLVED_ARMOR), armorPieces())
                .bow(0.9f, 1.3f, 10.0f)
                .build());

    }

    private static void registerEnderio() {
        CONDUCTIVE_IRON = id("conductive_iron");
        SmitheryAPI.registerMaterial(CONDUCTIVE_IRON, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.5f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(100)
                        .meltingTemp(600.0f)
                        .partColor(0xFFC79E88)
                        .binderMultiplier(0.8f)
                , 1)
                .armor(80.0f, 5.625f, 28.0f, 0.8f, 1.0f, 28.0f)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .addHeadModifier(SoaSmitheryModifiers.CRUDE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR1), armorPieces())
                .build());

        DARK_STEEL = id("dark_steel");
        SmitheryAPI.registerMaterial(DARK_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(7.0f)
                        .attackDamage(5.0f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFF333333)
                        .binderMultiplier(1.0f)
                , 3)
                .armor(140.0f, 18.0f, 28.0f, 1.5f, 2.0f, 28.0f)
                .addUniversalModifier(SoaSmitheryModifiers.UNNATURAL)
                .addHeadModifier(SoaSmitheryModifiers.ENDERFERENCE)
                .addUniversalModifier(SoaSmitheryModifiers.DENSE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MUNDANE_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .build());

        ELECTRICAL_STEEL = id("electrical_steel");
        SmitheryAPI.registerMaterial(ELECTRICAL_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF9FA4A8)
                        .binderMultiplier(0.9f)
                , 2)
                .armor(60.0f, 7.8f, 8.4f, 1.0f, 1.0f, 8.4f)
                .addUniversalModifier(SoaSmitheryModifiers.LIGHTWEIGHT)
                .addHeadModifier(SoaSmitheryModifiers.SHOCKING)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .build());

        END_STEEL = id("end_steel");
        SmitheryAPI.registerMaterial(END_STEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(13.6f)
                        .attackDamage(8.0f)
                        .durabilityPerIngot(1200)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFBCB463)
                        .binderMultiplier(1.36f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERFERENCE), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.UNNATURAL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MUNDANE_ARMOR1), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.STEADY_ARMOR), armorPieces())
                .armor(150.0f, 21.0f, 16.8f, 2.2f, 2.75f, 16.8f)
                .build());

        ENDER_CRYSTAL = id("ender_crystal");
        SmitheryAPI.registerMaterial(ENDER_CRYSTAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFBFE6D4)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDSPEED), SmitheryPartTypes.ARCANE_FOCUS)
                .build());

        ENDORIUM = id("endorium");
        SmitheryAPI.registerMaterial(ENDORIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF2EA89F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(120.0f, 25.2f, 0.0f, 20.7529f, 0.0f, 0.0f)
                .build());

        ENERGETIC_ALLOY = id("energetic_alloy");
        SmitheryAPI.registerMaterial(ENERGETIC_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(7.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFFEEBB43)
                        .binderMultiplier(0.9f)
                , 2)
                .armor(80.0f, 16.5f, 44.8f, 1.2f, 0.5f, 44.8f)
                .addUniversalModifier(SoaSmitheryModifiers.PETRAMOR)
                .addHeadModifier(SoaSmitheryModifiers.UNNATURAL)
                .addUniversalModifier(SoaSmitheryModifiers.HOLY)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AMBITIOUS_ARMOR), armorPieces())
                .build());

        CONSTRUCTION_ALLOY = id("construction_alloy");
        SmitheryAPI.registerMaterial(CONSTRUCTION_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(3.0f)
                        .attackDamage(1.5f)
                        .durabilityPerIngot(30)
                        .meltingTemp(0.0f)
                        .partColor(0xFF8F8F97)
                        .binderMultiplier(0.5f)
                , 1)
                .armor(50.0f, 10.0f, 1.12f, 1.2f, 0.0f, 1.12f)
                .addUniversalModifier(SoaSmitheryModifiers.CHEAPSKATE)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHEAP_ARMOR), armorPieces())
                .build());

        PULSATING_CRYSTAL = id("pulsating_crystal");
        SmitheryAPI.registerMaterial(PULSATING_CRYSTAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFA1FEFB)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERFERENCE), SmitheryPartTypes.ARCANE_FOCUS)
                .build());

        PULSATING_IRON = id("pulsating_iron");
        SmitheryAPI.registerMaterial(PULSATING_IRON, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.0f)
                        .attackDamage(3.5f)
                        .durabilityPerIngot(150)
                        .meltingTemp(600.0f)
                        .partColor(0xFF6FBF83)
                        .binderMultiplier(0.85f)
                , 1)
                .armor(100.0f, 9.0f, 28.0f, 1.3f, 1.0f, 28.0f)
                .addUniversalModifier(SoaSmitheryModifiers.ENDERPORT)
                .addUniversalModifier(SoaSmitheryModifiers.POISONOUS)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ENDERPORT_ARMOR), armorPieces())
                .build());

        REDSTONE_ALLOY = id("redstone_alloy");
        SmitheryAPI.registerMaterial(REDSTONE_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(5.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(200)
                        .meltingTemp(600.0f)
                        .partColor(0xFFE55252)
                        .binderMultiplier(0.85f)
                , 1)
                .armor(40.0f, 6.75f, 16.8f, 1.1f, 1.0f, 16.8f)
                .addUniversalModifier(SoaSmitheryModifiers.CRUDE)
                .addHeadModifier(SoaSmitheryModifiers.SHOCKING)
                .addUniversalModifier(SoaSmitheryModifiers.WRITABLE, java.util.Map.of("bonus_slots", 1))
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MAGNETIC_ARMOR1), armorPieces())
                .build());

        SOULARIUM = id("soularium");
        SmitheryAPI.registerMaterial(SOULARIUM, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.5f)
                        .durabilityPerIngot(300)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF614C2B)
                        .binderMultiplier(0.85f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CHUNKY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOMENTUM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SOUL_GUARD_ARMOR), armorPieces())
                .armor(100.0f, 4.5f, 140.0f, 1.4f, 0.5f, 140.0f)
                .build());

        STELLAR_ALLOY_GC = id("stellar_alloy_gc");
        SmitheryAPI.registerMaterial(STELLAR_ALLOY_GC, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.5f)
                        .attackDamage(10.9f)
                        .durabilityPerIngot(5000)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFDCE8F0)
                        .binderMultiplier(1.4f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOMENTUM), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SPARTAN_ARMOR), armorPieces())
                .armor(240.0f, 30.5f, 80.0f, 3.0f, 6.0f, 40.0f)
                .bow(1.6667f, 1.0f, 1.0f)
                .build());

        VIBRANT_ALLOY = id("vibrant_alloy");
        SmitheryAPI.registerMaterial(VIBRANT_ALLOY, binderSlots(MaterialStats.builder()
                        .harvestLevel(3)
                        .miningSpeed(16.2f)
                        .attackDamage(8.7f)
                        .durabilityPerIngot(600)
                        .meltingTemp(1500.0f)
                        .partColor(0xFFE0E979)
                        .binderMultiplier(0.9f)
                , 3)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.BANE_OF_NIGHT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MOTIONTRAIT_ARMOR), armorPieces())
                .armor(20.0f, 19.2f, 6.72f, 1.6f, 2.25f, 6.72f)
                .build());

        VIBRANT_CRYSTAL = id("vibrant_crystal");
        SmitheryAPI.registerMaterial(VIBRANT_CRYSTAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFB2FF9D)
                        .binderMultiplier(1.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_CHAIN_LIGHTNING),
                        SmitheryPartTypes.ARCANE_FOCUS)
                .build());

    }

    private static void registerThermalFoundation() {
        CONSTANTAN = id("constantan");
        SmitheryAPI.registerMaterial(CONSTANTAN, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(5.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(200)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF793C17)
                        .binderMultiplier(0.85f)
                , 2)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARCANE_FOCUS, SmitheryPartTypes.ARROW_HEAD, SmitheryPartTypes.SHURIKEN_BLADE)
                .addUniversalModifier(SoaSmitheryModifiers.FREEZING)
                .bow(0.75f, 1.25f, 3.0f)
                .build());

        MITHRIL = id("mithril");
        SmitheryAPI.registerMaterial(MITHRIL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(11.3f)
                        .attackDamage(7.9f)
                        .durabilityPerIngot(1400)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFBF43F2)
                        .binderMultiplier(1.4f)
                , 4)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MORTAL_WOUNDS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.RELIABLETRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .armor(180.0f, 21.2f, 160.0f, 2.4f, 2.0f, 220.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7692f, 1.5f, 2.5f)
                .arrowShaft(1.75f, 2)
                .build());

    }

    private static void registerProjecteEe2() {
        DARK_MATTER = id("dark_matter");
        SmitheryAPI.registerMaterial(DARK_MATTER, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(15.0f)
                        .attackDamage(14.0f)
                        .durabilityPerIngot(3200)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF0F0029)
                        .binderMultiplier(1.0f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.CALCIC_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SUPERHEAVY), armorPieces())
                .armor(200.0f, 28.4f, 100.0f, 4.0f, 2.5f, 80.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.FLETCHING, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARROW_SHAFT)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.HEAVY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.FLETCHING, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD, SmitheryPartTypes.ARROW_SHAFT)
                .bow(0.85f, 1.5f, 4.0f)
                .build());

        RED_MATTER = id("red_matter");
        SmitheryAPI.registerMaterial(RED_MATTER, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(12.4f)
                        .attackDamage(18.4f)
                        .durabilityPerIngot(2400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFFEEC57)
                        .binderMultiplier(1.56f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.GIANTSLAYER), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.MATTERTRAIT11), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ARIDICULOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SHIELDING_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_SUPERDENSE_ARMOR), armorPieces())
                .armor(500.0f, 35.4f, 200.0f, 8.0f, 3.5f, 100.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.DENSE), SmitheryPartTypes.BOW_LIMB)
                .addUniversalModifier(SoaSmitheryModifiers.MATTERTRAIT1)
                .bow(0.75f, 2.0f, 10.0f)
                .build());

    }

    private static void registerExtraUtilities() {
        XU_DEMONIC_METAL = id("xu_demonic_metal");
        SmitheryAPI.registerMaterial(XU_DEMONIC_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(8.6f)
                        .attackDamage(6.2f)
                        .durabilityPerIngot(1000)
                        .meltingTemp(2000.0f)
                        .partColor(0xFFD00700)
                        .binderMultiplier(1.2f)
                , 4)
                .armor(16.0f, 19.6f, 28.0f, 1.0f, 0.6f, 36.0f)
                .addHeadModifier(SoaSmitheryModifiers.XU_WHISPERING)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.XU_WHISPERING), armorPieces())
                .build());

        XU_ENCHANTED_METAL = id("xu_enchanted_metal");
        SmitheryAPI.registerMaterial(XU_ENCHANTED_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(2)
                        .miningSpeed(6.0f)
                        .attackDamage(4.0f)
                        .durabilityPerIngot(400)
                        .meltingTemp(1000.0f)
                        .partColor(0xFF4AC8B8)
                        .binderMultiplier(1.0f)
                , 2)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.ESTABLISHED), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.AMBITIOUS_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KNOWLEDGEFUL_ARMOR), armorPieces())
                .armor(120.0f, 14.0f, 8.96f, 1.8f, 1.5f, 8.96f)
                .build());

        XU_EVIL_METAL = id("xu_evil_metal");
        SmitheryAPI.registerMaterial(XU_EVIL_METAL, binderSlots(MaterialStats.builder()
                        .harvestLevel(4)
                        .miningSpeed(11.6f)
                        .attackDamage(9.2f)
                        .durabilityPerIngot(800)
                        .meltingTemp(2000.0f)
                        .partColor(0xFF000000)
                        .binderMultiplier(1.3f)
                , 4)
                .armor(12.0f, 24.4f, 32.0f, 2.0f, 1.5f, 38.0f)
                .addHeadModifier(SoaSmitheryModifiers.XU_WITHERING)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.XU_WITHERING), armorPieces())
                .build());

        XU_MAGICAL_WOOD = id("xu_magical_wood");
        SmitheryAPI.registerMaterial(XU_MAGICAL_WOOD, binderSlots(MaterialStats.builder()
                        .harvestLevel(1)
                        .miningSpeed(4.0f)
                        .attackDamage(3.0f)
                        .durabilityPerIngot(300)
                        .meltingTemp(600.0f)
                        .partColor(0xFF2E2414)
                        .binderMultiplier(1.0f)
                , 1)
                .armor(70.0f, 10.0f, 1.68f, 1.2f, 1.0f, 1.68f)
                .addUniversalModifier(SoaSmitheryModifiers.MAGICAL_MODIFIER)
                .addUniversalModifier(SoaSmitheryModifiers.BRITTLE)
                .build());

        XU_WITHERING = id("xu_withering");
        SmitheryAPI.registerMaterial(XU_WITHERING, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3F3A42)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(55.0f, 22.6f, 28.0f, 1.0f, 1.25f, 32.0f)
                .build());

    }

    private static void registerBotania() {
        GAIA = id("gaia");
        SmitheryAPI.registerMaterial(GAIA, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(8.5f)
                        .attackDamage(10.8f)
                        .durabilityPerIngot(1600)
                        .meltingTemp(2500.0f)
                        .partColor(0xFFE9E9E9)
                        .binderMultiplier(1.2f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GAIA_WRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MORTAL_WOUNDS), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DIVINE_GRACE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .armor(300.0f, 28.6f, 50.0f, 2.8f, 2.0f, 40.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7143f, 1.2f, 10.0f)
                .build());

        GAIASTEEL = id("gaiasteel");
        SmitheryAPI.registerMaterial(GAIASTEEL, binderSlots(MaterialStats.builder()
                        .harvestLevel(5)
                        .miningSpeed(9.5f)
                        .attackDamage(12.2f)
                        .durabilityPerIngot(2400)
                        .meltingTemp(2500.0f)
                        .partColor(0xFF3C1517)
                        .binderMultiplier(1.5f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DEADLY_PRECISION), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GAIA_WRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DIVINE_GRACE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.SECONDLIFETRAIT_ARMOR), armorPieces())
                .armor(500.0f, 33.2f, 100.0f, 3.4f, 3.0f, 70.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.4348f, 1.2f, 18.0f)
                .build());

        ORICHALCOS = id("orichalcos");
        SmitheryAPI.registerMaterial(ORICHALCOS, binderSlots(MaterialStats.builder()
                        .harvestLevel(7)
                        .miningSpeed(15.6f)
                        .attackDamage(23.2f)
                        .durabilityPerIngot(2200)
                        .meltingTemp(4000.0f)
                        .partColor(0xFF760084)
                        .binderMultiplier(2.8f)
                , 5)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.EXECUTIONERTRAIT), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_GAIA_WRATH), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_DIVINE_GRACE_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TERRAFIRMA2), armorPieces())
                .armor(320.0f, 52.0f, 100.0f, 10.0f, 5.0f, 80.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TCONEVO_MANA_INFUSED),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .bow(0.7143f, 1.3f, 13.0f)
                .build());

    }

    private static void registerMekores() {
        ORICHALCUM = id("orichalcum");
        SmitheryAPI.registerMaterial(ORICHALCUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF3EA88F)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(8.0f, 28.035f, 3.92f, 23.2941f, 0.0f, 3.92f)
                .build());

    }

    private static void registerTofucraft() {
        TOFUDIAMOND = id("tofudiamond");
        SmitheryAPI.registerMaterial(TOFUDIAMOND, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFE4F6FF)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.HOLY), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KUNGFU_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.PURIFYINGTRAIT_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), armorPieces())
                .armor(140.0f, 21.4f, 30.0f, 2.4f, 1.0f, 20.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

        TOFUMETAL = id("tofumetal");
        SmitheryAPI.registerMaterial(TOFUMETAL, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFFEBEFF1)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), allToolTypes())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.KUNGFU_ARMOR), armorPieces())
                .addModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY_ARMOR), armorPieces())
                .armor(70.0f, 17.2f, 20.0f, 1.2f, 0.5f, 10.0f)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.LIGHTWEIGHT),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .addPartModifier(ModifierEffect.of(SoaSmitheryModifiers.TASTY),
                        SmitheryPartTypes.BOW_LIMB, SmitheryPartTypes.GUARD, SmitheryPartTypes.BINDER, SmitheryPartTypes.LARGE_PLATE, SmitheryPartTypes.HANDLE, SmitheryPartTypes.SWORD_BLADE, SmitheryPartTypes.PICK_HEAD, SmitheryPartTypes.AXE_HEAD, SmitheryPartTypes.SHOVEL_HEAD, SmitheryPartTypes.HOE_HEAD, SmitheryPartTypes.SPEAR_HEAD, SmitheryPartTypes.LARGE_BLADE, SmitheryPartTypes.HAMMER_HEAD, SmitheryPartTypes.KAMA_HEAD)
                .build());

    }

    private static void registerDefiledLands() {
        UMBRIUM = id("umbrium");
        SmitheryAPI.registerMaterial(UMBRIUM, MaterialStats.builder()
                        .harvestLevel(0)
                        .miningSpeed(0.0f)
                        .attackDamage(0.0f)
                        .durabilityPerIngot(0)
                        .meltingTemp(0.0f)
                        .partColor(0xFF5D0E73)
                        .binderMultiplier(1.0f)
                        .castOnly(true)
                .armor(125.0f, 14.3f, 6.72f, 1.2f, 1.5f, 6.72f)
                .build());

    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("soa_additions", path);
    }

    private SoaSmitheryMaterials() {}
}
