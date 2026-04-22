package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class NyxMaterials {

    public static final TagKey<Block> NEEDS_METEOR_TOOL = BlockTags.create(new ResourceLocation(SoaAdditions.MODID, "needs_meteor_tool"));

    // 1.12 Nyx: harvestLevel=3, uses=2341, efficiency=7.0, damage=7.0, enchantability=18.
    // ForgeTier damage = bonus over base (SwordItem adds 4 + tier damage for its final hit damage);
    // 3.0 here keeps sword total ~7.0, matching original.
    public static final Tier METEOR_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(3, 2341, 7.0f, 3.0f, 18, NEEDS_METEOR_TOOL, () -> Ingredient.EMPTY),
            new ResourceLocation(SoaAdditions.MODID, "meteor"),
            List.of(Tiers.DIAMOND),
            List.of(Tiers.NETHERITE));

    // 1.12 addArmorMaterial: durability=49, reductions[feet,legs,chest,head]=[4,8,10,4],
    //                       enchantability=18, sound=EQUIP_DIAMOND, toughness=3.0.
    private static final Map<ArmorItem.Type, Integer> DURABILITY = new EnumMap<>(ArmorItem.Type.class);
    private static final Map<ArmorItem.Type, Integer> PROTECTION = new EnumMap<>(ArmorItem.Type.class);
    static {
        DURABILITY.put(ArmorItem.Type.BOOTS, 13);
        DURABILITY.put(ArmorItem.Type.LEGGINGS, 15);
        DURABILITY.put(ArmorItem.Type.CHESTPLATE, 16);
        DURABILITY.put(ArmorItem.Type.HELMET, 11);
        PROTECTION.put(ArmorItem.Type.BOOTS, 4);
        PROTECTION.put(ArmorItem.Type.LEGGINGS, 8);
        PROTECTION.put(ArmorItem.Type.CHESTPLATE, 10);
        PROTECTION.put(ArmorItem.Type.HELMET, 4);
    }

    public static final ArmorMaterial METEOR_ARMOR = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY.get(type) * 49;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION.get(type);
        }

        @Override
        public int getEnchantmentValue() { return 18; }

        @Override
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_DIAMOND; }

        @Override
        public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }

        @Override
        public String getName() { return SoaAdditions.MODID + ":meteor"; }

        @Override
        public float getToughness() { return 3.0f; }

        @Override
        public float getKnockbackResistance() { return 0.0f; }
    };

    private NyxMaterials() {}

    public static void bootstrap() {}
}
