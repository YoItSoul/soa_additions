package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Lightning — 4% chance on mob hit to spawn a (visual-only) lightning bolt at target. */
public class LightningModifier extends Modifier implements MeleeHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!(ctx.getAttacker() instanceof Player p)) return;
        if (!(p.level() instanceof ServerLevel sl)) return;
        LivingEntity target = ctx.getLivingTarget();
        if (target == null) return;
        if (sl.getRandom().nextFloat() >= 0.04F) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
        if (bolt == null) return;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        bolt.setVisualOnly(true);
        sl.addFreshEntity(bolt);
    }
}
