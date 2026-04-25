package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Tidal Force —
 *   tool (held): water_breathing 50t + 1.33× damage when wet.
 *   armor:       water_breathing III (25t per GC armor cadence).
 */
public class TidalForceModifier extends Modifier implements InventoryTickModifierHook, MeleeDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.MELEE_DAMAGE);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry mod, Level level, LivingEntity holder,
                                int slot, boolean selected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide()) return;
        boolean isArmor = slot >= 36 && slot <= 39;
        if (selected) {
            holder.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 50, 0, false, false));
        } else if (isArmor) {
            holder.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 25, 2, false, false));
        }
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float base, float dmg) {
        LivingEntity a = ctx.getAttacker();
        return (a.isInWater() || a.isInWaterRainOrBubble()) ? dmg * 1.33F : dmg;
    }
}
