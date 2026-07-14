package com.soul.soa_additions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deterministic, stateless NxN screen detection for Task Collectors. A block
 * "anchors" a screen when its in-plane left and down neighbours are not
 * matching collectors (bottom-left corner). The screen size is the largest
 * complete NxN square of matching collectors extending right/up from the
 * anchor. Non-anchor blocks covered by their anchor's square render nothing;
 * blocks outside any complete square (ragged shapes) render as 1×1.
 *
 * <p>Pure world reads — no formation state, no packets; server and client
 * independently agree because the algorithm is deterministic.</p>
 */
public final class ScreenGeometry {

    private ScreenGeometry() {}

    public static final int MAX_SIZE = 7;

    public record Screen(BlockPos anchor, int size) {}

    private static boolean matches(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState s = level.getBlockState(pos);
        return s.getBlock() instanceof TaskCollectorBlock && s.getValue(TaskCollectorBlock.FACING) == facing;
    }

    /** In-plane "right" axis as seen by a viewer looking at the screen face. */
    public static Direction rightOf(Direction facing) { return facing.getCounterClockWise(); }

    /** Walk to this block's anchor (leftmost, then bottommost run). */
    public static BlockPos anchorOf(BlockGetter level, BlockPos pos, Direction facing) {
        Direction right = rightOf(facing);
        BlockPos p = pos;
        int guard = 0;
        while (guard++ < MAX_SIZE && matches(level, p.relative(right.getOpposite()), facing)) p = p.relative(right.getOpposite());
        guard = 0;
        while (guard++ < MAX_SIZE && matches(level, p.below(), facing)) p = p.below();
        return p;
    }

    /** Largest complete NxN square of matching collectors anchored (bottom-left) at {@code anchor}. */
    public static int sizeAt(BlockGetter level, BlockPos anchor, Direction facing) {
        Direction right = rightOf(facing);
        int best = 1;
        for (int n = 2; n <= MAX_SIZE; n++) {
            boolean ok = true;
            for (int r = 0; r < n && ok; r++) {
                for (int u = 0; u < n && ok; u++) {
                    if (!matches(level, anchor.relative(right, r).above(u), facing)) ok = false;
                }
            }
            if (!ok) break;
            best = n;
        }
        return best;
    }

    /**
     * The screen this block belongs to, or a 1×1 screen of itself when it
     * isn't covered by its anchor's square. Returns {@code null} when the
     * block is covered but NOT the anchor (someone else draws it).
     */
    public static Screen screenFor(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos anchor = anchorOf(level, pos, facing);
        int n = sizeAt(level, anchor, facing);
        if (anchor.equals(pos)) return new Screen(anchor, n);
        // covered by the anchor's square?
        Direction right = rightOf(facing);
        int dr = horizontalOffset(anchor, pos, right);
        int du = pos.getY() - anchor.getY();
        if (dr >= 0 && dr < n && du >= 0 && du < n) return null;   // anchor draws us
        return new Screen(pos, 1);                                  // ragged remainder: draw self
    }

    private static int horizontalOffset(BlockPos anchor, BlockPos pos, Direction right) {
        return (pos.getX() - anchor.getX()) * right.getStepX()
             + (pos.getZ() - anchor.getZ()) * right.getStepZ();
    }
}
