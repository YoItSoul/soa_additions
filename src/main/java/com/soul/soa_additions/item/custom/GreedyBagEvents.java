package com.soul.soa_additions.item.custom;

import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Listens for inventory-changing events and tells the GreedyBag
 * to absorb any newly-restricted items.  Also intercepts ground
 * pickups so that restricted items go straight into the bag
 * instead of being rejected.
 */
@Mod.EventBusSubscriber(modid = "soa_additions", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GreedyBagEvents {

    private GreedyBagEvents() {}

    /**
     * HIGH priority so we run before ItemStages can reject the pickup.
     * If the item is restricted and the player has a bag, absorb it
     * directly and cancel the vanilla pickup (so ItemStages never sees it).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;

        ItemEntity itemEntity = event.getItem();
        ItemStack ground = itemEntity.getItem();
        if (ground.isEmpty()) return;

        Restriction restriction = RestrictionManager.INSTANCE
                .getRestriction(sp, ground);
        if (restriction != null && restriction.isRestricted(ground)) {
            ItemStack bag = findBag(sp);
            if (!bag.isEmpty()) {
                ((GreedyBagItem) bag.getItem()).storeItem(bag, ground.copy());
                itemEntity.discard();
                event.setCanceled(true);
            }
        }
    }

    /**
     * Per-tick absorber at HIGHEST priority so we run BEFORE ItemStages'
     * own NORMAL-priority {@code onPlayerTick}, which drops restricted
     * inventory items on the ground via {@code player.drop()} (40-tick
     * pickup delay — easy to walk away from or have despawn). By absorbing
     * here first, ItemStages never sees anything to drop.
     *
     * <p>Catches every entry path our event handlers miss: crafting results,
     * /give, container shift-click, mob drops via auto-pickup, etc.</p>
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player instanceof ServerPlayer sp)) return;
        handleChange(sp);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        handleChange(event.getEntity());
    }

    private static void handleChange(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        ItemStack bag = findBag(sp);
        if (!bag.isEmpty()) {
            ((GreedyBagItem) bag.getItem()).absorbRestrictedItems(sp, bag);
        }
    }

    /** Find a GreedyBag in the player's inventory or Curios slots. */
    private static ItemStack findBag(ServerPlayer player) {
        // Check regular inventory
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.items.get(i);
            if (stack.getItem() instanceof GreedyBagItem) {
                return stack;
            }
        }

        // Check Curios slots
        try {
            var optional = CuriosApi.getCuriosInventory(player);
            var result = new ItemStack[]{ ItemStack.EMPTY };
            optional.ifPresent(handler ->
                handler.findFirstCurio(s -> s.getItem() instanceof GreedyBagItem)
                       .ifPresent(slot -> result[0] = slot.stack())
            );
            return result[0];
        } catch (NoClassDefFoundError e) {
            return ItemStack.EMPTY;
        }
    }
}
