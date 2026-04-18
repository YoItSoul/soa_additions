package com.soul.soa_additions.tconstructevo.item;

import com.soul.soa_additions.tconstructevo.TConEvoItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

/**
 * Miscellaneous non-material, non-artifact items registered by TConstructEvo.
 * Currently just the Coalescence Matrix (creative-tab icon + used as a crafting
 * component for the Sceptre later).
 */
public final class TCEMiscItems {

    public static final RegistryObject<Item> COALESCENCE_MATRIX = TConEvoItems.register(
            "coalescence_matrix",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(16)));

    private TCEMiscItems() {}

    public static void bootstrap() {
        // Touching the class ensures <clinit> runs and RegistryObjects are created.
    }
}
