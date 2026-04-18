package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.Random;
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
 * Blasting — chance on hit to create a small non-grief explosion at the
 * target. Server-only; uses vanilla's Level.explode with block damage
 * disabled so the trait doesn't turn mining tools into terrain grinders.
 */
public class BlastingModifier extends Modifier implements MeleeHitModifierHook {

    private static final float CHANCE_PER_LEVEL = 0.08F;
    private static final float RADIUS = 2.0F;

    private final Random rng = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }
        LivingEntity living = context.getLivingTarget();
        if (living == null) {
            return;
        }
        Level level = living.level();
        if (level.isClientSide) {
            return;
        }
        float chance = CHANCE_PER_LEVEL * modifier.getEffectiveLevel();
        if (rng.nextFloat() >= chance) {
            return;
        }
        level.explode(context.getAttacker(), living.getX(), living.getY() + living.getBbHeight() / 2.0, living.getZ(),
                RADIUS, Level.ExplosionInteraction.NONE);
    }
}
