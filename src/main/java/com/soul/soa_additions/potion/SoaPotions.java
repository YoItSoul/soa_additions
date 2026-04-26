package com.soul.soa_additions.potion;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * SoA Potions — port of the GreedyCraft PotionCore brews documented in
 * {@code D:/Minecraft/Instances/GreedyCraft/scripts/recipes/vanilla/brewing.zs}.
 *
 * <p>PotionCore (the 1.12.2 mod TmTravlr maintained) was never updated to
 * 1.20.1, so we add the GC-relevant subset here as native MobEffects under
 * the {@code soa_additions} namespace. Brewing recipes are wired in
 * {@link SoaBrewing} via Forge's RegisterBrewingRecipesEvent.</p>
 *
 * <p>Effects ported (18):</p>
 * <ul>
 *   <li>health_boost — bonus max-HP buff (matches GC golden_apple brew)</li>
 *   <li>repair       — held tool ticks toward repair</li>
 *   <li>flight       — creative-style flight</li>
 *   <li>iron_skin    — flat +armor attribute</li>
 *   <li>diamond_skin — armor + armor_toughness</li>
 *   <li>reach        — +interaction range</li>
 *   <li>extension    — extends OTHER active beneficial effects</li>
 *   <li>purity       — strips negative effects every tick</li>
 *   <li>step_up      — +step_height attribute</li>
 *   <li>magic_shield — absorption-style damage soak</li>
 *   <li>archery      — +arrow damage</li>
 *   <li>solid_core   — +knockback resistance</li>
 *   <li>weight       — heavier gravity (negative jump strength)</li>
 *   <li>lightning    — periodic lightning bolt on attacked target</li>
 *   <li>fire         — periodic fire damage to attackers</li>
 *   <li>explode      — death triggers an explosion</li>
 *   <li>launch       — periodic upward boost</li>
 *   <li>revival      — auto-revive once on death</li>
 *   <li>teleport     — random short-range teleport every cycle</li>
 *   <li>magic_focus  — bridges to TConEvo DAMAGE_BOOST (alias)</li>
 * </ul>
 */
public final class SoaPotions {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SoaAdditions.MODID);

    public static final RegistryObject<MobEffect> HEALTH_BOOST  = reg("health_boost",  HealthBoostEffect::new);
    public static final RegistryObject<MobEffect> REPAIR        = reg("repair",        RepairEffect::new);
    public static final RegistryObject<MobEffect> FLIGHT        = reg("flight",        FlightEffect::new);
    public static final RegistryObject<MobEffect> IRON_SKIN     = reg("iron_skin",     IronSkinEffect::new);
    public static final RegistryObject<MobEffect> DIAMOND_SKIN  = reg("diamond_skin",  DiamondSkinEffect::new);
    public static final RegistryObject<MobEffect> REACH         = reg("reach",         ReachEffect::new);
    public static final RegistryObject<MobEffect> EXTENSION     = reg("extension",     ExtensionEffect::new);
    public static final RegistryObject<MobEffect> PURITY        = reg("purity",        PurityEffect::new);
    public static final RegistryObject<MobEffect> STEP_UP       = reg("step_up",       StepUpEffect::new);
    public static final RegistryObject<MobEffect> MAGIC_SHIELD  = reg("magic_shield",  MagicShieldEffect::new);
    public static final RegistryObject<MobEffect> ARCHERY       = reg("archery",       ArcheryEffect::new);
    public static final RegistryObject<MobEffect> SOLID_CORE    = reg("solid_core",    SolidCoreEffect::new);
    public static final RegistryObject<MobEffect> WEIGHT        = reg("weight",        WeightEffect::new);
    public static final RegistryObject<MobEffect> LIGHTNING     = reg("lightning",     LightningEffect::new);
    public static final RegistryObject<MobEffect> FIRE          = reg("fire",          FireEffect::new);
    public static final RegistryObject<MobEffect> EXPLODE       = reg("explode",       ExplodeEffect::new);
    public static final RegistryObject<MobEffect> LAUNCH        = reg("launch",        LaunchEffect::new);
    public static final RegistryObject<MobEffect> REVIVAL       = reg("revival",       RevivalEffect::new);
    public static final RegistryObject<MobEffect> TELEPORT      = reg("teleport",      TeleportEffect::new);

    private SoaPotions() {}

    private static <E extends MobEffect> RegistryObject<E> reg(String name, Supplier<E> factory) {
        return EFFECTS.register(name, factory);
    }

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
