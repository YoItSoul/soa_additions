package com.soul.soa_additions.registry;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Ports GC's scripts/tweaks/harvestlevel.zs hardness overrides. Harvest-level
 * gating is handled by datapack tags (SoaTiers); this class only covers the
 * destroyTime bumps that GC applied to make bedrock/barrier/etc. take longer to mine.
 * Bedrock's vanilla destroyTime is -1 (unbreakable) — overriding it to 500 makes
 * it breakable with the appropriate tool tier (mythical, per the tag).
 *
 * In 1.20.1 the per-state destroy speed is cached on {@code BlockBehaviour$BlockStateBase}
 * (SRG field {@code f_60599_}); it's no longer a field on BlockBehaviour itself, so
 * the override has to walk every BlockState of the block and patch its cache.
 */
public final class HardnessOverrides {

    private static final Logger LOG = LogUtils.getLogger();

    /** Cached destroy-speed field on BlockStateBase. SRG name works in both
     *  dev (where ObfuscationReflectionHelper maps SRG → official) and prod
     *  (where the runtime field is already SRG-named). */
    private static final Field DESTROY_SPEED_FIELD;
    static {
        Field f = null;
        try {
            f = ObfuscationReflectionHelper.findField(BlockBehaviour.BlockStateBase.class, "f_60599_");
        } catch (Throwable t) {
            LOG.error("[soa_additions] Could not find BlockStateBase destroy-speed field (f_60599_); hardness overrides disabled: {}", t.toString());
        }
        DESTROY_SPEED_FIELD = f;
    }

    private HardnessOverrides() {}

    public static void apply() {
        if (DESTROY_SPEED_FIELD == null) return;
        set(Blocks.BEDROCK, 500.0f);
        set(Blocks.END_PORTAL_FRAME, 60.0f);
        set(Blocks.BARRIER, 1000.0f);
        set(Blocks.SPAWNER, 100.0f);
        setById("quark:monster_box", 150.0f);
    }

    private static void set(Block block, float destroyTime) {
        try {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                DESTROY_SPEED_FIELD.setFloat(state, destroyTime);
            }
        } catch (ReflectiveOperationException e) {
            LOG.error("[soa_additions] Failed to set destroy speed on {}: {}",
                    BuiltInRegistries.BLOCK.getKey(block), e.toString());
        }
    }

    private static void setById(String id, float destroyTime) {
        ResourceLocation rl = new ResourceLocation(id);
        Block block = BuiltInRegistries.BLOCK.get(rl);
        if (block == Blocks.AIR) return;
        set(block, destroyTime);
    }
}
