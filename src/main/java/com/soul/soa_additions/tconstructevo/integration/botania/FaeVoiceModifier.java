package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.PixieEntity;

/**
 * Fae Voice — chance on-hit to summon a Botania pixie that strikes the target.
 * Uses Botania's {@link PixieEntity} and {@link BotaniaEntities#PIXIE}
 * entity-type so the renderer, particle trail, and removal logic all come from
 * Botania. Damage and chance are configurable; pixie type 0 matches the wild
 * flower-forest palette.
 */
public class FaeVoiceModifier extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (target == null || !(attacker instanceof Player player)) return;
        Level level = player.level();
        if (level.isClientSide()) return;
        double chance = TConEvoConfig.FAE_VOICE_PIXIE_CHANCE.get() * modifier.getLevel();
        if (player.getRandom().nextDouble() >= chance) return;
        PixieEntity pixie = BotaniaEntities.PIXIE.create(level);
        if (pixie == null) return;
        pixie.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        pixie.setProps(target, player, 0, (float) (double) TConEvoConfig.FAE_VOICE_PIXIE_DAMAGE.get());
        level.addFreshEntity(pixie);
        target.invulnerableTime = 0;
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
