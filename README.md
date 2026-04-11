<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge&logo=curseforge" alt="Minecraft 1.20.1"/>
  <img src="https://img.shields.io/badge/Forge-47+-E04E14?style=for-the-badge&logo=curseforge" alt="Forge 47+"/>
  <img src="https://img.shields.io/badge/Version-3.1.1-blue?style=for-the-badge" alt="Version 3.1.1"/>
  <img src="https://img.shields.io/github/license/YoItSoul/soa_additions?style=for-the-badge" alt="License"/>
</p>

# Souls of Avarice Additions

**The all-in-one modpack companion mod.** Built for the Souls of Avarice modpack, but designed to work in any 1.20.1 Forge pack. A quest book system, new ore tiers, combat mechanics, server tools, and more — all in one jar.

> **Found a bug?** [Open an issue](https://github.com/YoItSoul/soa_additions/issues/new/choose) — bug report templates are ready to go.

---

## Features at a Glance

| Feature | Description |
|---------|-------------|
| **Quest Book** | Full quest system with visual dependency graph, in-game editor, teams, and browser overlay |
| **Ore Tiers** | 4 new progression tiers past Netherite (Infernium, Void, Abyssal, Ether) |
| **Headshots** | Skill-based combat with headshot multipliers for melee and projectiles |
| **Grove Shrine** | World-spawn landmark with boon effects for new players |
| **Server Profiler** | Background performance sampling with automatic JFR capture |
| **Anti-Cheat** | Multi-layer cheat detection with mod scanning and behavior monitoring |
| **Registry Export** | Dump all items, blocks, entities, etc. to JSON for documentation |

---

## Quest Book

A complete quest system built from scratch. No external quest mods needed.

**Write quests in JSON datapacks** or use the **in-game editor** to build your quest tree without ever leaving Minecraft. Quests are displayed as a visual dependency graph with automatic layout — drag nodes to rearrange, draw dependency lines, and see your tree take shape in real time.

### Task Types (13)

| Task | Description |
|------|-------------|
| Kill | Kill a specific entity type |
| Item | Collect items (hold or consume variant) |
| Craft | Craft a specific item |
| Mine | Break a specific block |
| Place | Place a specific block |
| Tame / Breed | Tame or breed specific animals |
| Advancement | Earn a vanilla/modded advancement |
| Dimension | Visit a dimension |
| Stat | Reach a stat threshold |
| Observe | Custom observation tracking |
| Checkmark | Manual checkbox |
| Custom Trigger | Triggered by external events |

### Reward Types (5)

Items, XP, commands, progression stages, and packmode locking.

### Teams

Players can team up. Quest progress is shared — when one teammate kills the dragon, everyone's task updates. Rewards can be claimed individually or as a team.

### Three Difficulty Modes

**Casual, Adventure, and Expert.** Each quest can target one or more modes, so a single quest tree serves all your players. The mode locks per-world on first choice.

### Second Screen Mode

Run `/soa quests overlay` to get a personal URL that opens your quest book in a browser. The web view mirrors the in-game graph with pan/zoom, tooltips, and live progress updates via Server-Sent Events. Perfect for a second monitor.

### Editor & Datapack Support

- Load quests from any datapack — drop JSON in `data/yourpack/quests/`
- Full in-game editor for operators: create chapters, add quests, set tasks and rewards, drag to reposition
- Edits save to a world-level override layer so your base datapack stays clean
- Import existing FTB Quests trees with one command
- JEI integration for recipe lookups inside the quest book

---

## New Ore Tiers

Four new progression tiers beyond Netherite, each found in a different dimension:

| Tier | Material | Found In |
|------|----------|----------|
| 5 | **Infernium** | Nether |
| 6 | **Void** | Deep Dark |
| 7 | **Abyssal** | Deep Ocean |
| 8 | **Ether** | The End |

Enable **tool requirements** in the config to make higher-tier blocks punish underprepared tools with heavy durability damage.

---

## Headshot System

Land a hit near a mob's head and deal bonus damage:

- **Headshot:** 2x damage + Blindness and Slowness
- **Critical headshot:** 3x damage
- **Fast projectiles** add velocity to the multiplier
- Headshots damage helmets and can break them, applying Nausea

Works with both melee and projectiles. No extra items or enchantments needed.

---

## Server Tools

### Profiler
Background sampling every 10 seconds — memory, GC, TPS, entity/chunk counts. Auto-captures Java Flight Recordings on long GC pauses or near-OOM. Integrates with Spark if installed.

### Anti-Cheat
Four layers: client mod list scanning, silent-client flagging, OP command monitoring, and server-side mod audit.

### Registry Export
`/soa export all` dumps every item, block, entity, biome, structure, enchantment, and effect to organized JSON files.

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
- Set the tool damage multiplier, profiler interval, overlay port
- 40+ client-side color settings for the quest book UI

---

## Building from Source

```bash
git clone https://github.com/YoItSoul/soa_additions.git
cd soa_additions
./gradlew build
```

The built jar will be in `build/libs/`.

---

## Requirements

- **Minecraft** 1.20.1
- **Forge** 47+

**Optional:** JEI (recipe integration), Spark (profiler integration)

---

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).
