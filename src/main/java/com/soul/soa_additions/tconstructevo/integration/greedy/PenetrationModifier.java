package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Penetration — bonus damage scaled by target's armor (cap +100% at 50 armor). */
public class PenetrationModifier extends Modifier implements MeleeDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return dmg;
        int armor = target.getArmorValue();
        if (armor <= 0) return dmg;
        float mult = Math.min(1.0F, armor * 0.02F);
        return dmg * (1.0F + mult);
    }
}
