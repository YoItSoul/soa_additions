package com.soul.soa_additions.bloodarsenal.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

/**
 * Hosts imperfect-ritual activation on Blood Magic's own blank Ritual Stone.
 *
 * <p>BM 1.20 kept the imperfect-ritual API ({@link ImperfectRitual},
 * {@link IImperfectRitualStone}, RitualManager's imperfect registry) but never
 * re-added the dedicated Imperfect Ritual Stone block, so the four rituals this
 * pack ports were unreachable. Rather than adding a new block, activation is:
 * place the ritual's topper block directly above a blank
 * {@code bloodmagic:ritualstone} and right-click the stone with an EMPTY main
 * hand. The empty-hand gate keeps the Ritual Diviner's own right-click
 * behaviour on ritual stones untouched.</p>
 *
 * <p>1.12 parity: LP is syphoned (with health as fallback) and the topper
 * block is consumed on success.</p>
 */
public final class ImperfectRitualStoneHandler {

    private static final ResourceLocation BLANK_RITUAL_STONE =
            ResourceLocation.fromNamespaceAndPath("bloodmagic", "ritualstone");

    private ImperfectRitualStoneHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (!player.getMainHandItem().isEmpty()) return;

        Level level = event.getLevel();
        BlockState stone = level.getBlockState(event.getPos());
        if (!BLANK_RITUAL_STONE.equals(ForgeRegistries.BLOCKS.getKey(stone.getBlock()))) return;

        BlockPos topperPos = event.getPos().above();
        ImperfectRitual ritual = BloodMagic.RITUAL_MANAGER.getImperfectRitual(level.getBlockState(topperPos));
        if (ritual == null) return;
        if (!BloodMagic.RITUAL_MANAGER.enabled(BloodMagic.RITUAL_MANAGER.getId(ritual), true)) return;

        // Claim the interaction on both sides so the hand doesn't swing twice.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        if (level.isClientSide()) return;

        if (!NetworkHelper.getSoulNetwork(player)
                .syphonAndDamage(player, new SoulTicket(ritual.getActivationCost())).isSuccess()) return;

        if (ritual.onActivate(new StoneAdapter(level, event.getPos()), player)) {
            level.removeBlock(topperPos, false);
        }
    }

    private record StoneAdapter(Level level, BlockPos pos) implements IImperfectRitualStone {
        @Override
        public boolean performRitual(Level world, BlockPos position, ImperfectRitual ritual, Player player) {
            return ritual.onActivate(this, player);
        }

        @Override
        public Level getRitualWorld() {
            return level;
        }

        @Override
        public BlockPos getRitualPos() {
            return pos;
        }
    }
}
