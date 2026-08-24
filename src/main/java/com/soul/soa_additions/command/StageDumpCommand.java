package com.soul.soa_additions.command;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.mixin.itemstages.RestrictionManagerAccessor;
import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * {@code /soastagedump} — writes the pack's complete staging state to
 * {@code logs/soa_stage_dump.json} for offline comparison with GreedyCraft.
 *
 * <p>The GC→SoA staging port has been audited stage group by stage group, but
 * that can only find what an auditor thought to look for. GreedyCraft's
 * crafttweaker.log records its full runtime state — 5,542 item-stage additions
 * (including 39 ore-dictionary expansion blocks) and 986 recipe stagings. This
 * command produces the same picture from the running pack so the two can be
 * diffed wholesale, turning "we fixed N findings" into a measured coverage
 * figure plus an explicit list of what is still unstaged.</p>
 *
 * <p>Both halves are enumerated the only way each mod allows:</p>
 * <ul>
 *   <li><b>Items</b> — ItemStages has no listing API, so this walks the
 *       restriction multimap (via {@link RestrictionManagerAccessor}) and tests
 *       every registered item against every restriction. Restrictions are
 *       predicates, so there is no way to ask one which items it covers; they
 *       have to be probed. That is ~35k items × ~1.5k restrictions, hence the
 *       admin-only permission and the "this will hitch" warning.</li>
 *   <li><b>Recipes</b> — RecipeStages wraps staged recipes in
 *       {@code IStagedRecipe}, which exposes {@code getStage()} directly.</li>
 * </ul>
 *
 * <p>Names are taken from a bare {@link ItemStack}, matching the display-name
 * matching rule the port uses. ItemStages' "Unknown Item" renaming is a
 * client-side tooltip effect and does not reach {@code getHoverName} here.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StageDumpCommand {

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private StageDumpCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soastagedump")
                        .requires(src -> src.hasPermission(2))
                        .executes(StageDumpCommand::dump));
    }

    private record Entry(String id, String name) {}

    private record RecipeEntry(String recipe, String output, String name) {}

    private static int dump(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Dumping stage state - the server will hitch for a few seconds..."), true);

        final Map<String, List<Entry>> items = new LinkedHashMap<>();
        int itemCount = 0;
        try {
            final Multimap<String, Restriction> restrictions =
                    ((RestrictionManagerAccessor) RestrictionManager.INSTANCE).soa$restrictions();

            for (Item item : ForgeRegistries.ITEMS) {
                final ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, Restriction> entry : restrictions.entries()) {
                    if (entry.getValue().isRestricted(stack)) {
                        items.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                .add(new Entry(ForgeRegistries.ITEMS.getKey(item).toString(),
                                        stack.getHoverName().getString()));
                        itemCount++;
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Stage dump: could not read ItemStages restrictions", t);
            src.sendFailure(Component.literal("Could not read ItemStages restrictions: " + t));
            return 0;
        }

        final Map<String, List<RecipeEntry>> recipes = new LinkedHashMap<>();
        int recipeCount = 0;
        try {
            // RecipeStages is not a compile dependency of this mod, so IStagedRecipe
            // is reached reflectively rather than dragging in a maven coordinate for
            // one debug command.
            final Class<?> stagedType = Class.forName("com.blamejared.recipestages.recipes.IStagedRecipe");
            final var getStage = stagedType.getMethod("getStage");
            final var access = src.getServer().registryAccess();
            for (Recipe<?> recipe : src.getServer().getRecipeManager().getRecipes()) {
                if (!stagedType.isInstance(recipe)) {
                    continue;
                }
                final String stage = String.valueOf(getStage.invoke(recipe));
                final ItemStack result = recipe.getResultItem(access);
                recipes.computeIfAbsent(stage, k -> new ArrayList<>())
                        .add(new RecipeEntry(recipe.getId().toString(),
                                result.isEmpty() ? "" : ForgeRegistries.ITEMS.getKey(result.getItem()).toString(),
                                result.isEmpty() ? "" : result.getHoverName().getString()));
                recipeCount++;
            }
        } catch (Throwable t) {
            LOGGER.error("Stage dump: could not read staged recipes", t);
            src.sendFailure(Component.literal("Could not read staged recipes: " + t));
        }

        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("items", items);
        root.put("recipes", recipes);

        final Path out = FMLPaths.GAMEDIR.get().resolve("logs").resolve("soa_stage_dump.json");
        try {
            Files.createDirectories(out.getParent());
            try (Writer w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            LOGGER.error("Stage dump: could not write {}", out, e);
            src.sendFailure(Component.literal("Could not write dump: " + e));
            return 0;
        }

        final int stagedItems = itemCount;
        final int stagedRecipes = recipeCount;
        final int itemStages = items.size();
        final int recipeStages = recipes.size();
        src.sendSuccess(() -> Component.literal(
                "Wrote " + out + " - " + stagedItems + " item restrictions across " + itemStages
                        + " stages, " + stagedRecipes + " staged recipes across " + recipeStages + " stages."), true);
        return 1;
    }
}
