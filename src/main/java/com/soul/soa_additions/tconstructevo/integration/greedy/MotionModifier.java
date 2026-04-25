package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Motion — tool: 1.33× damage sprinting; armor: -7% damage sprinting. (Knockback amp branch dropped — no TC3 hook.) */
public class MotionModifier extends Modifier implements MeleeDamageModifierHook, ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        return ctx.getAttacker().isSprinting() ? dmg * 1.33F : dmg;
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        return ctx.getEntity().isSprinting() ? dmg * 0.93F : dmg;
    }
}
