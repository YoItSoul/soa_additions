package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
 * FluxBurn — on-hit, drains RF/OP from each piece of the target's armour and
 * converts the burned total into bonus magic damage. We look up DE's
 * {@link CapabilityOP#OP} first so Draconic shield/energy buffers are drained
 * at OP (long) precision, and fall back to Forge's {@link ForgeCapabilities#ENERGY}
 * for mundane RF armour. This matches the 1.12.2 "shield RF first, RF fallback"
 * priority while letting DE own the storage math.
 */
public class FluxBurnModifier extends Modifier implements MeleeHitModifierHook {

    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || attacker == null || target.level().isClientSide()) return;

        int level = modifier.getLevel();
        float fraction = (float) (TConEvoConfig.FLUX_BURN_FRACTION_PER_LEVEL.get() * level);
        if (fraction <= 0) return;
        int minBurn = TConEvoConfig.FLUX_BURN_MIN_PER_LEVEL.get() * level;
        int rawMax = TConEvoConfig.FLUX_BURN_MAX_PER_LEVEL.get();
        int maxBurn = rawMax > 0 ? rawMax * level : Integer.MAX_VALUE;

        long totalBurned = 0L;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            IOPStorage op = stack.getCapability(CapabilityOP.OP).orElse(null);
            if (op != null && op.canExtract()) {
                long stored = op.getOPStored();
                if (stored <= 0) continue;
                long burn = Math.min(Math.max((long) Math.ceil(stored * (double) fraction), minBurn), maxBurn);
                long extracted = op.extractOP(burn, false);
                if (extracted > 0) totalBurned += extracted;
                continue;
            }
            IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (energy == null || !energy.canExtract()) continue;
            int stored = energy.getEnergyStored();
            if (stored <= 0) continue;
            int target_burn = (int) Math.min(Math.max((long) Math.ceil(stored * fraction), minBurn), maxBurn);
            int extracted = energy.extractEnergy(target_burn, false);
            if (extracted > 0) totalBurned += extracted;
        }

        if (totalBurned <= 0) return;
        double energyPerDamage = TConEvoConfig.FLUX_BURN_ENERGY_PER_DAMAGE.get();
        float bonus = (float) (totalBurned / energyPerDamage);
        if (bonus < 0.01F) return;
        target.invulnerableTime = 0;
        target.hurt(target.damageSources().magic(), bonus);
    }

    @Override
    public int getPriority() {
        return 17;
    }
}
