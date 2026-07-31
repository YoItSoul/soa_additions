package com.soul.soa_additions.combat;

import com.google.common.collect.Multimap;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * How much of a headshot a piece of headgear soaks, ported from GreedyCraft's
 * iblis-headshots so helmet-by-helmet feel matches the 1.12 pack.
 *
 * <p>The returned value is a <em>damage-remaining</em> multiplier: {@code 1.0}
 * means no protection at all, {@code 0.0} means a perfect helmet. It is derived
 * from the item's own armour attribute rather than a hand-maintained table, so
 * every modded helmet in the pack gets a value without us curating a list.</p>
 *
 * <p>The curve is vanilla's armour formula evaluated against 1.0 damage and then
 * raised to the 16th power, which is what makes helmet tier matter so much:
 * 1 armour point gives ~30% protection, 3 gives ~83%, 8 gives ~99.7%.</p>
 */
public final class HeadgearProtection {

    private HeadgearProtection() {}

    /** Damage-remaining multiplier for this headgear; 1.0 when it protects nothing. */
    public static float getProtection(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(EquipmentSlot.HEAD);
        if (mods.isEmpty()) return 1.0F;

        AttributeInstance armorAttr = new AttributeInstance(Attributes.ARMOR, instance -> {});
        for (AttributeModifier mod : mods.get(Attributes.ARMOR)) {
            armorAttr.addTransientModifier(mod);
        }
        float armor = (float) armorAttr.getValue();
        if (armor <= 0.0F) return 1.0F;

        // GC passes the helmet's *armour* value in as toughness as well. That is an
        // upstream bug — the mod builds its toughness accumulator from the ARMOR
        // attribute instead of ARMOR_TOUGHNESS — but the pack's balance was tuned
        // against the buggy numbers, so reproducing it is what keeps feel 1:1.
        // (For vanilla helmets the difference is under a percentage point anyway.)
        float remaining = CombatRules.getDamageAfterAbsorb(1.0F, armor, armor);

        remaining *= remaining;   // ^2
        remaining *= remaining;   // ^4
        remaining *= remaining;   // ^8
        remaining *= remaining;   // ^16
        return remaining;
    }

    /** Protection as a whole percentage, matching the number GC put on tooltips. */
    public static int getProtectionPercent(ItemStack stack) {
        return (int) Math.ceil((1.0F - getProtection(stack)) * 100.0F);
    }
}
