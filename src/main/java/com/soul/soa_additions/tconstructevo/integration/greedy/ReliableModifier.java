package com.soul.soa_additions.tconstructevo.integration.greedy;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Reliable — durability gambit:
 *   - holder above 90% HP: 75% chance no durability lost, 25% normal
 *   - holder below 90% HP: 2x durability damage
 */
public class ReliableModifier extends Modifier implements ToolDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }
    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry mod, int amount, @Nullable LivingEntity holder) {
        if (holder == null) return amount;
        if (holder.getHealth() > holder.getMaxHealth() * 0.9F) {
            return holder.getRandom().nextFloat() < 0.25F ? amount : 0;
        }
        return amount * 2;
    }
}
