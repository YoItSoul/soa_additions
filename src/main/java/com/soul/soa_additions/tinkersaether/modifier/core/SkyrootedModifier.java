package com.soul.soa_additions.tinkersaether.modifier.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Skyrooted": when harvesting a naturally-generated Aether
 * block (any block in the {@code aether:} namespace), drops are duplicated.
 * The 1.12 version checked a "natural" tag on the block; in 1.20 Aether 1.5
 * doesn't expose that flag, so we approximate by namespace.
 */
public class SkyrootedModifier extends Modifier implements ProcessLootModifierHook {

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        if (drops.isEmpty()) return;
        BlockState state = ctx.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE);
        if (state == null) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null || !"aether".equals(id.getNamespace())) return;
        List<ItemStack> dupes = new ArrayList<>(drops.size());
        for (ItemStack s : drops) dupes.add(s.copy());
        drops.addAll(dupes);
    }
}
