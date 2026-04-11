package com.soul.soa_additions.quest.events;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.progress.ProgressService;
import com.soul.soa_additions.quest.task.AdvancementTask;
import com.soul.soa_additions.quest.task.BreedTask;
import com.soul.soa_additions.quest.task.CraftTask;
import com.soul.soa_additions.quest.task.DimensionTask;
import com.soul.soa_additions.quest.task.KillTask;
import com.soul.soa_additions.quest.task.MineTask;
import com.soul.soa_additions.quest.task.PlaceTask;
import com.soul.soa_additions.quest.task.TameTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Maps Forge events to {@link ProgressService} calls, one event type per task
 * type. Each handler picks the task type it cares about via predicate so the
 * progress service doesn't need to know anything about the specific event.
 *
 * <p>The dimension check runs on player tick rather than a dedicated event
 * because Forge doesn't expose a reliable cross-dimension change event that
 * fires on both portal and command teleport. Throttled to once per second
 * per player so it's not a hot-path cost.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class QuestEventBridge {

    private QuestEventBridge() {}

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());

        ProgressService.apply(player, 1, KillTask.TYPE, task -> {
            KillTask kt = (KillTask) task;
            return kt.entity().equals(entityId);
        });
    }

    // Item tasks are now handled by InventoryItemPoller below — it scans the
    // player's full inventory every 10 ticks, which catches ground pickups,
    // crafting, creative menu, /give, container transfers, trading, and
    // every other source of items uniformly. The old ItemPickupEvent and
    // ItemCraftedEvent handlers only covered two of those paths and silently
    // missed the rest (crafting, creative menu, container pulls).

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getCrafting();
        if (stack.isEmpty()) return;
        int count = stack.getCount();
        final ItemStack crafted = stack;

        ProgressService.apply(player, count, CraftTask.TYPE, task -> {
            CraftTask ct = (CraftTask) task;
            return ct.matches(crafted);
        });
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());

        ProgressService.apply(player, 1, PlaceTask.TYPE, task -> {
            PlaceTask pt = (PlaceTask) task;
            return pt.block().equals(blockId);
        });
    }

    @SubscribeEvent
    public static void onTame(net.minecraftforge.event.entity.living.AnimalTameEvent event) {
        if (!(event.getTamer() instanceof ServerPlayer player)) return;
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getAnimal().getType());

        ProgressService.apply(player, 1, TameTask.TYPE, task -> {
            TameTask tt = (TameTask) task;
            return tt.entity().equals(entityId);
        });
    }

    @SubscribeEvent
    public static void onBreed(net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof ServerPlayer player)) return;
        // Parent type is authoritative — BabyEntitySpawnEvent's child may be
        // null for some mods, but parentA is always set when the vanilla
        // breeding flow fires this event.
        if (event.getParentA() == null) return;
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getParentA().getType());

        ProgressService.apply(player, 1, BreedTask.TYPE, task -> {
            BreedTask bt = (BreedTask) task;
            return bt.entity().equals(entityId);
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        BlockState state = event.getState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        ProgressService.apply(player, 1, MineTask.TYPE, task -> {
            MineTask mt = (MineTask) task;
            return mt.block().equals(blockId);
        });
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation advId = event.getAdvancement().getId();

        ProgressService.apply(player, 1, AdvancementTask.TYPE, task -> {
            AdvancementTask at = (AdvancementTask) task;
            return at.advancement().equals(advId);
        });
    }

    // Unified tick poller: dimension check every 20 ticks, inventory item
    // scan every 10 ticks, stat poll every 100 ticks. Merged into a single
    // handler to avoid paying the phase/instanceof gate cost twice per tick.
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        long tick = player.server.getTickCount();
        // Stagger per-player so two players online don't both poll on the
        // same tick. Cheap, deterministic, no shared state.
        int offset = (player.getUUID().hashCode() & 0x7fffffff);
        long staggered = tick + offset;

        if (staggered % 10 == 0) {
            InventoryItemPoller.poll(player);
        }

        if (staggered % 20 == 0) {
            ResourceLocation dimId = player.level().dimension().location();
            ProgressService.apply(player, 1, DimensionTask.TYPE, task -> {
                DimensionTask dt = (DimensionTask) task;
                return dt.dimension().equals(dimId);
            });
            ObserveTaskPoller.poll(player);
        }

        if (staggered % 100 == 0) {
            StatTaskPoller.poll(player);
        }
    }
}
