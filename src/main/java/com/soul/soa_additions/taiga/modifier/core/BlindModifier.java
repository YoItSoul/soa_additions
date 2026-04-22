package com.soul.soa_additions.taiga.modifier.core;

import com.soul.soa_additions.taiga.util.TaigaUtils;
import java.util.Random;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "blind": 1% (3% at night) chance to apply blindness or nausea. */
public class BlindModifier extends Modifier implements MeleeHitModifierHook, BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK);
    }

    private boolean roll(net.minecraft.world.level.Level level) {
        boolean night = TaigaUtils.isNight(level);
        return rng.nextFloat() <= 0.01F || (night && rng.nextFloat() <= 0.03F);
    }

    private void applyEffect(LivingEntity entity, int durBase) {
        if (rng.nextBoolean()) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, rng.nextInt(durBase * 2) + durBase));
        } else {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, rng.nextInt(durBase * 2) + durBase));
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        LivingEntity p = context.getPlayer();
        if (p == null) return;
        if (roll(p.level())) applyEffect(p, 100);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return;
        if (roll(target.level())) applyEffect(target, 200);
    }
}
