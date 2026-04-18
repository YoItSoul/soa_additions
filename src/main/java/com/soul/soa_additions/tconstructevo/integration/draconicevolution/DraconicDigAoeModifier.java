package com.soul.soa_additions.tconstructevo.integration.draconicevolution;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Draconic Dig AoE — after breaking a block, destroys every block inside a
 * cube centred on the primary break position, clamped to blocks whose destroy
 * speed is non-negative (so bedrock/barriers are preserved) and that are
 * {@linkplain BlockState#canHarvestBlock harvestable} by the player.
 *
 * <p>Recursion guarded by {@link ToolHarvestContext#isAOE()} — the hook only
 * fires its cube expansion on the <em>primary</em> break; each bonus break is
 * delegated to {@link Level#destroyBlock(BlockPos, boolean, net.minecraft.world.entity.Entity)},
 * which handles the drop without re-entering TC3's harvest pipeline. That
 * deliberately bypasses other tool modifiers on the bonus blocks — a single-
 * swing AoE should act like a worldedit sweep, not a recursive harvest chain.</p>
 *
 * <p>Side-channel note: the 1.12.2 DE version also widened attack AoE on the
 * same tool. See {@link DraconicAttackAoeModifier} for the melee side.</p>
 */
public class DraconicDigAoeModifier extends Modifier implements BlockBreakModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isAOE()) return;
        int radius = TConEvoConfig.DRACONIC_DIG_AOE_RADIUS_PER_LEVEL.get() * modifier.getLevel();
        if (radius <= 0) return;
        ServerPlayer player = context.getPlayer();
        if (player == null) return;
        Level level = context.getWorld();
        BlockPos origin = context.getPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getDestroySpeed(level, pos) < 0) continue;
                    level.destroyBlock(pos, true, player);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 21;
    }
}
