package com.soul.soa_additions.taiga;

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

public final class TaigaBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TaigaItems.TAIGA_ID);

    private static final BlockBehaviour.Properties ORE_PROPS =
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)
                    .mapColor(MapColor.STONE)
                    .strength(50f, 1200f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE);

    private static final BlockBehaviour.Properties METAL_BLOCK_PROPS =
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK)
                    .mapColor(MapColor.METAL)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops();

    public static final RegistryObject<Block> ABYSSUM_ORE    = registerBlock("abyssum_ore",    () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> AURORIUM_ORE   = registerBlock("aurorium_ore",   () -> new Block(ORE_PROPS));
    // crystal ores drop XP like vanilla gem ores (1.12 BlockTiberium parity)
    public static final RegistryObject<Block> DILITHIUM_ORE  = registerBlock("dilithium_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(ORE_PROPS,
                    net.minecraft.util.valueproviders.UniformInt.of(2, 5)));
    public static final RegistryObject<Block> DURANITE_ORE   = registerBlock("duranite_ore",   () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> EEZO_ORE       = registerBlock("eezo_ore",       () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> JAUXUM_ORE     = registerBlock("jauxum_ore",     () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> KARMESINE_ORE  = registerBlock("karmesine_ore",  () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> OSRAM_ORE      = registerBlock("osram_ore",      () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> OVIUM_ORE      = registerBlock("ovium_ore",      () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> PALLADIUM_ORE  = registerBlock("palladium_ore",  () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> PROMETHEUM_ORE = registerBlock("prometheum_ore", () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> TIBERIUM_ORE   = registerBlock("tiberium_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(ORE_PROPS,
                    net.minecraft.util.valueproviders.UniformInt.of(2, 5)));
    public static final RegistryObject<Block> VALYRIUM_ORE   = registerBlock("valyrium_ore",   () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> VIBRANIUM_ORE  = registerBlock("vibranium_ore",  () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> BASALT_BLOCK   = registerBlock("basalt_block",   () -> new Block(ORE_PROPS));
    public static final RegistryObject<Block> METEORITE_BLOCK = registerBlock("meteorite_block", () -> new Block(ORE_PROPS));

    private static final BlockBehaviour.Properties COBBLE_PROPS =
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.COBBLESTONE)
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 10.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE);

    /** Moon-rock cobbles — meteorite/obsidiorite blocks drop these when mined (1.12 breakMoonRock). */
    public static final RegistryObject<Block> METEORITE_COBBLE =
            registerBlock("meteorite_cobble", () -> new Block(COBBLE_PROPS));
    public static final RegistryObject<Block> OBSIDIORITE_COBBLE =
            registerBlock("obsidiorite_cobble", () -> new Block(COBBLE_PROPS));

    /** Storage blocks for every metal material (original TAIGA 1.12 {mat}_block roster; basalt/
     *  meteorite have world-block forms above, obsidiorite/uru still live in soa_additions:). */
    private static final String[] STORAGE_MATERIALS = {
            "abyssum", "adamant", "astrium", "aurorium", "dilithium", "duranite",
            "dyonite", "eezo", "fractum", "ignitz", "imperomite", "iox", "jauxum",
            "karmesine", "lumix", "nihilite", "niob", "nucleum", "osram", "ovium",
            "palladium", "prometheum", "proxii", "seismum", "solarium", "terrax",
            "tiberium", "triberium", "tritonite", "valyrium", "vibranium", "violium",
            "yrdeen"
    };

    static {
        for (String mat : STORAGE_MATERIALS) {
            registerBlock(mat + "_block", () -> new Block(METAL_BLOCK_PROPS));
        }
    }

    private TaigaBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        TaigaItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
