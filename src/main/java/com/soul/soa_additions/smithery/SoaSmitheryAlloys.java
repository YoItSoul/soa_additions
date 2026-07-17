package com.soul.soa_additions.smithery;

import com.soul.smithery.api.alloy.AlloyRecipe;
import com.soul.smithery.api.alloy.AlloyRecipes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * GC-parity alloy registry.
 *
 * <p>Ratios are 1:1 with GreedyCraft: the GC CraftTweaker overrides
 * ({@code scripts/recipes/mods/tconstruct.zs}) where present, otherwise the stock
 * TAIGA 1.3.4 recipes (preserved in {@code data/taiga/recipes/smeltery/alloys/}) or
 * TConstruct 1.12 {@code TinkerSmeltery.registerAlloys} bytecode. Outputs are the GC
 * amounts — most GC alloys are deliberately lossy, so output != sum of inputs.
 *
 * <p>Input ids must be the material id the melting layer actually stores fluid under:
 * vanilla iron/gold/copper/redstone/stone/emerald/blood melt into {@code smithery:*}
 * (builtin SmitheryMeltingRecipes), everything SOA-registered melts into
 * {@code soa_additions:*}.
 */
public final class SoaSmitheryAlloys {

    public static void register() {

        // ================================================================
        // GC CraftTweaker alloys (tconstruct.zs) — GC replaced stock yrdeen
        // with this recipe, so the three stock yrdeen variants are omitted.
        // ================================================================

        alloy("yrdeen", 1915,
                List.of(in("uru", 432), in("valyrium", 432), in("signalum", 144)),
                out("yrdeen", 432));

        alloy("adamant", 0,
                List.of(in("nihilite", 144), in("iox", 432)),
                out("adamant", 432));

        alloy("emerald", 0,
                List.of(in("experience", 432), sm("redstone", 288), in("glowstone", 288)),
                smOut("emerald", 288));

        alloy("end_steel", 0,
                List.of(in("obsidian", 144), in("dark_steel", 144), in("ender", 125)),
                out("end_steel", 144));

        alloy("experience", 0,
                List.of(sm("blood", 1152), sm("emerald", 144), sm("gold", 144)),
                out("experience", 2304));

        // NOTE: pyrotheum has no 1.20 item/melting source — dormant until one exists.
        alloy("fierymetal", 0,
                List.of(sm("iron", 288), in("pyrotheum", 144), in("lava", 144)),
                out("fierymetal", 288));

        alloy("fluxed_electrum", 0,
                List.of(in("electrum", 72), sm("redstone", 25)),
                out("fluxed_electrum", 72));

        alloy("fusion_matrix", 0,
                List.of(in("manyullyn", 288), in("adamant", 144), in("enderium", 288)),
                out("fusion_matrix", 288));

        // NOTE: milk/chocolate_liquor have no melting source yet — dormant.
        alloy("liquid_chocolate", 0,
                List.of(in("chocolate_liquor", 144), in("milk", 144)),
                out("liquid_chocolate", 288));

        alloy("modularium_from_conductive_iron", 0,
                List.of(in("conductive_iron", 144), in("bronze", 144)),
                out("modularium", 288));

        alloy("modularium_from_iron", 0,
                List.of(sm("iron", 36), in("bronze", 36), sm("redstone", 25)),
                out("modularium", 72));

        alloy("netherite", 0,
                List.of(sm("gold", 576), sm("ancient_debris", 576)),
                smOut("netherite", 144));

        alloy("scorched", 0,
                List.of(in("lava", 144), sm("stone", 144)),
                out("scorched", 144));

        alloy("stainless_steel", 0,
                List.of(in("manganese_steel", 576), in("nickel", 144), in("chromium", 144)),
                out("stainless_steel", 576));

        alloy("manganese_steel", 0,
                List.of(in("steel", 288), in("manganese", 144)),
                out("manganese_steel", 288));

        alloy("terra_alloy", 0,
                List.of(in("cytosinite", 144), in("cryonium", 144), in("infernium", 144), in("titanium", 144)),
                out("terra_alloy", 288));

        // NOTE: cryotheum has no 1.20 item/melting source — dormant until one exists.
        alloy("gelid_enderium", 0,
                List.of(in("enderium", 18), in("cryotheum", 125)),
                out("gelid_enderium", 18));

        // ================================================================
        // TConstruct 1.12 classic alloys (TinkerSmeltery.registerAlloys).
        // knightslime substitutions: purple slime -> slime, seared stone ->
        // molten stone (neither 1.12 fluid exists in the 1.20 pack).
        // ================================================================

        alloy("manyullyn", 0,
                List.of(in("cobalt", 144), in("ardite", 144)),
                out("manyullyn", 144));

        alloy("pigiron", 0,
                List.of(sm("iron", 144), sm("blood", 40), in("clay", 72)),
                out("pigiron", 144));

        alloy("knightslime", 0,
                List.of(sm("iron", 72), in("slime", 125), sm("stone", 144)),
                out("knightslime", 72));

        alloy("alubrass", 0,
                List.of(sm("copper", 144), in("aluminium", 432)),
                out("alubrass", 576));

        // ================================================================
        // Stock TAIGA 1.3.4 alloys (all kept by GC). Temps from the original
        // recipe data. nitronite omitted: its magma input had no obtainable
        // source in GC either (magma/nitronite items were never registered).
        // ignitz's 1.12 alubrass input survives via the alubrass alloy above.
        // ================================================================

        alloy("adamant_from_vibranium", 3050,
                List.of(in("vibranium", 144), in("solarium", 144), in("iox", 432)),
                out("adamant", 432));

        alloy("astrium", 850,
                List.of(in("terrax", 432), in("aurorium", 288)),
                out("astrium", 288));

        alloy("dyonite_from_tiberium", 800,
                List.of(in("tiberium", 1728), in("fractum", 144), in("seismum", 144), in("osram", 144)),
                out("dyonite", 432));

        alloy("dyonite_from_triberium", 800,
                List.of(in("triberium", 432), in("fractum", 144), in("seismum", 144), in("osram", 144)),
                out("dyonite", 432));

        alloy("fractum", 700,
                List.of(in("triberium", 432), in("obsidian", 432), in("abyssum", 144)),
                out("fractum", 288));

        alloy("ignitz", 850,
                List.of(in("alubrass", 288), in("terrax", 288), in("osram", 144)),
                out("ignitz", 288));

        alloy("imperomite", 1400,
                List.of(in("duranite", 432), in("prometheum", 144), in("abyssum", 144)),
                out("imperomite", 288));

        alloy("iox_from_meteorite", 950,
                List.of(in("eezo", 288), in("abyssum", 288), in("osram", 288),
                        in("meteorite", 1296), in("obsidian", 1296)),
                out("iox", 144));

        alloy("iox_from_obsidiorite", 1050,
                List.of(in("eezo", 288), in("abyssum", 288), in("osram", 288), in("obsidiorite", 1296)),
                out("iox", 144));

        alloy("lumix", 850,
                List.of(in("palladium", 144), in("terrax", 144)),
                out("lumix", 144));

        alloy("nihilite", 3050,
                List.of(in("vibranium", 144), in("solarium", 144)),
                out("nihilite", 144));

        alloy("niob", 1400,
                List.of(in("palladium", 432), in("duranite", 144), in("osram", 144)),
                out("niob", 432));

        alloy("nucleum_from_imperomite", 900,
                List.of(in("imperomite", 432), in("osram", 144), in("eezo", 144)),
                out("nucleum", 432));

        alloy("nucleum_from_niob", 700,
                List.of(in("niob", 432), in("eezo", 144), in("abyssum", 144)),
                out("nucleum", 432));

        alloy("nucleum_from_proxii", 800,
                List.of(in("proxii", 432), in("abyssum", 144), in("osram", 144)),
                out("nucleum", 432));

        alloy("obsidiorite", 950,
                List.of(in("meteorite", 144), in("obsidian", 144)),
                out("obsidiorite", 144));

        alloy("proxii", 850,
                List.of(in("prometheum", 432), in("palladium", 432), in("eezo", 144)),
                out("proxii", 432));

        alloy("seismum", 550,
                List.of(in("obsidian", 576), in("triberium", 288), in("eezo", 144)),
                out("seismum", 576));

        alloy("solarium", 1915,
                List.of(in("valyrium", 288), in("uru", 288), in("nucleum", 144)),
                out("solarium", 288));

        alloy("terrax", 750,
                List.of(in("karmesine", 144), in("ovium", 144), in("jauxum", 144)),
                out("terrax", 288));

        alloy("triberium", 550,
                List.of(in("tiberium", 720), in("basalt", 144)),
                out("triberium", 144));

        alloy("triberium_from_dilithium", 1500,
                List.of(in("tiberium", 720), in("dilithium", 288)),
                out("triberium", 144));

        alloy("tritonite", 850,
                List.of(in("cobalt", 432), in("terrax", 288)),
                out("tritonite", 288));

        alloy("violium", 750,
                List.of(in("aurorium", 432), in("alubrass", 288)),
                out("violium", 288));
    }

    private static void alloy(String name, float minTempC, List<AlloyRecipe.Input> inputs, AlloyRecipe.Output result) {
        AlloyRecipes.register(id(name), new AlloyRecipe(inputs, result, minTempC));
    }

    /** Input keyed to an SOA-registered material fluid. */
    private static AlloyRecipe.Input in(String material, int mb) {
        return new AlloyRecipe.Input(id(material), mb);
    }

    /** Input keyed to a Smithery-builtin material fluid (vanilla melts). */
    private static AlloyRecipe.Input sm(String material, int mb) {
        return new AlloyRecipe.Input(ResourceLocation.fromNamespaceAndPath("smithery", material), mb);
    }

    private static AlloyRecipe.Output out(String material, int mb) {
        return new AlloyRecipe.Output(id(material), mb);
    }

    /** Output keyed to a Smithery-builtin material fluid (must match what vanilla items melt into). */
    private static AlloyRecipe.Output smOut(String material, int mb) {
        return new AlloyRecipe.Output(ResourceLocation.fromNamespaceAndPath("smithery", material), mb);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("soa_additions", path);
    }

    private SoaSmitheryAlloys() {}
}
