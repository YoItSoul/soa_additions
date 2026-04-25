package com.soul.soa_additions.tr.knowledge;

import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.network.ScannedTargetsSyncPacket;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Per-player record of which blocks and items the player has scanned (i.e.
 * had their aspect composition revealed). Drives the Monocle HUD overlay,
 * tooltip aspect lines, and the research-points economy: showing a block's
 * aspects requires the player to have scanned that block first.
 *
 * <p>Read path is intentionally allocation-free for the per-frame check —
 * a single {@code BuiltInRegistries.BLOCK.getKey(block)} call (registries
 * cache the ResourceLocation) followed by one HashSet lookup. Holding the
 * data in a HashSet rather than an ordered set is a deliberate trade: we
 * lose iteration determinism but gain O(1) lookups, which matters when
 * the HUD calls this every render tick.
 *
 * <p>Blocks and items live in separate sets because their registries are
 * distinct and identifiers can collide (e.g. {@code minecraft:stone} exists
 * as both). Entities will get a third set when scanning living things lands.
 */
public final class ScannedTargets {

    public static final Capability<ScannedTargets.Data> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation CAP_ID =
            new ResourceLocation(ThaumicRemnants.MODID, "scanned_targets");

    private ScannedTargets() {}

    // ---------------- Data + capability provider ----------------

    public static final class Data {
        private final Set<ResourceLocation> blocks   = new HashSet<>();
        private final Set<ResourceLocation> items    = new HashSet<>();
        private final Set<ResourceLocation> entities = new HashSet<>();

        // ---- Fast read path ----

        public boolean hasBlock(ResourceLocation id)  { return blocks.contains(id); }
        public boolean hasItem(ResourceLocation id)   { return items.contains(id); }
        public boolean hasEntity(ResourceLocation id) { return entities.contains(id); }

        public boolean hasBlock(Block block) {
            if (block == null) return false;
            return blocks.contains(BuiltInRegistries.BLOCK.getKey(block));
        }
        public boolean hasItem(Item item) {
            if (item == null) return false;
            return items.contains(BuiltInRegistries.ITEM.getKey(item));
        }
        public boolean hasItem(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;
            return items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        public boolean hasEntity(EntityType<?> type) {
            if (type == null) return false;
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
            return key != null && entities.contains(key);
        }

        public int blockCount()  { return blocks.size(); }
        public int itemCount()   { return items.size(); }
        public int entityCount() { return entities.size(); }

        public Set<ResourceLocation> blockSnapshot()  { return Collections.unmodifiableSet(new HashSet<>(blocks)); }
        public Set<ResourceLocation> itemSnapshot()   { return Collections.unmodifiableSet(new HashSet<>(items)); }
        public Set<ResourceLocation> entitySnapshot() { return Collections.unmodifiableSet(new HashSet<>(entities)); }

        // ---- Mutators (package-private; go through the outer class API) ----

        boolean addBlock(ResourceLocation id)    { return blocks.add(id); }
        boolean addItem(ResourceLocation id)     { return items.add(id); }
        boolean addEntity(ResourceLocation id)   { return entities.add(id); }
        boolean removeBlock(ResourceLocation id) { return blocks.remove(id); }
        boolean removeItem(ResourceLocation id)  { return items.remove(id); }
        boolean removeEntity(ResourceLocation id){ return entities.remove(id); }
        void clearBlocks()   { blocks.clear(); }
        void clearItems()    { items.clear(); }
        void clearEntities() { entities.clear(); }
        void clearAll() { blocks.clear(); items.clear(); entities.clear(); }

        // ---- NBT ----

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            ListTag bl = new ListTag();
            for (ResourceLocation id : blocks) bl.add(StringTag.valueOf(id.toString()));
            tag.put("blocks", bl);
            ListTag il = new ListTag();
            for (ResourceLocation id : items) il.add(StringTag.valueOf(id.toString()));
            tag.put("items", il);
            ListTag el = new ListTag();
            for (ResourceLocation id : entities) el.add(StringTag.valueOf(id.toString()));
            tag.put("entities", el);
            return tag;
        }

        public void read(CompoundTag tag) {
            blocks.clear();
            items.clear();
            entities.clear();
            ListTag bl = tag.getList("blocks", Tag.TAG_STRING);
            for (int i = 0; i < bl.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(bl.getString(i));
                if (rl != null) blocks.add(rl);
            }
            ListTag il = tag.getList("items", Tag.TAG_STRING);
            for (int i = 0; i < il.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(il.getString(i));
                if (rl != null) items.add(rl);
            }
            ListTag el = tag.getList("entities", Tag.TAG_STRING);
            for (int i = 0; i < el.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(el.getString(i));
                if (rl != null) entities.add(rl);
            }
        }
    }

    static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final Data data = new Data();
        private final LazyOptional<Data> opt = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override public CompoundTag serializeNBT() { return data.write(); }
        @Override public void deserializeNBT(CompoundTag tag) { data.read(tag); }
    }

    // ---------------- Public API ----------------

    private static final Data EMPTY = new Data();

    public static Data of(@Nullable Player player) {
        if (player == null) return EMPTY;
        return player.getCapability(CAP).orElse(EMPTY);
    }

    /** The shared sentinel returned by {@link #of} when no capability is
     *  attached. Identity-comparable: callers that need to distinguish
     *  "player has no scans yet" from "player has no capability at all"
     *  use {@code data == ScannedTargets.empty()} rather than checking
     *  {@code data.size() == 0}. */
    public static Data empty() { return EMPTY; }

    /** Server-side: mark a block as scanned. Returns true if newly added.
     *  Triggers a sync packet so the client HUD updates immediately.
     *  <p>Side effect: also grants the player {@link KnownAspects} for every
     *  aspect in the target's composition — Thaumcraft 4 semantics where
     *  scanning is the primary discovery mechanism. Without this, scanning
     *  would gate the tooltip but the inner known-aspects filter would still
     *  hide everything for any item with compound aspects. */
    public static boolean scanBlock(ServerPlayer player, Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return scanBlock(player, id);
    }

    public static boolean scanBlock(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) {
            ThaumicRemnants.LOG.info(
                    "[monocle/srv] scanBlock({}) ABORTED — ScannedTargets cap not attached for {}",
                    id, player.getName().getString());
            return false;
        }
        boolean added = d.addBlock(id);
        if (added) syncTo(player);
        // Run the chained-discovery cascade UNCONDITIONALLY — not just when
        // newly added. If cascade silently failed on the first scan (cap not
        // attached, etc.), every subsequent scan re-attempts it, eventually
        // catching up the player's known set. Cascade is idempotent: it only
        // grants aspects that are now discoverable, and only sends a sync if
        // something actually changed.
        KnownAspects.cascadeFromScans(player);
        return added;
    }

    public static boolean scanItem(ServerPlayer player, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return scanItem(player, id);
    }

    public static boolean scanItem(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) {
            ThaumicRemnants.LOG.info(
                    "[monocle/srv] scanItem({}) ABORTED — ScannedTargets cap not attached for {}",
                    id, player.getName().getString());
            return false;
        }
        boolean added = d.addItem(id);
        if (added) syncTo(player);
        // Cascade unconditionally — see scanBlock note.
        KnownAspects.cascadeFromScans(player);
        return added;
    }

    public static boolean unscanBlock(ServerPlayer player, Block block) {
        return unscanBlock(player, BuiltInRegistries.BLOCK.getKey(block));
    }
    public static boolean unscanBlock(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean removed = d.removeBlock(id);
        if (removed) syncTo(player);
        return removed;
    }

    public static boolean unscanItem(ServerPlayer player, Item item) {
        return unscanItem(player, BuiltInRegistries.ITEM.getKey(item));
    }
    public static boolean unscanItem(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean removed = d.removeItem(id);
        if (removed) syncTo(player);
        return removed;
    }

    public static boolean scanEntity(ServerPlayer player, EntityType<?> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return false;
        return scanEntity(player, id);
    }

    public static boolean scanEntity(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean added = d.addEntity(id);
        if (added) syncTo(player);
        KnownAspects.cascadeFromScans(player);
        return added;
    }

    public static boolean unscanEntity(ServerPlayer player, EntityType<?> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return false;
        return unscanEntity(player, id);
    }
    public static boolean unscanEntity(ServerPlayer player, ResourceLocation id) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean removed = d.removeEntity(id);
        if (removed) syncTo(player);
        return removed;
    }

    /** Mark every registered block AND item as scanned. Used by
     *  {@code /tr scan all} for testing. Reports total newly added. */
    public static int scanAll(ServerPlayer player) {
        Data d = of(player);
        if (d == EMPTY) return 0;
        int before = d.blockCount() + d.itemCount();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) d.addBlock(id);
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet())  d.addItem(id);
        int added = (d.blockCount() + d.itemCount()) - before;
        if (added > 0) syncTo(player);
        return added;
    }

    public static int unscanAll(ServerPlayer player) {
        Data d = of(player);
        if (d == EMPTY) return 0;
        int n = d.blockCount() + d.itemCount();
        d.clearAll();
        if (n > 0) syncTo(player);
        return n;
    }

    public static void syncTo(ServerPlayer player) {
        Data d = of(player);
        TrNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ScannedTargetsSyncPacket(d.blockSnapshot(), d.itemSnapshot(), d.entitySnapshot()));
    }

    // ---------------- Registration glue ----------------

    public static void register(IEventBus modBus) {
        modBus.addListener(ScannedTargets::onRegisterCaps);
        // AttachCapabilitiesEvent<Entity> is a GENERIC event — plain
        // addListener silently registers but never fires for it; only
        // addGenericListener with the matching type filter actually binds.
        // (This is the bug that took us many iterations to find — the
        // annotation @Mod.EventBusSubscriber inspects the method signature
        // and routes to addGenericListener under the hood, which is why
        // SOMETIMES the annotation path worked.)
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addGenericListener(
                net.minecraft.world.entity.Entity.class, ScannedTargets::onAttachCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ScannedTargets::onCloneCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ScannedTargets::onLoginCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ScannedTargets::onRespawnCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ScannedTargets::onDimChangeCap);
    }

    /** Direct subscriber — see register(). */
    public static void onAttachCap(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        if (event.getCapabilities().containsKey(CAP_ID)) return;
        event.addCapability(CAP_ID, new Provider());
        ThaumicRemnants.LOG.info("[monocle/srv] ScannedTargets cap attached to {}",
                event.getObject().getUUID());
    }

    public static void onCloneCap(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player oldP = event.getOriginal();
        Player newP = event.getEntity();
        oldP.reviveCaps();
        Data old = of(oldP);
        Data fresh = of(newP);
        if (old != EMPTY && fresh != EMPTY) {
            fresh.read(old.write());
        }
        oldP.invalidateCaps();
    }

    public static void onLoginCap(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    public static void onRespawnCap(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    public static void onDimChangeCap(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    private static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(Data.class);
    }

    /** Auto-subscribed to the Forge bus by Forge's annotation scan, instead
     *  of via the manual {@code MinecraftForge.EVENT_BUS.register(Class)}
     *  call we use elsewhere — that path silently dropped this class's
     *  listeners for reasons we never identified, leaving the cap unattached
     *  on every player. The auto-subscribe path is the same one
     *  {@link com.soul.soa_additions.tr.command.TrCommands} and other Forge
     *  handlers use successfully throughout the codebase. */
    @Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onAttach(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (!(event.getObject() instanceof Player)) return;
            // Idempotent — same FakePlayer/Moonlight gotcha as KnownAspects.
            if (event.getCapabilities().containsKey(CAP_ID)) return;
            event.addCapability(CAP_ID, new Provider());
            // No player.getGameProfile() here — the gameProfile field is
            // assigned in Player's constructor BODY, but AttachCapabilitiesEvent
            // fires from Entity's constructor (a super-call) before that body
            // runs, so getGameProfile() returns null and an NPE here crashes
            // the server tick loop. Use UUID instead, which IS set early.
            ThaumicRemnants.LOG.debug("Attached scanned-targets cap to entity {}",
                    event.getObject().getUUID());
        }

        /** Persist scan history across death — same shape as KnownAspects.Events.onClone. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onClone(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                Player oldP = event.getOriginal();
                Player newP = event.getEntity();
                oldP.reviveCaps();
                Data old = of(oldP);
                Data fresh = of(newP);
                if (old != EMPTY && fresh != EMPTY) {
                    fresh.read(old.write());
                }
                oldP.invalidateCaps();
            }
        }

        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }

        @SubscribeEvent
        public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }

        @SubscribeEvent
        public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }
    }
}
