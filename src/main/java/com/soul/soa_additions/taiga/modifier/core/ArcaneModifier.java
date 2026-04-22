package com.soul.soa_additions.taiga.modifier.core;

import com.soul.soa_additions.taiga.util.TaigaUtils;
import java.util.Random;
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

/** TAIGA "arcane": 5% at night, heal tool on block break or melee hit. */
public class ArcaneModifier extends Modifier implements MeleeHitModifierHook, BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || context.getPlayer() == null) return;
        if (rng.nextFloat() <= 0.05F && TaigaUtils.isNight(context.getWorld())) {
            ToolDamageUtil.repair((ToolStack) tool, rng.nextInt(8) + 1);
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (context.getAttacker() == null) return;
        if (rng.nextFloat() <= 0.05F && TaigaUtils.isNight(context.getAttacker().level())) {
            ToolDamageUtil.repair((ToolStack) tool, rng.nextInt(8) + 1);
        }
    }
}
