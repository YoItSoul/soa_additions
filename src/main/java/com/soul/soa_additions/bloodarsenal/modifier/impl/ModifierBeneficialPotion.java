package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

/**
 * HANDLE modifier — periodically applies a stored potion effect from NBT.
 * The potion is set via the Sanguine Infusion ritual using a potion item.
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.modifiers.ModifierBeneficialPotion</p>
 */
public class ModifierBeneficialPotion extends Modifier {

    private static final Random random = new Random();

    public ModifierBeneficialPotion() {
        super("beneficial_potion", EnumModifierType.HANDLE, 5);
    }

    @Override
    public void onUpdate(Level level, Player player, ItemStack stack, int slotIndex, int modLevel) {
        if (level.isClientSide()) return;

        // Chance check: random(level+1) >= random(maxLevel)
        if (random.nextInt(modLevel + 1) < random.nextInt(getMaxLevel())) return;

        if (!stack.hasTag()) return;
        CompoundTag data = stack.getTag().getCompound("ba_potion_itemstack");
        if (data.isEmpty()) return;

        // Read potion effect from stored ItemStack NBT
        ItemStack potionStack = ItemStack.of(data);
        if (potionStack.isEmpty()) return;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (effects.isEmpty()) return;

        MobEffect potion = effects.get(0).getEffect();

        if (potion.isInstantenous() && level.getGameTime() % (40 * getMaxLevel() - 20 * modLevel) == 0) {
            potion.applyInstantenousEffect(player, player, player, modLevel, 1);
        } else {
            player.addEffect(new MobEffectInstance(potion, 20 + 40 * (modLevel + 1), modLevel));
        }
    }

    @Override
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {
        // Special NBT is written by the ritual when applying the modifier
    }

    @Override
    public void readSpecialNBT(CompoundTag tag) {
        // Read from stack tag directly in onUpdate
    }
}
