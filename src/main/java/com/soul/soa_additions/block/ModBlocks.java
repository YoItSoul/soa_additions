package com.soul.soa_additions.block;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SoaAdditions.MODID);

    private static final BlockBehaviour.Properties ORE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)
            .mapColor(MapColor.STONE)
            .strength(50f, 1200f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    // Obsidian-like shell host in End cube formations
    private static final BlockBehaviour.Properties OBSIDIORITE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OBSIDIAN)
            .mapColor(MapColor.COLOR_BLACK)
            .strength(50f, 1200f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    // Uru ore: End-exclusive, netherite tier
    private static final BlockBehaviour.Properties URU_ORE_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.OBSIDIAN)
            .mapColor(MapColor.COLOR_RED)
            .strength(60f, 1800f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE);

    private static final BlockBehaviour.Properties URU_BLOCK_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHERITE_BLOCK)
            .mapColor(MapColor.COLOR_RED)
            .strength(60f, 1800f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL);

    // Ores
    public static final RegistryObject<Block> INFERNIUM_ORE_BLOCK = registerBlock("infernium_ore_block", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> VOID_ORE_BLOCK      = registerBlock("void_ore_block",      () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ABYSSAL_ORE_BLOCK   = registerBlock("abyssal_ore_block",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ETHER_ORE_BLOCK     = registerBlock("ether_ore_block",     () -> new Block(ORE_PROPERTIES));
    // Thaumcraft's cinnabar ore, ported 1:1 from ThaumcraftWorldGenerator.generateOres: overworld
    // only (it returns early on dimension -1), single blocks rather than veins, 18 attempts per
    // chunk, y 0-51 (rand.nextInt(worldHeight / 5)). Mined it drops itself and smelts to quicksilver.
    public static final RegistryObject<Block> CINNABAR_ORE        = registerBlock("cinnabar_ore",        () -> new Block(ORE_PROPERTIES));
    // Thaumcraft's amber ore, same generateOres pass: 20 attempts per chunk, single blocks, but placed
    // relative to the terrain top (world.getHeight(x, z) - rand.nextInt(25)) rather than at a fixed
    // depth. Drops 1-2 amber; TC's 6.6% amber-curio swap has no 1.20 analogue and is omitted.
    public static final RegistryObject<Block> AMBER_ORE           = registerBlock("amber_ore",           () -> new Block(ORE_PROPERTIES));

    // End-exclusive taiga-style formations
    public static final RegistryObject<Block> OBSIDIORITE = registerBlock("obsidiorite", () -> new Block(OBSIDIORITE_PROPERTIES));
    public static final RegistryObject<Block> URU_ORE     = registerBlock("uru_ore",     () -> new Block(URU_ORE_PROPERTIES));
    public static final RegistryObject<Block> URU_BLOCK   = registerBlock("uru_block",   () -> new Block(URU_BLOCK_PROPERTIES));

    // TConEvo blocks moved to TConEvoBlocks.java (tconevo: namespace)
    // Nyx blocks moved to NyxBlocks.java (nyx: namespace)

    private static final BlockBehaviour.Properties METAL_BLOCK_PROPS = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
            .mapColor(MapColor.METAL)
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops();

    // ============================================================
    // Additions framework blocks — 1:1 port of GC Additions JSON pack.
    // ============================================================

    public static final RegistryObject<Block> AEROITE_ORE    = registerBlock("aeroite_ore",    () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> AQUALITE_ORE   = registerBlock("aqualite_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ASGARDIUM_ORE  = registerBlock("asgardium_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CHROMIUM_ORE   = registerBlock("chromium_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CRYONIUM_ORE   = registerBlock("cryonium_ore",   () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> CYTOSINITE_ORE = registerBlock("cytosinite_ore", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> EXPERIENCE_ORE = registerBlock("experience_ore", () -> new Block(ORE_PROPERTIES));
    // infernium_ore intentionally not registered — duplicates pre-existing INFERNIUM_ORE_BLOCK above.
    public static final RegistryObject<Block> MANGANESE_ORE  = registerBlock("manganese_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> SHADOWIUM_ORE  = registerBlock("shadowium_ore",  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> SHADOWNIUM_ORE = registerBlock("shadownium_ore", () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> TITANIUM_ORE   = registerBlock("titanium_ore",   () -> new Block(ORE_PROPERTIES));

    public static final RegistryObject<Block> AEONSTEEL_BLOCK             = registerBlock("aeonsteel_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> AQUALITE_BLOCK              = registerBlock("aqualite_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ASTRAL_METAL_BLOCK          = registerBlock("astral_metal_block",          () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CHROMASTEEL_BLOCK           = registerBlock("chromasteel_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> COSMILITE_BLOCK             = registerBlock("cosmilite_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CRIMSONITE_BLOCK            = registerBlock("crimsonite_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CRYONIUM_BLOCK              = registerBlock("cryonium_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> CYTOSINITE_BLOCK            = registerBlock("cytosinite_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DURASTEEL_BLOCK             = registerBlock("durasteel_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ELECTRONIUM_BLOCK           = registerBlock("electronium_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> EXPERIENCE_BLOCK_BLOCK      = registerBlock("experience_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> FUSION_MATRIX_BLOCK         = registerBlock("fusion_matrix_block",         () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> INFERNIUM_BLOCK             = registerBlock("infernium_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MANGANESE_STEEL_BLOCK       = registerBlock("manganese_steel_block",       () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MATTER_BLOCK                = registerBlock("matter_block",                () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> MODULARIUM_BLOCK            = registerBlock("modularium_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PROTONIUM_BLOCK             = registerBlock("protonium_block",             () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> STAINLESS_STEEL_BLOCK       = registerBlock("stainless_steel_block",       () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> TERRA_ALLOY_BLOCK           = registerBlock("terra_alloy_block",           () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> TITANIUM_BLOCK              = registerBlock("titanium_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> COMPRESSED_EXPERIENCE_BLOCK = registerBlock("compressed_experience_block", () -> new Block(METAL_BLOCK_PROPS));

    // GC greedycraft-hardened_stone: hardness 125, resistance 1000, diamond
    // harvest level (needs_diamond_tool tag). YUNG's Law seal block — see
    // worldgen/HardenedStoneSealFeature.
    public static final RegistryObject<Block> HARDENED_STONE              = registerBlock("hardened_stone",              () -> new Block(
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)
                    .mapColor(MapColor.STONE)
                    .strength(125f, 1000f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Block> INFINITY_BLOCK_BLOCK        = registerBlock("infinity_block_block",        () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> INFINITY_BLOCK_BLOCK_BLOCK  = registerBlock("infinity_block_block_block",  () -> new Block(METAL_BLOCK_PROPS));
    // Drops and tool requirement defer to whatever block it is standing in for.
    public static final RegistryObject<Block> UNKNOWN_BLOCK               = registerBlock("unknown_block",               () -> new UnknownBlock(ORE_PROPERTIES));

    // External 1.12 mod ports (Thermal Foundation, TConstruct, ExtraUtilities2)
    public static final RegistryObject<Block> IRIDIUM_BLOCK               = registerBlock("iridium_block",               () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> IRIDIUM_ORE                 = registerBlock("iridium_ore",                 () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> PLATINUM_BLOCK              = registerBlock("platinum_block",              () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> PLATINUM_ORE                = registerBlock("platinum_ore",                () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> ARDITE_BLOCK                = registerBlock("ardite_block",                () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> ARDITE_ORE                  = registerBlock("ardite_ore",                  () -> new Block(ORE_PROPERTIES));
    public static final RegistryObject<Block> EVIL_METAL_BLOCK            = registerBlock("evil_metal_block",            () -> new Block(METAL_BLOCK_PROPS));
    public static final RegistryObject<Block> DEMONIC_METAL_BLOCK         = registerBlock("demonic_metal_block",         () -> new Block(METAL_BLOCK_PROPS));

    // Tinkers' Aether port (1.12 tinkersaether → 1.20.1)
    public static final RegistryObject<Block> VALKYRIE_BLOCK              = registerBlock("valkyrie_block",              () -> new Block(METAL_BLOCK_PROPS));

    // Quest resource sink + task screen (FE / XP / Mana / EMC / item consume-tasks).
    // Place an NxN wall of them (same facing) to form one large screen.
    public static final RegistryObject<TaskCollectorBlock> TASK_COLLECTOR =
            registerBlock("task_collector", () -> new TaskCollectorBlock(METAL_BLOCK_PROPS));


    // GC "Tofu Machine Case" (1.12 tofucraft:tf_machine_case — TofuCraft Reload 1.20
    // dropped the casing intermediate; minted exactly, 1.12 texture). GC recipe:
    // ring of 8 Metal Tofu Blocks -> 8 (kubejs soa_reported_fixes.js).
    public static final RegistryObject<Block> TF_MACHINE_CASE =
            registerBlock("tf_machine_case", () -> new Block(METAL_BLOCK_PROPS));

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
