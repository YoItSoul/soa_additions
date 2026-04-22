package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "cursed": small accumulating chance to damage the holder with a
 * "Curse" damage source. The curse counter lives in the tool's persistent data.
 */
public class CursedModifier extends Modifier implements InventoryTickModifierHook {
    private static final ResourceLocation KEY = new ResourceLocation("taiga", "cursed_curse");
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.INVENTORY_TICK); }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide) return;
        CompoundTag data = ((ToolStack) tool).getPersistentData().getCompound(KEY);
        int curse = data.getInt("curse");
        if (rng.nextInt((60000 + curse) / (curse + 1)) == 1) {
            curse += isSelected ? 10 : 1;
            float dmg = rng.nextFloat() * holder.getMaxHealth() / 2.0F;
            holder.hurt(holder.damageSources().generic(), dmg);
            CompoundTag out = new CompoundTag();
            out.putInt("curse", curse);
            ((ToolStack) tool).getPersistentData().put(KEY, out);
        }
    }
}
