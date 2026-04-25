# Thaumic Remnants — Design Specification

> **Naming history:** the design was originally drafted as "Aetherium Arcanum"; it was renamed to **Thaumic Remnants** (mod id `tr`) before the first implementation milestone. The body of this document below still uses the old "aetherium" name in many places — that is historical only. Every `aetherium:`/`com.aetherium.arcanum.*` reference in the prose maps to `tr:`/`com.soul.soa_additions.tr.*` in the actual codebase. Do not copy/paste the structure block below as-is.

**Target:** Minecraft 1.20.1, Forge
**Purpose:** A unified replacement for Thaumcraft (1.7.10-era research/aspect/aura), Astral Sorcery (1.12.2-era ritual/perk/constellation), and the dimensional progression framing of AbyssalCraft. Thaumon's decorative blocks are absorbed as functional infrastructure.

This document is laid out as the mod's source tree. Each file entry describes the content, mechanics, and implementation notes for that logical unit. No code — design only.

---

## Project Root

Thaumic Remnants ships as a sub-package of `soa_additions` rather than a standalone mod jar (per the `feedback_mods_toml_single_entry.md` convention — own namespace, no separate `[[mods]]` entry). Effective layout:

```
soa_additions/
└── src/main/
    ├── java/com/soul/soa_additions/tr/   # mod code
    └── resources/
        ├── assets/tr/                     # textures, lang, item models
        └── data/tr/                       # datapack content (recipes, tags, aspect tree)
```

---

## `mods.toml`

- The `tr` namespace is registered through soa_additions' `mods.toml`; no separate Forge `@Mod` container.
- Display name (in-game): **Thaumic Remnants**
- Minecraft: 1.20.1
- Forge: 47.x
- Dependencies: Forge (required), Curios API (required — bauble slots), JEI (optional integration), Patchouli (optional — fallback book rendering if custom Thaumonomicon UI is not shipped in v1).
- Soft dependencies declared (whitelist targets): alexsmobs, iceandfire, twilightforest, upgrade_aquatic, mowziesmobs, rats, irons_spellbooks. Mod does not crash if absent.

---

## `src/main/java/com/aetherium/arcanum/`

```
arcanum/
├── AetheriumArcanum.java         # mod entrypoint, registration bus
├── core/                         # cross-cutting systems (Weave, Flux, Aspects)
├── research/                     # Thaumonomicon + hex puzzle
├── grimoire/                     # Arcane Grimoire, focus pages, clasps, attunement
├── crafting/                     # Artificer tiers (workbench, infusion, celestial)
├── aura/                         # chunk Weave data, confluences, taint spread
├── constellation/                # Astral-style constellations, perks, attunement
├── ritual/                       # multiblock ritual framework
├── dimension/                    # The Aetherial Expanse + layers
├── entity/                       # corrupted mob wrapper + whitelist system
├── block/                        # all blocks incl. Thaumon absorption, Alchemical Fire
├── item/                         # all items
├── worldgen/                     # Expanse features, Warded Shell, Conduits
└── compat/                       # JEI, Curios, cross-mod whitelist loader
```

---

# `core/` — Cross-Cutting Systems

## `core/Weave.md`

**Concept:** A single unified world-level magical field replacing Thaumcraft's Vis, Astral Sorcery's Starlight, and anything like them as separate resources. Every chunk carries Weave state.

**Three measurable properties per chunk:**

1. **Vis** — ambient magical energy. Range 0–1000. Regenerates slowly over time (roughly +1/sec baseline). Consumed by Grimoire casts, infusions, rituals. Biome-weighted initial composition (desert = Ignis-heavy, ocean = Aqua-heavy).
2. **Resonance** — sky-linked component. Range 0–500. Rises at night, scales with moon phase, peaks when attuned constellations are overhead. Blocked by any non-transparent block between the chunk surface and the sky.
3. **Flux** — corruption byproduct. Range 0–1000. Accumulates from Grimoire overuse, failed infusions, dimensional rifts, player death in magical context. Passively decays slowly outdoors in daylight; accelerated decay near Silverwood; does not decay at all in the Expanse.

**Implementation notes:**
- Stored as Forge `LevelChunk` capability (`WeaveChunkCapability`).
- Lazy-evaluated, does not tick. Capability stores last-update timestamp; values computed on read from elapsed ticks.
- Aspect composition stored as a pre-sized fastutil `Object2FloatOpenHashMap<Aspect>` (6 primal slots), normalized to sum of Vis value.
- Cross-chunk averaging: Grimoire draws and ritual effects read from a 3×3 chunk area around the caster, weighted toward center, capability handles cached for the duration of the channel.

**Visualization:**
- Arcane Monocle held in main hand displays a HUD overlay showing local Vis/Resonance/Flux bars + aspect wheel.
- Upgraded Arcane Monocle (Silverwood+) shows per-chunk numeric values and 3×3 heatmap.

**Initial chunk aspect composition (biome-driven, 3D-aware):**
- On a chunk's first load, its aspect composition is seeded from a **biome → aspect** lookup table — TC4 model, but adapted for 1.18+ 3D biomes.
- Sample altitudes spread through the full build range (Y = 280, 128, 64, 16, -16, -50, -64) collect every biome present anywhere in the chunk's vertical column. **Any biome present contributes** — a chunk with plains on the surface and deep_dark hidden underground gets seed contributions from BOTH. Same biome at multiple sample points is counted once.
- Per-biome compositions are summed, then a per-chunk ±25% jitter is applied on the total. Deterministic per (chunk position, world seed) so reseeds reproduce exactly.
- Default table is data-driven: `data/<ns>/tr_aspects/biome/<biome-path>.json` using the same JSON shape as item aspects (`{"aspects": {"tr:terra": 50, ...}}`).
- Mod biomes that lack an explicit JSON entry fall back to a sensible default mix derived from their biome tags (`forge:is_hot` → +Ignis, `forge:is_wet` → +Aqua, `minecraft:is_forest` → +Herba/Arbor, etc.).

**Aspect impartment (NEW — replaces TC4's silverwood-only generation):**
- Every block destroyed in a chunk has a **chance to impart a fraction of its aspect composition** into that chunk's pool. Default per-block-break: roll 25% per aspect; on success, add `floor(amount * 0.5)` (minimum 1) of that aspect to the chunk. Applies to all breaks regardless of game mode (creative included). Configurable per-aspect via datapack.
- Every entity death in a chunk imparts aspects similarly, weighted by entity aspect composition (which lives in `data/<ns>/tr_aspects/entity/<entity>.json` — same JSON shape as items). **Any** death contributes — hostile, neutral, passive — same "any block can contribute" semantics. The randomness + small per-roll yield is the throttle, not the entity type.
- Player-killed entities: 50% per-aspect roll, full amount. Environmental kills: 25% per-aspect roll, half amount.
- Initial chunk seeds are intentionally far below the cap — TC4-style baselines of ~30-100 total per chunk (plains ~65, forest ~70, desert ~70, jungle ~85, void/deep_dark ~100), with caps of 500 per aspect and 1000 total. That gives ~90% headroom for impart, so block-break / mob-kill activity actually moves the needle in a chunk's pool over time. A "pristine" chunk has roughly TC4 vis levels; a heavily-worked chunk can carry 5-10× that.
- Imparted aspects accumulate above the biome baseline up to those caps. Past saturation, further impart silently no-ops (no overflow, no error) — meaning a chunk can hold at most 2 fully-stacked aspects, or any denser mix of partials summing to 1000.
- This is the main world-driven progression: scanning teaches you what aspects exist; harvesting and combat builds the chunk pool you'll later draw from for auramancy.

---

## `core/Aspects.md`

**Six primal aspects (Latin nomenclature, mirroring Thaumcraft 4):**
- **Aer** (air, flight, sound)
- **Aqua** (water, cold, fluidity)
- **Ignis** (fire, heat, destruction)
- **Terra** (earth, solidity, mass)
- **Ordo** (order, precision, logic)
- **Perditio** (entropy, decay, void)

**Compound aspects** — every higher aspect derives from exactly two primals or compounds. Full tree lives in `research/AspectTree.md`.

**Data structure:**
- Each aspect is a registered object with ID, color, icon, parents (for compounds), and "affinity tags" matching block/item/entity data tags.
- Aspects attach to items, blocks, and entities via a data-driven tag/JSON system under `data/aetherium/aspects/`. Pack makers can override or extend.
- Default aspect assignments ship for vanilla items and for every whitelisted mod where detectable.

**Scanning:**
- Arcane Monocle zoom on target. First scan of an item/entity/block grants research points (2–10 per aspect present).
- Already-scanned targets give 0 points but reveal aspects in UI.
- Scanning contributes to Theorycraft progression (see `research/`).

---

## `core/Flux.md`

**Three flux tracks:**

### Personal Flux (player-bound, persistent)
- Range 0–100. Stored as player capability, persistent across deaths.
- Accumulated from: failed infusions performed while holding the Grimoire, extended time at 100% Temporary Flux without cleansing, corrupted attacks tagged for permanent contamination.
- Does NOT accumulate from normal Tainted Water exposure — that now feeds Temporary Flux instead.
- Slow natural decay: −1/in-game day when in pure chunks. Does not decay in Expanse.
- Reduced by: Cleansing Ritual at Infusion Altar, drinking purified brews, endgame content completion (one-time −50 cleanse).

### Temporary Flux (player-bound, fast-cycling)
- Range 0–100. Resets whenever player is outside any Flux source for 30 seconds.
- Accumulated from: Tainted Water contact, close-range hits from corrupted mobs in the Expanse, prolonged exposure to tainted biomes, high-chunk-Flux ambient fields.
- This is the **contact-based insanity mechanic.** Rises rapidly during the Sunken Vale descent, drops rapidly once the player surfaces or returns to a pure zone.
- Consequences scale with current value:
  - 20%+: faint purple vignette on screen edges.
  - 40%+: occasional phantom mob silhouettes visible in peripheral vision (non-interactive, despawn when looked at directly).
  - 60%+: **hallucinated mobs** begin spawning frequently — they look and sound real, deal no damage, despawn on hit or after 5 seconds. Frequency scales with Temp Flux value.
  - 75%+: phantom hit sounds, fake damage indicators (screen shake + hit noise with no actual damage), ambient whispering.
  - 90%+: hallucinated mobs appear in swarms, brief screen-warp effects, Grimoire occasional misfires.
  - 100%: convert +1 Temporary Flux to +1 Personal Flux every 10 seconds until Temp Flux drops. This is the only path to persistent contamination from environmental sources.
- **Key design intent:** Temporary Flux is the horror dial. It makes the descent feel genuinely unsettling without permanently punishing the player for exposure they needed to make progress. You can push into dangerous zones, come back, and recover — but while you're in it, you can't trust your own eyes.

### Chunk Flux (world-bound)
- Per-chunk value on the Weave capability. Range 0–1000.
- High chunk Flux causes:
  - Aspect distribution warps toward Perditio/Vacuos.
  - Grass and leaves slowly transform to corrupted variants.
  - Natural mob spawns gain the corrupted wrapper (see `entity/`).
  - Vis regeneration halts above 500 chunk Flux.
  - Contributes +0.2/sec to nearby players' Temporary Flux when above 600.
- Taint biome spread: chunks that hit max Flux for sustained periods (8 in-game days) convert to the Tainted biome, a purple-mist hazard zone that spreads at a slow rate to adjacent high-flux chunks. Never spawns naturally in the Overworld — only as consequence of player actions.

**Hallucinated mob implementation:**
- Client-side visual entities, not server-tracked.
- Spawn rate determined by local player's Temp Flux on each client tick.
- Models pull from a pool of "seen" entity types (entities the player has encountered) — scarier because it uses what you already fear.
- Despawn on: looked at for >1 sec, hit by player attack, 5-second timeout, Temp Flux drops below 60%.
- Cannot damage player. Do not block movement. Do not drop loot.
- Sound effects are real (heard via client audio) but come from non-existent entities.

**Implementation notes:**
- Personal Flux and Temporary Flux are separate capabilities on `Player`, both sync to client for HUD.
- HUD shows both bars when either is non-zero.
- Taint spread runs on a weekly tick scheduler, not per-tick, to avoid lag.
- Taint is reversible via a Silverwood planting + Cleansing Ritual sequence.

---
# `research/` — Thaumonomicon and the Hex Puzzle

## `research/Thaumonomicon.md`

**Obtained:** right-click any vanilla bookshelf with a crafted Greatwood Grimoire. Book is given once per player; loss recoverable by recipe (book + Greatwood + ink sac).

**Tabs (initial):**
- Basics (scanning, aspects, first Grimoire)
- Thaumaturgy (Grimoire tiers, focus pages, charms)
- Alchemy (crucible, essentia, brews)
- Artifice (infusion altar, caps, bauble crafting)
- Golemancy (helper constructs — simplified vs TC4; optional v1)
- Constellations (starlight, attunement, perks)
- Rituals (multiblock rites)
- Expeditions (the Expanse, dimensional gear, prep)
- Eldritch (endgame, revealed only after Heart of the Expanse)

**Implementation:**
- Book UI is custom GUI (not Patchouli), fullscreen, styled with Thaumcraft 4 aesthetic (parchment, inked diagrams, aspect icons).
- Entries gated by research completion — locked entries show as "???" with hint text about required scans.
- Each tab rendered as a node graph; completing a node unlocks linked nodes.

---

## `research/HexPuzzle.md`

**The core research minigame** — a hexagonal board puzzle where the player connects a start aspect to an end aspect by placing aspect "marbles" in domino-chain fashion across a field of blocked and tainted slots.

### Board Structure

- The board itself is a **large pointy-top hexagon** made up of a grid of **smaller pointy-top hexagonal slots**.
- Slot and outer frame orientations match (both pointy-top) for a visually cohesive, symmetrical layout.
- Slots are spaced with visible gaps between them — they read as discrete pieces, not a continuous honeycomb.
- Each slot can hold one **aspect marble** — a round piece labeled with an aspect icon, sitting inside its hex slot like a marble in a divot.
- Boards come in **three fixed complexity tiers** for GUI consistency. Each tier has a hard size cap so the board always fits the research screen without scrolling, zoom, or layout tricks.

| Tier | Radius | Slot count | Row layout | GUI scale | Typical use |
|---|---|---|---|---|---|
| **Basic** | 2 | 19 slots | 3-4-5-4-3 | Large, easily readable marbles | First scans, aspect theory, basic Thaumaturgy |
| **Advanced** | 4 | 61 slots | 5-6-7-8-9-8-7-6-5 | Medium marbles, still no scroll | Alchemy, Artifice, Infusion topics, constellations |
| **Expert** | 6 | 127 slots | 7-8-9-10-11-12-13-12-11-10-9-8-7 | Small marbles, full-screen board | Void Metal, dimensional research, high-tier rituals |

Slot counts follow the standard hex-number sequence `3r² + 3r + 1`: radius 2 = 19, radius 4 = 61, radius 6 = 127. Each tier adds exactly two rings of slots to the previous tier, creating a clean visual progression between difficulty levels. **Nothing larger than Expert ships.** If a topic needs more depth, it gets split into a chain of multiple Expert puzzles.

### Corrupted Variants

Each tier has a **corrupted variant** that adds taint slots to the board layout. This doubles the practical topic variety without needing a fourth tier:

| Variant | Adds |
|---|---|
| **Basic** | Blocked slots + pre-placed hints only |
| **Basic (Corrupted)** | Above + 1–3 taint slots |
| **Advanced** | Blocked slots + pre-placed hints only |
| **Advanced (Corrupted)** | Above + 3–6 taint slots |
| **Expert** | Blocked slots + pre-placed hints only |
| **Expert (Corrupted)** | Above + 6–12 taint slots |

Corrupted variants gate **specifically** the research branches tied to Flux, taint, and Eldritch content. Thematically: the corruption is bleeding into the research itself, and the player must work around or cleanse it to gain the knowledge. A non-corrupted Expert topic is a pure test of aspect chaining; a corrupted Expert topic is the same plus an economic decision about whether to spend Ordo points neutralizing taint vs routing around it.

**Topics declare their tier and corruption flag in JSON.** The board generator uses tier → radius + base layout rules, and the corruption flag toggles taint placement on top.

### Slot Types

Each slot on a board is one of:

| Slot Type | Behavior |
|---|---|
| **Empty** | Can be filled with an aspect marble purchased with research points. |
| **Pre-placed aspect** | Locked aspect marble placed by the topic definition — cannot be moved or replaced. These include the start node, the end node, and any "hint" pieces. |
| **Blocked** | Wall/stone slot. Cannot be filled. Blocks all connection. Appears as a solid inert hex. |
| **Taint slot** | Corrupted hex (visually: violet-swirling, Perditio-aspect fog inside the slot). **Any aspect placed adjacent to a taint slot has its connections on the taint-touching side severed.** Taint slots can be **neutralized** by spending Ordo points (see below) or by connecting a specific purification aspect. |
| **Unstable slot** | Rare. Accepts marbles but has a chance to "crack" on placement — the marble remains but all its connections are marked as fragile and don't count toward completion unless reinforced by redundant paths. |

### Connection Rules (Domino-Chain Style)

A hex slot has **6 neighbors** (pointy-top hex adjacency — E, W, NE, NW, SE, SW). Two adjacent marbles are **connected** if their aspects are compatible:

- **Identical aspect** — always connected (Aqua ↔ Aqua).
- **Parent → compound** — connected if one aspect is a direct parent of the other (Aqua ↔ Victus, because Victus = Aqua + Terra).
- **Sibling compound** — connected if both share a common parent (Victus ↔ Herba, because both contain Terra).

Like dominos, valid connections form **chains**. A chain begins at the start node and propagates through every directly-connected compatible neighbor. The puzzle is solved when **the end node is in the same chain as the start node**.

**Taint disrupts chains.** If a marble is adjacent to a taint slot, the specific edge on the taint side is dead — connections cannot form through that edge. If a marble is surrounded by taint on enough sides, it becomes an island regardless of which aspects are next to it.

**Blocked slots disrupt chains.** A blocked slot is simply a wall — no connection flows through it, but it doesn't actively corrupt neighbors the way taint does.

### Branching and Multi-End Puzzles

Advanced research boards can have:

- **Multiple end nodes** — all must be connected to the start node (or to each other, depending on topic flag).
- **Gated intermediates** — specific mandatory aspects that must appear in the chain (e.g., "this research requires the chain to pass through an Ordo-aspect marble").
- **Exclusion rules** — certain aspects forbidden on the board for that topic (e.g., Eldritch research may forbid all Lux aspects).

### GUI Layout

The research screen has three persistent regions, plus the overlay chrome:

```
┌─────────────────────────────────────────────────────────────────────┐
│  Topic Title                                               [ X ]    │
│  Tier · Category                                                    │
├────────────────────────────────────────────────────┬────────────────┤
│                                                    │  ASPECT PANEL  │
│                                                    │  ┌──────────┐  │
│                                                    │  │ ▲ scroll │  │
│                                                    │  ├──────────┤  │
│                                                    │  │ Aqua  45 │  │
│               BOARD VIEWPORT                       │  │ Ignis 23 │  │
│                                                    │  │ Terra 12 │  │
│          (pan + zoom interactive)                  │  │ Aer   30 │  │
│                                                    │  │ Ordo   9 │  │
│                                                    │  │ Perd.  4 │  │
│                                                    │  │ Victus 7 │  │
│                                                    │  │ Herba  5 │  │
│                                                    │  │  ...     │  │
│                                                    │  ├──────────┤  │
│                                                    │  │ ▼ scroll │  │
│                                                    │  └──────────┘  │
├────────────────────────────────────────────────────┴────────────────┤
│  [ − zoom out ]    [ reset view ]    [ + zoom in ]    [ SUBMIT ]    │
└─────────────────────────────────────────────────────────────────────┘
```

**Board viewport (center-left, dominant region):**
- Renders the hex board. Pan-and-zoom enabled regardless of tier.
- **Default zoom** fits the entire board in the viewport with all slots at their tier's native pixel size. Basic boards look large and readable at default; Expert boards default to their fully-zoomed-out view.
- **Zoom in** (mouse wheel up, `+` button, or ctrl-scroll): slots enlarge, player can inspect individual marbles, aspect symbols, and taint swirl detail. Pan clamps so the board never leaves the viewport.
- **Zoom out** (mouse wheel down, `−` button): useful on Expert boards to see the whole board when zoomed in on a region.
- **Reset view** button snaps back to the default "board fills viewport" framing.
- **Middle-click drag** or **shift+drag** pans the view. Left-click drag is reserved for moving marbles.
- Zoom clamped to a reasonable range (e.g., 0.5× to 3.0× of default) so the board can't be zoomed to uselessness in either direction.
- Zoom state is per-session, not saved — every topic opens at default zoom.

**Aspect panel (right side, ~20% of screen width):**
- Vertical scrolling list of every aspect the player has points in.
- Each row shows: aspect icon, aspect name, point balance, and a "draw marble" button.
- Scroll via mouse wheel when hovering the panel, or via the up/down arrow buttons at top and bottom of the panel.
- Sort order defaults to primals first (Aer, Aqua, Ignis, Terra, Ordo, Perditio), then compounds alphabetical. A sort-mode toggle at the top can switch to "most-used-first" or "currently-in-hand-first".
- **Search bar** at the top of the panel filters aspects by name — essential once the player has 40+ aspect types. Filter is client-local, no packet traffic.
- Clicking an aspect row "picks up" one marble of that aspect (costs 1 point, cursor now holds the marble).
- Right-clicking an aspect row picks up a stack of 5 if the player has the points (shortcut for heavy placement phases).
- Panel shows a "currently held" indicator at the top when a marble is on the cursor, with a cancel-to-refund option.

**Bottom toolbar:**
- Zoom controls grouped on the left.
- Submit button on the right, only enabled when the board contains at least one placed marble. Submit triggers server-side validation.
- Central "reset view" to recover from any pan/zoom confusion.

**Header:**
- Topic title and category on the left. Close button on the right. Tier and corruption status shown as a subtitle (e.g., "Advanced · Corrupted · Artifice").

**Resolution targets:**
- Minimum supported: 1280×720. All three tiers fit at default zoom.
- At 1920×1080 and higher, default zoom may be larger than "fits viewport" because the board simply has more room — aspect panel stays fixed width, board gets the bonus space.
- Minecraft GUI scaling settings are respected. Panel and button sizes scale proportionally.

### Player Interaction

1. Open a topic at the Research Table. Board renders with all pre-placed marbles, blocked slots, and taint slots visible.
2. Player's "hand" is an inventory of aspect marbles they've generated by spending research points (e.g., spend 3 Terra points → gain 1 Terra marble).
3. **Drag marble from hand to any empty slot.** Marble snaps into the slot. Board immediately re-evaluates chains — connected-to-start marbles glow with a subtle aspect-color trail.
4. **Right-click a placed marble** to pick it back up (returns to hand, no point cost — but placed marbles in certain slot types become locked).
5. **Right-click a taint slot** while holding an Ordo marble of specific threshold quantity to neutralize it (taint is consumed, slot becomes empty).
6. Player can leave the Research Table mid-puzzle; state persists in the player capability.
7. **Submit button** validates the board server-side. Success unlocks the research topic; failure provides specific feedback ("start and end are not connected", "chain broken at coordinate 4,7 by taint", etc.).

### Point Economy

- Research points are **per-aspect** (e.g., 45 Aqua points, 12 Ordo points).
- Earned via scanning items/blocks/entities with the Arcane Monocle.
- Spent to generate marbles for the hand (1 marble = 1 point of that aspect; compound marbles may cost 2 points of one parent or 1 of each parent).
- Marbles returned to hand refund their cost.
- Submitting a winning solution **expends all placed marbles** (non-refundable). The successful board is saved as a visual record in the Thaumonomicon, so players can see their own solution.
- Neutralizing a taint slot costs 3 Ordo points directly (consumed, not returned).

### Topic JSON Schema (example)

```
{
  "id": "aetherium:basic_infusion",
  "category": "artifice",
  "title": "Basic Infusion",
  "tier": "advanced",
  "corrupted": false,
  "prerequisites": ["aetherium:aspect_theory"],
  "slots": [
    { "pos": [0, 0], "type": "start", "aspect": "aqua" },
    { "pos": [5, 0], "type": "end", "aspect": "praecantatio" },
    { "pos": [2, -1], "type": "blocked" },
    { "pos": [3, 1], "type": "hint", "aspect": "ordo" }
  ],
  "rules": {
    "multi_end": false,
    "gated_aspects": [],
    "excluded_aspects": []
  }
}
```

- `tier` is one of `"basic"`, `"advanced"`, `"expert"` — determines board radius and GUI scaling.
- `corrupted: true` enables automatic taint slot placement (density scales with tier per the table above). Taint positions are either author-specified in the `slots` array or auto-placed by a seeded RNG using the topic ID for reproducibility.
- Slot coordinates use **axial hex coordinates** (q, r) — standard for hex grids. Coordinates must fall within the tier's radius or the topic fails validation at datapack load.
- Omitted slots default to empty.

### Visual & Audio Feedback

- Connected chains glow in a soft aspect-color gradient running from start to end.
- Placing a marble into a slot that completes the puzzle: chain pulses brightly, success chime, pieces settle with a quiet click.
- Placing into a taint-adjacent slot where the taint kills a needed edge: short discordant sound, the dead edge renders as a crack.
- Neutralizing a taint: Ordo marble dissolves into the taint, violet fog lifts, soft chime.
- Submitting an invalid board: red highlight on the first broken edge or isolated segment.

**Implementation notes:**
- Client renders the board in a custom `Screen` subclass. Hex slots are stored in a **fixed-size array** sized to the tier (19/61/127 slots). No sparse map, no dynamic sizing.
- A single GUI layout template handles all three tiers — only the slot pixel-size and spacing vary per tier. Guaranteed fit on every supported screen resolution (1280×720 minimum).
- Pointy-top hex orientation throughout: slots, outer frame, and axial coordinate system all use the same orientation for layout simplicity.
- Aspect compatibility is resolved from the pre-built adjacency BitSet from `AspectTree.md`.
- Chain evaluation after a marble placement uses a **flood fill** from the start node — bounded at 127 hexes max (Expert), trivial cost.
- Board state stored on the player capability as a compact byte array: one byte per slot (4 bits slot type + 4 bits aspect ID). Maximum in-progress board state: **127 bytes** (Expert tier).
- Server-side submit revalidates the entire chain before accepting completion — client cannot spoof success.

---
## `research/AspectTree.md`

Full enumeration of every aspect, parents, color, icon filename, and default tag-based affinities. Used as data reference by multiple systems. Lives as a single authoritative JSON under `data/aetherium/aspects/tree.json`.

All aspect names use Latin nomenclature inherited from Thaumcraft 4 (1.7.10). The complete tree contains 48 aspects: 6 primal and 42 compound.

### Primal Aspects (6)

| Aspect | Meaning | Color | Description |
|---|---|---|---|
| **Aer** | Air | Light blue | Air, wind, breath, sound, movement through atmosphere |
| **Aqua** | Water | Cyan | Water, fluid, moisture, the sea, dissolution |
| **Ignis** | Fire | Red-orange | Fire, heat, combustion, energy release |
| **Terra** | Earth | Green | Earth, stone, mass, solidity, foundation |
| **Ordo** | Order | Silver-white | Order, regularity, structure, logic, law |
| **Perditio** | Entropy | Dark gray | Entropy, decay, destruction, chaos, dissolution |

### Compound Aspects — Tier 1 (two primals, 9 aspects)

| Aspect | Meaning | Parents | Description |
|---|---|---|---|
| **Vacuos** | Void | Aer + Perditio | Emptiness, nothingness, vacuum, hunger of space |
| **Lux** | Light | Aer + Ignis | Light, brilliance, visibility, illumination |
| **Potentia** | Power | Ordo + Ignis | Energy, potential, power, force |
| **Motus** | Motion | Aer + Ordo | Movement, speed, travel, kinetic force |
| **Gelum** | Cold | Ignis + Perditio | Cold, ice, frost, absence of heat |
| **Vitreus** | Crystal | Terra + Ordo | Glass, crystal, transparency, gemstone |
| **Victus** | Life | Aqua + Terra | Life, vitality, living essence, growth |
| **Venenum** | Poison | Aqua + Perditio | Poison, toxin, corruption of the body |
| **Permutatio** | Exchange | Perditio + Ordo | Exchange, trade, transformation, transmutation |
| **Tempestas** | Weather | Aer + Aqua | Weather, storm, rain, atmospheric force |

### Compound Aspects — Tier 2 (one primal + one tier 1, or two tier 1s, 14 aspects)

| Aspect | Meaning | Parents | Description |
|---|---|---|---|
| **Metallum** | Metal | Terra + Vitreus | Metal, ore, refined mineral, conductivity |
| **Mortuus** | Death | Victus + Perditio | Death, decay of the living, cessation |
| **Volatus** | Flight | Aer + Motus | Flight, soaring, levitation, weightlessness |
| **Tenebrae** | Darkness | Vacuos + Lux | Darkness, shadow, concealment, the unseen |
| **Spiritus** | Spirit | Victus + Mortuus | Spirit, soul, ghost, the animating force beyond flesh |
| **Sano** | Healing | Ordo + Victus | Healing, restoration, cure, mending of the body |
| **Iter** | Journey | Motus + Terra | Journey, travel, roads, pilgrimage |
| **Praecantatio** | Magic | Vacuos + Potentia | Magic, enchantment, the arcane arts |
| **Herba** | Plant | Victus + Terra | Plant, herb, vegetation, green growth |
| **Limus** | Slime | Victus + Aqua | Slime, mucus, viscous fluid, ooze |
| **Bestia** | Beast | Motus + Victus | Animal, beast, creature, wild nature |
| **Fames** | Hunger | Victus + Vacuos | Hunger, starvation, consumption, appetite |
| **Vinculum** | Trap | Motus + Perditio | Trap, binding, snare, imprisonment |
| **Alienis** | Eldritch | Vacuos + Tenebrae | Eldritch, alien, otherworldly, strange and unknowable |

### Compound Aspects — Tier 3 (higher compounds, 11 aspects)

| Aspect | Meaning | Parents | Description |
|---|---|---|---|
| **Auram** | Aura | Praecantatio + Aer | Aura, magical atmosphere, ambient enchantment |
| **Vitium** | Taint | Praecantatio + Perditio | Taint, corruption of magic, warped essence |
| **Arbor** | Tree | Aer + Herba | Tree, wood, bark, tall growth |
| **Corpus** | Flesh | Mortuus + Bestia | Flesh, body, physical form, corporeal matter |
| **Exanimis** | Undead | Motus + Mortuus | Undead, reanimated, death that moves |
| **Cognitio** | Mind | Ignis + Spiritus | Mind, thought, knowledge, intellect, cognition |
| **Sensus** | Sense | Aer + Spiritus | Sense, perception, awareness, feeling |
| **Humanus** | Human | Bestia + Cognitio | Human, civilization, sapience, culture |
| **Messis** | Crop | Herba + Humanus | Crop, cultivated plant, agriculture, harvest |
| **Lucrum** | Greed | Humanus + Fames | Greed, profit, avarice, wealth, desire for more |
| **Perfodio** | Mining | Humanus + Terra | Mining, excavation, delving, unearthing |

### Compound Aspects — Tier 4 (highest compounds, 8 aspects)

| Aspect | Meaning | Parents | Description |
|---|---|---|---|
| **Instrumentum** | Tool | Humanus + Ordo | Tool, instrument, implement, device |
| **Fabrica** | Craft | Humanus + Instrumentum | Craft, workshop, fabrication, construction |
| **Machina** | Machine | Motus + Instrumentum | Machine, mechanism, automation, gears |
| **Telum** | Weapon | Instrumentum + Ignis | Weapon, arms, offensive implement, blade |
| **Tutamen** | Armor | Instrumentum + Terra | Armor, shield, protection, defense |
| **Pannus** | Cloth | Instrumentum + Bestia | Cloth, fabric, textile, woven material |
| **Meto** | Harvest | Messis + Instrumentum | Harvest, reaping, gathering of crops |

**Total: 48 aspects** (6 primal + 42 compound)

**Adjacency compatibility graph:** pre-built at datapack reload as a `BitSet[]` indexed by aspect numeric ID. "Is aspect A compatible with aspect B?" resolves to a single bit check — used by the hex puzzle chain evaluator.

---

# `grimoire/` — Arcane Grimoire System

## `grimoire/GrimoireChassis.md`

**Single held book item, page-based.** The Grimoire is an `ItemStack` with NBT storing:
- Binding type (determines tier, Vis capacity, perk slots)
- Clasp type (determines efficiency, which subsystems are unlocked)
- Inscribed focus pages (multiple pages, one active at a time)
- Attuned constellation shard (optional, one slot)

The Grimoire is held in the main hand. Casting uses right-click. The open/closed state is visual — the book flips open during casting and channeling, closes when idle.

**Bindings (the book's cover/spine — determines capacity):**
| Binding | Material | Tier | Max Vis stored | Page slots | Perk slots |
|---|---|---|---|---|---|
| Leather | Leather + paper | 1 | 25 | 3 | 0 |
| Greatwood | Greatwood plank + iron clasps | 2 | 100 | 5 | 1 |
| Silverwood | Silverwood plank + thaumium clasps | 3 | 250 | 7 | 2 |
| Void Metal | Void Metal plates | 4 | 500 | 9 | 3 |
| Primordial | Primordial Pearl inlay | 5 | 1000 | 12 | 4 |

**Clasps (metal fittings on the cover — determines what systems the Grimoire can interact with):**
| Clasp | Systems enabled |
|---|---|
| Iron | Thaumaturgy only (basic focus pages) |
| Gold | + Alchemy & crucible priming |
| Thaumium | + Artifice & infusion activation |
| Void Metal | + Rituals, constellation channeling |
| Primordial | + Eldritch pages, dimensional work |

**Upgrade path:**
- Grimoire is iterative. Player can swap clasps at the Arcane Workbench without remaking the book.
- Bindings are replaced (consumed) when upgrading; inscribed pages, attuned shard, and all progress carry over.
- The Grimoire the player crafts at hour 2 is the same book they hold at hour 200 — thicker, heavier, more powerful.

**Vis draw:**
- Holding right-click channels Vis from the Weave into the Grimoire at a rate determined by clasp tier.
- Vis stored in the Grimoire is the pool consumed by active casts; channeling continuously refills from local chunks (within 3×3 chunk radius).
- If local Weave is depleted, the Grimoire can't refill — forces exploration to Vis-rich areas.

**Page selection:**
- Scroll wheel (while holding the Grimoire) cycles through inscribed pages. The active page name and icon display on the HUD.
- Shift+right-click opens the Grimoire GUI showing all inscribed pages in a book layout. Click a page to set it active, or drag pages to reorder.

**Implementation notes:**
- Item is one `Item` class with NBT-driven rendering (model override via ItemProperties — open/closed/casting states).
- Component registries for bindings, clasps, focus pages live in `grimoire/component/`.
- Tooltips show current configuration, active page, and remaining Vis.
- Book opening/closing animation via ARM_SWING + custom TransformType overrides.

---

## `grimoire/FocusPages.md`

**Focus Page = active ability inscribed onto a page.** Pages are crafted items that are consumed when inscribed into the Grimoire at the Arcane Workbench. Inscribed pages can be torn out (destroyed) to make room for new ones, but cannot be recovered.

**Crafting focus pages:** each page is crafted at the Arcane Workbench using 1 Blank Vellum (paper + Greatwood pulp) + the relevant Aspect Runes + Vis. Higher-tier pages require Infusion Altar crafting.

**Categories & examples:**

### Thaumaturgy pages
- Page of Fire — flame projectile, Ignis cost.
- Page of Shock — electric chain, Aer/Ignis.
- Page of Excavation — area mining, Terra/Perditio.
- Page of Warding — placeable protective barrier, Ordo/Terra.
- Page of Frost — ice projectile + slowness, Aqua/Perditio.
- Page of the Primal — primal aspect AoE burst (costs all six primals).

### Astral/ritual pages
- Page of Starlight — line attack powered by Resonance, not Vis.
- Page of Invocation — channels the equipped shard's ritual effect in an area.
- Page of Ignition — used to ignite multiblock rituals and activate the Infusion Altar.
- Page of the Celestial Gate — opens a temporary short-range teleport (channels Resonance heavily).

### Expanse/Eldritch pages
- Page of Rift Sensing — passive; pulses toward nearest dimensional anchor or structure of interest.
- Page of Anchoring — plants a recall point (one at a time; ignored inside Expanse layers that block teleport).
- Page of Banishment — finisher move effective on high-tier enemies; used to break tethers during encounters.

**Page upgrades:**
- Each focus page has three augment slots. Augments are small crafted items (inscribed as marginalia on the page) that modify: cost, range, projectile count, elemental conversion. Applied at the Arcane Workbench by placing the Grimoire + augment item.

---

## `grimoire/GrimoireAttunement.md`

**Constellation shard socket.** A fully-attuned Grimoire (Void Metal clasp minimum) gains one shard slot embedded in the cover.

- Slotting a Vicio shard: passive +20% movement speed, unlocks the Vicio branch of the perk tree.
- Slotting an Armara shard: passive damage reduction, defense branch.
- Slotting a Discidia shard: offense branch.
- Slotting an Aevitas shard: utility/regen branch.
- Additional shards unlocked from Expanse content (dark constellations, Weave-severing shards).

**Switchable:** player can swap shards at a Starlight Altar. Re-attuning takes one in-game night cycle.

---
# `crafting/` — Artificer Tiers

## `crafting/Tier1_ArcaneWorkbench.md`

**Multiblock:** none — single block.

**Recipe:** vanilla crafting table + Greatwood plank overlay + Arcane Stone base + iron clasps.

**Function:**
- Opens a 3×3 crafting grid with one additional **Grimoire slot** on the side.
- When a Grimoire is placed in the slot and the recipe requires Vis (marked recipes), the Grimoire's Vis pool is drained to power the craft.
- Accepts all standard Thaumaturgy recipes: Grimoire clasps, focus pages, basic baubles, Arcane Monocle, Marble Runes, charms, Greatwood/Silverwood processing.
- JEI integration shows Vis cost on Vis-requiring recipes.

**Block model:** reuses Thaumon's arcane workbench aesthetic; new block entity with inventory persistence.

---

## `crafting/Tier2_InfusionAltar.md`

**Multiblock** (manual build, no auto-assemble):

```
        [P]
    [P]  M  [P]       M = Runic Matrix (central block, Silverwood + Arcane Stone core)
        [P]           P = Pedestal (Arcane Stone with candle on top)

Optional 2nd ring (8-pedestal variant) for advanced recipes.
```

**Recipe execution:**
1. Player places the central "recipe target" item on the Matrix.
2. Player places component items on surrounding pedestals.
3. Player right-clicks Matrix with a Thaumium-clasp-or-higher Grimoire.
4. Infusion begins: Vis channels from surrounding Weave + Grimoire pool. Aspect requirements must match combined pedestal items' aspect profile.

**Stability system:**
- Each infusion has a **Stability** score (0–100, starts at 100).
- Stability decays during infusion based on:
  - Aspect mismatch (−10 per missing aspect unit)
  - Chunk Flux level (−0.1 per Flux point per second)
  - Insufficient nearby Vis (−5 per second)
- Stability increases by:
  - Nearby **Candles** (+2 each, max 8 candles)
  - Silverwood **pillars** built vertically next to pedestals (+5 each, max 4)
  - Amber **Batteries** within 16 blocks (+10 each, max 2)
- If stability hits 0: infusion fails. Central item destroyed. Pedestal items destroyed. Local chunk gains +200 Flux. Player gains +15 Personal Flux.
- If stability remains >0 at completion: recipe succeeds, central item replaced with result.

**Recipes unlocked here:**
- Aquafilter Charm, Tidewarded Plate components
- Silverwood Grimoire binding upgrades
- Void Metal clasps
- First ritual pedestals
- Constellation shard attunement items

---

## `crafting/Tier3_CelestialArtificer.md`

**Large multiblock:** roughly 11×11×7 footprint.

```
Top view of base (Y=0):
  . . E . A . A . E . .
  . E P . . . . . P E .
  E P S . . . . . S P E
  . . . . M . . . . . .
  A . . . . . . . . . A
  . . M . C . M . . .
  A . . . . . . . . . A
  . . . . M . . . . . .
  E P S . . . . . S P E
  . E P . . . . . P E .
  . . E . A . A . E . .

C = Celestial Core (central block, active)
M = Matrix Pedestal (collects convergent light)
S = Silverwood Pillar (channels Resonance upward, 5 high)
P = Arcane Stone Pillar (structural, 5 high)
E = Eldritch Stone block (corner anchors)
A = Amber Lens Housing (requires open sky)
```

**Function:**
- Top-tier infusions for Levity Sigil, Skybound Regalia, Primordial Grimoire bindings, endgame artifacts.
- Ritual priming: player places ritual focus items on Matrix Pedestals and activates with a Primordial-tier Grimoire.
- Only operable when attuned constellation is overhead AND sky is visible AND chunk Resonance > 300.
- Recipes may require specific constellation — e.g., Skybound Regalia requires Volatus constellation ascendant.

**Construction gates:**
- Eldritch Stone is not craftable until a progression drop unlocks the recipe (Eldritch Shards).
- Amber Lens Housing requires post-Layer 2 loot (Tideglass + Stormbound Plume).
- Celestial Core requires post-Layer 3 loot.
- So: the Celestial Artificer is *only completable after clearing all three Expanse layers*. Full structure is a progression summary in physical form.

**Implementation notes:**
- Multiblock validation via a dedicated pattern checker on right-click of Celestial Core.
- Invalid structure → core glows red, displays missing blocks as ghost outlines in GUI.
- Ritual animations handled via particle systems + custom shader for aspect-light convergence.

---

# `aura/` — The Weave Data Layer

## `aura/WeaveChunkCapability.md`

**Per-chunk data container.**

Fields:
- `float vis` (0–1000)
- `float resonance` (0–500)
- `float flux` (0–1000)
- `Map<Aspect, Float> aspectComposition` (distribution of Vis across primals; sums to `vis`)
- `Map<Aspect, Float> freeAspectAura` (aspect pool released by Alchemical Fire; all 48 aspects keyed, default 0. Soft cap 500 total. Decays 1%/hour passively, 5%/min above cap. Consumed by rune crafting, infusion, and rituals.)
- `long lastTickTimestamp`
- `boolean tainted` (flag for biome conversion)

**Serialization:** NBT-based, persisted with the chunk on save.
**Syncing:** only sent to client when an Arcane Monocle is worn or the chunk contains a Weave Confluence the player has line-of-sight to.

**Initial values on chunk generation:**
- Vis: 400–600 baseline, modified by biome.
- Resonance: 0 (fills from sky exposure at night).
- Flux: 0 in Overworld; 100–300 in Expanse zones depending on layer.
- Aspect composition biased by biome tag:
  - Forest → Herba-heavy
  - Desert → Ignis-heavy
  - Ocean → Aqua-heavy
  - Mountains → Terra-heavy
  - End → Vacuos/Tenebrae-heavy
  - Nether → Ignis/Perditio-heavy

---

## `aura/WeaveConfluence.md`

**Block entity spawned at the center of every chunk during worldgen** (not visible until scanned with an Arcane Monocle).

- Acts as the "node" concept — a physical interaction point for the chunk's Weave.
- Can be channeled directly by a Grimoire (higher Vis draw rate than ambient).
- Can be **purified** (by planting Silverwood saplings within 8 blocks → reduces chunk Flux cap and accelerates decay).
- Can be **tainted** (by excessive player flux dumps → adopts Tenebrae/Perditio heavy composition).
- Can be **drained** (by a specific endgame ritual that extracts its aspect composition into a crystal — destroys the node permanently; should feel like a tough moral choice).

**Visual:** a small floating orb of aspect-colored light, 3 block radius glow, particles matching its dominant aspect. Only rendered when player has an Arcane Monocle equipped or when it's taking damage.

---

## `aura/TaintBiome.md`

**Conversion:** chunks with sustained Flux at 800+ for 8 in-game days convert. Handled in a weekly scheduler tick.

**Visuals:**
- Fog (purple/violet, dense).
- Grass and leaves retextured (dark purple, wilted).
- Ambient particle spawns (drifting motes).
- Sky tint shifts violet.

**Mechanical effects:**
- Mobs spawning here get the corrupted wrapper automatically.
- Player passive Flux gain: +0.5/sec while inside.
- Crops don't grow.
- Vanilla passive mobs flee / despawn.

**Reversal:**
- Silverwood planting + Cleansing Ritual lowers chunk Flux below threshold; within 4 days the biome reverts.
- Players who fully cleanse a tainted chunk get a research bonus.

---

# `constellation/` — Astral-Style Progression

## `constellation/ConstellationRegistry.md`

**Constellations enumerated:**

Primary (major) — visible in the overworld:
- **Vicio** (travel — movement perks)
- **Armara** (defense — armor/toughness perks)
- **Discidia** (offense — damage/crit perks)
- **Aevitas** (life — regen/crop/utility perks)
- **Evorsio** (destruction — mining/building perks)
- **Lucerna** (light — visibility/navigation perks)

Weak (minor) — visible only under specific conditions:
- **Fornax** (fire, ritual boosts for fire effects)
- **Mineralis** (stone, ore affinity)
- **Bootes** (beast, tame/mount perks)
- **Horologium** (time, ritual duration extensions)
- **Octans** (water/sea)
- **Pelotrio** (paired, ritual area expansion)

Dark (Expanse-only) — visible only in the Aetherial Expanse:
- **Gelu** (unlocked post-Layer 1, cold/brittle damage)
- **Umbra** (unlocked post-Layer 2, Weave-sever perks)
- **Aetherialis** (unlocked post-Layer 3, flight/gravity mastery)

**Data:**
- Each constellation is a JSON under `data/aetherium/constellations/`.
- Visibility rules (moon phase, biome, dimension, altitude) in JSON.
- Visible star positions for each constellation — rendered as custom sky layer.

---

## `constellation/AttunementAltar.md`

**Multiblock:**
```
    [A] [A] [A]
  [A]  C  [A]      C = Attunement Core (Silverwood + Amber)
    [A] [A] [A]    A = Arcane Stone (ring)
```

**Use:**
1. Build altar under open sky.
2. Insert constellation paper (found in loot or derived from Starlight readings).
3. Stand on Core during the night when target constellation is overhead.
4. Beam-of-light event; player is now attuned to that constellation.
5. Attunement is permanent until switched (re-ritual required).

**Player capability:** one primary constellation attunement at a time. Stores it, exposes it to the perk tree, feeds into Grimoire shard crafting (crafted shard requires attuned player + constellation paper + Silverwood).

---

## `constellation/PerkTree.md`

**Tree structure:** radial — central "root" perk at attunement, branching into ~30 nodes per constellation.

**Progression currency:** perk points from XP levels (spent 1 point per node). Every level gained = 1 perk point.

**Example Vicio tree:**
- Root: +5% walk speed.
- Tier 1 (adjacent to root, costs 1 point each):
  - Fleet Foot (+5% walk)
  - Sure Step (no fall damage from own jump height)
  - Quick Swim (+15% swim speed)
- Tier 2 (require one tier 1):
  - Sprinter's Stride (sprinting costs no hunger)
  - Leap (+1 jump height)
  - Dasher (damage ×0.5 in first second after sprint start)
- Tier 3...
- Apex: Celestial Stride (brief flight burst on double-jump; costs Grimoire Vis)

**Switching attunement:** perk tree for the previous constellation is **dormant** (not refunded). Re-attuning to it later restores progress. This rewards long-term commitment to one path but allows exploration.

**UI:** fullscreen star-map style view, nodes connected by thin lines. Earned nodes glow; available nodes are bright; locked nodes are dim.

---

## `constellation/ConstellationPaper.md`

**Item:** discovery record.
- Generated in loot: ancient city chests, woodland mansion chests, Expanse ruins.
- Also derivable from **Starlight Infuser** (Tier 2 crafting using Aquamarine-equivalent gem + Silverwood).
- Required for attunement ritual and for crafting focus pages / shards tied to that constellation.

---

# `ritual/` — Multiblock Rituals (Astral-style)

## `ritual/RitualFramework.md`

**Shared pattern system:**
- A ritual is a pedestal ring (3×3 to 11×11) centered on a **Ritual Anchor** block.
- Ritual Anchor built from Arcane Stone + attunement crystal.
- Pedestals hold **Ritual Focus** items — special crafted tokens that define effect.
- Activated by the player with a Thaumium-clasp+ Grimoire.
- Once active, the ritual runs continuously as long as:
  - Local Resonance > focus's threshold
  - Sky is visible (most rituals)
  - Focus pages are not removed

**Ritual types (initial set):**
- **Growth Ritual** — crops/saplings in radius grow 5× faster.
- **Fertility Ritual** — tames/breeds in radius produce faster, higher yields.
- **Regeneration Ritual** — nearby players regenerate HP.
- **Weather Ritual** — clears/rains/snows in radius.
- **Cleansing Ritual** — reduces chunk Flux in radius.
- **Celestial Gateway Ritual** — opens the portal to the final endgame dimension (requires Heart of the Expanse as focus).

**Night/day constraints:**
- Many rituals only run at night.
- Some require specific constellations overhead.
- Performance scales with Resonance pool.

---
# `dimension/` — The Aetherial Expanse

## `dimension/Expanse.md`

**Single dimension, three vertical layers, progressive vertical challenge chain.**

**Portal:**
- Frame: Greatwood logs (obsidian-equivalent role).
- Ignition: Vis-Charged Focus (special item crafted at Arcane Workbench — one-shot, consumed on ignition).
- Stable portal block: Arcane Stone core inside the frame, glows with aspect-colored light.

**Dimension registration:**
- Custom dimension type: `aetherium:expanse`.
- Custom biome source: hard-zoned horizontal biomes (no gradient — clean borders for "Verdant" / "Sunken" / "Cloudspire" aesthetic coverage, though the gating is primarily vertical).
- Custom chunk generator: see `worldgen/ExpanseGenerator.md`.

**Layers are gated not by biome but by Y-altitude and environmental hazards:**
- Verdant Reaches: Y:63 to Y:200 (surface + inland).
- Sunken Vale: Y:120 (ocean surface) down to Y:-200 (trench floor).
- Cloudspire: Y:200 and above (gravity ceiling begins here).

**Respawn:** first entry anchors player's respawn-on-death to the Overworld portal exit, not inside the dimension. No beds work in the Expanse. Respawn Anchors fail.

**Teleport blocks:**
- Ender pearls: work normally above water and below Y:60 outside water; FIZZLE inside Tainted Water (no effect, consumed).
- Chorus fruit: works normally.
- Waystones / return scrolls: **disabled** by a dimension tag; datapack hook allows pack makers to override.

---

## `dimension/Layer1_VerdantReaches.md`

**Theme:** overgrown arcane ruins, aspect-saturated forests, high-Vis badlands. Heavy Herba / Victus / Terra aspects.

**Biome-like sub-zones:**
- **Ruined Canopy** — vast ancient forest with partial stone ruins beneath. Common loot: Rooted Sigils, constellation papers, aspect-rich tree stumps for scanning.
- **Sigil Plains** — open grasslands with standing stones and Earthbound Totems (encounter-summon structures).
- **Badlands of Ash** — high-Ignis zone with hostile mob density.

**Unique mob drops (native, not whitelist-dependent):**
- Rooted Sigil — summoning component.
- Verdant Essence — Tier 2 alchemy reagent.

**Structures:**
- Earthbound Totem — encounter-summon structure.
- Abandoned Research Camps — small structures with Thaumonomicon scraps, Greatwood-era tools, ancient scan records giving free research points.

**Hazards:**
- Mild ambient Flux (+50/chunk baseline).
- Corrupted mobs at 2× normal spawn density.

---

## `dimension/Layer2_SunkenVale.md`

**Theme:** impossibly deep oceans, bioluminescent trenches, sunken floating-island wreckage from Cloudspire above. Heavy Aqua / Perditio / Ordo aspects.

**Physical structure (critical design):**
- Ocean surface at Y:120.
- Average ocean floor at Y:-180.
- Deepest trenches reach Y:-200.
- **Warded Shell** (Aether-Warded Stone, unbreakable): 4–6 block lining around every body of water, following the 3D contour of the water volume. Generated during worldgen as a continuous membrane.
- **Only one way in, one way out: through the water surface.** No mining the floor, no tunneling from a beach.

**Movement is never restricted.** The horror of the descent is sensory, not mechanical. The player retains full speed, full swim rate, full jump, and full combat agency throughout. A fair fight is a scarier fight — nothing feels worse than dying because the game took your controls away.

**Three distinct depth zones, 100 blocks each:**

Each zone is visually darker than the one above it — achieved via three visual mechanisms layered together:

1. **Skylight falloff:** dimension generator caps sky-light propagation per zone (normal → dim → zero).
2. **Ambient fog color + density:** fog tints progressively darker violet per zone, density increases.
3. **Aether-Warded Stone tint:** the shell wall blocks themselves get progressively darker textures per zone (three variant blockstates selected by Y-level during worldgen), visually reinforcing descent as the walls around you literally darken.

| Zone | Y range | Effective visibility | Particle intensity | Tainted Water drain | Mobs | Sound |
|---|---|---|---|---|---|---|
| **The Shallows** | 120 → 20 | Clear blue-green, light level 10+ near surface trailing to ~6 | Mild violet motes drifting upward | ×1 | Common aquatic corrupted | Distant whale-song tones, gentle ambient |
| **The Dim** | 20 → -80 | Light level ~3, fog obscures beyond 8 blocks, night vision works but limited | Moderate; motes drift laterally and orbit player | ×2 | Larger predators | Low sub-bass pulses, irregular stingers |
| **The Black** | -80 → -180 | Light level 0, night vision suppressed, Vis-lantern gives 3-block radius | Peak; motes move *toward* player, water looks thick with suspension | ×3 | Silent flux-DoT attackers, rarer but deadlier | Whispering layer directionless, heartbeat, periodic false-hit sounds |
| **The Conduit's Chamber** | Sub-zone at trench floor of The Black | Conduit glow only, chamber is fully enclosed | Peak density, static-like haze | ×5 | None until encounter trigger | Silence except heartbeat + Conduit hum |

**Visual darkness implementation (technical):**
- Each zone's Aether-Warded Stone variant is a separate block (`aether_warded_stone_shallow`, `_dim`, `_black`) with the same mechanical properties but darker textures.
- Worldgen selects the variant based on the Y-level of the shell block placement.
- Client-side dimension fog shader interpolates between zone colors based on player Y.
- Night vision effect is canceled by a tick listener when player is inside Tainted Water and Y < -80.

**Tainted Water:**
- Fluid tag `aetherium:tainted_water` — visually similar to normal water with violet tint and faint swirling particles.
- Contact without filtration: −1 HP per 2 sec + **Temporary Personal Flux** +2 per sec while in contact (see `core/Flux.md` for revised Flux model).
- Aquafilter Charm: full immunity to HP damage and permanent Flux gain, but still accumulates Temporary Flux at reduced rate (+0.5/sec while worn). Durability: 1 per minute of immersion (see item entry).
- Tidewarded Plate: full immunity to all Tainted Water effects including Temporary Flux. No movement penalty — the armor is just protective, not cumbersome. Durability drains at 1 per 2 minutes of immersion.

**Abyssal Conduit (structure):**
- Generated one per trench, at the trench's lowest point.
- Large multiblock of Aether-Warded Stone + aspect-light pillars.
- Surrounded by Warded Shell — cannot be approached except by descending through the water column.
- Functions as an encounter-summon platform.
- Emits the chamber's only stable light source (post-encounter = pacified; pre-encounter it pulses erratically).

**Night Vision suppression implementation:**
- Dimension flag; potion effect applies but is canceled by environmental flag below Y:0.
- Explicitly documented in Thaumonomicon research entry so players aren't confused.

**Vis-Lantern item:**
- Greatwood + iron + glass + Amber dust.
- Placeable on Aether-Warded Stone walls.
- Burns Grimoire Vis (requires Grimoire in off-hand to "refuel" a lantern when placed; each refuel = 30 minutes real-time).
- 5-block radius light at baseline, 2-block below Y:-100.
- Can be broken by corrupted mobs; breaking a refueled one gives a small Vis drop back.

**Minimap/mod blocks:**
- Dimension added to `aetherium:blocks_minimap` tag. Supported minimap mods (Xaero's, JourneyMap, etc.) respect the tag and blank the map.
- No waystones. No return scrolls. No ender chests below Y:60.

**Audio:**
- Zone-specific ambient loops registered as standard Minecraft ambient sounds per biome; engine handles cross-fading, not custom code.
- Heartbeat / whispers are client-only, triggered by local Temp Flux threshold crossings — one sound event per crossing, not a loop.

---

## `dimension/Layer3_Cloudspire.md`

**Theme:** floating islands ascending through cloud layers into thin starlit air. Dimension's sky meets the Weave directly — constellations visible here that never appear overworld. Heavy Aer / Lux / Auram aspects.

**Physical structure:**
- First floating islands at Y:200–240.
- **Gravity ceiling begins at Y:200** — gravity multiplier increases linearly.
- Y:200–250: gravity ×2. Jumping works but reduced; fall damage increases.
- Y:250–300: gravity ×3. Jumping nearly useless. Flight perks consume Vis at 2× rate.
- Y:300+: gravity ×4. Lethal for unprepared players — a fall from island to island is almost always fatal.
- Top cluster at Y:320+ (Celestial Anchors).

**Gear required:**
- Levity Sigil (Curios charm) — negates heightened gravity while Grimoire has Vis. Drains Grimoire Vis at 1/sec above Y:200, scaling with altitude.
- Skybound Regalia (full armor) — permanent gravity negation, no Vis drain. Requires Tidewarded Plate as crafting base (rewards players who went heavy at Layer 2).

**Features:**
- **Celestial Anchors** — encounter platforms; one per sky region, at Y:320+.
- **Sunken Wreckage fragments** — some Cloudspire islands appear "broken" from below, with trails of stone that fell into the Sunken Vale. Connects the layers visually.

**Unique mob drops:**
- Stormbound Plume (summoning component).
- Skyclad Feather (craft material for Levity Sigil).

**Hazards:**
- Gravity kills unprepared.
- Falls between islands are almost always fatal even with Levity Sigil once Vis drains.
- Flying corrupted mobs (whitelist-driven) including dragons if pack has them.

**Triumphant tone contrast:**
- Bright, open, starlit sky. Music shifts from horror (Layer 2) to orchestral ascension.
- Constellations visible during daytime through thin atmosphere.
- Reward for surviving the ocean: the most beautiful area in the mod.

---

## `dimension/EndgameArc.md`

**After Heart of the Expanse, the final act.**

**Unlock:** research "Celestial Gateway" at Celestial Artificer. Ritual uses Heart of the Expanse as the central focus.

**Destination: The Inner Weave.** A small pocket dimension (single biome, single structure) that exists "inside" the Weave itself. Visually: impossible geometry, aspect-colored sky, the player's own shadow behaves strangely.

**Final encounter:** the Eldritch Guardian — a mirror of the player's own research history. Its HP and attack pattern scale with the player's research completion. A player who rushed will face an easier but less interesting encounter; a player who completionist-scanned everything will face the full challenge.

**Drop:**
- Primordial Core artifact (permanent bauble — grants passive +25% Vis regen, +1 perk slot on attuned Grimoire).
- Research unlock: "Aetheric Ascendancy" — allows crafting a Phoenix-equivalent one-shot resurrection item.
- Permanent title/effect rather than item treadmill. The endgame is a *state*, not a loot cycle.

**Design philosophy:** the endgame isn't another bigger fight. It's a narrative and mechanical closure that feeds back into overworld play — permanent buffs that change how the rest of the game feels.

---
# `entity/` — Corrupted Mob System

## `entity/CorruptedWrapper.md`

**Concept:** rather than adding unique Expanse mobs, the mod dynamically applies a "Corrupted" treatment to existing mobs on spawn inside the Expanse (or in Tainted biomes in the Overworld).

**Applied via `EntityJoinLevelEvent` listener:**
1. Check entity's dimension.
2. If in Expanse or tainted chunk:
   - Attach `CorruptedAttribute` capability to entity.
   - Buffs: +30% HP, +15% damage.
   - Visual: particle aura (violet motes), texture darken shader.
   - Loot: +chance to drop Tainted Essence.
   - Attack effect: 10% chance per hit to add +1 to player's Personal Flux pool.
3. Store "origin mod" and "entity type" so players with scanner gear get themed scan entries.

**Cleansed drops:**
- If player kills a corrupted entity with a Silverwood-bound Grimoire equipped, drop table swaps to "cleansed" variant — doubled material drops, includes a small amount of Pure Essence (alchemy reagent).

---

## `entity/MobWhitelist.md`

**Data-driven spawn whitelist per Expanse layer.**

Location: `data/aetherium/spawn_whitelists/`
Files:
- `verdant_reaches.json`
- `sunken_vale.json`
- `cloudspire.json`

Structure:
```
{
  "fallback": [
    { "entity": "minecraft:husk", "weight": 20 }
  ],
  "whitelist": [
    { "entity": "alexsmobs:bone_serpent", "weight": 5, "min_light": 0, "max_light": 7 },
    { "entity": "iceandfire:cockatrice", "weight": 3 },
    { "entity": "twilightforest:redcap", "weight": 8 }
  ]
}
```

**Behavior:**
- On Expanse chunk load, mob spawn entries for that biome are replaced with the whitelist's intersection with present mods.
- If a whitelisted mod is absent, entry is silently skipped.
- If no whitelist entries resolve, the fallback list is used (ensures dimension always spawns mobs).

**Player-authored tags (`data/aetherium/tags/entity_types/`):**
- `aquatic_corrupted` — any entity in this tag can drop Deep-Tainted Scale in the Sunken Vale.
- `flying_corrupted` — any entity in this tag can drop Stormbound Feather in the Cloudspire.
- `terrestrial_corrupted` — verdant drops.

Pack makers extend by writing datapack tag additions. No Java changes required.

---

# `block/` — All Blocks

## `block/ThaumonIntegration.md`

**Thaumon's blocks are not reimported — this mod registers its own blocks that mechanically consume and depend on Thaumon's.** The design choice: if Thaumon is present, its blocks are used as functional inputs; if absent, this mod registers fallback decorative versions.

### Functional promotions (what each Thaumon block DOES in this mod):

| Thaumon block | Mechanical role |
|---|---|
| Greatwood log/plank/sapling | Tier 2 Grimoire binding crafting. Arcane Workbench body. Greatwood saplings grow into aura-generating trees — each mature tree adds +0.2/sec passive Vis regen to its chunk. Portal frame block for Expanse entry. |
| Silverwood log/plank/sapling | Tier 3 Grimoire binding. Infusion Altar pillars. Silverwood saplings grow into pure-aura trees — +0.5/sec Vis regen and −1 Flux/sec to chunk within 8 blocks. Required for Cleansing Rituals. |
| Arcane Stone (plain/bricks/tiles/pillars) | Infusion Altar pedestal base. Artificer structural blocks. Ritual Anchor chassis. Block-replacement recipes: vanilla stone + aspect dust + infusion → arcane stone tier. |
| Eldritch Stone | Dimensional portal frames (for the endgame Celestial Gateway). Multiblock structural for Celestial Artificer corners. Recipe LOCKED behind progression gate (Eldritch Shards). |
| Amber blocks | Vis batteries — place adjacent to Infusion Altar as stability boosters (each Amber Battery stores 200 Vis; recharged by ambient Weave). Amber Lens Housing for Celestial Artificer (requires open sky). |
| Tabletop candle | +2 infusion stability each; +research speed in 8-block radius. |
| Tabletop tome | Functional bookshelf — counts for enchanting table bonus and Thaumonomicon interaction. Placeable on tables. |
| Tabletop alchemical glassware | Cosmetic + functional: acts as an aspect-display when placed on an Alchemy Crucible's adjacent tile, showing which aspects are in the crucible. |

### Fallback registration:
- If Thaumon is absent at load time, this mod registers its own `greatwood_log`, `silverwood_log`, `arcane_stone`, `eldritch_stone`, `amber_block`. Textures are originals.
- Detection: check mod loaded state in `FMLCommonSetupEvent`. Register conditionally.
- Documentation prompts users to install Thaumon for full decorative coverage.

---

## `block/AetherWardedStone.md`

**Unbreakable shell block for Sunken Vale worldgen.**

- Hardness: -1.0f (matches bedrock).
- Blast resistance: 3600000 (matches bedrock).
- Tagged with `minecraft:features_cannot_replace` and custom `aetherium:unbreakable`.
- Texture: dark gray with faint violet aspect-vein pattern.
- Placeable by creative only.
- Generates continuously as a 4–6 block shell around all water bodies in the Sunken Vale during worldgen.
- Explicit override in the mining logic to ignore Efficiency enchants, modded pickaxes, and any "bedrock breaker" mechanics — checks for the unbreakable tag rather than hardness alone.

---

## `block/AbyssalConduit.md`

**Encounter-summon platform. Multi-block structure.**

- 3×3×2 footprint.
- Central core block (Aether-Warded Stone variant, glows faintly).
- 4 aspect-pillars at corners (colored: Aqua, Perditio, Vacuos, Ordo).
- Right-clickable with the appropriate summon item to trigger the encounter.
- Passive: emits the Conduit's Chamber illumination (pre-encounter = erratic pulsing; post-victory = steady white).
- Found only in Sunken Vale worldgen — one per trench.

---

## `block/RunicMatrix.md` & `block/CelestialCore.md`

**Central blocks of Tier 2 / Tier 3 crafting multiblocks.** Each is a block entity tracking:
- Held item (the infusion target / ritual focus).
- Linked pedestals (discovered on activation via multiblock pattern match).
- Current Stability / active recipe state.
- Syncs to client for GUI rendering.

Full behavior described in `crafting/Tier2_InfusionAltar.md` and `crafting/Tier3_CelestialArtificer.md`.

---

## `block/AlchemicalFire.md`

**Purple-flame campfire for extracting aspects from items.**

**Model:** vanilla campfire model with custom purple flame textures (`alchemical_fire.png`, `alchemical_fire_log_lit.png`). Emits violet particles instead of orange. Ambient purple light level 12.

**Recipe:** crafted at Arcane Workbench — 3 Arcane Stone + 1 Nether Wart + 1 Soul Sand + Grimoire (costs 25 Vis).

**Mechanic — Aspect Extraction:**
- Right-click or toss any item onto the fire. The item is consumed (destroyed).
- The fire selects 33% of the item's aspects at random (rounded up, minimum 1 aspect) and releases them into the chunk's Weave as free aura.
- Released aspects are added to the chunk's `WeaveChunkCapability.aspectComposition` map, increasing the chunk's ambient aspect pool.
- The remaining 67% of aspects are lost (burned away).
- Visual: each released aspect produces a colored mote that rises from the flame and disperses — the mote color matches the aspect color. Unreleased aspects produce grey/dark smoke.
- Stacks are consumed one item at a time (one item per 2 seconds while a stack sits on the fire).

**Chunk Aura Budget:**
- Each chunk has a soft cap of 500 total free aspect points in its aura pool.
- Burning items beyond this cap still works but excess aspects decay at 5%/minute back toward the cap.
- Free aspect aura in a chunk is available for: rune crafting (see `item/AspectRunes.md`), infusion recipes, and ritual fuel.
- Aspects in chunk aura decay passively at 1%/hour (slow enough to stockpile before a crafting session, fast enough that chunks don't stay saturated forever).

**Interaction with Weave:**
- Alchemical Fire does NOT generate Vis — it only adds typed aspects to the chunk pool.
- Heavy use generates Flux: each item burned adds +1 Flux to the chunk. Burning high-aspect items (6+ aspects) adds +3 Flux instead.

**Block states:** `lit` (true/false), `signal_fire` (true/false — soul sand underneath intensifies the purple glow and increases extraction rate to 50% of aspects instead of 33%).

---

## `block/AlchemyCrucible.md`

**Thaumcraft-style essentia brewing.**

- Cauldron-like, placed over a heat source (Alchemical Fire, campfire, or furnace/lava).
- Dropping items in while heated decomposes them into their aspects (using the aspect data from core/).
- Essentia fills the crucible as colored liquid (visualized via block texture state).
- Player dips empty Phials (crafted bottle items) in the crucible to extract specific aspects.
- Overfilling with mismatched aspects causes Flux splash in chunk.
- Aspects in a crucible can be consumed by Artifice recipes on adjacent Infusion Altar.
- When placed over an Alchemical Fire specifically, decomposition is 100% efficient (all aspects extracted into the crucible, none lost). Over a regular campfire, 15% of aspects are lost as Flux.

---

## `block/ResearchTable.md`

**Greatwood workstation for the hex puzzle.**

- Requires a paper stack + quill + ink in inventory to start research.
- Right-click opens the hex grid GUI.
- Storage slot for unfinished research notes (lets players start a topic, leave, return later).

---
# `item/` — Items

## `item/Grimoire.md`
Unified Arcane Grimoire item. NBT-driven component system with inscribed focus pages. See `grimoire/GrimoireChassis.md`.

## `item/ArcaneMonocle.md`

**Curios head slot item. Passive always-on aspect scanner.** Replaces the Thaumometer concept entirely — no need to hold anything, just wear it.

**Model:** a small brass-and-amethyst monocle rendered on the player's face (Curios `head` slot). Enchanted-item glint. Custom first-person overlay: faint purple lens tint on the left edge of the screen when worn.

**Recipe:** Arcane Workbench — 1 Amethyst Shard + 1 Gold Ingot + 1 Glass Pane + 1 Greatwood Plank + Grimoire (costs 20 Vis). Unlocked after researching "Aspect Theory."

**Core Behaviors:**

1. **Passive world scanning:** While worn, any block, entity, or dropped item under the player's crosshair displays an aspect overlay tooltip showing all of that target's aspects with icons and quantities. No click required — just look at it. First scan of a new target grants research points (2–10 per aspect present). Already-scanned targets show aspects but grant 0 points.

2. **Inventory scanning:** While worn, hovering over any item in any inventory screen (player inventory, crafting grid, creative mode) appends that item's full aspect list to its tooltip. This is the primary way players evaluate what items are worth burning in Alchemical Fire.

3. **Container scanning:** When the player opens any storage container (chest, barrel, shulker box, hopper, etc.) while wearing the monocle, ALL items in that container show their aspects in their tooltips. A summary panel on the right side of the container GUI shows the total combined aspect breakdown of everything in the container.

4. **Moon phase display:** A small moon phase icon renders in the corner of the HUD while worn. Shows current phase name on hover. The chunk's current Resonance value displays next to it at night.

5. **Chunk aura readout:** A subtle HUD bar along the bottom-left shows the local chunk's free aspect aura pool (from Alchemical Fire burns), Vis, Resonance, and Flux. Toggled on/off with a keybind (default: H).

**Upgrade tiers:**
- Basic (Greatwood frame): passive scanning, inventory/container tooltips, moon phase.
- Silverwood-upgraded: adds numeric chunk Weave values and 3×3 chunk heatmap overlay (toggled with keybind).
- Primordial: full 3×3 Weave heatmap, live Flux readings, can detect buried structures (Conduits, Totems) as ghost outlines through terrain, and highlights items with rare aspects in containers with a golden glow.

**Implementation notes:**
- Registered as a Curios `ICurioItem` for the `head` slot.
- Tooltip injection via `ItemTooltipEvent` — checks `CuriosApi.getCuriosHelper().findEquippedCurio()` for monocle presence, then appends aspect lines.
- World scanning via `RenderGuiOverlayEvent` — raycasts from player eye position, resolves target block/entity, renders aspect overlay if monocle is equipped.
- Container scanning hooks into `ContainerScreenEvent.Render` to inject aspect data into slot tooltips and render the summary panel.
- Moon phase reads from `level.getMoonPhase()`. Resonance reads from `WeaveChunkCapability`.
- Chunk aura readout reads from `WeaveChunkCapability.freeAspectAura` map.

---

## `item/AspectRunes.md`

**Craftable rune items — one per aspect (48 total).** Used as research materials, ritual components, and crafting reagents.

**Appearance:** each rune uses the `marble_rune.png` base texture with the aspect's unique symbol overlaid in the aspect's color.

**Recipe — Simple Infusion:**
- Place a Marble Rune (blank) on the Infusion Altar matrix.
- No pedestal items required.
- The infusion draws the target aspect directly from the chunk's free aspect aura pool (from Alchemical Fire burns).
- Cost: 10 points of the target aspect from chunk aura + 5 Vis from Grimoire.
- Player selects which aspect to imbue via a radial menu that appears when activating the matrix (only aspects with sufficient chunk aura are selectable).
- Infusion takes 3 seconds, produces one typed Aspect Rune.

**Marble Rune (blank) recipe:** Arcane Workbench — 1 Arcane Stone + 1 Lapis Lazuli + Grimoire (costs 5 Vis). Produces 4 blank runes.

**Uses:**
- Research: placed on the Research Table as marbles for the hex puzzle (each rune acts as a pre-typed marble of its aspect, saving research points).
- Infusion recipes: many Tier 2+ recipes accept Aspect Runes on pedestals as a way to provide specific aspects without needing to find items that naturally contain them.
- Ritual fuel: certain rituals consume Aspect Runes placed on ritual pedestals.
- Trading: villager Thaumaturge profession buys/sells Aspect Runes.

## `item/VisChargedFocus.md`
Consumed to ignite the Expanse portal. Crafted at Arcane Workbench with 4 aspect dusts (one of each primary group).

## `item/AquafilterCharm.md`
Curios charm slot. Grants water breathing + Tainted Water HP-damage immunity. Reduces Temporary Flux gain from Tainted Water by 75% (does not fully block it). Visual: a small amber pendant.

**Durability:** 1440 (24 in-game hours of continuous immersion). Loses 1 durability per minute of active Tainted Water contact (ticked every 1200 ticks while submerged in the tainted fluid tag). Does not lose durability out of water. Breaks on zero durability and must be recrafted at Infusion Altar. Repairable via Infusion Altar (not anvil) using Silverwood + Amber + Ordo aspect.

**No movement penalty.** The charm is a filter, not a burden — player swims and walks at full speed while worn.

## `item/TidewardedPlate.md`
Full armor set:
- Helmet with water-filtering visor
- Chestplate with integrated aspect-channel vents
- Leggings with pressure-resistant plating
- Boots with silverwood soles

All pieces infused at Infusion Altar. **Set bonus:** full Tainted Water immunity (HP + Temporary Flux both fully blocked) + water breathing + enhanced underwater vision (slight brightness boost up to zone cap, does not override The Black's darkness).

**No movement penalty.** The armor is a sealed suit, not encumbering — full walk speed, swim speed, jump height.

**Durability:** standard armor durability (diamond-tier) + each piece drains an additional 1 durability per 2 minutes of Tainted Water immersion. Durability pool survives contact; pieces only break from combat + prolonged exposure. Repairable at Infusion Altar with Thaumium + Aqua/Ordo aspects.

## `item/LevitySigil.md`
Curios charm slot. Negates Cloudspire gravity while Grimoire has Vis.

## `item/SkyboundRegalia.md`
Full armor set built on Tidewarded base. Replaces Tidewarded when upgraded. Permanent gravity negation + water breathing + aspect damage reduction.

## `item/VisLantern.md`
Placeable light source for Sunken Vale descent. Fueled by Grimoire Vis. Breaks if attacked by corrupted mobs.

## `item/ConstellationPaper.md`
Discovery record. Loot-generated or crafted. Required for attunement and shard crafting.

## `item/ProgressionItems.md`
Layer progression items (Heart of the Expanse and associated chain keys). Non-stackable, no despawn flag set. Each is a quest item gating access to the next layer's content.

## `item/Phial.md`
Essentia storage. Six primary phial types (one per primal aspect); compound aspects stored in labeled variants.

## `item/AspectDust.md`
Crafting reagent derived from crucible work. One per aspect.

## `item/TaintedEssence.md` / `item/PureEssence.md`
Alchemy reagents from corrupted/cleansed mob kills.

---
# `worldgen/` — World Generation

## `worldgen/ExpanseGenerator.md`

**Custom chunk generator for the Aetherial Expanse.**

**Noise:**
- Terrain noise similar to Overworld but amplified vertically.
- Cloudspire islands generated via a separate sparse noise above Y:200.

**Biome placement:**
- Horizontal biome zones (Verdant sub-zones on land).
- Vertical layer logic handled not by biome but by structure and environmental flags (gravity, Tainted Water).

**Sunken Vale generation:**
1. Identify ocean/lake masks (Voronoi regions or similar).
2. For each water volume, compute 3D bounding shell.
3. Fill shell perimeter (4–6 blocks thick) with Aether-Warded Stone.
4. Inside shell, fill with Tainted Water fluid.
5. At lowest point of each trench, place Abyssal Conduit structure.

**Cloudspire generation:**
- Start at Y:200. Place floating island blobs using 3D simplex noise, decreasing density upward.
- At Y:320+, place sparse Celestial Anchor structures.
- Generate Sunken Wreckage debris trails between islands.

**Ruin & structure generation:**
- Earthbound Totems: surface placement in Verdant Sigil Plains sub-zone, one per ~5000 blocks.
- Research Camps: rare Verdant surface structures with loot.
- Abyssal Conduit: exactly one per trench.
- Celestial Anchor: one per floating-island cluster in Cloudspire.

---

## `worldgen/OverworldTaint.md`

**Tainted biome spread** (see `aura/TaintBiome.md`) is the only modification to Overworld generation.

No ore generation additions (Aetherium items are crafted/ritualed, not mined).

---

# `compat/` — Cross-Mod Integration

## `compat/JEI.md`
- Custom recipe categories: Arcane Workbench, Infusion Altar, Celestial Artificer, Alchemy Crucible.
- Shows Vis cost, required aspects, stability minimum.
- Displays aspects on items via an overlay tooltip layer.

## `compat/Curios.md`
- Aquafilter Charm, Levity Sigil, Primordial Core → Curios charm slots.
- Required dependency.

## `compat/WhitelistLoader.md`
- Reads `data/aetherium/spawn_whitelists/` JSONs on datapack reload.
- Resolves entity type presence via Forge registries.
- Builds per-dimension spawn table override.

## `compat/Patchouli.md` (optional)
- Fallback Thaumonomicon renderer if custom GUI is disabled.
- For v1: can be shipped alongside. For v2+: custom GUI mandatory.

---

# Data Directories

## `data/aetherium/`

```
data/aetherium/
├── aspects/
│   ├── tree.json                 # full aspect graph (48 aspects)
│   └── affinities/*.json         # per-item/block/entity aspect assignments
├── research/
│   ├── topics/*.json             # research entries + hex puzzle configs
│   └── categories.json
├── constellations/
│   ├── vicio.json, armara.json, ... (all 15)
│   └── star_maps/*.json          # visual star positions
├── spawn_whitelists/
│   ├── verdant_reaches.json
│   ├── sunken_vale.json
│   └── cloudspire.json
├── rituals/*.json                # ritual focus definitions
├── tags/
│   ├── blocks/unbreakable.json
│   ├── entity_types/aquatic_corrupted.json
│   ├── entity_types/flying_corrupted.json
│   └── entity_types/terrestrial_corrupted.json
├── loot_tables/                  # encounter drops, structure loot
├── recipes/                      # crafting, infusion, celestial
└── worldgen/
    ├── biome/*.json              # expanse biomes
    ├── dimension/expanse.json
    ├── dimension_type/expanse.json
    └── structure/*.json          # earthbound_totem, abyssal_conduit, celestial_anchor
```

---

## `assets/aetherium/`

```
assets/aetherium/
├── blockstates/
├── models/{block,item}/
├── textures/
│   ├── block/ (arcane_stone, eldritch_stone, aether_warded_stone, runic_matrix, celestial_core, abyssal_conduit, ...)
│   ├── item/ (Grimoire tiers, focus pages, charms, phials, essence, ...)
│   ├── entity/ (corrupted overlay, constellation particles, ...)
│   ├── gui/ (thaumonomicon UI, hex grid, perk tree, infusion stability meter, ...)
│   ├── environment/ (expanse sky layers, constellation star maps, tainted fog, ...)
│   └── particle/ (aspect-colored motes, flux corruption, vis channel, ...)
├── lang/en_us.json
├── sounds.json
└── sounds/
    ├── ambient/ (expanse layer-specific loops, depth zone audio)
    ├── grimoire/ (channel, cast, misfire)
    └── ritual/ (infusion success/fail, ritual ignite)
```

---
# Implementation Priority (Build Order)

**Every version below has a mandatory performance gate before release. If Spark/VisualVM/JFR shows this mod on the profiler at idle, the version is not shipped. No feature ships ahead of its perf budget.**

**v0.1 — Foundation:**
1. Weave capability + chunk data
2. Aspect registry + scanning
3. Grimoire chassis (leather/greatwood tiers, iron/gold clasps, 3 focus pages)
4. Arcane Monocle + Alchemical Fire + Aspect Runes
5. Arcane Workbench
6. Thaumonomicon basic UI (tab list, no hex puzzle yet)
7. Thaumon integration for Greatwood/Arcane Stone

**v0.2 — Research & Alchemy:**
1. Hex puzzle research UI
2. Research data format + 15 starter topics
3. Alchemy Crucible + Phials + aspect dusts
4. Infusion Altar + stability system
5. Flux (personal + chunk) + visual effects

**v0.3 — Constellations:**
1. Constellation registry + sky rendering
2. Starlight Altar + attunement
3. Perk tree UI + 4 primary constellations fully implemented
4. Grimoire attunement shard slot

**v0.4 — The Expanse (Layer 1):**
1. Dimension registration
2. Verdant Reaches generation
3. Corrupted mob wrapper
4. Mob whitelist loader
5. Layer 1 encounter + Eldritch Stone unlock flow

**v0.5 — The Descent (Layer 2):**
1. Sunken Vale generation + Aether-Warded Stone (three zone variants: shallow, dim, black)
2. Three-zone environmental system (skylight caps, fog shader interpolation, zone-gated night vision suppression)
3. Temporary Flux pool + hallucinated mob client entity system
4. Vis Lantern
5. Aquafilter Charm (with durability) + Tidewarded Plate
6. Abyssal Conduit + Layer 2 encounter

**v0.6 — The Sky (Layer 3):**
1. Cloudspire generation + gravity system
2. Levity Sigil + Skybound Regalia
3. Celestial Anchor + Layer 3 encounter
4. Celestial Artificer multiblock

**v0.7 — Endgame:**
1. Heart of the Expanse + Celestial Gateway ritual
2. Inner Weave dimension
3. Eldritch Guardian encounter
4. Primordial Core artifact
5. Eldritch research tab

**v1.0 — Polish:**
1. Rituals framework + full ritual set
2. Golemancy (optional subsystem)
3. Full JEI integration
4. Patchouli fallback book
5. Tainted biome Overworld spread
6. Custom encounter music, ambient audio pass
7. Balancing, datapack hooks, modpack maker documentation

---

# Design Constraints & Principles

0. **Performance is feature-zero.** Every system is designed to be invisible on a profiler at idle and minimally impactful when active. Targets: < 0.5ms avg / < 2ms peak per tick on populated servers. No system ships without proving it meets the performance contract in the Performance Manual. If it shows up on Spark, it doesn't ship.

1. **One cohesive fiction.** Everything is "the Weave" — no separate magical systems, no bolted-on lore. Thaumcraft, Astral, and AbyssalCraft references are aesthetic inspirations, not distinct codebases.

2. **One Grimoire, one progression.** The Grimoire the player crafts at hour 2 is the book they hold at hour 200 — upgraded, never replaced.

3. **Thaumon blocks have jobs.** Every Thaumon block is mechanically required somewhere. Decorative-only is not acceptable.

4. **Horror through mechanics, not jumpscares.** The Sunken Vale descent is the mod's horror peak. Three 100-block zones of progressively darker Warded Shell, no escape tunnel, hallucinated mobs from Temporary Flux, and an encounter at the bottom.

5. **Never take control from the player.** The player's movement speed, swim rate, jump height, and combat responsiveness are never reduced by the mod. No pressure speed penalties, no encumbrance from armor, no "you feel heavy" debuffs. Horror and challenge come from the environment, not from crippling inputs. A fair fight is a scarier fight.

6. **Cross-mod friendly.** Corrupted wrapper + whitelist system means the Expanse's mob roster adapts to whatever pack the mod is installed in.

7. **Data-driven where possible.** Aspects, research, constellations, whitelists, rituals — all JSON. Pack makers can extend without Java code.

8. **No Abyssal flavor.** The corruption is Thaumcraft-flavored Flux, not Lovecraftian dread. No Shoggoths, no Necronomicon, no tentacles — corrupted violet mists, warped aspects, Weave-thinning.

9. **Vertical progression in a single dimension.** Three encounters in one dimension with environmental gates is cleaner than three dimensions with copy-paste portal frames.

10. **The player must return to the workbench.** Every piece of dimensional progression requires returning to the Overworld crafting loop. Dimension and magic system are one loop, not two.

11. **Endgame is a state, not a treadmill.** The Primordial Core is a permanent character upgrade that changes how the rest of the game feels, not the start of another loot grind.

---
# Performance Manual

**This mod MUST NOT appear on a TPS profiler. Every system is designed for zero overhead at idle and minimal overhead when active.**

This is not a guideline. Every file in this document inherits these rules. Any design decision that violates them is rejected regardless of feature appeal.

## Core Rules

### 1. Nothing ticks that doesn't need to
- **No `@SubscribeEvent` on `ServerTickEvent` or `LevelTickEvent`** for iterative work. Those fire 20×/sec across every loaded level.
- Use **scheduled tick queues** (`ServerLevel#getBlockTicks`) or **distance-gated tick conditions** instead.
- **Block entities that don't need ticks must not implement `BlockEntityTicker`.** Static-state entities (most decorative/storage) never tick.
- Player capabilities tick via a single mod-wide `PlayerTickEvent.Post` dispatcher — one event handler total, not one per capability.

### 2. Chunk work is lazy and batched
- Weave state does NOT tick per-chunk every tick. It uses **lazy evaluation**: values are stored with a `lastUpdateTime` timestamp and recomputed on read using elapsed-tick delta math. Zero idle cost.
- Aspect decomposition, Flux decay, Vis regen: all math functions of `(currentTick - lastTick)`, computed on access only.
- Chunk-level batch operations (taint spread, biome conversion) run on a **weekly scheduler** (once per 168000 ticks) via `ServerLevel` scheduled tasks, not per-tick polling.
- Loaded chunks are iterated **once per minute maximum** for any whole-world sweep.

### 3. Zero allocation in hot paths
- No `new` calls in any tick handler. Pre-allocate pooled buffers for particle emitters, packet builders, math vectors.
- Aspect lookups use `Object2FloatOpenHashMap` (fastutil) with pre-sized capacity — never `HashMap<Aspect, Float>`.
- Capability reads cache the `LazyOptional` handle on first access; never call `.getCapability()` in a tick loop.
- String concatenation for tooltips/logs uses `StringBuilder` reuse, never `String +`.

### 4. Client/server separation is absolute
- **Hallucinated mobs are client-side particle-system entities, NOT `LivingEntity` instances.** Zero server packets, zero server tick cost. Server doesn't know they exist.
- Fog/shader/visual effects read local player state only; no server round-trips.
- HUD rendering reads cached capability values updated by opportunistic sync (on meaningful change), not polled every frame.
- Client visuals tick only on the client render thread, gated on distance-to-camera and frustum culling.

### 5. Networking is event-driven, not polled
- Capability sync packets fire **only on value change beyond threshold** (e.g., Personal Flux changes by ≥1, not on every sub-integer float update).
- Chunk Weave data syncs only when an Arcane Monocle is worn AND the chunk is within 4-chunk radius of the player AND the value changed meaningfully.
- Coalesced packets: all capability changes for a single player in a single tick bundle into one packet.

### 6. Worldgen overhead is paid once, at generation
- Aether-Warded Shell, Weave Confluences, Abyssal Conduits, all structures generate during chunk feature placement — never retroactively placed on existing chunks.
- No post-gen sweeps. No "check every chunk on load" validation.
- Shell generation uses a single pass with a pre-computed 3D bounding mask per water volume, not per-block neighbor checks.

### 7. Fluid ticking is opt-in, not ambient
- Tainted Water does NOT tick the player every game tick to check submersion.
- Uses Forge's `Fluid#handleEntityInsideOrOn` with a per-player rate limiter (checks apply at most once per 20 ticks while in fluid).
- Temporary Flux accumulation while submerged is applied in batches — +10 per second rather than +0.5 per tick.

### 8. Rendering is aggressively culled
- Custom fog shaders activate via a **dimension type flag** only, never polled.
- Weave Confluence particle systems render only when player has Arcane Monocle worn AND Confluence is within 32 blocks of camera AND Confluence is within frustum.
- Particle budgets are hard-capped per system; excess spawn requests are dropped, not queued.
- HUD elements draw only when relevant (Grimoire equipped = Grimoire HUD; Monocle worn = aspect HUD; layered when both active).

### 9. Data is loaded once and interned
- Aspect registry, affinity maps, research tree, constellation data all load on server start / datapack reload into immutable data structures.
- No JSON parsing in any runtime path.
- Aspect-to-item affinity uses a pre-built `ImmutableMap<Item, AspectList>` keyed by registry ID — O(1) lookup.
- Entity type tags resolved once at world load, cached as `Set<EntityType<?>>` per layer.

### 10. Mob wrapper is additive, not overriding
- Corrupted mob treatment is a **capability attached at spawn**, not a subclass, not an entity replacement.
- Applied once in `EntityJoinLevelEvent` (one handler, dimension-gated, early return for non-Expanse).
- Visual overlay is client-only (shader tint + particle emitter), zero server rendering cost.
- Flux-on-hit handler fires on `LivingHurtEvent` only, checks capability presence first (one `hasCapability` call, then return if absent).

### 11. No reflection in hot paths
- All cross-mod integration (mob whitelist, aspect affinity) uses registry-ID string matching resolved ONCE at datapack load, stored as direct `EntityType<?>` / `Item` references afterward.
- Integration with JEI, Curios, Patchouli uses their public APIs only, never reflection.

### 12. Datapack reload is the only config hot path
- Hot-reload happens on `/reload` and world load. No per-tick config checks.
- All conditional logic (is-Expanse-dimension, is-tainted-chunk, aspect of item) reads from pre-computed structures.

### 13. Degradation over refusal
- Every visual system has quality tiers auto-selected from the player's graphics settings.
  - Fast graphics: no fog shader, flat fog color change only.
  - Fancy graphics: full fog shader, reduced particle density.
  - Fabulous graphics: all effects at full.
- Hallucinated mob spawn frequency scales with client FPS — drops if frame time exceeds 20ms to prevent death spirals.

## Per-System Contracts

### Weave Capability
- Zero idle cost. Capability never ticks; math is deferred to read-time.
- Backing store: fastutil primitive map pre-sized to exactly 6 slots (primals only). Compound aspects derived on-demand, never stored.
- Read caching: Grimoire channel caches the 9 `LazyOptional` handles on start, releases on stop. No repeated `getCapability()` lookups during channel.
- Write coalescing: all mutations in a single server tick to one chunk's Weave batch via a dirty flag, applied once at end of tick.
- NBT footprint: fixed ~48 bytes per chunk (3 floats + 6 aspect floats + long timestamp + bool). No lists, no strings.
- No cross-chunk iteration on load. Chunks wake independently with their stored state; neighbors are never re-synced.
- Sync gating: Weave state sent to clients only when an Arcane Monocle is worn AND chunk is within 4-chunk radius AND value changed beyond threshold since last sync.

### Aspect Registry
- Affinity lookup is O(1). All aspect-to-item/block/entity mappings built into an `ImmutableMap<ResourceLocation, AspectList>` at datapack load. Runtime scanning = single map lookup.
- AspectList is a flat frozen array of `(Aspect, float)` tuples sorted by aspect ID. Stack-allocatable for tooltip rendering.
- JSON parsed once. All `data/aetherium/aspects/` JSONs parsed on `AddReloadListenerEvent`, stored as immutable structures, never re-parsed.
- No per-tick scanning. Scan event fires only on right-click action — zero ambient cost.
- Player scanned-list: bitset keyed by registry ID hash, not a `Set<String>`. ~8 KB per player for a full-modpack bitset, single AND-check per scan attempt.
- Aspect compatibility graph: pre-built as an adjacency `BitSet[]` at reload — single bit check per compatibility query.

### Flux System
- Single player tick dispatcher. One `PlayerTickEvent.Post` handler for the whole mod manages all player-bound capabilities (Flux, Grimoire state, cooldowns). Not one event per capability.
- Flux ticks at 1Hz, not 20Hz. Player handler runs its work only when `player.tickCount % 20 == 0`. Early return on other ticks.
- Temp Flux decay is lazy. When outside Flux sources, store `lastExposureTick`. On next read/tick, compute decay from delta. No decay ticks while decaying — just math on access.
- Sync threshold: Personal Flux syncs only on ≥1 integer change. Temp Flux syncs only on ≥5 integer change OR threshold crossing (40%, 60%, 75%, 90%). Avoids spamming packets during gradient fluctuation.
- Hallucination spawn is client-side, gated on local cached Temp Flux value. Zero server work.
- Hallucination pool: client keeps a ring buffer of the last 8 entity types the player has seen rendered. New hallucinations pull from the buffer, not from entity registry scans.
- Hallucination tick budget: hard cap of 6 simultaneous hallucinations per client. Spawn requests beyond cap are dropped silently.
- Taint biome spread runs on `ServerLevel` tick scheduler once per 168000 ticks (weekly). Uses a priority queue of "contaminated chunks" maintained incrementally on Flux write, not a scan.

### Research / Hex Puzzle
- GUI-only runtime cost. Research system is entirely client-interactive — it costs nothing when no player has the Thaumonomicon open. No background ticking, no world scan.
- Bounded board sizes mean all memory and CPU costs are constant-bounded at compile time. No allocation scales with topic complexity beyond the Expert cap.
- Chain evaluation on placement is O(n) in filled slot count via flood-fill. At Expert tier, 127 slots is ~microseconds per evaluation — happens on placement, not per frame.
- Aspect compatibility check is a single BitSet bit lookup from the pre-built adjacency graph. No map lookups, no iteration.
- In-progress board state maxes at 127 bytes per open research session on the player capability.
- Marble drag is client-local. No network traffic while dragging; only placement confirmation fires a packet — a compact `(topicId, slotIndex, aspectId)` tuple under 12 bytes.
- Topic JSONs parsed once at datapack reload into immutable `TopicDefinition` records keyed by ID.
- Corrupted variant generation is deterministic and done at datapack load, not runtime.

### Grimoire System
- No `inventoryTick` overrides. Grimoire does not tick in inventory. Held-item behavior fires only on active use events (right-click, release).
- NBT parse caching: Grimoire configuration (binding/clasp/pages/shard) parsed from NBT once on first access per tick and cached on the `ItemStack` via a capability-stored interpretation object. Invalidated only on NBT mutation.
- Vis channel loop: active channel runs on client animation + a single server-side scheduled task per channeling player, not a tick event. Server task self-deschedules on channel stop.
- Model resolution: ItemProperties delegates to a single float override that encodes (core_tier * 100 + cap_tier * 10 + focus_id) — one float read, no per-frame NBT parse.
- Tooltip composition: pre-built `Component` templates cached per configuration hash. No string concatenation per frame.

### Crafting (Infusion Altar / Celestial Artificer)
- Infusion ticks ONLY while active via a block-entity-scoped scheduled task started on craft trigger, ended on success/failure. The Runic Matrix does not implement `BlockEntityTicker`.
- Pedestal items cached as `ItemStack[]` on infusion start — server does not re-scan pedestals every tick. Mid-infusion pedestal removal handled via block-change events, not polling.
- Stability calculation is incremental, updated once per second (20-tick gate). Modifiers (candles, pillars, batteries) discovered once at infusion start and cached.
- Visual particles are client-driven from a single packet per second containing (matrix pos, current stability). No per-tick sync.
- Multiblock validated ONCE on activation, not continuously. Pattern match runs on right-click trigger; 