package com.soul.soa_additions.tconstructevo.integration.bloodmagic;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

/**
 * Sentient — scales melee damage with the wielder's demon-will aura. We read
 * the largest active will type and its total via Blood Magic's
 * {@link PlayerDemonWillHandler}, bucket the total into tiers, and add flat
 * bonus damage to the swing. BM owns the will state and its sync; this
 * modifier is purely a scaling layer riding on {@code MELEE_DAMAGE}.
 *
 * <p>Per-type effects (corrosive poison, steadfast absorption, etc.) from the
 * 1.12.2 original are intentionally deferred: base damage scaling already
 * ports the bulk of the feel without diverging from BM's own data model.</p>
 */
public class SentientModifier extends Modifier implements MeleeDamageModifierHook {

    private static final int MAX_TIER = 8;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity attacker = context.getAttacker();
        if (!(attacker instanceof Player player)) return damage;
        EnumDemonWillType type = PlayerDemonWillHandler.getLargestWillType(player);
        if (type == null) return damage;
        double will = PlayerDemonWillHandler.getTotalDemonWill(type, player);
        if (will <= 0) return damage;
        double step = TConEvoConfig.SENTIENT_TIER_STEP.get();
        int tier = (int) Math.min(MAX_TIER, Math.floor(will / Math.max(step, 1e-9D)));
        if (tier <= 0) return damage;
        double bonus = tier * TConEvoConfig.SENTIENT_DAMAGE_PER_TIER.get() * modifier.getLevel();
        return damage + (float) bonus;
    }

    @Override
    public int getPriority() {
        return 110;
    }
}
