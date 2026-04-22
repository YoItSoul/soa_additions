package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** TAIGA "fragile": chance to randomly damage OR heal the tool on break. */
public class FragileModifier extends Modifier implements BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.BLOCK_BREAK); }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || !context.isEffective()) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;
        int max = tool.getStats().getInt(slimeknights.tconstruct.library.tools.stat.ToolStats.DURABILITY);
        int cur = tool.getCurrentDurability();
        if (max <= 50) return;
        float bonus = 0.99F * (0.4F / (max - 50) * cur + 0.55F);
        if (rng.nextFloat() > bonus) return;
        if (rng.nextBoolean()) {
            ToolDamageUtil.damageAnimated((ToolStack) tool, rng.nextInt(3), player);
        } else {
            ToolDamageUtil.repair((ToolStack) tool, rng.nextInt(3));
        }
    }
}
