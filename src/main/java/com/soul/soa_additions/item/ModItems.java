package com.soul.soa_additions.item;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.item.QuestBookItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SoaAdditions.MODID);

    // ========== Existing items ==========

    public static final RegistryObject<Item> ABYSSAL_INGOT   = registerRareIngot("abyssal_ingot");
    public static final RegistryObject<Item> ETHER_INGOT     = registerRareIngot("ether_ingot");
    public static final RegistryObject<Item> INFERNIUM_INGOT = registerRareIngot("infernium_ingot");
    public static final RegistryObject<Item> VOID_INGOT      = registerRareIngot("void_ingot");

    // ========== TConstruct classic alloys (items for the ported alloy fluids) ==========

    public static final RegistryObject<Item> MANYULLYN_INGOT   = registerRareIngot("manyullyn_ingot");
    public static final RegistryObject<Item> MANYULLYN_NUGGET  = registerRareIngot("manyullyn_nugget");
    public static final RegistryObject<Item> PIGIRON_INGOT     = registerRareIngot("pigiron_ingot");
    public static final RegistryObject<Item> PIGIRON_NUGGET    = registerRareIngot("pigiron_nugget");
    public static final RegistryObject<Item> KNIGHTSLIME_INGOT = registerRareIngot("knightslime_ingot");
    public static final RegistryObject<Item> KNIGHTSLIME_NUGGET = registerRareIngot("knightslime_nugget");
    public static final RegistryObject<Item> ALUBRASS_INGOT    = registerRareIngot("alubrass_ingot");
    public static final RegistryObject<Item> ALUBRASS_NUGGET   = registerRareIngot("alubrass_nugget");

    // ========== GreedyCraft custom metals ==========

    public static final RegistryObject<Item> AEONSTEEL_INGOT      = registerRareIngot("aeonsteel_ingot");
    public static final RegistryObject<Item> AEROITE_INGOT        = registerRareIngot("aeroite_ingot");
    public static final RegistryObject<Item> AQUALITE_INGOT       = registerRareIngot("aqualite_ingot");
    public static final RegistryObject<Item> ASGARDIUM_INGOT      = registerRareIngot("asgardium_ingot");
    public static final RegistryObject<Item> ASTRAL_METAL_INGOT   = registerRareIngot("astral_metal_ingot");
    public static final RegistryObject<Item> CHROMASTEEL_INGOT    = registerRareIngot("chromasteel_ingot");
    public static final RegistryObject<Item> CHROMIUM_INGOT       = registerRareIngot("chromium_ingot");
    public static final RegistryObject<Item> COSMILITE_INGOT      = registerRareIngot("cosmilite_ingot");
    public static final RegistryObject<Item> CRIMSONITE_INGOT     = registerRareIngot("crimsonite_ingot");
    public static final RegistryObject<Item> CRYONIUM_INGOT       = registerRareIngot("cryonium_ingot");
    public static final RegistryObject<Item> CYTOSINITE_INGOT     = registerRareIngot("cytosinite_ingot");
    public static final RegistryObject<Item> DURASTEEL_INGOT      = registerRareIngot("durasteel_ingot");
    public static final RegistryObject<Item> ELECTRONIUM_INGOT    = registerRareIngot("electronium_ingot");
    public static final RegistryObject<Item> MANGANESE_INGOT      = registerRareIngot("manganese_ingot");
    public static final RegistryObject<Item> MANGANESE_STEEL_INGOT = registerRareIngot("manganese_steel_ingot");
    public static final RegistryObject<Item> PROTONIUM_INGOT      = registerRareIngot("protonium_ingot");
    public static final RegistryObject<Item> SHADOWIUM_INGOT      = registerRareIngot("shadowium_ingot");
    public static final RegistryObject<Item> STAINLESS_STEEL_INGOT = registerRareIngot("stainless_steel_ingot");
    public static final RegistryObject<Item> TERRA_ALLOY_INGOT    = registerRareIngot("terra_alloy_ingot");
    public static final RegistryObject<Item> TITANIUM_INGOT       = registerRareIngot("titanium_ingot");

    // ========== Materials from mods not in SOA (for TCon) ==========

    public static final RegistryObject<Item> ETHAXIUM_INGOT       = registerRareIngot("ethaxium_ingot");
    public static final RegistryObject<Item> SCARLITE              = registerRareIngot("scarlite");
    public static final RegistryObject<Item> HEPHAESTITE           = registerRareIngot("hephaestite");
    public static final RegistryObject<Item> RAVAGING_INGOT        = registerRareIngot("ravaging_ingot");
    public static final RegistryObject<Item> REMORSEFUL_GEM        = registerRareIngot("remorseful_gem");
    public static final RegistryObject<Item> MITHRILLIUM_INGOT     = registerRareIngot("mithrillium_ingot");
    public static final RegistryObject<Item> ADAMINITE_INGOT       = registerRareIngot("adaminite_ingot");
    public static final RegistryObject<Item> MITHMINITE_INGOT      = registerRareIngot("mithminite_ingot");
    public static final RegistryObject<Item> YELLORIUM_INGOT       = registerRareIngot("yellorium_ingot");
    public static final RegistryObject<Item> CINCINNASITE          = registerRareIngot("cincinnasite");
    public static final RegistryObject<Item> RIME_CRYSTAL          = registerRareIngot("rime_crystal");
    public static final RegistryObject<Item> TOFU_GEM              = registerRareIngot("tofu_gem");
    public static final RegistryObject<Item> STRONG_TOFU_GEM       = registerRareIngot("strong_tofu_gem");
    public static final RegistryObject<Item> SAKURA_DIAMOND        = registerRareIngot("sakura_diamond");
    public static final RegistryObject<Item> CHOCOLATE_BAR         = registerRareIngot("chocolate_bar");
    // meteor_ingot moved to NyxItems (nyx: namespace)
    public static final RegistryObject<Item> MODULARIUM_INGOT      = registerRareIngot("modularium_ingot");
    public static final RegistryObject<Item> SPECTRE_INGOT         = registerRareIngot("spectre_ingot");
    public static final RegistryObject<Item> ORICHALCOS_INGOT      = registerRareIngot("orichalcos_ingot");
    public static final RegistryObject<Item> GAIASTEEL_INGOT       = registerRareIngot("gaiasteel_ingot");
    public static final RegistryObject<Item> FUSION_MATRIX_INGOT   = registerRareIngot("fusion_matrix_ingot");

    // ========== External 1.12 mod ports (Thermal Foundation, TConstruct, ExtraUtilities2) ==========
    public static final RegistryObject<Item> IRIDIUM_INGOT         = registerRareIngot("iridium_ingot");
    public static final RegistryObject<Item> IRIDIUM_NUGGET        = registerRareIngot("iridium_nugget");
    public static final RegistryObject<Item> IRIDIUM_DUST          = registerRareIngot("iridium_dust");
    public static final RegistryObject<Item> PLATINUM_INGOT        = registerRareIngot("platinum_ingot");
    public static final RegistryObject<Item> PLATINUM_NUGGET       = registerRareIngot("platinum_nugget");
    public static final RegistryObject<Item> PLATINUM_DUST         = registerRareIngot("platinum_dust");
    public static final RegistryObject<Item> ARDITE_INGOT          = registerRareIngot("ardite_ingot");
    public static final RegistryObject<Item> ARDITE_NUGGET         = registerRareIngot("ardite_nugget");
    public static final RegistryObject<Item> EVIL_METAL_INGOT      = registerRareIngot("evil_metal_ingot");
    public static final RegistryObject<Item> DEMONIC_METAL_INGOT   = registerRareIngot("demonic_metal_ingot");

    // ========== TCon material items (tconevo/plustic port, mods not in SOA) ==========

    public static final RegistryObject<Item> RESTONIA_CRYSTAL = registerRareIngot("restonia_crystal");
    public static final RegistryObject<Item> PALIS_CRYSTAL = registerRareIngot("palis_crystal");
    public static final RegistryObject<Item> DIAMANTINE_CRYSTAL = registerRareIngot("diamantine_crystal");
    public static final RegistryObject<Item> VOID_CRYSTAL = registerRareIngot("void_crystal");
    public static final RegistryObject<Item> EMERALDIC_CRYSTAL = registerRareIngot("emeraldic_crystal");
    public static final RegistryObject<Item> ENORI_CRYSTAL = registerRareIngot("enori_crystal");
    public static final RegistryObject<Item> BLACK_QUARTZ = registerRareIngot("black_quartz");
    public static final RegistryObject<Item> GHOSTWOOD_LOG = registerRareIngot("ghostwood_log");
    public static final RegistryObject<Item> BLOODWOOD_LOG = registerRareIngot("bloodwood_log");
    public static final RegistryObject<Item> DARKWOOD_LOG = registerRareIngot("darkwood_log");
    public static final RegistryObject<Item> FUSEWOOD_LOG = registerRareIngot("fusewood_log");
    public static final RegistryObject<Item> FLUX_CRYSTAL = registerRareIngot("flux_crystal");
    public static final RegistryObject<Item> AQUAMARINE = registerRareIngot("aquamarine");
    public static final RegistryObject<Item> STARMETAL_INGOT = registerRareIngot("starmetal_ingot");
    public static final RegistryObject<Item> URU_INGOT = registerRareIngot("uru_ingot");

    public static final RegistryObject<Item> MEAT_METAL_INGOT = registerRareIngot("meat_metal_ingot");
    public static final RegistryObject<Item> PINK_SLIME_CRYSTAL = registerRareIngot("pink_slime_crystal");
    public static final RegistryObject<Item> MIRION_INGOT = registerRareIngot("mirion_ingot");
    public static final RegistryObject<Item> ALUMITE_INGOT = registerRareIngot("alumite_ingot");
    public static final RegistryObject<Item> OSGLOGLAS_INGOT = registerRareIngot("osgloglas_ingot");
    public static final RegistryObject<Item> OSMIRIDIUM_INGOT = registerRareIngot("osmiridium_ingot");

    // Tinkers' Aether port (1.12 tinkersaether → 1.20.1)
    public static final RegistryObject<Item> VALKYRIE_INGOT  = registerRareIngot("valkyrie_ingot");
    public static final RegistryObject<Item> VALKYRIE_NUGGET = registerRareIngot("valkyrie_nugget");
    public static final RegistryObject<Item> SWET_CRYSTAL    = registerRareIngot("swet_crystal");

    // TCon bowstring/fletching materials (GC additions + RandomThings spectre + Natura)
    public static final RegistryObject<Item> NYLON_STRING = registerRareIngot("nylon_string");
    public static final RegistryObject<Item> RUBBER_BAND = registerRareIngot("rubber_band");
    public static final RegistryObject<Item> NYLON_CLOTH = registerRareIngot("nylon_cloth");
    public static final RegistryObject<Item> SPECTRE_STRING = registerRareIngot("spectre_string");
    public static final RegistryObject<Item> FLAMESTRING = registerRareIngot("flamestring");

    // TCon BoP gem materials (plustic-sourced, for mods not in SOA)
    public static final RegistryObject<Item> SAPPHIRE = registerRareIngot("sapphire");
    public static final RegistryObject<Item> RUBY = registerRareIngot("ruby");
    public static final RegistryObject<Item> PERIDOT = registerRareIngot("peridot");
    public static final RegistryObject<Item> MALACHITE = registerRareIngot("malachite");
    public static final RegistryObject<Item> TOPAZ = registerRareIngot("topaz");
    public static final RegistryObject<Item> TANZANITE = registerRareIngot("tanzanite");
    public static final RegistryObject<Item> AMETHYST_GEM = registerRareIngot("amethyst");
    public static final RegistryObject<Item> AMBER = registerRareIngot("amber");

    // ========== Ghost-material items added for Tinker compat (porting GC originals) ==========
    // These materials were registered in soa_additions Tinker materials but had no
    // obtainable item, leaving JEI showing nothing. Sourced from:
    //   thaumium / void_metal ........ Thaumcraft 1.12 (ingot/nugget/block)
    //   pink_metal ................... TConEvo metal (ingot only; texture from EnderIO crystalline pink slime)
    //   fluix_steel .................. lazy-ae2 / threng (ingot only)
    //   ender_biotite ................ Quark biotite item (representative for tconevo material)
    //   perfect ...................... Additions GC addon "perfectly_generic_item"
    //   crystal_leaf / skyroot_leaf .. Aether 1.12 leaves (textures from aether_legacy)
    public static final RegistryObject<Item> THAUMIUM_INGOT      = registerRareIngot("thaumium_ingot");
    public static final RegistryObject<Item> THAUMIUM_NUGGET     = registerRareIngot("thaumium_nugget");
    public static final RegistryObject<Item> THAUMIUM_BLOCK      = registerRareIngot("thaumium_block");
    public static final RegistryObject<Item> VOID_METAL_INGOT    = registerRareIngot("void_metal_ingot");
    public static final RegistryObject<Item> VOID_METAL_NUGGET   = registerRareIngot("void_metal_nugget");
    public static final RegistryObject<Item> VOID_METAL_BLOCK    = registerRareIngot("void_metal_block");
    public static final RegistryObject<Item> FLUIX_STEEL_INGOT   = registerRareIngot("fluix_steel_ingot");
    public static final RegistryObject<Item> ENDER_BIOTITE       = registerRareIngot("ender_biotite");
    public static final RegistryObject<Item> PERFECT             = registerRareIngot("perfect");
    public static final RegistryObject<Item> CRYSTAL_LEAF        = registerRareIngot("crystal_leaf");
    public static final RegistryObject<Item> SKYROOT_LEAF        = registerRareIngot("skyroot_leaf");

    // ========== GC forge-catalyst materials from mods not in SOA 1.20 ==========
    // Sourced from: abyssalcraft (dreadium), botanicadditions (gaiasteel), forestry (honey_drop),
    // biomesoplenty (terrestrial_artifact), avaritia 1.12 (unstable_matrix = neutron_pile),
    // tconevo (reinforced = metal:20, supremium = metal:30→prismarine alias).
    // NOTE 2026-07-23: unstable_matrix / rainbow_petal / prismarine_ingot are retired —
    // the forge JSONs now use the exact GC items (tconevo:coalescence_matrix,
    // soa_additions:orichalcos_ingot, tconevo:sentient_metal_ingot). See PARITY_REPORT.
    public static final RegistryObject<Item> DREADIUM_INGOT       = registerRareIngot("dreadium_ingot");
    // EnderIO Endergy alloys (absent from EIO 1.20): exact GC items, produced in the
    // EIO alloy smelter via ported enderio:alloy_smelting recipes (gc_machines).
    public static final RegistryObject<Item> MELODIC_ALLOY_INGOT  = registerRareIngot("melodic_alloy_ingot");
    public static final RegistryObject<Item> STELLAR_ALLOY_INGOT  = registerRareIngot("stellar_alloy_ingot");
    // Construct's Armory "Resistance" (conarm:resist_mat): Guardian II quest reward;
    // grants smithery:resistant via data/soa_additions/smithery/modifier_source.
    public static final RegistryObject<Item> RESISTANCE_MAT       = registerRareIngot("resistance_mat");
    // Draconic Additions "Chaotic Energy Core" (draconicadditions:chaotic_energy_core):
    // DA 1.20 dropped the item. Exact GC item + DA fusion recipe (soa_reported_fixes.js);
    // used by the Cosmic Solar Panel fusion.
    public static final RegistryObject<Item> CHAOTIC_ENERGY_CORE  = registerRareIngot("chaotic_energy_core");
    // TConstruct "Coagulated Blood" (tconstruct:edible:3, oredict slimeballBlood):
    // blood slimeball used by the Elven Trade scarlite recipe. Exact TCon texture;
    // tagged forge:slimeballs + forge:slimeballs/blood. Smithery-native source:
    // molten mob-blood + ingot cast -> coagulated (SmitheryIntegration), melts back
    // losslessly (SoaSmitheryMelting).
    public static final RegistryObject<Item> COAGULATED_BLOOD     = registerRareIngot("coagulated_blood");

    // GC "Tablet of Enlightenment" (contenttweaker): stage-backup item — NBT
    // {stage} grants that GameStages stage on use; owner-bound; self-duplicating
    // in a crafting grid (TabletDupeRecipe + crafting-remainder copy).
    public static final RegistryObject<Item> TABLET_OF_ENLIGHTENMENT = ITEMS.register(
            "tablet_of_enlightenment",
            () -> new TabletOfEnlightenmentItem(new Item.Properties()
                    .rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> GAIA_STEEL_INGOT     = registerRareIngot("gaia_steel_ingot");
    public static final RegistryObject<Item> HONEY_DROP           = registerRareIngot("honey_drop");
    public static final RegistryObject<Item> PRISMARINE_INGOT     = registerRareIngot("prismarine_ingot");
    public static final RegistryObject<Item> RAINBOW_PETAL        = registerRareIngot("rainbow_petal");
    public static final RegistryObject<Item> REINFORCED_INGOT     = registerRareIngot("reinforced_ingot");
    public static final RegistryObject<Item> TERRESTRIAL_ARTIFACT = registerRareIngot("terrestrial_artifact");
    public static final RegistryObject<Item> UNSTABLE_MATRIX      = registerRareIngot("unstable_matrix");

    public static final RegistryObject<Item> CHEATER_COIN = ITEMS.register("cheater_coin",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    /** Rainbow Rune — replaces 1.12 quark:rune:16 (Rainbow Rune). Carries the
     *  Botania Mana Rune texture with a permanent enchanted glint via isFoil.
     *  Crafted via the Mana Pool at the same mana cost as a Terrasteel Ingot
     *  (500,000 mana). */
    public static final RegistryObject<Item> RAINBOW_RUNE = ITEMS.register("rainbow_rune",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)) {
                @Override public boolean isFoil(net.minecraft.world.item.ItemStack stack) { return true; }
            });

    public static final RegistryObject<Item> QUEST_BOOK = ITEMS.register("quest_book", QuestBookItem::new);

    public static final RegistryObject<Item> ORB_OF_AVARICE = ITEMS.register("orb_of_avarice",
            com.soul.soa_additions.donor.DonorTokenItem::new);

    /**
     * Never obtainable — registered purely so that
     * {@code assets/soa_additions/models/item/unknown_item.json} is baked as an
     * ordinary item model, which is what
     * {@link com.soul.soa_additions.itemstages.UnknownItemModels} swaps in for a
     * staged non-block item. It was previously requested through
     * {@code ModelEvent.RegisterAdditional}, which did not yield a model
     * retrievable under {@code unknown_item#inventory}, so every hidden item fell
     * through to the question-mark cube instead of the flat sprite.
     *
     * <p>Deliberately in no creative tab, which also keeps it out of JEI — JEI
     * builds its ingredient list from creative tab contents. This mirrors
     * {@code ModBlocks.UNKNOWN_BLOCK}, registered the same way for the same
     * reason.</p>
     */
    public static final RegistryObject<Item> UNKNOWN_ITEM = ITEMS.register("unknown_item",
            () -> new Item(new Item.Properties()));

    // ========== Singularities (chaotic stage) ==========

    public static final RegistryObject<Item> FLUX_SINGULARITY = stageItem("flux_singularity", false);
    public static final RegistryObject<Item> MANA_SINGULARITY = stageItem("mana_singularity", false);
    public static final RegistryObject<Item> EXPERIENCE_SINGULARITY = stageItem("experience_singularity", false);
    public static final RegistryObject<Item> MATTER_SINGULARITY = stageItem("matter_singularity", false);
    public static final RegistryObject<Item> ANTI_ENTROPY_MATTER = stageItem("anti_entropy_matter", false);

    // ========== Chaotic Dominator stage ==========

    public static final RegistryObject<Item> DEATH_COIN = stageGrantItem("death_coin",
            new Item.Properties().stacksTo(1), false, "super_hardmode",
            "\u00a7bRight click to enter Super Hardmode.",
            "\u00a7bLeft click air to exit Super Hardmode");

    // Behaviour lives in the pack's soa_difficulty_items.js: right-click raises
    // by 10, sneak-right-click lowers by 10. Wiring a Java half here would make
    // a sneak-right-click apply twice.
    public static final RegistryObject<Item> DIFFICULTY_CHANGER = stageItem("difficulty_changer",
            new Item.Properties().stacksTo(1), false,
            "\u00a7eShift-right-click to lower difficulty and right click to raise difficulty.",
            "\u00a7a\u00a7oDoes not consume");

    // ========== Descendant of the Sun stage ==========

    public static final RegistryObject<Item> INFERNIUM_NUGGET = ITEMS.register("infernium_nugget",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    // ========== Expert stage ==========

    public static final RegistryObject<Item> FAKE_PHILOSOPHER_STONE = stageItem("fake_philosopher_stone",
            new Item.Properties().stacksTo(1), false,
            "\u00a76Fake, but can still turn sand into glass.",
            "\u00a7d\u00a7lEXPERT ONLY");

    public static final RegistryObject<Item> UNDEAD_MEDKIT = ITEMS.register("undead_medkit",
            () -> new StageFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(0).saturationMod(0f).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HARM, 1, 3), 1.0f)
                            .build()),
                    false, 10, false,
                    "\u00a76A medkit designed for the undead!",
                    "\u00a72UNDEAD ONLY. USE AT YOUR OWN RISK.",
                    "\u00a7d\u00a7lEXPERT ONLY"));

    public static final RegistryObject<Item> STRANGE_LOLIPOP = ITEMS.register("strange_lolipop",
            () -> new StageFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(10).saturationMod(0.5f).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 4), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 5), 1.0f)
                            .build()),
                    true, 32, false,
                    "\u00a7eMakes you move super slow",
                    "\u00a7eBut grants a huge boost to damage.",
                    "\u00a7d\u00a7lEXPERT ONLY"));

    public static final RegistryObject<Item> ADRENALINE = useItem("adrenaline", false,
            RightClickActions.adrenalineRush(),
            "\u00a76Faster, Bigger, Stronger!",
            "\u00a7eRight click to use",
            "\u00a7d\u00a7lEXPERT ONLY");

    public static final RegistryObject<Item> SHIELD_GUM = ITEMS.register("shield_gum",
            () -> new StageFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(8).saturationMod(0.625f).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3), 1.0f)
                            .build()),
                    true, 60, false,
                    "\u00a7eGains 80% damage reduction for 10s!",
                    "\u00a76But chewing it takes a long time...",
                    "\u00a7d\u00a7lEXPERT ONLY"));

    public static final RegistryObject<Item> GOODIE_BAG = stageItem("goodie_bag", true,
            "\u00a76Might contain expert mode exclusive items!",
            "\u00a7d\u00a7lEXPERT ONLY");

    // ========== Fusion Matrix stage ==========

    public static final RegistryObject<Item> BEAST_HAND = stageItem("beast_hand", false,
            "\u00a7eShift-right-click a \u00a7cSummoning Altar\u00a7e to summon \u00a7bFrostmaw");

    // ========== Graduated stage ==========

    public static final RegistryObject<Item> CREATIVE_CONTROLLER = useItemSneakPair("creative_controller",
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), false, false,
            RightClickActions.setGameMode(net.minecraft.world.level.GameType.CREATIVE),
            RightClickActions.setGameMode(net.minecraft.world.level.GameType.SURVIVAL),
            "\u00a7bRight click to switch to creative mode and shift-right-click to switch back.",
            "\u00a76You are unstoppable now.");

    // GreedyCraft 'Creative Modifier' (was tconstruct:materials:50). Modifier part;
    // combine with a tool/armor at the tool/armor forge to apply a free (creative)
    // modifier slot. NOTE: the Smithery modifier behavior still needs wiring in
    // SoaSmitheryModifiers.java \u2014 this registers the item + texture only.
    public static final RegistryObject<Item> CREATIVE_MODIFIER = stageItem("creative_modifier",
            new Item.Properties().rarity(Rarity.EPIC), true,
            "\u00a7dCombine with tools or armors with tool forge / armor forge",
            "\u00a7dto apply a creative modifier (a free modifier slot).");

    public static final RegistryObject<Item> OCD_CERTIFICATE = stageItem("ocd_certificate", false,
            "\u00a7bYou are a qualified OCD patient now.",
            "\u00a7aWhat? You are expecting some bigger reward?",
            "\u00a7dYou have done everything.",
            "\u00a76Now take a break.");

    // ========== Hardmode stage ==========

    public static final RegistryObject<Item> FORBIDDEN_BIBLE = stageItem("forbidden_bible", false,
            "\u00a74Demonic whispers are written all over it.",
            "\u00a74You might summon an evil presence by using this on a summoning altar.");

    public static final RegistryObject<Item> TRUE_BLOOD_SIGIL = useItemPersistent("true_blood_sigil",
            new Item.Properties().stacksTo(1), true,
            RightClickActions.setTimeToNight(),
            "\u00a7eRight click to summon a blood moon",
            "\u00a7eLeft click to skip a blood moon",
            "\u00a7bDoes not consume when used");

    public static final RegistryObject<Item> ORDINARY_MEDAL = stageItem("ordinary_medal", false,
            "\u00a7e\u00a7oNo hero is born to be a hero.");

    public static final RegistryObject<Item> MEDKIT_SUPER = ITEMS.register("medkit_super",
            () -> new StageFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(0).saturationMod(0f).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 9), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 4), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 1200, 3), 1.0f)
                            .build()),
                    false, 80, true,
                    "\u00a77Increases max health by 80 for 60s.",
                    "\u00a77Gains 60s of Regen V.",
                    "\u00a77Restores 64 health."));

    public static final RegistryObject<Item> WITHER_SOUL = stageItem("wither_soul", true,
            "\u00a75\u00a7oThe soul of the most mighty Wither.");

    public static final RegistryObject<Item> DRAGON_SOUL = stageItem("dragon_soul", true,
            "\u00a7d\u00a7oSoul of the most mighty dragon.");

    public static final RegistryObject<Item> CREATIVE_SHARD = stageItem("creative_shard", true,
            "\u00a75\u00a7oAll heroes are born to be ordinary.");

    // ========== Master Wizard stage ==========

    public static final RegistryObject<Item> PURIFYING_PILL = stageItem("purifying_pill", true,
            "\u00a7eThe best mental medicine.",
            "\u00a7dCleans all warp when used.");

    public static final RegistryObject<Item> ENERGY_MATTER_CORE = stageItem("energy_matter_core", true,
            "\u00a7eE=mc\u00b2, obviously.");

    // ========== Nether stage ==========

    public static final RegistryObject<Item> SHINING_STAR = useItem("shining_star", false,
            RightClickActions.summonMeteor(),
            "\u00a7bRight click to summon a meteor on top of you",
            "\u00a7cDo not use this at your base");

    public static final RegistryObject<Item> MEDKIT_BIG = ITEMS.register("medkit_big",
            () -> new StageFoodItem(
                    new Item.Properties().food(new FoodProperties.Builder()
                            .nutrition(0).saturationMod(0f).alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 800, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 800, 4), 1.0f)
                            .build()),
                    false, 60, true,
                    "\u00a77Increases max health by 20 for 40s.",
                    "\u00a77Gains 40s of Regen III.",
                    "\u00a77Restores 32 health."));

    public static final RegistryObject<Item> BLOOD_SIGIL = useItem("blood_sigil", false,
            RightClickActions.setTimeToNight(),
            "\u00a7eSummons blood moon when used",
            "\u00a7eBloodmoon will occur at the next possible night",
            "\u00a77Consumed on use");

    public static final RegistryObject<Item> BLOODY_SACRIFICE = useItem("bloody_sacrifice", false,
            RightClickActions.setTimeToDay(),
            "\u00a7eSkip a blood moon to the next morning.",
            "\u00a77Consumed on use");

    public static final RegistryObject<Item> AWAKENED_EYE = stageGrantItem("awakened_eye",
            new Item.Properties().stacksTo(1), false, "abyssal_conquerer",
            "\u00a7eRight click to unlock game stage: \u00a76abyssal_conquerer");

    // ========== Skilled Wizard stage ==========

    public static final RegistryObject<Item> ARCANE_CRYSTAL_BALL = stageGrantItem("arcane_crystal_ball", true, "master_wizard",
            "\u00a75You only made this to prove your magical skills.",
            "\u00a7eRight click to unlock game stage: \u00a76master_wizard");

    // ========== Wielder of Infinity stage ==========

    public static final RegistryObject<Item> PIONEER_MEDAL = stageItem("pioneer_medal", false,
            "\u00a7b\u00a7oNever be satisfied about yourself.");

    public static final RegistryObject<Item> GREEDY_MEDAL = stageItem("greedy_medal", false,
            "\u00a75\u00a7oNothing else matters now.");

    public static final RegistryObject<Item> CREATIVE_SOUL = stageItem("creative_soul",
            new Item.Properties().rarity(Rarity.EPIC), true,
            "\u00a75Only a little bit of the infinite power of creativity.",
            "\u00a75You can already feel the pulse inside it.");

    // ========== Wither Slayer stage ==========

    public static final RegistryObject<Item> BRAVERY_CERTIFICATE = stageGrantItem("bravery_certificate", false, "fearless_man",
            "\u00a7e\u00a7oOnly the bravest ones shall have it.",
            "\u00a7eRight click to unlock game stage: \u00a76fearless_man");

    public static final RegistryObject<Item> ENDER_CHARM = stageGrantItem("ender_charm", false, "ender_charm",
            "\u00a73Only those who once fell into the abyss shall enter the End.",
            "\u00a7eRight click to unlock game stage: \u00a76ender_charm");

    // ========== Wyvern stage ==========

    public static final RegistryObject<Item> SOLARIUM_STAR = stageGrantItem("solarium_star", true, "descendant_of_the_sun",
            "\u00a7eRight click to unlock game stage: \u00a76descendant_of_the_sun",
            "\u00a76A new ore will spawn in the Nether");

    public static final RegistryObject<Item> SUN_TOTEM = stageItem("sun_totem", true,
            "\u00a7eShift-right-click a \u00a7cSummoning Altar\u00a7e to summon \u00a76Barako, the Sun Chief");

    public static final RegistryObject<Item> SOLAR_SEED = stageItem("solar_seed", false,
            "\u00a7eCombine with a broken solarium star to activate its real power.");

    public static final RegistryObject<Item> BROKEN_SOLARIUM_STAR = stageItem("broken_solarium_star", false,
            "\u00a7eLooks like it's broken. Fix it with a Solar Seed!");

    // ========== Loot / Reward items ==========

    public static final RegistryObject<Item> NORTH_STAR = stageItem("north_star", true);

    public static final RegistryObject<Item> OVERFLUX_CAPACITOR = stageItem("overflux_capacitor", true,
            "\u00a75\u00a7lEPIC");

    public static final RegistryObject<Item> GLIDER = stageItem("glider",
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), true,
            "\u00a77Sewn in India with ordinary cotton string,",
            "\u00a77but sewn VERY well.",
            "\u00a7bUnbreakable");

    public static final RegistryObject<Item> SWORD_OF_THE_RNG_GOD = ITEMS.register("sword_of_the_rng_god",
            () -> new SwordItem(Tiers.DIAMOND, 3, -2.4f,
                    new Item.Properties().rarity(Rarity.EPIC)) {
                @Override
                public boolean isFoil(ItemStack stack) { return true; }

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
                    list.add(Component.literal("\u00a79+2147483647 Melee Damage"));
                    list.add(Component.literal("\u00a77Sharpness 32767"));
                    list.add(Component.literal("\u00a77Unbreaking 32767"));
                    list.add(Component.literal("\u00a77Looting 32767"));
                    list.add(Component.literal("\u00a77Fire Aspect 32767"));
                    list.add(Component.literal("\u00a77Knockback 32767"));
                    list.add(Component.literal("\u00a77Smite 32767"));
                    list.add(Component.literal("\u00a77Bane of Arthropods 32767"));
                    list.add(Component.literal("\u00a7bUnbreakable"));
                    list.add(Component.literal("\u00a7dDrop chance is 0.000042069% from loot crates"));
                    list.add(Component.literal("\u00a70\u00a7oAll of above are bullshit"));
                }
            });

    public static final RegistryObject<Item> STICK_OF_KNOCKBACK = ITEMS.register("stick_of_knockback",
            () -> new SwordItem(Tiers.WOOD, 0, -2.4f,
                    new Item.Properties().rarity(Rarity.RARE)) {
                @Override public boolean isFoil(ItemStack stack) { return true; }
                @Override public ItemStack getDefaultInstance() {
                    ItemStack stack = new ItemStack(this);
                    stack.enchant(Enchantments.KNOCKBACK, 10);
                    return stack;
                }
                @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
                    list.add(Component.literal("\u00a7d\u00a7oSTAY THE FRICK AWAY FROM ME!!"));
                }
            });

    // ========== Remaining GC items (quests/rewards) ==========

    public static final RegistryObject<Item> ANCIENT_TOME = stageItem("ancient_tome", true,
            "\u00a7eYou have finally restored the artifact of the ancients.",
            "\u00a7eIt's all written in a strange language you don't understand.");
    public static final RegistryObject<Item> ANCIENT_TOME_FRAGMENT = stageItem("ancient_tome_fragment",
            new Item.Properties().stacksTo(63), false,
            "\u00a77A tiny fragment from an ancient artifact.",
            "\u00a7eDropped from mobs at high difficulties.");
    public static final RegistryObject<Item> ANCIENT_TOME_PAGE = stageItem("ancient_tome_page", false,
            "\u00a77A page of an ancient artifact.",
            "\u00a77It's partly repaired, but still not understandable.");
    public static final RegistryObject<Item> AURORA_HEART = stageItem("aurora_heart", true);
    public static final RegistryObject<Item> BAG_OF_DYES = useItem("bag_of_dyes", false,
            rollTable("containers/bag_of_dyes"),
            "\u00a7eLooks like it's filled with dyes... open it and have a look?");
    public static final RegistryObject<Item> BLACK_HOLE_REMNANT = stageItem("black_hole_remnant", false);
    public static final RegistryObject<Item> BLUEPRINT = stageItem("blueprint", false,
            "\u00a7bUsed to craft tinker's construct tool blueprints");
    public static final RegistryObject<Item> BLUEPRINT_LASER_GUN = stageGrantItem("blueprint_laser_gun", false,
            "laser_gun_blueprint",
            "\u00a7eRight click to unlock tinker's construct tool: \u00a76Laser Gun");
    public static final RegistryObject<Item> BLUEPRINT_SHURIKEN = stageGrantItem("blueprint_shuriken", false,
            "shuriken_blueprint",
            "\u00a7eRight click to unlock tinker's construct tool: \u00a76Shuriken");
    public static final RegistryObject<Item> BLUEPRINT_TACTIC = stageGrantItem("blueprint_tactic", false,
            "tactic_blueprint",
            "\u00a7eRight click to unlock tinker's construct tool: \u00a7bCleaver, Rapier, Longsword and Katana");
    public static final RegistryObject<Item> BLUEPRINT_WAND = stageGrantItem("blueprint_wand", false,
            "wand_blueprint",
            "\u00a7eRight click to unlock tinker's construct tool: \u00a7dSceptre");
    public static final RegistryObject<Item> BOUNTY_HUNTER_MEDAL = stageItem("bounty_hunter_medal", false,
            "\u00a77Looks awesome.");
    public static final RegistryObject<Item> BOUNTY_HUNTER_MEDAL_BRONZE = stageItem("bounty_hunter_medal_bronze", false,
            "\u00a7eCan be obtained from bounties.");
    public static final RegistryObject<Item> BOUNTY_HUNTER_MEDAL_EMERALD = useItemPersistent(
            "bounty_hunter_medal_emerald", new Item.Properties(), false,
            RightClickActions.bountyMerchants(5.0),
            "\u00a7bThis medal looks very special!",
            "\u00a7eRight click to turn nearby villagers into Bounty Merchants.",
            "\u00a77Has a chance to be rewarded by bounties.");
    public static final RegistryObject<Item> BOUNTY_HUNTER_MEDAL_SILVER = stageItem("bounty_hunter_medal_silver", false);
    public static final RegistryObject<Item> CATALYST_STAR = stageItem("catalyst_star", true,
            "\u00a7eA super efficient catalyst.");
    public static final RegistryObject<Item> CITY_DEFENDER_MEDAL = stageItem("city_defender_medal", false,
            "\u00a77\u00a7oThank you... for what you did to protect this city.");
    public static final RegistryObject<Item> COOKED_HUMAN_MEAT = ITEMS.register("cooked_human_meat",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.45f).build()),
                    false, 32, false));
    public static final RegistryObject<Item> CRUDE_HATCHET = ITEMS.register("crude_hatchet",
            () -> new net.minecraft.world.item.AxeItem(Tiers.WOOD, 6.0f, -3.2f, new Item.Properties()) {
                @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
                    list.add(Component.literal("\u00a77...It works!"));
                }
            });
    public static final RegistryObject<Item> CRYONIC_ARTIFACT = stageItem("cryonic_artifact",
            new Item.Properties().stacksTo(1), true,
            "\u00a73It is cold enough to freeze the sun.");
    public static final RegistryObject<Item> DEATH_COUNTER = useItemPersistent("death_counter",
            new Item.Properties(), false,
            RightClickActions.showDeathLeaderboard(),
            "\u00a77\u00a7oRight click to show the death leaderboard.");
    public static final RegistryObject<Item> DELIVERY_ORDER = useItem("delivery_order", false,
            rollTable("containers/delivery_order"),
            "\u00a7bWarps a load of goods to you with future technology!",
            "\u00a76Right click to use");
    public static final RegistryObject<Item> DIFFICULTY_SYNCER = stageItem("difficulty_syncer",
            new Item.Properties().stacksTo(1), true,
            "\u00a7dWhen used, set your difficulty to the supposed difficulty at your game stage.",
            "\u00a76Used to fix some bugs",
            "\u00a7eDoes not consume",
            "\u00a77\u00a7m/syncdifficulty does this too but this is for people who can't remember commands.");
    public static final RegistryObject<Item> ELYSIA_PROJECT_LORE = ITEMS.register("elysia_project_lore",
            () -> new BookOpenItem(new Item.Properties().stacksTo(1), false, "the_elysia_project"));
    public static final RegistryObject<Item> EMERGENCY_BUTTON = useItem("emergency_button",
            new Item.Properties().stacksTo(1), false,
            RightClickActions.clearEntities(),
            "\u00a74EMERGENCY USE ONLY",
            "\u00a7cRight click to clear all entities besides players.",
            "\u00a7cConsumed on use.");
    public static final RegistryObject<Item> EXECUTOR_TERMINAL = ITEMS.register("executor_terminal",
            () -> new ExecutorTerminalItem(new Item.Properties().stacksTo(1), false,
                    "\u00a74\u00a7oIt's up to you to decide the fate of this world.",
                    "\u00a7c\u00a7oRight click to terminate the Elysian Project and the current simulation.",
                    "\u00a7c\u00a7lTHIS CANNOT BE UNDONE."));
    public static final RegistryObject<Item> EXPERIENCE_INGOT = useItem("experience_ingot", false,
            RightClickActions.grantXp(9),
            "\u00a77Used to store XP",
            "\u00a7aRight click to consume and gain XP");
    public static final RegistryObject<Item> EXPERIENCE_NUGGET = useItem("experience_nugget", false,
            RightClickActions.grantXp(1),
            "\u00a77Used to store XP",
            "\u00a7aRight click to consume and gain XP");
    public static final RegistryObject<Item> EXPERIENCE_TRANSPORTER = stageItem("experience_transporter",
            new Item.Properties().stacksTo(1), true,
            "\u00a7eTransports XP between tools via crafting.");
    public static final RegistryObject<Item> EXPERIMENT_NOTE = stageItem("experiment_note", false,
            "\u00a77A broken experiment log, the text on it can't be seen clearly.");
    public static final RegistryObject<Item> FOOD_BAG = useItem("food_bag", false,
            rollTable("containers/food_bag"),
            "\u00a7eFilled with delicious food!",
            "\u00a77Right click to open");
    public static final RegistryObject<Item> FURNITURE_CRATE = useItem("furniture_crate", false,
            rollTable("containers/furniture_crate"),
            "\u00a7bFilled with furnitures and decorations!",
            "\u00a77Right click to open");
    public static final RegistryObject<Item> GIFT = useItem("gift", false,
            rollTable("containers/gift"),
            "\u00a7aHo ho ho!",
            "\u00a7eRight click to open");
    public static final RegistryObject<Item> GRASS_STRING = stageItem("grass_string", false,
            "\u00a7a100% Natural, just too easy to break.");
    public static final RegistryObject<Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new BookOpenItem(new Item.Properties().stacksTo(1), false, "greedycraft_guide_book",
                    "\u00a76There's only one line of text on it:",
                    "\u00a7b\"Give up, before you become bald.\""));
    public static final RegistryObject<Item> HUAJI = ITEMS.register("huaji",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10).saturationMod(20f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 800, 3), 1f)
                    .build()),
                    false, 32, false,
                    "\u00a7eGives you health boost."));
    public static final RegistryObject<Item> INFINITY_STONE = stageItem("infinity_stone",
            new Item.Properties().stacksTo(1), true,
            "\u00a75For the worthy...");
    public static final RegistryObject<Item> ITEM_PURGER = useItemPersistent("item_purger",
            new Item.Properties().stacksTo(1), false,
            RightClickActions.clearGroundItems(),
            "\u00a7bRight click to clear all ground items.");
    public static final RegistryObject<Item> LOLI_LOLIPOP = ITEMS.register("loli_lolipop",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10).saturationMod(0.6f).alwaysEat().build()),
                    false, 32, false,
                    "\u00a76Used to tame or lure little maids."));
    public static final RegistryObject<Item> LUCKY_CLOVER = stageItem("lucky_clover", false,
            "\u00a7bThe symbol of luck.",
            "\u00a77Can be obtained by punching tall grass.");
    public static final RegistryObject<Item> MEDKIT_SMALL = ITEMS.register("medkit_small",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(0).saturationMod(0f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 1), 1f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 0), 1f)
                    .build()),
                    false, 40, true,
                    "\u00a77Restores 16 health.",
                    "\u00a77Gains 20s of Regen I."));
    public static final RegistryObject<Item> ONE_PUNCH = ITEMS.register("one_punch",
            () -> new SwordItem(Tiers.DIAMOND, 1330, -2.4f,
                    new Item.Properties().stacksTo(1).durability(1).rarity(Rarity.EPIC)) {
                @Override public boolean isFoil(ItemStack stack) { return true; }
                @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
                    list.add(Component.literal("\u00a7c\u00a7oHuman beings are strong because we can change ourselves."));
                }
            });
    public static final RegistryObject<Item> PASSPORT = useItemSneakPair("passport",
            new Item.Properties(), false, false,
            RightClickActions.unlockAllStages(),
            RightClickActions.lockAllStages(),
            "\u00a76Right click to unlock all game stages",
            "\u00a76Shift-right-click to lock all game stages",
            "\u00a7bCreative mode only");
    public static final RegistryObject<Item> PEARL_OF_KNOWLEDGE = useItem("pearl_of_knowledge", true,
            RightClickActions.grantXp(59049),
            "\u00a7eStores a lot of XP.",
            "\u00a7aRight click to use.");
    public static final RegistryObject<Item> PEBBLE = stageItem("pebble", false,
            "\u00a77White and smooth.");
    public static final RegistryObject<Item> PINECONE = ITEMS.register("pinecone",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(2).saturationMod(0.15f)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0), 0.33f)
                    .build()),
                    false, 32, false));
    public static final RegistryObject<Item> PLANT_FIBRE = stageItem("plant_fibre", false);
    public static final RegistryObject<Item> PLATE_OF_HONOR = stageItem("plate_of_honor", false,
            "\u00a7eCombine with tools or armors to apply a modifier.");
    public static final RegistryObject<Item> POOP = ITEMS.register("poop",
            () -> new StageFoodItem(new Item.Properties().stacksTo(63).food(new FoodProperties.Builder()
                    .nutrition(1).saturationMod(1f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 1200, 127), 1f)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 300, 3), 1f)
                    .build()),
                    false, 32, false,
                    "\u00a71Ewwwww stinky!",
                    "\u00a78It's fuel (but why)"));
    public static final RegistryObject<Item> POOPBURGER = ITEMS.register("poopburger",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20).saturationMod(0.5f).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 10), 1f)
                    .build()),
                    false, 32, false,
                    "\u00a7b????????"));
    public static final RegistryObject<Item> PURIFYING_DUST = useItem("purifying_dust", false,
            RightClickActions.pureDaisyBurst(7),
            "\u00a72When used, converts nearby blocks like a pure daisy.");
    public static final RegistryObject<Item> RAW_HUMAN_MEAT = ITEMS.register("raw_human_meat",
            () -> new StageFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(2).saturationMod(0.75f)
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 160, 0), 0.5f)
                    .build()),
                    false, 32, false));
    public static final RegistryObject<Item> RED_PACKET = useItem("red_packet", false,
            rollTable("containers/gift"),
            "\u00a7eHappy lunar new year!",
            "\u00a76Right click to open");
    public static final RegistryObject<Item> RESPAWN_ANCHOR = useItem("respawn_anchor", true,
            RightClickActions.setSpawn(),
            "\u00a7eRight click to set current location as spawn point.");
    // Jukebox discs — music by Mangonade. Craftable from any music disc +
    // a marker item (stick/dirt), see data/soa_additions/recipes/music_disc_*.json.
    public static final RegistryObject<Item> MUSIC_DISC_STENCIL = ITEMS.register("music_disc_stencil",
            () -> new net.minecraft.world.item.RecordItem(14,
                    com.soul.soa_additions.sound.ModSounds.MUSIC_DISC_STENCIL,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 129));
    public static final RegistryObject<Item> MUSIC_DISC_SUNRISE = ITEMS.register("music_disc_sunrise",
            () -> new net.minecraft.world.item.RecordItem(15,
                    com.soul.soa_additions.sound.ModSounds.MUSIC_DISC_SUNRISE,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 192));
    public static final RegistryObject<Item> REWARD_TICKET_COMMON = rewardTicket("reward_ticket_common", false,
            "containers/loot_crate_common",
            "\u00a77Right click to get a \u00a7aCOMMON\u00a7r\u00a77 Loot Crate");
    public static final RegistryObject<Item> REWARD_TICKET_EPIC = rewardTicket("reward_ticket_epic", false,
            "containers/loot_crate_epic",
            "\u00a77Right click to get an \u00a7dEPIC\u00a7r\u00a77 Loot Crate");
    public static final RegistryObject<Item> REWARD_TICKET_LEGENDARY = rewardTicket("reward_ticket_legendary", true,
            "containers/loot_crate_legendary",
            "\u00a77Right click to get a \u00a76LEGENDARY\u00a7r\u00a77 Loot Crate");
    public static final RegistryObject<Item> REWARD_TICKET_RARE = rewardTicket("reward_ticket_rare", false,
            "containers/loot_crate_rare",
            "\u00a77Right click to get a \u00a79RARE\u00a7r\u00a77 Loot Crate");
    public static final RegistryObject<Item> ROYAL_GEL = stageItem("royal_gel", false,
            "\u00a7eBoth tasty and flammable!");
    public static final RegistryObject<Item> SAND_OF_TIME = stageItem("sand_of_time", true,
            "\u00a7bThese sand flows upwards when in a hourglass.");
    public static final RegistryObject<Item> SKILL_RESET_SCROLL = stageItem("skill_reset_scroll",
            new Item.Properties().stacksTo(1), true,
            "\u00a76Resets all your skills.",
            "\u00a7cNo XP will be given back.");
    public static final RegistryObject<Item> SLIME_CROWN = useItem("slime_crown", false,
            RightClickActions.summonSlimeKing(),
            "\u00a7eSummons the Slime God",
            "\u00a76Right click to use");
    public static final RegistryObject<Item> STAINLESS_STEEL_BALL = stageItem("stainless_steel_ball", false);
    public static final RegistryObject<Item> STRANGE_MATTER = stageItem("strange_matter", false,
            "\u00a72The most dangerous substance in the universe.");
    public static final RegistryObject<Item> SUNNY_DOLL = useItem("sunny_doll", false,
            RightClickActions.clearWeather(),
            "\u00a7bHey, It'll clear up now~",
            "\u00a7eSets the weather to sunny when used.");
    public static final RegistryObject<Item> TIME_FRAGMENT = stageItem("time_fragment",
            new Item.Properties().stacksTo(63), true,
            "\u00a7bSeems to be debris from another dimension.");
    public static final RegistryObject<Item> TIME_SHARD = stageItem("time_shard", true);
    public static final RegistryObject<Item> TITANIUM_NUGGET = stageItem("titanium_nugget", false);
    /** Locked loot chest found in battle towers \u2014 useless until combined with a
     *  {@link #TOWER_CHEST_KEY} in a crafting grid (see recipes/tower_chest_unlocked.json). */
    public static final RegistryObject<Item> TOWER_CHEST = stageItem("tower_chest", false,
            "\u00a7eSeems like it needs a key to open.");
    public static final RegistryObject<Item> TOWER_CHEST_KEY = stageItem("tower_chest_key",
            new Item.Properties().stacksTo(1), false,
            "\u00a7eUnlock Tower Chests by putting them in crafting table!");
    public static final RegistryObject<Item> TOWER_CHEST_UNLOCKED = useItem("tower_chest_unlocked", false,
            RightClickActions.rollTreasureChest(1, 10),
            "\u00a76Right click to open");
    public static final RegistryObject<Item> TRUE_EYE_OF_ENDER = stageItem("true_eye_of_ender",
            new Item.Properties().stacksTo(1), true,
            "\u00a7ePut it in inventory to prevent yourself from being teleported by endermen and endermites.");
    public static final RegistryObject<Item> TWILIGHT_GEM = stageItem("twilight_gem", true,
            "\u00a7bA gem that gives out a mystical glow.",
            "\u00a76Used to activate the Twilight Forest portal.");
    public static final RegistryObject<Item> TWILIGHT_SHIELD = stageGrantItem("twilight_shield", true, "twilight_shield",
            "\u00a76Can resist the heat from hell.",
            "\u00a7eRight click to unlock game stage: \u00a76twilight_shield");

    // GreedyCraft 'Wither Bone' (was netherex:wither_bone; tconstruct:materials:17
    // was the 'Necrotic Bone' equivalent). Drops from wither skeletons (see
    // data/soa_additions/loot_modifiers/wither_bone_drop.json) and is intended as
    // a Smithery modifier part (behavior TBD in SoaSmitheryModifiers.java).
    public static final RegistryObject<Item> WITHER_BONE = stageItem("wither_bone", false,
            "\u00a78A bone steeped in wither energy.",
            "\u00a77Drops from Wither Skeletons.");

    // ========== Nyx items moved to NyxItems.java (nyx: namespace) ==========

    // ========== Ore Sight potions (custom items for dynamic per-block names) ==========
    // Three bottle forms; the same item handles base + long durations (the
    // Potion registry entry on the stack controls effect timing). Brewing
    // recipes set the OreSightBlock NBT key so getName() composes a
    // translation like "Gold Ore Sight Potion".

    public static final RegistryObject<Item> ORE_SIGHT_POTION = ITEMS.register("ore_sight_potion",
            () -> new com.soul.soa_additions.oresight.OreSightPotionItem(
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ORE_SIGHT_SPLASH_POTION = ITEMS.register("ore_sight_splash_potion",
            () -> new com.soul.soa_additions.oresight.OreSightPotionItem.Splash(
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ORE_SIGHT_LINGERING_POTION = ITEMS.register("ore_sight_lingering_potion",
            () -> new com.soul.soa_additions.oresight.OreSightPotionItem.Lingering(
                    new Item.Properties().stacksTo(1)));

    // ========== Private constructor ==========

    private ModItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    // ========== Helper methods ==========

    private static RegistryObject<Item> registerRareIngot(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    }

    private static RegistryObject<Item> stageItem(String name, boolean foil, String... tooltip) {
        return ITEMS.register(name, () -> new StageItem(new Item.Properties(), foil, tooltip));
    }

    private static RegistryObject<Item> stageItem(String name, Item.Properties props, boolean foil, String... tooltip) {
        return ITEMS.register(name, () -> new StageItem(props, foil, tooltip));
    }

    /** Stage-granting variant: right-clicking adds {@code stageToGrant} via
     *  GameStages and consumes one from the stack. */
    private static RegistryObject<Item> stageGrantItem(String name, boolean foil, String stageToGrant, String... tooltip) {
        return ITEMS.register(name, () -> new StageItem(new Item.Properties(), foil, stageToGrant, tooltip));
    }

    private static RegistryObject<Item> stageGrantItem(String name, Item.Properties props, boolean foil, String stageToGrant, String... tooltip) {
        return ITEMS.register(name, () -> new StageItem(props, foil, stageToGrant, tooltip));
    }

    private static RegistryObject<Item> rewardTicket(String name, boolean foil, String lootName, String... tooltip) {
        ResourceLocation lootId = new ResourceLocation(SoaAdditions.MODID, lootName);
        return ITEMS.register(name, () -> new RewardTicketItem(new Item.Properties(), foil, lootId, tooltip));
    }

    /** Register an item with a right-click action ported from a GreedyCraft
     *  Additions-mod effect JSON. Consumes one on successful use. */
    private static RegistryObject<Item> useItem(String name, boolean foil,
                                                UseActionItem.UseAction action, String... tooltip) {
        return ITEMS.register(name, () -> new UseActionItem(new Item.Properties(), foil, true, action, tooltip));
    }

    private static RegistryObject<Item> useItem(String name, Item.Properties props, boolean foil,
                                                UseActionItem.UseAction action, String... tooltip) {
        return ITEMS.register(name, () -> new UseActionItem(props, foil, true, action, tooltip));
    }

    /** Like {@link #useItem} but does not consume the stack — for reusable
     *  trigger items (true_blood_sigil, item_purger). */
    private static RegistryObject<Item> useItemPersistent(String name, Item.Properties props, boolean foil,
                                                          UseActionItem.UseAction action, String... tooltip) {
        return ITEMS.register(name, () -> new UseActionItem(props, foil, false, action, tooltip));
    }

    /** Right-click / sneak-right-click pair — the 1.20.1 flavour of
     *  GreedyCraft's paired {@code item_right_click} / {@code item_left_click}
     *  effect bundles. */
    private static RegistryObject<Item> useItemSneakPair(String name, Item.Properties props, boolean foil,
                                                         boolean consumeOnUse,
                                                         UseActionItem.UseAction rightClick,
                                                         UseActionItem.UseAction sneakRightClick,
                                                         String... tooltip) {
        return ITEMS.register(name, () -> new UseActionItem(props, foil, consumeOnUse,
                rightClick, sneakRightClick, tooltip));
    }

    /** Build a loot-table-rolling action from a path under
     *  {@code data/soa_additions/loot_tables/}. */
    private static UseActionItem.UseAction rollTable(String path) {
        return RightClickActions.rollLootTableAt(new ResourceLocation(SoaAdditions.MODID, path));
    }
}
