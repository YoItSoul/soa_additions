package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
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
 * Knowledgeful —
 *   tool: +0.2% damage per XP level (cap 300, +60% max).
 *   armor: scale incoming reduction with xp/300 × 36% (per-piece via calcSingleArmor).
 */
public class KnowledgefulModifier extends Modifier implements MeleeDamageModifierHook, ModifyDamageModifierHook {

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
        if (!(ctx.getAttacker() instanceof Player p)) return dmg;
        int xp = Math.min(p.experienceLevel, 300);
        return dmg * (1.0F + xp * 0.002F);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        if (!(ctx.getEntity() instanceof Player p)) return dmg;
        float xp = Math.min(p.experienceLevel, 300);
        float reduction = (xp / 300.0F) * 0.36F;
        return dmg * perPiece(reduction);
    }
}
