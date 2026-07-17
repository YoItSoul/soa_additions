package com.soul.soa_additions.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * The four TConEvo MobEffects (1.12 {@code TconEvoPotions}), registered under the
 * {@code tconevo:} namespace so the existing lang keys ({@code effect.tconevo.*})
 * resolve. GC config values: damage boost +5%/level, damage reduction 4%/level,
 * mortal wounds blocks 75% of healing.
 *
 * <p>Immortality death-save, damage-reduction and heal-suppression logic live in
 * {@link TconEvoEffectEvents}; damage boost is a plain attack-damage attribute.</p>
 */
public final class TconEvoEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "tconevo");

    /** Prevents death while active; consumed by the save (1.12: undispellable). */
    public static final RegistryObject<MobEffect> IMMORTALITY = EFFECTS.register("immortality",
            () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xEBC583) {
                @Override
                public List<ItemStack> getCurativeItems() { return List.of(); }
            });

    /** Blocks 75% of all healing received (Potion of Suppressed Healing). */
    public static final RegistryObject<MobEffect> MORTAL_WOUNDS = EFFECTS.register("mortal_wounds",
            () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x5F5D0E));

    /** -4% damage taken per level (Potion of Endurance). */
    public static final RegistryObject<MobEffect> DAMAGE_REDUCTION = EFFECTS.register("damage_reduction",
            () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x5A051A));

    /** +5% attack damage per level (Potion of Power). */
    public static final RegistryObject<MobEffect> DAMAGE_BOOST = EFFECTS.register("damage_boost",
            () -> {
                SimpleEffect e = new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xF82038);
                e.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        "a68351eb-5327-45da-a5c5-2af42f08300d", 0.05,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                return e;
            });

    private static class SimpleEffect extends MobEffect {
        SimpleEffect(MobEffectCategory category, int color) { super(category, color); }
    }

    private TconEvoEffects() {}

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
