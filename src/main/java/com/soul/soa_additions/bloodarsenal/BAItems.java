package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.item.BloodOrangeItem;
import com.soul.soa_additions.bloodarsenal.item.ModifierTomeItem;
import com.soul.soa_additions.bloodarsenal.item.bauble.*;
import com.soul.soa_additions.bloodarsenal.item.sigil.*;
import com.soul.soa_additions.bloodarsenal.item.stasis.*;
import com.soul.soa_additions.bloodarsenal.item.tool.*;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred register for all Blood Arsenal items.
 * Registered under the {@code bloodarsenal} modid.
 */
public final class BAItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "bloodarsenal");

    // ── Base materials ───────────────────────────────────────────────────

    public static final RegistryObject<Item> GLASS_SHARD = ITEMS.register("glass_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_STICK = ITEMS.register("blood_infused_stick",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_GLOWSTONE_DUST = ITEMS.register("blood_infused_glowstone_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> INERT_BLOOD_INFUSED_IRON_INGOT = ITEMS.register("inert_blood_infused_iron_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_INGOT = ITEMS.register("blood_infused_iron_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLOOD_BURNED_STRING = ITEMS.register("blood_burned_string",
            () -> new Item(new Item.Properties()));

    // Stasis Plate crafting component (1.12: bloodarsenal:base_item meta 5).
    // Distinct from the STASIS_PLATE trap block registered in BABlocks.
    public static final RegistryObject<Item> STASIS_PLATE_ITEM = ITEMS.register("stasis_plate_item",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── Food ─────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> BLOOD_ORANGE = ITEMS.register("blood_orange",
            BloodOrangeItem::new);

    // ── Reagents ─────────────────────────────────────────────────────────

    public static final RegistryObject<Item> REAGENT_SWIMMING = ITEMS.register("reagent_swimming",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> REAGENT_ENDER = ITEMS.register("reagent_ender",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> REAGENT_LIGHTNING = ITEMS.register("reagent_lightning",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> REAGENT_DIVINITY = ITEMS.register("reagent_divinity",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // ── Gems ─────────────────────────────────────────────────────────────

    public static final RegistryObject<Item> GEM_SELF_SACRIFICE = ITEMS.register("gem_self_sacrifice",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GEM_SACRIFICE = ITEMS.register("gem_sacrifice",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GEM_TARTARIC = ITEMS.register("gem_tartaric",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── Blood Diamonds ───────────────────────────────────────────────────

    public static final RegistryObject<Item> BLOOD_DIAMOND = ITEMS.register("blood_diamond",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BLOOD_DIAMOND_INERT = ITEMS.register("blood_diamond_inert",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BLOOD_DIAMOND_INFUSED = ITEMS.register("blood_diamond_infused",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BLOOD_DIAMOND_BOUND = ITEMS.register("blood_diamond_bound",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // ── Modifier Tome ────────────────────────────────────────────────────

    public static final RegistryObject<Item> MODIFIER_TOME = ITEMS.register("modifier_tome",
            ModifierTomeItem::new);

    // ── Blood-Infused Wooden Tools ──────────────────────────────────────

    public static final RegistryObject<Item> BLOOD_INFUSED_WOODEN_SWORD = ITEMS.register("blood_infused_wooden_sword",
            () -> new BloodInfusedSwordItem(BATiers.BLOOD_INFUSED_WOODEN, 3, -2.4f, new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_WOODEN_AXE = ITEMS.register("blood_infused_wooden_axe",
            () -> new BloodInfusedAxeItem(BATiers.BLOOD_INFUSED_WOODEN, 6.0f, -3.2f, new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_WOODEN_PICKAXE = ITEMS.register("blood_infused_wooden_pickaxe",
            () -> new BloodInfusedPickaxeItem(BATiers.BLOOD_INFUSED_WOODEN, 1, -2.8f, new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_WOODEN_SHOVEL = ITEMS.register("blood_infused_wooden_shovel",
            () -> new BloodInfusedShovelItem(BATiers.BLOOD_INFUSED_WOODEN, 1.5f, -3.0f, new Item.Properties()));

    public static final RegistryObject<Item> BLOOD_INFUSED_WOODEN_SICKLE = ITEMS.register("blood_infused_wooden_sickle",
            () -> new SickleItem(BATiers.BLOOD_INFUSED_WOODEN, 2.0f, -2.8f, 1, true, new Item.Properties()));

    // ── Blood-Infused Iron Tools ────────────────────────────────────────

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_SWORD = ITEMS.register("blood_infused_iron_sword",
            () -> new BloodInfusedSwordItem(BATiers.BLOOD_INFUSED_IRON, 3, -2.4f, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_AXE = ITEMS.register("blood_infused_iron_axe",
            () -> new BloodInfusedAxeItem(BATiers.BLOOD_INFUSED_IRON, 6.0f, -3.1f, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_PICKAXE = ITEMS.register("blood_infused_iron_pickaxe",
            () -> new BloodInfusedPickaxeItem(BATiers.BLOOD_INFUSED_IRON, 1, -2.8f, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_SHOVEL = ITEMS.register("blood_infused_iron_shovel",
            () -> new BloodInfusedShovelItem(BATiers.BLOOD_INFUSED_IRON, 1.5f, -3.0f, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BLOOD_INFUSED_IRON_SICKLE = ITEMS.register("blood_infused_iron_sickle",
            () -> new SickleItem(BATiers.BLOOD_INFUSED_IRON, 3.0f, -2.8f, 2, true, new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── Special Weapons ─────────────────────────────────────────────────

    public static final RegistryObject<Item> WARP_BLADE = ITEMS.register("warp_blade",
            () -> new WarpBladeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> STYGIAN_DAGGER = ITEMS.register("stygian_dagger",
            () -> new StygianDaggerItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GLASS_SACRIFICIAL_DAGGER = ITEMS.register("glass_sacrificial_dagger",
            () -> new GlassSacrificialDaggerItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GLASS_DAGGER_OF_SACRIFICE = ITEMS.register("glass_dagger_of_sacrifice",
            () -> new GlassDaggerOfSacrificeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── Bound Tools ─────────────────────────────────────────────────────

    public static final RegistryObject<Item> BOUND_STICK = ITEMS.register("bound_stick",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BOUND_IGNITER = ITEMS.register("bound_igniter",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BOUND_SHEARS = ITEMS.register("bound_shears",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BOUND_SICKLE = ITEMS.register("bound_sickle",
            () -> new SickleItem(Tiers.NETHERITE, 4.0f, -2.8f, 3, true,
                    new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    // ── Stasis Tools ────────────────────────────────────────────────────

    public static final RegistryObject<Item> STASIS_SWORD = ITEMS.register("stasis_sword",
            () -> new StasisSwordItem(Tiers.DIAMOND, 3, -2.4f, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final RegistryObject<Item> STASIS_PICKAXE = ITEMS.register("stasis_pickaxe",
            () -> new StasisPickaxeItem(Tiers.DIAMOND, 1, -2.8f, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final RegistryObject<Item> STASIS_AXE = ITEMS.register("stasis_axe",
            () -> new StasisAxeItem(Tiers.DIAMOND, 5.0f, -3.0f, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final RegistryObject<Item> STASIS_SHOVEL = ITEMS.register("stasis_shovel",
            () -> new StasisShovelItem(Tiers.DIAMOND, 1.5f, -3.0f, new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    // ── Sigils ──────────────────────────────────────────────────────────

    public static final RegistryObject<Item> SIGIL_LIGHTNING = ITEMS.register("sigil_lightning",
            () -> new LightningSigilItem());

    public static final RegistryObject<Item> SIGIL_SWIMMING = ITEMS.register("sigil_swimming",
            () -> new SwimmingSigilItem());

    public static final RegistryObject<Item> SIGIL_ENDER = ITEMS.register("sigil_ender",
            () -> new EnderSigilItem());

    public static final RegistryObject<Item> SIGIL_DIVINITY = ITEMS.register("sigil_divinity",
            () -> new DivinitySigilItem());

    public static final RegistryObject<Item> SIGIL_SENTIENCE = ITEMS.register("sigil_sentience",
            () -> new SentienceSigilItem());

    public static final RegistryObject<Item> SIGIL_AUGMENTED_HOLDING = ITEMS.register("sigil_augmented_holding",
            () -> new AugmentedHoldingSigilItem());

    // ── Baubles ─────────────────────────────────────────────────────────

    public static final RegistryObject<Item> VAMPIRE_RING = ITEMS.register("vampire_ring",
            VampireRingItem::new);

    public static final RegistryObject<Item> SACRIFICE_AMULET = ITEMS.register("sacrifice_amulet",
            SacrificeAmuletItem::new);

    public static final RegistryObject<Item> SELF_SACRIFICE_AMULET = ITEMS.register("self_sacrifice_amulet",
            SelfSacrificeAmuletItem::new);

    public static final RegistryObject<Item> SOUL_PENDANT_PETTY = ITEMS.register("soul_pendant_petty",
            () -> new SoulPendantItem(0, 64));

    public static final RegistryObject<Item> SOUL_PENDANT_LESSER = ITEMS.register("soul_pendant_lesser",
            () -> new SoulPendantItem(1, 256));

    public static final RegistryObject<Item> SOUL_PENDANT_COMMON = ITEMS.register("soul_pendant_common",
            () -> new SoulPendantItem(2, 1024));

    public static final RegistryObject<Item> SOUL_PENDANT_GREATER = ITEMS.register("soul_pendant_greater",
            () -> new SoulPendantItem(3, 4096));

    public static final RegistryObject<Item> SOUL_PENDANT_GRAND = ITEMS.register("soul_pendant_grand",
            () -> new SoulPendantItem(4, 16384));

    // ── Fluid bucket ────────────────────────────────────────────────────

    static {
        BAFluids.REFINED_LIFE_ESSENCE_BUCKET = ITEMS.register("refined_life_essence_bucket",
                () -> new BucketItem(BAFluids.REFINED_LIFE_ESSENCE_SOURCE,
                        new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    }

    private BAItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
