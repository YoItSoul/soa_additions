package com.soul.soa_additions.compat;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MissingMappingsHandler {

    private static final Logger LOG = LoggerFactory.getLogger("SOA/MissingMappings");

    private static final Set<String> REMOVED_NAMESPACES = Set.of(
            "tconstruct", "mantle", "tinkersaether",
            "tinkers_things", "tinkerslevellingaddon", "tinkers_katanas",
            "tinkers_better_combat", "tinkersdelight", "jaopca",
            "tr"
    );

    private static final String MOLTEN_PREFIX = "molten_";

    /** knightslime was merged into Smithery's slimeknightium; see remapOrIgnore. */
    private static final String KNIGHTSLIME = "knightslime";
    private static final String SLIMEKNIGHTIUM = "slimeknightium";

    private static final Set<String> NYX_ITEMS = Set.of(
            "meteor_ingot", "meteor_dust", "meteor_shard", "fallen_star",
            "unrefined_crystal", "meteor_finder", "scythe",
            "meteor_pickaxe", "meteor_axe", "meteor_shovel", "meteor_hoe",
            "meteor_sword", "meteor_hammer", "meteor_bow",
            "meteor_helm", "meteor_chest", "meteor_pants", "meteor_boots",
            "lunar_water_bottle",
            "meteor_block", "star_block", "chiseled_star_block", "star_slab",
            "star_stairs", "meteor_rock", "gleaning_meteor_rock", "meteor_glass",
            "cracked_star_block", "crystal", "lunar_water_cauldron", "star_air"
    );

    private static final Set<String> NYX_ENTITIES = Set.of(
            "falling_star", "falling_meteor", "cauldron_tracker"
    );

    private static final Set<String> NYX_SOUNDS = Set.of(
            "lunar_water", "falling_star", "falling_star_impact",
            "falling_meteor", "falling_meteor_impact", "hammer_start", "hammer_end"
    );

    private static final Set<String> NYX_ENCHANTMENTS = Set.of(
            "lunar_edge", "lunar_shield"
    );

    private static final Set<String> TCONEVO_ITEMS = Set.of(
            "coalescence_matrix", "artifact_unsealer", "pink_metal_ingot",
            "bound_metal_ingot", "chaotic_ingot", "draconic_metal_ingot",
            "energetic_metal_ingot", "essence_metal_ingot", "primal_metal_ingot",
            "sentient_metal_ingot", "universal_metal_ingot", "wyvern_ingot",
            "bound_metal_nugget", "chaotic_nugget", "draconic_metal_nugget",
            "energetic_metal_nugget", "essence_metal_nugget", "primal_metal_nugget",
            "sentient_metal_nugget", "universal_metal_nugget", "wyvern_nugget",
            "bound_metal_dust", "chaotic_dust", "draconic_metal_dust",
            "energetic_metal_dust", "essence_metal_dust", "primal_metal_dust",
            "sentient_metal_dust", "universal_metal_dust", "wyvern_dust",
            "bound_metal_plate", "chaotic_plate", "draconic_metal_plate",
            "energetic_metal_plate", "essence_metal_plate", "primal_metal_plate",
            "sentient_metal_plate", "universal_metal_plate", "wyvern_plate",
            "bound_metal_gear", "chaotic_gear", "draconic_metal_gear",
            "energetic_metal_gear", "essence_metal_gear", "primal_metal_gear",
            "sentient_metal_gear", "universal_metal_gear", "wyvern_gear",
            "bound_metal_block", "chaotic_block", "draconic_metal_block",
            "energetic_metal_block", "essence_metal_block", "primal_metal_block",
            "sentient_metal_block", "universal_metal_block", "wyvern_block",
            "pink_slimy_mud"
    );

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remapOrIgnore(event, ForgeRegistries.Keys.BLOCKS, ForgeRegistries.BLOCKS);
        remapOrIgnore(event, ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS);
        remapOrIgnore(event, ForgeRegistries.Keys.FLUIDS, ForgeRegistries.FLUIDS);
        ignoreMoltenOrRemoved(event, ForgeRegistries.Keys.FLUID_TYPES);
        remapNyxRegistry(event, ForgeRegistries.Keys.ENTITY_TYPES, ForgeRegistries.ENTITY_TYPES, NYX_ENTITIES);
        ignoreMissing(event, ForgeRegistries.Keys.MOB_EFFECTS);
        ignoreMissing(event, ForgeRegistries.Keys.POTIONS);
        remapNyxRegistry(event, ForgeRegistries.Keys.SOUND_EVENTS, ForgeRegistries.SOUND_EVENTS, NYX_SOUNDS);
        remapNyxRegistry(event, ForgeRegistries.Keys.ENCHANTMENTS, ForgeRegistries.ENCHANTMENTS, NYX_ENCHANTMENTS);
        ignoreMissing(event, ResourceKey.createRegistryKey(
                ResourceLocation.fromNamespaceAndPath("mekanism", "slurry")));
    }

    private static <T> void remapOrIgnore(MissingMappingsEvent event,
                                           ResourceKey<? extends Registry<T>> registryKey,
                                           IForgeRegistry<T> registry) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registryKey)) {
            String ns = mapping.getKey().getNamespace();
            String path = mapping.getKey().getPath();

            if (REMOVED_NAMESPACES.contains(ns)) {
                mapping.ignore();
                continue;
            }

            // knightslime merged into slimeknightium. Smithery registers a part item and a molten
            // fluid per material, so dropping the duplicate orphans a couple of dozen smithery:
            // entries in any world that already had them — remap rather than strand a player on
            // the missing-registry screen. Covers parts (knightslime_pick_head) and fluids
            // (molten_knightslime) alike, since both just carry the material name in their path.
            if ("smithery".equals(ns) && path.contains(KNIGHTSLIME)) {
                ResourceLocation merged = ResourceLocation.fromNamespaceAndPath(
                        ns, path.replace(KNIGHTSLIME, SLIMEKNIGHTIUM));
                if (registry.containsKey(merged)) {
                    mapping.remap(registry.getValue(merged));
                    LOG.info("Remapped {} -> {}", mapping.getKey(), merged);
                } else {
                    mapping.ignore();
                    LOG.warn("No slimeknightium equivalent for {}, ignoring", mapping.getKey());
                }
                continue;
            }

            if ("soa_additions".equals(ns)) {
                if (path.startsWith(MOLTEN_PREFIX)) {
                    ResourceLocation smitheryId = ResourceLocation.fromNamespaceAndPath("smithery", path);
                    if (registry.containsKey(smitheryId)) {
                        mapping.remap(registry.getValue(smitheryId));
                        LOG.info("Remapped {} -> {}", mapping.getKey(), smitheryId);
                    } else {
                        mapping.ignore();
                        LOG.warn("No smithery equivalent for {}, ignoring", mapping.getKey());
                    }
                } else if (NYX_ITEMS.contains(path)) {
                    ResourceLocation nyxId = ResourceLocation.fromNamespaceAndPath("nyx", path);
                    if (registry.containsKey(nyxId)) {
                        mapping.remap(registry.getValue(nyxId));
                        LOG.info("Remapped {} -> {}", mapping.getKey(), nyxId);
                    } else {
                        mapping.ignore();
                        LOG.warn("No nyx equivalent for {}, ignoring", mapping.getKey());
                    }
                } else if (TCONEVO_ITEMS.contains(path)) {
                    ResourceLocation tconevoId = ResourceLocation.fromNamespaceAndPath("tconevo", path);
                    if (registry.containsKey(tconevoId)) {
                        mapping.remap(registry.getValue(tconevoId));
                        LOG.info("Remapped {} -> {}", mapping.getKey(), tconevoId);
                    } else {
                        mapping.ignore();
                        LOG.warn("No tconevo equivalent for {}, ignoring", mapping.getKey());
                    }
                }
            }
        }
    }

    private static <T> void ignoreMoltenOrRemoved(MissingMappingsEvent event,
                                                    ResourceKey<? extends Registry<T>> registryKey) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registryKey)) {
            String ns = mapping.getKey().getNamespace();
            if (REMOVED_NAMESPACES.contains(ns)) {
                mapping.ignore();
            } else if ("soa_additions".equals(ns) && mapping.getKey().getPath().startsWith(MOLTEN_PREFIX)) {
                mapping.ignore();
            }
        }
    }

    private static <T> void remapNyxRegistry(MissingMappingsEvent event,
                                               ResourceKey<? extends Registry<T>> registryKey,
                                               IForgeRegistry<T> registry,
                                               Set<String> nyxPaths) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registryKey)) {
            String ns = mapping.getKey().getNamespace();
            String path = mapping.getKey().getPath();
            if (REMOVED_NAMESPACES.contains(ns)) {
                mapping.ignore();
            } else if ("soa_additions".equals(ns) && nyxPaths.contains(path)) {
                ResourceLocation nyxId = ResourceLocation.fromNamespaceAndPath("nyx", path);
                if (registry.containsKey(nyxId)) {
                    mapping.remap(registry.getValue(nyxId));
                    LOG.info("Remapped {} -> {}", mapping.getKey(), nyxId);
                } else {
                    mapping.ignore();
                    LOG.warn("No nyx equivalent for {}, ignoring", mapping.getKey());
                }
            }
        }
    }

    private static <T> void ignoreMissing(MissingMappingsEvent event,
                                           ResourceKey<? extends Registry<T>> registryKey) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registryKey)) {
            if (REMOVED_NAMESPACES.contains(mapping.getKey().getNamespace())) {
                mapping.ignore();
            }
        }
    }

    private MissingMappingsHandler() {}
}
