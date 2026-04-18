package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Draconic Attack AoE — on every melee hit, damages every {@link LivingEntity}
 * inside a spherical radius around the primary target. Radius scales linearly
 * with modifier level; each secondary target takes a configurable fraction of
 * the primary damage dealt. Secondary hits go out under
 * {@link Player#damageSources()}.mobAttack() to keep kill attribution/XP drops
 * routed through the wielder.
 *
 * <p>The 1.12.2 implementation widened {@code ExtraBlockBreak}'s bounding box
 * via a custom {@code IBoxExpansion}; TC3's AoE plumbing lives in tool-
 * definition iterators that can't be mutated from a modifier at runtime. This
 * direct MELEE_HIT iteration produces equivalent user-visible behaviour
 * without touching the tool definition pipeline.</p>
 */
public class DraconicAttackAoeModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || !(attacker instanceof Player player) || player.level().isClientSide()) return;

        double radius = TConEvoConfig.DRACONIC_ATTACK_AOE_RADIUS_PER_LEVEL.get() * modifier.getLevel();
        if (radius <= 0) return;
        float secondaryDamage = (float) (damageDealt * TConEvoConfig.DRACONIC_ATTACK_AOE_DAMAGE_FRACTION.get());
        if (secondaryDamage <= 0) return;

        AABB box = target.getBoundingBox().inflate(radius, radius / 2.0D, radius);
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != target && e != player && e.isAlive() && !e.isSpectator() && !player.isAlliedTo(e));

        DamageSource src = player.damageSources().mobAttack(player);
        for (LivingEntity entity : nearby) {
            entity.invulnerableTime = 0;
            entity.hurt(src, secondaryDamage);
        }
    }

    @Override
    public int getPriority() {
        return 22;
    }
}
