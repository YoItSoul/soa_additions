package com.soul.soa_additions.tconstructevo.modifier.core;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.world.damagesource.DamageSource;
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
 * Aftershock — immediately deals a bonus magic-damage hit to the target,
 * bypassing the usual post-hit invulnerability window. Only fires on fully
 * charged swings to match the 1.12.2 timing.
 *
 * <p>A re-entry guard keyed on the target's UUID blocks the secondary hit
 * from chaining through any other {@link MeleeHitModifierHook} that runs
 * another attack (Blasting, ChainLightning, etc.) — otherwise two instances
 * of Aftershock would ping-pong until vanilla caught up.</p>
 */
public class AftershockModifier extends Modifier implements MeleeHitModifierHook {

    private static final float DAMAGE_PER_LEVEL = 2.0F;

    private final ConcurrentMap<UUID, Boolean> inFlight = new ConcurrentHashMap<>();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public int getPriority() {
        // Lower priority so primary-hit effects resolve first (Sundering,
        // Corrupting, etc.) before the secondary damage tick lands.
        return 40;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isFullyCharged() || damageDealt <= 0.0F) {
            return;
        }
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null) {
            return;
        }
        Level level = target.level();
        if (level.isClientSide) {
            return;
        }
        UUID key = target.getUUID();
        if (inFlight.putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }
        try {
            float bonus = DAMAGE_PER_LEVEL * modifier.getEffectiveLevel();
            target.invulnerableTime = 0;
            DamageSource source = level.damageSources().magic();
            target.hurt(source, bonus);
        } finally {
            inFlight.remove(key);
        }
    }
}
