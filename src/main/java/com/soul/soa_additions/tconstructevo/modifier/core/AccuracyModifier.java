package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoAttributes;
import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Accuracy — while the tool is held in either hand, contributes a MULTIPLY_BASE
 * modifier to the holder's {@code soa_additions:tconevo/accuracy} attribute.
 * The consumer of that attribute (pierce-through-evasion roll) ships with the
 * Evasion trait in a later milestone; for now the attribute value is still
 * read by the TrueStrike potion's placeholder path and by any downstream mod
 * that uses the attribute.
 */
public class AccuracyModifier extends Modifier implements AttributesModifierHook {

    private static final UUID ATTR_MAIN = UUID.fromString("748f667b-2f06-4893-ad4f-f27de29a4d1d");
    private static final UUID ATTR_OFF = UUID.fromString("a2fdbf86-6e9d-4b4b-9f76-3c56f90a0a3b");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ATTRIBUTES);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot,
                              BiConsumer<Attribute, AttributeModifier> consumer) {
        UUID id;
        if (slot == EquipmentSlot.MAINHAND) id = ATTR_MAIN;
        else if (slot == EquipmentSlot.OFFHAND) id = ATTR_OFF;
        else return;

        double amount = modifier.getLevel() * TConEvoConfig.ACCURACY_CHANCE_PER_LEVEL.get();
        if (amount <= 0.0D) return;
        consumer.accept(
                TConEvoAttributes.ACCURACY.get(),
                new AttributeModifier(id, "tconevo.accuracy", amount, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
