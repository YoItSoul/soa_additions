package com.soul.soa_additions.compat.jer;

import com.soul.soa_additions.SoaAdditions;
import jeresources.api.IJERAPI;
import jeresources.api.IWorldGenRegistry;
import jeresources.api.distributions.DistributionBase;
import jeresources.api.distributions.DistributionSquare;
import jeresources.api.drop.LootDrop;
import jeresources.api.restrictions.BiomeRestriction;
import jeresources.api.restrictions.DimensionRestriction;
import jeresources.api.restrictions.Restriction;
import jeresources.compatibility.api.JERAPI;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Just Enough Resources integration — declares where every SoA-shipped ore
 * spawns in JER's "World Gen" tab so players can look up depths/biomes/
 * dimensions without needing the wiki.
 *
 * <p><b>Why this isn't an {@code IJERPlugin}:</b> JER 1.20.1-1.4.0.247's
 * Forge plugin scanner ({@code jeresources.forge.ForgePlatformHelper.injectApi})
 * checks {@code AnnotationData.annotationType().equals(IJERPlugin.class)} —
 * comparing against the <i>interface</i> type, not the {@code @JERPlugin}
 * annotation. Forge records interface implementations in {@code getClasses()},
 * not {@code getAnnotations()}, so the lookup never matches anything and no
 * mod plugin actually gets registered. Looks like a leftover from Fabric-style
 * entrypoint discovery (the {@code IJERPlugin.entry_point = "jer_mod_plugin"}
 * constant matches Fabric's entrypoints key naming) that never got wired up
 * on the Forge side.</p>
 *
 * <p><b>Workaround:</b> we register directly against {@code JERAPI.getInstance()}
 * during {@link InterModProcessEvent}, which fires after all mods'
 * {@code FMLCommonSetupEvent} have completed (JER's singleton is initialized
 * by then). Same effect, no scanner involvement.</p>
 *
 * <p>Per-ore parameters mirror the corresponding placed-feature JSONs and
 * {@code tools/taiga_worldgen_1to1.py} source defaults; updating one without
 * the other will cause JER to lie.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JerIntegration {

    private JerIntegration() {}

    @SubscribeEvent
    public static void onInterModProcess(InterModProcessEvent event) {
        if (!ModList.get().isLoaded("jeresources")) return;
        IJERAPI api = JERAPI.getInstance();
        if (api == null) return;
        IWorldGenRegistry reg = api.getWorldGenRegistry();
        if (reg == null) return;
        registerAll(reg);
    }

    private static void registerAll(IWorldGenRegistry reg) {
        // ───── soa_additions ores ─────
        // Block ID                                count, size, minY, maxY,  restriction
        addOre(reg, "soa_additions:abyssal_ore_block",   12, 3,  -50, -10,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:aeroite_ore",         31, 8,  -64, 128,  Restriction.OVERWORLD);  // rarity 1/2 baked into count
        addOre(reg, "soa_additions:aqualite_ore",         9, 8,  -64,  40,  Restriction.OVERWORLD);
        addOreInDimension(reg, "soa_additions:asgardium_ore", 5, 4, 15, 90, "aether", "the_aether");
        addOre(reg, "soa_additions:chromium_ore",         9, 4,  -64,  24,  Restriction.OVERWORLD);  // rarity 1/3 baked into count
        addOre(reg, "soa_additions:cryonium_ore",        21, 4,  -64,  32,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:cytosinite_ore",       5, 4,  -64,  55,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:ether_ore_block",     16, 3,    0,  20,  Restriction.END);
        addOre(reg, "soa_additions:experience_ore",       3, 4,  -64,  32,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:manganese_ore",       24, 4,  -64,  36,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:infernium_ore_block",  8, 4,    1,  25,  Restriction.NETHER);
        addOre(reg, "soa_additions:shadowium_ore",        2, 4,  -64,  50,  Restriction.OVERWORLD);
        addOre(reg, "soa_additions:titanium_ore",         1, 4,    5,  80,  Restriction.END);
        addOre(reg, "soa_additions:void_ore_block",       4, 3,  -64, -60,  Restriction.OVERWORLD);
        // uru_ore is placed by the custom DuraniteMeteorFeature analog
        // (UruObsidioriteFeature) in the End at y=40..180, ~10% per chunk.
        addOre(reg, "soa_additions:uru_ore",              1, 1,   40, 180,  Restriction.END);

        // ───── TAIGA ports (canonical thingpack at data/taiga/) ─────
        // Stone variants — single-block placements in andesite/diorite/granite.
        addOre(reg, "taiga:karmesine_ore",   8, 1,   0,  96, Restriction.OVERWORLD);
        addOre(reg, "taiga:ovium_ore",       8, 1,   0,  96, Restriction.OVERWORLD);
        addOre(reg, "taiga:jauxum_ore",      8, 1,   0,  96, Restriction.OVERWORLD);

        // Vibranium primary — 5 specific biomes.
        addOreInBiomes(reg, "taiga:vibranium_ore", 16, 4, -64, 64,
                "minecraft:forest", "minecraft:plains", "minecraft:taiga",
                "minecraft:snowy_plains", "minecraft:desert");
        // Vibranium secondary — rare global, 1/7 chance per chunk.
        addOre(reg, "taiga:vibranium_ore",   2, 3, -64, 128, Restriction.OVERWORLD);

        // Dilithium — 7 specific biomes.
        addOreInBiomes(reg, "taiga:dilithium_ore", 24, 5, -64, 64,
                "minecraft:desert", "minecraft:forest", "minecraft:badlands",
                "minecraft:ocean", "minecraft:deep_ocean",
                "minecraft:frozen_ocean", "minecraft:mushroom_fields");

        // Special replacers (lava, bedrock).
        addOre(reg, "taiga:basalt_block",   24, 1, -64,  64, Restriction.OVERWORLD);  // replaces lava
        addOre(reg, "taiga:eezo_ore",        3, 1, -60, -50, Restriction.OVERWORLD);  // replaces bedrock

        // Nether ores.
        addOre(reg, "taiga:tiberium_ore",   15, 22,  32, 128, Restriction.NETHER);
        addOre(reg, "taiga:prometheum_ore", 18,  3,   0,  32, Restriction.NETHER);
        addOre(reg, "taiga:valyrium_ore",   10,  3,   0, 128, Restriction.NETHER);
        addOre(reg, "taiga:osram_ore",       1,  1,   0,  64, Restriction.NETHER);   // 1/7 lava-replace

        // End ores.
        addOre(reg, "taiga:aurorium_ore",   10, 3,  32, 48, Restriction.END);
        addOre(reg, "taiga:palladium_ore",  10, 3,  48, 64, Restriction.END);
        addOre(reg, "taiga:abyssum_ore",     4, 1,   0, 16, Restriction.END);

        // Duranite — meteor (custom Java feature). 6% chunk chance, y=16..112.
        // JER doesn't model meteors directly; treat as a rare ore with effective
        // y range so it shows up in the lookup tab.
        addOre(reg, "taiga:duranite_ore",    1, 5,  16, 112, Restriction.OVERWORLD);

        org.slf4j.LoggerFactory.getLogger(SoaAdditions.MODID)
                .info("[JER] registered ore worldgen entries");
    }

    /** Register an ore using the standard square distribution + block-as-drop. */
    private static void addOre(IWorldGenRegistry reg, String blockId,
                               int veinCount, int veinSize, int minY, int maxY,
                               Restriction restriction) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block == null) return; // mod absent or block id changed
        Item item = block.asItem();
        if (item == Items.AIR) return;
        ItemStack stack = new ItemStack(item);
        DistributionBase dist = new DistributionSquare(veinCount, veinSize, minY, maxY);
        reg.register(stack, dist, restriction, new LootDrop(stack));
    }

    /** Register an ore restricted to a specific (modded) dimension. */
    private static void addOreInDimension(IWorldGenRegistry reg, String blockId,
                                          int veinCount, int veinSize, int minY, int maxY,
                                          String dimNamespace, String dimPath) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block == null) return;
        Item item = block.asItem();
        if (item == Items.AIR) return;
        ItemStack stack = new ItemStack(item);
        DistributionBase dist = new DistributionSquare(veinCount, veinSize, minY, maxY);
        ResourceKey<net.minecraft.world.level.Level> dimKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(dimNamespace, dimPath));
        DimensionRestriction dimRestriction = new DimensionRestriction(dimKey);
        Restriction restriction = new Restriction(BiomeRestriction.NO_RESTRICTION, dimRestriction);
        reg.register(stack, dist, restriction, new LootDrop(stack));
    }

    /** Register an ore restricted to specific overworld biomes. */
    private static void addOreInBiomes(IWorldGenRegistry reg, String blockId,
                                       int veinCount, int veinSize, int minY, int maxY,
                                       String... biomeIds) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block == null) return;
        Item item = block.asItem();
        if (item == Items.AIR) return;
        ItemStack stack = new ItemStack(item);
        DistributionBase dist = new DistributionSquare(veinCount, veinSize, minY, maxY);

        @SuppressWarnings("unchecked")
        ResourceKey<Biome>[] keys = (ResourceKey<Biome>[]) new ResourceKey<?>[biomeIds.length];
        for (int i = 0; i < biomeIds.length; i++) {
            keys[i] = ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                    new ResourceLocation(biomeIds[i]));
        }
        ResourceKey<Biome>[] rest = java.util.Arrays.copyOfRange(keys, 1, keys.length);
        BiomeRestriction biomeRestriction = new BiomeRestriction(keys[0], rest);
        Restriction restriction = new Restriction(biomeRestriction, DimensionRestriction.OVERWORLD);
        reg.register(stack, dist, restriction, new LootDrop(stack));
    }
}
