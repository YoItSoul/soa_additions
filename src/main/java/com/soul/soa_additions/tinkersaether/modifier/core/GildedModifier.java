package com.soul.soa_additions.tinkersaether.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Gilded": when harvesting a Golden Oak log, 30% chance to
 * yield a Golden Amber gem alongside the normal log drop. In 1.12 the chance
 * was hard-coded to 30% via a config-driven RNG; we keep that value.
 */
public class GildedModifier extends Modifier implements ProcessLootModifierHook {
    private static final ResourceLocation GOLDEN_OAK_LOG  = new ResourceLocation("aether", "golden_oak_log");
    private static final ResourceLocation GOLDEN_AMBER_ID = new ResourceLocation("aether", "golden_amber");
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        BlockState state = ctx.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null || !id.equals(GOLDEN_OAK_LOG)) return;
        if (rng.nextFloat() >= 0.30F) return;
        Item amber = BuiltInRegistries.ITEM.get(GOLDEN_AMBER_ID);
        if (amber == null) return;
        ItemStack stack = new ItemStack(amber);
        if (stack.isEmpty()) return;
        drops.add(stack);
    }
}
