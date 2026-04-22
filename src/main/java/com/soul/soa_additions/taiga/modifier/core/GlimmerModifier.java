package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "glimmer": self night vision while holding; 5% extra on hit/break. */
public class GlimmerModifier extends Modifier implements InventoryTickModifierHook, BlockBreakModifierHook, MeleeHitModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.BLOCK_BREAK, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide || !isSelected || !(holder instanceof Player)) return;
        holder.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false));
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE() || context.getPlayer() == null) return;
        if (rng.nextFloat() <= 0.05F) {
            context.getPlayer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, rng.nextInt(600) + 300));
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        if (attacker == null) return;
        if (rng.nextFloat() <= 0.05F) {
            attacker.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, rng.nextInt(600) + 300));
        }
    }
}
