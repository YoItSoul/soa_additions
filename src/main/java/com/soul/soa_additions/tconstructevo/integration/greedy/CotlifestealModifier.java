package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Cotlifesteal — 33% chance to heal damage*5% (cap 5) on non-undead mob hit. */
public class CotlifestealModifier extends Modifier implements MeleeHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!(ctx.getAttacker() instanceof Player p) || p.level().isClientSide()) return;
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return;
        if (target.getMobType() == MobType.UNDEAD) return;
        if (p.getRandom().nextFloat() >= 0.33F) return;
        float heal = Math.min(dmg * 0.05F, 5.0F);
        p.heal(heal);
    }
}
