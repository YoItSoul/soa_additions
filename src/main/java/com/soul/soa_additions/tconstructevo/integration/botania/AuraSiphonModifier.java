package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.api.mana.ManaItemHandler;

/**
 * Aura Siphon — on-hit, feeds mana back into the wielder's mana-storage items
 * (wand pool, mana rings, mana tablets, etc.) proportional to damage dealt.
 * Uses Botania's {@link ManaItemHandler#dispatchMana} so whichever receptacle
 * Botania prefers wins — discounts, proficiency, and elven-flavour logic are
 * all owned by Botania.
 */
public class AuraSiphonModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;
        LivingEntity attacker = context.getAttacker();
        if (!(attacker instanceof Player player) || player.level().isClientSide()) return;
        int mana = (int) Math.round(damageDealt * TConEvoConfig.AURA_SIPHON_MULTIPLIER.get() * modifier.getLevel());
        if (mana <= 0) return;
        ManaItemHandler.instance().dispatchMana(player.getMainHandItem(), player, mana, true);
    }

    @Override
    public int getPriority() {
        return 40;
    }
}
