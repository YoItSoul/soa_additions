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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for inventory-changing events and tells the GreedyBag
 * to absorb any newly-restricted items.  Also intercepts ground
 * pickups so that restricted items go straight into the bag
 * instead of being rejected.
 *
 * <p>Registered to the Forge event bus by {@link
 * com.soul.soa_additions.curios.CuriosIntegration} when Curios is present.
 * NOT annotated with {@code @Mod.EventBusSubscriber} so Forge's class scanner
 * doesn't load it (and its ICurio-dependent siblings) when Curios is absent.
 */
public final class GreedyBagEvents {

    private GreedyBagEvents() {}

    /**
     * Last-seen {@link Inventory#getTimesChanged()} per player, so the
     * per-tick absorber only does the full inventory + Curios scan when the
     * inventory actually changed. Idle ticks cost one map lookup + int compare.
     */
    private static final Map<UUID, Integer> LAST_INV_CHANGE = new ConcurrentHashMap<>();

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

        // Only scan when the inventory actually changed since our last scan;
        // getTimesChanged() bumps on every setChanged() (pickup, craft, /give,
        // shift-click, ...), so this still beats ItemStages to any new item.
        int changes = sp.getInventory().getTimesChanged();
        Integer last = LAST_INV_CHANGE.get(sp.getUUID());
        if (last != null && last == changes) return;
        LAST_INV_CHANGE.put(sp.getUUID(), changes);

        handleChange(sp);
    }

    /**
     * The tick absorber above gates on {@link Inventory#getTimesChanged()},
     * which Curios does not bump — a stack that only ever moves between Curios
     * slots would otherwise sit there unabsorbed until something happened to
     * touch the vanilla inventory. This fires whenever a Curios slot's
     * contents change, so equipping a restricted trinket is caught at once.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            handleChange(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        handleChange(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_INV_CHANGE.remove(event.getEntity().getUUID());
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
