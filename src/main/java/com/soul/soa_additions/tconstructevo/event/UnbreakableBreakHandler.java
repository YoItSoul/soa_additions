package com.soul.soa_additions.tconstructevo.event;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import com.soul.soa_additions.tconstructevo.integration.avaritia.AvaritiaModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

/**
 * Server-side handler that lets Omnipotence-carrying tools break otherwise
 * unbreakable blocks (hardness &lt; 0). Replaces the 1.12.2 coremod ASM patch
 * {@code TransformBreakUnbreakable} which redirected
 * {@code ForgeHooks.blockStrength}. On 1.20.1 the same effect is achieved
 * cleanly by intercepting {@link PlayerInteractEvent.LeftClickBlock} — that
 * event fires before vanilla's hardness-aborts-break check, so we can
 * manually drop the block and cancel the interaction.
 *
 * <p>Gated by {@link TConEvoConfig#BREAK_UNBREAKABLE} and the
 * {@link TConEvoConfig#BREAK_UNBREAKABLE_HARVEST_LEVEL} minimum mining
 * tier. Only fires when Avaritia is loaded so the Omnipotence modifier
 * exists; otherwise the handler short-circuits immediately.</p>
 */
public final class UnbreakableBreakHandler {

    private UnbreakableBreakHandler() {}

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!TConEvoConfig.BREAK_UNBREAKABLE.get()) return;
        if (!ModList.get().isLoaded("avaritia")) return;
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;
        if (state.getDestroySpeed(world, pos) >= 0) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IModifiable)) return;

        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return;
        if (tool.getModifiers().getLevel(new ModifierId(AvaritiaModifiers.OMNIPOTENCE.getId())) <= 0) return;

        Tier tier = tool.getStats().get(ToolStats.HARVEST_TIER);
        int required = TConEvoConfig.BREAK_UNBREAKABLE_HARVEST_LEVEL.get();
        if (tier == null || tier.getLevel() < required) return;

        world.destroyBlock(pos, !player.isCreative(), player);
        event.setCanceled(true);
    }
}
