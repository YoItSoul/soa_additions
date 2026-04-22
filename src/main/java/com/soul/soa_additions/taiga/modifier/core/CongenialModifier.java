package com.soul.soa_additions.taiga.modifier.core;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * TAIGA "congenial": first kill binds the tool to that mob's display name.
 * After binding, damage against same-name targets scales ×(1..10), against
 * other targets scales /(5..9). Binding side effect lives in the event bus.
 */
public class CongenialModifier extends Modifier implements MeleeDamageModifierHook {
    public static final ResourceLocation KEY = new ResourceLocation("taiga", "congenial");
    private final Random rng = new Random();

    @Override protected void registerHooks(ModuleHookMap.Builder b) { b.addHook(this, ModifierHooks.MELEE_DAMAGE); }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        if (target == null) return damage;
        CompoundTag data = ((ToolStack) tool).getPersistentData().getCompound(KEY);
        String bound = data.getString("name");
        if (bound.isEmpty()) return damage;
        String current = target.getName().getString();
        if (bound.equals(current)) {
            return damage * (1.0F + rng.nextFloat() * 9.0F);
        }
        return damage / (rng.nextInt(5) + 5);
    }
}
