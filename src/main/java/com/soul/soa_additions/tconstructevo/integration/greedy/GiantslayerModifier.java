package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Giantslayer — bonus damage when target.HP > attacker.maxHP. Multiplier ∈ [1.0, 2.5]. */
public class GiantslayerModifier extends Modifier implements MeleeDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return dmg;
        float mult = 0.05F * (target.getHealth() / ctx.getAttacker().getMaxHealth());
        if (mult < 1.0F) mult = 1.0F;
        if (mult > 2.5F) mult = 2.5F;
        return dmg * mult;
    }
}
