package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWillGem;
import wayoftime.bloodmagic.common.item.BloodMagicItems;

/**
 * Willful — rolls a drop of a pre-filled Petty Tartaric Gem on kill. The gem
 * item and will-filling math come from Blood Magic's {@link IDemonWillGem}
 * capability-style interface, so the drop's NBT is readable by every other
 * BM tool that consumes demon will.
 */
public class WillfulModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null || target.level().isClientSide()) return;
        if (target.isAlive()) return;
        double roll = TConEvoConfig.WILLFUL_DROP_PROBABILITY.get();
        if (target.getRandom().nextDouble() >= roll) return;

        ItemStack gem = new ItemStack(BloodMagicItems.PETTY_GEM.get());
        if (gem.getItem() instanceof IDemonWillGem gemItem) {
            double will = TConEvoConfig.WILLFUL_GEM_WILL_AMOUNT.get();
            if (will > 0) gemItem.fillWill(EnumDemonWillType.DEFAULT, gem, will, true);
        }
        target.spawnAtLocation(gem);
    }
}
