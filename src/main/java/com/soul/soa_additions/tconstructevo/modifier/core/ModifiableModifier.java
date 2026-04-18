package com.soul.soa_additions.tconstructevo.modifier.core;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

/**
 * Modifiable — grants one free {@link SlotType#UPGRADE upgrade} slot per
 * level. Mirrors 1.12.2's {@code TraitModifiable}, which bumped the raw
 * {@code modifiers} count on the tool NBT. 1.20.1 uses typed slot pools
 * instead, so we credit the upgrade pool which is the closest equivalent.
 */
public class ModifiableModifier extends Modifier implements VolatileDataModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT data) {
        data.addSlots(SlotType.UPGRADE, modifier.getLevel());
    }
}
