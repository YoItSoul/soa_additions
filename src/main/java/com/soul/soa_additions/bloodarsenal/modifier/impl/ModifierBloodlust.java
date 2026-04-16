package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.google.common.collect.Multimap;
import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;
import java.util.UUID;

/**
 * HEAD modifier — bloodlust multiplier builds on hits, decays over time.
 * Damage = floor(mult/4+1) * (6+0.5*(level+1)) * 1.1375^mult
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.modifiers.ModifierBloodlust</p>
 */
public class ModifierBloodlust extends Modifier {

    private static final UUID BLOODLUST_UUID = UUID.fromString("b4f3c2d5-6e7f-8091-a2b3-c4d5e6f7a8b9");
    private static final String TAG_MULT = "bloodlustMult";
    private static final Random random = new Random();

    private double multiplier = 0;

    public ModifierBloodlust() {
        super("bloodlust", EnumModifierType.HEAD, 5);
    }

    @Override
    public void onUpdate(Level level, Player player, ItemStack stack, int slotIndex, int modLevel) {
        if (level.isClientSide()) return;

        // Decay multiplier every 20 ticks (1 second), 50% chance
        if (level.getGameTime() % 20 == 0 && multiplier > 0 && random.nextInt(4) < 2) {
            multiplier = Math.max(multiplier - (0.1 + ((double) random.nextInt(5) / 100)), 0);
        }
    }

    @Override
    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target, int modLevel) {
        multiplier = getMultiplierCapped(multiplier + (random.nextDouble() * (modLevel + 1)) / 3, modLevel);
    }

    @Override
    public void getAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, int modLevel) {
        double damage = Math.floor(multiplier / 4 + 1) * (6 + 0.5 * (modLevel + 1)) * Math.pow(1.1375, multiplier);
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BLOODLUST_UUID,
                "Weapon modifier", damage, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {
        tag.putDouble("multiplier", multiplier);
    }

    @Override
    public void readSpecialNBT(CompoundTag tag) {
        multiplier = tag.getDouble("multiplier");
    }

    private double getMultiplierCapped(double mult, int level) {
        double max = switch (level + 1) {
            case 1 -> 4;
            case 2 -> 7;
            case 3 -> 10;
            case 4 -> 12;
            case 5 -> 14;
            case 6 -> 15;
            default -> 4;
        };
        return Math.min(mult, max);
    }
}
