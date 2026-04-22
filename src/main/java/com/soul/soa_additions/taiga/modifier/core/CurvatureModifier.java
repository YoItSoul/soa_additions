package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "curvature": 15% chance to swap positions with target on hit. */
public class CurvatureModifier extends Modifier implements MeleeHitModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_HIT); }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (rng.nextFloat() > 0.15F) return;
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null) return;
        BlockPos ap = attacker.blockPosition();
        BlockPos tp = target.blockPosition();
        attacker.teleportTo(tp.getX() + 0.5, tp.getY(), tp.getZ() + 0.5);
        target.teleportTo(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
    }
}
