# Blastem proximity check force-loads chunks on the server thread

**Affected build:** `defiledlands-forge-1.9-1.20.1.jar` — package `lykrast.defiledlands`, Architectury-based (uses `dev.architectury.registry.registries.RegistrySupplier`)
**Minecraft / Loader:** 1.20.1, Forge 47.4.10
**Severity:** Severe — measured at 58.1% of total server-thread time in a modpack context

> **Attribution note before filing:** this is a 1.20.1 Forge port. Lykrast's original Defiled Lands
> is a 1.12-era mod, and Defiled Lands: Reborn (`com.euphony.defiled_lands_reborn`) is a separate
> codebase that does **not** contain this bug. File this only against the repository that actually
> ships the `lykrast.defiledlands` 1.20.1 port. Do not file it against the original 1.12 mod or
> against Reborn — neither contains the code below.

## Summary

`ForgeEventHandler.checkBlastemAround` calls `ServerLevel.getBlockState` on every block position in
an entity's bounding box without checking whether those positions are in loaded chunks. On the
server thread, `getBlockState` against an unloaded position does not return air — it routes through
`ServerChunkCache.getChunkBlocking`, which parks the calling thread until the chunk is fully
available.

Because this runs from a `LevelTickEvent` over `ServerLevel.getAllEntities()`, the main server
thread ends up synchronously generating terrain inside a tick.

## The code

Decompiled from the shipped jar:

```java
public static void onLevelTick(LevelTickEvent e) {
    if (e.phase != END) return;
    if (!(e.level instanceof ServerLevel level)) return;
    if (level.getGameTime() % 10 != 0) return;          // every 10 ticks
    level.getAllEntities().forEach(entity -> {          // ALL entities, not just ticking ones
        if (entity instanceof LivingEntity le && PlantUtils.vulnerableToBlastem(le))
            checkBlastemAround(level, le);
    });
}

private static void checkBlastemAround(ServerLevel level, LivingEntity entity) {
    AABB box = entity.getBoundingBox().inflate(0.1D);
    for (int x = floor(box.minX); x <= floor(box.maxX); x++)
      for (int y = floor(box.minY); y <= floor(box.maxY); y++)
        for (int z = floor(box.minZ); z <= floor(box.maxZ); z++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);   // <-- no hasChunk / isLoaded guard
            if (state.is(ModBlocks.BLASTEM.get())) { ... }
        }
}
```

There is no chunk-load check anywhere on this path.

## Why it is expensive

`getAllEntities()` includes entities that are not currently being ticked, including ones adjacent to
the loaded-chunk frontier. Any such entity whose bounding box crosses into an unloaded chunk forces
a blocking load.

The cost is wildly asymmetric between environments:

- **Dedicated server / previously-explored terrain:** the chunk exists on disk, `getChunkBlocking`
  returns in microseconds, and the bug is effectively invisible.
- **Singleplayer / newly-generated terrain:** the chunk does not exist, so the blocking call runs
  the *entire worldgen stack* inline on the tick thread while everything else waits.

In a pack with heavy worldgen this is catastrophic. It presents to users as the world freezing while
terrain generates, and as singleplayer being dramatically slower than a dedicated server running the
identical modpack.

## Measurement

spark profile, server thread, 120-second sample, singleplayer, Forge 1.20.1:

```
MinecraftServer.tickServer                        77.9%
└─ ForgeEventFactory.onPostLevelTick               60.5%
   └─ lykrast.defiledlands.ForgeEventHandler
      └─ checkBlastemAround                        58.1%   69,688 ms
         └─ ServerLevel.getBlockState              58.1%   69,664 ms
            └─ ServerChunkCache.getChunk
               └─ getChunkBlocking                 58.1%
                  └─ Unsafe.park
```

**69,688 ms of 120,000 ms — 58.1% of the entire server thread — parked inside this handler.**

Note that self-time attribution does not reveal this: the mod's own frames show near-zero self time
because the cost is parked in `jdk` internals. Only the inclusive call tree exposes it.

## Suggested fix

Guard the sweep with a non-blocking load check before touching any block state:

```java
private static void checkBlastemAround(ServerLevel level, LivingEntity entity) {
    AABB box = entity.getBoundingBox().inflate(0.1D);
    int minCX = SectionPos.blockToSectionCoord(Mth.floor(box.minX));
    int maxCX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX));
    int minCZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ));
    int maxCZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ));
    for (int cx = minCX; cx <= maxCX; cx++)
        for (int cz = minCZ; cz <= maxCZ; cz++)
            if (!level.hasChunk(cx, cz)) return;   // never force-load from a tick handler
    // ... existing sweep ...
}
```

`LevelReader.hasChunk(int, int)` is non-blocking. Behaviour inside loaded terrain is unchanged; the
only difference is that blastem no longer reaches entities in chunks the server has not loaded —
which it could previously only do by stalling the tick to load them.

### Better still

Implement the effect as `Block.entityInside(BlockState, Level, BlockPos, Entity)` on the blastem
block instead of a global tick sweep. That hook is driven by the entity's own collision handling,
runs only for entities in ticking chunks, scales with nearby blastem rather than with total entity
count, and cannot force-load anything. Defiled Lands: Reborn implements it this way and does not
exhibit the problem.
