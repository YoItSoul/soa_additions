package com.soul.soa_additions.potion;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * SoA Brewing Potions — wraps each {@link SoaPotions} effect into a
 * {@link Potion} entry that the brewing stand can produce. Each effect
 * gets up to three Potion variants:
 *
 * <ul>
 *   <li>{@code <name>}        — base (3 minutes default duration)</li>
 *   <li>{@code long_<name>}   — extended (8 min, redstone reagent)</li>
 *   <li>{@code strong_<name>} — amplified (1.5 min, glowstone reagent)</li>
 * </ul>
 *
 * Effects whose game logic depends on amplifier scaling (HealthBoost,
 * IronSkin, DiamondSkin, MagicShield) get all three variants. Effects
 * that don't use amplifier (Repair, Flight, Purity, Lightning, etc.)
 * register only base + long since strong has no extra payoff.
 */
public final class SoaBrewingPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, SoaAdditions.MODID);

    private static final int BASE_TICKS   = 3 * 60 * 20;   // 3 minutes
    private static final int LONG_TICKS   = 8 * 60 * 20;   // 8 minutes
    private static final int STRONG_TICKS = 90 * 20;        // 1.5 minutes

    // Effects with three variants (amplifier scales their behavior or
    // attribute value).
    public static final RegistryObject<Potion> HEALTH_BOOST        = base("health_boost", SoaPotions.HEALTH_BOOST);
    public static final RegistryObject<Potion> LONG_HEALTH_BOOST   = lng("long_health_boost", SoaPotions.HEALTH_BOOST);
    public static final RegistryObject<Potion> STRONG_HEALTH_BOOST = strong("strong_health_boost", SoaPotions.HEALTH_BOOST);

    public static final RegistryObject<Potion> REPAIR              = base("repair", SoaPotions.REPAIR);
    public static final RegistryObject<Potion> LONG_REPAIR         = lng("long_repair", SoaPotions.REPAIR);
    public static final RegistryObject<Potion> STRONG_REPAIR       = strong("strong_repair", SoaPotions.REPAIR);

    public static final RegistryObject<Potion> FLIGHT              = base("flight", SoaPotions.FLIGHT);
    public static final RegistryObject<Potion> LONG_FLIGHT         = lng("long_flight", SoaPotions.FLIGHT);

    public static final RegistryObject<Potion> IRON_SKIN           = base("iron_skin", SoaPotions.IRON_SKIN);
    public static final RegistryObject<Potion> LONG_IRON_SKIN      = lng("long_iron_skin", SoaPotions.IRON_SKIN);
    public static final RegistryObject<Potion> STRONG_IRON_SKIN    = strong("strong_iron_skin", SoaPotions.IRON_SKIN);

    public static final RegistryObject<Potion> DIAMOND_SKIN        = base("diamond_skin", SoaPotions.DIAMOND_SKIN);
    public static final RegistryObject<Potion> LONG_DIAMOND_SKIN   = lng("long_diamond_skin", SoaPotions.DIAMOND_SKIN);
    public static final RegistryObject<Potion> STRONG_DIAMOND_SKIN = strong("strong_diamond_skin", SoaPotions.DIAMOND_SKIN);

    public static final RegistryObject<Potion> REACH               = base("reach", SoaPotions.REACH);
    public static final RegistryObject<Potion> LONG_REACH          = lng("long_reach", SoaPotions.REACH);
    public static final RegistryObject<Potion> STRONG_REACH        = strong("strong_reach", SoaPotions.REACH);

    public static final RegistryObject<Potion> EXTENSION           = base("extension", SoaPotions.EXTENSION);
    public static final RegistryObject<Potion> LONG_EXTENSION      = lng("long_extension", SoaPotions.EXTENSION);
    public static final RegistryObject<Potion> STRONG_EXTENSION    = strong("strong_extension", SoaPotions.EXTENSION);

    public static final RegistryObject<Potion> PURITY              = base("purity", SoaPotions.PURITY);
    public static final RegistryObject<Potion> LONG_PURITY         = lng("long_purity", SoaPotions.PURITY);

    public static final RegistryObject<Potion> STEP_UP             = base("step_up", SoaPotions.STEP_UP);
    public static final RegistryObject<Potion> LONG_STEP_UP        = lng("long_step_up", SoaPotions.STEP_UP);
    public static final RegistryObject<Potion> STRONG_STEP_UP      = strong("strong_step_up", SoaPotions.STEP_UP);

    public static final RegistryObject<Potion> MAGIC_SHIELD        = base("magic_shield", SoaPotions.MAGIC_SHIELD);
    public static final RegistryObject<Potion> LONG_MAGIC_SHIELD   = lng("long_magic_shield", SoaPotions.MAGIC_SHIELD);
    public static final RegistryObject<Potion> STRONG_MAGIC_SHIELD = strong("strong_magic_shield", SoaPotions.MAGIC_SHIELD);

    public static final RegistryObject<Potion> ARCHERY             = base("archery", SoaPotions.ARCHERY);
    public static final RegistryObject<Potion> LONG_ARCHERY        = lng("long_archery", SoaPotions.ARCHERY);
    public static final RegistryObject<Potion> STRONG_ARCHERY      = strong("strong_archery", SoaPotions.ARCHERY);

    public static final RegistryObject<Potion> SOLID_CORE          = base("solid_core", SoaPotions.SOLID_CORE);
    public static final RegistryObject<Potion> LONG_SOLID_CORE     = lng("long_solid_core", SoaPotions.SOLID_CORE);

    public static final RegistryObject<Potion> WEIGHT              = base("weight", SoaPotions.WEIGHT);
    public static final RegistryObject<Potion> LONG_WEIGHT         = lng("long_weight", SoaPotions.WEIGHT);
    public static final RegistryObject<Potion> STRONG_WEIGHT       = strong("strong_weight", SoaPotions.WEIGHT);

    public static final RegistryObject<Potion> LIGHTNING           = base("lightning", SoaPotions.LIGHTNING);

    public static final RegistryObject<Potion> FIRE                = base("fire", SoaPotions.FIRE);
    public static final RegistryObject<Potion> STRONG_FIRE         = strong("strong_fire", SoaPotions.FIRE);

    public static final RegistryObject<Potion> EXPLODE             = base("explode", SoaPotions.EXPLODE);
    public static final RegistryObject<Potion> LONG_EXPLODE        = lng("long_explode", SoaPotions.EXPLODE);
    public static final RegistryObject<Potion> STRONG_EXPLODE      = strong("strong_explode", SoaPotions.EXPLODE);

    public static final RegistryObject<Potion> LAUNCH              = base("launch", SoaPotions.LAUNCH);
    public static final RegistryObject<Potion> STRONG_LAUNCH       = strong("strong_launch", SoaPotions.LAUNCH);

    public static final RegistryObject<Potion> REVIVAL             = base("revival", SoaPotions.REVIVAL);
    public static final RegistryObject<Potion> LONG_REVIVAL        = lng("long_revival", SoaPotions.REVIVAL);
    public static final RegistryObject<Potion> STRONG_REVIVAL      = strong("strong_revival", SoaPotions.REVIVAL);

    public static final RegistryObject<Potion> TELEPORT            = base("teleport", SoaPotions.TELEPORT);
    public static final RegistryObject<Potion> LONG_TELEPORT       = lng("long_teleport", SoaPotions.TELEPORT);
    public static final RegistryObject<Potion> STRONG_TELEPORT     = strong("strong_teleport", SoaPotions.TELEPORT);

    // Ore-sight system: a no-effect "mutagenic" intermediate (water + thaumon:mutagen)
    // and the ore-sight base/long potions. The actual targeted ore is encoded on the
    // Potion ItemStack's NBT under the "OreSight" tag (set by the brewing recipe);
    // the Potion registry entry is the same regardless of which ore is being tracked.
    public static final RegistryObject<Potion> MUTAGENIC           = POTIONS.register("mutagenic",
            () -> new Potion("mutagenic"));
    public static final RegistryObject<Potion> ORE_SIGHT           = base("ore_sight", SoaPotions.ORE_SIGHT);
    public static final RegistryObject<Potion> LONG_ORE_SIGHT      = lng("long_ore_sight", SoaPotions.ORE_SIGHT);

    /** Master ore-sight — highlights ALL ores in range. White bottle tint
     *  via {@link SoaPotions#MASTER_ORE_SIGHT}. Registered without a brewing
     *  recipe (creative-only / loot-table reward / command grant). */
    public static final RegistryObject<Potion> MASTER_ORE_SIGHT      = base("master_ore_sight", SoaPotions.MASTER_ORE_SIGHT);
    public static final RegistryObject<Potion> LONG_MASTER_ORE_SIGHT = lng("long_master_ore_sight", SoaPotions.MASTER_ORE_SIGHT);

    // === GreedyCraft PotionCore families (ported 2026-07-23) ===
    // blindness / levitation reuse vanilla MobEffects; the rest are SoaPotions.
    public static final RegistryObject<Potion> BLINDNESS         = baseS("blindness", () -> MobEffects.BLINDNESS);
    public static final RegistryObject<Potion> LONG_BLINDNESS    = lngS("long_blindness", () -> MobEffects.BLINDNESS);
    public static final RegistryObject<Potion> STRONG_BLINDNESS  = strongS("strong_blindness", () -> MobEffects.BLINDNESS);

    public static final RegistryObject<Potion> LEVITATION        = baseS("levitation", () -> MobEffects.LEVITATION);
    public static final RegistryObject<Potion> LONG_LEVITATION   = lngS("long_levitation", () -> MobEffects.LEVITATION);
    public static final RegistryObject<Potion> STRONG_LEVITATION = strongS("strong_levitation", () -> MobEffects.LEVITATION);

    public static final RegistryObject<Potion> LOVE              = base("love", SoaPotions.LOVE);

    public static final RegistryObject<Potion> MAGIC_FOCUS        = base("magic_focus", SoaPotions.MAGIC_FOCUS);
    public static final RegistryObject<Potion> LONG_MAGIC_FOCUS   = lng("long_magic_focus", SoaPotions.MAGIC_FOCUS);
    public static final RegistryObject<Potion> STRONG_MAGIC_FOCUS = strong("strong_magic_focus", SoaPotions.MAGIC_FOCUS);

    public static final RegistryObject<Potion> DROWN             = base("drown", SoaPotions.DROWN);
    public static final RegistryObject<Potion> LONG_DROWN        = lng("long_drown", SoaPotions.DROWN);

    public static final RegistryObject<Potion> CLIMB             = base("climb", SoaPotions.CLIMB);
    public static final RegistryObject<Potion> LONG_CLIMB        = lng("long_climb", SoaPotions.CLIMB);

    public static final RegistryObject<Potion> VULNERABLE        = base("vulnerable", SoaPotions.VULNERABLE);
    public static final RegistryObject<Potion> LONG_VULNERABLE   = lng("long_vulnerable", SoaPotions.VULNERABLE);
    public static final RegistryObject<Potion> STRONG_VULNERABLE = strong("strong_vulnerable", SoaPotions.VULNERABLE);

    public static final RegistryObject<Potion> BURST             = base("burst", SoaPotions.BURST);
    public static final RegistryObject<Potion> STRONG_BURST      = strong("strong_burst", SoaPotions.BURST);

    private SoaBrewingPotions() {}

    private static RegistryObject<Potion> base(String name, net.minecraftforge.registries.RegistryObject<? extends net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name,
                new MobEffectInstance(effect.get(), BASE_TICKS, 0)));
    }

    private static RegistryObject<Potion> lng(String name, net.minecraftforge.registries.RegistryObject<? extends net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name,
                new MobEffectInstance(effect.get(), LONG_TICKS, 0)));
    }

    private static RegistryObject<Potion> strong(String name, net.minecraftforge.registries.RegistryObject<? extends net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name,
                new MobEffectInstance(effect.get(), STRONG_TICKS, 1)));
    }

    // Supplier overloads — for vanilla MobEffects (blindness, levitation) that
    // aren't SoA-registered RegistryObjects.
    private static RegistryObject<Potion> baseS(String name, java.util.function.Supplier<net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect.get(), BASE_TICKS, 0)));
    }
    private static RegistryObject<Potion> lngS(String name, java.util.function.Supplier<net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect.get(), LONG_TICKS, 0)));
    }
    private static RegistryObject<Potion> strongS(String name, java.util.function.Supplier<net.minecraft.world.effect.MobEffect> effect) {
        return POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect.get(), STRONG_TICKS, 1)));
    }

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }
}
