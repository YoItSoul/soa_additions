package com.soul.soa_additions.potion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SoA Brewing — registers all brewing-stand recipes that convert
 * Awkward (and SoA-base) potions into the SoA-specific potion variants
 * declared in {@link SoaBrewingPotions}.
 *
 * <p>Forge 1.20.1 doesn't expose {@code RegisterBrewingRecipesEvent}
 * (added in 1.20.4), and vanilla 1.20.1 brewing isn't recipe-driven, so
 * we register via Forge's {@link BrewingRecipeRegistry} from the mod's
 * {@code FMLCommonSetupEvent.enqueueWork(...)} block. The previous
 * approach (KubeJS {@code event.recipes.minecraft.brewing(...)}) didn't
 * work — that schema doesn't exist in KubeJS 6 for 1.20.1.</p>
 *
 * <p>Each logical rule registers three {@link BrewingRecipe} entries —
 * one per bottle item ({@code POTION}, {@code SPLASH_POTION},
 * {@code LINGERING_POTION}) — using {@link StrictNBTIngredient} on the
 * input so the bottle's potion NBT must match exactly.</p>
 *
 * <p>Reagent translation table (preserved from GC's {@code brewing.zs}):
 * see commit history / archived KubeJS port for rationale.</p>
 */
public final class SoaBrewing {

    private static final Logger LOGGER = LoggerFactory.getLogger("SoaBrewing");

    private static final Item[] BOTTLES = { Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION };

    private static int registered;
    private static int skipped;

    private SoaBrewing() {}

    /**
     * Registers every SoA brewing recipe. Must be invoked from
     * {@code FMLCommonSetupEvent.enqueueWork(...)} so the Potion and Item
     * registries are fully populated and we're on the main thread.
     */
    public static void registerBrewing() {
        registered = 0;
        skipped = 0;

        // === Awkward → base ===
        addItem("minecraft:awkward", Items.GOLDEN_APPLE,                  SoaBrewingPotions.HEALTH_BOOST);
        addItem("minecraft:awkward", "soa_additions:ancient_tome_page",   SoaBrewingPotions.REPAIR);
        addItem("minecraft:awkward", Items.POPPED_CHORUS_FRUIT,           SoaBrewingPotions.FLIGHT);
        addTag( "minecraft:awkward", "forge:ingots/iron",                 SoaBrewingPotions.IRON_SKIN);
        addTag( "minecraft:awkward", "forge:gems/diamond",                SoaBrewingPotions.DIAMOND_SKIN);
        addItem("minecraft:awkward", Items.PISTON,                        SoaBrewingPotions.REACH);
        addItem("minecraft:awkward", Items.CLOCK,                         SoaBrewingPotions.EXTENSION);
        addItem("minecraft:awkward", "botania:pure_daisy",                SoaBrewingPotions.PURITY);
        addItem("minecraft:awkward", Items.DIRT,                          SoaBrewingPotions.STEP_UP);
        addItem("minecraft:awkward", "tconstruct:manyullyn_ingot",        SoaBrewingPotions.MAGIC_SHIELD);
        addItem("minecraft:awkward", Items.LEATHER,                       SoaBrewingPotions.ARCHERY);
        addTag( "minecraft:awkward", "forge:ingots/lead",                 SoaBrewingPotions.SOLID_CORE);
        addTag( "minecraft:awkward", "forge:storage_blocks/lead",         SoaBrewingPotions.WEIGHT);
        addItem("minecraft:awkward", "draconicevolution:draconium_ingot", SoaBrewingPotions.LIGHTNING);
        addItem("minecraft:awkward", Items.FIRE_CHARGE,                   SoaBrewingPotions.FIRE);
        addItem("minecraft:awkward", Items.TNT,                           SoaBrewingPotions.EXPLODE);
        addItem("minecraft:awkward", Items.FIREWORK_ROCKET,               SoaBrewingPotions.LAUNCH);
        addItem("minecraft:awkward", "soa_additions:ancient_tome",        SoaBrewingPotions.REVIVAL);
        addItem("minecraft:awkward", Items.ENDER_PEARL,                   SoaBrewingPotions.TELEPORT);

        // === Long variants (redstone) ===
        addPotion(SoaBrewingPotions.HEALTH_BOOST,  Items.REDSTONE, SoaBrewingPotions.LONG_HEALTH_BOOST);
        addPotion(SoaBrewingPotions.REPAIR,        Items.REDSTONE, SoaBrewingPotions.LONG_REPAIR);
        addPotion(SoaBrewingPotions.FLIGHT,        Items.REDSTONE, SoaBrewingPotions.LONG_FLIGHT);
        addPotion(SoaBrewingPotions.IRON_SKIN,     Items.REDSTONE, SoaBrewingPotions.LONG_IRON_SKIN);
        addPotion(SoaBrewingPotions.DIAMOND_SKIN,  Items.REDSTONE, SoaBrewingPotions.LONG_DIAMOND_SKIN);
        addPotion(SoaBrewingPotions.REACH,         Items.REDSTONE, SoaBrewingPotions.LONG_REACH);
        addPotion(SoaBrewingPotions.EXTENSION,     Items.REDSTONE, SoaBrewingPotions.LONG_EXTENSION);
        addPotion(SoaBrewingPotions.PURITY,        Items.REDSTONE, SoaBrewingPotions.LONG_PURITY);
        addPotion(SoaBrewingPotions.STEP_UP,       Items.REDSTONE, SoaBrewingPotions.LONG_STEP_UP);
        addPotion(SoaBrewingPotions.MAGIC_SHIELD,  Items.REDSTONE, SoaBrewingPotions.LONG_MAGIC_SHIELD);
        addPotion(SoaBrewingPotions.ARCHERY,       Items.REDSTONE, SoaBrewingPotions.LONG_ARCHERY);
        addPotion(SoaBrewingPotions.SOLID_CORE,    Items.REDSTONE, SoaBrewingPotions.LONG_SOLID_CORE);
        addPotion(SoaBrewingPotions.WEIGHT,        Items.REDSTONE, SoaBrewingPotions.LONG_WEIGHT);
        addPotion(SoaBrewingPotions.EXPLODE,       Items.REDSTONE, SoaBrewingPotions.LONG_EXPLODE);
        addPotion(SoaBrewingPotions.REVIVAL,       Items.REDSTONE, SoaBrewingPotions.LONG_REVIVAL);
        addPotion(SoaBrewingPotions.TELEPORT,      Items.REDSTONE, SoaBrewingPotions.LONG_TELEPORT);

        // === Strong variants (glowstone) ===
        addPotion(SoaBrewingPotions.HEALTH_BOOST,  Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_HEALTH_BOOST);
        addPotion(SoaBrewingPotions.REPAIR,        Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_REPAIR);
        addPotion(SoaBrewingPotions.IRON_SKIN,     Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_IRON_SKIN);
        addPotion(SoaBrewingPotions.DIAMOND_SKIN,  Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_DIAMOND_SKIN);
        addPotion(SoaBrewingPotions.REACH,         Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_REACH);
        addPotion(SoaBrewingPotions.EXTENSION,     Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_EXTENSION);
        addPotion(SoaBrewingPotions.STEP_UP,       Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_STEP_UP);
        addPotion(SoaBrewingPotions.MAGIC_SHIELD,  Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_MAGIC_SHIELD);
        addPotion(SoaBrewingPotions.ARCHERY,       Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_ARCHERY);
        addPotion(SoaBrewingPotions.WEIGHT,        Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_WEIGHT);
        addPotion(SoaBrewingPotions.FIRE,          Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_FIRE);
        addPotion(SoaBrewingPotions.EXPLODE,       Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_EXPLODE);
        addPotion(SoaBrewingPotions.LAUNCH,        Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_LAUNCH);
        addPotion(SoaBrewingPotions.REVIVAL,       Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_REVIVAL);
        addPotion(SoaBrewingPotions.TELEPORT,      Items.GLOWSTONE_DUST, SoaBrewingPotions.STRONG_TELEPORT);

        LOGGER.info("[SoaBrewing] registered {} brewing recipes ({} bottle types × rules), {} rules skipped",
                registered, BOTTLES.length, skipped);
    }

    // ---- helpers -----------------------------------------------------

    /** Awkward (or other vanilla) potion id + Forge {@link Item} reagent + SoA potion output. */
    private static void addItem(String inputPotionId, Item reagent, RegistryObject<Potion> output) {
        Potion in = lookupPotion(inputPotionId);
        if (in == null) { warnSkip(inputPotionId, reagent.toString(), output); return; }
        addAllBottles(in, Ingredient.of(reagent), output.get());
    }

    /** Awkward potion id + cross-mod reagent item id (e.g. {@code soa_additions:ancient_tome_page}) + SoA potion output. */
    private static void addItem(String inputPotionId, String reagentId, RegistryObject<Potion> output) {
        Potion in = lookupPotion(inputPotionId);
        Item reagent = ForgeRegistries.ITEMS.getValue(new ResourceLocation(reagentId));
        if (in == null || reagent == null || reagent == Items.AIR) {
            warnSkip(inputPotionId, reagentId, output);
            return;
        }
        addAllBottles(in, Ingredient.of(reagent), output.get());
    }

    /** SoA potion → {@link Item} reagent → SoA potion output (used for long/strong chains). */
    private static void addPotion(RegistryObject<Potion> input, Item reagent, RegistryObject<Potion> output) {
        addAllBottles(input.get(), Ingredient.of(reagent), output.get());
    }

    /** Awkward potion id + tag reagent + SoA potion output. */
    private static void addTag(String inputPotionId, String tagId, RegistryObject<Potion> output) {
        Potion in = lookupPotion(inputPotionId);
        if (in == null) { warnSkip(inputPotionId, "#" + tagId, output); return; }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(tagId));
        addAllBottles(in, Ingredient.of(tag), output.get());
    }

    /** Register one BrewingRecipe per bottle type for the given (input potion, reagent, output potion). */
    private static void addAllBottles(Potion inputPotion, Ingredient reagent, Potion outputPotion) {
        for (Item bottle : BOTTLES) {
            ItemStack inputStack  = PotionUtils.setPotion(new ItemStack(bottle), inputPotion);
            ItemStack outputStack = PotionUtils.setPotion(new ItemStack(bottle), outputPotion);
            BrewingRecipeRegistry.addRecipe(new BrewingRecipe(
                    StrictNBTIngredient.of(inputStack),
                    reagent,
                    outputStack));
            registered++;
        }
    }

    private static Potion lookupPotion(String id) {
        return ForgeRegistries.POTIONS.getValue(new ResourceLocation(id));
    }

    private static void warnSkip(String input, String reagent, RegistryObject<Potion> output) {
        LOGGER.warn("[SoaBrewing] skipped {} + {} → {}: input potion or reagent not registered",
                input, reagent, output.getId());
        skipped++;
    }
}
