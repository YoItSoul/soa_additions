package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.nyx.block.CrystalBlock;
import com.soul.soa_additions.nyx.block.LunarWaterCauldronBlock;
import com.soul.soa_additions.nyx.block.MeteorRockBlock;
import com.soul.soa_additions.nyx.block.StarAirBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Secondary block registry for the Nyx port. Reuses the shared ITEMS registry
 *  so block-items go through the same tab plumbing as ModBlocks. */
public final class NyxBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SoaAdditions.MODID);

    private static final BlockBehaviour.Properties METEOR_ROCK_PROPS =
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(32.0f, 3000.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties METEOR_GLASS_PROPS =
            BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .lightLevel(s -> 9)
                    .noOcclusion();

    private static final BlockBehaviour.Properties CRACKED_STAR_PROPS =
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0f, 6.0f)
                    .lightLevel(s -> 7)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties CRYSTAL_PROPS =
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 15);

    private static final BlockBehaviour.Properties LUNAR_CAULDRON_PROPS =
            BlockBehaviour.Properties.copy(Blocks.CAULDRON)
                    .lightLevel(s -> 13)
                    .noOcclusion();

    private static final BlockBehaviour.Properties STAR_AIR_PROPS =
            BlockBehaviour.Properties.copy(Blocks.AIR)
                    .lightLevel(s -> 15)
                    .noLootTable();

    public static final RegistryObject<Block> METEOR_ROCK =
            registerBlock("meteor_rock", () -> new MeteorRockBlock(METEOR_ROCK_PROPS));
    public static final RegistryObject<Block> GLEANING_METEOR_ROCK =
            registerBlock("gleaning_meteor_rock", () -> new MeteorRockBlock(METEOR_ROCK_PROPS));
    public static final RegistryObject<Block> METEOR_GLASS =
            registerBlock("meteor_glass", () -> new Block(METEOR_GLASS_PROPS));
    public static final RegistryObject<Block> CRACKED_STAR_BLOCK =
            registerBlock("cracked_star_block", () -> new Block(CRACKED_STAR_PROPS));
    public static final RegistryObject<Block> CRYSTAL =
            registerBlock("crystal", () -> new CrystalBlock(CRYSTAL_PROPS));
    public static final RegistryObject<Block> LUNAR_WATER_CAULDRON =
            registerBlock("lunar_water_cauldron", () -> new LunarWaterCauldronBlock(LUNAR_CAULDRON_PROPS));
    public static final RegistryObject<Block> STAR_AIR =
            registerBlockNoItem("star_air", () -> new StarAirBlock(STAR_AIR_PROPS));

    private NyxBlocks() {}

    public static void register(IEventBus bus) { BLOCKS.register(bus); }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> out = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name,
                () -> new BlockItem(out.get(), new Item.Properties().rarity(Rarity.RARE)));
        return out;
    }

    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }
}
