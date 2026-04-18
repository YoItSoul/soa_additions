package com.soul.soa_additions.tconstructevo.integration.avaritia;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Omnipotence — Infinity-tier weapon trait. Guarantees every hit deals at
 * least half the target's max HP, topping up the damage via a bypass-armor
 * magic follow-up when the base hit undershoots. The 1.12.2 original used
 * a mutable DamageSource and reset invulnerableTime so crits still counted;
 * we do the same here, then consult the {@code omnipotenceHitsCreative}
 * config gate when deciding whether to spawn a creative-bypass hit.
 */
public class OmnipotenceModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null || target.level().isClientSide()) return;
        float floor = target.getMaxHealth() * 0.5F;
        float undealt = floor - damageDealt;
        if (undealt <= 0) return;
        target.invulnerableTime = 0;
        DamageSource src = target.damageSources().magic();
        // hurt() already respects creative-mode invulnerability; directly subtracting
        // HP is the only way to reproduce 1.12.2's creative-bypass behaviour.
        if (TConEvoConfig.OMNIPOTENCE_HITS_CREATIVE.get()) {
            target.setHealth(Math.max(target.getHealth() - undealt, 0.0F));
        } else {
            target.hurt(src, undealt);
        }
    }

    @Override
    public int getPriority() {
        return 15;
    }
}
