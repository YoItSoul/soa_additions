package com.soul.soa_additions.tr.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.Aspects;
import com.soul.soa_additions.tr.knowledge.KnownAspects;
import com.soul.soa_additions.tr.knowledge.ScannedTargets;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Op-only commands for managing player aspect discovery.
 *
 * <pre>
 * /tr discover &lt;aspect&gt; [target]   — grant one aspect (defaults to self)
 * /tr discover all      [target]   — grant every aspect
 * /tr forget   &lt;aspect&gt; [target]   — remove one aspect
 * /tr forget   all      [target]   — remove all
 * /tr list              [target]   — list known aspects
 * </pre>
 *
 * <p>Autocomplete on the {@code aspect} argument lists exactly the aspects
 * the target does NOT yet know for {@code discover}, and exactly the aspects
 * the target DOES know for {@code forget}, plus the literal {@code all}.
 * This makes survival ops grant new aspects with one tab instead of paging
 * through a 48-entry list, and forgets the same way.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrCommands {

    private TrCommands() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tr")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("discover")
                        .then(Commands.argument("aspect", StringArgumentType.word())
                                .suggests(suggestUndiscoveredOrAll())
                                .executes(ctx -> runDiscover(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> runDiscover(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                .then(Commands.literal("forget")
                        .then(Commands.argument("aspect", StringArgumentType.word())
                                .suggests(suggestKnownOrAll())
                                .executes(ctx -> runForget(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> runForget(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                .then(Commands.literal("list")
                        .executes(ctx -> runList(ctx, self(ctx)))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> runList(ctx, EntityArgument.getPlayer(ctx, "target")))))
                // ----- Block/item scan management -----
                .then(Commands.literal("scan")
                        .then(Commands.literal("block")
                                .then(Commands.argument("block", ResourceLocationArgument.id())
                                        .suggests(suggestUnscannedBlocks())
                                        .executes(ctx -> runScanBlock(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> runScanBlock(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                        .then(Commands.literal("item")
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(suggestUnscannedItems())
                                        .executes(ctx -> runScanItem(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> runScanItem(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                        .then(Commands.literal("looking")
                                .executes(ctx -> runScanLooking(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> runScanLooking(ctx, EntityArgument.getPlayer(ctx, "target")))))
                        .then(Commands.literal("all")
                                .executes(ctx -> runScanAll(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> runScanAll(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                .then(Commands.literal("unscan")
                        .then(Commands.literal("block")
                                .then(Commands.argument("block", ResourceLocationArgument.id())
                                        .suggests(suggestScannedBlocks())
                                        .executes(ctx -> runUnscanBlock(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> runUnscanBlock(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                        .then(Commands.literal("item")
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests(suggestScannedItems())
                                        .executes(ctx -> runUnscanItem(ctx, self(ctx)))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> runUnscanItem(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                        .then(Commands.literal("all")
                                .executes(ctx -> runUnscanAll(ctx, self(ctx)))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> runUnscanAll(ctx, EntityArgument.getPlayer(ctx, "target"))))))
                // ----- Chunk aura inspection -----
                .then(Commands.literal("aura")
                        .executes(TrCommands::runAuraHere)
                        .then(Commands.literal("here").executes(TrCommands::runAuraHere))
                        .then(Commands.argument("chunkX", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                .then(Commands.argument("chunkZ", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                        .executes(TrCommands::runAuraAt))))
                // ----- Aspect resolution debug -----
                .then(Commands.literal("aspects")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests(suggestAllItems())
                                .executes(TrCommands::runAspectsOf))));
    }

    // ---------------- Suggestion providers ----------------

    /** Suggest aspect ids the target doesn't yet know, plus "all". The
     *  target falls back to the command source's own player when no target
     *  argument is present yet (during typing). */
    private static SuggestionProvider<CommandSourceStack> suggestUndiscoveredOrAll() {
        return (ctx, builder) -> suggestFiltered(ctx, builder,
                aspect -> !knownByEffectiveTarget(ctx).contains(aspect.id()));
    }

    private static SuggestionProvider<CommandSourceStack> suggestKnownOrAll() {
        return (ctx, builder) -> suggestFiltered(ctx, builder,
                aspect -> knownByEffectiveTarget(ctx).contains(aspect.id()));
    }

    private static CompletableFuture<Suggestions> suggestFiltered(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder,
            java.util.function.Predicate<Aspect> include) {
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add("all");
        for (Aspect a : Aspects.all()) {
            if (include.test(a)) options.add(a.id().getPath());
        }
        return SharedSuggestionProvider.suggest(options, builder);
    }

    /** Best-effort target lookup during suggest — at suggestion time the
     *  optional {@code target} argument may not be parsed yet, so we fall
     *  back to the command source. */
    private static Collection<ResourceLocation> knownByEffectiveTarget(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            return KnownAspects.of(target).snapshot();
        } catch (Throwable ignored) {}
        try {
            ServerPlayer self = ctx.getSource().getPlayerOrException();
            return KnownAspects.of(self).snapshot();
        } catch (Throwable ignored) {}
        return Collections.emptySet();
    }

    private static ServerPlayer self(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    // ---------------- Executors ----------------

    private static int runDiscover(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        String name = StringArgumentType.getString(ctx, "aspect");
        CommandSourceStack src = ctx.getSource();
        if ("all".equalsIgnoreCase(name)) {
            int added = KnownAspects.discoverAll(target);
            src.sendSuccess(() -> Component.translatable("command.tr.discover.all",
                    added, target.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return added;
        }
        Aspect aspect = lookup(name);
        if (aspect == null) { sendUnknown(src, name); return 0; }
        boolean added = KnownAspects.discover(target, aspect);
        if (added) {
            src.sendSuccess(() -> Component.translatable("command.tr.discover.success",
                    aspect.displayName(), target.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return 1;
        } else {
            src.sendSuccess(() -> Component.translatable("command.tr.discover.already",
                    target.getDisplayName(), aspect.displayName()).withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
    }

    private static int runForget(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        String name = StringArgumentType.getString(ctx, "aspect");
        CommandSourceStack src = ctx.getSource();
        if ("all".equalsIgnoreCase(name)) {
            int removed = KnownAspects.forgetAll(target);
            src.sendSuccess(() -> Component.translatable("command.tr.forget.all",
                    target.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
            return removed;
        }
        Aspect aspect = lookup(name);
        if (aspect == null) { sendUnknown(src, name); return 0; }
        boolean removed = KnownAspects.forget(target, aspect);
        if (removed) {
            src.sendSuccess(() -> Component.translatable("command.tr.forget.success",
                    aspect.displayName(), target.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
            return 1;
        } else {
            src.sendSuccess(() -> Component.translatable("command.tr.forget.unknown",
                    target.getDisplayName(), aspect.displayName()).withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
    }

    private static int runList(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack src = ctx.getSource();
        KnownAspects.Data d = KnownAspects.of(target);
        int total = Aspects.all().size();
        src.sendSuccess(() -> Component.translatable("command.tr.list.header",
                target.getDisplayName(), d.size(), total).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        for (Aspect a : Aspects.all()) {
            if (!d.has(a)) continue;
            src.sendSuccess(() -> Component.translatable("command.tr.list.entry",
                    a.displayName(), a.tier()).withStyle(ChatFormatting.GRAY), false);
        }
        return d.size();
    }

    private static Aspect lookup(String name) {
        try {
            ResourceLocation rl = name.indexOf(':') >= 0
                    ? new ResourceLocation(name)
                    : new ResourceLocation(ThaumicRemnants.MODID, name);
            return Aspects.byId(rl);
        } catch (Exception e) {
            return null;
        }
    }

    private static void sendUnknown(CommandSourceStack src, String name) {
        src.sendFailure(Component.literal("Unknown aspect: " + name).withStyle(ChatFormatting.RED));
    }

    // ---------------- Scan: suggestion providers ----------------

    /** Suggest registered block ids the target hasn't yet scanned. Limits
     *  matches to a sensible cap so a fresh save doesn't try to suggest
     *  thousands of vanilla+mod blocks at once. */
    private static SuggestionProvider<CommandSourceStack> suggestUnscannedBlocks() {
        return (ctx, builder) -> {
            ScannedTargets.Data d = scannedByEffectiveTarget(ctx);
            return SharedSuggestionProvider.suggestResource(
                    BuiltInRegistries.BLOCK.keySet().stream().filter(id -> !d.hasBlock(id)),
                    builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestScannedBlocks() {
        return (ctx, builder) -> {
            ScannedTargets.Data d = scannedByEffectiveTarget(ctx);
            return SharedSuggestionProvider.suggestResource(d.blockSnapshot().stream(), builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestUnscannedItems() {
        return (ctx, builder) -> {
            ScannedTargets.Data d = scannedByEffectiveTarget(ctx);
            return SharedSuggestionProvider.suggestResource(
                    BuiltInRegistries.ITEM.keySet().stream().filter(id -> !d.hasItem(id)),
                    builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestScannedItems() {
        return (ctx, builder) -> {
            ScannedTargets.Data d = scannedByEffectiveTarget(ctx);
            return SharedSuggestionProvider.suggestResource(d.itemSnapshot().stream(), builder);
        };
    }

    private static SuggestionProvider<CommandSourceStack> suggestAllItems() {
        return (ctx, builder) -> SharedSuggestionProvider.suggestResource(
                BuiltInRegistries.ITEM.keySet().stream(), builder);
    }

    private static ScannedTargets.Data scannedByEffectiveTarget(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
            return ScannedTargets.of(target);
        } catch (Throwable ignored) {}
        try {
            ServerPlayer self = ctx.getSource().getPlayerOrException();
            return ScannedTargets.of(self);
        } catch (Throwable ignored) {}
        return new ScannedTargets.Data();
    }

    // ---------------- Scan: executors ----------------

    private static int runScanBlock(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "block");
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            ctx.getSource().sendFailure(Component.literal("Unknown block: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }
        // Disambiguate "already scanned" vs "capability not attached" before
        // calling the API, so the player gets a useful error in either case.
        ScannedTargets.Data d = ScannedTargets.of(target);
        if (d == ScannedTargets.empty()) return capMissing(ctx.getSource(), target);
        if (d.hasBlock(id)) {
            sendScanResult(ctx.getSource(), false, "block", id, target);
            return 0;
        }
        ScannedTargets.scanBlock(target, id);
        sendScanResult(ctx.getSource(), true, "block", id, target);
        return 1;
    }

    private static int runScanItem(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            ctx.getSource().sendFailure(Component.literal("Unknown item: " + id).withStyle(ChatFormatting.RED));
            return 0;
        }
        ScannedTargets.Data d = ScannedTargets.of(target);
        if (d == ScannedTargets.empty()) return capMissing(ctx.getSource(), target);
        if (d.hasItem(id)) {
            sendScanResult(ctx.getSource(), false, "item", id, target);
            return 0;
        }
        ScannedTargets.scanItem(target, id);
        sendScanResult(ctx.getSource(), true, "item", id, target);
        return 1;
    }

    /** Scan whatever block the source is looking at (raycast from the command
     *  caller's eye). Useful for survival ops who want to grant the block
     *  they're staring at without typing its id. */
    private static int runScanLooking(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer caller = ctx.getSource().getPlayerOrException();
        var hit = caller.pick(20.0, 0f, false);
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult bhr)
                || bhr.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
            ctx.getSource().sendFailure(Component.literal("Not looking at any block.").withStyle(ChatFormatting.RED));
            return 0;
        }
        Block b = caller.level().getBlockState(bhr.getBlockPos()).getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b);
        ScannedTargets.Data d = ScannedTargets.of(target);
        if (d == ScannedTargets.empty()) return capMissing(ctx.getSource(), target);
        if (d.hasBlock(id)) {
            sendScanResult(ctx.getSource(), false, "block", id, target);
            return 0;
        }
        ScannedTargets.scanBlock(target, id);
        sendScanResult(ctx.getSource(), true, "block", id, target);
        return 1;
    }

    private static int capMissing(CommandSourceStack src, ServerPlayer target) {
        src.sendFailure(Component.literal(
                "Scanned-targets capability not attached to " + target.getGameProfile().getName()
                        + " — was the cap registered? (mod load order issue, or a stale jar)")
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int runScanAll(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        int added = ScannedTargets.scanAll(target);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.tr.scan.all",
                added, target.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return added;
    }

    private static int runUnscanBlock(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "block");
        boolean removed = ScannedTargets.unscanBlock(target, id);
        sendUnscanResult(ctx.getSource(), removed, "block", id, target);
        return removed ? 1 : 0;
    }

    private static int runUnscanItem(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        boolean removed = ScannedTargets.unscanItem(target, id);
        sendUnscanResult(ctx.getSource(), removed, "item", id, target);
        return removed ? 1 : 0;
    }

    private static int runUnscanAll(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        int removed = ScannedTargets.unscanAll(target);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.tr.unscan.all",
                target.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
        return removed;
    }

    private static void sendScanResult(CommandSourceStack src, boolean added, String kind,
                                        ResourceLocation id, ServerPlayer target) {
        if (added) {
            src.sendSuccess(() -> Component.translatable("command.tr.scan." + kind + ".success",
                    id.toString(), target.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        } else {
            src.sendSuccess(() -> Component.translatable("command.tr.scan." + kind + ".already",
                    target.getDisplayName(), id.toString()).withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static void sendUnscanResult(CommandSourceStack src, boolean removed, String kind,
                                          ResourceLocation id, ServerPlayer target) {
        if (removed) {
            src.sendSuccess(() -> Component.translatable("command.tr.unscan." + kind + ".success",
                    id.toString(), target.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
        } else {
            src.sendSuccess(() -> Component.translatable("command.tr.unscan." + kind + ".unknown",
                    target.getDisplayName(), id.toString()).withStyle(ChatFormatting.GRAY), false);
        }
    }

    // ---------------- Aura inspection ----------------

    private static int runAuraHere(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer caller = ctx.getSource().getPlayerOrException();
        var pos = caller.blockPosition();
        return showAura(ctx, pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static int runAuraAt(CommandContext<CommandSourceStack> ctx) {
        int x = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "chunkX");
        int z = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "chunkZ");
        return showAura(ctx, x, z);
    }

    /** Print the chunk's aspect pool to the source. Sorted by amount
     *  descending so the dominant aspects (the player's likely auramancy
     *  draws) appear first. */
    private static int showAura(CommandContext<CommandSourceStack> ctx, int chunkX, int chunkZ) {
        CommandSourceStack src = ctx.getSource();
        var sl = src.getLevel();
        var chunk = sl.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) {
            src.sendFailure(Component.literal("Chunk [" + chunkX + ", " + chunkZ + "] is not loaded.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        var aura = com.soul.soa_additions.tr.aura.ChunkAura.of(chunk);
        if (aura == com.soul.soa_additions.tr.aura.ChunkAura.empty()) {
            src.sendFailure(Component.literal("ChunkAura capability not attached to chunk [" + chunkX + ", " + chunkZ + "].")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        var snapshot = aura.snapshot();
        if (snapshot.isEmpty()) {
            src.sendSuccess(() -> Component.literal("Chunk [" + chunkX + ", " + chunkZ + "] aura: empty (no biome seed entries, no impart yet)")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        // Header line with total / cap so the player can see how saturated the chunk is.
        int total = aura.total();
        src.sendSuccess(() -> Component.literal(String.format(
                        "Chunk [%d, %d] aura — total %d / %d",
                        chunkX, chunkZ, total,
                        com.soul.soa_additions.tr.aura.ChunkAura.TOTAL_CAP))
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        // Sorted desc by amount; tiebreak on aspect path for stable output.
        snapshot.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    return cmp != 0 ? cmp : a.getKey().id().getPath().compareTo(b.getKey().id().getPath());
                })
                .forEach(entry -> src.sendSuccess(() -> Component.literal(String.format(
                                "  %s × %d",
                                entry.getKey().englishName(), entry.getValue()))
                        .withStyle(ChatFormatting.GRAY), false));
        return total;
    }

    /** /tr aspects <item> — surface what the resolver returns for an item,
     *  broken down by which layer (JSON / tag / recipe-derived) contributed
     *  what. Lets us audit the dynamic system from in-game without scraping
     *  logs. */
    private static int runAspectsOf(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        CommandSourceStack src = ctx.getSource();
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || id == null) {
            src.sendFailure(Component.literal("Unknown item: " + id));
            return 0;
        }

        var jsonOnly = com.soul.soa_additions.tr.core.AspectMap.forItemJsonOnly(item);
        var tagOnly = com.soul.soa_additions.tr.aspect.derive.TagAspectRegistry.resolve(item);
        var resolved = com.soul.soa_additions.tr.core.AspectMap.forItem(item, src.getLevel());

        src.sendSuccess(() -> Component.literal("Aspects for " + id)
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        printAspectLine(src, "JSON override", jsonOnly);
        printAspectLine(src, "Tag bulk", tagOnly);
        printAspectLine(src, "Resolved (final)", resolved);
        src.sendSuccess(() -> Component.literal(
                "  (resolved = JSON if present, else tag + recipe-derived. Cache size: "
                        + com.soul.soa_additions.tr.aspect.derive.RecipeAspectDeriver.cacheSize() + ")")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return resolved.size();
    }

    private static void printAspectLine(CommandSourceStack src, String label,
                                         java.util.List<com.soul.soa_additions.tr.core.AspectStack> stacks) {
        if (stacks.isEmpty()) {
            src.sendSuccess(() -> Component.literal("  " + label + ": (none)")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
            return;
        }
        StringBuilder sb = new StringBuilder("  ").append(label).append(": ");
        for (int i = 0; i < stacks.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(stacks.get(i).aspect().englishName())
                    .append("×").append(stacks.get(i).amount());
        }
        src.sendSuccess(() -> Component.literal(sb.toString())
                .withStyle(ChatFormatting.GRAY), false);
    }
}
