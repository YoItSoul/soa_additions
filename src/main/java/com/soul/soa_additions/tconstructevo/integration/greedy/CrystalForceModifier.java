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
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Crystal Force —
 *   tool: damage = base × (0.8 + frac × 0.5). High-durability = more damage.
 *   armor: incoming = base × (1.05 - frac × 0.12). High-durability = less damage.
 */
public class CrystalForceModifier extends Modifier implements MeleeDamageModifierHook, ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        int max = tool.getStats().getInt(ToolStats.DURABILITY);
        if (max <= 0) return dmg;
        float frac = 1.0F - (float) tool.getDamage() / (float) max;
        return dmg * (0.8F + frac * 0.5F);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        int max = tool.getStats().getInt(ToolStats.DURABILITY);
        if (max <= 0) return dmg;
        float frac = 1.0F - (float) tool.getDamage() / (float) max;
        return dmg * (1.05F - frac * 0.12F);
    }
}
