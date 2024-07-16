package com.soul.soa_additions;

import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CustomToolTiers implements Tier {

    public static final CustomToolTiers INFERNIUM = new CustomToolTiers(5, 1561, 8.0F, 3.0F, 10, Ingredient.of(ModItems.INFERNIUM_INGOT.get()));
    public static final CustomToolTiers VOID = new CustomToolTiers(6, 1561, 8.0F, 3.0F, 10, Ingredient.of(ModItems.VOID_INGOT.get()));
    public static final CustomToolTiers ABYSSAL = new CustomToolTiers(7, 1561, 8.0F, 3.0F, 10, Ingredient.of(ModItems.ABYSSAL_INGOT.get()));
    public static final CustomToolTiers ETHER = new CustomToolTiers(8, 1561, 8.0F, 3.0F, 10, Ingredient.of(ModItems.ETHER_INGOT.get()));

    private final int harvestLevel;
    private final int maxUses;
    private final float efficiency;
    private final float attackDamage;
    private final int enchantability;
    private final Ingredient repairMaterial;

    public CustomToolTiers(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, Ingredient repairMaterial) {
        this.harvestLevel = harvestLevel;
        this.maxUses = maxUses;
        this.efficiency = efficiency;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairMaterial = repairMaterial;
    }

    @Override
    public int getUses() {
        return this.maxUses;
    }

    @Override
    public float getSpeed() {
        return this.efficiency;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    @Override
    public int getLevel() {
        return this.harvestLevel;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairMaterial;
    }

    @Override
    public @Nullable TagKey<Block> getTag() {
        return Tier.super.getTag();
    }
}
