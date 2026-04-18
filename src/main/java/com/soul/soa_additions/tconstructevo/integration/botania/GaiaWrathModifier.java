package com.soul.soa_additions.tconstructevo.integration.botania;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.BotaniaItems;

/**
 * Gaia Wrath — left-click-empty spawns a Terra Blade-style mana burst that
 * one-shots at its minimum mana loss. We piggyback on TC3's existing
 * {@link GeneralInteractionModifierHook} wiring: the client-side
 * {@code ClientInteractionHandler} fires {@code InteractWithAirPacket.LEFT_CLICK}
 * on swing when the item is tagged {@code #tconstruct:interactable_left}, and
 * the server's {@code InteractionHandler.onLeftClickInteraction} routes it back
 * into this hook with {@link InteractionSource#LEFT_CLICK}. Terra swords and
 * the default Tinkers weapon family already carry that tag, so no client code
 * or custom packet is needed here.
 *
 * <p>The burst is a straight port of the 1.12.2 version: colour 2162464,
 * mana from config, min-mana-loss 40, gravity 0, velocity scaled 7×, and a
 * fake terra-sword source-lens stack so Botania's lens dispatch lines up.</p>
 */
public class GaiaWrathModifier extends Modifier implements GeneralInteractionModifierHook {

    private static final int BURST_COLOR = 2162464;
    private static final int MIN_MANA_LOSS = 40;
    private static final float MANA_LOSS_PER_TICK = 4.0F;
    private static final double VELOCITY_MULTIPLIER = 7.0D;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (source != InteractionSource.LEFT_CLICK) return InteractionResult.PASS;
        if (tool.isBroken()) return InteractionResult.PASS;
        Level level = player.level();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        int mana = TConEvoConfig.GAIA_WRATH_MANA_COST.get();
        ManaBurstEntity burst = new ManaBurstEntity(player);
        burst.setColor(BURST_COLOR);
        burst.setMana(mana);
        burst.setStartingMana(mana);
        burst.setMinManaLoss(MIN_MANA_LOSS);
        burst.setManaLossPerTick(MANA_LOSS_PER_TICK);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(VELOCITY_MULTIPLIER));

        ItemStack fakeLens = new ItemStack(BotaniaItems.terraSword);
        CompoundTag lensTag = fakeLens.getOrCreateTag();
        lensTag.putString("attackerUsername", player.getGameProfile().getName());
        burst.setSourceLens(fakeLens);
        level.addFreshEntity(burst);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                BotaniaSounds.terraBlade, SoundSource.PLAYERS, 0.4F, 1.4F);

        ToolDamageUtil.damageAnimated(tool, 1, player, source.getSlot(hand));
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getPriority() {
        return 25;
    }
}
