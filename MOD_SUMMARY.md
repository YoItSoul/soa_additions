# Souls of Avarice Additions (SOA Additions)

**Mod ID:** `soa_additions`
**Version:** 3.1.1
**Minecraft:** 1.20.1 (Forge 47+)

A comprehensive modpack enhancement mod built around a full-featured quest book system, new progression tiers, world generation, combat mechanics, server diagnostics, and anti-cheat tooling.

---

## Quest System

The centerpiece of the mod. A complete quest framework supporting datapacks, in-game editing, team-based progression, and a browser overlay.

### Chapters & Quests

Quests are organized into **chapters** loaded from `data/<namespace>/quests/<chapter>.json`. Chapters support hierarchical sub-categories via `parent_chapter`, with the UI rendering depth-based headings (h1/h2/h3). Each quest defines tasks, rewards, dependencies, visibility rules, and optional mutual exclusions.

### Pack Modes

Three difficulty modes filter which quests are available: **Casual**, **Adventure**, and **Expert**. The mode is locked per-world and quests can target one or more modes, allowing a single quest tree to serve multiple difficulties.

### Task Types (13)

| Task | Description |
|------|-------------|
| Kill | Kill a specific entity type |
| Item | Collect items (hold or consume variant) |
| Craft | Craft a specific item |
| Mine | Break a specific block |
| Place | Place a specific block |
| Tame | Tame a specific animal |
| Breed | Breed a specific animal |
| Advancement | Earn a vanilla advancement |
| Dimension | Visit a dimension |
| Stat | Reach a stat threshold |
| Observe | Custom observation tracking |
| Checkmark | Manual checkbox |
| Custom Trigger | Triggered by external events |

### Reward Types (5)

| Reward | Description |
|--------|-------------|
| Item | Grant items to inventory |
| XP | Grant experience points |
| Command | Execute a command as the player |
| Grant Stage | Unlock a progression stage |
| Lock Packmode | Lock the world's difficulty mode |

### Teams

Players can form quest teams. Progress is shared across the team — when one member completes a task, it counts for everyone. Claims can be per-player or per-team depending on the quest's repeat scope.

### In-Game Editor

Operators (permission level 4) can toggle edit mode with `/soa quests editmode`. The editor supports creating, deleting, and modifying quests and chapters in-game. Edits can target the world-level override layer or the base datapack. Changes are synced to all clients in real-time.

### Graph Layout

Quests are displayed as a dependency graph using a Sugiyama-style layered layout algorithm. Nodes are positioned automatically via topological sort, longest-path layer assignment, and barycenter crossing reduction. Manual positioning via drag-and-drop is also supported. Dependency lines render between nodes with arrow markers, and OR-dependencies use dashed lines.

### Web Overlay

An embedded HTTP server (default port 25580) provides a "second screen" browser view of the quest book. Each player gets a unique token URL via `/soa quests overlay`. The overlay mirrors the in-game graph layout with pan/zoom, tooltips, and detail popups. Progress updates stream live via Server-Sent Events — task counts, quest status, and claims all reflect in the browser as they happen in-game.

### FTB Quests Import

Existing quest trees from FTB Quests can be imported into the SOA format via `/ftb-quests-import`.

### JEI Integration

The quest book integrates with Just Enough Items for recipe lookups from within the quest UI.

---

## Custom Ores & Tool Tiers

Four new ore tiers extend vanilla's progression past Netherite:

| Tier | Level | Ore Location |
|------|-------|-------------|
| Infernium | 5 | Nether |
| Void | 6 | Deep Dark |
| Abyssal | 7 | Deep Ocean |
| Ether | 8 | The End |

Each tier has a corresponding ore block, ingot, and tool requirement tag. Ores generate via biome modifiers in their respective dimensions.

### Tool Requirements

When enabled, blocks tagged with a tier requirement take massively increased damage to tools of insufficient tier (configurable multiplier, default 10x). This encourages progression through the intended upgrade path.

---

## Grove Shrine

On first world start, a grove shrine spawns near world spawn. The shrine consists of a **Grove Spawn Block** that transforms into the structure on its first tick, and a **Grove Boon Block** — an unbreakable, glowing block that provides effects to nearby players. A dedicated Point of Interest type is registered for villager pathfinding interaction.

---

## Combat: Headshot System

Projectile and melee hits that land near an entity's eye height are detected as headshots:

- **Base headshot:** 2x damage
- **Critical headshot:** 3x damage
- **Velocity bonus:** +0.2x per unit of impact speed
- **Effects on target:** Blindness (60 ticks), Slowness (40 ticks), Nausea if helmet breaks (80 ticks)
- Headshots damage and can destroy helmets

---

## Anti-Cheat

Four detection layers run on join and during play:

1. **Client Mod Scan** — On login, the client sends its full mod list and resource packs. The server scans for known cheat clients (xray, baritone, etc.) and flags matches.
2. **Silent Client Detection** — Players who never send a mod report within the deadline are flagged as potentially running a modified client.
3. **Command Heuristics** — OP-level commands are monitored; non-whitelisted commands are flagged.
4. **Server-Side Mod Scan** — The server's own mod list is scanned at startup for forbidden entries.

---

## JVM Profiler & Diagnostics

A background sampler thread collects server performance data:

- Heap/non-heap memory usage and allocation rate
- Per-collector GC counts and durations
- Thread counts, CPU load, system RAM
- Server TPS, loaded chunks, entity counts, player counts
- Data is written to CSV files in `logs/soa_jvm_stats/`

On shutdown, a summary is logged with peak heap, average/longest GC pauses, average TPS, and a suggested `-Xmx` value.

### Auto-JFR

When a long GC pause or near-OOM condition is detected, a 30-second Java Flight Recording is automatically captured for post-mortem analysis.

### Spark Integration

If the Spark profiler is installed, SOA can trigger a 120-second profile and retrieve the resulting URL for telemetry or manual review.

### Commands

- `/soa optimizer` — View current JVM stats
- `/soa compat` — View compatibility diagnostics

---

## Telemetry

An opt-in telemetry system sends a one-shot JSON report per launch and periodic heartbeats during play:

- **Collected:** Minecraft username, UUID, OS/CPU/RAM, JVM version and args (secrets stripped), heap size, load time, mod count, GPU info
- **Not collected:** File paths, environment variables, world data
- **Heartbeat:** Every 5 minutes — playing status, dimension, heap stats
- **Endpoint:** Configurable (default: `https://telemetry.soulsofavarice.com/report`)

An anonymous install UUID is stored locally in `config/soa_additions/install_id.txt`.

---

## Registry Export

`/soa export [target]` dumps game registry contents to JSON files in `soa_exports/`:

| Target | Contents |
|--------|----------|
| items | Name, stack size, durability, rarity, food properties |
| blocks | Hardness, blast resistance, friction, speed factor |
| entities | Class, category, size, fire immunity, tracking range |
| structures | All registered structures |
| biomes | All registered biomes |
| dimensions | All registered dimensions |
| effects | All status effects |
| enchantments | All enchantments |
| all | Everything above |

---

## Configuration

### Server Config (`soa_additions.toml`)

| Option | Default | Description |
|--------|---------|-------------|
| `enableToolRequirements` | false | Heavy tool damage without required tier |
| `enableGroveBoons` | true | Grove Boon blocks apply effects |
| `toolDamageMultiplier` | 10.0 | Tool damage multiplier for tier violations |
| `jvmProfiler.enabled` | true | Background JVM sampler |
| `jvmProfiler.intervalSeconds` | 10 | Sample interval |
| `jvmProfiler.keepSessions` | 20 | Session file retention |
| `jvmProfiler.autoJfrOnSpike` | true | Auto-capture JFR on GC/OOM spike |
| `questWebOverlay.enabled` | true | Quest overlay HTTP server |
| `questWebOverlay.port` | 25580 | Overlay server port |
| `telemetry.enabled` | true | Send usage reports |
| `telemetry.heartbeatMinutes` | 5 | Heartbeat interval |
| `telemetry.autoSparkProfile` | true | Auto 120s Spark profile |

### Client Config (`soa_additions-questbook.toml`)

40+ ARGB color settings for the quest book UI — backgrounds, text, buttons, edges, node states, and more.

---

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/soa quests claim <id>` | None | Claim a completed quest's rewards |
| `/soa quests task complete <player> <quest> <index>` | OP (2) | Force-complete a task |
| `/soa quests task uncomplete <player> <quest> <index>` | OP (2) | Reset a task |
| `/soa quests overlay` | None | Get personal web overlay URL |
| `/soa quests editmode` | OP (4) | Toggle in-game quest editor |
| `/soa quests edittarget <target>` | OP (4) | Set edit save target |
| `/soa quests reset <quest> [player]` | OP (2) | Reset quest progress |
| `/soa quests team ...` | None | Team management |
| `/soa quests packmode` | None | View/lock pack mode |
| `/soa export [target]` | OP (2) | Export registry data |
| `/soa optimizer` | None | View JVM stats |
| `/soa compat` | None | View compatibility info |
| `/ftb-quests-import` | OP (4) | Import FTB Quests data |

---

## Networking

10 custom packets over a single Forge `SimpleChannel`:

| Packet | Direction | Purpose |
|--------|-----------|---------|
| ClientModReportPacket | Client to Server | Mod list for anti-cheat |
| QuestSyncPacket | Server to Client | Full progress snapshot |
| QuestClaimPacket | Client to Server | Claim quest reward |
| QuestCheckmarkPacket | Client to Server | Toggle checkmark task |
| QuestSubmitPacket | Client to Server | Submit item task |
| QuestEditStatePacket | Server to Client | Edit mode state |
| QuestMovePacket | Bidirectional | Move quest node position |
| QuestEditPacket | Bidirectional | Edit quest properties |
| ChapterEditPacket | Bidirectional | Edit chapter properties |
| QuestDefinitionSyncPacket | Server to Client | Full quest tree definitions |

---

## Registered Game Objects

- **Items:** 6 (4 ingots, cheater coin, quest book)
- **Blocks:** 6 (4 ores, grove spawn, grove boon)
- **Block Entities:** 2 (grove spawn, grove boon)
- **Creative Tab:** 1 (SOA Additions)
- **POI Types:** 1 (shrine boon)
- **Tool Tiers:** 4 (Infernium, Void, Abyssal, Ether)
- **Biome Modifiers:** 4 (ore generation per dimension)
- **Advancements:** 2 (visit grove, cheats detected)
- **Loot Tables:** 6 (block drops)
