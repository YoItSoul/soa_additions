package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Vampiric — heals the attacker for a fraction of damage dealt. Per-level
 * fraction is driven by {@link TConEvoConfig#VAMPIRIC_LIFESTEAL_PER_LEVEL}
 * (default 10% per level).
 */
public class VampiricModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || attacker.level().isClientSide) {
            return;
        }
        float perLevel = (float) TConEvoConfig.VAMPIRIC_LIFESTEAL_PER_LEVEL.get().doubleValue();
        float heal = damageDealt * perLevel * modifier.getEffectiveLevel();
        if (heal > 0.0F) {
            attacker.heal(heal);
        }
    }
}
