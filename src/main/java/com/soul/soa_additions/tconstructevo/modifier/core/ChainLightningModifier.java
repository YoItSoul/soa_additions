package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Chain Lightning — on hit, rolls a per-level chance to bounce reduced
 * damage across nearby living entities. Each bounce deals a fraction of the
 * original damage; total bounces cap at {@value #MAX_BOUNCES}.
 *
 * <p>This uses vanilla lightning-tagged magic damage rather than summoning
 * an actual bolt so it plays nicely with Protection enchantments and
 * doesn't set the world on fire.</p>
 */
public class ChainLightningModifier extends Modifier implements MeleeHitModifierHook {

    private static final float CHANCE_PER_LEVEL = 0.15F;
    private static final float DAMAGE_FRACTION = 0.50F;
    private static final int MAX_BOUNCES = 4;
    private static final double RANGE = 4.0;

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
        LivingEntity origin = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (origin == null || attacker == null) {
            return;
        }
        Level level = origin.level();
        if (level.isClientSide) {
            return;
        }
        float chance = CHANCE_PER_LEVEL * modifier.getEffectiveLevel();
        if (rng.nextFloat() >= chance) {
            return;
        }
        float bounceDamage = damageDealt * DAMAGE_FRACTION;
        if (bounceDamage <= 0.0F) {
            return;
        }
        Set<LivingEntity> hit = new LinkedHashSet<>();
        hit.add(origin);
        LivingEntity last = origin;
        DamageSource source = level.damageSources().lightningBolt();
        for (int i = 0; i < MAX_BOUNCES; i++) {
            LivingEntity next = null;
            double closestSq = Double.POSITIVE_INFINITY;
            AABB box = new AABB(last.getX() - RANGE, last.getY() - RANGE, last.getZ() - RANGE,
                    last.getX() + RANGE, last.getY() + RANGE, last.getZ() + RANGE);
            for (Entity e : level.getEntities(attacker, box)) {
                if (e instanceof LivingEntity le && !hit.contains(le)) {
                    double distSq = last.distanceToSqr(e);
                    if (distSq < closestSq) {
                        next = le;
                        closestSq = distSq;
                    }
                }
            }
            if (next == null) {
                break;
            }
            next.invulnerableTime = 0;
            next.hurt(source, bounceDamage);
            hit.add(next);
            last = next;
        }
    }
}
