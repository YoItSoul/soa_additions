# Souls of Avarice Additions

**The all-in-one modpack companion mod.** Built for the Souls of Avarice modpack, but designed to work in any 1.20.1 Forge pack. A quest book system, new ore tiers, combat mechanics, server tools, and more — all in one jar.

---

## Quest Book

A complete quest system built from scratch. No external quest mods needed.

**Write quests in JSON datapacks** or use the **in-game editor** to build your quest tree without ever leaving Minecraft. Quests are displayed as a visual dependency graph with automatic layout — drag nodes to rearrange, draw dependency lines, and see your tree take shape in real time.

### What can quests do?

**13 task types** cover just about everything a player can do:
- Kill mobs, mine blocks, place blocks, craft items, collect items
- Tame or breed animals
- Visit dimensions, earn advancements, reach stat thresholds
- Manual checkboxes and custom triggers for anything else

**5 reward types** for when they finish:
- Items, XP, commands, progression stages, and packmode locking

### Teams

Players can team up. Quest progress is shared — when one teammate kills the dragon, everyone's task updates. Rewards can be claimed individually or as a team.

### Three Difficulty Modes

**Casual, Adventure, and Expert.** Each quest can target one or more modes, so a single quest tree serves all your players. The mode locks per-world on first choice.

### Second Screen Mode

Run `/soa quests overlay` to get a personal URL that opens your quest book in a browser. The web view mirrors the in-game graph exactly — same layout, same dependency lines, same node positions. Progress updates live as you play, including individual task counts. Perfect for a second monitor or a tablet next to your desk.

### Datapack & Editor Support

- Load quests from any datapack — your pack, addon packs, or the built-in ones
- Full in-game editor for operators: create chapters, add quests, set tasks and rewards, drag to reposition
- Edits save to a world-level override layer so your base datapack stays clean
- Import existing quest trees from FTB Quests with one command
- JEI integration for recipe lookups inside the quest book

---

## New Ore Tiers

Four new progression tiers beyond Netherite, each found in a different dimension:

- **Infernium** (Tier 5) — Nether
- **Void** (Tier 6) — Deep Dark
- **Abyssal** (Tier 7) — Deep Ocean
- **Ether** (Tier 8) — The End

Each tier comes with an ore block and ingot. Enable **tool requirements** in the config to make higher-tier blocks punish underprepared tools with heavy durability damage — encouraging players to follow the intended progression.

---

## Grove Shrine

A small shrine generates near world spawn when a world is first created. At its heart is a **Grove Boon Block** — an unbreakable, softly glowing block that grants effects to nearby players. A starting landmark to orient new players and kick off their adventure.

---

## Headshot System

Land a hit near a mob's head and deal bonus damage:

- **Headshot:** 2x damage + Blindness and Slowness on the target
- **Critical headshot:** 3x damage
- **Fast projectiles** deal even more — velocity adds to the multiplier
- Headshots damage helmets and can break them, applying Nausea when they shatter

Works with both melee and projectiles. Adds a skill element to combat without any extra items or enchantments.

---

## Server Tools

### Built-In Profiler

A background thread samples your server's vitals every 10 seconds and writes them to CSV:
- Memory usage, allocation rate, GC pauses
- TPS, entity count, chunk count, player count
- On shutdown, get a summary with peak memory, worst GC pause, average TPS, and a suggested `-Xmx` value

If the profiler detects a long GC pause or near-OOM condition, it automatically captures a 30-second Java Flight Recording for diagnosis. Integrates with Spark if installed.

### Anti-Cheat

Four layers of detection, no configuration required:
1. Clients report their mod list on login — known cheat mods are flagged
2. Clients that never report at all are flagged as suspicious
3. OP-level command usage is monitored for unusual patterns
4. The server's own mod list is scanned at startup

### Registry Export

`/soa export all` dumps every item, block, entity, biome, structure, enchantment, and effect in the game to organized JSON files. Useful for pack developers building spreadsheets, wikis, or quest lists.

### Telemetry

Optional, anonymous usage reporting to help pack developers understand their playerbase. Collects hardware info, load times, and mod counts — never file paths, world data, or environment variables. Fully configurable and easily disabled.

---

## Commands

| Command | Description |
|---------|-------------|
| `/soa quests overlay` | Get your personal quest book browser URL |
| `/soa quests claim <id>` | Claim a completed quest |
| `/soa quests editmode` | Toggle the in-game quest editor (OP) |
| `/soa quests reset <quest>` | Reset quest progress (OP) |
| `/soa quests team ...` | Create, join, or leave quest teams |
| `/soa quests packmode` | View or lock your difficulty mode |
| `/soa optimizer` | View live server performance stats |
| `/soa export [target]` | Export game registries to JSON (OP) |
| `/ftb-quests-import` | Import from FTB Quests (OP) |

---

## Configuration

Everything is configurable in `soa_additions.toml`:
- Toggle tool requirements, grove boons, profiler, web overlay, telemetry
- Set the tool damage multiplier, profiler interval, overlay port, heartbeat frequency
- 40+ client-side color settings for the quest book UI in a separate config file

---

## For Pack Developers

- **Datapack quests:** Drop JSON files in `data/yourpack/quests/` and they load automatically
- **Java API:** Register custom task types and reward types via the task/reward registries
- **Programmatic quests:** Define quests in pure Java with the builder DSL — no JSON needed
- **FTB import:** Migrate existing FTB Quests trees in one command
- **Pack modes:** Tag quests with Casual/Adventure/Expert to serve multiple difficulties from one tree
- **Registry export:** Generate complete item/block/entity lists for documentation

---

**Requires:** Minecraft 1.20.1, Forge 47+
**Optional:** JEI (recipe integration), Spark (profiler integration)
