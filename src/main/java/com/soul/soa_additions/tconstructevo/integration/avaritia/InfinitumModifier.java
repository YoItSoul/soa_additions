package com.soul.soa_additions.tconstructevo.integration.avaritia;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Infinitum — signature trait of Infinity material tools. Truly unbreakable:
 * every durability damage event is swallowed. The 1.12.2 approach was to set
 * the item's vanilla {@code Unbreakable} NBT; on TConstruct 3.x tools that
 * flag doesn't fire, so we short-circuit the damage pipeline instead.
 */
public class InfinitumModifier extends Modifier implements ToolDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        return 0;
    }

    @Override
    public int getPriority() {
        return 250;
    }
}
