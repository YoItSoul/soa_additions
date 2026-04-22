package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.SoaAdditions;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

/**
 * Material IDs used by the Draconic integration. The material definitions
 * themselves live in
 * {@code src/main/resources/data/soa_additions/materials/definition/tconevo/*.json}
 * (the 1.20.1 TConstruct material system is data-driven); this class just
 * exposes the IDs so Java code can reference them without string literals.
 *
 * <p>Draconic Evolution exposes three tool-relevant tiers in 1.20.1:</p>
 * <ul>
 *   <li><b>Draconium Ingot</b> — the base metallic tier (smelted from ore)</li>
 *   <li><b>Draconium (Awakened)</b> — chaos-guardian tier, obtained via fusion</li>
 *   <li><b>Wyvern</b> — pseudo-material used for the chaos-tier upgrade path</li>
 * </ul>
 * A fourth "chaotic" tier will be added once the chaos-shard recipe is wired.
 */
public final class DraconicMaterials {

    public static final MaterialId DRACONIUM = new MaterialId("tconevo", "draconium");
    public static final MaterialId WYVERN = new MaterialId("tconevo", "wyvern");
    public static final MaterialId AWAKENED_DRACONIUM = new MaterialId("tconevo", "awakened_draconium");
    public static final MaterialId CHAOTIC = new MaterialId("tconevo", "chaotic");

    private DraconicMaterials() {}

    public static void bootstrap() {
        // No code-side registration needed — materials are loaded from data.
        // This method exists so DraconicIntegration can reference the class
        // and trigger <clinit> for the IDs above.
    }
}
