package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.api.mana.ManaItemHandler;

/**
 * Mana-Infused — pays durability damage out of the wielder's mana pool. Every
 * absorbed point of damage consumes {@code manaInfusedCost} mana via Botania's
 * {@link ManaItemHandler#requestManaExactForTool}, which means Botania's own
 * proficiency/discount logic applies and player-held mana items stay the
 * source of truth.
 */
public class ManaInfusedModifier extends Modifier implements ToolDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (amount <= 0 || !(holder instanceof Player player) || player.level().isClientSide()) return amount;
        int costPerDamage = TConEvoConfig.MANA_INFUSED_COST.get();
        if (costPerDamage <= 0) return amount;
        ItemStack stack = player.getMainHandItem();
        int absorbed = 0;
        for (int i = 0; i < amount; i++) {
            if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, costPerDamage, true)) break;
            absorbed++;
        }
        return amount - absorbed;
    }

    @Override
    public int getPriority() {
        return 25;
    }
}
