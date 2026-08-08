package com.soul.soa_additions.smithery;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.cast.CastResults;
import com.soul.smithery.api.cast.CastTemplates;
import com.soul.smithery.api.material.Material;
import com.soul.smithery.api.part.PartType;
import com.soul.smithery.registry.SmitheryFluids;
import com.soul.smithery.registry.SmitheryItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public final class SmitheryIntegration {

    private static final Logger LOG = LogManager.getLogger("SOA-Smithery");

    public static final DeferredRegister<Item> PART_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "soa_additions");

    private static final ResourceLocation INGOT_PT =
            ResourceLocation.fromNamespaceAndPath("smithery", "ingot");
    private static final ResourceLocation NUGGET_PT =
            ResourceLocation.fromNamespaceAndPath("smithery", "nugget");

    /**
     * Enables DE module grids on gear composed from the draconic-tier TConEvo/PlusTiC
     * materials. Tier indices follow BrandonsCore TechLevel: 1=WYVERN, 2=DRACONIC, 3=CHAOTIC.
     */
    private static void registerDraconicTiers() {
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("draconium"), 0);
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("wyvern_metal"), 1);
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("wyvern_plustic"), 1);
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("draconic_metal"), 2);
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("chaotic_metal"), 3);
        com.soul.smithery.compat.draconic.DraconicCompat.registerMaterialTier(soaId("chaotic_plustic"), 3);
    }

    private static ResourceLocation soaId(String path) {
        return ResourceLocation.fromNamespaceAndPath("soa_additions", path);
    }

    /**
     * Registers every material's polishing stone as a source of Constructs Armory's "Polished".
     *
     * <p>1.12 generated one ModPolished per armour material and applied it with that material's
     * Polishing Kit; Smithery's per-material polishing stones are the same object, so each one
     * registers its own source. Damaged armour is repaired by the stone instead — the anvil handler
     * defers to RepairHandler on gear that has taken damage.</p>
     *
     * <p>Must run after item registration.</p>
     */
    public static void registerPolishingKitSources() {
        int n = 0;
        for (Material mat : SmitheryAPI.MATERIALS.all()) {
            var stone = com.soul.smithery.registry.SmitheryItems.findPart(
                    mat.id(), com.soul.smithery.content.SmitheryPartTypes.POLISHING_STONE.id());
            if (stone == null) continue;
            com.soul.smithery.api.modifier.ModifierSources.register(stone,
                    com.soul.smithery.api.modifier.ModifierEffect.of(
                            SoaSmitheryModifiers.POLISHED_ARMOR, java.util.Map.of("level", 1)));
            n++;
        }
        LOG_POLISH.info("Registered {} polishing stones as Polished sources", n);
    }

    private static final org.apache.logging.log4j.Logger LOG_POLISH =
            org.apache.logging.log4j.LogManager.getLogger("SOA-SmitheryPolish");

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) ->
                e.enqueueWork(SmitheryIntegration::registerPolishingKitSources));
        SoaSmitheryModifiers.register();
        SoaSmitheryMaterials.register();
        SoaSmitheryAlloys.register();
        SoaSmitheryFuels.register();
        SoaSmitheryMelting.register();
        registerDraconicTiers();

        Set<String> existingFluidPaths = new HashSet<>();
        for (ResourceLocation id : SmitheryFluids.entries().keySet()) {
            existingFluidPaths.add(id.getPath());
        }

        int fluidCount = 0;
        int partCount = 0;
        int remeltCount = 0;
        int castResultCount = 0;
        int matCount = 0;

        for (Material mat : SmitheryAPI.MATERIALS.all()) {
            if (!"soa_additions".equals(mat.id().getNamespace())) continue;
            matCount++;

            String path = mat.id().getPath();

            if (mat.stats().meltingTemp() > 0f
                    && !existingFluidPaths.contains(path)) {
                SmitheryFluids.registerOne(mat);
                fluidCount++;
            }

            if (mat.stats().meltingTemp() > 0f) {
                ResourceLocation matId = mat.id();
                ResourceLocation ingotItemId =
                        ResourceLocation.fromNamespaceAndPath("soa_additions", path + "_ingot");
                ResourceLocation nuggetItemId =
                        ResourceLocation.fromNamespaceAndPath("soa_additions", path + "_nugget");
                ResourceLocation bareItemId =
                        ResourceLocation.fromNamespaceAndPath("soa_additions", path);

                CastResults.register(matId, INGOT_PT, () -> {
                    Item item = ForgeRegistries.ITEMS.getValue(ingotItemId);
                    if (item != Items.AIR) return item;
                    item = ForgeRegistries.ITEMS.getValue(bareItemId);
                    return item == Items.AIR ? null : item;
                });
                CastResults.register(matId, NUGGET_PT, () -> {
                    Item item = ForgeRegistries.ITEMS.getValue(nuggetItemId);
                    return item == Items.AIR ? null : item;
                });
                castResultCount += 2;
            }

            if (mat.stats().castOnly()) continue;

            SmitheryItems.registerPartsFor(mat.id(), PART_ITEMS);
            partCount++;

            if (mat.stats().meltingTemp() > 0f) {
                for (PartType pt : SmitheryAPI.PART_TYPES.all()) {
                    if (pt.syntheticCast()) continue;
                    String partItemId = "soa_additions:" + path + "_" + pt.id().getPath();
                    SmitheryAPI.registerMeltingRecipe(partItemId, mat.id().toString(), pt.castMb());
                    remeltCount++;
                }
            }
        }

        registerCrossModCastResults();

        PART_ITEMS.register(modEventBus);

        LOG.info("SOA Smithery integration: {} materials, {} fluids, {} part-materials (x {} part types), {} remelt recipes, {} cast results",
                matCount, fluidCount, partCount,
                SmitheryAPI.PART_TYPES.all().size(), remeltCount, castResultCount);
    }

    private static void registerCrossModCastResults() {
        // Thermal Foundation
        castIngot("bronze",       "thermal", "bronze_ingot",       "bronze_nugget");
        castIngot("constantan",   "thermal", "constantan_ingot",   "constantan_nugget");
        castIngot("electrum",     "thermal", "electrum_ingot",     "electrum_nugget");
        castIngot("enderium",     "thermal", "enderium_ingot",     "enderium_nugget");
        castIngot("invar",        "thermal", "invar_ingot",        "invar_nugget");
        castIngot("lead",         "thermal", "lead_ingot",         "lead_nugget");
        castIngot("lumium",       "thermal", "lumium_ingot",       "lumium_nugget");
        castIngot("nickel",       "thermal", "nickel_ingot",       "nickel_nugget");
        castIngot("signalum",     "thermal", "signalum_ingot",     "signalum_nugget");
        castIngot("silver",       "thermal", "silver_ingot",       "silver_nugget");
        castIngot("tin",          "thermal", "tin_ingot",          "tin_nugget");

        // EnderIO
        castIngot("conductive_iron",  "enderio", "conductive_alloy_ingot",  "conductive_alloy_nugget");
        castIngot("dark_steel",       "enderio", "dark_steel_ingot",        "dark_steel_nugget");
        castIngot("end_steel",        "enderio", "end_steel_ingot",         "end_steel_nugget");
        castIngot("energetic_alloy",  "enderio", "energetic_alloy_ingot",   "energetic_alloy_nugget");
        castIngot("pulsating_iron",   "enderio", "pulsating_alloy_ingot",   "pulsating_alloy_nugget");
        castIngot("redstone_alloy",   "enderio", "redstone_alloy_ingot",    "redstone_alloy_nugget");
        castIngot("soularium",        "enderio", "soularium_ingot",         "soularium_nugget");
        castIngot("vibrant_alloy",    "enderio", "vibrant_alloy_ingot",     "vibrant_alloy_nugget");
        castIngot("copper_alloy",     "enderio", "copper_alloy_ingot",      "copper_alloy_nugget");

        // Botania
        castIngot("manasteel",   "botania", "manasteel_ingot",  "manasteel_nugget");
        castIngot("elementium",  "botania", "elementium_ingot", "elementium_nugget");
        castIngot("terrasteel",  "botania", "terrasteel_ingot", "terrasteel_nugget");
        castIngotOnly("gaia",    "botania", "gaia_ingot");

        // Mekanism (prefix format)
        castIngot("steel",             "mekanism", "ingot_steel",             "nugget_steel");
        castIngot("refined_glowstone", "mekanism", "ingot_refined_glowstone", "nugget_refined_glowstone");
        castIngot("refined_obsidian",  "mekanism", "ingot_refined_obsidian",  "nugget_refined_obsidian");
        castIngot("osmium",            "bno",      "osmium_ingot",            "osmium_nugget");

        // Twilight Forest
        castIngotOnly("fiery",       "twilightforest", "fiery_ingot");
        castIngotOnly("knightmetal", "twilightforest", "knightmetal_ingot");
        castIngotOnly("steeleaf",    "twilightforest", "steeleaf_ingot");

        // Draconic Evolution
        castIngot("draconium",       "draconicevolution", "draconium_ingot",           "draconium_nugget");
        castIngot("awakened_plustic", "draconicevolution", "awakened_draconium_ingot",  "awakened_draconium_nugget");

        // Mystical Agriculture
        castIngotOnly("inferium", "mysticalagriculture", "inferium_ingot");
        castIngotOnly("soulium",  "mysticalagriculture", "soulium_ingot");

        // Aether
        castIngotOnly("zanite", "aether", "zanite_gemstone");

        // BNO
        castIngot("aluminium", "bno", "aluminum_ingot", "aluminum_nugget");

        // ProjectE
        castIngotOnly("dark_matter", "projecte", "dark_matter");
        castIngotOnly("red_matter",  "projecte", "red_matter");

        // Avaritia
        castIngot("neutronium",     "avaritia", "neutron_ingot",        "neutron_nugget");
        castIngot("infinity",       "avaritia", "infinity_ingot",       "infinity_nugget");
        castIngotOnly("crystal_matrix", "avaritia", "crystal_matrix_ingot");

        // AllTheModium
        castIngot("allthemodium", "allthemodium", "allthemodium_ingot", "allthemodium_nugget");
        castIngot("vibranium",    "allthemodium", "vibranium_ingot",    "vibranium_nugget");
        castIngot("unobtainium",  "allthemodium", "unobtainium_ingot", "unobtainium_nugget");

        // Big Reactors
        castIngotOnly("yellorium", "bigreactors", "yellorium_ingot");
        castIngotOnly("cyanite",   "bigreactors", "cyanite_ingot");

        // Mystical Agriculture addons
        castIngot("insanium", "mysticalagradditions", "insanium_ingot", "insanium_nugget");

        // Naming mismatches within soa_additions
        castIngotOnly("chaotic_metal", "tconevo", "chaotic_ingot");
        castIngotOnly("wyvern_metal",  "tconevo", "wyvern_ingot");

        // Valoria
        castIngot("cobalt", "valoria", "cobalt_ingot", "cobalt_nugget");

        // Embers
        castIngot("mithril", "embers", "mithril_ingot", "mithril_nugget");

        // Redstone Arsenal
        castIngot("fluxed_electrum", "redstone_arsenal", "flux_ingot", "flux_nugget");

        // TAIGA — every material castable back to ingot/nugget (melting was one-way;
        // original TC 1.12 had ingot+nugget casts for all TAIGA fluids).
        String[] taigaMats = {
                "abyssum", "adamant", "astrium", "aurorium", "basalt",
                "dilithium", "duranite", "dyonite", "eezo", "fractum",
                "ignitz", "imperomite", "iox", "jauxum", "karmesine",
                "lumix", "meteorite", "nihilite", "niob", "nucleum",
                "obsidiorite", "osram", "ovium", "palladium", "prometheum",
                "proxii", "seismum", "solarium", "terrax", "tiberium",
                "triberium", "tritonite", "valyrium", "vibranium", "violium",
                "yrdeen"
        };
        for (String mat : taigaMats) {
            castIngot(mat, "taiga", mat + "_ingot", mat + "_nugget");
        }
        // uru ingot lives in soa_additions:, its new nugget in taiga: (split namespaces)
        castIngotOnly("uru", "soa_additions", "uru_ingot");
        ResourceLocation uruMat = ResourceLocation.fromNamespaceAndPath("soa_additions", "uru");
        ResourceLocation uruNugget = ResourceLocation.fromNamespaceAndPath("taiga", "uru_nugget");
        CastResults.register(uruMat, NUGGET_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(uruNugget);
            return item == Items.AIR ? null : item;
        });

        // TConstruct classic alloys (items in soa_additions:)
        castIngot("manyullyn",   "soa_additions", "manyullyn_ingot",   "manyullyn_nugget");
        castIngot("pigiron",     "soa_additions", "pigiron_ingot",     "pigiron_nugget");
        castIngot("alubrass",    "soa_additions", "alubrass_ingot",    "alubrass_nugget");

        // Knightslime merged into Smithery's slimeknightium: the material now lives in smithery:
        // while its ingot and nugget stay soa_additions: items. The helpers above assume both
        // share the soa_additions namespace, so this pours directly (same shape as blood below).
        ResourceLocation slimeknightiumMat = ResourceLocation.fromNamespaceAndPath("smithery", "slimeknightium");
        ResourceLocation knightslimeIngot = ResourceLocation.fromNamespaceAndPath("soa_additions", "knightslime_ingot");
        ResourceLocation knightslimeNugget = ResourceLocation.fromNamespaceAndPath("soa_additions", "knightslime_nugget");
        CastResults.register(slimeknightiumMat, INGOT_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(knightslimeIngot);
            return item == Items.AIR ? null : item;
        });
        CastResults.register(slimeknightiumMat, NUGGET_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(knightslimeNugget);
            return item == Items.AIR ? null : item;
        });

        // TConstruct "Coagulated Blood" (tconstruct:edible:3, oredict slimeballBlood):
        // pouring smithery's mob-blood into an ingot cast coagulates it. Material lives
        // in smithery: (SmitheryMaterials.BLOOD), so register directly (helpers assume
        // soa_additions: material ids). Melting back is registered in SoaSmitheryMelting.
        ResourceLocation bloodMat = ResourceLocation.fromNamespaceAndPath("smithery", "blood");
        ResourceLocation coagulated = ResourceLocation.fromNamespaceAndPath("soa_additions", "coagulated_blood");
        CastResults.register(bloodMat, INGOT_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(coagulated);
            return item == Items.AIR ? null : item;
        });

        // Malum
        castIngot("soul_stained_steel", "malum", "soul_stained_steel_ingot", "soul_stained_steel_nugget");
        castIngot("hallowed_gold",      "malum", "hallowed_gold_ingot",      "hallowed_gold_nugget");
        castIngot("cthonic_gold",       "malum", "cthonic_gold",             "cthonic_gold_fragment");
        castIngot("malignant_pewter",   "malum", "malignant_pewter_ingot",   "malignant_pewter_nugget");
        castIngot("blazing_quartz",     "malum", "blazing_quartz",           "blazing_quartz_fragment");
        castIngotOnly("soulstone",      "malum", "processed_soulstone");

        // Valoria
        castIngot("ancient",     "valoria", "ancient_ingot",    "ancient_shard");
        castIngot("black_gold",  "valoria", "black_gold_ingot", "black_gold_nugget");
        castIngotOnly("aquarius", "valoria", "aquarius_ingot");
        castIngotOnly("crimtane", "valoria", "crimtane_ingot");
        castIngotOnly("infernal", "valoria", "infernal_ingot");
        castIngotOnly("nature",   "valoria", "nature_ingot");
        castIngotOnly("pearlium", "valoria", "pearlium_ingot");
        castIngotOnly("void",     "valoria", "void_ingot");
    }

    private static void castIngot(String matPath, String ns, String ingotPath, String nuggetPath) {
        ResourceLocation matId = ResourceLocation.fromNamespaceAndPath("soa_additions", matPath);
        ResourceLocation ingotId = ResourceLocation.fromNamespaceAndPath(ns, ingotPath);
        ResourceLocation nuggetId = ResourceLocation.fromNamespaceAndPath(ns, nuggetPath);
        CastResults.register(matId, INGOT_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(ingotId);
            return item == Items.AIR ? null : item;
        });
        CastResults.register(matId, NUGGET_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(nuggetId);
            return item == Items.AIR ? null : item;
        });
    }

    private static void castIngotOnly(String matPath, String ns, String ingotPath) {
        ResourceLocation matId = ResourceLocation.fromNamespaceAndPath("soa_additions", matPath);
        ResourceLocation ingotId = ResourceLocation.fromNamespaceAndPath(ns, ingotPath);
        CastResults.register(matId, INGOT_PT, () -> {
            Item item = ForgeRegistries.ITEMS.getValue(ingotId);
            return item == Items.AIR ? null : item;
        });
    }

    private SmitheryIntegration() {}
}
