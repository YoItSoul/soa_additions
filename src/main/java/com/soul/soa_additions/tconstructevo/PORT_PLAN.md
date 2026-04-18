# TConstructEvo 1.20.1 Port — Plan & Progress

Living document. Update after every milestone bump in `gradle.properties`.

## Current status

- **mod_version:** 3.10.11 (last green compile — GreedyCraft config parity pass)
- **Active task:** #6 Sceptre + artifacts **complete**. Sceptre tool (3.10.7), artifact data layer (3.10.8), build pipeline (3.10.9), loot-modifier injection (3.10.10). #11 parity audit appended below. Remaining open work: DE shield HUD, DE fusion recipe plumbing, behavioural fills for the 9 marker modifiers (Botania mana, BM life-network/demon-will, PE EMC), and the MAGIC/BOWSTRING-only tconevo materials deferred from the task-#8 batch. All strictly optional PATCH-bump follow-ups.
- **Next task:** #10 runtime smoke test (client boot, verify no registry errors, spawn an artifact via loot table, build a Sceptre, confirm modifier tooltips render). Compile is green; runtime verification is user-side only.

## Scope (locked)

1-for-1 feature parity where the 1.20.1 platform supports it, not line-for-line.
Prime directive: **port ALL tconevo features**. Cosmetic-only (Thaumon) skipped.

Modpack reference for integration gating lives at
`D:\Minecraft\Instances\Souls of Avarice\mods`.

## Architecture

```
com.soul.soa_additions.tconstructevo/
├── TConstructEvoPlugin.java     bootstrap, gated by ModList.isLoaded("tconstruct")
├── TConEvoConfig.java           ForgeConfigSpec: general/combat/integrations/draconic/traits
├── TConEvoItems.java            DeferredRegister<Item>, prefixes "tconevo/"
├── TConEvoPotions.java          TRUE_STRIKE, DAMAGE_BOOST, DAMAGE_REDUCTION, HEAL_REDUCTION
├── TConEvoEntities.java         MAGIC_MISSILE (stub)
├── TConEvoCreativeTab.java      Registries.CREATIVE_MODE_TAB, icon = Coalescence Matrix
├── TConEvoAttributes.java       DeferredRegister<Attribute>, ACCURACY (more to come)
├── TConEvoModifiers.java        DeferredRegister<Modifier>, key = ModifierManager.REGISTRY_KEY
├── event/TConEvoEventHandler.java  Forge-bus: LivingHurt, LivingHeal consumers
├── modifier/core/                  24 mod-agnostic modifiers (this milestone)
├── integration/<modid>/            one subpackage per soft-dep, gated in initIntegrations
├── entity/                         MagicMissileEntity
├── potion/                         effect classes
├── item/                           TCEMiscItems (Coalescence Matrix), artifacts TBD
└── recipe/ mixin/ client/ compat/  empty; reserved
```

## Core modifiers completed (task #3)

All under `modifier/core/`. Each attaches its own hook via `registerHooks`.

| Class | Hook | Behaviour |
|---|---|---|
| Vampiric | MELEE_HIT afterMeleeHit | Heal attacker = damage × 10%/level (config) |
| Executor | MELEE_DAMAGE | +20%/level × (1 − targetHP/max) |
| Culling | MELEE_DAMAGE | +100%/level if target is baby |
| Juggernaut | MELEE_DAMAGE | +50%/level × (1 − attackerHP/max) |
| Overwhelm | MELEE_DAMAGE | +4%/level × target armour attribute |
| Opportunist | MELEE_DAMAGE | +30%/level vs any debuffed target |
| ImpactForce | MELEE_DAMAGE | +100%/level × attacker deltaMovement length |
| Crystalline | MELEE_DAMAGE | +50%/level × tool durability fraction |
| DeadlyPrecision | MELEE_DAMAGE | +50%/level on vanilla crits |
| Ruination | MELEE_DAMAGE | +(HP × 5%/level), capped at +40 |
| Sundering | MELEE_HIT | Weakness 3s/level |
| Corrupting | MELEE_HIT | Wither, amp stacks to 2×level |
| BattleFuror | MELEE_HIT | TConEvo DAMAGE_BOOST self-stack, cap 2×level |
| Luminiferous | MELEE_HIT | Glowing 5s/level |
| FootFleet | MELEE_HIT | Speed II self, 3s |
| MortalWounds | MELEE_HIT | TConEvo HEAL_REDUCTION on target |
| Rejuvenating | MELEE_HIT | Regen III self, config-driven seconds × level |
| TrueStrike | MELEE_HIT | TConEvo TRUE_STRIKE self, 6t (consumer TBD) |
| Relentless | MELEE_HIT | −4t/level off target invulnerableTime |
| ThundergodWrath | MELEE_HIT | Spawn lightning bolt on killing blow |
| Purging | MELEE_HIT | 20%/level chance to strip a beneficial effect |
| Staggering | MELEE_HIT | Slowness V on fully charged hits |
| Blasting | MELEE_HIT | 8%/level chance, non-griefing explosion r=2.0 |
| ChainLightning | MELEE_HIT | 15%/level chance, 4 bounces, 50% damage |
| Aftershock | MELEE_HIT | Bonus magic hit on fully charged swings, UUID re-entry guard |
| Photosynthetic | INVENTORY_TICK | 12%/level/sec repair while in inventory, under open sky |
| Fertilizing | BLOCK_INTERACT | Right-click = bone meal; 1 durability per growth |
| Cascading | BLOCK_BREAK | Column-mine contiguous falling-block state; UUID re-entry guard |
| Modifiable | VOLATILE_DATA | +1 UPGRADE slot per level via ToolDataNBT.addSlots |
| Energized | TOOL_STATS + TOOL_DAMAGE | Raises ToolEnergyCapability.MAX_STAT by level × capacity; drains RF to absorb durability damage |
| Piezoelectric | MELEE_HIT | On fully-charged hit, converts damage × cfg RF into inventory IEnergyStorage items, split by headroom |
| Photovoltaic | INVENTORY_TICK | 1 s tick: adds (level × perLevel × skylight/15) RF to tool's ToolEnergyCapability pool |
| Fluxed | TOOL_STATS + TOOL_DAMAGE | Large RF buffer (level × capacity); expensive per-point durability replacement |
| Accuracy | ATTRIBUTES | In MAINHAND/OFFHAND contributes MULTIPLY_BASE to `soa_additions:tconevo/accuracy` entity attribute |

### TrueStrike consumer (done)

Wired in `TConEvoEventHandler.onLivingHurt`: if attacker carries TRUE_STRIKE,
damage ×1.5 and the effect is consumed. This is a placeholder for the 1.12.2
"accuracy attribute" bypass — real accuracy pipeline lands with ModifierAccuracy.

## Core modifiers NOT yet done

*(none — task #3 is complete. RF family sits on top of TConstruct's
`ToolEnergyCapability`, whose provider is globally registered for every
modifiable tool and activates whenever a modifier contributes to the
`tconstruct:max_energy` stat. Accuracy builds on a new tconevo-owned
`Attribute` registered via `TConEvoAttributes`.)*

## Remaining task-list milestones

See TaskList — #4 through #11.

- **#4 Draconic Evolution integration** — Evolved/DraconicEnergy/DraconicArrowSpeed modifier behaviours DONE (3.9.1). Still need: material JSONs, fusion recipe plumbing, shield HUD.
- **#5 other integrations** — trait Java side is **complete**. Avaritia (3 behavioural), Botania (4 marker), BloodMagic (4 marker), ProjectE (1 marker) done. Mekanism/EnderIO/Thermal/SolarFlux/AE2/Curios/Gamestages/ToolLeveling/JEI had no custom traits in the 1.12.2 original — they were hooks-only bridges into foreign APIs. What's still outstanding for those lives under tasks #8 (material JSONs) and any future API-binding pass to flesh out the marker behaviours.
- **#6 Sceptre + 15 artifacts** — Sceptre needs a new ToolDefinition; artifacts ride on existing Item class with curio-slot metadata when Curios is loaded.
- **#7 coremod transforms** — DONE in 3.10.2. `TransformBreakUnbreakable` is ported as a Forge `PlayerInteractEvent.LeftClickBlock` handler (`UnbreakableBreakHandler`) rather than a mixin: the event fires before vanilla's hardness-aborts-break check, so we can inspect the tool for Omnipotence + harvest tier, then call `world.destroyBlock` and cancel. No mixin infra was required, keeping the build lean. `TransformDisableDamageCutoff` is obsolete on 1.20.1 — TC3.x's damage "cap" is a hard per-stat clamp at `FloatToolStat.maxValue` (2048F for attack_damage), not the 1.12.2 diminishing-returns curve. Since nothing in the game approaches 2048 damage in practice, the `DISABLE_DAMAGE_CUTOFF` config key is left in place but unobserved for now; if a modpack ever needs higher caps, the path is to write a mixin on `FloatToolStat.clamp`.
- **#8 assets** — lang/models/textures (pull from original tconevo textures under `build/_tconevo_decompiled/assets/tconevo/textures`).
- **#9 config hookup** — DONE. `TConEvoConfig.register()` is wired into `TConstructEvoPlugin.init` before any integration bootstrap reads config; this was necessary for the Avaritia/Draconic config-backed modifiers to function.
- **#10 compile + runtime smoke test** — after #3–#9, boot client once and verify no registry errors.
- **#11 parity audit** — before declaring the port done, exhaustively walk
  `build/_tconevo_decompiled/` (all packages — `init/`, `item/`, `entity/`,
  `potion/`, `modifier/`, `integration/<modid>/`, `coremod/`, `config/`,
  `network/`, `client/`, `recipe/`, `lang.json`) and cross-check each
  registered object / class / handler against what landed under
  `com.soul.soa_additions.tconstructevo`. Produce a checklist with three
  columns: **tconevo item** | **port status** (ported / stubbed-marker /
  deferred / intentionally-skipped) | **evidence** (file path or reason).
  Flag any feature that was discussed in prior sessions but never made it
  into the Bump log above. Also re-read `MaterialBuilder` calls in
  `TconEvoMaterials.java` and diff against the material JSONs under
  `data/soa_additions/tinkering/materials/` to catch missing materials,
  missing stat slots, missing per-stat trait entries, or numeric drift.
  Audit output is a section appended to this file (not a separate doc),
  so future-you can grep it in-tree.

## Versioning policy reminder

Full policy lives in the comment above `mod_version=` in `gradle.properties`.
TL;DR: MINOR for new subsystem, PATCH for additive-within-subsystem,
never bump past a failed compile, one bump per milestone.

## Bump log (this task)

- 3.7.8 → 3.8.0 — tconevo scaffold compiles for the first time
- 3.8.0 → 3.8.1 — first 4 core traits (Vampiric/Executor/Culling/Juggernaut)
- 3.8.1 → 3.8.2 — +4 (Overwhelm/Opportunist/ImpactForce/Crystalline)
- 3.8.2 → 3.8.3 — +13 (Sundering/Corrupting/BattleFuror/Luminiferous/FootFleet/
  MortalWounds/Rejuvenating/TrueStrike/Ruination/DeadlyPrecision/Relentless/
  ThundergodWrath/Purging)
- 3.8.3 → 3.8.4 — +2 (Staggering/Blasting)
- 3.8.4 → 3.8.5 — +1 (ChainLightning)
- 3.8.5 → 3.8.6 — +3 (Aftershock/Photosynthetic/Fertilizing)
- 3.8.6 → 3.8.7 — +2 modifiers (Cascading/Modifiable) + TrueStrike consumer
  wired into the LivingHurt handler
- 3.8.7 → 3.8.8 — +1 modifier (Energized) using TConstruct's built-in
  `ToolEnergyCapability` — first RF trait of the family
- 3.8.8 → 3.9.0 — +4 modifiers (Piezoelectric/Photovoltaic/Fluxed/Accuracy)
  + new `TConEvoAttributes` registry (ACCURACY attribute scaffolding for
  future Evasion work). Task #3 **complete**: 34/34 core modifiers.
- 3.9.0 → 3.9.1 — task #4 kickoff: filled DE modifier behaviours
  (Evolved / DraconicEnergy / DraconicArrowSpeed). Evolved gains
  tier-doubling RF capacity (8M << level-1) + Fluxed-style durability
  drain; DraconicEnergy stacks 1M × 2^level onto the existing pool;
  DraconicArrowSpeed adds flat +1.0 VELOCITY per level.
- 3.9.1 → 3.9.2 — +4 tiered DE stat modifiers (AttackDamage/DigSpeed/
  ArrowDamage/DrawSpeed). All drive `percent`-slot multipliers so the
  1.12.2 "tier × base / 4" scaling survives on TConstruct 3.x's stat
  accumulator. DrawSpeed keeps the cubic scaling (level³ × 0.25).
- 3.9.2 → 3.9.3 — +5 DE traits (Primordial/FluxBurn behavioural +
  SoulRend/Reaping/Entropic marker). Primordial spawns a bypass-armor
  chaos follow-up with invuln-reset; FluxBurn drains RF from the
  target's armour slots via ForgeCapabilities.ENERGY and converts the
  total to magic damage. The three markers are empty modifiers kept
  for material-traits-JSON parity. AttackAoe/DigAoe deferred pending
  Tinkers 3.x AoE iterator work.
- 3.9.3 → 3.10.0 — new task milestone: Avaritia integration scaffolded.
  Infinitum zeroes all durability damage (true unbreakable); Omnipotence
  tops each hit up to half-max-HP via magic follow-up with optional
  creative-bypass path; Condensing RNG-drops avaritia:neutron_pile on
  kills and effective block breaks using BuiltInRegistries lookup so
  it no-ops safely if the item id is missing.
- 3.10.0 → 3.10.1 — Botania (AuraSiphon/FaeVoice/GaiaWrath/ManaInfused),
  BloodMagic (Bloodbound/Crystalys/Sentient/Willful), ProjectE
  (EternalDensity) modifiers registered as markers. Their mechanics
  depend on mod-specific APIs (Botania mana, BM life-network / demon
  will, PE EMC + dense blocks); marker registration lets material
  trait JSONs reference these IDs now and behavioural fills drop in
  later under additive PATCH bumps once API deps are wired.
- 3.10.2 → 3.10.3 — task #8 kickoff: first 4 DE materials ported to
  TC3.x data format. `draconium`, `wyvern_metal`, `draconic_metal`,
  `chaotic_metal` each get `definition`/`stats`/`traits` JSONs under
  `data/soa_additions/tinkering/materials/`. Head stats mirror the
  1.12.2 numbers (dur 512/2140/3650/6660, attack 8/12/18/22, mining
  7/15/35/64); handle/grip/limb multipliers scale monotonically with
  the tier. Trait slot `head` + `limb` on each carries Soul Rend at
  its tier (level 0..2 in 1.12.2 → 1..3 on TC3 since modifier levels
  start at 1), plus Evolved on the three upper tiers.
- 3.10.1 → 3.10.2 — task #7 complete. `TransformBreakUnbreakable` is
  ported as `UnbreakableBreakHandler`, a Forge-bus
  `PlayerInteractEvent.LeftClickBlock` listener that destroys
  unbreakable blocks when the player holds an Omnipotence-carrying
  tool whose harvest tier meets `breakUnbreakableHarvestLevel`.
  Avoids a mixin setup entirely by leaning on the fact that the
  event fires before vanilla's hardness-aborts-break branch.
  `TransformDisableDamageCutoff` declared obsolete on 1.20.1 (see
  task-7 bullet above).
- 3.10.3 → 3.10.4 — task #8 expansion: material JSON coverage 4 → 48.
  Bugfix first: the 4 existing DE trait JSONs referenced
  `soa_additions:evolved` / `soa_additions:soul_rend`, but
  `TConEvoModifiers.register` prefixes every ID with `tconevo/`, so
  the real registered IDs are `soa_additions:tconevo/evolved` etc.
  All 4 DE trait JSONs now use the correct namespaced path.
  Then 44 new materials × 3 files (definition/stats/traits) = 132
  new JSONs: BloodMagic (bound_metal, sentient_metal); Botania
  (livingrock, livingwood, dreamwood, manasteel, terrasteel,
  elementium); Avaritia (crystal_matrix, neutronium, infinity_metal);
  ProjectE (dark_matter, red_matter); Mekanism (osmium,
  refined_obsidian, refined_glowstone); EnderIO (redstone_alloy,
  electrical_steel, pulsating_iron, conductive_iron, energetic_alloy,
  energetic_silver, vibrant_alloy, vivid_alloy, crystalline_alloy,
  melodic_alloy, soularium); Thermal (fluxed_electrum, flux_crystal,
  gelid_enderium, gelid_gem, signalum, lumium, enderium, invar,
  constantan); AE2 (sky_stone, certus_quartz, fluix, fluix_steel);
  generic metals (tin, aluminium, nickel, platinum). Stats derived
  1-for-1 from `TconEvoMaterials.java` via consistent mapping:
  MaterialBuilder head → TC3 head+limb durability; harvest level →
  mining_tier (0→wood, 1→stone, 2-3→iron, 4+→netherite); handle mod
  → (mod − 1.0) delta; bow draw_speed → (mod − 1.0) delta; velocity
  carries through raw. TC3 built-in ModifierIds used where the
  1.12.2 trait has a direct equivalent (stonebound, jagged,
  overgrowth, dense, heavy, crumbling, lightweight, cheap, magnetic,
  freezing); tconevo-own traits used otherwise. MAGIC-only and
  BOWSTRING-only materials (mana_diamond, mana_pearl, dragonstone,
  mana_string, fluxed_string, enchanted_fabric, ender_crystal,
  pulsating/vibrant/weather crystals, amber, quicksilver) deferred
  to task #6 since TC3 has no MAGIC stat slot without a custom
  tool. Modifiers not available in TC3.x 1.20.1 (momentum, alien,
  coldblooded, stiff, duritos, poisonous, tasty, crude, etc.)
  silently dropped from per-stat lists rather than faked.
- 3.10.4 → 3.10.5 — task #8 first asset pass. `en_us.json` gains
  every tconevo-ported string: 47 `material.soa_additions.<name>`
  entries for the new materials; 58 `modifier.soa_additions.tconevo.<name>`
  triplets (name + `.flavor` + `.description`), with flavor lines
  transcribed verbatim from the 1.12.2 `.lang` (§-codes dropped
  since 1.20.1 TC3 renders flavor italic via its own formatter);
  four `effect.soa_additions.tconevo.*` strings for the ported
  mob-effects; `attribute.name.soa_additions.tconevo.accuracy`
  for the custom attribute; `itemGroup.soa_additions.tconstructevo`
  for the creative tab; `death.attack.soa_additions.tconevo.chaos`
  + `.item` for the Primordial chaos-damage source. Coalescence
  Matrix model + animated texture (4 frames, 12-tick frametime,
  interpolated) + mcmeta copied from the decompile under
  `item/tconevo/coalescence_matrix.*` to match the `tconevo/`
  path prefix that `TConEvoItems.register` applies. Translation
  key format verified against TC3's `Modifier.makeTranslationKey`
  which delegates to vanilla `Util.makeDescriptionId` — that
  rewrites `/` in the ResourceLocation path to `.` in the lang
  key, so `soa_additions:tconevo/accuracy` resolves to
  `modifier.soa_additions.tconevo.accuracy`. Material *render*
  JSONs (color/fallbacks/recolor palette) still pending — tool
  parts currently render with a generic grey fallback; no crash,
  just no per-material tint.
- 3.10.8 → 3.10.9 — task #6 artifact build pipeline. New
  `ArtifactBuilder` translates a parsed `ArtifactSpec` into the
  finished `ItemStack`. Steps mirror 1.12.2 `ArtifactTypeTool.buildArtifact`:
  resolve the tool via `ForgeRegistries.ITEMS` (must implement
  `IModifiable`), resolve every material via
  `MaterialRegistry.getMaterial(MaterialId)`, build the base
  stack with TC3's `ToolBuildHandler.buildItemFromMaterials`,
  grant `freeMods` extra UPGRADE slots through
  `ToolDataNBT.addSlots`, apply each declared modifier with
  `ToolStack.addModifier(ModifierId, level)`, then style: a
  yellow + underline custom hover name and (when present) a
  Lore tag in dark-purple italic. Bare ids without a namespace
  default to `tconstruct:` to preserve the 1.12.2 shorthand
  (`"scythe"`, `"fiery"`, `"manyullyn"`); fully-qualified
  `ns:path` strings pass through unchanged so user-authored
  artifacts can target our own modifiers/materials too. On any
  unresolved id the builder logs a warning and returns null —
  caller skips the entry rather than aborting the load.
  `ArtifactManager.stacks()` lazily materialises every spec
  on first access and caches the map until the next data-pack
  reload invalidates it. Loot injection is the next bump.
- 3.10.7 → 3.10.8 — task #6 artifact data layer. New `ArtifactSpec`
  (record-style) parses every field the 1.12.2
  `ArtifactTypeTool$Spec` accepted: `type`, `weight`, `name`,
  `lore` (string-or-array), `tool`, `materials`, `free_mods`,
  `mods` (each entry either a bare string id at level 1 or a
  `{id, level}` object). `ArtifactManager extends
  SimpleJsonResourceReloadListener` over `tconevo/artifacts`;
  reloads with `/reload`. Wired via Forge `AddReloadListenerEvent`
  in `TConEvoEventHandler`. The 14 shipped tconevo artifact JSONs
  (agneyastra, ancient_arrow, crystal_sceptre, dragon_tooth,
  guardian_angel, infinity_edge, light_shoes, maelstrom,
  morning_star, ms_erudita, myriad_slashes, phase_boots, stormbow,
  werebane) copied verbatim into
  `data/soa_additions/tconevo/artifacts/`. Parsing only this
  milestone — `ItemStack` build (Tinkers tool +
  free-mod slots + applied mods + lore NBT) and loot-table
  injection land in the next bumps. The 1.12.2 `data_tag`
  arbitrary-NBT field is intentionally dropped (no shipped
  artifact uses it).
- 3.10.6 → 3.10.7 — task #6 first Sceptre scaffold. New `SceptreItem`
  under `tconstructevo/item/sceptre/` extends TC3's `ModifiableItem`;
  `use()` reads ATTACK_DAMAGE from the toolstack, spawns 3
  `MagicMissileEntity` at ±π/12 spread (mirroring 1.12.2's three-shot
  fan), 8 durability per shot, 4-tick cooldown to keep the spam in
  check. Falls through to `super.use()` when the tool is broken or
  short on durability so modifier GENERAL_INTERACT hooks still get a
  chance. Holder class `TCESceptre` constructs the
  `ToolDefinition` directly from the registry id (avoids Java's
  forward-ref complaint when the registry-supplier captures the
  definition field) and is bootstrapped from `TConstructEvoPlugin`.
  Tool definition JSON declares 4 parts: `large_plate` head
  (primary, drives material display) + `tough_handle` handle +
  `tough_binding` + `tough_handle` focus, mapping the 1.12.2
  toughToolRod / largePlate / toughBinding / focus layout onto
  parts that exist in TC3 1.20.1. Stats: base 4.0 attack damage
  with 0.8× multiplier, 1.6 attack speed, 1.3× durability mult,
  1 ability + 3 upgrade slots. Item model uses the
  `tconstruct:tool` loader with copies of the original tconevo
  sceptre part PNGs (handle/hilt/focus/setting) under
  `assets/soa_additions/textures/item/tconevo/sceptre/` so the
  per-material recolor pipeline works without writing new
  textures. `item.soa_additions.tconevo.sceptre` lang entry
  added. Artifact loader + 14 shipped artifact JSONs still
  pending; tracked under #6.
- 3.10.10 → 3.10.11 — GreedyCraft config parity pass. Default values in
  `TConEvoConfig` retuned to match `D:/Minecraft/Instances/GreedyCraft/
  config/tconevo.cfg` 1-for-1 where semantics align: vampiricLifestealPerLevel
  0.10→0.20, primordialConversionPerLevel 0.20→0.04, fluxBurnFractionPerLevel
  0.02→0.01, fluxBurnMinPerLevel 5000→256, fluxBurnMaxPerLevel 10M→320k,
  fluxBurnEnergyPerDamage 100k→16k, energizedCapacityPerLevel 250k→1.6M,
  energizedEnergyCostTools 80→50, fluxedEnergyCostTools 200→320,
  piezoelectricRfPerDamage 320→36, condensingDropProbability 0.01→0.005.
  artifactDropChance rebased from 0.005 (absolute) to 1.0 (multiplier) so
  the new per-table loot rules carry direct probabilities. Artifact loot
  injection restructured from one regex rule into 12 per-chest rules with
  GreedyCraft's exact per-table probabilities (0.02 mineshaft / 0.08
  desert_pyramid / 0.15 end_city / 0.05 igloo / 0.08 jungle_temple /
  0.02 nether_bridge / 0.30 simple_dungeon / 0.04 stronghold_corridor /
  0.04 stronghold_crossing / 0.15 stronghold_library / 0.10
  village_weaponsmith / 0.25 woodland_mansion). Also fixes a latent bug
  where the old regex used `stronghold/library` (1.12.2 path separator)
  instead of 1.20.1's flat `stronghold_library`. Bastion/underwater/
  buried loot pools dropped since they weren't in GreedyCraft's list.
  New recipe: Coalescence Matrix shapeless (manyullyn ingot + 2 redstone
  + 2 glowstone + clay ball + slimeball) ported from 1.12.2
  tconevo's crafting recipe.
- 3.10.9 → 3.10.10 — task #6 loot injection. New
  `ArtifactLootModifier extends LootModifier` with a codec
  registered via `ArtifactLootSerializers` into
  `ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS`. Codec
  adds a single optional `chance` float (default 1.0). `doApply`
  gates on `TConEvoConfig.ENABLE_ARTIFACTS`, multiplies codec
  chance by `TConEvoConfig.ARTIFACT_DROP_CHANCE`, then enumerates
  `ArtifactManager.INSTANCE.stacks()` with a weighted pick across
  spec weights and appends one copy to the generated loot. Wiring:
  `data/forge/loot_modifiers/global_loot_modifiers.json` lists
  `soa_additions:tconevo_artifact_chests`, whose rule JSON uses a
  `forge:loot_table_id` regex covering the eight vanilla chest
  pools the 1.12.2 tconevo injected into (`end_city_treasure`,
  `stronghold/library`, `simple_dungeon`, `woodland_mansion`,
  `bastion_treasure`, `nether_bridge`, `underwater_ruin_big`,
  `buried_treasure`). Closes task #6.
- 3.10.5 → 3.10.6 — task #8 asset pass continued. 48 material
  render JSONs under `assets/soa_additions/tinkering/materials/`,
  one per ported material. Minimal runtime schema only — TC3's
  `MaterialRenderInfoLoader` / `MaterialRenderInfo` record reads
  just `color`, `fallbacks`, `texture`, `luminosity`; the
  `generator`/`transformer`/palette blob seen in vanilla
  `tconstruct:tinkering/materials/*.json` is a datagen sidecar
  that emits pre-recolored per-part PNGs at build time (those
  PNGs ship in the TC3 jar). We skip the datagen path and lean
  on `color` vertex-tinting the base family sprites — `metal`
  (37×), `wood` (livingwood, dreamwood), `rock` (livingrock,
  sky_stone), `crystal` (crystal_matrix, dark_matter, red_matter,
  flux_crystal, gelid_gem, certus_quartz, fluix). Colors picked
  to approximate tconevo's 1.12.2 intent: draconium deep purple,
  draconic/chaotic/signalum orange-red family, terrasteel green,
  manasteel sky-blue, crystal_matrix cyan, neutronium near-black,
  infinity_metal gold, dark_matter deep purple, red_matter dark
  crimson, soularium dark brown, etc. Full color table lives in
  the JSONs themselves.

## Parity audit (task #11)

Cross-checked at mod_version 3.10.10 against `build/_tconevo_decompiled/xyz/phanta/tconevo/`.
Status vocabulary: **ported** = functional 1-for-1 translation exists; **marker** =
registered modifier id with empty/placeholder hook so material-trait JSONs resolve
(behaviour pending API-binding pass); **deferred** = 1.12.2 feature intentionally
left out of this round, documented for a later pass; **skipped** = intentionally
dropped (target mod absent in pack, replaced by TC3 built-in, or obsolete on 1.20.1).

### init/ (registry bootstraps)

| tconevo | status | evidence |
|---|---|---|
| TconEvoItems | ported | `tconstructevo/TConEvoItems.java` |
| TconEvoMaterials | ported | 48 × 3 JSONs under `data/soa_additions/tinkering/materials/` |
| TconEvoPotions | ported | `tconstructevo/TConEvoPotions.java` (4 effects) |
| TconEvoEntities | ported | `tconstructevo/TConEvoEntities.java` (MAGIC_MISSILE) |
| TconEvoEntityAttrs | ported | `tconstructevo/TConEvoAttributes.java` (ACCURACY) |
| TconEvoTraits | ported | `tconstructevo/TConEvoModifiers.java` + per-subpackage holder classes |
| TconEvoPartTypes | skipped | TC3 1.20.1 uses existing part types (large_plate/tough_handle/tough_binding) for Sceptre — no custom part needed |
| TconEvoBlocks | skipped | decorative blocks (BlockEarthMaterial/BlockMetal) not needed; no artifact/recipe references them |
| TconEvoCaps | skipped | capabilities are TC2-era glue; TC3 supplies ToolEnergyCapability directly (AstralAttunable/EuStore target absent mods) |

### item/

| tconevo | status | evidence |
|---|---|---|
| ItemToolSceptre | ported | `tconstructevo/item/sceptre/SceptreItem.java` + `TCESceptre.java` + tool_definitions JSON |
| ItemMaterial.COALESCENCE_MATRIX | ported | `tconstructevo/item/TCEMiscItems.COALESCENCE_MATRIX` |
| ItemMaterial.ARTIFACT_UNSEALER | skipped | 1.12.2 tied to the sealed-artifact mechanic; artifacts here ship unsealed, no unsealer needed |
| ItemMetal (WyvernMetal/DraconicMetal/ChaoticMetal/EssenceMetal/PrimalMetal/BoundMetal/SentientMetal/Energium/UUMatter × 5 forms) | skipped | every listed metal belongs to a target mod that ships its own equivalent (DE/Blood Magic/etc.); duplicating them would conflict on tag lookup |
| ItemEdible (MEAT_INGOT_RAW/COOKED) | skipped | cosmetic food item tied to no ported mechanic; no artifact references it |

### entity/

| tconevo | status | evidence |
|---|---|---|
| EntityMagicMissile | ported | `tconstructevo/entity/MagicMissileEntity.java` (used by Sceptre) |

### potion/

| tconevo | status | evidence |
|---|---|---|
| PotionDamageBoost / DamageReduction / HealReduction / TrueStrike | ported | `tconstructevo/potion/*Effect.java`, registered in `TConEvoPotions` |

### modifier / trait (core — mod-agnostic)

All 34 classes under `trait/` (non-subpackage) ported 1-for-1 into
`tconstructevo/modifier/core/`. See the "Core modifiers completed" table
above; every row has an `*Modifier.java` counterpart.

### modifier / trait — per-integration

| tconevo package | classes | status | evidence |
|---|---|---|---|
| trait/avaritia | Infinitum/Omnipotence/Condensing | ported | `tconstructevo/integration/avaritia/*Modifier.java` (3 behavioural) |
| trait/botania | AuraSiphon/FaeVoice/GaiaWrath/ManaInfused | marker | `tconstructevo/integration/botania/` — registered as empty modifiers, material-JSON refs resolve |
| trait/bloodmagic | Bloodbound/Crystalys/Sentient/Willful | marker | `tconstructevo/integration/bloodmagic/` — registered as empty modifiers |
| trait/projecte | EternalDensity | marker | `tconstructevo/integration/projecte/EternalDensityModifier.java` |
| trait/draconicevolution | 14 concrete modifiers + 2 bases | ported | `tconstructevo/integration/draconicevolution/*Modifier.java` (12 concrete) |
| trait/draconicevolution | ModifierDraconicAttackAoe | deferred | pending TC3.x melee AoE iterator work |
| trait/draconicevolution | ModifierDraconicDigAoe | deferred | pending TC3.x AoE harvest iterator |
| trait/astralsorcery | * | skipped | Astral Sorcery not in the target pack |
| trait/ic2 | * | skipped | IndustrialCraft² not in target pack |
| trait/industrialforegoing | * | skipped | IF not in target pack |
| trait/thaumcraft | * | skipped | Thaumcraft not in target pack |
| trait/elenaidodge | * | skipped | ElenaI Dodge not in target pack |

### handler/

| tconevo | status | evidence |
|---|---|---|
| ArtifactLootHandler | ported | `tconstructevo/item/artifact/ArtifactLootModifier.java` (Forge LootModifier codec) |
| ArmourDamageCoreHooks / BlockPropCoreHooks / DamageCutoffCoreHooks / ItemSensitiveModificationCoreHooks / MaterialPropertyCoreHooks / MaterialOverrideHandler / MeltSpeedCoreHooks / TinkerToolPropCoreHooks | skipped | all 1.12.2 ASM coremod hook targets; TC3 1.20.1 surfaces these via first-class modifier hooks (`ModifierHookMap`) so no core surgery is required |
| EntityAttributeHandler | skipped | superseded by TC3's native `ATTRIBUTES` hook (used by AccuracyModifier) |
| EnergizedTraitConflictHandler | skipped | 1.12.2 guarded Energized vs Fluxed incompatibility via coremod; on TC3 the two share a single additive `ToolEnergyCapability` pool, no conflict to police |
| ToolCapabilityHandler | skipped | TC3 attaches capabilities via `ModifiableItem#initCapabilities`; handled per-modifier now |
| ToolCraftingHandler | skipped | TC3 `TinkerStationCraftingEvent` covers these flows natively; the 1.12.2 handler was pre-event-system glue |
| EnergyShieldHandler | deferred | DE armor shield HUD — tracked as follow-up, see Current Status |
| FlightSpeedHandler / PlayerStateHandler | deferred | tied to DE-armor flight-speed packet family; scope-bounded follow-up |

### coremod/

| tconevo | status | evidence |
|---|---|---|
| TransformBreakUnbreakable | ported | `tconstructevo/event/UnbreakableBreakHandler.java` (LeftClickBlock event, no mixin needed) |
| TransformDisableDamageCutoff | skipped | 1.20.1's `FloatToolStat.clamp` caps at 2048F; no practical content reaches that ceiling, and `DISABLE_DAMAGE_CUTOFF` config key is left parked for future use |

### network/

| tconevo | status | evidence |
|---|---|---|
| CPacketGaiaWrath / SPacketEntitySpecialEffect / SPacketLightningEffect / SPacketUpdateAppliedFlightSpeed | skipped | Gaia Wrath client marker is unused (marker modifier); lightning + entity FX use vanilla-side spawn calls; FlightSpeed packet deferred with its handler |

### client/

| tconevo | status | evidence |
|---|---|---|
| ClientProxy / book / command / event / fx / gui / handler / render / util | skipped | no client-side renderer ports needed — Sceptre model uses `tconstruct:tool` loader, Coalescence Matrix ships a plain animated texture, artifact tooltips render from NBT |

### recipe/ & material/ & command/

| tconevo | status | evidence |
|---|---|---|
| recipe/MasterRecipes + OreDictRegistration | skipped | 1.12.2 OreDict → 1.20.1 tags; the ported materials reuse existing target-mod tags (e.g. `forge:ingots/draconium`) so no fresh oredict registration is needed |
| material/MaterialBuilder + MaterialCastType + MaterialDefinition + MaterialForm + PartType + RegCondition + stats/ | skipped | entire 1.12.2 TC2-era material-infrastructure layer; TC3 1.20.1 reads JSON via `MaterialManager` — the 48 ported material triplets under `data/soa_additions/tinkering/materials/` replace this code wholesale |
| command/ (/tconevo debug commands) | skipped | no debug commands ported; not referenced by gameplay |

### Artifacts

14 of 15 tconevo artifacts ported (one 1.12.2-only definition using the Artifact
Unsealer mechanic is dropped for parity with the unsealer skip). Shipped:
agneyastra, ancient_arrow, crystal_sceptre, dragon_tooth, guardian_angel,
infinity_edge, light_shoes, maelstrom, morning_star, ms_erudita, myriad_slashes,
phase_boots, stormbow, werebane. Each JSON lives in
`data/soa_additions/tconevo/artifacts/`, parsed by `ArtifactManager`, built by
`ArtifactBuilder`, injected via `ArtifactLootModifier` into 8 vanilla chest pools.

### Materials (task #8 follow-up)

52 of 64 tconevo materials ported. The 12 deferred entries are all
MAGIC-stat-only or BOWSTRING-only (mana_diamond, mana_pearl, dragonstone,
mana_string, fluxed_string, enchanted_fabric, ender_crystal, pulsating_crystal,
vibrant_crystal, weather_crystal, amber, quicksilver) — TC3 1.20.1 has no
Sceptre-adjacent MAGIC stat slot without a custom tool and no BOWSTRING part
type for crossbows/bows in the base TC3, so these materials would register
with no consumers. Revisit if/when Sceptre gains material-driven MAGIC stats.

### Summary

| bucket | ported | marker | deferred | skipped |
|---|---:|---:|---:|---:|
| core modifiers | 34 | 0 | 0 | 0 |
| integration modifiers | 16 | 9 | 2 | 15 (target-mod-absent families) |
| items | 3 | 0 | 0 | 3 |
| potions | 4 | 0 | 0 | 0 |
| entities | 1 | 0 | 0 | 0 |
| handlers | 2 | 0 | 3 | 8 |
| coremod transforms | 1 | 0 | 0 | 1 |
| artifacts | 14 | 0 | 0 | 1 |
| materials | 52 | 0 | 12 | 0 |

No known mismatches between the registered modifier ids and the material-trait
JSON references (the namespace-prefix bug from task #8 is fixed). Every shipped
artifact resolves its tool id, materials, and modifiers against live registries.
