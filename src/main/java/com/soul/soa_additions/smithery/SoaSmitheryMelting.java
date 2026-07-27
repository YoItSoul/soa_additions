package com.soul.soa_additions.smithery;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.forge.ForgeMobDrops;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public final class SoaSmitheryMelting {

    private static final int NUGGET_MB = 16;
    private static final int INGOT_MB  = 144;
    private static final int ORE_MB    = 288;
    private static final int BLOCK_MB  = 1296;

    public static void register() {
        registerEntityMelting();
        registerMeltingRecipes();
    }

    private static void registerEntityMelting() {
        // GC tconstruct.zs entity melting, complete set:
        //   cow->milk, enderman->ender, snowman->water, iron golem->iron, zombie piglin->gold
        ForgeMobDrops.register(Cow.class,
                ResourceLocation.fromNamespaceAndPath("soa_additions", "milk"));
        ForgeMobDrops.register(EnderMan.class,
                ResourceLocation.fromNamespaceAndPath("soa_additions", "ender"));
        ForgeMobDrops.register(SnowGolem.class,
                ResourceLocation.fromNamespaceAndPath("soa_additions", "water"));
        ForgeMobDrops.register(IronGolem.class,
                ResourceLocation.fromNamespaceAndPath("smithery", "iron"));
        ForgeMobDrops.register(ZombifiedPiglin.class,
                ResourceLocation.fromNamespaceAndPath("smithery", "gold"));
    }

    private static void registerMeltingRecipes() {

        // TConstruct "Coagulated Blood" (edible:3): melts back to smithery mob-blood at
        // the ingot-cast volume so cast->melt is lossless. Cast side registered in
        // SmitheryIntegration (smithery:blood x ingot cast -> coagulated_blood).
        recipe("soa_additions:coagulated_blood", "smithery:blood", INGOT_MB);

        // ================================================================
        // SOA-registered custom metals (ingots/blocks/ores in soa_additions)
        // ================================================================

        // Adaminite
        recipe("soa_additions:adaminite_ingot",         "soa_additions:adaminite", INGOT_MB);
        recipe("jaopca:storage_blocks.adaminite",       "soa_additions:adaminite", BLOCK_MB);

        // Aeonsteel
        recipe("soa_additions:aeonsteel_ingot",         "soa_additions:aeonsteel", INGOT_MB);
        recipe("soa_additions:aeonsteel_block",         "soa_additions:aeonsteel", BLOCK_MB);

        // Aeroite
        recipe("soa_additions:aeroite_ingot",           "soa_additions:aeroite", INGOT_MB);
        recipe("soa_additions:aeroite_ore",             "soa_additions:aeroite", ORE_MB);
        recipe("jaopca:storage_blocks.aeroite",         "soa_additions:aeroite", BLOCK_MB);
        recipe("jaopca:nuggets.aeroite",                "soa_additions:aeroite", NUGGET_MB);
        recipe("jaopca:dusts.aeroite",                  "soa_additions:aeroite", INGOT_MB);

        // Alumite
        recipe("soa_additions:alumite_ingot",           "soa_additions:alumite", INGOT_MB);
        recipe("jaopca:storage_blocks.alumite",         "soa_additions:alumite", BLOCK_MB);

        // Aqualite
        recipe("soa_additions:aqualite_ingot",          "soa_additions:aqualite", INGOT_MB);
        recipe("soa_additions:aqualite_block",          "soa_additions:aqualite", BLOCK_MB);
        recipe("soa_additions:aqualite_ore",            "soa_additions:aqualite", ORE_MB);
        recipe("jaopca:nuggets.aqualite",               "soa_additions:aqualite", NUGGET_MB);
        recipe("jaopca:dusts.aqualite",                 "soa_additions:aqualite", INGOT_MB);

        // Ardite
        recipe("soa_additions:ardite_ingot",            "soa_additions:ardite", INGOT_MB);
        recipe("soa_additions:ardite_block",            "soa_additions:ardite", BLOCK_MB);
        recipe("soa_additions:ardite_nugget",           "soa_additions:ardite", NUGGET_MB);
        recipe("soa_additions:ardite_ore",              "soa_additions:ardite", ORE_MB);
        recipe("jaopca:dusts.ardite",                   "soa_additions:ardite", INGOT_MB);

        // Asgardium
        recipe("soa_additions:asgardium_ingot",         "soa_additions:asgardium", INGOT_MB);
        recipe("soa_additions:asgardium_ore",           "soa_additions:asgardium", ORE_MB);
        recipe("jaopca:storage_blocks.asgardium",       "soa_additions:asgardium", BLOCK_MB);
        recipe("jaopca:nuggets.asgardium",              "soa_additions:asgardium", NUGGET_MB);
        recipe("jaopca:dusts.asgardium",                "soa_additions:asgardium", INGOT_MB);

        // Astral Metal
        recipe("soa_additions:astral_metal_ingot",      "soa_additions:astral_metal", INGOT_MB);
        recipe("soa_additions:astral_metal_block",      "soa_additions:astral_metal", BLOCK_MB);

        // Bound Metal
        recipe("tconevo:bound_metal_ingot",       "soa_additions:bound_metal", INGOT_MB);
        recipe("tconevo:bound_metal_block",       "soa_additions:bound_metal", BLOCK_MB);
        recipe("tconevo:bound_metal_nugget",      "soa_additions:bound_metal", NUGGET_MB);
        recipe("tconevo:bound_metal_dust",        "soa_additions:bound_metal", INGOT_MB);

        // Chaotic Metal (naming mismatch: material=chaotic_metal, items=chaotic_*)
        recipe("tconevo:chaotic_ingot",           "soa_additions:chaotic_metal", INGOT_MB);
        recipe("tconevo:chaotic_block",           "soa_additions:chaotic_metal", BLOCK_MB);
        recipe("tconevo:chaotic_nugget",          "soa_additions:chaotic_metal", NUGGET_MB);
        recipe("tconevo:chaotic_dust",            "soa_additions:chaotic_metal", INGOT_MB);

        // Chromasteel
        recipe("soa_additions:chromasteel_ingot",       "soa_additions:chromasteel", INGOT_MB);
        recipe("soa_additions:chromasteel_block",       "soa_additions:chromasteel", BLOCK_MB);

        // Chromium
        recipe("soa_additions:chromium_ingot",          "soa_additions:chromium", INGOT_MB);
        recipe("soa_additions:chromium_ore",            "soa_additions:chromium", ORE_MB);
        recipe("jaopca:storage_blocks.chromium",        "soa_additions:chromium", BLOCK_MB);
        recipe("jaopca:nuggets.chromium",               "soa_additions:chromium", NUGGET_MB);
        recipe("jaopca:dusts.chromium",                 "soa_additions:chromium", INGOT_MB);

        // Cosmilite
        recipe("soa_additions:cosmilite_ingot",         "soa_additions:cosmilite", INGOT_MB);
        recipe("soa_additions:cosmilite_block",         "soa_additions:cosmilite", BLOCK_MB);

        // Crimsonite
        recipe("soa_additions:crimsonite_ingot",        "soa_additions:crimsonite", INGOT_MB);
        recipe("soa_additions:crimsonite_block",        "soa_additions:crimsonite", BLOCK_MB);

        // Cryonium
        recipe("soa_additions:cryonium_ingot",          "soa_additions:cryonium", INGOT_MB);
        recipe("soa_additions:cryonium_block",          "soa_additions:cryonium", BLOCK_MB);
        recipe("soa_additions:cryonium_ore",            "soa_additions:cryonium", ORE_MB);
        recipe("jaopca:nuggets.cryonium",               "soa_additions:cryonium", NUGGET_MB);
        recipe("jaopca:dusts.cryonium",                 "soa_additions:cryonium", INGOT_MB);

        // Cytosinite
        recipe("soa_additions:cytosinite_ingot",        "soa_additions:cytosinite", INGOT_MB);
        recipe("soa_additions:cytosinite_block",        "soa_additions:cytosinite", BLOCK_MB);
        recipe("soa_additions:cytosinite_ore",          "soa_additions:cytosinite", ORE_MB);
        recipe("jaopca:dusts.cytosinite",               "soa_additions:cytosinite", INGOT_MB);

        // Demonic Metal
        recipe("soa_additions:demonic_metal_ingot",     "soa_additions:demonic_metal", INGOT_MB);
        recipe("soa_additions:demonic_metal_block",     "soa_additions:demonic_metal", BLOCK_MB);

        // Draconic Metal
        recipe("tconevo:draconic_metal_ingot",    "soa_additions:draconic_metal", INGOT_MB);
        recipe("tconevo:draconic_metal_block",    "soa_additions:draconic_metal", BLOCK_MB);
        recipe("tconevo:draconic_metal_nugget",   "soa_additions:draconic_metal", NUGGET_MB);
        recipe("tconevo:draconic_metal_dust",     "soa_additions:draconic_metal", INGOT_MB);

        // Dreadium
        recipe("soa_additions:dreadium_ingot",          "soa_additions:dreadium", INGOT_MB);
        recipe("jaopca:storage_blocks.dreadium",        "soa_additions:dreadium", BLOCK_MB);

        // Durasteel
        recipe("soa_additions:durasteel_ingot",         "soa_additions:durasteel", INGOT_MB);
        recipe("soa_additions:durasteel_block",         "soa_additions:durasteel", BLOCK_MB);

        // Electronium
        recipe("soa_additions:electronium_ingot",       "soa_additions:electronium", INGOT_MB);
        recipe("soa_additions:electronium_block",       "soa_additions:electronium", BLOCK_MB);

        // Energetic Metal
        recipe("tconevo:energetic_metal_ingot",   "soa_additions:energetic_metal", INGOT_MB);
        recipe("tconevo:energetic_metal_block",   "soa_additions:energetic_metal", BLOCK_MB);
        recipe("tconevo:energetic_metal_nugget",  "soa_additions:energetic_metal", NUGGET_MB);
        recipe("tconevo:energetic_metal_dust",    "soa_additions:energetic_metal", INGOT_MB);

        // Essence Metal
        recipe("tconevo:essence_metal_ingot",     "soa_additions:essence_metal", INGOT_MB);
        recipe("tconevo:essence_metal_block",     "soa_additions:essence_metal", BLOCK_MB);
        recipe("tconevo:essence_metal_nugget",    "soa_additions:essence_metal", NUGGET_MB);
        recipe("tconevo:essence_metal_dust",      "soa_additions:essence_metal", INGOT_MB);

        // Ethaxium
        recipe("soa_additions:ethaxium_ingot",          "soa_additions:ethaxium", INGOT_MB);
        recipe("jaopca:storage_blocks.ethaxium",        "soa_additions:ethaxium", BLOCK_MB);

        // Evil Metal
        recipe("soa_additions:evil_metal_ingot",        "soa_additions:evil_metal", INGOT_MB);
        recipe("soa_additions:evil_metal_block",        "soa_additions:evil_metal", BLOCK_MB);

        // Experience
        recipe("soa_additions:experience_ingot",        "soa_additions:experience", INGOT_MB);
        recipe("soa_additions:experience_block",        "soa_additions:experience", BLOCK_MB);
        recipe("soa_additions:experience_nugget",       "soa_additions:experience", NUGGET_MB);
        recipe("soa_additions:experience_ore",          "soa_additions:experience", ORE_MB);
        recipe("soa_additions:compressed_experience_block", "soa_additions:experience", BLOCK_MB * 9);
        recipe("actuallyadditions:item_solidified_experience", "soa_additions:experience", 160);
        recipe("jaopca:dusts.experience",               "soa_additions:experience", INGOT_MB);

        // Fluix Steel
        recipe("soa_additions:fluix_steel_ingot",       "soa_additions:fluix_steel", INGOT_MB);
        recipe("jaopca:storage_blocks.fluix_steel",     "soa_additions:fluix_steel", BLOCK_MB);

        // Fusion Matrix
        recipe("soa_additions:fusion_matrix_ingot",     "soa_additions:fusion_matrix", INGOT_MB);
        recipe("soa_additions:fusion_matrix_block",     "soa_additions:fusion_matrix", BLOCK_MB);

        // Gaiasteel
        recipe("soa_additions:gaiasteel_ingot",         "soa_additions:gaiasteel", INGOT_MB);
        recipe("jaopca:storage_blocks.gaiasteel",       "soa_additions:gaiasteel", BLOCK_MB);

        // Infernium
        recipe("soa_additions:infernium_ingot",         "soa_additions:infernium", INGOT_MB);
        recipe("soa_additions:infernium_block",         "soa_additions:infernium", BLOCK_MB);
        recipe("soa_additions:infernium_nugget",        "soa_additions:infernium", NUGGET_MB);

        // Iridium
        recipe("soa_additions:iridium_ingot",           "soa_additions:iridium", INGOT_MB);
        recipe("soa_additions:iridium_block",           "soa_additions:iridium", BLOCK_MB);
        recipe("soa_additions:iridium_nugget",          "soa_additions:iridium", NUGGET_MB);
        recipe("soa_additions:iridium_ore",             "soa_additions:iridium", ORE_MB);
        recipe("soa_additions:iridium_dust",            "soa_additions:iridium", INGOT_MB);

        // Manganese
        recipe("soa_additions:manganese_ingot",         "soa_additions:manganese", INGOT_MB);
        recipe("soa_additions:manganese_ore",           "soa_additions:manganese", ORE_MB);
        recipe("jaopca:storage_blocks.manganese",       "soa_additions:manganese", BLOCK_MB);
        recipe("jaopca:nuggets.manganese",              "soa_additions:manganese", NUGGET_MB);
        recipe("jaopca:dusts.manganese",                "soa_additions:manganese", INGOT_MB);

        // Manganese Steel
        recipe("soa_additions:manganese_steel_ingot",   "soa_additions:manganese_steel", INGOT_MB);
        recipe("soa_additions:manganese_steel_block",   "soa_additions:manganese_steel", BLOCK_MB);

        // Meteor (Nyx port)
        recipe("nyx:meteor_ingot",                      "soa_additions:meteor", INGOT_MB);
        recipe("nyx:meteor_block",                      "soa_additions:meteor", BLOCK_MB);
        recipe("nyx:meteor_dust",                       "soa_additions:meteor", INGOT_MB);

        // Mirion
        recipe("soa_additions:mirion_ingot",            "soa_additions:mirion", INGOT_MB);
        recipe("jaopca:storage_blocks.mirion",          "soa_additions:mirion", BLOCK_MB);

        // Mithminite
        recipe("soa_additions:mithminite_ingot",        "soa_additions:mithminite", INGOT_MB);
        recipe("jaopca:storage_blocks.mithminite",      "soa_additions:mithminite", BLOCK_MB);

        // Mithrillium
        recipe("soa_additions:mithrillium_ingot",       "soa_additions:mithrillium", INGOT_MB);
        recipe("jaopca:storage_blocks.mithrillium",     "soa_additions:mithrillium", BLOCK_MB);

        // Modularium
        recipe("soa_additions:modularium_ingot",        "soa_additions:modularium", INGOT_MB);
        recipe("soa_additions:modularium_block",        "soa_additions:modularium", BLOCK_MB);

        // Orichalcos
        recipe("soa_additions:orichalcos_ingot",        "soa_additions:orichalcos", INGOT_MB);
        recipe("jaopca:storage_blocks.orichalcos",      "soa_additions:orichalcos", BLOCK_MB);

        // Osgloglas
        recipe("soa_additions:osgloglas_ingot",         "soa_additions:osgloglas", INGOT_MB);
        recipe("jaopca:storage_blocks.osgloglas",       "soa_additions:osgloglas", BLOCK_MB);

        // Osmiridium
        recipe("soa_additions:osmiridium_ingot",        "soa_additions:osmiridium", INGOT_MB);
        recipe("jaopca:storage_blocks.osmiridium",      "soa_additions:osmiridium", BLOCK_MB);

        // Pink Metal
        recipe("tconevo:pink_metal_ingot",        "soa_additions:pink_metal", INGOT_MB);
        recipe("jaopca:storage_blocks.pink_metal",      "soa_additions:pink_metal", BLOCK_MB);

        // Platinum
        recipe("soa_additions:platinum_ingot",          "soa_additions:platinum", INGOT_MB);
        recipe("soa_additions:platinum_block",          "soa_additions:platinum", BLOCK_MB);
        recipe("soa_additions:platinum_nugget",         "soa_additions:platinum", NUGGET_MB);
        recipe("soa_additions:platinum_ore",            "soa_additions:platinum", ORE_MB);
        recipe("soa_additions:platinum_dust",           "soa_additions:platinum", INGOT_MB);

        // Primal Metal
        recipe("tconevo:primal_metal_ingot",      "soa_additions:primal_metal", INGOT_MB);
        recipe("tconevo:primal_metal_block",      "soa_additions:primal_metal", BLOCK_MB);
        recipe("tconevo:primal_metal_nugget",     "soa_additions:primal_metal", NUGGET_MB);
        recipe("tconevo:primal_metal_dust",       "soa_additions:primal_metal", INGOT_MB);

        // Protonium
        recipe("soa_additions:protonium_ingot",         "soa_additions:protonium", INGOT_MB);
        recipe("soa_additions:protonium_block",         "soa_additions:protonium", BLOCK_MB);

        // Ravaging
        recipe("soa_additions:ravaging_ingot",          "soa_additions:ravaging", INGOT_MB);
        recipe("jaopca:storage_blocks.ravaging",        "soa_additions:ravaging", BLOCK_MB);

        // Sentient Metal
        recipe("tconevo:sentient_metal_ingot",    "soa_additions:sentient_metal", INGOT_MB);
        recipe("tconevo:sentient_metal_block",    "soa_additions:sentient_metal", BLOCK_MB);
        recipe("tconevo:sentient_metal_nugget",   "soa_additions:sentient_metal", NUGGET_MB);
        recipe("tconevo:sentient_metal_dust",     "soa_additions:sentient_metal", INGOT_MB);

        // Shadowium
        recipe("soa_additions:shadowium_ingot",         "soa_additions:shadowium", INGOT_MB);
        recipe("soa_additions:shadowium_ore",           "soa_additions:shadowium", ORE_MB);
        recipe("jaopca:storage_blocks.shadowium",       "soa_additions:shadowium", BLOCK_MB);
        recipe("jaopca:nuggets.shadowium",              "soa_additions:shadowium", NUGGET_MB);
        recipe("jaopca:dusts.shadowium",                "soa_additions:shadowium", INGOT_MB);

        // Spectre
        recipe("soa_additions:spectre_ingot",           "soa_additions:spectre", INGOT_MB);
        recipe("jaopca:storage_blocks.spectre",         "soa_additions:spectre", BLOCK_MB);

        // Stainless Steel
        recipe("soa_additions:stainless_steel_ingot",   "soa_additions:stainless_steel", INGOT_MB);
        recipe("soa_additions:stainless_steel_block",   "soa_additions:stainless_steel", BLOCK_MB);

        // Starmetal
        recipe("soa_additions:starmetal_ingot",         "soa_additions:starmetal", INGOT_MB);
        recipe("jaopca:storage_blocks.starmetal",       "soa_additions:starmetal", BLOCK_MB);

        // Terra Alloy
        recipe("soa_additions:terra_alloy_ingot",       "soa_additions:terra_alloy", INGOT_MB);
        recipe("soa_additions:terra_alloy_block",       "soa_additions:terra_alloy", BLOCK_MB);

        // Thaumium
        recipe("soa_additions:thaumium_ingot",          "soa_additions:thaumium", INGOT_MB);
        recipe("soa_additions:thaumium_block",          "soa_additions:thaumium", BLOCK_MB);
        recipe("soa_additions:thaumium_nugget",         "soa_additions:thaumium", NUGGET_MB);

        // Titanium
        recipe("soa_additions:titanium_ingot",          "soa_additions:titanium", INGOT_MB);
        recipe("soa_additions:titanium_block",          "soa_additions:titanium", BLOCK_MB);
        recipe("soa_additions:titanium_nugget",         "soa_additions:titanium", NUGGET_MB);
        recipe("soa_additions:titanium_ore",            "soa_additions:titanium", ORE_MB);
        recipe("jaopca:dusts.titanium",                 "soa_additions:titanium", INGOT_MB);

        // Universal Metal
        recipe("tconevo:universal_metal_ingot",   "soa_additions:universal_metal", INGOT_MB);
        recipe("tconevo:universal_metal_block",   "soa_additions:universal_metal", BLOCK_MB);
        recipe("tconevo:universal_metal_nugget",  "soa_additions:universal_metal", NUGGET_MB);
        recipe("tconevo:universal_metal_dust",    "soa_additions:universal_metal", INGOT_MB);

        // URU
        recipe("soa_additions:uru_ingot",               "soa_additions:uru", INGOT_MB);
        recipe("soa_additions:uru_block",               "soa_additions:uru", BLOCK_MB);
        recipe("soa_additions:uru_ore",                 "soa_additions:uru", ORE_MB);
        recipe("taiga:uru_dust",                        "soa_additions:uru", INGOT_MB);
        recipe("taiga:uru_nugget",                      "soa_additions:uru", NUGGET_MB);
        recipe("jaopca:nuggets.uru",                    "soa_additions:uru", NUGGET_MB);
        recipe("jaopca:dusts.uru",                      "soa_additions:uru", INGOT_MB);

        // Valkyrie
        recipe("soa_additions:valkyrie_ingot",          "soa_additions:valkyrie", INGOT_MB);
        recipe("soa_additions:valkyrie_block",          "soa_additions:valkyrie", BLOCK_MB);
        recipe("soa_additions:valkyrie_nugget",         "soa_additions:valkyrie", NUGGET_MB);

        // Void Metal
        recipe("soa_additions:void_metal_ingot",        "soa_additions:void_metal", INGOT_MB);
        recipe("soa_additions:void_metal_block",        "soa_additions:void_metal", BLOCK_MB);
        recipe("soa_additions:void_metal_nugget",       "soa_additions:void_metal", NUGGET_MB);

        // Wyvern Metal (naming mismatch: material=wyvern_metal, items=wyvern_*)
        recipe("tconevo:wyvern_ingot",            "soa_additions:wyvern_metal", INGOT_MB);
        recipe("tconevo:wyvern_block",            "soa_additions:wyvern_metal", BLOCK_MB);
        recipe("tconevo:wyvern_nugget",           "soa_additions:wyvern_metal", NUGGET_MB);
        recipe("tconevo:wyvern_dust",             "soa_additions:wyvern_metal", INGOT_MB);

        // Yellorium
        recipe("soa_additions:yellorium_ingot",         "soa_additions:yellorium", INGOT_MB);
        recipe("bigreactors:yellorium_ingot",           "soa_additions:yellorium", INGOT_MB);
        recipe("bigreactors:yellorium_block",           "soa_additions:yellorium", BLOCK_MB);
        recipe("bigreactors:yellorium_nugget",          "soa_additions:yellorium", NUGGET_MB);
        recipe("bigreactors:yellorium_dust",            "soa_additions:yellorium", INGOT_MB);

        // Abyssal / Ether / Void (original soa_additions items)
        recipe("soa_additions:abyssal_ingot",           "soa_additions:abyssal", INGOT_MB);
        recipe("soa_additions:abyssal_ore_block",       "soa_additions:abyssal", ORE_MB);
        recipe("soa_additions:ether_ingot",             "soa_additions:ether", INGOT_MB);
        recipe("soa_additions:ether_ore_block",         "soa_additions:ether", ORE_MB);
        recipe("soa_additions:infernium_ore_block",     "soa_additions:infernium", ORE_MB);
        recipe("soa_additions:void_ingot",              "soa_additions:void", INGOT_MB);
        recipe("soa_additions:void_ore_block",          "soa_additions:void", ORE_MB);

        // Cobalt (from Valoria)
        recipe("valoria:cobalt_ingot",                  "soa_additions:cobalt", INGOT_MB);
        recipe("valoria:cobalt_block",                  "soa_additions:cobalt", BLOCK_MB);
        recipe("valoria:cobalt_nugget",                 "soa_additions:cobalt", NUGGET_MB);
        recipe("valoria:cobalt_ore",                    "soa_additions:cobalt", ORE_MB);
        recipe("valoria:deepslate_cobalt_ore",          "soa_additions:cobalt", ORE_MB);
        recipe("jaopca:storage_blocks.cobalt",          "soa_additions:cobalt", BLOCK_MB);

        // Mithril (from Embers)
        recipe("embers:mithril_ingot",                  "soa_additions:mithril", INGOT_MB);
        recipe("embers:mithril_block",                  "soa_additions:mithril", BLOCK_MB);
        recipe("embers:mithril_nugget",                 "soa_additions:mithril", NUGGET_MB);

        // Fluxed Electrum (from Redstone Arsenal)
        recipe("redstone_arsenal:flux_ingot",           "soa_additions:fluxed_electrum", INGOT_MB);
        recipe("redstone_arsenal:flux_nugget",          "soa_additions:fluxed_electrum", NUGGET_MB);
        recipe("redstone_arsenal:flux_metal_block",     "soa_additions:fluxed_electrum", BLOCK_MB);
        recipe("redstone_arsenal:flux_dust",            "soa_additions:fluxed_electrum", INGOT_MB);

        // Tanzanite (soa_additions gem)
        recipe("soa_additions:tanzanite",               "soa_additions:tanzanite", INGOT_MB);

        // Solarium (soa_additions item)
        recipe("soa_additions:solarium_star",           "soa_additions:solarium", INGOT_MB);

        // ================================================================
        // TAIGA materials (1.12 taiga → soa_additions port)
        // ================================================================
        taiga("abyssum",     true);
        taiga("adamant",     false);
        taiga("astrium",     false);
        taiga("aurorium",    true);
        taiga("basalt",      false);
        taiga("dilithium",   true);
        taiga("duranite",    true);
        taiga("dyonite",     false);
        taiga("eezo",        true);
        taiga("fractum",     false);
        taiga("ignitz",      false);
        taiga("imperomite",  false);
        taiga("iox",         false);
        taiga("jauxum",      true);
        taiga("karmesine",   true);
        taiga("lumix",       false);
        taiga("meteorite",   false);
        taiga("nihilite",    false);
        taiga("niob",        false);
        taiga("nucleum",     false);
        taiga("obsidiorite", false);
        taiga("osram",       true);
        taiga("ovium",       true);
        taiga("palladium",   true);
        taiga("prometheum",  true);
        taiga("proxii",      false);
        taiga("seismum",     false);
        taiga("solarium",    false);
        taiga("terrax",      false);
        taiga("tiberium",    true);
        taiga("triberium",   false);
        taiga("tritonite",   false);
        taiga("valyrium",    true);
        taiga("vibranium",   true);
        taiga("violium",     false);
        taiga("yrdeen",      false);
        recipe("taiga:dilithium_crystal",       "soa_additions:dilithium", INGOT_MB);
        recipe("taiga:tiberium_crystal",        "soa_additions:tiberium", INGOT_MB);

        // TAIGA storage blocks (1296mb, matching original forge:storage_blocks melts)
        String[] taigaStorage = {
                "abyssum", "adamant", "astrium", "aurorium", "dilithium", "duranite",
                "dyonite", "eezo", "fractum", "ignitz", "imperomite", "iox", "jauxum",
                "karmesine", "lumix", "nihilite", "niob", "nucleum", "osram", "ovium",
                "palladium", "prometheum", "proxii", "seismum", "solarium", "terrax",
                "tiberium", "triberium", "tritonite", "valyrium", "vibranium", "violium",
                "yrdeen"
        };
        for (String mat : taigaStorage) {
            recipe("taiga:" + mat + "_block", "soa_additions:" + mat, BLOCK_MB);
        }
        // World moon rocks + cobbles (cobble at ingot value, hulls at block value)
        recipe("taiga:basalt_block",            "soa_additions:basalt", BLOCK_MB);
        recipe("taiga:meteorite_block",         "soa_additions:meteorite", BLOCK_MB);
        recipe("taiga:meteorite_cobble",        "soa_additions:meteorite", INGOT_MB);
        recipe("taiga:obsidiorite_cobble",      "soa_additions:obsidiorite", INGOT_MB);
        recipe("soa_additions:obsidiorite",     "soa_additions:obsidiorite", ORE_MB);

        // TConstruct classic alloy items (re-meltable)
        for (String mat : new String[]{"manyullyn", "pigiron", "knightslime", "alubrass"}) {
            recipe("soa_additions:" + mat + "_ingot",  "soa_additions:" + mat, INGOT_MB);
            recipe("soa_additions:" + mat + "_nugget", "soa_additions:" + mat, NUGGET_MB);
        }

        // ================================================================
        // Malum metals
        // ================================================================
        recipe("malum:soul_stained_steel_ingot",    "soa_additions:soul_stained_steel", INGOT_MB);
        recipe("malum:soul_stained_steel_nugget",   "soa_additions:soul_stained_steel", NUGGET_MB);
        recipe("malum:block_of_soul_stained_steel", "soa_additions:soul_stained_steel", BLOCK_MB);
        recipe("malum:hallowed_gold_ingot",         "soa_additions:hallowed_gold", INGOT_MB);
        recipe("malum:hallowed_gold_nugget",        "soa_additions:hallowed_gold", NUGGET_MB);
        recipe("malum:block_of_hallowed_gold",      "soa_additions:hallowed_gold", BLOCK_MB);
        recipe("malum:cthonic_gold",                "soa_additions:cthonic_gold", INGOT_MB);
        recipe("malum:cthonic_gold_fragment",       "soa_additions:cthonic_gold", NUGGET_MB);
        recipe("malum:cthonic_gold_ore",            "soa_additions:cthonic_gold", ORE_MB);
        recipe("malum:block_of_cthonic_gold",       "soa_additions:cthonic_gold", BLOCK_MB);
        recipe("malum:malignant_pewter_ingot",      "soa_additions:malignant_pewter", INGOT_MB);
        recipe("malum:malignant_pewter_nugget",     "soa_additions:malignant_pewter", NUGGET_MB);
        recipe("malum:block_of_malignant_pewter",   "soa_additions:malignant_pewter", BLOCK_MB);
        recipe("malum:processed_soulstone",         "soa_additions:soulstone", INGOT_MB);
        recipe("malum:crushed_soulstone",           "soa_additions:soulstone", INGOT_MB);
        recipe("malum:raw_soulstone",               "soa_additions:soulstone", ORE_MB);
        recipe("malum:soulstone_ore",               "soa_additions:soulstone", ORE_MB);
        recipe("malum:deepslate_soulstone_ore",     "soa_additions:soulstone", ORE_MB);
        recipe("malum:block_of_soulstone",          "soa_additions:soulstone", BLOCK_MB);
        recipe("malum:blazing_quartz",              "soa_additions:blazing_quartz", INGOT_MB);
        recipe("malum:blazing_quartz_fragment",     "soa_additions:blazing_quartz", NUGGET_MB);
        recipe("malum:blazing_quartz_ore",          "soa_additions:blazing_quartz", ORE_MB);
        recipe("malum:block_of_blazing_quartz",     "soa_additions:blazing_quartz", BLOCK_MB);

        // ================================================================
        // Valoria metals & gems
        // ================================================================
        recipe("valoria:ancient_ingot",             "soa_additions:ancient", INGOT_MB);
        recipe("valoria:ancient_shard",             "soa_additions:ancient", NUGGET_MB);
        recipe("valoria:aquarius_ingot",            "soa_additions:aquarius", INGOT_MB);
        recipe("valoria:aquarius_block",            "soa_additions:aquarius", BLOCK_MB);
        recipe("valoria:black_gold_ingot",          "soa_additions:black_gold", INGOT_MB);
        recipe("valoria:black_gold_nugget",         "soa_additions:black_gold", NUGGET_MB);
        recipe("valoria:black_gold_block",          "soa_additions:black_gold", BLOCK_MB);
        recipe("valoria:crimtane_ingot",            "soa_additions:crimtane", INGOT_MB);
        recipe("valoria:crimtane_block",            "soa_additions:crimtane", BLOCK_MB);
        recipe("valoria:infernal_ingot",            "soa_additions:infernal", INGOT_MB);
        recipe("valoria:infernal_block",            "soa_additions:infernal", BLOCK_MB);
        recipe("valoria:nature_ingot",              "soa_additions:nature", INGOT_MB);
        recipe("valoria:nature_block",              "soa_additions:nature", BLOCK_MB);
        recipe("valoria:pearlium_ingot",            "soa_additions:pearlium", INGOT_MB);
        recipe("valoria:pearlium",                  "soa_additions:pearlium", ORE_MB);
        recipe("valoria:pearlium_ore",              "soa_additions:pearlium", ORE_MB);
        recipe("valoria:void_ingot",                "soa_additions:void", INGOT_MB);
        recipe("valoria:ruby_gem",                  "soa_additions:ruby", INGOT_MB);
        recipe("valoria:ruby_block",                "soa_additions:ruby", BLOCK_MB);
        recipe("valoria:sapphire_gem",              "soa_additions:sapphire", INGOT_MB);
        recipe("valoria:sapphire_ore",              "soa_additions:sapphire", ORE_MB);
        recipe("valoria:sapphire_block",            "soa_additions:sapphire", BLOCK_MB);

        // Misc soa_additions gem/crystal items
        recipe("soa_additions:scarlite",                "soa_additions:scarlite", INGOT_MB);
        recipe("soa_additions:hephaestite",             "soa_additions:hephaestite", INGOT_MB);
        recipe("soa_additions:remorseful_gem",          "soa_additions:remorseful", INGOT_MB);
        recipe("soa_additions:cincinnasite",            "soa_additions:cincinnasite", INGOT_MB);
        recipe("soa_additions:rime_crystal",            "soa_additions:rime", INGOT_MB);
        recipe("soa_additions:tofu_gem",                "soa_additions:tofu", INGOT_MB);
        recipe("soa_additions:strong_tofu_gem",         "soa_additions:strong_tofu", INGOT_MB);
        recipe("soa_additions:sakura_diamond",          "soa_additions:sakura", INGOT_MB);
        recipe("soa_additions:chocolate_bar",           "soa_additions:chocolate", INGOT_MB);
        recipe("soa_additions:flux_crystal",            "soa_additions:flux_crystal", INGOT_MB);
        recipe("soa_additions:aquamarine",              "soa_additions:aquamarine", INGOT_MB);
        recipe("soa_additions:black_quartz",            "soa_additions:black_quartz", INGOT_MB);
        recipe("soa_additions:restonia_crystal",        "soa_additions:restonia", INGOT_MB);
        recipe("soa_additions:palis_crystal",           "soa_additions:palis", INGOT_MB);
        recipe("soa_additions:diamantine_crystal",      "soa_additions:diamantine", INGOT_MB);
        recipe("soa_additions:void_crystal",            "soa_additions:void_crystal_mat", INGOT_MB);
        recipe("soa_additions:emeraldic_crystal",       "soa_additions:emeraldic", INGOT_MB);
        recipe("soa_additions:enori_crystal",           "soa_additions:enori", INGOT_MB);
        recipe("tconevo:essence_metal_ingot",     "soa_additions:essence_metal", INGOT_MB);

        // Ghostwood / Bloodwood / Darkwood / Fusewood logs
        recipe("soa_additions:ghostwood_log",           "soa_additions:ghostwood", INGOT_MB);
        recipe("soa_additions:bloodwood_log",           "soa_additions:bloodwood", INGOT_MB);
        recipe("soa_additions:darkwood_log",            "soa_additions:darkwood", INGOT_MB);
        recipe("soa_additions:fusewood_log",            "soa_additions:fusewood", INGOT_MB);

        // ================================================================
        // Cross-mod items — Thermal Foundation
        // ================================================================
        thermal("bronze");
        thermal("constantan");
        thermal("electrum");
        thermal("enderium");
        thermal("invar");
        thermal("lead");
        thermal("lumium");
        thermal("nickel");
        thermal("signalum");
        thermal("silver");
        thermal("tin");

        // ================================================================
        // Cross-mod items — EnderIO
        // ================================================================
        enderio("conductive_iron",  "conductive_alloy");
        enderio("dark_steel",       "dark_steel");
        enderio("end_steel",        "end_steel");
        enderio("energetic_alloy",  "energetic_alloy");
        enderio("pulsating_iron",   "pulsating_alloy");
        enderio("redstone_alloy",   "redstone_alloy");
        enderio("soularium",        "soularium");
        enderio("vibrant_alloy",    "vibrant_alloy");
        enderio("copper_alloy",     "copper_alloy");

        // ================================================================
        // Cross-mod items — Botania
        // ================================================================
        botania("manasteel",   "manasteel");
        botania("elementium",  "elementium");
        botania("terrasteel",  "terrasteel");
        recipe("botania:gaia_ingot",                    "soa_additions:gaia", INGOT_MB);

        // ================================================================
        // Cross-mod items — Mekanism (prefix format: ingot_name)
        // ================================================================
        mekanism("steel");
        mekanism("refined_glowstone");
        mekanism("refined_obsidian");
        recipe("bno:osmium_ingot",                      "soa_additions:osmium", INGOT_MB);
        recipe("bno:osmium_block",                      "soa_additions:osmium", BLOCK_MB);
        recipe("bno:osmium_nugget",                     "soa_additions:osmium", NUGGET_MB);
        recipe("mekanism:dust_osmium",                  "soa_additions:osmium", INGOT_MB);

        // ================================================================
        // Cross-mod items — Twilight Forest
        // ================================================================
        recipe("twilightforest:fiery_ingot",            "soa_additions:fiery", INGOT_MB);
        recipe("twilightforest:fiery_block",            "soa_additions:fiery", BLOCK_MB);
        recipe("twilightforest:knightmetal_ingot",      "soa_additions:knightmetal", INGOT_MB);
        recipe("twilightforest:knightmetal_block",      "soa_additions:knightmetal", BLOCK_MB);
        recipe("twilightforest:steeleaf_ingot",         "soa_additions:steeleaf", INGOT_MB);
        recipe("twilightforest:steeleaf_block",         "soa_additions:steeleaf", BLOCK_MB);

        // ================================================================
        // Cross-mod items — Draconic Evolution
        // ================================================================
        recipe("draconicevolution:draconium_ingot",     "soa_additions:draconium", INGOT_MB);
        recipe("draconicevolution:draconium_block",     "soa_additions:draconium", BLOCK_MB);
        recipe("draconicevolution:draconium_nugget",    "soa_additions:draconium", NUGGET_MB);
        recipe("draconicevolution:draconium_dust",      "soa_additions:draconium", INGOT_MB);
        recipe("draconicevolution:awakened_draconium_ingot",  "soa_additions:awakened_plustic", INGOT_MB);
        recipe("draconicevolution:awakened_draconium_block",  "soa_additions:awakened_plustic", BLOCK_MB);
        recipe("draconicevolution:awakened_draconium_nugget", "soa_additions:awakened_plustic", NUGGET_MB);

        // ================================================================
        // Cross-mod items — Mystical Agriculture / Additions
        // ================================================================
        // Materials use the ma.* prefix; 1.20 MA tier names map onto the 1.12
        // materials GC used: tertium -> intermedium, imperium -> superium.
        recipe("mysticalagriculture:inferium_essence",   "soa_additions:ma.inferium", INGOT_MB);
        recipe("mysticalagriculture:inferium_ingot",     "soa_additions:ma.inferium", INGOT_MB);
        recipe("mysticalagriculture:inferium_block",     "soa_additions:ma.inferium", BLOCK_MB);
        recipe("mysticalagriculture:prudentium_essence", "soa_additions:ma.prudentium", INGOT_MB);
        recipe("mysticalagriculture:prudentium_block",   "soa_additions:ma.prudentium", BLOCK_MB);
        recipe("mysticalagriculture:tertium_essence",    "soa_additions:ma.intermedium", INGOT_MB);
        recipe("mysticalagriculture:tertium_block",      "soa_additions:ma.intermedium", BLOCK_MB);
        recipe("mysticalagriculture:imperium_essence",   "soa_additions:ma.superium", INGOT_MB);
        recipe("mysticalagriculture:imperium_block",     "soa_additions:ma.superium", BLOCK_MB);
        recipe("mysticalagriculture:supremium_essence",  "soa_additions:ma.supremium", INGOT_MB);
        recipe("mysticalagriculture:supremium_block",    "soa_additions:ma.supremium", BLOCK_MB);
        recipe("mysticalagriculture:soulium_ingot",      "soa_additions:ma.soulium", INGOT_MB);
        recipe("mysticalagriculture:soulium_dust",       "soa_additions:ma.soulium", INGOT_MB);
        recipe("mysticalagriculture:prosperity_shard",   "soa_additions:ma.prosperity", INGOT_MB);
        recipe("mysticalagriculture:prosperity_block",   "soa_additions:ma.prosperity", BLOCK_MB);
        recipe("mysticalagradditions:insanium_essence",  "soa_additions:insanium", INGOT_MB);
        recipe("mysticalagradditions:insanium_ingot",    "soa_additions:insanium", INGOT_MB);
        recipe("mysticalagradditions:insanium_block",    "soa_additions:insanium", BLOCK_MB);
        recipe("mysticalagradditions:insanium_nugget",   "soa_additions:insanium", NUGGET_MB);

        // ================================================================
        // Cross-mod items — Aether
        // ================================================================
        recipe("aether:zanite_gemstone",                "soa_additions:zanite", INGOT_MB);
        recipe("aether:zanite_block",                   "soa_additions:zanite", BLOCK_MB);
        recipe("aether:gravitite_plate",                "soa_additions:gravitite", INGOT_MB);
        recipe("aether:enchanted_gravitite",            "soa_additions:gravitite", BLOCK_MB);

        // ================================================================
        // Cross-mod items — BNO (Aluminum)
        // ================================================================
        recipe("bno:aluminum_ingot",                    "soa_additions:aluminium", INGOT_MB);
        recipe("bno:aluminum_block",                    "soa_additions:aluminium", BLOCK_MB);
        recipe("bno:aluminum_nugget",                   "soa_additions:aluminium", NUGGET_MB);

        // ================================================================
        // Cross-mod items — ProjectE
        // ================================================================
        recipe("projecte:dark_matter",                  "soa_additions:dark_matter", INGOT_MB);
        recipe("projecte:dark_matter_block",            "soa_additions:dark_matter", BLOCK_MB);
        recipe("projecte:red_matter",                   "soa_additions:red_matter", INGOT_MB);
        recipe("projecte:red_matter_block",             "soa_additions:red_matter", BLOCK_MB);

        // ================================================================
        // Cross-mod items — Avaritia
        // ================================================================
        recipe("avaritia:crystal_matrix_ingot",         "soa_additions:crystal_matrix", INGOT_MB);
        recipe("avaritia:neutron_ingot",                "soa_additions:neutronium", INGOT_MB);
        recipe("avaritia:neutron_block",                "soa_additions:neutronium", BLOCK_MB);
        recipe("avaritia:neutron_nugget",               "soa_additions:neutronium", NUGGET_MB);
        recipe("avaritia:infinity_ingot",               "soa_additions:infinity", INGOT_MB);
        recipe("avaritia:infinity_block",               "soa_additions:infinity", BLOCK_MB);
        recipe("avaritia:infinity_nugget",              "soa_additions:infinity", NUGGET_MB);

        // ================================================================
        // Cross-mod items — AllTheModium
        // ================================================================
        recipe("allthemodium:allthemodium_ingot",       "soa_additions:allthemodium", INGOT_MB);
        recipe("allthemodium:allthemodium_block",       "soa_additions:allthemodium", BLOCK_MB);
        recipe("allthemodium:allthemodium_nugget",      "soa_additions:allthemodium", NUGGET_MB);
        recipe("allthemodium:vibranium_ingot",          "soa_additions:vibranium", INGOT_MB);
        recipe("allthemodium:vibranium_block",          "soa_additions:vibranium", BLOCK_MB);
        recipe("allthemodium:vibranium_nugget",         "soa_additions:vibranium", NUGGET_MB);
        recipe("allthemodium:unobtainium_ingot",        "soa_additions:unobtainium", INGOT_MB);
        recipe("allthemodium:unobtainium_block",        "soa_additions:unobtainium", BLOCK_MB);
        recipe("allthemodium:unobtainium_nugget",       "soa_additions:unobtainium", NUGGET_MB);

        // ================================================================
        // Cross-mod items — Big Reactors
        // ================================================================
        recipe("bigreactors:cyanite_ingot",             "soa_additions:cyanite", INGOT_MB);
        recipe("bigreactors:cyanite_block",             "soa_additions:cyanite", BLOCK_MB);
        recipe("bigreactors:cyanite_dust",              "soa_additions:cyanite", INGOT_MB);

        // ================================================================
        // Cross-mod items — Misc
        // ================================================================
        // smithery:gold is the fluid vanilla gold ingots melt into — coins must match
        // or coin-gold can't alloy with ingot-gold.
        recipe("treasure2:gold_coin",                   "smithery:gold", ORE_MB);
        recipe("treasure2:silver_coin",                 "soa_additions:silver", ORE_MB);

        // ================================================================
        // Base alloy fluids (TC 1.12 smeltery parity — feed the GC alloys)
        // ================================================================
        recipe("minecraft:obsidian",                    "soa_additions:obsidian", 288);
        recipe("minecraft:clay_ball",                   "soa_additions:clay", INGOT_MB);
        recipe("minecraft:clay",                        "soa_additions:clay", INGOT_MB * 4);
        recipe("minecraft:glowstone_dust",              "soa_additions:glowstone", 250);
        recipe("minecraft:glowstone",                   "soa_additions:glowstone", 1000);
        recipe("minecraft:ender_pearl",                 "soa_additions:ender", 250);
        recipe("minecraft:rotten_flesh",                "smithery:blood", 40);
        recipe("minecraft:slime_ball",                  "soa_additions:slime", 125);
        recipe("minecraft:slime_block",                 "soa_additions:slime", 1125);
        recipe("minecraft:magma_block",                 "soa_additions:lava", 250);
    }

    private static void thermal(String name) {
        recipe("thermal:" + name + "_ingot",  "soa_additions:" + name, INGOT_MB);
        recipe("thermal:" + name + "_block",  "soa_additions:" + name, BLOCK_MB);
        recipe("thermal:" + name + "_nugget", "soa_additions:" + name, NUGGET_MB);
        recipe("thermal:" + name + "_dust",   "soa_additions:" + name, INGOT_MB);
    }

    private static void enderio(String matName, String eioName) {
        recipe("enderio:" + eioName + "_ingot",  "soa_additions:" + matName, INGOT_MB);
        recipe("enderio:" + eioName + "_block",  "soa_additions:" + matName, BLOCK_MB);
        recipe("enderio:" + eioName + "_nugget", "soa_additions:" + matName, NUGGET_MB);
    }

    private static void botania(String matName, String botName) {
        recipe("botania:" + botName + "_ingot",  "soa_additions:" + matName, INGOT_MB);
        recipe("botania:" + botName + "_block",  "soa_additions:" + matName, BLOCK_MB);
        recipe("botania:" + botName + "_nugget", "soa_additions:" + matName, NUGGET_MB);
    }

    private static void mekanism(String name) {
        recipe("mekanism:ingot_" + name,  "soa_additions:" + name, INGOT_MB);
        recipe("mekanism:block_" + name,  "soa_additions:" + name, BLOCK_MB);
        recipe("mekanism:nugget_" + name, "soa_additions:" + name, NUGGET_MB);
        recipe("mekanism:dust_" + name,   "soa_additions:" + name, INGOT_MB);
    }

    private static void taiga(String name, boolean hasOre) {
        String mat = "soa_additions:" + name;
        recipe("taiga:" + name + "_ingot",  mat, INGOT_MB);
        recipe("taiga:" + name + "_nugget", mat, NUGGET_MB);
        recipe("taiga:" + name + "_dust",   mat, INGOT_MB);
        if (hasOre) {
            recipe("taiga:" + name + "_ore", mat, ORE_MB);
        }
    }

    private static void recipe(String input, String material, int mb) {
        SmitheryAPI.registerMeltingRecipe(input, material, mb);
    }

    private SoaSmitheryMelting() {}
}
