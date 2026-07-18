package com.soul.soa_additions.export;

import com.google.common.collect.Multimap;
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
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * {@code /soa export <target>} — dumps registry contents to JSON files under
 * {@code <gamedir>/soa_exports/}. Useful for building wikis, checklists, or
 * feeding other tooling. Targets: {@code items}, {@code blocks}, {@code entities},
 * {@code structures}, {@code biomes}, {@code dimensions}, {@code equipment},
 * {@code books}, {@code all}.
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
                                            "bosses", "structures", "biomes", "dimensions", "effects",
                                            "enchantments", "fluids", "sounds", "particles",
                                            "block_entities", "villager_professions", "tags",
                                            "tinker_materials", "equipment", "books"}) {
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
            if (all || "bosses".equalsIgnoreCase(target))
                totals.put("bosses", write(outDir.resolve("bosses.json"), dumpBosses()));
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
            if (all || "equipment".equalsIgnoreCase(target))
                totals.put("equipment", write(outDir.resolve("equipment.json"), dumpEquipment()));
            if (all || "books".equalsIgnoreCase(target))
                totals.put("books", write(outDir.resolve("books.json"), dumpBooks()));
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

    /**
     * Dumps every entity type that spawns with a central boss-bar overlay.
     * Detection is purely structural — no whitelist, no per-mod knowledge:
     *   1. {@code forge:bosses} entity_type tag (when a mod opts in).
     *   2. Reflection: any {@link BossEvent} field declared on the entity class
     *      or its supertypes. The boss bar is rendered from
     *      {@code ClientboundBossEventPacket}s emitted by a server-side
     *      {@code ServerBossEvent}, which mods almost universally store as a
     *      field on the entity class. Catches vanilla Wither and Ender Dragon,
     *      Mowzie's Mobs, Cataclysm, Bosses of Mass Destruction, Twilight
     *      Forest, Ice and Fire, Aether, etc., without naming any of them.
     */
    private static JsonArray dumpBosses() {
        JsonArray arr = new JsonArray();
        TagKey<EntityType<?>> bossTag = TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation("forge", "bosses"));

        List<EntityType<?>> types = new ArrayList<>();
        BuiltInRegistries.ENTITY_TYPE.forEach(types::add);
        types.sort(Comparator.comparing(t -> BuiltInRegistries.ENTITY_TYPE.getKey(t).toString()));

        for (EntityType<?> et : types) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(et);

            boolean tagged = isTagged(et, bossTag);
            boolean reflective = hasBossEventField(et.getBaseClass());
            if (!(tagged || reflective)) continue;

            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("class", et.getBaseClass().getName());

            JsonArray sources = new JsonArray();
            if (tagged) sources.add("forge:bosses");
            if (reflective) sources.add("boss_event_field");
            o.add("detected_via", sources);

            safe(() -> o.addProperty("name", et.getDescription().getString()));
            safe(() -> o.addProperty("category", et.getCategory().getName()));
            safe(() -> o.addProperty("width", et.getWidth()));
            safe(() -> o.addProperty("height", et.getHeight()));
            safe(() -> o.addProperty("fire_immune", et.fireImmune()));
            safe(() -> o.addProperty("summonable", et.canSummon()));
            JsonObject stats = dumpStats(et);
            if (stats != null) o.add("stats", stats);
            arr.add(o);
        }
        return arr;
    }

    /**
     * Reads base attribute values from the entity's {@link AttributeSupplier},
     * which is the same source the engine consults at spawn-time. Iterates the
     * full attribute registry so modded attributes (Forge swim speed, gravity,
     * step height, plus anything a content mod registers) are picked up
     * automatically alongside vanilla {@code generic.max_health},
     * {@code generic.attack_damage}, etc.
     */
    private static JsonObject dumpStats(EntityType<?> et) {
        if (!LivingEntity.class.isAssignableFrom(et.getBaseClass())) return null;
        if (!DefaultAttributes.hasSupplier(et)) return null;
        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> living = (EntityType<? extends LivingEntity>) et;
        AttributeSupplier supplier;
        try {
            supplier = DefaultAttributes.getSupplier(living);
        } catch (Throwable t) {
            return null;
        }
        if (supplier == null) return null;
        JsonObject stats = new JsonObject();
        List<Attribute> attrs = new ArrayList<>();
        BuiltInRegistries.ATTRIBUTE.forEach(attrs::add);
        attrs.sort(Comparator.comparing(a -> {
            ResourceLocation k = BuiltInRegistries.ATTRIBUTE.getKey(a);
            return k == null ? "" : k.toString();
        }));
        for (Attribute attr : attrs) {
            try {
                if (!supplier.hasAttribute(attr)) continue;
                ResourceLocation aId = BuiltInRegistries.ATTRIBUTE.getKey(attr);
                if (aId == null) continue;
                stats.addProperty(aId.toString(), supplier.getBaseValue(attr));
            } catch (Throwable ignored) {}
        }
        return stats.size() == 0 ? null : stats;
    }

    private static boolean isTagged(EntityType<?> et, TagKey<EntityType<?>> tag) {
        Optional<Holder.Reference<EntityType<?>>> holder =
                BuiltInRegistries.ENTITY_TYPE.getResourceKey(et)
                        .flatMap(BuiltInRegistries.ENTITY_TYPE::getHolder);
        return holder.map(h -> h.is(tag)).orElse(false);
    }

    private static boolean hasBossEventField(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    if (BossEvent.class.isAssignableFrom(f.getType())) return true;
                }
            } catch (Throwable ignored) {
                // Some classloaders refuse getDeclaredFields on certain modded classes;
                // fall through to the next supertype.
            }
            c = c.getSuperclass();
        }
        return false;
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

    /**
     * Dumps every armor piece, weapon, and tool with combat/durability stats.
     * Detection is structural — no per-mod knowledge:
     *   1. Vanilla item classes ({@link SwordItem}, {@link DiggerItem} family,
     *      {@link ArmorItem}, {@link BowItem}, {@link ShieldItem}, etc.).
     *   2. Forge {@code ToolActions} — modded gear that doesn't extend vanilla
     *      classes but declares what it can do (dig, sweep, block, ...).
     *   3. Attribute modifiers on the default stack — anything granting
     *      attack damage in the main hand or armor/toughness in an armor slot.
     */
    private static JsonArray dumpEquipment() {
        JsonArray arr = new JsonArray();
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(items::add);
        items.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString()));
        for (Item item : items) {
            try {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                ItemStack stack = item.getDefaultInstance();

                Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> mods = new EnumMap<>(EquipmentSlot.class);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    try {
                        Multimap<Attribute, AttributeModifier> m = stack.getAttributeModifiers(slot);
                        if (m != null && !m.isEmpty()) mods.put(slot, m);
                    } catch (Throwable ignored) {}
                }

                String[] cat = classifyEquipment(item, stack, mods);
                if (cat == null) continue;

                JsonObject o = new JsonObject();
                o.addProperty("id", id.toString());
                o.addProperty("mod", id.getNamespace());
                o.addProperty("class", item.getClass().getName());
                o.addProperty("category", cat[0]);
                o.addProperty("type", cat[1]);
                safe(() -> o.addProperty("name", stack.getHoverName().getString()));
                safe(() -> o.addProperty("max_damage", item.getMaxDamage()));
                safe(() -> o.addProperty("enchantability", item.getEnchantmentValue()));
                safe(() -> o.addProperty("rarity", stack.getRarity().name()));
                safe(() -> o.addProperty("fire_resistant", item.isFireResistant()));

                // Player-visible combat numbers: 1.0 base attack damage / 4.0 base
                // attack speed plus flat main-hand ADDITION modifiers.
                Multimap<Attribute, AttributeModifier> main = mods.get(EquipmentSlot.MAINHAND);
                if (main != null && main.containsKey(Attributes.ATTACK_DAMAGE)) {
                    double dmg = 1.0, spd = 4.0;
                    for (AttributeModifier m : main.get(Attributes.ATTACK_DAMAGE))
                        if (m.getOperation() == AttributeModifier.Operation.ADDITION) dmg += m.getAmount();
                    for (AttributeModifier m : main.get(Attributes.ATTACK_SPEED))
                        if (m.getOperation() == AttributeModifier.Operation.ADDITION) spd += m.getAmount();
                    o.addProperty("attack_damage", dmg);
                    o.addProperty("attack_speed", spd);
                }

                if (item instanceof TieredItem tiered) {
                    Tier tier = tiered.getTier();
                    JsonObject t = new JsonObject();
                    ResourceLocation tierName = TierSortingRegistry.getName(tier);
                    if (tierName != null) t.addProperty("name", tierName.toString());
                    safe(() -> t.addProperty("level", tier.getLevel()));
                    safe(() -> t.addProperty("uses", tier.getUses()));
                    safe(() -> t.addProperty("mining_speed", tier.getSpeed()));
                    safe(() -> t.addProperty("attack_damage_bonus", tier.getAttackDamageBonus()));
                    safe(() -> t.addProperty("enchantability", tier.getEnchantmentValue()));
                    safe(() -> {
                        JsonArray repair = new JsonArray();
                        for (ItemStack rs : tier.getRepairIngredient().getItems())
                            repair.add(BuiltInRegistries.ITEM.getKey(rs.getItem()).toString());
                        if (repair.size() > 0) t.add("repair_items", repair);
                    });
                    o.add("tier", t);
                }

                if (item instanceof ArmorItem armor) {
                    JsonObject a = new JsonObject();
                    safe(() -> a.addProperty("slot", armor.getType().getSlot().getName()));
                    safe(() -> a.addProperty("material", armor.getMaterial().getName()));
                    safe(() -> a.addProperty("defense", armor.getDefense()));
                    safe(() -> a.addProperty("toughness", armor.getToughness()));
                    safe(() -> a.addProperty("knockback_resistance", armor.getMaterial().getKnockbackResistance()));
                    safe(() -> a.addProperty("armor_enchantability", armor.getMaterial().getEnchantmentValue()));
                    o.add("armor", a);
                }

                if (!mods.isEmpty()) {
                    JsonArray modifiers = new JsonArray();
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        Multimap<Attribute, AttributeModifier> m = mods.get(slot);
                        if (m == null) continue;
                        List<Attribute> attrs = new ArrayList<>(m.keySet());
                        attrs.sort(Comparator.comparing(a2 -> {
                            ResourceLocation k = BuiltInRegistries.ATTRIBUTE.getKey(a2);
                            return k == null ? "" : k.toString();
                        }));
                        for (Attribute attr : attrs) {
                            ResourceLocation aId = BuiltInRegistries.ATTRIBUTE.getKey(attr);
                            for (AttributeModifier mod : m.get(attr)) {
                                JsonObject mo = new JsonObject();
                                mo.addProperty("slot", slot.getName());
                                mo.addProperty("attribute", aId == null ? attr.getDescriptionId() : aId.toString());
                                mo.addProperty("amount", mod.getAmount());
                                mo.addProperty("operation", mod.getOperation().name());
                                modifiers.add(mo);
                            }
                        }
                    }
                    o.add("attribute_modifiers", modifiers);
                }

                arr.add(o);
            } catch (Throwable ignored) {
                // A single broken modded item must not kill the whole export.
            }
        }
        return arr;
    }

    /** Returns {category, type} or null if the item isn't equipment. */
    private static String[] classifyEquipment(Item item, ItemStack stack,
            Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> mods) {
        // 1. Vanilla class hierarchy. Order matters: the specific DiggerItem
        //    subclasses and Bow/Crossbow before their parents.
        if (item instanceof SwordItem) return new String[]{"weapon", "sword"};
        if (item instanceof PickaxeItem) return new String[]{"tool", "pickaxe"};
        if (item instanceof AxeItem) return new String[]{"tool", "axe"};
        if (item instanceof ShovelItem) return new String[]{"tool", "shovel"};
        if (item instanceof HoeItem) return new String[]{"tool", "hoe"};
        if (item instanceof DiggerItem) return new String[]{"tool", "digger"};
        if (item instanceof ShearsItem) return new String[]{"tool", "shears"};
        if (item instanceof FishingRodItem) return new String[]{"tool", "fishing_rod"};
        if (item instanceof BowItem) return new String[]{"weapon", "bow"};
        if (item instanceof CrossbowItem) return new String[]{"weapon", "crossbow"};
        if (item instanceof TridentItem) return new String[]{"weapon", "trident"};
        if (item instanceof ProjectileWeaponItem) return new String[]{"weapon", "projectile_weapon"};
        if (item instanceof ShieldItem) return new String[]{"armor", "shield"};
        if (item instanceof ElytraItem) return new String[]{"armor", "elytra"};
        if (item instanceof ArmorItem armor) return new String[]{"armor", armor.getType().getName()};
        if (item instanceof HorseArmorItem) return new String[]{"armor", "horse_armor"};

        // 2. Forge ToolActions — modded gear declaring capabilities.
        try {
            if (stack.canPerformAction(ToolActions.SWORD_SWEEP)) return new String[]{"weapon", "sword"};
            if (stack.canPerformAction(ToolActions.PICKAXE_DIG)) return new String[]{"tool", "pickaxe"};
            if (stack.canPerformAction(ToolActions.AXE_DIG)) return new String[]{"tool", "axe"};
            if (stack.canPerformAction(ToolActions.SHOVEL_DIG)) return new String[]{"tool", "shovel"};
            if (stack.canPerformAction(ToolActions.HOE_DIG)) return new String[]{"tool", "hoe"};
            if (stack.canPerformAction(ToolActions.SHEARS_DIG)) return new String[]{"tool", "shears"};
            if (stack.canPerformAction(ToolActions.FISHING_ROD_CAST)) return new String[]{"tool", "fishing_rod"};
            if (stack.canPerformAction(ToolActions.SHIELD_BLOCK)) return new String[]{"armor", "shield"};
        } catch (Throwable ignored) {}

        // 3. Attribute fallback — anything granting armor in an armor slot or
        //    attack damage in the main hand.
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            Multimap<Attribute, AttributeModifier> m = mods.get(slot);
            if (m != null && (m.containsKey(Attributes.ARMOR) || m.containsKey(Attributes.ARMOR_TOUGHNESS)))
                return new String[]{"armor", slot.getName()};
        }
        Multimap<Attribute, AttributeModifier> main = mods.get(EquipmentSlot.MAINHAND);
        if (main != null && main.containsKey(Attributes.ATTACK_DAMAGE))
            return new String[]{"weapon", "weapon"};
        return null;
    }

    /**
     * Dumps every "book-like" item — mod guide books (Valoria codex, Ars
     * Nouveau worn notebook, Patchouli-driven guides), lexica, tomes, journals.
     * Detection is heuristic since guide books share no common superclass:
     * keyword match on the registry path and the display name, plus the
     * vanilla book classes.
     */
    private static JsonArray dumpBooks() {
        String[] keywords = {"book", "codex", "guide", "journal", "tome", "manual",
                "notebook", "lexicon", "encyclopedia", "grimoire", "compendium",
                "nomicon", "chronicle", "primer", "dictionary", "almanac", "diary",
                "opus", "thesaurus", "atlas"};
        JsonArray arr = new JsonArray();
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(items::add);
        items.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(i).toString()));
        for (Item item : items) {
            try {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                ItemStack stack = item.getDefaultInstance();
                String path = id.getPath();
                String name = "";
                try { name = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT); } catch (Throwable ignored) {}
                String cls = item.getClass().getName().toLowerCase(java.util.Locale.ROOT);

                List<String> via = new ArrayList<>();
                for (String kw : keywords) {
                    if (path.contains(kw)) { via.add("id:" + kw); break; }
                }
                if (via.isEmpty()) {
                    for (String kw : keywords) {
                        if (name.contains(kw)) { via.add("name:" + kw); break; }
                    }
                }
                if (via.isEmpty() && (cls.contains("book") || cls.contains("codex")
                        || cls.contains("tome") || cls.contains("lexicon"))) {
                    via.add("class");
                }
                if (via.isEmpty()) continue;

                JsonObject o = new JsonObject();
                o.addProperty("id", id.toString());
                o.addProperty("mod", id.getNamespace());
                o.addProperty("class", item.getClass().getName());
                safe(() -> o.addProperty("name", stack.getHoverName().getString()));
                safe(() -> o.addProperty("rarity", stack.getRarity().name()));
                safe(() -> o.addProperty("max_stack", item.getMaxStackSize()));
                JsonArray viaArr = new JsonArray();
                via.forEach(viaArr::add);
                o.add("matched_via", viaArr);
                arr.add(o);
            } catch (Throwable ignored) {}
        }
        return arr;
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
