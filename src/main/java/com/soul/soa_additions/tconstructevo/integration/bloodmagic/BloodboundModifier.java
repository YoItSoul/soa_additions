package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

/**
 * Bloodbound — drains Life Points from the wielder's soul network in place of
 * durability damage. Uses Blood Magic's own {@link NetworkHelper} /
 * {@link SoulNetwork} so any LP orb / accessory the player already has is the
 * source of truth, and LP upgrades from BM modpacks apply transparently.
 */
public class BloodboundModifier extends Modifier implements ToolDamageModifierHook {

    private static final Component TICKET_DESC = Component.literal("tconevo.bloodbound");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (amount <= 0 || !(holder instanceof Player player) || player.level().isClientSide()) return amount;
        int unitCost = TConEvoConfig.BLOODBOUND_TOOL_COST.get();
        if (unitCost <= 0) return amount;
        int bloodCost = amount * unitCost;
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        if (network == null || network.getCurrentEssence() <= 0) return amount;
        int consumed = network.syphon(new SoulTicket(TICKET_DESC, bloodCost));
        if (consumed <= 0) return amount;
        return Math.max((int) Math.ceil(amount * (1.0F - (float) consumed / bloodCost)), 0);
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
