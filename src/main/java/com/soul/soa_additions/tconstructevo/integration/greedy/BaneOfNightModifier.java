package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Bane of Night — +2.5% damage per light level below 7. */
public class BaneOfNightModifier extends Modifier implements MeleeDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        BlockPos pos = ctx.getAttacker().blockPosition();
        int light = ctx.getAttacker().level().getBrightness(LightLayer.BLOCK, pos);
        int dark = Math.max(0, 7 - light);
        return dmg * (1.0F + 0.025F * dark);
    }
}
