package com.soul.soa_additions.bloodarsenal.modifier.impl;

import com.soul.soa_additions.bloodarsenal.modifier.EnumModifierType;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

/**
 * HEAD modifier — chance to apply a stored negative potion effect on hit.
 * The potion is set via the Sanguine Infusion ritual using a potion item.
 * Incompatible with Flame.
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.modifiers.ModifierBadPotion</p>
 */
public class ModifierBadPotion extends Modifier {

    private static final Random random = new Random();

    public ModifierBadPotion() {
        super("bad_potion", EnumModifierType.HEAD, 5);
    }

    @Override
    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target, int modLevel) {
        // Chance check: random(level+1) >= random(maxLevel)
        if (random.nextInt(modLevel + 1) < random.nextInt(getMaxLevel())) return;

        if (!stack.hasTag()) return;
        CompoundTag data = stack.getTag().getCompound("ba_potion_itemstack");
        if (data.isEmpty()) return;

        ItemStack potionStack = ItemStack.of(data);
        if (potionStack.isEmpty()) return;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (effects.isEmpty()) return;

        MobEffect potion = effects.get(0).getEffect();

        if (potion.isInstantenous()) {
            potion.applyInstantenousEffect(player, player, target, modLevel, 1);
        } else {
            target.addEffect(new MobEffectInstance(potion, 20 + 40 * (modLevel + 1), modLevel));
        }
    }

    @Override
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {
        // Special NBT is written by the ritual when applying the modifier
    }

    @Override
    public void readSpecialNBT(CompoundTag tag) {
        // Read from stack tag directly in hitEntity
    }
}
