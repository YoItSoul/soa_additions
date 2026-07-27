package com.soul.soa_additions.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

/**
 * A {@link StageItem} that opens a Patchouli book on use. GC bound its lore
 * books (The Elysian Project, Introduction to GreedyCraft) to pack-level
 * Patchouli books via the omnipedia; the 1.20 port registers those books
 * data-driven under soa_additions and opens them straight from the lore item.
 */
public class BookOpenItem extends StageItem {

    private final ResourceLocation book;

    public BookOpenItem(Properties props, boolean foil, String bookId, String... tooltip) {
        super(props, foil, tooltip);
        this.book = ResourceLocation.fromNamespaceAndPath("soa_additions", bookId);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            PatchouliAPI.get().openBookGUI(sp, book);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
