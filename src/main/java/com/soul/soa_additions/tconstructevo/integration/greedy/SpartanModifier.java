package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Spartan —
 *   tool: <33% HP, +1.5..+2.5× outgoing damage scaling.
 *   armor: <33% HP, 30..75% incoming damage reduction (per-piece via calcSingleArmor).
 */
public class SpartanModifier extends Modifier implements MeleeDamageModifierHook, ModifyDamageModifierHook {

    /** GC's calcSingleArmor: full-set reduction → per-piece factor. */
    private static float perPiece(float fullSetReduction) {
        if (fullSetReduction <= 0F) return 1F;
        if (fullSetReduction >= 1F) return 0F;
        return (float) Math.pow(1.0F - fullSetReduction, 0.25);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        LivingEntity attacker = ctx.getAttacker();
        float ratio = attacker.getHealth() / attacker.getMaxHealth();
        if (ratio >= 0.33F) return dmg;
        float mult = 1.5F + (1.0F - attacker.getHealth() / (attacker.getMaxHealth() * 0.33F));
        return dmg * mult;
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        LivingEntity entity = ctx.getEntity();
        float ratio = entity.getHealth() / entity.getMaxHealth();
        if (ratio >= 0.33F) return dmg;
        float reduction = 0.3F + (1.0F - entity.getHealth() / (entity.getMaxHealth() * 0.33F)) * 0.45F;
        return dmg * perPiece(reduction);
    }
}
