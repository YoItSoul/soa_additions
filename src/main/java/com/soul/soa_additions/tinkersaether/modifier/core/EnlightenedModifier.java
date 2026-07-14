package com.soul.soa_additions.tinkersaether.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Enlightened": 5% chance to drop an Ambrosium shard whenever
 * the tool harvests a block. Falls back silently to a no-op if Aether is not
 * loaded (the ambrosium item lookup returns AIR).
 */
public class EnlightenedModifier extends Modifier implements ProcessLootModifierHook {
    private static final ResourceLocation AMBROSIUM_ID = new ResourceLocation("aether", "ambrosium_shard");
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        if (rng.nextFloat() >= 0.05F) return;
        Item ambrosium = BuiltInRegistries.ITEM.get(AMBROSIUM_ID);
        if (ambrosium == null) return;
        ItemStack stack = new ItemStack(ambrosium);
        if (stack.isEmpty()) return;
        drops.add(stack);
    }
}
