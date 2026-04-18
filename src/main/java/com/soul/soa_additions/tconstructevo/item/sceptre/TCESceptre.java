package com.soul.soa_additions.tconstructevo.item.sceptre;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tconstructevo.TConEvoItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

/**
 * Registry holder for the Sceptre tool. Mirrors the pattern that
 * {@code TinkerTools} uses in TC3.x: an unstackable, non-vanilla-damageable
 * item whose {@link ToolDefinition} is keyed by the registry id, so the
 * loader resolves the tool definition JSON at
 * {@code data/soa_additions/tinkering/tool_definitions/tconevo/sceptre.json}.
 *
 * <p>The {@code SCEPTRE_DEFINITION} field has to be declared <em>before</em>
 * {@code SCEPTRE} because the registry-supplier lambda captures it; Java's
 * forward-reference rule rejects the other order even though both static
 * initialisers run before the supplier ever fires.</p>
 */
public final class TCESceptre {

    private static final Item.Properties PROPS = new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE);

    public static final ToolDefinition SCEPTRE_DEFINITION =
            ToolDefinition.create(new ResourceLocation(SoaAdditions.MODID, "tconevo/sceptre"));

    public static final RegistryObject<SceptreItem> SCEPTRE =
            TConEvoItems.register("sceptre", () -> new SceptreItem(PROPS, SCEPTRE_DEFINITION));

    private TCESceptre() {}

    public static void bootstrap() {
        // Touching the class triggers <clinit>; SCEPTRE is registered via TConEvoItems.
    }
}
