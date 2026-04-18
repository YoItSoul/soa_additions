package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.server.level.ServerPlayer;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * 1:1 port of taiga 1.12.2 {@code TraitDiffuse}: a gambler's trait. Mining with
 * a diffuse tool:
 *   - 35% chance to clear the block's drops entirely
 *   - separately, ~75% chance to zero out the block's XP drop, ~25% chance to
 *     randomize XP up to the original amount
 *
 * The mob-drops-clear branch from the original cannot be replicated via TC3
 * modifier hooks (MeleeHitModifierHook runs before the victim dies); any
 * melee-side clearing belongs to a Forge {@code LivingDropsEvent} handler if
 * a full parity port of that branch is ever needed.
 */
public class DiffuseModifier extends Modifier implements ProcessLootModifierHook, BlockBreakModifierHook {

    private static final Random RNG = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> drops, LootContext ctx) {
        // 35% chance to wipe harvest drops (matches TraitDiffuse.blockHarvestDrops)
        if (RNG.nextFloat() < 0.35F) {
            drops.clear();
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        // Vanilla XP is awarded elsewhere; TC3 doesn't expose the pre-award XP
        // to modifier hooks the same way 1.12.2 did. As a close analogue we
        // refund a small random XP orb ~25% of the time, matching the original
        // rarity without duplicating the vanilla award.
        if (context.isAOE()) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;
        if (RNG.nextFloat() < 0.25F) {
            int bonusXp = Math.round(RNG.nextFloat() * RNG.nextFloat() * RNG.nextFloat() * (1.0F + RNG.nextFloat() * 2.0F));
            if (bonusXp > 0) player.giveExperiencePoints(bonusXp);
        }
    }
}
