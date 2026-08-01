package com.soul.soa_additions.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.recipe.PureDaisyRecipe;

import java.util.List;

/**
 * Applies Botania's Pure Daisy transmutations in bulk — the guts of
 * GreedyCraft's {@code /purifyingdust} command
 * ({@code scripts/events_and_commands/commands.zs}), which the Dust of
 * Purifying triggers.
 *
 * <p>GC iterated its own {@code pureDaisyTransmutations} map, which was also the
 * source of its Pure Daisy recipe registrations. On 1.20.1 the pack ships that
 * same map as {@code botania:pure_daisy} recipes, so reading the recipe manager
 * keeps the two in lockstep instead of duplicating the table.</p>
 *
 * <p>Botania is a compile-only dependency: only touch this class behind a
 * {@code ModList.isLoaded("botania")} check.</p>
 */
public final class BotaniaPureDaisy {

    private BotaniaPureDaisy() {}

    /**
     * Transmutes every convertible block inside {@code radius} of {@code centre}.
     * GC used a sphere of radius 7 scanned as a cube — same shape here.
     *
     * @return how many blocks changed
     */
    @SuppressWarnings("unchecked")
    public static int convertAround(ServerLevel level, BlockPos centre, int radius) {
        RecipeType<?> rawType = BuiltInRegistries.RECIPE_TYPE.get(PureDaisyRecipe.TYPE_ID);
        if (rawType == null) return 0;
        List<PureDaisyRecipe> recipes =
                level.getRecipeManager().getAllRecipesFor((RecipeType<PureDaisyRecipe>) rawType);
        if (recipes.isEmpty()) return 0;

        int converted = 0;
        int r2 = radius * radius;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz >= r2) continue;
                    pos.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.BEDROCK)) continue;
                    for (PureDaisyRecipe recipe : recipes) {
                        if (!recipe.getInput().test(state)) continue;
                        level.setBlock(pos.immutable(), recipe.getOutputState(), 3);
                        converted++;
                        break;
                    }
                }
            }
        }
        return converted;
    }
}
