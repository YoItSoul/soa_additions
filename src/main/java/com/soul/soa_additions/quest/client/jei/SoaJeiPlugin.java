package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.oresight.OreSight;
import com.soul.soa_additions.oresight.OreSightBrewing;
import com.soul.soa_additions.potion.SoaBrewingPotions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI integration entry point. JEI scans for {@link JeiPlugin} annotated
 * classes and instantiates them at startup; we use the hook for two things:
 *
 * <ol>
 *   <li>Capture the {@link IJeiRuntime} so {@link JeiCompat} can open recipe
 *       views from the quest book.</li>
 *   <li>Register brewing recipes that vanilla's auto-scanner can't surface —
 *       specifically the ore-sight chain, which uses dynamic
 *       {@link net.minecraftforge.common.brewing.IBrewingRecipe} matchers
 *       rather than static {@link net.minecraftforge.common.brewing.BrewingRecipe}
 *       triples. Without explicit registration JEI shows nothing for the
 *       Mutagenic / Ore Sight / Long Ore Sight steps.</li>
 * </ol>
 */
@JeiPlugin
public final class SoaJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(SoaAdditions.MODID, "jei_plugin");

    private static boolean lootbagsLoaded() {
        return net.minecraftforge.fml.ModList.get().isLoaded("lootbags");
    }

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new QuestRewardCategory(guiHelper,
                new ItemStack(ModItems.QUEST_BOOK.get())));
        if (lootbagsLoaded()) {
            var bagItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("lootbags", "loot_bag"));
            registration.addRecipeCategories(new LootCrateCategory(guiHelper,
                    new ItemStack(bagItem != null ? bagItem : Items.CHEST)));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.QUEST_BOOK.get()), QuestRewardCategory.TYPE);
        if (lootbagsLoaded()) {
            var bagItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("lootbags", "loot_bag"));
            if (bagItem != null) {
                registration.addRecipeCatalyst(new ItemStack(bagItem), LootCrateCategory.TYPE);
            }
            registration.addRecipeCatalyst(new ItemStack(ModItems.REWARD_TICKET_COMMON.get()), LootCrateCategory.TYPE);
            registration.addRecipeCatalyst(new ItemStack(ModItems.REWARD_TICKET_RARE.get()), LootCrateCategory.TYPE);
            registration.addRecipeCatalyst(new ItemStack(ModItems.REWARD_TICKET_EPIC.get()), LootCrateCategory.TYPE);
            registration.addRecipeCatalyst(new ItemStack(ModItems.REWARD_TICKET_LEGENDARY.get()), LootCrateCategory.TYPE);
        }
    }

    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiCompat.setRuntime(runtime);
    }

    @Override public void onRuntimeUnavailable() {
        JeiCompat.setRuntime(null);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiBrewingRecipe> brewing = new ArrayList<>();

        // ── Step 1: water + thaumon:mutagen → mutagenic
        var mutagenItem = ForgeRegistries.ITEMS.getValue(OreSightBrewing.MUTAGEN_ID);
        if (mutagenItem != null) {
            ItemStack waterPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            ItemStack mutagenicPotion = PotionUtils.setPotion(new ItemStack(Items.POTION),
                    SoaBrewingPotions.MUTAGENIC.get());
            brewing.add(factory.createBrewingRecipe(
                    List.of(new ItemStack(mutagenItem)),
                    waterPotion, mutagenicPotion));
        }

        // ── Step 2: mutagenic + ore-block → ore_sight (per ore)
        // ── Step 3: ore_sight + redstone → long_ore_sight (per ore)
        Potion baseMutagenic = SoaBrewingPotions.MUTAGENIC.get();
        Potion baseOreSight = SoaBrewingPotions.ORE_SIGHT.get();
        Potion longOreSight = SoaBrewingPotions.LONG_ORE_SIGHT.get();
        ItemStack mutagenicPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), baseMutagenic);
        ItemStack redstone = new ItemStack(Items.REDSTONE);

        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (!OreSight.isOreBlock(block)) continue;
            ItemStack blockStack = new ItemStack(block.asItem());
            if (blockStack.isEmpty()) continue;
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
            if (blockId == null) continue;

            // Output: ore_sight potion tagged with this block's id (custom item)
            ItemStack oreSightPotion = PotionUtils.setPotion(
                    new ItemStack(ModItems.ORE_SIGHT_POTION.get()), baseOreSight);
            CompoundTag tag = oreSightPotion.getOrCreateTag();
            tag.putString(OreSightBrewing.NBT_BLOCK, blockId.toString());

            brewing.add(factory.createBrewingRecipe(
                    List.of(blockStack), mutagenicPotion, oreSightPotion));

            // Long variant: ore_sight + redstone → long_ore_sight (NBT preserved)
            ItemStack longPotion = PotionUtils.setPotion(
                    new ItemStack(ModItems.ORE_SIGHT_POTION.get()), longOreSight);
            longPotion.getOrCreateTag().putString(OreSightBrewing.NBT_BLOCK, blockId.toString());

            brewing.add(factory.createBrewingRecipe(
                    List.of(redstone), oreSightPotion.copy(), longPotion));
        }

        registration.addRecipes(RecipeTypes.BREWING, brewing);

        registration.addRecipes(QuestRewardCategory.TYPE, buildQuestRewardDisplays());
        if (lootbagsLoaded()) {
            registration.addRecipes(LootCrateCategory.TYPE, buildLootCratePages());
        }
    }

    // ── Quest Rewards category data ─────────────────────────────────────

    private static List<QuestRewardCategory.Display> buildQuestRewardDisplays() {
        List<QuestRewardCategory.Display> out = new ArrayList<>();
        for (var chapter : com.soul.soa_additions.quest.QuestRegistry.allChapters()) {
            for (var quest : chapter.quests()) {
                List<ItemStack> rewards = new ArrayList<>();
                for (var reward : quest.rewards()) {
                    if (reward instanceof com.soul.soa_additions.quest.reward.ItemReward ir) {
                        var item = ForgeRegistries.ITEMS.getValue(ir.item());
                        if (item != null && item != Items.AIR) rewards.add(new ItemStack(item, ir.count()));
                    } else if (reward instanceof com.soul.soa_additions.quest.reward.CommandReward cr) {
                        ItemStack given = parseGiveCommand(cr.command());
                        if (given != null) rewards.add(given);
                    }
                }
                if (rewards.isEmpty()) continue;
                out.add(new QuestRewardCategory.Display(
                        quest.title(), chapter.title(), resolveQuestIcon(quest.icon()), rewards));
            }
        }
        return out;
    }

    /** Parse "/give @p modid:item{nbt} count" command rewards so loot-crate
     *  gives (and similar) show up as visible quest rewards. */
    private static ItemStack parseGiveCommand(String command) {
        try {
            var m = GIVE_PATTERN.matcher(command.trim());
            if (!m.matches()) return null;
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(m.group(1)));
            if (item == null || item == Items.AIR) return null;
            ItemStack stack = new ItemStack(item, m.group(3) != null ? Integer.parseInt(m.group(3)) : 1);
            if (m.group(2) != null) {
                stack.setTag(net.minecraft.nbt.TagParser.parseTag(m.group(2)));
            }
            return stack;
        } catch (Exception e) {
            return null;
        }
    }

    private static final java.util.regex.Pattern GIVE_PATTERN = java.util.regex.Pattern.compile(
            "^/?give\\s+\\S+\\s+([a-z0-9_.:\\-/]+)(\\{.*})?(?:\\s+(\\d+))?\\s*$");

    /** Same icon-string format the quest book uses: {@code "id"} or {@code "id{snbt}"}. */
    private static ItemStack resolveQuestIcon(String icon) {
        if (icon == null || icon.isEmpty()) return new ItemStack(ModItems.QUEST_BOOK.get());
        try {
            String itemId = icon;
            String snbt = null;
            int brace = icon.indexOf('{');
            if (brace >= 0) {
                itemId = icon.substring(0, brace);
                snbt = icon.substring(brace);
            }
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null || item == Items.AIR) return new ItemStack(ModItems.QUEST_BOOK.get());
            ItemStack stack = new ItemStack(item);
            if (snbt != null) stack.setTag(net.minecraft.nbt.TagParser.parseTag(snbt));
            return stack;
        } catch (Exception e) {
            return new ItemStack(ModItems.QUEST_BOOK.get());
        }
    }

    // ── Loot Crate Contents category data ───────────────────────────────

    private static List<LootCrateCategory.Page> buildLootCratePages() {
        List<LootCrateCategory.Page> pages = new ArrayList<>();
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return pages;
        var typeObj = net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE
                .get(new ResourceLocation("lootbags", "loot"));
        if (typeObj == null) return pages;
        @SuppressWarnings("unchecked")
        var lootType = (net.minecraft.world.item.crafting.RecipeType<tech.thatgravyboat.lootbags.common.recipe.Loot>) typeObj;

        for (var loot : mc.level.getRecipeManager().getAllRecipesFor(lootType)) {
            if (!(loot.output() instanceof tech.thatgravyboat.lootbags.api.LootListOutput listOutput)) continue;
            List<tech.thatgravyboat.lootbags.api.LootEntry> entries = new ArrayList<>(listOutput.entries());
            entries.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
            double total = entries.stream().mapToInt(tech.thatgravyboat.lootbags.api.LootEntry::weight).sum();

            ItemStack bag = loot.createLootBag();
            ItemStack ticket = ticketFor(loot.id());
            int pageCount = Math.max(1, (entries.size() + LootCrateCategory.PAGE_SIZE - 1) / LootCrateCategory.PAGE_SIZE);
            for (int p = 0; p < pageCount; p++) {
                int from = p * LootCrateCategory.PAGE_SIZE;
                int to = Math.min(entries.size(), from + LootCrateCategory.PAGE_SIZE);
                pages.add(new LootCrateCategory.Page(loot.id(), loot.name(), bag, ticket,
                        entries.subList(from, to), p, pageCount, total));
            }
        }
        return pages;
    }

    /** Map a lootbags:loot recipe id (…loot_crate_<tier>) to its reward ticket. */
    private static ItemStack ticketFor(ResourceLocation lootId) {
        String path = lootId.getPath();
        if (path.endsWith("loot_crate_common")) return new ItemStack(ModItems.REWARD_TICKET_COMMON.get());
        if (path.endsWith("loot_crate_rare")) return new ItemStack(ModItems.REWARD_TICKET_RARE.get());
        if (path.endsWith("loot_crate_epic")) return new ItemStack(ModItems.REWARD_TICKET_EPIC.get());
        if (path.endsWith("loot_crate_legendary")) return new ItemStack(ModItems.REWARD_TICKET_LEGENDARY.get());
        return ItemStack.EMPTY;
    }
}
