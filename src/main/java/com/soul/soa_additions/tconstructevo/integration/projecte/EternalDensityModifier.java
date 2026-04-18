package com.soul.soa_additions.tconstructevo.integration.projecte;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Eternal Density — converts dealt damage into EMC injected into any
 * {@link IItemEmcHolder} the wielder carries (Gem of Eternal Density, EMC
 * battery, etc.). Distribution is proportional to how much room each holder
 * still has, so full containers stay full and empty containers fill faster.
 * Only the EMC-transfer arm of the original 1.12.2 trait is ported here; the
 * dense-block mining speed / auto-harvest leg needs a ProjectE Equivalent-
 * Exchange block tier registry that 1.20.1 ProjectE no longer exposes.
 */
public class EternalDensityModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;
        LivingEntity attacker = context.getAttacker();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        long amount = (long) Math.ceil(damageDealt * TConEvoConfig.ETERNAL_DENSITY_EMC_PER_DAMAGE.get() * modifier.getLevel());
        if (amount <= 0) return;
        distributeEmc(player, amount);
    }

    private static void distributeEmc(Player player, long amount) {
        record Recipient(ItemStack stack, IItemEmcHolder holder, long missing) {}
        java.util.List<Recipient> recipients = new java.util.ArrayList<>();
        long totalMissing = 0L;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            IItemEmcHolder holder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).orElse(null);
            if (holder == null) continue;
            long missing = holder.getMaximumEmc(stack) - holder.getStoredEmc(stack);
            if (missing <= 0) continue;
            recipients.add(new Recipient(stack, holder, missing));
            totalMissing += missing;
        }
        if (totalMissing <= 0) return;
        for (Recipient r : recipients) {
            long share = Math.round(amount * ((double) r.missing / totalMissing));
            if (share > 0) r.holder.insertEmc(r.stack, share, IEmcStorage.EmcAction.EXECUTE);
        }
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
