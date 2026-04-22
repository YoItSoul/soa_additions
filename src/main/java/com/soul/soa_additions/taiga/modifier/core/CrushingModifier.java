package com.soul.soa_additions.taiga.modifier.core;

import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "crushing": when mining stone, replace drops with weighted sand/gravel/cobble/stone. */
public class CrushingModifier extends Modifier implements ProcessLootModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.PROCESS_LOOT); }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        boolean mineStone = drops.stream().anyMatch(s -> s.getItem() == Items.COBBLESTONE || s.getItem() == Items.STONE);
        if (!mineStone) return;
        drops.clear();
        float f = rng.nextFloat();
        if (f < 0.3F) drops.add(new ItemStack(Blocks.SAND));
        else if (f < 0.6F) drops.add(new ItemStack(Blocks.GRAVEL));
        else if (f <= 0.9F) drops.add(new ItemStack(Blocks.COBBLESTONE));
        else drops.add(new ItemStack(Blocks.STONE));
    }
}
