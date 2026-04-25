package com.soul.soa_additions.tr;

import com.soul.soa_additions.tr.block.AstralWardBlock;
import com.soul.soa_additions.tr.block.AstralWardBlockEntity;
import com.soul.soa_additions.tr.block.AstralWardBlockItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Thaumic Remnants block registrations. All blocks live in the {@code tr}
 * namespace via the dedicated DeferredRegister.
 */
public final class TrBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ThaumicRemnants.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ThaumicRemnants.MODID);

    public static final RegistryObject<AstralWardBlock> ASTRAL_WARD = BLOCKS.register("astral_ward",
            () -> new AstralWardBlock(Block.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)
                    .lightLevel(s -> 7)));

    public static final RegistryObject<BlockItem> ASTRAL_WARD_ITEM = ITEMS.register("astral_ward",
            () -> new AstralWardBlockItem(ASTRAL_WARD.get(), new Item.Properties()));

    private TrBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /**
     * O(N) over the chunk's loaded BlockEntities (typically <50). Returns true
     * if any AstralWardBlockEntity is present anywhere in the chunk's Y range.
     */
    public static boolean isChunkWarded(LevelChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof AstralWardBlockEntity) return true;
        }
        return false;
    }

    /**
     * Returns true if any loaded ward in this dimension is within 1 chunk of
     * {@code targetChunk} — covers the ward's own chunk plus the 8 neighbours.
     *
     * <p>Backed by {@link com.soul.soa_additions.tr.block.WardRegistry}, NOT
     * by chunk traversal. The previous implementation called
     * {@code ServerChunkCache.getChunk(x, z, false)} which deadlocked under
     * C2ME when invoked from inside a chunk-load event — even with
     * {@code load=false}, the call goes through {@code managedBlock} which
     * C2ME's mixin re-routes through an async wait that hangs forever when
     * the firing event already holds the chunk lock. The registry sidesteps
     * the chunk system entirely.
     */
    public static boolean isAreaWarded(ServerLevel level, ChunkPos targetChunk) {
        return com.soul.soa_additions.tr.block.WardRegistry
                .isChunkInWardArea(level.dimension(), targetChunk);
    }
}
