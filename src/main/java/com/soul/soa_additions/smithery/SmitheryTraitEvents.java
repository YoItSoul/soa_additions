package com.soul.soa_additions.smithery;

import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.SmitheryToolItem;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolStats;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Smithery trait behaviors that need raw Forge event access (e.g. event cancellation)
 * beyond what the Modifier onBlockBreak hook offers.
 *
 * <p>Mutate (TAIGA nucleum): 5% chance that breaking a "natural" block cancels the
 * break and transforms the block into a random other natural block. Ported from
 * {@code com.sosnitzka.taiga.traits.TraitMutate} (1.12): same 13-block pool, same
 * odds, break cancelled so nothing drops.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class SmitheryTraitEvents {

    private SmitheryTraitEvents() {}

    /** 1.20 equivalents of the 1.12 mutate pool (grass -> grass_block, snow -> snow_block, wheat kept). */
    private static final List<Block> MUTABLE = List.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.SAND,
            Blocks.GRASS_BLOCK, Blocks.CLAY, Blocks.NETHERRACK, Blocks.ICE,
            Blocks.SNOW_BLOCK, Blocks.MAGMA_BLOCK, Blocks.LAVA, Blocks.WATER,
            Blocks.WHEAT);

    @SubscribeEvent
    public static void onMutateBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        if (!MUTABLE.contains(event.getState().getBlock())) return;

        Level level = player.level();
        if (level.getRandom().nextFloat() <= 0.95f) return;
        if (!hasMutate(tool)) return;

        Block replacement = MUTABLE.get(level.getRandom().nextInt(MUTABLE.size()));
        event.setCanceled(true);
        level.setBlockAndUpdate(event.getPos(), replacement.defaultBlockState());
    }

    private static boolean hasMutate(ItemStack tool) {
        ToolComposition comp = SmitheryToolData.getComposition(tool);
        if (comp == null || !comp.isValid()) return false;
        ToolStats stats = ToolStats.compute(comp, SmitheryToolData.getAppliedModifiers(tool));
        for (ToolStats.ResolvedEffect r : stats.activeEffects) {
            if (SoaSmitheryModifiers.MUTATE.equals(r.effect().modifierId())) return true;
        }
        return false;
    }
}
