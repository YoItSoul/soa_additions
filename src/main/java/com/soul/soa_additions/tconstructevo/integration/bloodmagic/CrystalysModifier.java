package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import wayoftime.bloodmagic.common.item.BloodMagicItems;

/**
 * Crystalys — rolls a drop of Blood Magic's Weak Blood Shard when a hostile
 * target is killed. The item comes straight from BM's registry so texture,
 * tooltip, and downstream recipes all track whatever BM ships.
 */
public class CrystalysModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null || target.level().isClientSide()) return;
        if (target.isAlive() || target instanceof Animal) return;
        double roll = TConEvoConfig.CRYSTALYS_DROP_PROBABILITY.get();
        if (target.getRandom().nextDouble() >= roll) return;
        ItemStack drop = new ItemStack(BloodMagicItems.WEAK_BLOOD_SHARD.get());
        target.spawnAtLocation(drop);
    }
}
