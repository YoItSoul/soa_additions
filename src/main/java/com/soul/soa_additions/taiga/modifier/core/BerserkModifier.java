package com.soul.soa_additions.taiga.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * TAIGA "berserk": +damage and +mining speed at the cost of durability.
 * The 1.12 version was toggleable via alt+right-click; in TC3 we apply a
 * constant 1.5x boost (passive) which keeps the functional character without
 * the per-tool state machine.
 */
public class BerserkModifier extends Modifier implements MeleeDamageModifierHook, BreakSpeedModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.BREAK_SPEED);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        return damage * 2.0F;
    }

    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier,
                             net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event,
                             net.minecraft.core.Direction sideHit, boolean isEffective, float miningSpeedModifier) {
        if (isEffective) {
            event.setNewSpeed(event.getNewSpeed() * 2.0F);
        }
    }
}
