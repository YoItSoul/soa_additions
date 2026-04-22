package com.soul.soa_additions.taiga.modifier.core;

import com.soul.soa_additions.taiga.util.TaigaUtils;
import java.util.Random;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "bright": daytime +damage, night -damage. On block break, small chance of self night vision. */
public class BrightModifier extends Modifier implements MeleeDamageModifierHook, BlockBreakModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return damage;
        boolean night = TaigaUtils.isNight(target.level());
        return night ? damage / (1.0F + rng.nextFloat() / 3.0F)
                     : damage * (1.0F + rng.nextFloat() / 2.0F);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        if (rng.nextFloat() >= 0.9F && context.getPlayer() != null) {
            context.getPlayer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200));
        }
    }
}
