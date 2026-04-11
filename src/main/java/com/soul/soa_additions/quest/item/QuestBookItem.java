package com.soul.soa_additions.quest.item;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Right-click to open the quest book. The client dispatches the screen via
 * {@link com.soul.soa_additions.quest.client.QuestBookClient#openBook()};
 * the server-side branch is a no-op — no container is opened, because the
 * book UI is pure client state driven by {@link
 * com.soul.soa_additions.quest.net.ClientQuestState}.
 *
 * <p>Stack size is 1 and the item is non-cosmetic so players can't
 * accidentally lose their book to an auto-feed hopper.</p>
 */
public final class QuestBookItem extends Item {

    public QuestBookItem() {
        super(new Properties().stacksTo(1));
    }

    /** Always render the enchanted glint so the book reads as "magical" in
     *  inventory without an actual enchantment NBT (which would let players
     *  disenchant it at a grindstone). */
    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
            com.soul.soa_additions.quest.client.QuestBookClient.openBook();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
