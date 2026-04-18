package com.soul.soa_additions.tconstructevo.modifier.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Thundergod's Wrath — summons a non-visual lightning bolt on the target if
 * the attack would kill them. Matches the original tconevo "execute with
 * style" behaviour.
 */
public class ThundergodWrathModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity living = context.getLivingTarget();
        if (living == null) {
            return;
        }
        Level level = living.level();
        if (level.isClientSide || !(level instanceof ServerLevel server)) {
            return;
        }
        if (living.getHealth() + damageDealt + 1.0E-4F < living.getMaxHealth()) {
            return;
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt != null) {
            bolt.moveTo(living.getX(), living.getY(), living.getZ());
            bolt.setVisualOnly(false);
            server.addFreshEntity(bolt);
        }
    }
}
