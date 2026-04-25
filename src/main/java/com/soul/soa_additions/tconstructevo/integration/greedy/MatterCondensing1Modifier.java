package com.soul.soa_additions.tconstructevo.integration.greedy;

import java.math.BigInteger;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Matter Condensing I — adds floor(damage * 0.1) personal EMC on hostile-mob hit. */
public class MatterCondensing1Modifier extends Modifier implements MeleeHitModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry mod, ToolAttackContext ctx, float dmg) {
        if (!(ctx.getAttacker() instanceof Player p) || p.level().isClientSide()) return;
        if (!(ctx.getTarget() instanceof Mob)) return;
        long emc = (long) Math.floor(dmg * 0.1);
        if (emc <= 0) return;
        p.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
            k.setEmc(k.getEmc().add(BigInteger.valueOf(emc)));
            if (p instanceof ServerPlayer sp) k.syncEmc(sp);
        });
    }
}
