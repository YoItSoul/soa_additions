package com.soul.soa_additions.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * {@code /soa export <target>} — dumps registry contents to JSON files under
 * {@code <gamedir>/soa_exports/}. Useful for building wikis, checklists, or
 * feeding other tooling. Targets: {@code items}, {@code blocks}, {@code entities},
 * {@code structures}, {@code biomes}, {@code dimensions}, {@code all}.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class RegistryExportCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private RegistryExportCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soa")
                .then(Commands.literal("export")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> run(ctx.getSource(), "all"))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    for (String s : new String[]{"all", "items", "blocks", "entities",
                                            "structures", "biomes", "dimensions", "effects",
                                            "enchantments", "fluids", "sounds", "particles",
                                            "block_entities", "villager_professions", "tags"}) {
                                        b.suggest(s);
                                    }
                                    return b.buildFuture();
                                })
                                .executes(ctx -> run(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))));
    }

    private static int run(CommandSourceStack src, String target) {
        MinecraftServer server = src.getServer();
        Path outDir = FMLPaths.GAMEDIR.get().resolve("soa_exports");
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            src.sendFailure(Component.literal("Could not create soa_exports dir: " + e.getMessage()));
            return 0;
        }

        Map<String, Integer> totals = new TreeMap<>();
        long started = System.currentTimeMillis();

        try {
            boolean all = "all".equalsIgnoreCase(target);
            if (all || "items".equalsIgnoreCase(target))
                totals.put("items", write(outDir.resolve("items.json"), dumpItems()));
            if (all || "blocks".equalsIgnoreCase(target))
                totals.put("blocks", write(outDir.resolve("blocks.json"), dumpBlocks()));
            if (all || "entities".equalsIgnoreCase(target))
                totals.put("entities", write(outDir.resolve("entities.json"), dumpEntities()));
            if (all || "structures".equalsIgnoreCase(target))
                totals.put("structures", write(outDir.resolve("structures.json"),
                        dumpDatapack(server, Registries.STRUCTURE)));
            if (all || "biomes".equalsIgnoreCase(target))
                totals.put("biomes", write(outDir.resolve("biomes.json"), dumpBiomes(server)));
            if (all || "dimensions".equalsIgnoreCase(target))
                totals.put("dimensions", write(outDir.resolve("dimensions.json"), dumpDimensions(server)));
            if (all || "effects".equalsIgnoreCase(target))
                totals.put("effects", write(outDir.resolve("mob_effects.json"), dumpIds(BuiltInRegistries.MOB_EFFECT)));
            if (all || "enchantments".equalsIgnoreCase(target))
                totals.put("enchantments", write(outDir.resolve("enchantments.json"), dumpIds(BuiltInRegistries.ENCHANTMENT)));
            if (all || "fluids".equalsIgnoreCase(target))
                totals.put("fluids", write(outDir.resolve("fluids.json"), dumpIds(BuiltInRegistries.FLUID)));
            if (all || "sounds".equalsIgnoreCase(target))
                totals.put("sounds", write(outDir.resolve("sounds.json"), dumpIds(BuiltInRegistries.SOUND_EVENT)));
            if (all || "particles".equalsIgnoreCase(target))
                totals.put("particles", write(outDir.resolve("particles.json"), dumpIds(BuiltInRegistries.PARTICLE_TYPE)));
            if (all || "block_entities".equalsIgnoreCase(target))
                totals.put("block_entities", write(outDir.resolve("block_entities.json"), dumpIds(BuiltInRegistries.BLOCK_ENTITY_TYPE)));
            if (all || "villager_professions".equalsIgnoreCase(target))
                totals.put("villager_professions", write(outDir.resolve("villager_professions.json"), dumpIds(BuiltInRegistries.VILLAGER_PROFESSION)));
            if (all || "tags".equalsIgnoreCase(target))
                totals.put("tags", write(outDir.resolve("tags.json"), dumpAllTags(server)));
        } catch (IOException e) {
            src.sendFailure(Component.literal("Export failed: " + e.getMessage()));
            return 0;
        }

        if (totals.isEmpty()) {
            src.sendFailure(Component.literal("Unknown export target: " + target));
            return 0;
        }

        JsonObject summary = new JsonObject();
        summary.addProperty("generated", OffsetDateTime.now().toString());
        summary.addProperty("target", target);
        JsonObject totalsJson = new JsonObject();
        totals.forEach(totalsJson::addProperty);
        summary.add("totals", totalsJson);
        try {
            Files.writeString(outDir.resolve("summary.json"), GSON.toJson(summary));
        } catch (IOException ignored) {}

        long ms = System.currentTimeMillis() - started;
        src.sendSuccess(() -> Component.literal("── SOA Registry Export ──").withStyle(ChatFormatting.GOLD), true);
        src.sendSuccess(() -> Component.literal("  output: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(outDir.toString()).withStyle(ChatFormatting.AQUA)), false);
        totals.forEach((k, v) -> src.sendSuccess(() -> Component.literal("  " + k + ": ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(v)).withStyle(ChatFormatting.GREEN)), false));
        src.sendSuccess(() -> Component.literal("  took " + ms + " ms").withStyle(ChatFormatting.DARK_GRAY), false);
        return Command.SINGLE_SUCCESS;
    }

    // ---------- dumpers ----------

    private static JsonArray dumpItems() {
        JsonArray arr = new JsonArray();
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(items::add);
        items.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString()));
        for (Item item : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            ItemStack stack = item.getDefaultInstance();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("class", item.getClass().getName());
            safe(() -> o.addProperty("name", stack.getHoverName().getString()));
            safe(() -> o.addProperty("max_stack", item.getMaxStackSize()));
            safe(() -> o.addProperty("max_damage", item.getMaxDamage()));
            safe(() -> o.addProperty("rarity", stack.getRarity().name()));
            safe(() -> o.addProperty("is_edible", item.isEdible()));
            safe(() -> o.addProperty("can_be_depleted", item.canBeDepleted()));
            safe(() -> o.addProperty("fire_resistant", item.isFireResistant()));
            if (item.isEdible()) {
                FoodProperties fp = item.getFoodProperties();
                if (fp != null) {
                    JsonObject food = new JsonObject();
                    food.addProperty("nutrition", fp.getNutrition());
                    food.addProperty("saturation", fp.getSaturationModifier());
                    food.addProperty("meat", fp.isMeat());
                    food.addProperty("always_eat", fp.canAlwaysEat());
                    food.addProperty("fast", fp.isFastFood());
                    o.add("food", food);
                }
            }
            arr.add(o);
        }
        return arr;
    }

    private static JsonArray dumpBlocks() {
        JsonArray arr = new JsonArray();
        List<Block> blocks = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(blocks::add);
        blocks.sort(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b).toString()));
        for (Block block : blocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            BlockState state = block.defaultBlockState();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("class", block.getClass().getName());
            safe(() -> o.addProperty("name", block.getName().getString()));
            safe(() -> o.addProperty("hardness", block.defaultDestroyTime()));
            safe(() -> o.addProperty("explosion_resistance", block.getExplosionResistance()));
            safe(() -> o.addProperty("friction", block.getFriction()));
            safe(() -> o.addProperty("jump_factor", block.getJumpFactor()));
            safe(() -> o.addProperty("speed_factor", block.getSpeedFactor()));
            safe(() -> o.addProperty("light_emission", state.getLightEmission()));
            safe(() -> o.addProperty("sound_type", block.getSoundType(state).getClass().getSimpleName()));
            arr.add(o);
        }
        return arr;
    }

    private static JsonArray dumpEntities() {
        JsonArray arr = new JsonArray();
        List<EntityType<?>> types = new ArrayList<>();
        BuiltInRegistries.ENTITY_TYPE.forEach(types::add);
        types.sort(Comparator.comparing(t -> BuiltInRegistries.ENTITY_TYPE.getKey(t).toString()));
        for (EntityType<?> et : types) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(et);
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("class", et.getBaseClass().getName());
            safe(() -> o.addProperty("category", et.getCategory().getName()));
            safe(() -> o.addProperty("width", et.getWidth()));
            safe(() -> o.addProperty("height", et.getHeight()));
            safe(() -> o.addProperty("fire_immune", et.fireImmune()));
            safe(() -> o.addProperty("summonable", et.canSummon()));
            safe(() -> o.addProperty("serialize", et.canSerialize()));
            safe(() -> o.addProperty("name", et.getDescription().getString()));
            safe(() -> o.addProperty("tracking_range", et.clientTrackingRange()));
            safe(() -> o.addProperty("update_interval", et.updateInterval()));
            arr.add(o);
        }
        return arr;
    }

    private static JsonArray dumpBiomes(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        Registry<Biome> reg = server.registryAccess().registryOrThrow(Registries.BIOME);
        List<ResourceLocation> ids = new ArrayList<>(reg.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation rl : ids) {
            Biome biome = reg.get(rl);
            JsonObject o = new JsonObject();
            o.addProperty("id", rl.toString());
            o.addProperty("mod", rl.getNamespace());
            if (biome != null) {
                safe(() -> o.addProperty("base_temperature", biome.getBaseTemperature()));
                safe(() -> o.addProperty("downfall", biome.getModifiedClimateSettings().downfall()));
                safe(() -> o.addProperty("precipitation", biome.hasPrecipitation()));
                safe(() -> o.addProperty("fog_color", biome.getFogColor()));
                safe(() -> o.addProperty("sky_color", biome.getSkyColor()));
                safe(() -> o.addProperty("water_color", biome.getWaterColor()));
                safe(() -> o.addProperty("water_fog_color", biome.getWaterFogColor()));
            }
            arr.add(o);
        }
        return arr;
    }

    private static JsonArray dumpDimensions(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        List<ResourceLocation> loaded = new ArrayList<>();
        for (ServerLevel lvl : server.getAllLevels()) {
            loaded.add(lvl.dimension().location());
        }
        Registry<net.minecraft.world.level.dimension.DimensionType> reg =
                server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        List<ResourceLocation> ids = new ArrayList<>(reg.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation rl : ids) {
            net.minecraft.world.level.dimension.DimensionType dt = reg.get(rl);
            JsonObject o = new JsonObject();
            o.addProperty("id", rl.toString());
            o.addProperty("mod", rl.getNamespace());
            o.addProperty("loaded_as_level", loaded.contains(rl));
            if (dt != null) {
                safe(() -> o.addProperty("has_skylight", dt.hasSkyLight()));
                safe(() -> o.addProperty("has_ceiling", dt.hasCeiling()));
                safe(() -> o.addProperty("ultra_warm", dt.ultraWarm()));
                safe(() -> o.addProperty("natural", dt.natural()));
                safe(() -> o.addProperty("coordinate_scale", dt.coordinateScale()));
                safe(() -> o.addProperty("bed_works", dt.bedWorks()));
                safe(() -> o.addProperty("respawn_anchor_works", dt.respawnAnchorWorks()));
                safe(() -> o.addProperty("has_raids", dt.hasRaids()));
                safe(() -> o.addProperty("min_y", dt.minY()));
                safe(() -> o.addProperty("height", dt.height()));
                safe(() -> o.addProperty("logical_height", dt.logicalHeight()));
                safe(() -> o.addProperty("ambient_light", dt.ambientLight()));
                safe(() -> o.addProperty("fixed_time_present", dt.fixedTime().isPresent()));
            }
            arr.add(o);
        }
        return arr;
    }

    private static <T> JsonArray dumpDatapack(MinecraftServer server, ResourceKey<Registry<T>> key) {
        JsonArray arr = new JsonArray();
        Registry<T> reg = server.registryAccess().registryOrThrow(key);
        List<ResourceLocation> ids = new ArrayList<>(reg.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation rl : ids) {
            JsonObject o = new JsonObject();
            o.addProperty("id", rl.toString());
            o.addProperty("mod", rl.getNamespace());
            T value = reg.get(rl);
            if (value != null) {
                safe(() -> o.addProperty("class", value.getClass().getName()));
            }
            arr.add(o);
        }
        return arr;
    }

    private static <T> JsonArray dumpIds(Registry<T> reg) {
        JsonArray arr = new JsonArray();
        List<ResourceLocation> ids = new ArrayList<>(reg.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation rl : ids) {
            JsonObject o = new JsonObject();
            o.addProperty("id", rl.toString());
            o.addProperty("mod", rl.getNamespace());
            arr.add(o);
        }
        return arr;
    }

    private static JsonArray dumpAllTags(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        // Built-in registries
        dumpTagsFrom(arr, "item", BuiltInRegistries.ITEM);
        dumpTagsFrom(arr, "block", BuiltInRegistries.BLOCK);
        dumpTagsFrom(arr, "entity_type", BuiltInRegistries.ENTITY_TYPE);
        dumpTagsFrom(arr, "fluid", BuiltInRegistries.FLUID);
        dumpTagsFrom(arr, "enchantment", BuiltInRegistries.ENCHANTMENT);
        dumpTagsFrom(arr, "mob_effect", BuiltInRegistries.MOB_EFFECT);
        dumpTagsFrom(arr, "potion", BuiltInRegistries.POTION);
        dumpTagsFrom(arr, "block_entity_type", BuiltInRegistries.BLOCK_ENTITY_TYPE);
        dumpTagsFrom(arr, "particle_type", BuiltInRegistries.PARTICLE_TYPE);
        dumpTagsFrom(arr, "painting_variant", BuiltInRegistries.PAINTING_VARIANT);
        dumpTagsFrom(arr, "point_of_interest_type", BuiltInRegistries.POINT_OF_INTEREST_TYPE);
        dumpTagsFrom(arr, "banner_pattern", BuiltInRegistries.BANNER_PATTERN);
        dumpTagsFrom(arr, "cat_variant", BuiltInRegistries.CAT_VARIANT);
        dumpTagsFrom(arr, "instrument", BuiltInRegistries.INSTRUMENT);
        dumpTagsFrom(arr, "game_event", BuiltInRegistries.GAME_EVENT);
        // Datapack registries
        try { dumpTagsFrom(arr, "biome", server.registryAccess().registryOrThrow(Registries.BIOME)); } catch (Exception ignored) {}
        try { dumpTagsFrom(arr, "structure", server.registryAccess().registryOrThrow(Registries.STRUCTURE)); } catch (Exception ignored) {}
        try { dumpTagsFrom(arr, "damage_type", server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)); } catch (Exception ignored) {}
        return arr;
    }

    private static <T> void dumpTagsFrom(JsonArray arr, String registryName, Registry<T> registry) {
        List<TagKey<T>> tagKeys = registry.getTagNames()
                .sorted(Comparator.comparing(k -> k.location().toString()))
                .collect(Collectors.toList());
        for (TagKey<T> tagKey : tagKeys) {
            var holders = registry.getTag(tagKey);
            if (holders.isEmpty()) continue;
            JsonObject o = new JsonObject();
            o.addProperty("tag", "#" + tagKey.location());
            o.addProperty("registry", registryName);
            o.addProperty("mod", tagKey.location().getNamespace());
            JsonArray entries = new JsonArray();
            for (Holder<T> holder : holders.get()) {
                holder.unwrapKey().ifPresent(key -> entries.add(key.location().toString()));
            }
            o.addProperty("count", entries.size());
            o.add("entries", entries);
            arr.add(o);
        }
    }

    // ---------- helpers ----------

    private static int write(Path path, JsonArray array) throws IOException {
        // Also build a by_mod count map and wrap.
        Map<String, Integer> byMod = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject o = array.get(i).getAsJsonObject();
            String mod = o.has("mod") ? o.get("mod").getAsString() : "?";
            byMod.merge(mod, 1, Integer::sum);
        }
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("count", array.size());
        JsonObject byModJson = new JsonObject();
        byMod.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> byModJson.addProperty(e.getKey(), e.getValue()));
        wrapper.add("by_mod", byModJson);
        wrapper.add("entries", array);
        Files.writeString(path, GSON.toJson(wrapper));
        return array.size();
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Throwable; }

    private static void safe(ThrowingRunnable r) {
        try { r.run(); } catch (Throwable ignored) {}
    }
}
