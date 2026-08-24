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
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Field;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * {@code /soa export <target>} — dumps registry contents to JSON files under
 * {@code <gamedir>/soa_exports/}. Useful for building wikis, checklists, or
 * feeding other tooling. Targets: {@code items}, {@code blocks}, {@code entities},
 * {@code structures}, {@code biomes}, {@code dimensions}, {@code equipment},
 * {@code books}, {@code recipes}, {@code loot_tables}, {@code brewing},
 * {@code advancements}, {@code spawns}, {@code villager_trades}, {@code all}.
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
                        // Deliberately ungated. The export is read-only reference data —
                        // it grants nothing and changes no world state — and it is on
                        // AntiCheatHandler.SAFE_SOA_SUBPATHS for the same reason. Players
                        // building checklists/wikis need it without an op asking first.
                        // Caveat: on a dedicated server the files land in the *server's*
                        // gamedir, and "all" costs a few seconds of main-thread time.
                        .executes(ctx -> run(ctx.getSource(), "all"))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    for (String s : new String[]{"all", "items", "blocks", "entities",
                                            "bosses", "structures", "biomes", "dimensions", "effects",
                                            "enchantments", "fluids", "sounds", "particles",
                                            "block_entities", "villager_professions", "tags",
                                            "smithery_materials", "equipment", "books", "recipes",
                                            "loot_tables", "brewing", "advancements",
                                            "spawns", "villager_trades", "jei"}) {
                                        b.suggest(s);
                                    }
                                    return b.buildFuture();
                                })
                                .executes(ctx -> run(ctx.getSource(), StringArgumentType.getString(ctx, "target"))))));
    }

    private static int run(CommandSourceStack src, String target) {
        if ("jei".equalsIgnoreCase(target)) {
            // handled client-side by JeiRecipeExport; if we got here the client
            // command did not intercept, which means JEI is not installed.
            src.sendFailure(Component.literal(
                    "The jei target is a client-side export and needs JEI installed. "
                    + "Run it from a client, not a dedicated server."));
            return 0;
        }
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
                totals.put("effects", write(outDir.resolve("mob_effects.json"), dumpIds(ForgeRegistries.MOB_EFFECTS)));
            if (all || "enchantments".equalsIgnoreCase(target))
                totals.put("enchantments", write(outDir.resolve("enchantments.json"), dumpIds(ForgeRegistries.ENCHANTMENTS)));
            if (all || "fluids".equalsIgnoreCase(target))
                totals.put("fluids", write(outDir.resolve("fluids.json"), dumpIds(ForgeRegistries.FLUIDS)));
            if (all || "sounds".equalsIgnoreCase(target))
                totals.put("sounds", write(outDir.resolve("sounds.json"), dumpIds(ForgeRegistries.SOUND_EVENTS)));
            if (all || "particles".equalsIgnoreCase(target))
                totals.put("particles", write(outDir.resolve("particles.json"), dumpIds(ForgeRegistries.PARTICLE_TYPES)));
            if (all || "block_entities".equalsIgnoreCase(target))
                totals.put("block_entities", write(outDir.resolve("block_entities.json"), dumpIds(ForgeRegistries.BLOCK_ENTITY_TYPES)));
            if (all || "villager_professions".equalsIgnoreCase(target))
                totals.put("villager_professions", write(outDir.resolve("villager_professions.json"), dumpIds(ForgeRegistries.VILLAGER_PROFESSIONS)));
            if (all || "tags".equalsIgnoreCase(target))
                totals.put("tags", write(outDir.resolve("tags.json"), dumpAllTags(server)));
            if (all || "smithery_materials".equalsIgnoreCase(target))
                totals.put("smithery_materials",
                        write(outDir.resolve("smithery_materials.json"), dumpSmitheryMaterials()));
            if (all || "equipment".equalsIgnoreCase(target))
                totals.put("equipment", write(outDir.resolve("equipment.json"), dumpEquipment()));
            if (all || "books".equalsIgnoreCase(target))
                totals.put("books", write(outDir.resolve("books.json"), dumpBooks()));
            if (all || "recipes".equalsIgnoreCase(target))
                totals.put("recipes", write(outDir.resolve("recipes.json"), dumpRecipes(server)));
            if (all || "loot_tables".equalsIgnoreCase(target))
                totals.put("loot_tables", write(outDir.resolve("loot_tables.json"), dumpLootTables(server)));
            if (all || "brewing".equalsIgnoreCase(target))
                totals.put("brewing", write(outDir.resolve("brewing.json"), dumpBrewing()));
            if (all || "advancements".equalsIgnoreCase(target))
                totals.put("advancements", write(outDir.resolve("advancements.json"), dumpAdvancements(server)));
            if (all || "spawns".equalsIgnoreCase(target))
                totals.put("spawns", write(outDir.resolve("spawns.json"), dumpSpawns(server)));
            if (all || "villager_trades".equalsIgnoreCase(target))
                totals.put("villager_trades", write(outDir.resolve("villager_trades.json"), dumpVillagerTrades()));
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
        ForgeRegistries.ITEMS.forEach(items::add);
        items.sort(Comparator.comparing(i -> ForgeRegistries.ITEMS.getKey(i).toString()));
        for (Item item : items) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            ItemStack stack = item.getDefaultInstance();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("class", item.getClass().getName());
            safe(() -> o.addProperty("name", stack.getHoverName().getString()));
            safe(() -> o.addProperty("max_stack", stack.getMaxStackSize()));
            safe(() -> o.addProperty("max_damage", stack.getMaxDamage()));
            safe(() -> o.addProperty("rarity", stack.getRarity().name()));
            safe(() -> o.addProperty("is_edible", item.isEdible()));
            safe(() -> o.addProperty("can_be_depleted", item.canBeDepleted()));
            safe(() -> o.addProperty("fire_resistant", item.isFireResistant()));
            if (item.isEdible()) {
                // The stack-aware overload is the non-deprecated one, but it takes an entity and a
                // mod override is free to dereference it. dumpItems has no per-item catch, so an
                // NPE here would truncate items.json and everything after it.
                FoodProperties fp = null;
                try { fp = stack.getFoodProperties(null); } catch (Throwable ignored) {}
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

    // The Forge-sensitive forms of these getters all want a BlockGetter and a BlockPos (and an
    // Explosion, for blast resistance) — a world this command deliberately does not have, because
    // it dumps static registry data. Passing EmptyBlockGetter/BlockPos.ZERO/null to satisfy the
    // signature would invite an NPE from any third-party block that actually reads them.
    @SuppressWarnings("deprecation")
    private static JsonArray dumpBlocks() {
        JsonArray arr = new JsonArray();
        List<Block> blocks = new ArrayList<>();
        ForgeRegistries.BLOCKS.forEach(blocks::add);
        blocks.sort(Comparator.comparing(b -> ForgeRegistries.BLOCKS.getKey(b).toString()));
        for (Block block : blocks) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
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
        ForgeRegistries.ENTITY_TYPES.forEach(types::add);
        types.sort(Comparator.comparing(t -> ForgeRegistries.ENTITY_TYPES.getKey(t).toString()));
        for (EntityType<?> et : types) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(et);
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
        ForgeRegistries.ENTITY_TYPES.forEach(types::add);
        types.sort(Comparator.comparing(t -> ForgeRegistries.ENTITY_TYPES.getKey(t).toString()));

        for (EntityType<?> et : types) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(et);

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
        ForgeRegistries.ATTRIBUTES.forEach(attrs::add);
        attrs.sort(Comparator.comparing(a -> {
            ResourceLocation k = ForgeRegistries.ATTRIBUTES.getKey(a);
            return k == null ? "" : k.toString();
        }));
        for (Attribute attr : attrs) {
            try {
                if (!supplier.hasAttribute(attr)) continue;
                ResourceLocation aId = ForgeRegistries.ATTRIBUTES.getKey(attr);
                if (aId == null) continue;
                stats.addProperty(aId.toString(), supplier.getBaseValue(attr));
            } catch (Throwable ignored) {}
        }
        return stats.size() == 0 ? null : stats;
    }

    private static boolean isTagged(EntityType<?> et, TagKey<EntityType<?>> tag) {
        Optional<Holder<EntityType<?>>> holder = ForgeRegistries.ENTITY_TYPES.getHolder(et);
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

    private static <T> JsonArray dumpIds(IForgeRegistry<T> reg) {
        JsonArray arr = new JsonArray();
        List<ResourceLocation> ids = new ArrayList<>(reg.getKeys());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation rl : ids) {
            JsonObject o = new JsonObject();
            o.addProperty("id", rl.toString());
            o.addProperty("mod", rl.getNamespace());
            arr.add(o);
        }
        return arr;
    }

    // BuiltInRegistries, not ForgeRegistries: four of the registries below (banner_pattern,
    // cat_variant, instrument, game_event) have no ForgeRegistries field at all, and dumpTagsFrom
    // needs Registry#getTagNames/#getTag — IForgeRegistry only offers a @Nullable ITagManager.
    @SuppressWarnings("deprecation")
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
    // getLevel: the export records the numeric ladder rank, which is what the pack's docs show.
    @SuppressWarnings("deprecation")
    private static JsonArray dumpEquipment() {
        JsonArray arr = new JsonArray();
        List<Item> items = new ArrayList<>();
        ForgeRegistries.ITEMS.forEach(items::add);
        items.sort(Comparator.comparing(i -> ForgeRegistries.ITEMS.getKey(i).toString()));
        for (Item item : items) {
            try {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
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
                safe(() -> o.addProperty("max_damage", stack.getMaxDamage()));
                safe(() -> o.addProperty("enchantability", stack.getEnchantmentValue()));
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
                            repair.add(ForgeRegistries.ITEMS.getKey(rs.getItem()).toString());
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
                            ResourceLocation k = ForgeRegistries.ATTRIBUTES.getKey(a2);
                            return k == null ? "" : k.toString();
                        }));
                        for (Attribute attr : attrs) {
                            ResourceLocation aId = ForgeRegistries.ATTRIBUTES.getKey(attr);
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
        ForgeRegistries.ITEMS.forEach(items::add);
        items.sort(Comparator.comparing(i -> ForgeRegistries.ITEMS.getKey(i).toString()));
        for (Item item : items) {
            try {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
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
                safe(() -> o.addProperty("max_stack", stack.getMaxStackSize()));
                JsonArray viaArr = new JsonArray();
                via.forEach(viaArr::add);
                o.add("matched_via", viaArr);
                arr.add(o);
            } catch (Throwable ignored) {}
        }
        return arr;
    }

    // ---------- helpers ----------

    /**
     * Dumps every recipe the server actually has loaded, read straight off the
     * live {@code RecipeManager}. That is the only complete view: it includes
     * mod-jar recipes, recipes nested inside JarJar sub-mods, datapack recipes,
     * {@code kubejs/data} JSON <em>and</em> recipes added by KubeJS scripts —
     * none of which can be reconstructed reliably by scanning files on disk.
     *
     * <p>Ingredients are emitted as their own {@code Ingredient.toJson()} form
     * ({@code {"item": ...}} or {@code {"tag": ...}}), so tags stay unresolved
     * and compact; resolve them against {@code tags.json} if needed.</p>
     *
     * <p>Recipes with a custom {@link net.minecraft.world.item.crafting.Recipe}
     * implementation may not populate {@code getIngredients()} or
     * {@code getResultItem()}. Those cases are flagged explicitly with
     * {@code ingredients_available: false} / {@code result_available: false}
     * rather than being written out as if they had none — an empty list and
     * "the API would not tell us" are different facts.</p>
     */
    private static JsonArray dumpRecipes(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        List<net.minecraft.world.item.crafting.Recipe<?>> recipes =
                new ArrayList<>(server.getRecipeManager().getRecipes());
        recipes.sort(Comparator.comparing(r -> r.getId().toString()));

        for (net.minecraft.world.item.crafting.Recipe<?> recipe : recipes) {
            ResourceLocation id = recipe.getId();
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            safe(() -> {
                ResourceLocation t = ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType());
                if (t != null) o.addProperty("type", t.toString());
            });
            safe(() -> {
                ResourceLocation s = ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipe.getSerializer());
                if (s != null) o.addProperty("serializer", s.toString());
            });
            safe(() -> o.addProperty("class", recipe.getClass().getName()));
            safe(() -> {
                String g = recipe.getGroup();
                if (g != null && !g.isEmpty()) o.addProperty("group", g);
            });
            safe(() -> { if (recipe.isSpecial()) o.addProperty("special", true); });

            // ---- result ----
            // ~25% of loaded recipes are custom machine types whose Recipe class
            // returns nothing from getResultItem(RegistryAccess) — Blood Magic
            // altar, Malum spirit_infusion, Thermal press, EnderIO alloy_smelting,
            // Create mixing, CustomMachinery, and so on. Fall back through the
            // mod's own accessors and then declared fields, and always record
            // which path produced the value via `result_source`.
            String[] resultSource = {null};
            safe(() -> {
                ItemStack out = recipe.getResultItem(server.registryAccess());
                if (out != null && !out.isEmpty()) {
                    o.add("result", stackJson(out));
                    resultSource[0] = "api";
                }
            });
            if (resultSource[0] == null) {
                safe(() -> {
                    ItemStack out = reflectResult(recipe);
                    if (out != null && !out.isEmpty()) {
                        o.add("result", stackJson(out));
                        resultSource[0] = "reflected";
                    }
                });
            }
            if (resultSource[0] != null) o.addProperty("result_source", resultSource[0]);
            else o.addProperty("result_available", false);

            // ---- ingredients ----
            String[] ingSource = {null};
            safe(() -> {
                JsonArray ings = new JsonArray();
                for (net.minecraft.world.item.crafting.Ingredient ing : recipe.getIngredients()) {
                    safe(() -> ings.add(ing.toJson()));
                }
                if (ings.size() > 0) {
                    o.add("ingredients", ings);
                    ingSource[0] = "api";
                }
            });
            if (ingSource[0] == null) {
                safe(() -> {
                    JsonArray ings = reflectIngredients(recipe);
                    if (ings.size() > 0) {
                        o.add("ingredients", ings);
                        ingSource[0] = "reflected";
                    }
                });
            }
            if (ingSource[0] != null) o.addProperty("ingredients_source", ingSource[0]);
            else o.addProperty("ingredients_available", false);

            arr.add(o);
        }
        return arr;
    }

    /** Common no-arg output accessors used by modded recipe classes. */
    private static final String[] RESULT_METHODS = {
            "getResultItem", "getOutput", "getResult", "getOutputItem",
            "output", "result", "getRecipeOutput", "assemble",
    };

    /** Last-resort output discovery: the mod's own accessor, then declared fields. */
    private static ItemStack reflectResult(Object recipe) {
        for (String name : RESULT_METHODS) {
            try {
                java.lang.reflect.Method m = recipe.getClass().getMethod(name);
                if (!ItemStack.class.isAssignableFrom(m.getReturnType())) continue;
                m.setAccessible(true);
                ItemStack s = (ItemStack) m.invoke(recipe);
                if (s != null && !s.isEmpty()) return s;
            } catch (Throwable ignored) {}
        }
        for (Class<?> c = recipe.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object v = f.get(recipe);
                    if (v instanceof ItemStack s && !s.isEmpty()) return s;
                    if (v instanceof List<?> list && !list.isEmpty()
                            && list.get(0) instanceof ItemStack s2 && !s2.isEmpty()) return s2;
                    if (v instanceof ItemStack[] a && a.length > 0 && !a[0].isEmpty()) return a[0];
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    /** Last-resort input discovery for recipes that leave getIngredients() empty. */
    private static JsonArray reflectIngredients(Object recipe) {
        JsonArray out = new JsonArray();
        for (Class<?> c = recipe.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object v = f.get(recipe);
                    if (v instanceof net.minecraft.world.item.crafting.Ingredient ing) {
                        if (!ing.isEmpty()) out.add(ing.toJson());
                    } else if (v instanceof List<?> list) {
                        for (Object o : list) {
                            if (o instanceof net.minecraft.world.item.crafting.Ingredient i2 && !i2.isEmpty())
                                out.add(i2.toJson());
                        }
                    } else if (v instanceof net.minecraft.world.item.crafting.Ingredient[] a) {
                        for (net.minecraft.world.item.crafting.Ingredient i2 : a) {
                            if (i2 != null && !i2.isEmpty()) out.add(i2.toJson());
                        }
                    }
                } catch (Throwable ignored) {}
            }
            if (out.size() > 0) break;
        }
        return out;
    }

    // ------------------------------------------------------------------
    //  Obtainability exports: where does an item actually come from?
    // ------------------------------------------------------------------

    /** Walks a serialized loot table and collects every {@code minecraft:item} entry name. */
    private static void collectLootItems(com.google.gson.JsonElement el, java.util.Set<String> out) {
        if (el == null) return;
        if (el.isJsonArray()) {
            for (com.google.gson.JsonElement child : el.getAsJsonArray()) collectLootItems(child, out);
            return;
        }
        if (!el.isJsonObject()) return;
        JsonObject o = el.getAsJsonObject();
        if (o.has("type") && o.has("name")) {
            String t = o.get("type").getAsString();
            if (t.equals("minecraft:item") || t.equals("item")) {
                safe(() -> out.add(o.get("name").getAsString()));
            }
        }
        // loot-modifying functions can also add items (set_item, etc.)
        if (o.has("function") && o.has("item")) safe(() -> out.add(o.get("item").getAsString()));
        for (Map.Entry<String, com.google.gson.JsonElement> e : o.entrySet()) {
            collectLootItems(e.getValue(), out);
        }
    }

    /**
     * Every loot table the server has loaded, as its own serialized JSON plus a
     * flattened {@code items} list of everything it can drop. This is the answer
     * to "is this item obtainable at all" for anything that isn't crafted —
     * mob drops, chest loot, block drops, boss bags.
     *
     * <p>Also dumps Forge global loot modifiers, which is how several mods inject
     * items into <em>vanilla</em> tables (Forbidden &amp; Arcanus puts Elementarium
     * into temple chests this way). The modifier registry is not public API, so it
     * is read reflectively; if that fails the entry says so instead of being
     * silently omitted.</p>
     */
    private static JsonArray dumpLootTables(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        com.google.gson.Gson lootGson =
                net.minecraft.world.level.storage.loot.Deserializers.createLootTableSerializer().create();

        List<ResourceLocation> ids = new ArrayList<>(
                server.getLootData().getKeys(net.minecraft.world.level.storage.loot.LootDataType.TABLE));
        ids.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation id : ids) {
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            o.addProperty("kind", "loot_table");
            safe(() -> {
                net.minecraft.world.level.storage.loot.LootTable table = server.getLootData().getElement(
                        new net.minecraft.world.level.storage.loot.LootDataId<>(
                                net.minecraft.world.level.storage.loot.LootDataType.TABLE, id));
                if (table == null) {
                    o.addProperty("table_available", false);
                    return;
                }
                com.google.gson.JsonElement json = lootGson.toJsonTree(table);
                java.util.Set<String> items = new java.util.TreeSet<>();
                collectLootItems(json, items);
                JsonArray itemArr = new JsonArray();
                items.forEach(itemArr::add);
                o.add("items", itemArr);
                o.add("table", json);
            });
            arr.add(o);
        }

        // ---- Forge global loot modifiers ----
        JsonObject glm = new JsonObject();
        glm.addProperty("id", "forge:global_loot_modifiers");
        glm.addProperty("mod", "forge");
        glm.addProperty("kind", "global_loot_modifiers");
        boolean[] ok = {false};
        safe(() -> {
            java.lang.reflect.Method m = Class.forName("net.minecraftforge.common.ForgeInternalHandler")
                    .getDeclaredMethod("getLootModifierManager");
            m.setAccessible(true);
            Object manager = m.invoke(null);
            Field f = manager.getClass().getDeclaredField("registeredLootModifiers");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<ResourceLocation, ?> mods = (Map<ResourceLocation, ?>) f.get(manager);
            JsonArray list = new JsonArray();
            mods.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                    .forEach(e -> {
                        JsonObject mo = new JsonObject();
                        mo.addProperty("id", e.getKey().toString());
                        safe(() -> mo.addProperty("class", e.getValue().getClass().getName()));
                        list.add(mo);
                    });
            glm.add("modifiers", list);
            ok[0] = true;
        });
        if (!ok[0]) glm.addProperty("modifiers_available", false);
        arr.add(glm);

        return arr;
    }

    /**
     * Brewing recipes. These live in {@link net.minecraft.world.item.alchemy.PotionBrewing}
     * and Forge's {@code BrewingRecipeRegistry} — <em>not</em> in the RecipeManager —
     * so {@code /soa export recipes} does not cover them.
     */
    private static JsonArray dumpBrewing() {
        JsonArray arr = new JsonArray();

        // Vanilla + mod-registered mixes (private static lists, read reflectively).
        String[] listFields = {"POTION_MIXES", "CONTAINER_MIXES"};
        for (String fieldName : listFields) {
            boolean[] ok = {false};
            safe(() -> {
                Field f = net.minecraft.world.item.alchemy.PotionBrewing.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                List<?> mixes = (List<?>) f.get(null);
                for (Object mix : mixes) {
                    JsonObject o = new JsonObject();
                    o.addProperty("kind", fieldName.equals("POTION_MIXES") ? "potion_mix" : "container_mix");
                    safe(() -> {
                        Field from = mix.getClass().getField("from");
                        Field to = mix.getClass().getField("to");
                        Field ing = mix.getClass().getField("ingredient");
                        Holder.Reference<?> fromH = (Holder.Reference<?>) from.get(mix);
                        Holder.Reference<?> toH = (Holder.Reference<?>) to.get(mix);
                        o.addProperty("from", fromH.key().location().toString());
                        o.addProperty("to", toH.key().location().toString());
                        o.addProperty("mod", toH.key().location().getNamespace());
                        net.minecraft.world.item.crafting.Ingredient i =
                                (net.minecraft.world.item.crafting.Ingredient) ing.get(mix);
                        o.add("ingredient", i.toJson());
                    });
                    arr.add(o);
                }
                ok[0] = true;
            });
            if (!ok[0]) {
                JsonObject o = new JsonObject();
                o.addProperty("kind", fieldName.toLowerCase(java.util.Locale.ROOT));
                o.addProperty("mod", "minecraft");
                o.addProperty("available", false);
                arr.add(o);
            }
        }

        // Forge IBrewingRecipe instances (custom mod brewing).
        safe(() -> {
            for (net.minecraftforge.common.brewing.IBrewingRecipe r :
                    net.minecraftforge.common.brewing.BrewingRecipeRegistry.getRecipes()) {
                JsonObject o = new JsonObject();
                o.addProperty("kind", "forge_brewing_recipe");
                o.addProperty("class", r.getClass().getName());
                o.addProperty("mod", r.getClass().getName().split("\\.")[0]);
                if (r instanceof net.minecraftforge.common.brewing.BrewingRecipe br) {
                    safe(() -> o.add("input", br.getInput().toJson()));
                    safe(() -> o.add("ingredient", br.getIngredient().toJson()));
                    safe(() -> {
                        ItemStack out = br.getOutput();
                        if (out != null && !out.isEmpty())
                            o.addProperty("output", ForgeRegistries.ITEMS.getKey(out.getItem()).toString());
                    });
                } else {
                    // e.g. VanillaBrewingRecipe — its data is the mix lists above.
                    o.addProperty("details_available", false);
                }
                arr.add(o);
            }
        });
        return arr;
    }

    /** All loaded advancements — quest tasks reference these and mods gate content on them. */
    private static JsonArray dumpAdvancements(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        List<Advancement> advs = new ArrayList<>(server.getAdvancements().getAllAdvancements());
        advs.sort(Comparator.comparing(a -> a.getId().toString()));
        for (Advancement a : advs) {
            JsonObject o = new JsonObject();
            o.addProperty("id", a.getId().toString());
            o.addProperty("mod", a.getId().getNamespace());
            safe(() -> { if (a.getParent() != null) o.addProperty("parent", a.getParent().getId().toString()); });
            safe(() -> {
                DisplayInfo d = a.getDisplay();
                if (d != null) {
                    o.addProperty("title", d.getTitle().getString());
                    o.addProperty("description", d.getDescription().getString());
                    o.addProperty("frame", d.getFrame().getName());
                    o.addProperty("hidden", d.isHidden());
                    safe(() -> o.addProperty("icon",
                            ForgeRegistries.ITEMS.getKey(d.getIcon().getItem()).toString()));
                }
            });
            safe(() -> {
                JsonArray crit = new JsonArray();
                a.getCriteria().keySet().stream().sorted().forEach(crit::add);
                o.add("criteria", crit);
            });
            arr.add(o);
        }
        return arr;
    }

    /** Mob spawn entries per biome — answers "where do I actually find this creature". */
    private static JsonArray dumpSpawns(MinecraftServer server) {
        JsonArray arr = new JsonArray();
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        List<ResourceLocation> ids = new ArrayList<>(biomes.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation id : ids) {
            Biome biome = biomes.get(id);
            if (biome == null) continue;
            JsonObject o = new JsonObject();
            o.addProperty("id", id.toString());
            o.addProperty("mod", id.getNamespace());
            safe(() -> {
                JsonArray spawns = new JsonArray();
                for (net.minecraft.world.entity.MobCategory cat : net.minecraft.world.entity.MobCategory.values()) {
                    for (net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData sd
                            : biome.getMobSettings().getMobs(cat).unwrap()) {
                        JsonObject s = new JsonObject();
                        s.addProperty("category", cat.getName());
                        safe(() -> s.addProperty("entity",
                                ForgeRegistries.ENTITY_TYPES.getKey(sd.type).toString()));
                        safe(() -> s.addProperty("weight", sd.getWeight().asInt()));
                        s.addProperty("min_count", sd.minCount);
                        s.addProperty("max_count", sd.maxCount);
                        spawns.add(s);
                    }
                }
                o.add("spawns", spawns);
            });
            arr.add(o);
        }
        return arr;
    }

    /**
     * Villager and wandering trader offers. {@code ItemListing} builds its offer at
     * runtime, so each listing is invoked once with a fixed seed — offers with
     * randomised contents will show one representative roll, and the listing class
     * is always recorded so a non-deterministic entry is identifiable.
     */
    private static JsonArray dumpVillagerTrades() {
        JsonArray arr = new JsonArray();
        net.minecraft.util.RandomSource rng = net.minecraft.util.RandomSource.create(0L);

        safe(() -> net.minecraft.world.entity.npc.VillagerTrades.TRADES.entrySet().stream()
                .sorted(Comparator.comparing(e -> String.valueOf(
                        ForgeRegistries.VILLAGER_PROFESSIONS.getKey(e.getKey()))))
                .forEach(entry -> {
                    ResourceLocation prof = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(entry.getKey());
                    entry.getValue().forEach((level, listings) ->
                            addTradeRows(arr, prof == null ? "?" : prof.toString(), level, listings, rng));
                }));

        safe(() -> net.minecraft.world.entity.npc.VillagerTrades.WANDERING_TRADER_TRADES.forEach(
                (level, listings) -> addTradeRows(arr, "minecraft:wandering_trader", level, listings, rng)));

        return arr;
    }

    private static void addTradeRows(JsonArray arr, String profession, int level,
                                     net.minecraft.world.entity.npc.VillagerTrades.ItemListing[] listings,
                                     net.minecraft.util.RandomSource rng) {
        if (listings == null) return;
        for (net.minecraft.world.entity.npc.VillagerTrades.ItemListing listing : listings) {
            JsonObject o = new JsonObject();
            o.addProperty("profession", profession);
            o.addProperty("mod", profession.contains(":") ? profession.split(":")[0] : "?");
            o.addProperty("level", level);
            safe(() -> o.addProperty("listing_class", listing.getClass().getName()));
            boolean[] ok = {false};
            safe(() -> {
                MerchantOffer offer = listing.getOffer(null, rng);
                if (offer != null) {
                    safe(() -> o.add("cost_a", stackJson(offer.getBaseCostA())));
                    safe(() -> { if (!offer.getCostB().isEmpty()) o.add("cost_b", stackJson(offer.getCostB())); });
                    safe(() -> o.add("result", stackJson(offer.getResult())));
                    safe(() -> o.addProperty("max_uses", offer.getMaxUses()));
                    safe(() -> o.addProperty("xp", offer.getXp()));
                    ok[0] = true;
                }
            });
            if (!ok[0]) o.addProperty("offer_available", false);
            arr.add(o);
        }
    }

    private static JsonObject stackJson(ItemStack stack) {
        JsonObject o = new JsonObject();
        o.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        o.addProperty("count", stack.getCount());
        return o;
    }

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

    /**
     * Every registered Smithery material with its full stat block. Empty when Smithery is absent —
     * the class doing the work names Smithery types, so it must not load without it.
     */
    private static JsonArray dumpSmitheryMaterials() {
        if (!net.minecraftforge.fml.ModList.get().isLoaded("smithery")) return new JsonArray();
        return com.soul.soa_additions.smithery.SmitheryMaterialExport.dump();
    }
}
