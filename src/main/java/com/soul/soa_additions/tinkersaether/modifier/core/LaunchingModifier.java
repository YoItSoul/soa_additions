package com.soul.soa_additions.tinkersaether.modifier.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Launching": melee hit launches the target straight up.
 * Original 1.12 used a fixed 1.0 motionY add; we keep the value and add a
 * tiny horizontal scatter so stacked targets don't perfectly overlap.
 */
public class LaunchingModifier extends Modifier implements MeleeHitModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return;
        Vec3 m = target.getDeltaMovement();
        target.setDeltaMovement(m.x, Math.max(m.y, 0) + 1.0D, m.z);
        target.hurtMarked = true;
    }
}
