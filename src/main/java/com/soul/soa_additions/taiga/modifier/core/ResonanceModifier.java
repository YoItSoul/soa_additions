package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "resonance": 33% chance to knock the target back on melee hit. */
public class ResonanceModifier extends Modifier implements MeleeHitModifierHook {
    private static final float CHANCE = 0.33F;
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_HIT); }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null) return;
        if (rng.nextFloat() > CHANCE) return;
        float strength = rng.nextFloat() * rng.nextFloat() * 10.0F;
        target.knockback(strength, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
    }
}
