package com.soul.soa_additions.bloodarsenal.item.tool;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * Custom tool tiers for Blood Arsenal blood-infused tools.
 * Wooden: wood-equivalent with higher enchantability (18).
 * Iron: iron-equivalent with UNCOMMON rarity (registered on items).
 */
public enum BATiers implements Tier {

    BLOOD_INFUSED_WOODEN(0, 250, 2.0f, 0.0f, 18, () -> Ingredient.EMPTY),
    BLOOD_INFUSED_IRON(2, 500, 6.0f, 2.0f, 14, () -> Ingredient.EMPTY);

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    BATiers(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return attackDamageBonus; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
}
