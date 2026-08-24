package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.SoaAdditions;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import tech.thatgravyboat.lootbags.api.LootEntry;

import java.util.List;

/**
 * "Loot Crate Contents" JEI category. Resourceful Lootbags ships only a
 * subtype interpreter — no content view — so players had no way to see what
 * a crate can roll. One page shows up to {@value #COLUMNS}×{@value #ROWS}
 * weighted entries; crates with more entries emit multiple pages (the rare
 * crate has 121). Recipe-lookup (R) on any contained item lists the crate,
 * and the bag + matching reward ticket are shown as the inputs.
 */
public final class LootCrateCategory implements IRecipeCategory<LootCrateCategory.Page> {

    /** One JEI "recipe": a slice of a crate's weighted entry list. */
    public record Page(ResourceLocation lootId, String crateName, ItemStack bag, ItemStack ticket,
                       List<LootEntry> entries, int page, int pages, double totalWeight) {}

    public static final RecipeType<Page> TYPE =
            RecipeType.create(SoaAdditions.MODID, "loot_crate", Page.class);

    static final int COLUMNS = 9;
    static final int ROWS = 5;
    public static final int PAGE_SIZE = COLUMNS * ROWS;

    private static final int HEADER_H = 24;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public LootCrateCategory(IGuiHelper helper, ItemStack iconStack) {
        this.background = helper.createBlankDrawable(COLUMNS * 18, HEADER_H + ROWS * 18);
        this.icon = helper.createDrawableItemStack(iconStack);
        this.slot = helper.getSlotDrawable();
    }

    @Override public RecipeType<Page> getRecipeType() { return TYPE; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public Component getTitle() {
        return Component.literal("Loot Crate Contents");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 3)
                .setBackground(slot, -1, -1)
                .addItemStack(page.bag());
        if (!page.ticket().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 21, 3)
                    .setBackground(slot, -1, -1)
                    .addItemStack(page.ticket());
        }
        for (int i = 0; i < page.entries().size() && i < PAGE_SIZE; i++) {
            LootEntry entry = page.entries().get(i);
            int x = (i % COLUMNS) * 18 + 1;
            int y = HEADER_H + (i / COLUMNS) * 18 + 1;
            double pct = page.totalWeight() > 0 ? entry.weight() * 100.0 / page.totalWeight() : 0;
            String pctText = pct >= 1 ? String.format("%.1f%%", pct) : String.format("%.2f%%", pct);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(slot, -1, -1)
                    .addItemStack(entry.stack())
                    .addTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.literal("Weight " + entry.weight() + " (" + pctText + " per roll)")
                                    .withStyle(ChatFormatting.GOLD)));
        }
    }

    @Override
    public void draw(Page page, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
                     net.minecraft.client.gui.GuiGraphics g, double mouseX, double mouseY) {
        var font = net.minecraft.client.Minecraft.getInstance().font;
        String label = page.crateName() + (page.pages() > 1
                ? " (" + (page.page() + 1) + "/" + page.pages() + ")" : "");
        g.drawString(font, label, 42, 7, 0xFF808080, false);
    }
}
