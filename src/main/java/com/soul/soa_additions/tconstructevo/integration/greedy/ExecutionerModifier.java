package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Executioner — instakill via OUT_OF_WORLD on targets below 20% HP (10% if boss-scale). */
public class ExecutionerModifier extends Modifier implements MeleeHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!(ctx.getAttacker() instanceof Player p)) return;
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return;
        if (dmg < 0.1F) return;
        // "boss" = anything with maxHealth >= 100 (rough proxy without Forge boss flag).
        float threshold = target.getMaxHealth() >= 100.0F ? 0.1F : 0.2F;
        if ((target.getHealth() / target.getMaxHealth()) >= threshold) return;
        DamageSource src = target.damageSources().fellOutOfWorld();
        target.hurt(src, Float.MAX_VALUE);
    }
}
