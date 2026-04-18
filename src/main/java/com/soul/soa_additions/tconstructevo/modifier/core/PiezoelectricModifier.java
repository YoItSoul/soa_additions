package com.soul.soa_additions.tconstructevo.modifier.core;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Piezoelectric — converts melee damage dealt into RF, distributed across every
 * {@link IEnergyStorage}-capable item in the attacker's inventory (including
 * the struck tool if it has capacity). Mirrors 1.12.2 {@code TraitPiezoelectric}:
 * proportional split based on each recipient's missing headroom, only on fully
 * charged hits.
 */
public class PiezoelectricModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F || !context.isFullyCharged()) return;
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || attacker.level().isClientSide) return;
        if (!(attacker instanceof Player player)) return;

        int energy = (int) Math.ceil(damageDealt * TConEvoConfig.PIEZOELECTRIC_RF_PER_DAMAGE.get() * modifier.getLevel());
        if (energy <= 0) return;

        List<IEnergyStorage> recipients = new ArrayList<>();
        List<Integer> headroom = new ArrayList<>();
        long totalMissing = 0L;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            IEnergyStorage cap = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (cap == null || !cap.canReceive()) continue;
            int missing = cap.receiveEnergy(Integer.MAX_VALUE, true);
            if (missing <= 0) continue;
            recipients.add(cap);
            headroom.add(missing);
            totalMissing += missing;
        }
        if (recipients.isEmpty() || totalMissing == 0L) return;

        for (int i = 0; i < recipients.size(); i++) {
            int share = (int) Math.round((double) energy * headroom.get(i) / totalMissing);
            if (share > 0) recipients.get(i).receiveEnergy(share, false);
        }
    }
}
