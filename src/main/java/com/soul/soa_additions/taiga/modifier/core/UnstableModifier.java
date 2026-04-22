package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** TAIGA "unstable": 3% chance explosion on break, 4% on hit. */
public class UnstableModifier extends Modifier implements MeleeHitModifierHook, BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        if (rng.nextFloat() > 0.03F) return;
        Level level = context.getWorld();
        BlockPos pos = context.getPos();
        LivingEntity source = rng.nextBoolean() ? context.getPlayer() : null;
        if (!level.isClientSide) {
            level.explode(source, pos.getX(), pos.getY(), pos.getZ(), 1.2F + rng.nextFloat() * 5.0F,
                    Level.ExplosionInteraction.TNT);
        }
        ToolDamageUtil.damageAnimated((ToolStack) tool, rng.nextInt(10) + 2, context.getPlayer());
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (rng.nextFloat() > 0.04F) return;
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null || attacker.level().isClientSide) return;
        LivingEntity source = rng.nextBoolean() ? attacker : target;
        attacker.level().explode(source, target.getX(), target.getY(), target.getZ(),
                1.2F + rng.nextFloat() * 5.0F, Level.ExplosionInteraction.TNT);
    }
}
