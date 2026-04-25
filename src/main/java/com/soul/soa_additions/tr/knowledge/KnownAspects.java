package com.soul.soa_additions.tr.knowledge;

import com.soul.soa_additions.tr.ThaumicRemnants;
import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.Aspects;
import com.soul.soa_additions.tr.network.KnownAspectsSyncPacket;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-player record of which aspects the player has discovered. Backs every
 * "hide unknown aspect" UI element in Thaumic Remnants — JEI rune visibility,
 * future Monocle scan output, the eventual Thaumonomicon page gating.
 *
 * <p>Stored as a Forge capability on the player entity, serialised to NBT,
 * synced to client on login/respawn/dimension change so client-side
 * filters (JEI in particular) have authoritative state.
 *
 * <p>API: {@link #discover(ServerPlayer, Aspect)} server-side to grant an
 * aspect; client-side reads use {@link ClientKnownAspects}. Mutating from
 * the client is not supported — discovery is server-authoritative.
 */
public final class KnownAspects {

    public static final Capability<KnownAspects.Data> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation CAP_ID =
            new ResourceLocation(ThaumicRemnants.MODID, "known_aspects");

    private KnownAspects() {}

    // ---------------- Data + capability provider ----------------

    /** Backing instance held by the capability provider. */
    public static final class Data {
        // LinkedHashSet keeps a consistent order for /tr list output.
        private final Set<ResourceLocation> known = new LinkedHashSet<>();

        public boolean has(Aspect a) { return known.contains(a.id()); }
        public boolean has(ResourceLocation id) { return known.contains(id); }
        public Set<ResourceLocation> snapshot() { return Collections.unmodifiableSet(new LinkedHashSet<>(known)); }
        public int size() { return known.size(); }

        /** @return true if newly added */
        boolean add(ResourceLocation id) { return known.add(id); }
        boolean remove(ResourceLocation id) { return known.remove(id); }
        void clear() { known.clear(); }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (ResourceLocation id : known) list.add(StringTag.valueOf(id.toString()));
            tag.put("known", list);
            return tag;
        }

        public void read(CompoundTag tag) {
            known.clear();
            ListTag list = tag.getList("known", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
                if (rl != null) known.add(rl);
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

        void invalidate() { opt.invalidate(); }
    }

    // ---------------- Public API ----------------

    /** Read the player's known-aspects, if the capability is attached. Returns
     *  an empty {@link Data} for null or non-player carriers so callers don't
     *  have to null-check.
     *
     *  <p>No bootstrap: every aspect (primals included) is discovered through
     *  {@link #cascadeFromScans} the first time a scanned item contains it.
     *  Primals have no parent prerequisites, so the very first scan that
     *  includes a primal auto-discovers it. This is the uniform Thaumcraft 4
     *  rule — primals aren't "free", they're just free of prerequisites. */
    public static Data of(@Nullable Player player) {
        if (player == null) return EMPTY;
        return player.getCapability(CAP).orElse(EMPTY);
    }

    private static final Data EMPTY = new Data();

    /** Server-side: grant a single aspect. Returns true if the player didn't
     *  already know it. Triggers a sync packet so the client can refresh
     *  JEI visibility, and a cascade pass over the player's scanned items in
     *  case learning this aspect just unlocked one of its compound children
     *  in something they previously scanned.
     *
     *  <p>This direct-grant path BYPASSES the parent-prerequisite check —
     *  it's the op /tr discover entry point, treated as a god-mode override.
     *  The natural-progression discovery path is
     *  {@link #cascadeFromScans(ServerPlayer)}, called by the scan commands. */
    public static boolean discover(ServerPlayer player, Aspect aspect) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean added = d.add(aspect.id());
        if (added) {
            syncTo(player);
            cascadeFromScans(player);
        }
        return added;
    }

    /** Server-side: grant every registered aspect (used by /tr discover all). */
    public static int discoverAll(ServerPlayer player) {
        Data d = of(player);
        if (d == EMPTY) return 0;
        int before = d.size();
        for (Aspect a : Aspects.all()) d.add(a.id());
        int added = d.size() - before;
        if (added > 0) syncTo(player);
        return added;
    }

    /** Walks every block/item in the player's {@link ScannedTargets} and
     *  discovers any aspect in their composition whose parents are already
     *  known. Iterates to a fixpoint so a chain of [primal → tier-1 → tier-2]
     *  inside a single composition all unlock in one pass.
     *
     *  <p>Called automatically by:
     *  <ul>
     *    <li>{@link ScannedTargets#scanBlock}/{@code scanItem} — first scan
     *        of a target sees its aspects evaluated against current
     *        knowledge; eligible ones discover, ineligible ones are silently
     *        deferred (no research points yet — they just don't unlock).</li>
     *    <li>{@link #discover(ServerPlayer, Aspect)} — when an op direct-grants
     *        an aspect, that may unlock children in already-scanned targets.</li>
     *  </ul>
     *
     *  <p>This is the chained-discovery semantic from Thaumcraft 4: compound
     *  aspects can only be discovered if BOTH parents are already known.
     *  Without this gating, a player could scan one mid/late-game item and
     *  immediately know all 48 aspects, skipping the whole progression. */
    public static void cascadeFromScans(ServerPlayer player) {
        Data known = of(player);
        if (known == EMPTY) {
            ThaumicRemnants.LOG.info(
                    "[monocle/srv] cascadeFromScans aborted — KnownAspects cap not attached for {}",
                    player.getName().getString());
            return;
        }
        com.soul.soa_additions.tr.knowledge.ScannedTargets.Data scanned =
                com.soul.soa_additions.tr.knowledge.ScannedTargets.of(player);
        if (scanned == com.soul.soa_additions.tr.knowledge.ScannedTargets.empty()) {
            ThaumicRemnants.LOG.info(
                    "[monocle/srv] cascadeFromScans aborted — ScannedTargets cap not attached for {}",
                    player.getName().getString());
            return;
        }

        int initialSize = known.size();
        ThaumicRemnants.LOG.info(
                "[monocle/srv] cascadeFromScans entered: {} known, {} scanned blocks, {} scanned items, {} scanned entities",
                initialSize, scanned.blockSnapshot().size(),
                scanned.itemSnapshot().size(), scanned.entitySnapshot().size());
        int aspectsEvaluated = 0;
        int aspectsAdded = 0;
        boolean changed;
        int safety = 0;
        do {
            changed = false;
            for (ResourceLocation id : scanned.blockSnapshot()) {
                var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(id);
                var aspects = com.soul.soa_additions.tr.core.AspectMap.forBlock(block);
                for (var as : aspects) {
                    aspectsEvaluated++;
                    if (tryDiscoverInPlace(known, as.aspect())) {
                        aspectsAdded++;
                        changed = true;
                    }
                }
            }
            for (ResourceLocation id : scanned.itemSnapshot()) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
                var aspects = com.soul.soa_additions.tr.core.AspectMap.forItem(item);
                for (var as : aspects) {
                    aspectsEvaluated++;
                    if (tryDiscoverInPlace(known, as.aspect())) {
                        aspectsAdded++;
                        changed = true;
                    }
                }
            }
            for (ResourceLocation id : scanned.entitySnapshot()) {
                for (var as : com.soul.soa_additions.tr.aura.EntityAspectMap.forId(id)) {
                    aspectsEvaluated++;
                    if (tryDiscoverInPlace(known, as.aspect())) {
                        aspectsAdded++;
                        changed = true;
                    }
                }
            }
        } while (changed && ++safety < 16);
        ThaumicRemnants.LOG.info(
                "[monocle/srv] cascadeFromScans done: evaluated {} aspect-instances, added {} aspects, {} loop passes",
                aspectsEvaluated, aspectsAdded, safety);

        if (known.size() > initialSize) {
            ThaumicRemnants.LOG.info(
                    "[monocle/srv] cascadeFromScans: {} grew from {} to {} known aspects",
                    player.getName().getString(), initialSize, known.size());
            syncTo(player);
        }
    }

    /** True if {@code aspect} can be discovered given the player's current
     *  known set: primals always; compounds require BOTH parents already
     *  in the set. Mirrors Thaumcraft 4's discovery prerequisite rule. */
    private static boolean canDiscoverNaturally(Data d, Aspect aspect) {
        if (aspect.isPrimal()) return true;
        Aspect a = aspect.parentA();
        Aspect b = aspect.parentB();
        return a != null && b != null && d.has(a) && d.has(b);
    }

    /** Discover an aspect in-place on the data set, gated by prereqs. Used
     *  inside the cascade loop so we can mutate without going through the
     *  full discover() path (which would re-cascade). */
    private static boolean tryDiscoverInPlace(Data d, Aspect a) {
        if (d.has(a)) return false;
        if (!canDiscoverNaturally(d, a)) return false;
        return d.add(a.id());
    }

    public static boolean forget(ServerPlayer player, Aspect aspect) {
        Data d = of(player);
        if (d == EMPTY) return false;
        boolean removed = d.remove(aspect.id());
        if (removed) syncTo(player);
        return removed;
    }

    public static int forgetAll(ServerPlayer player) {
        Data d = of(player);
        if (d == EMPTY) return 0;
        int n = d.size();
        d.clear();
        if (n > 0) syncTo(player);
        return n;
    }

    /** Send the current set to one player. */
    public static void syncTo(ServerPlayer player) {
        var snap = of(player).snapshot();
        ThaumicRemnants.LOG.info("[monocle/srv] sending KnownAspectsSyncPacket to {} ({} aspects)",
                player.getName().getString(), snap.size());
        TrNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new KnownAspectsSyncPacket(snap));
    }

    // ---------------- Registration glue ----------------

    public static void register(IEventBus modBus) {
        modBus.addListener(KnownAspects::onRegisterCaps);
        // AttachCapabilitiesEvent is generic — must use addGenericListener
        // with the type filter, NOT plain addListener. See
        // ScannedTargets.register() for the full note.
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addGenericListener(
                net.minecraft.world.entity.Entity.class, KnownAspects::onAttachCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(KnownAspects::onCloneCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(KnownAspects::onLoginCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(KnownAspects::onRespawnCap);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(KnownAspects::onDimChangeCap);
    }

    public static void onAttachCap(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        if (event.getCapabilities().containsKey(CAP_ID)) return;
        event.addCapability(CAP_ID, new Provider());
        ThaumicRemnants.LOG.info("[monocle/srv] KnownAspects cap attached to {}",
                event.getObject().getUUID());
    }

    public static void onCloneCap(Clone event) {
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

    public static void onLoginCap(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    public static void onRespawnCap(PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    public static void onDimChangeCap(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
    }

    private static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(Data.class);
    }

    /** Auto-subscribed via Forge's annotation scan — same pattern as
     *  {@link ScannedTargets.Events}. The earlier manual-register approach
     *  also worked here, but standardising on the auto-subscribe path keeps
     *  both knowledge caps registered identically. */
    @Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onAttach(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (!(event.getObject() instanceof Player)) return;
            // Forge fires gatherCapabilities more than once for some entity
            // construction paths — notably FakePlayer, where Moonlight's
            // factory re-triggers it and we crash with "Duplicate Capability
            // Key" on the second call. Idempotent: skip if already present.
            if (event.getCapabilities().containsKey(CAP_ID)) return;
            event.addCapability(CAP_ID, new Provider());
        }

        /** Persist knowledge across death. Forge reuses the old player instance
         *  for clone, so we just copy data wholesale. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onClone(Clone event) {
            if (event.isWasDeath()) {
                Player oldP = event.getOriginal();
                Player newP = event.getEntity();
                oldP.reviveCaps(); // required to access caps on the dead player
                Data old = of(oldP);
                Data fresh = of(newP);
                if (old != EMPTY && fresh != EMPTY) {
                    fresh.read(old.write());
                }
                oldP.invalidateCaps();
            }
        }

        @SubscribeEvent
        public static void onLogin(PlayerLoggedInEvent event) {
            // Sync the persisted set on join — what the player knew last
            // session is restored client-side. NO bootstrap of primals here:
            // primals are discovered via the same cascade rule as every other
            // aspect (parents must be known; primals have no parents, so the
            // first scan that includes a primal auto-discovers it).
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }

        @SubscribeEvent
        public static void onRespawn(PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }

        @SubscribeEvent
        public static void onChangeDimension(PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncTo(sp);
        }

        /** First login bootstrap: every player starts with the 6 primal
         *  aspects discovered. Compounds are gated and revealed via Monocle
         *  scans / discovery events / op grants. Without this, a fresh world
         *  would hide every rune in JEI on day 1, which is more punishing
         *  than the design intends. */
    }
}
