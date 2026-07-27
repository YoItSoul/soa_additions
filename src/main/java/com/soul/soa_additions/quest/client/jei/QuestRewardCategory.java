package com.soul.soa_additions.quest.client.jei;

import com.soul.soa_additions.SoaAdditions;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * "Quest Rewards" JEI category: one entry per quest that hands out items, so
 * recipe-lookup (R) on an item shows which quests grant it. Covers plain
 * {@code ItemReward}s plus items parsed out of {@code CommandReward}
 * {@code /give} commands (the loot-crate gives use those).
 */
public final class QuestRewardCategory implements IRecipeCategory<QuestRewardCategory.Display> {

    /** One quest's item rewards, pre-resolved for display. */
    public record Display(String questTitle, String chapterTitle, ItemStack questIcon,
                          List<ItemStack> rewards) {}

    public static final RecipeType<Display> TYPE =
            RecipeType.create(SoaAdditions.MODID, "quest_reward", Display.class);

    private static final int COLUMNS = 9;
    private static final int ROWS = 3;
    public static final int MAX_REWARDS = COLUMNS * ROWS;
    private static final int HEADER_H = 26;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public QuestRewardCategory(IGuiHelper helper, ItemStack iconStack) {
        this.background = helper.createBlankDrawable(COLUMNS * 18, HEADER_H + ROWS * 18);
        this.icon = helper.createDrawableItemStack(iconStack);
        this.slot = helper.getSlotDrawable();
    }

    @Override public RecipeType<Display> getRecipeType() { return TYPE; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public Component getTitle() {
        return Component.literal("Quest Rewards");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 4)
                .setBackground(slot, -1, -1)
                .addItemStack(display.questIcon());
        for (int i = 0; i < display.rewards().size() && i < MAX_REWARDS; i++) {
            int x = (i % COLUMNS) * 18 + 1;
            int y = HEADER_H + (i / COLUMNS) * 18 + 1;
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(slot, -1, -1)
                    .addItemStack(display.rewards().get(i));
        }
    }

    @Override
    public void draw(Display display, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
                     net.minecraft.client.gui.GuiGraphics g, double mouseX, double mouseY) {
        var font = net.minecraft.client.Minecraft.getInstance().font;
        // Quest title (keeps its § formatting), chapter beneath in gray.
        g.drawString(font, display.questTitle(), 22, 3, 0xFFFFFFFF, false);
        g.pose().pushPose();
        g.pose().translate(22f, 14f, 0f);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.drawString(font, "Chapter: " + display.chapterTitle(), 0, 0, 0xFF808080, false);
        g.pose().popPose();
    }
}
