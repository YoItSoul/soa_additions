package com.soul.soa_additions.tconstructevo.integration.greedy;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Poopy — tool drops a poop item (0.5%) on hostile-mob hit; armor applies
 * Nausea while equipped. INVENTORY_TICK selects armor slots only via the
 * !selected check (selected only true in the held mainhand).
 */
public class PoopyModifier extends Modifier implements MeleeHitModifierHook, InventoryTickModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!(ctx.getAttacker() instanceof Player p) || p.level().isClientSide()) return;
        if (!(ctx.getTarget() instanceof Mob)) return;
        if (p.getRandom().nextFloat() >= 0.005F) return;
        var poop = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("soa_additions", "poop"));
        if (poop != null) p.addItem(new ItemStack(poop));
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry mod, Level level, LivingEntity holder,
                                int slot, boolean selected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide() || selected) return;          // armor slots only
        if (slot < 36 || slot > 39) return;                    // inv slots 36..39 = boots..helmet
        holder.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, 0, false, false));
    }
}
