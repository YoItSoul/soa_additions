# Performance Audit

## Quest Book Screen (Client)

### 1. Color config re-parsed on every frame
**File:** `src/main/java/com/soul/soa_additions/quest/client/QuestBookScreen.java` lines 74–109  
**Severity:** High

All 30+ `COL_*()` methods call `QuestBookConfig.argb()` on every invocation, which reads a Forge config value, trims and strips string prefixes, calls `Long.parseLong()`, and catches exceptions. These methods are called dozens of times per render frame across gradient backgrounds, node colors, borders, and text. The config's own comment acknowledges this: *"parses the string each call."*

**Fix:** Cache all color values as `int` fields when the screen opens (or when config reloads). Only re-parse on a config reload event.

---

### 2. `recomputeEditorValidation()` called every render frame
**File:** `src/main/java/com/soul/soa_additions/quest/client/QuestBookScreen.java` line 793  
**Severity:** High (edit mode only)

`renderGraph()` calls `recomputeEditorValidation()` unconditionally on every frame while in edit mode. That method iterates all quests to build a dependency adjacency map, then runs a full DFS per edge to detect cycles — O(n² × m) per frame.

**Fix:** Track a `boolean validationDirty` flag. Set it to `true` only when a quest or dependency is actually added, removed, or modified. Run `recomputeEditorValidation()` once when the flag is set, not every frame.

---

### 3. Per-pixel silhouette rendering for ICON-shaped nodes
**File:** `src/main/java/com/soul/soa_additions/quest/client/QuestBookScreen.java` lines 943–954, 1107–1121  
**Severity:** High (chapters with ICON-shaped quests)

For `ICON` shape nodes, the silhouette mask is drawn with a nested loop calling `g.fill()` per pixel. With a 32×32 mask this is up to 1,024 individual draw calls per node per frame. The hover stroke in `strokeSilhouette()` uses the same pattern.

**Fix:** Pre-bake the silhouette into a texture or `NativeImage` at cache-build time (alongside `buildMask()`), then draw it as a single textured quad each frame.

---

## Server-Side

### 4. GroveBlockEntity — `UPDATE_ALL` flag on every block during generation
**File:** `src/main/java/com/soul/soa_additions/block/entity/GroveBlockEntity.java` lines 47, 81, 91, 94, 105, 113, 138, 140, 200, 203  
**Severity:** High

Grove generation uses `Block.UPDATE_ALL` for every block placed in `generateSphericalStructure()` (~3,600 blocks), `createSkyOpening()` (~200 blocks), `generateLushVegetation()` (~27,000 block scan), and `generatePillars()`. `UPDATE_ALL` triggers neighbor updates, light recalculation, and chunk rebuilds on every single call, causing a severe server freeze spike on first grove spawn.

**Fix:** Use `Block.UPDATE_NONE` or `Block.UPDATE_CLIENTS` for all blocks during generation. After placing all blocks, send one bulk light/chunk update, or call a single `sendBlockUpdated` pass.

---

### 5. GroveBoonBlockEntity — advancement registry lookup every 60 ticks per nearby player
**File:** `src/main/java/com/soul/soa_additions/block/entity/GroveBoonBlockEntity.java` lines 72–83  
**Severity:** High

Every 60 ticks the boon block scans nearby players and calls `server.getAdvancements().getAdvancement(VISIT_GROVE_ADVANCEMENT)` for each one, which walks the advancement registry tree on the hot server tick path.

**Fix:** Cache the `Advancement` reference as a static field on first use. The registry doesn't change at runtime.

```java
private static Advancement cachedAdvancement;

private static Advancement getVisitAdvancement(MinecraftServer server) {
    if (cachedAdvancement == null)
        cachedAdvancement = server.getAdvancements().getAdvancement(VISIT_GROVE_ADVANCEMENT);
    return cachedAdvancement;
}
```

---

### 6. ModGrovePlacement — force-loading chunks during placement search
**File:** `src/main/java/com/soul/soa_additions/worldgen/ModGrovePlacement.java` line 36  
**Severity:** High

`getChunk(..., true)` is called up to `MAX_ATTEMPTS` times while searching for a valid grove spawn location. Each call force-loads a chunk. On a fresh world this can load 50+ chunks unnecessarily, causing a server freeze on first boot.

**Fix:** Pass `false` to avoid force-loading, and skip positions whose chunks aren't already loaded:
```java
if (!level.hasChunkAt(potentialPos)) continue;
```

---

### 7. DonorOrbManager — 200-block AABB entity query every second per player
**File:** `src/main/java/com/soul/soa_additions/donor/DonorOrbManager.java` lines 83–88  
**Severity:** Medium

A server tick handler runs every 20 ticks and calls `hasExistingOrb()` for every online player. That method queries all `DonorOrbEntity` instances within a 200-block (radius 100) AABB. With 20 players this is 20 large entity scans per second.

**Fix:** Maintain a `Map<UUID, DonorOrbEntity>` keyed by player UUID, populated on orb spawn and cleared on orb removal. Replace the AABB scan with a map lookup.

---

### 8. QuestSyncPacket — full quest snapshot sent to all team members on every claim
**File:** `src/main/java/com/soul/soa_additions/quest/net/QuestSyncPacket.java` lines 52–93  
**Severity:** Medium

Every quest completion serializes the entire quest book state (all quests) and sends it to every online team member. The comment in the file even notes it is "not a delta protocol yet."

**Fix:** Send only the changed quest entries. Create a `QuestDeltaPacket` containing just the list of `(questId, newStatus)` tuples that changed. Fall back to a full sync only on login or reconnect.

---

### 9. QuestNotifier — full NBT read/write round-trip per quest completion
**File:** `src/main/java/com/soul/soa_additions/quest/progress/QuestNotifier.java` lines 84–104  
**Severity:** Medium

`markNotified()` calls `loadNotified()` (full NBT parse into a `HashSet`) then immediately calls `saveNotified()` (full NBT write) for every individual quest completion. Multi-quest claims trigger this repeatedly.

**Fix:** Cache the notified set in memory for the duration of the session. Flush to NBT only on player logout or periodically. For individual lookups, `player.getPersistentData().contains("notified_" + questId)` is faster than deserializing a list.

---

### 10. QuestEvaluator — fixed-point iteration capped at 16 passes
**File:** `src/main/java/com/soul/soa_additions/quest/progress/QuestEvaluator.java` lines 75–89  
**Severity:** Medium

`recomputeAll()` uses a fixed-point loop with a hard `guard > 16` break. Quest packs with dependency chains deeper than 16 levels will silently fail to propagate unlocks in a single evaluation pass — those quests will remain locked until the next trigger.

**Fix:** Replace the fixed-point loop with a topological sort of the dependency graph. Evaluate quests in topological order so every quest is computed exactly once per pass, and depth is no longer a factor.

---

### 11. ObserveTaskPoller — oversized AABB for entity raycasts
**File:** `src/main/java/com/soul/soa_additions/quest/events/ObserveTaskPoller.java` lines 102–105  
**Severity:** Low–Medium

For long-range observe tasks (`maxReach` up to 256), the AABB passed to `ProjectileUtil.getEntityHitResult()` becomes enormous and scans a very large entity volume. This runs every 20 ticks per player with active observe tasks.

**Fix:** Clamp `maxReach` to a reasonable maximum (64–128 blocks). If long-range observing is intentional, first collect entities within a reasonable radius, then raycast only that subset.

---

### 12. InventoryItemPoller — lambda allocation in per-slot loop
**File:** `src/main/java/com/soul/soa_additions/quest/events/InventoryItemPoller.java` lines 50–63  
**Severity:** Low

`Map.merge(id, count, Integer::sum)` allocates a synthetic method reference on each call. With 40 inventory slots this is 40 allocations per poll per player.

**Fix:**
```java
owned.put(id, owned.getOrDefault(id, 0) + stack.getCount());
```

---

### 13. Telemetry — potential duplicate heartbeat tasks on repeated `startHeartbeat()` calls
**File:** `src/main/java/com/soul/soa_additions/telemetry/Telemetry.java` lines 193–230  
**Severity:** Low

If `startHeartbeat()` is called more than once (e.g., dimension change), a new scheduled task is created without cancelling the previous one. Both tasks then fire concurrently, doubling heartbeat I/O.

**Fix:** Cancel the existing task before scheduling a new one:
```java
if (heartbeatTask != null && !heartbeatTask.isDone())
    heartbeatTask.cancel(false);
heartbeatTask = heartbeatExec.scheduleAtFixedRate(...);
```

---

## Client-Side (Other)

### 14. IconPickerPopup — static item registry cache never invalidates
**File:** `src/main/java/com/soul/soa_additions/quest/client/IconPickerPopup.java` lines 50, 63–77  
**Severity:** Low

`CACHED_ALL` is a static field populated on first use with a full walk of `BuiltInRegistries.ITEM` and is never cleared. On large modpacks with 5,000+ items the first open is noticeably slow (~50–100ms), and the cache goes stale if datapacks reload mid-session.

**Fix:** Invalidate `CACHED_ALL` by setting it to `null` in a `ResourceManagerReloadEvent` listener.

---

## Priority Summary

| # | Issue | Severity | Side | Fix Effort |
|---|-------|----------|------|-----------|
| 1 | Color config parsed every frame | High | Client | Low |
| 2 | Cycle detection every render frame | High | Client | Low |
| 3 | Per-pixel silhouette draw calls | High | Client | Medium |
| 4 | `UPDATE_ALL` during grove generation | High | Server | Low |
| 5 | Advancement registry lookup every 60 ticks | High | Server | Low |
| 6 | Force-loading chunks in grove placement | High | Server | Low |
| 7 | 200-block AABB query per player per second | Medium | Server | Medium |
| 8 | Full quest snapshot on every claim | Medium | Server | High |
| 9 | NBT round-trip per quest completion | Medium | Server | Medium |
| 10 | Fixed-point evaluation limited to 16 passes | Medium | Server | Medium |
| 11 | Oversized AABB in observe task poller | Low–Med | Server | Low |
| 12 | Lambda allocation in inventory poller | Low | Server | Low |
| 13 | Duplicate telemetry heartbeat tasks | Low | Server | Low |
| 14 | Icon picker cache never invalidates | Low | Client | Low |
