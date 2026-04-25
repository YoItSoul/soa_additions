package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Leveling Defense — scales with TinkersLevellingAddon "leveling" stat.
 * Without that addon's API hook, falls back to flat ~3% reduction per
 * modifier level (cap 50%).
 */
public class LevelingdefenseModifier extends Modifier implements ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        float reduction = Math.min(0.5F, 0.03F * mod.getEffectiveLevel());
        return dmg * (1.0F - reduction);
    }
}
