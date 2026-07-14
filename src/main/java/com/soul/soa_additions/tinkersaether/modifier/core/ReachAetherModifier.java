package com.soul.soa_additions.tinkersaether.modifier.core;

import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * tinkersaether "Reach": +1 to entity reach and block reach while the tool
 * is held in the main hand. Implemented via TC3's
 * {@link AttributesModifierHook} which runs only for the held slot. Suffix
 * {@code _aether} on the modifier id to avoid clashing with TC3's existing
 * {@code tconstruct:reach} bookkeeping if any.
 */
public class ReachAetherModifier extends Modifier implements AttributesModifierHook {
    private static final UUID UUID_BLOCK  = UUID.fromString("50aadd02-0001-4000-8001-000000000001");
    private static final UUID UUID_ENTITY = UUID.fromString("50aadd02-0001-4000-8001-000000000002");

    @Override protected void registerHooks(ModuleHookMap.Builder b) {
        b.addHook(this, ModifierHooks.ATTRIBUTES);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry entry, EquipmentSlot slot,
                              java.util.function.BiConsumer<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> consumer) {
        if (slot != EquipmentSlot.MAINHAND) return;
        consumer.accept(ForgeMod.BLOCK_REACH.get(),
                new AttributeModifier(UUID_BLOCK, "soa_additions:reach_aether_block", 1.0D, AttributeModifier.Operation.ADDITION));
        consumer.accept(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(UUID_ENTITY, "soa_additions:reach_aether_entity", 1.0D, AttributeModifier.Operation.ADDITION));
    }
}
