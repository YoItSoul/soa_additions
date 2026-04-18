package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.brandon3055.draconicevolution.init.DEDamage;
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
 * Primordial — converts a fraction of dealt damage into a second, armour-
 * bypassing "chaos damage" hit. On 1.20.1 we emit DE's own
 * {@link DEDamage#chaosImplosion(net.minecraft.world.level.Level)} source so
 * Draconic armour's chaos-resist modules actually interact with the follow-up,
 * instead of a generic magic hit. Invulnerable frames are zeroed so combos
 * don't get eaten by hit cooldowns.
 */
public class PrimordialModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null || damageDealt <= 0 || target.level().isClientSide()) return;
        float frac = (float) (TConEvoConfig.PRIMORDIAL_CONVERSION_PER_LEVEL.get() * modifier.getLevel());
        float chaos = damageDealt * frac;
        if (chaos < 0.01F) return;
        DamageSource src = DEDamage.chaosImplosion(target.level());
        target.invulnerableTime = 0;
        target.hurt(src, chaos);
    }

    @Override
    public int getPriority() {
        return 18;
    }
}
