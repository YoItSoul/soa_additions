package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Purging — rolls a per-level chance to strip a random beneficial status
 * effect from the target. Effective against buffed foes but toothless
 * against unbuffed ones.
 */
public class PurgingModifier extends Modifier implements MeleeHitModifierHook {

    private static final float CHANCE_PER_LEVEL = 0.20F;

    private final Random rng = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }
        LivingEntity living = context.getLivingTarget();
        if (living == null || living.level().isClientSide || !living.isAlive()) {
            return;
        }
        float chance = CHANCE_PER_LEVEL * modifier.getEffectiveLevel();
        if (rng.nextFloat() >= chance) {
            return;
        }
        List<MobEffect> beneficial = new ArrayList<>();
        for (MobEffectInstance inst : living.getActiveEffects()) {
            if (inst.getEffect().isBeneficial()) {
                beneficial.add(inst.getEffect());
            }
        }
        if (!beneficial.isEmpty()) {
            living.removeEffect(beneficial.get(rng.nextInt(beneficial.size())));
        }
    }
}
