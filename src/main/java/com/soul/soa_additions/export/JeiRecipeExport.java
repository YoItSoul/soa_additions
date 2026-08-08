package com.soul.soa_additions.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.soul.soa_additions.SoaAdditions;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code /soa export jei} — writes {@code soa_exports/jei_recipes.json},
 * every recipe JEI can display, with inputs, outputs and catalysts resolved.
 *
 * <p>For each JEI category this asks JEI to build the recipe's layout and then
 * reads the slot views back. Each slot carries a {@link RecipeIngredientRole}
 * (INPUT / OUTPUT / CATALYST / RENDER_ONLY) and its ingredients, so the same
 * code reads a vanilla shaped recipe, a Blood Magic altar infusion and a
 * CustomMachinery process without knowing anything about any of them.</p>
 *
 * <p>Ingredients are recorded by JEI's own ingredient helper: item stacks as
 * item ids, fluids and every other ingredient type by their type uid plus the
 * helper's resource location, so nothing is silently dropped for being a
 * non-item.</p>
 *
 * <p>Client command, because the JEI runtime is client-only. Run it in a world;
 * a dedicated server cannot produce this file.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
public final class JeiRecipeExport {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private JeiRecipeExport() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Registered on the CLIENT dispatcher under the same /soa export path as
        // the server-side targets, so it shows up where you'd look for it.
        // Forge checks the client dispatcher first: "/soa export jei" matches
        // here and runs locally, while "/soa export all" fails to match and
        // falls through to the server command in RegistryExportCommand.
        dispatcher.register(Commands.literal("soa")
                .then(Commands.literal("export")
                        .then(Commands.literal("jei")
                                .executes(ctx -> run(ctx.getSource())))));
    }

    private static int run(CommandSourceStack src) {
        IJeiRuntime runtime = JeiExportPlugin.runtime();
        if (runtime == null) {
            src.sendFailure(Component.literal(
                    "JEI runtime not available — is JEI installed and finished loading?"));
            return 0;
        }

        IRecipeManager rm = runtime.getRecipeManager();
        IIngredientManager im = runtime.getIngredientManager();
        IFocusFactory focus = runtime.getJeiHelpers().getFocusFactory();

        JsonArray out = new JsonArray();
        Map<String, Integer> perCategory = new HashMap<>();
        int failed = 0;

        List<IRecipeCategory<?>> categories =
                rm.createRecipeCategoryLookup().includeHidden().get().toList();

        for (IRecipeCategory<?> category : categories) {
            failed += dumpCategory(rm, im, focus, category, out, perCategory);
        }

        Path dir = FMLPaths.GAMEDIR.get().resolve("soa_exports");
        try {
            Files.createDirectories(dir);
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("count", out.size());
            wrapper.addProperty("categories", categories.size());
            wrapper.addProperty("unreadable", failed);
            JsonObject byCat = new JsonObject();
            perCategory.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> byCat.addProperty(e.getKey(), e.getValue()));
            wrapper.add("by_category", byCat);
            wrapper.add("entries", out);
            Files.writeString(dir.resolve("jei_recipes.json"), GSON.toJson(wrapper));
        } catch (IOException e) {
            src.sendFailure(Component.literal("Write failed: " + e.getMessage()));
            return 0;
        }

        final String summary = "Exported " + out.size() + " JEI recipes across "
                + categories.size() + " categories (" + failedCount(failed)
                + ") -> soa_exports/jei_recipes.json";
        src.sendSuccess(() -> Component.literal(summary), false);
        return 1;
    }

    private static String failedCount(int failed) {
        return failed == 0 ? "all readable" : failed + " unreadable";
    }

    /** Split out so the wildcard capture on IRecipeCategory has a name. */
    private static <T> int dumpCategory(IRecipeManager rm, IIngredientManager im, IFocusFactory focus,
                                        IRecipeCategory<T> category, JsonArray out,
                                        Map<String, Integer> perCategory) {
        RecipeType<T> type = category.getRecipeType();
        String typeUid = type.getUid().toString();
        int failed = 0;

        List<T> recipes;
        try {
            recipes = rm.createRecipeLookup(type).includeHidden().get().toList();
        } catch (Throwable t) {
            return 1;
        }

        for (T recipe : recipes) {
            JsonObject o = new JsonObject();
            o.addProperty("category", typeUid);
            o.addProperty("mod", type.getUid().getNamespace());
            try {
                o.addProperty("recipe_class", recipe.getClass().getName());
            } catch (Throwable ignored) {}
            // vanilla-style recipes still carry their datapack id; keep it so this
            // file can be joined against recipes.json
            if (recipe instanceof Recipe<?> r) {
                try {
                    o.addProperty("id", r.getId().toString());
                } catch (Throwable ignored) {}
            }

            JsonArray inputs = new JsonArray();
            JsonArray outputs = new JsonArray();
            JsonArray catalysts = new JsonArray();

            boolean read = false;
            try {
                Optional<IRecipeLayoutDrawable<T>> layout =
                        rm.createRecipeLayoutDrawable(category, recipe, focus.getEmptyFocusGroup());
                if (layout.isPresent()) {
                    for (IRecipeSlotView slot : layout.get().getRecipeSlotsView().getSlotViews()) {
                        JsonArray target = switch (slot.getRole()) {
                            case INPUT -> inputs;
                            case OUTPUT -> outputs;
                            case CATALYST -> catalysts;
                            default -> null;
                        };
                        if (target == null) continue;
                        JsonArray alts = new JsonArray();
                        slot.getAllIngredients().forEach(ing -> {
                            JsonObject j = describe(im, ing);
                            if (j != null) alts.add(j);
                        });
                        if (alts.size() > 0) target.add(alts);
                    }
                    read = true;
                }
            } catch (Throwable ignored) {}

            if (!read) {
                o.addProperty("readable", false);
                failed++;
            } else {
                o.add("inputs", inputs);
                o.add("outputs", outputs);
                if (catalysts.size() > 0) o.add("catalysts", catalysts);
            }
            out.add(o);
            perCategory.merge(typeUid, 1, Integer::sum);
        }
        return failed;
    }

    /** Describe one ingredient generically, via JEI's own helper for its type. */
    private static <V> JsonObject describe(IIngredientManager im, ITypedIngredient<V> ing) {
        JsonObject j = new JsonObject();
        try {
            V value = ing.getIngredient();
            if (value instanceof ItemStack stack) {
                if (stack.isEmpty()) return null;
                j.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                j.addProperty("count", stack.getCount());
                if (stack.hasTag()) j.addProperty("nbt", String.valueOf(stack.getTag()));
                return j;
            }
            // fluids and every other registered ingredient type
            IIngredientHelper<V> helper = im.getIngredientHelper(ing.getType());
            j.addProperty("type", ing.getType().getIngredientClass().getSimpleName());
            try {
                j.addProperty("id", helper.getResourceLocation(value).toString());
            } catch (Throwable ignored) {
                j.addProperty("uid", helper.getUniqueId(value, mezz.jei.api.ingredients.subtypes.UidContext.Recipe));
            }
            try {
                j.addProperty("name", helper.getDisplayName(value));
            } catch (Throwable ignored) {}
            return j;
        } catch (Throwable t) {
            return null;
        }
    }
}
