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

/** TAIGA "ported": 0.5% chance teleport wielder up 10 blocks on break or hit. */
public class PortedModifier extends Modifier implements MeleeHitModifierHook, BlockBreakModifierHook {
    private static final int DISTANCE = 10;
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK);
    }

    private void teleport(LivingEntity e, Level level) {
        BlockPos target = e.blockPosition().above(DISTANCE);
        if (target.getY() >= level.getMaxBuildHeight()) return;
        while (!level.getBlockState(target).isAir() && target.getY() <= level.getMaxBuildHeight()) {
            target = target.above();
        }
        if (!level.getBlockState(target).isAir()) return;
        e.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || context.getPlayer() == null) return;
        if (rng.nextFloat() <= 0.005F) {
            teleport(context.getPlayer(), context.getWorld());
            ToolDamageUtil.damageAnimated((ToolStack) tool, 10, context.getPlayer());
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        if (attacker == null) return;
        if (rng.nextFloat() <= 0.005F) {
            teleport(attacker, attacker.level());
            ToolDamageUtil.damageAnimated((ToolStack) tool, 10, attacker);
        }
    }
}
