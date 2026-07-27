package com.soul.soa_additions.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Restores the GreedyCraft-era Mystical Agriculture compat crops that MA 1.20 no longer
 * ships (their 1.12 target mods died; the SOA-ported materials live on in soa_additions).
 *
 * <p>Crops are registered under the {@code mysticalagriculture} namespace so MA's own item
 * registration produces {@code mysticalagriculture:<name>_essence} / {@code _seeds} — the exact
 * ids GC's ported recipes (organic infuser grows, seed crafts) already reference.
 *
 * <p>Tiers come from GC's mysticalagriculture.cfg; essence/seed/flower colors are averaged from
 * the MA 1.12 essence textures so the tinted 1.20 blank-texture pipeline matches the old look.
 * MA generates seed-crafting and seed-infusion recipes dynamically; essence -> material
 * conversions are datapack recipes in kubejs (recipes/ma_essence/).
 *
 * <p>Crops whose GC material has no 1.20 equivalent (EnderIO alloys, coralium, quicksilver,
 * slate, fluxed electrum, glowstone ingot) are deliberately not registered.
 */
@MysticalAgriculturePlugin
public final class SoaMysticalCropsPlugin implements IMysticalAgriculturePlugin {

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        register(registry, "abyssalnite",    CropTier.FOUR,  0x371565, "soa_additions:abyssal_ingot");
        register(registry, "aluminum_brass", CropTier.TWO,   0xB9902E, "soa_additions:alubrass_ingot");
        register(registry, "alumite",        CropTier.FOUR,  0xE5A6D8, "soa_additions:alumite_ingot");
        register(registry, "amber",          CropTier.FOUR,  0xB28942, "soa_additions:amber");
        register(registry, "ardite",         CropTier.THREE, 0x802E17, "soa_additions:ardite_ingot");
        register(registry, "black_quartz",   CropTier.THREE, 0x0D0C0B, "soa_additions:black_quartz");
        register(registry, "dreadium",       CropTier.FIVE,  0x730000, "soa_additions:dreadium_ingot");
        register(registry, "ender_amethyst", CropTier.FIVE,  0xA364A9, "soa_additions:amethyst");
        register(registry, "ender_biotite",  CropTier.THREE, 0x070B10, "soa_additions:ender_biotite");
        register(registry, "guardian",       CropTier.THREE, 0x48806F, "minecraft:prismarine_shard");
        register(registry, "knightslime",    CropTier.THREE, 0x845D9A, "soa_additions:knightslime_ingot");
        register(registry, "malachite",      CropTier.FOUR,  0x4B8D77, "soa_additions:malachite");
        register(registry, "tanzanite",      CropTier.FOUR,  0x704197, "soa_additions:tanzanite");
        register(registry, "thaumium",       CropTier.THREE, 0x3A3352, "soa_additions:thaumium_ingot");
        register(registry, "void_metal",     CropTier.FOUR,  0x140921, "soa_additions:void_metal_ingot");
        register(registry, "topaz",          CropTier.FOUR,  0xA96437, "soa_additions:topaz");

        // GC Mystical Creations customs (config CUSTOM_SEED_LIST); witch (mob chunk crop)
        // is omitted. fusion_matrix was GC tier 6 — capped at FIVE, MA core has no tier 6.
        register(registry, "cake",            CropTier.THREE, 0x724C1B, "minecraft:cake");
        register(registry, "chromium",        CropTier.FIVE,  0x4DB6AC, "soa_additions:chromium_ingot");
        register(registry, "stainless_steel", CropTier.FIVE,  0x757575, "soa_additions:stainless_steel_ingot");
        register(registry, "fusion_matrix",   CropTier.FIVE,  0x4A148C, "soa_additions:fusion_matrix_ingot");
        register(registry, "meteor",          CropTier.FIVE,  0xD32F2F, "nyx:meteor_ingot");
    }

    private static void register(ICropRegistry registry, String name, CropTier tier, int color, String materialId) {
        // Deliberately NO color setters: MA's ModelHandler NPE-crashes the client resource
        // reload when colored crops lack baked models, and its tinted-blank textures don't
        // ship in 7.0.x. We instead mirror MA's own crops exactly — explicit per-crop assets
        // (blockstate, item models, essence/seeds/flower textures) live in this mod's
        // resources under assets/mysticalagriculture/. The color param is retained as the
        // authoritative palette reference used to generate the custom crops' textures.
        Crop crop = new Crop(
                ResourceLocation.fromNamespaceAndPath("mysticalagriculture", name),
                tier,
                CropType.RESOURCE,
                LazyIngredient.item(materialId));
        registry.register(crop);
    }
}
