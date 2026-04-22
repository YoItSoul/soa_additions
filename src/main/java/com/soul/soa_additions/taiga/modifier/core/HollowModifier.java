package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "hollow": 1-3% chance make target invisible and heal them. 90% clear drops dispatched in event bus. */
public class HollowModifier extends Modifier implements MeleeHitModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_HIT); }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return;
        if (rng.nextFloat() > 0.01F) return;
        target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
        if (target.getMaxHealth() < 250.0F) {
            target.setHealth(target.getMaxHealth() * (1.8F - rng.nextFloat() * 0.4F));
        }
    }
}
