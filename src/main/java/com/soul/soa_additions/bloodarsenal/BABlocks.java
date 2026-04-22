package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.bloodarsenal.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Deferred register for all Blood Arsenal blocks.
 */
public final class BABlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "bloodarsenal");

    // ── Wood family ──────────────────────────────────────────────────────

    private static final BlockBehaviour.Properties WOOD_PROPS = BlockBehaviour.Properties.copy(Blocks.STONE)
            .mapColor(MapColor.CRIMSON_NYLIUM)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0f, 5.0f)
            .sound(SoundType.WOOD)
            .ignitedByLava();

    public static final RegistryObject<Block> BLOOD_INFUSED_PLANKS = registerBlock("blood_infused_planks",
            () -> new Block(WOOD_PROPS));

    public static final RegistryObject<RotatedPillarBlock> BLOOD_INFUSED_LOG = registerBlock("blood_infused_log",
            () -> new RotatedPillarBlock(WOOD_PROPS));

    public static final RegistryObject<StairBlock> BLOOD_INFUSED_STAIRS = registerBlock("blood_infused_stairs",
            () -> new StairBlock(() -> BLOOD_INFUSED_PLANKS.get().defaultBlockState(), WOOD_PROPS));

    public static final RegistryObject<SlabBlock> BLOOD_INFUSED_SLAB = registerBlock("blood_infused_slab",
            () -> new SlabBlock(WOOD_PROPS));

    public static final RegistryObject<FenceBlock> BLOOD_INFUSED_FENCE = registerBlock("blood_infused_fence",
            () -> new FenceBlock(WOOD_PROPS));

    public static final RegistryObject<FenceGateBlock> BLOOD_INFUSED_FENCE_GATE = registerBlock("blood_infused_fence_gate",
            () -> new FenceGateBlock(WOOD_PROPS, net.minecraft.world.level.block.state.properties.WoodType.CRIMSON));

    // ── Metal / special ──────────────────────────────────────────────────

    public static final RegistryObject<Block> BLOOD_INFUSED_IRON_BLOCK = registerBlock("blood_infused_iron_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_RED)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BLOOD_INFUSED_GLOWSTONE = registerBlock("blood_infused_glowstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 15)));

    public static final RegistryObject<TorchBlock> BLOOD_TORCH = registerBlock("blood_torch",
            () -> new TorchBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noCollission()
                    .instabreak()
                    .lightLevel(s -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY),
                    net.minecraft.core.particles.ParticleTypes.FLAME));

    // ── Glass family ─────────────────────────────────────────────────────

    public static final RegistryObject<Block> BLOOD_STAINED_GLASS = registerBlock("blood_stained_glass",
            () -> new HalfTransparentBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, g, p, e) -> false)
                    .isRedstoneConductor((s, g, p) -> false)
                    .isSuffocating((s, g, p) -> false)
                    .isViewBlocking((s, g, p) -> false)));

    public static final RegistryObject<IronBarsBlock> BLOOD_STAINED_GLASS_PANE = registerBlock("blood_stained_glass_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    // ── Decorative ───────────────────────────────────────────────────────

    public static final RegistryObject<Block> SLATE = registerBlock("slate",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<GlassShardsBlock> GLASS_SHARDS = registerBlock("glass_shards",
            () -> new GlassShardsBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.NONE)
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<BloodBurnedStringBlock> BLOOD_BURNED_STRING_BLOCK = registerBlock("block_blood_burned_string",
            () -> new BloodBurnedStringBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE)
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.WOOL)));

    // ── Functional ───────────────────────────────────────────────────────

    public static final RegistryObject<StasisPlateBlock> STASIS_PLATE = registerBlock("stasis_plate",
            () -> new StasisPlateBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.NONE)
                    .strength(2.0f, 5.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<AltareBlock> ALTARE = registerBlock("altare_aenigmatica",
            () -> new AltareBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_RED)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<BloodCapacitorBlock> BLOOD_CAPACITOR = registerBlock("blood_capacitor",
            () -> new BloodCapacitorBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    // ── Fluid block (no BlockItem) ─────────────────────────────────────
    // Initialized in register() since it depends on BAFluids being set up
    static {
        BAFluids.REFINED_LIFE_ESSENCE_BLOCK = BLOCKS.register("refined_life_essence_block",
                () -> new LiquidBlock(BAFluids.REFINED_LIFE_ESSENCE_SOURCE,
                        BlockBehaviour.Properties.copy(Blocks.WATER)
                                .mapColor(MapColor.COLOR_RED)
                                .noLootTable()));
    }

    private BABlocks() {}

    static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> reg = BLOCKS.register(name, block);
        BAItems.ITEMS.register(name, () -> new BlockItem(reg.get(), new Item.Properties()));
        return reg;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
