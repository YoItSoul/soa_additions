package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Status effects added by TConstructEvo (TrueStrike, DamageBoost,
 * DamageReduction, HealReduction). Mirrors {@code TconEvoPotions} from the
 * 1.12.2 source but uses 1.20.1's {@link MobEffect} class instead of Potion.
 */
public final class TConEvoPotions {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SoaAdditions.MODID);

    public static final RegistryObject<MobEffect> TRUE_STRIKE =
            register("tconevo/true_strike", com.soul.soa_additions.tconstructevo.potion.TrueStrikeEffect::new);
    public static final RegistryObject<MobEffect> DAMAGE_BOOST =
            register("tconevo/damage_boost", com.soul.soa_additions.tconstructevo.potion.DamageBoostEffect::new);
    public static final RegistryObject<MobEffect> DAMAGE_REDUCTION =
            register("tconevo/damage_reduction", com.soul.soa_additions.tconstructevo.potion.DamageReductionEffect::new);
    public static final RegistryObject<MobEffect> HEAL_REDUCTION =
            register("tconevo/heal_reduction", com.soul.soa_additions.tconstructevo.potion.HealReductionEffect::new);

    private TConEvoPotions() {}

    private static <E extends MobEffect> RegistryObject<E> register(String name, Supplier<E> factory) {
        return EFFECTS.register(name, factory);
    }

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
