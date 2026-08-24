package com.soul.soa_additions.smithery;

import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolCompositions;
import com.soul.smithery.api.modifier.ModifierEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Soulbound: gear carrying the trait stays with its owner through death.
 *
 * <p>Ported from Tinkers' Construct 1.12's {@code ModSoulbound} — "Tool remains in your inventory
 * after death", applied at the anvil with a Nether Star, and consumed when it saves the item.</p>
 *
 * <p>The work happens in {@link LivingDeathEvent} rather than on the drop event, because the
 * player's inventory is emptied inside {@code Player.die()} right after that event fires: pulling
 * the stacks out here means they are never dropped in the first place, so nothing can despawn in
 * the window before the player respawns. Held stacks go into a stash keyed by player UUID and come
 * back on respawn.</p>
 *
 * <p>The stash is deliberately in-memory. A server that crashes between a player's death and their
 * respawn loses the stashed items — but a disk-backed stash would need its own save data and a
 * migration path, and the crash window is a few seconds wide. If that trade stops being acceptable,
 * the fix is to write the stash into the player's persistent NBT
 * ({@code player.getPersistentData().get(Player.PERSISTED_NBT_TAG)}), which survives death by design.</p>
 *
 * <p>Registered to the Forge event bus by {@link SmitheryIntegration#init} when Smithery is
 * present. NOT annotated with {@code @Mod.EventBusSubscriber}: the handler bodies dereference
 * {@code com.soul.smithery} types, and Forge's scanner would subscribe them unconditionally — the
 * first block break without Smithery installed would then be a NoClassDefFoundError inside a
 * listener that nothing catches.</p>
 */
public final class SoulboundEvents {

    private SoulboundEvents() {}

    private static final Map<UUID, List<ItemStack>> STASH = new HashMap<>();

    /**
     * The stash belongs to one server run. Static state outlives the integrated server, so
     * quitting to the title screen after dying and loading another world used to hand the first
     * world's gear to the same UUID on respawn.
     */
    @SubscribeEvent
    public static void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        STASH.clear();
    }

    /**
     * Pulls soulbound gear out of the inventory before vanilla can drop it.
     *
     * <p>HIGHEST priority so the stacks are gone before any other mod's death handling counts,
     * copies or deletes them.</p>
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        List<ItemStack> kept = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (List<ItemStack> compartment : List.of(inv.items, inv.armor, inv.offhand)) {
            for (int i = 0; i < compartment.size(); i++) {
                ItemStack stack = compartment.get(i);
                if (stack.isEmpty()) continue;
                if (!SmitheryTraitEvents.hasTrait(stack, SoaSmitheryModifiers.SOULBOUND)
                        && !SmitheryTraitEvents.hasTrait(stack, SoaSmitheryModifiers.SOULBOUND_ARMOR)) {
                    continue;
                }
                kept.add(consume(stack));
                compartment.set(i, ItemStack.EMPTY);
            }
        }
        if (!kept.isEmpty()) {
            STASH.put(player.getUUID(), kept);
        }
    }

    /** Hands the stashed gear back to the freshly respawned player. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        List<ItemStack> kept = STASH.remove(event.getEntity().getUUID());
        if (kept == null) return;
        for (ItemStack stack : kept) {
            if (!event.getEntity().getInventory().add(stack)) {
                event.getEntity().drop(stack, false);   // full inventory: at their feet, not gone
            }
        }
    }

    /**
     * Spends the anvil-applied Soulbound charge, matching 1.12's "single use".
     *
     * <p>A Soulbound granted by a material trait cannot be spent — it is part of what the material
     * is — so that case keeps working every death, which is also how the 1.12 trait behaved when a
     * material carried it rather than an anvil application.</p>
     */
    private static ItemStack consume(ItemStack stack) {
        List<ModifierEffect> applied = new ArrayList<>(SmitheryToolData.getAppliedModifiers(stack));
        boolean spent = applied.removeIf(e ->
                SoaSmitheryModifiers.SOULBOUND.equals(e.modifierId())
                        || SoaSmitheryModifiers.SOULBOUND_ARMOR.equals(e.modifierId()));
        if (spent) {
            SmitheryToolData.setAppliedModifiers(stack, applied);
            ToolComposition comp = SmitheryToolData.getComposition(stack);
            if (comp != null) {
                ToolCompositions.apply(stack, comp);
            }
        }
        return stack;
    }
}
