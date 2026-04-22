package com.soul.soa_additions.taiga.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "heroic": +damage when tool low durability or wielder low health. */
public class HeroicModifier extends Modifier implements MeleeDamageModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_DAMAGE); }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity attacker = context.getAttacker();
        if (attacker == null) return damage;
        int dur = tool.getCurrentDurability();
        int max = tool.getStats().getInt(slimeknights.tconstruct.library.tools.stat.ToolStats.DURABILITY);
        if (max <= 0) return damage;
        boolean lowDur = dur < 0.1F * max;
        boolean lowHealth = attacker.getHealth() < attacker.getMaxHealth() / 8.0F;
        if (lowDur || lowHealth) {
            return damage * 1.5F;
        }
        return damage * 0.9F;
    }
}
