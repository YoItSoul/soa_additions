package com.soul.soa_additions.tr.client.tooltip;

import com.mojang.datafixers.util.Either;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.tr.TrItems;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.item.MonocleAccess;
import com.soul.soa_additions.tr.knowledge.ClientKnownAspects;
import com.soul.soa_additions.tr.knowledge.ClientScannedTargets;
import com.soul.soa_additions.tr.network.MonocleScanRequestPacket;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thaumcraft 4 style: while the player owns an Arcane Monocle (anywhere in
 * inventory or worn in a curio slot), hovered items in any container reveal
 * their aspects in the tooltip — and unscanned items get a scan request fired
 * automatically (with the XP-pickup ding on success). Without the monocle,
 * tooltips stay vanilla.
 *
 * <p>Two filters guard against false triggers:
 * <ul>
 *   <li><b>JEI / REI / EMI ghost panels</b> never produce slots in
 *       {@code containerMenu.slots} and pass distinct ItemStack instances —
 *       {@link #isStackInRealSlot} catches that.</li>
 *   <li><b>Per-session debounce</b> via {@link #SCAN_REQUESTED} — once we've
 *       fired a scan packet for an item id during this owner-session, we
 *       don't re-fire on every render frame while the cursor lingers.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AspectTooltipHandler {

    private static final Set<ResourceLocation> SCAN_REQUESTED = new HashSet<>();
    private static boolean wasOwning = false;

    private AspectTooltipHandler() {}

    @SubscribeEvent
    public static void onGather(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean owning = MonocleAccess.hasMonocle(mc.player, TrItems.ARCANE_MONOCLE.get());
        if (!owning && wasOwning) SCAN_REQUESTED.clear();
        wasOwning = owning;
        if (!owning) return;

        if (stack.getItem() == TrItems.ARCANE_MONOCLE.get()) return;

        // JEI/REI/EMI ghost panels render with their own ItemStack instances
        // that are NOT held by any container Slot. Block those — without this
        // gate, ownership of the monocle would let the player sweep-scan
        // every item in the game by waving over JEI.
        if (!isStackInRealSlot(mc.player, stack)) return;

        // TC4-style: every hovered item gets scanned, regardless of whether
        // we have aspect data for it on file. The ding plays even for empty-
        // aspect items so the player gets feedback that the scan happened.
        // Aspect data we don't have means no tooltip line — the scan is
        // recorded, the future tooltip is just bare.
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null && !hasBeenScanned(stack) && SCAN_REQUESTED.add(itemId)) {
            TrNetworking.CHANNEL.sendToServer(MonocleScanRequestPacket.item(itemId));
        }

        // Render aspect line only when we have data AND it's been scanned AND
        // the player knows at least one aspect from the composition. All three
        // conditions failing leaves the tooltip vanilla-clean.
        List<AspectStack> all = AspectMap.forItem(stack);
        if (all.isEmpty()) return;
        if (!hasBeenScanned(stack)) return;

        List<AspectStack> known = AspectMap.filter(all, ClientKnownAspects::has);
        if (known.isEmpty()) return;
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        elements.add(Either.right(new AspectTooltipComponent(known)));
    }

    /** IdentityHashSet of slot stacks for the current containerMenu, rebuilt
     *  only when the menu instance changes. Tooltip events fire 60+ times/sec
     *  for the same hovered slot — without caching we'd walk all 46+ slots
     *  per frame, comparing references. With caching, it's a single
     *  identity-set contains check. The menu changes when the player opens
     *  a different container, which is several seconds between events at most. */
    private static java.util.Set<ItemStack> slotStackCache = null;
    private static net.minecraft.world.inventory.AbstractContainerMenu cachedMenu = null;

    private static boolean isStackInRealSlot(LocalPlayer player, ItemStack stack) {
        var menu = player.containerMenu;
        if (menu == null) return false;
        if (menu != cachedMenu) {
            cachedMenu = menu;
            // IdentityHashMap-backed set for == comparison rather than .equals.
            slotStackCache = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            for (Slot slot : menu.slots) {
                slotStackCache.add(slot.getItem());
            }
        }
        // The cached set is "stale" within a menu instance — when slot
        // contents change (player picks up an item, etc.), the slot's
        // internal stack reference is replaced. Add a fast-path for the
        // current stack identity, falling back to a fresh menu walk if miss.
        if (slotStackCache.contains(stack)) return true;
        // Slow path: stack rotated in this menu. Walk + refresh.
        for (Slot slot : menu.slots) {
            ItemStack s = slot.getItem();
            if (s == stack) {
                slotStackCache.add(s);
                return true;
            }
        }
        return false;
    }

    private static boolean hasBeenScanned(ItemStack stack) {
        if (ClientScannedTargets.has(stack)) return true;
        if (stack.getItem() instanceof BlockItem bi) {
            return ClientScannedTargets.has(bi.getBlock());
        }
        return false;
    }
}
