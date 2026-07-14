package com.soul.soa_additions.oresight;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ModNetworking;
import com.soul.soa_additions.potion.SoaBrewingPotions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forge event hooks for the ore-sight system.
 *
 * <ul>
 *   <li>{@link #onRegisterCapabilities} — register the {@link OreSightTracker}
 *       capability with Forge.</li>
 *   <li>{@link #onAttachCapabilities} — attach a fresh tracker to every Player.</li>
 *   <li>{@link #onPlayerClone} — copy tracker NBT across death/dim-change.</li>
 *   <li>{@link #onItemUseFinish} — when a player drinks an Ore Sight potion,
 *       read the NBT and add the ore to their tracker, then sync.</li>
 *   <li>{@link #onPlayerTick} — periodic prune of expired entries (server-side).</li>
 * </ul>
 *
 * <p>The tooltip line that previously appended "Tracking: &lt;Block&gt;" was
 * removed when the dynamic per-block item name was introduced — the block
 * already shows in the item's display name, so a duplicate tooltip line was
 * just visual noise.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class OreSightEvents {

    private OreSightEvents() {}

    // ──────────────────── mod-bus events ────────────────────

    @Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(OreSightTracker.class);
        }
    }

    // ──────────────────── forge-bus events ────────────────────

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(OreSightTracker.CAP_ID, new OreSightTracker.Provider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        OreSightTracker old = event.getOriginal().getCapability(OreSightTracker.CAP).orElse(null);
        OreSightTracker fresh = event.getEntity().getCapability(OreSightTracker.CAP).orElse(null);
        if (old != null && fresh != null) {
            fresh.deserializeNBT(old.serializeNBT());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sync(sp);
        }
    }

    /** When the player finishes drinking an Ore Sight potion, register the
     *  ore (or master flag) on their tracker. The MobEffect itself is applied
     *  by vanilla {@code PotionItem.finishUsingItem} via the Potion's effect
     *  list. */
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();
        // Accept both vanilla potion items (defensive) and our custom ore-sight items.
        boolean isPotion = stack.getItem() == Items.POTION
                || stack.getItem() == Items.SPLASH_POTION
                || stack.getItem() == Items.LINGERING_POTION
                || OreSightBrewing.isOreSightPotionItem(stack.getItem());
        if (!isPotion) return;

        Potion potion = PotionUtils.getPotion(stack);
        Potion baseSight   = SoaBrewingPotions.ORE_SIGHT.get();
        Potion longSight   = SoaBrewingPotions.LONG_ORE_SIGHT.get();
        Potion masterSight = SoaBrewingPotions.MASTER_ORE_SIGHT.get();
        Potion longMaster  = SoaBrewingPotions.LONG_MASTER_ORE_SIGHT.get();

        boolean isMaster = potion == masterSight || potion == longMaster;
        boolean isRegular = potion == baseSight || potion == longSight;
        if (!isMaster && !isRegular) return;

        boolean isLong = potion == longSight || potion == longMaster;
        int durationTicks = isLong ? 8 * 60 * 20 : 3 * 60 * 20;
        long expiry = player.level().getGameTime() + durationTicks;

        OreSightTracker tracker = OreSightTracker.get(player).orElse(null);
        if (tracker == null) return;

        boolean changed;
        if (isMaster) {
            changed = tracker.setMaster(expiry);
        } else {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains(OreSightBrewing.NBT_BLOCK)) return;
            Block block = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation(tag.getString(OreSightBrewing.NBT_BLOCK)));
            if (block == null) return;
            changed = tracker.addOrRefresh(block, expiry);
        }
        if (changed) sync(player);
    }

    /** Prune expired tracker entries every 20 ticks (1 second). The HUD MobEffect
     *  is left to vanilla's drink-time application — vanilla's
     *  {@code addEffect} already takes the longer of (existing remaining) vs
     *  (new duration), so stacking ore-sights yields the right icon timer
     *  without us re-applying every tick (which would also fight the potion's
     *  default particle visibility). */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if ((player.level().getGameTime() % 20L) != 0L) return;
        OreSightTracker tracker = OreSightTracker.get(player).orElse(null);
        if (tracker == null) return;
        if (tracker.pruneExpired(player.level().getGameTime())) {
            sync(player);
        }
    }

    /** Send the player's current tracker state to themselves only. */
    public static void sync(ServerPlayer player) {
        OreSightTracker tracker = OreSightTracker.get(player).orElse(null);
        if (tracker == null) return;
        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OreSightSyncPacket(tracker.snapshot(), tracker.masterExpiry()));
    }
}
