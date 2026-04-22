package com.soul.soa_additions.taiga.modifier.core;

import com.soul.soa_additions.taiga.util.TaigaUtils;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** TAIGA "dark": night +damage, day -damage. */
public class DarkModifier extends Modifier implements MeleeDamageModifierHook {
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_DAMAGE); }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return damage;
        boolean night = TaigaUtils.isNight(target.level());
        return night ? damage * (1.0F + rng.nextFloat() / 2.0F)
                     : damage / (1.0F + rng.nextFloat() / 3.0F);
    }
}
