package com.soul.soa_additions.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * A reward ticket that hands the player a Resourceful Lootbags bag of the
 * corresponding Souls of Avarice tier when right-clicked. The lootbag itself
 * is {@code lootbags:loot_bag} with an {@code NBT: {Loot:"<loot_id>"}} tag
 * that points at a recipe in
 * {@code kubejs/data/soa_additions/recipes/loot_crate_*.json}.
 *
 * <p>Built on top of {@link StageItem} so the existing foil + tooltip
 * behaviour carries over. The only new piece is the {@code use} override
 * that consumes one ticket and spawns the configured lootbag. If Resourceful
 * Lootbags is missing from the pack the ticket falls back to being inert
 * (tooltip only) — it logs once and returns {@code PASS}.</p>
 */
public class RewardTicketItem extends StageItem {

    private static final ResourceLocation LOOT_BAG_ID = new ResourceLocation("lootbags", "loot_bag");

    private final ResourceLocation lootTableId;

    public RewardTicketItem(Properties props, boolean foil, ResourceLocation lootTableId, String... tooltip) {
        super(props, foil, tooltip);
        this.lootTableId = lootTableId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        // Client predicts; server does the work. Returning SUCCESS on the
        // client keeps the swing animation but the authoritative state change
        // happens on the server branch below.
        if (level.isClientSide) {
            return InteractionResultHolder.success(held);
        }

        Item lootBag = ForgeRegistries.ITEMS.getValue(LOOT_BAG_ID);
        if (lootBag == null) {
            // Lootbags isn't a 1.20 mod (yet); fall back to rolling our own
            // loot table at the player. This keeps the ticket useful on packs
            // without Lootbags and mirrors the GreedyCraft crate contents.
            if (level instanceof ServerLevel sl && player instanceof ServerPlayer sp) {
                RightClickActions.rollLootTableAt(lootTableId).apply(sl, sp, held);
                held.shrink(1);
                return InteractionResultHolder.consume(held);
            }
            return InteractionResultHolder.pass(held);
        }

        ItemStack bag = new ItemStack(lootBag);
        CompoundTag tag = bag.getOrCreateTag();
        tag.putString("Loot", lootTableId.toString());
        // Lootbags checks both the root "Loot" tag and a nested tag key; set
        // both so the bag resolves on either code path the mod takes.
        CompoundTag lootbagsSub = tag.getCompound("lootbags");
        lootbagsSub.putString("Loot", lootTableId.toString());
        tag.put("lootbags", lootbagsSub);

        // Copy the ticket's display name onto the bag so the player sees
        // "Common Loot Crate" in the popup rather than a generic lootbag name.
        if (held.hasCustomHoverName()) {
            bag.setHoverName(held.getHoverName());
        }

        held.shrink(1);

        if (!player.getInventory().add(bag)) {
            player.drop(bag, false);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);

        return InteractionResultHolder.consume(held);
    }

    /** Exposed so JEI / tooltips can show which crate this ticket pulls from. */
    public ResourceLocation lootTableId() { return lootTableId; }

    /** Helper to read the "Loot" tag from a Lootbags bag if we ever need it
     *  for cross-checking — handles both top-level and nested forms. */
    public static String readLootTag(ItemStack bag) {
        if (!bag.hasTag()) return "";
        CompoundTag tag = bag.getTag();
        if (tag.contains("Loot", Tag.TAG_STRING)) return tag.getString("Loot");
        if (tag.contains("lootbags", Tag.TAG_COMPOUND)) {
            return tag.getCompound("lootbags").getString("Loot");
        }
        return "";
    }
}
