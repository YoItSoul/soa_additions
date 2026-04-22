package com.soul.soa_additions.taiga.modifier.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "souleater": accumulated bonus damage based on kill count.
 * Kill tracking in {@link com.soul.soa_additions.taiga.event.TaigaTraitEvents}.
 */
public class SoulEaterModifier extends Modifier implements MeleeDamageModifierHook {
    public static final ResourceLocation KEY = new ResourceLocation("taiga", "souleater");

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_DAMAGE); }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        CompoundTag data = ((ToolStack) tool).getPersistentData().getCompound(KEY);
        return damage + data.getFloat("bonus");
    }
}
