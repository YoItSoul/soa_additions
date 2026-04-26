package com.soul.soa_additions.reskillable;

import net.bandit.reskillable.common.commands.skills.Skill;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Reproduces GreedyCraft's 9 custom Reskillable traits from
 * {@code scripts/compat/compatskills/traits.zs}. Reskillable 1.20.1 dropped
 * the {@code TraitCreator} CT API, so each trait is reimplemented as a
 * Forge event handler gated on a {@link SkillHelper} skill-level check.
 *
 * <p>The level thresholds and proc rates match GC verbatim. The only
 * substitution is item IDs that became absent in 1.20.1:
 *   <ul>
 *     <li>{@code additions:greedycraft-experience_ingot} →
 *         {@code soa_additions:experience_ingot}</li>
 *     <li>{@code mysticalagriculture:crafting} (inferium essence meta:0) →
 *         {@code mysticalagriculture:inferium_essence}</li>
 *     <li>{@code potion:potioncore:reach} →
 *         {@code soa_additions:reach} (PotionCore port)</li>
 *     <li>Building gadgets (1.12 buildinggadgets:*) →
 *         {@code buildinggadgets2:*} (1.20 fork)</li>
 *   </ul>
 *
 * <p>Subscribed via {@link #init()} from SoaAdditions main, gated on
 * {@code ModList.isLoaded("reskillable")}. Class never loads if Reskillable
 * is absent.
 */
public final class ReskillableTraits {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/reskillable_traits");

    private ReskillableTraits() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ReskillableTraits.class);
        LOG.info("Reskillable trait handlers registered (9 traits — bloodlust, fortified, "
                + "experience_grinder, turbo_miner, essence_reaper, magic_brew, strip_miner, "
                + "building_master, devourer_of_souls)");
    }

    // ---- Per-trait config thresholds (GC verbatim) ----
    private static final int  BLOODLUST_ATTACK     = 14;
    private static final double BLOODLUST_CHANCE   = 0.20;
    private static final int  FORTIFIED_DEFENSE    = 16;
    private static final double FORTIFIED_CHANCE   = 0.10;
    private static final float FORTIFIED_DMG_THRESH = 3.0f;
    private static final int  XP_GRINDER_GATHERING = 10;
    private static final double XP_GRINDER_CHANCE  = 0.10;
    private static final int  TURBO_MINER_MINING   = 12;
    private static final float TURBO_MINER_MULT    = 1.5f;
    private static final int  ESSENCE_REAPER_FARMING = 12;
    private static final double ESSENCE_REAPER_CHANCE = 0.30;
    private static final int  MAGIC_BREW_MAGIC     = 32;
    private static final int  MAGIC_BREW_ATTACK    = 26;
    private static final double MAGIC_BREW_PROC    = 0.00125;
    private static final int  STRIP_MINER_MINING   = 30;
    private static final int  STRIP_MINER_DEFENSE  = 25;
    private static final float STRIP_MINER_MULT    = 5.0f;
    private static final int  BUILDING_MASTER_AGILITY  = 16;
    private static final int  BUILDING_MASTER_BUILDING = 20;
    private static final int  DEVOURER_FARMING     = 34;
    private static final int  DEVOURER_GATHERING   = 28;
    private static final double DEVOURER_CHANCE    = 0.15;

    // Buff durations (ticks). 50t = 2.5s, 200t = 10s.
    private static final int SHORT_BUFF = 50;
    private static final int LONG_BUFF  = 200;

    // Cached references resolved lazily — avoids hitting registries before
    // Forge finishes initial registration.
    private static MobEffect reachEffect = null;
    private static Item experienceIngot = null;
    private static Item inferiumEssence = null;
    private static MobEffect getReach() {
        if (reachEffect == null) {
            reachEffect = ForgeRegistries.MOB_EFFECTS.getValue(
                    new ResourceLocation("soa_additions", "reach"));
        }
        return reachEffect;
    }
    private static Item getExperienceIngot() {
        if (experienceIngot == null) {
            experienceIngot = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("soa_additions", "experience_ingot"));
        }
        return experienceIngot;
    }
    private static Item getInferiumEssence() {
        if (inferiumEssence == null) {
            inferiumEssence = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("mysticalagriculture", "inferium_essence"));
        }
        return inferiumEssence;
    }

    /** Resolves the player who actually killed an entity, or null if none. */
    private static Player playerKillerOf(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player p) return p;
        return null;
    }

    private static Player playerKillerOf(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player p) return p;
        return null;
    }

    /** Adds an effect with showParticles=false, showIcon=false to keep it visually quiet. */
    private static void addQuietEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier) {
        if (effect == null) return;
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
    }

    // ============================================================
    //  bloodlust — attack 14 — 20% Regen II 2.5s on mob kill
    // ============================================================
    @SubscribeEvent
    public static void onBloodlust(LivingDeathEvent event) {
        Player killer = playerKillerOf(event);
        if (killer == null) return;
        if (!SkillHelper.hasLevel(killer, Skill.ATTACK, BLOODLUST_ATTACK)) return;
        if (ThreadLocalRandom.current().nextDouble() >= BLOODLUST_CHANCE) return;
        addQuietEffect(killer, MobEffects.REGENERATION, SHORT_BUFF, 1);
    }

    // ============================================================
    //  fortified — defense 16 — 10% Resistance II 2.5s when hit ≥3 dmg
    // ============================================================
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFortified(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAmount() < FORTIFIED_DMG_THRESH) return;
        if (!SkillHelper.hasLevel(player, Skill.DEFENSE, FORTIFIED_DEFENSE)) return;
        if (ThreadLocalRandom.current().nextDouble() >= FORTIFIED_CHANCE) return;
        addQuietEffect(player, MobEffects.DAMAGE_RESISTANCE, SHORT_BUFF, 1);
    }

    // ============================================================
    //  experience_grinder — gathering 10 — 10% drop 1-2 experience_ingot
    //  essence_reaper    — farming 12  — 30% drop 1-4 inferium_essence
    //  devourer_of_souls — farming 34 + gathering 28 — 15% restore 1-2 hunger
    //  Combined into a single LivingDrops handler since GC ran them on the
    //  same event family.
    // ============================================================
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        Player killer = playerKillerOf(event);
        if (killer == null) return;
        Level level = killer.level();
        if (level.isClientSide()) return;

        // experience_grinder
        if (SkillHelper.hasLevel(killer, Skill.GATHERING, XP_GRINDER_GATHERING)
                && ThreadLocalRandom.current().nextDouble() < XP_GRINDER_CHANCE) {
            Item xp = getExperienceIngot();
            if (xp != null) {
                int amount = 1 + ThreadLocalRandom.current().nextInt(2);  // 1-2
                event.getDrops().add(new ItemEntity(level,
                        event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        new ItemStack(xp, amount)));
            }
        }

        // essence_reaper
        if (SkillHelper.hasLevel(killer, Skill.FARMING, ESSENCE_REAPER_FARMING)
                && ThreadLocalRandom.current().nextDouble() < ESSENCE_REAPER_CHANCE) {
            Item essence = getInferiumEssence();
            if (essence != null) {
                int amount = 1 + ThreadLocalRandom.current().nextInt(4);  // 1-4
                event.getDrops().add(new ItemEntity(level,
                        event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(),
                        new ItemStack(essence, amount)));
            }
        }

        // devourer_of_souls
        if (SkillHelper.hasLevels(killer, Skill.FARMING, DEVOURER_FARMING,
                                          Skill.GATHERING, DEVOURER_GATHERING)
                && ThreadLocalRandom.current().nextDouble() < DEVOURER_CHANCE) {
            int hunger = 1 + ThreadLocalRandom.current().nextInt(2);  // 1-2
            killer.getFoodData().eat(hunger, 0.0f);
        }
    }

    // ============================================================
    //  turbo_miner — mining 12 — break speed × 1.5 on all blocks
    //  strip_miner — mining 30 + defense 25 — break speed × 5 on hardened
    //                stone-class blocks (vanilla 'deepslate' family)
    // ============================================================
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        // strip_miner first — multiplies on top of turbo_miner if both apply
        BlockState state = event.getState();
        if (isHardenedStone(state)
                && SkillHelper.hasLevels(player, Skill.MINING, STRIP_MINER_MINING,
                                                 Skill.DEFENSE, STRIP_MINER_DEFENSE)) {
            event.setNewSpeed(event.getNewSpeed() * STRIP_MINER_MULT);
        }

        // turbo_miner — generic ×1.5 on every block break
        if (SkillHelper.hasLevel(player, Skill.MINING, TURBO_MINER_MINING)) {
            event.setNewSpeed(event.getNewSpeed() * TURBO_MINER_MULT);
        }
    }

    /** GC's 1.12 'hardened_stone' check matched the {@code .id.contains("hardened_stone")}
     *  pattern. 1.20 doesn't have hardened_stone; the analog is the deepslate
     *  family (deepslate, cobbled_deepslate, polished_deepslate, *_bricks).
     *  Net intent: faster mining of high-tier rock variants players hit deep underground. */
    private static boolean isHardenedStone(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.DEEPSLATE) return true;
        if (block == Blocks.COBBLED_DEEPSLATE) return true;
        if (block == Blocks.POLISHED_DEEPSLATE) return true;
        if (block == Blocks.DEEPSLATE_BRICKS) return true;
        if (block == Blocks.DEEPSLATE_TILES) return true;
        // Mod hardened-stone equivalents (Quark gamma_stone, etc.) — match by tag if available
        TagKey<Block> deepslateTag = BlockTags.create(new ResourceLocation("forge", "stone/deepslate"));
        return state.is(deepslateTag);
    }

    // ============================================================
    //  magic_brew — magic 32 + attack 26 — every 10t poll, tiny chance
    //  for each of 7 buffs at level II for 10s
    //  building_master — agility 16 + building 20 — while holding a
    //  Building Gadgets item, jumpboost II + speed I + reach II for 2.5s
    // ============================================================
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player == null || player.level().isClientSide()) return;

        long time = player.level().getGameTime();

        // building_master — every 5t poll while holding a building gadget
        if (time % 5 == 0 && hasBuildingGadgetEquipped(player)
                && SkillHelper.hasLevels(player, Skill.AGILITY, BUILDING_MASTER_AGILITY,
                                                 Skill.BUILDING, BUILDING_MASTER_BUILDING)) {
            addQuietEffect(player, MobEffects.JUMP, SHORT_BUFF, 1);
            addQuietEffect(player, MobEffects.MOVEMENT_SPEED, SHORT_BUFF, 0);
            MobEffect reach = getReach();
            if (reach != null) addQuietEffect(player, reach, SHORT_BUFF, 1);
        }

        // magic_brew — every 10t poll, 7 independent rolls for buffs
        if (time % 10 == 0
                && SkillHelper.hasLevels(player, Skill.MAGIC, MAGIC_BREW_MAGIC,
                                                 Skill.ATTACK, MAGIC_BREW_ATTACK)) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.REGENERATION,      LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.DAMAGE_RESISTANCE, LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.DAMAGE_BOOST,      LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.LUCK,              LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.ABSORPTION,        LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.MOVEMENT_SPEED,    LONG_BUFF, 1);
            if (rng.nextDouble() < MAGIC_BREW_PROC) addQuietEffect(player, MobEffects.JUMP,              LONG_BUFF, 1);
        }
    }

    private static boolean hasBuildingGadgetEquipped(Player player) {
        ItemStack mh = player.getMainHandItem();
        ItemStack oh = player.getOffhandItem();
        return isBuildingGadget(mh) || isBuildingGadget(oh);
    }

    /** Matches BuildingGadgets2 (1.20.1 fork of the 1.12 buildinggadgets mod).
     *  IDs: buildinggadgets2:gadget_building / gadget_exchanging / gadget_copy_paste / gadget_destruction. */
    private static boolean isBuildingGadget(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        return "buildinggadgets2".equals(id.getNamespace())
                && id.getPath().startsWith("gadget_");
    }

    /** Resolves a BlockPos for the entity. Used by drops handler to position
     *  fresh ItemEntity drops at the kill location. */
    @SuppressWarnings("unused")
    private static BlockPos posOf(LivingEntity entity) {
        return entity.blockPosition();
    }
}
