package com.soul.soa_additions.taiga.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * TAIGA "analysing": 10% chance clear block/mob drops. XP-modification hooks
 * dispatch from {@link com.soul.soa_additions.taiga.event.TaigaTraitEvents}.
 */
public class AnalysingModifier extends Modifier implements ProcessLootModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.PROCESS_LOOT); }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        if (rng.nextFloat() < 0.1F) drops.clear();
    }
}
