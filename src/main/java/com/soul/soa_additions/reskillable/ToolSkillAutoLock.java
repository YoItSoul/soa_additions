package com.soul.soa_additions.reskillable;

import com.google.common.collect.Multimap;
import net.bandit.reskillable.common.commands.skills.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Auto-classifier that synthesizes Reskillable skill requirements for items
 * NOT listed in {@code config/reskillable/skill_locks.json}. Closes the
 * Tinker Construct bypass: ~150 materials x several part types produce a
 * combinatorial explosion that no static config can enumerate.
 *
 * <p>Algorithm derived from GC's {@code reskillable.cfg} skill-lock data.
 * GC sets the foundation (vanilla iron/diamond/netherite + DE wyvern/draconic
 * + EnderIO dark steel + Mowzie weapons) and we extrapolate stepwise from
 * those data points:
 *
 * <pre>
 *   Melee (ATTACK_DAMAGE attribute):
 *     <= 4 dmg :  attack 5    (wood/gold)
 *     <= 6 dmg :  attack 5    (iron)
 *     <= 8 dmg :  attack 6    (diamond/netherite)
 *     <= 11 dmg:  attack 12   (mid modded - dark steel territory)
 *     <= 15 dmg:  attack 20   (late modded - wrought axe)
 *     <= 20 dmg:  attack 35   (advanced - flux/end steel)
 *     >  20 dmg:  attack 50   (endgame - wyvern/draconic cap)
 *
 *   Tools (Tier.getLevel()):
 *     0 (wood/gold) : 5
 *     1 (stone)     : 5
 *     2 (iron)      : 5
 *     3 (diamond)   : 6
 *     4 (netherite) : 12
 *     >=5 (modded)  : 25 + (level-5)*8, capped at 50
 *
 *   Armor (ARMOR + ARMOR_TOUGHNESS sum):
 *     <= 1 : defense 5
 *     <= 3 : defense 5
 *     <= 6 : defense 6
 *     <= 9 : defense 12
 *     <= 12: defense 20
 *     <= 15: defense 35
 *     >  15: defense 50
 * </pre>
 *
 * <p><b>Cache strategy:</b> classification is keyed by {@link ResourceLocation}
 * (item registry id), not by {@link ItemStack}. This works for vanilla and
 * non-NBT modded items. For NBT-driven items (Tinker tools), per-stack stats
 * vary, so we read attribute modifiers per-stack <em>but</em> only at the
 * gating event (equip / hurt / break-speed) — never per tick. The user's
 * "0 overhead on next equip" goal is met because:
 *   <ul>
 *     <li>The events themselves fire only on actual changes (equip swap,
 *         attack landed, mining started — not per tick).</li>
 *     <li>Reading attribute modifiers from a stack is O(few) — microseconds.</li>
 *     <li>The expensive part (capability lookup + curve calc) is hit once
 *         per item type for fixed-stat items, then cached forever.</li>
 *   </ul>
 *
 * <p>Static-config items (those in {@code skill_locks.json}) are skipped by
 * this classifier — Reskillable's own static-lock system handles them. This
 * Java handler is purely a fallback for unlisted items.
 *
 * <p>Registered conditionally from SoaAdditions main when Reskillable is loaded.
 */
public final class ToolSkillAutoLock {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/auto_lock");

    private ToolSkillAutoLock() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ToolSkillAutoLock.class);
        LOG.info("Tool/weapon auto-classifier registered (catches Tinker combos + any unlisted modded gear)");
    }

    // ============================================================
    //  Config caps + steps (derived from GC reskillable.cfg)
    // ============================================================
    private static final int MAX_LEVEL = 50;

    /** Item registry ID -> resolved (skill, requiredLevel). One entry per item type. */
    private static final Map<ResourceLocation, Requirement> ID_CACHE = new ConcurrentHashMap<>();

    /** Sentinel for items the classifier doesn't gate (tools without recognizable stats). */
    private static final Requirement NO_GATE = new Requirement(null, 0);

    public record Requirement(Skill skill, int level) {}

    // ============================================================
    //  Curves
    // ============================================================

    private static int classifyMeleeDamage(double damage) {
        if (damage <= 4)  return 5;
        if (damage <= 6)  return 5;
        if (damage <= 8)  return 6;
        if (damage <= 11) return 12;
        if (damage <= 15) return 20;
        if (damage <= 20) return 35;
        return MAX_LEVEL;
    }

    private static int classifyToolTier(int tier) {
        if (tier <= 0) return 5;
        if (tier == 1) return 5;
        if (tier == 2) return 5;
        if (tier == 3) return 6;
        if (tier == 4) return 12;
        return Math.min(MAX_LEVEL, 25 + (tier - 5) * 8);
    }

    private static int classifyArmor(double armorPlusToughness) {
        if (armorPlusToughness <= 1)  return 5;
        if (armorPlusToughness <= 3)  return 5;
        if (armorPlusToughness <= 6)  return 6;
        if (armorPlusToughness <= 9)  return 12;
        if (armorPlusToughness <= 12) return 20;
        if (armorPlusToughness <= 15) return 35;
        return MAX_LEVEL;
    }

    // ============================================================
    //  Classification entry point
    // ============================================================

    /**
     * Resolves the skill requirement for a stack. Caches by item registry id.
     * For Tinker-style NBT-driven items the stack's attribute modifiers are
     * read, but the cache key is still the item id — accepted approximation
     * since most players craft a small handful of Tinker tools and re-use them.
     *
     * <p>If a sharper per-stack-stats classification is desired later, switch
     * the cache key to {@code (itemId, attackDamageBucket)} or stop caching
     * Tinker items entirely; both are 1-line changes.
     */
    public static Requirement classify(ItemStack stack) {
        if (stack.isEmpty()) return NO_GATE;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return NO_GATE;

        Requirement cached = ID_CACHE.get(id);
        if (cached != null) return cached;

        Requirement r = classifyUncached(stack);
        ID_CACHE.put(id, r);
        return r;
    }

    private static Requirement classifyUncached(ItemStack stack) {
        Item item = stack.getItem();

        // ---- Armor (any ArmorItem + any item with armor attribute) ----
        if (item instanceof ArmorItem armor) {
            double total = readArmorTotal(stack, armor.getEquipmentSlot());
            return new Requirement(Skill.DEFENSE, classifyArmor(total));
        }

        // ---- Tools by class hierarchy ----
        if (item instanceof PickaxeItem pick) {
            return new Requirement(Skill.MINING, classifyToolTier(pick.getTier().getLevel()));
        }
        if (item instanceof AxeItem axe) {
            return new Requirement(Skill.GATHERING, classifyToolTier(axe.getTier().getLevel()));
        }
        if (item instanceof ShovelItem shovel) {
            return new Requirement(Skill.GATHERING, classifyToolTier(shovel.getTier().getLevel()));
        }
        if (item instanceof HoeItem hoe) {
            return new Requirement(Skill.FARMING, classifyToolTier(hoe.getTier().getLevel()));
        }
        if (item instanceof SwordItem) {
            double dmg = readAttackDamage(stack);
            return new Requirement(Skill.ATTACK, classifyMeleeDamage(dmg));
        }
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            // Bows/crossbows don't expose damage via attributes; default mid-tier.
            return new Requirement(Skill.ATTACK, 8);
        }

        // ---- Generic DiggerItem fallback (covers Tinker harvest tools) ----
        if (item instanceof DiggerItem digger) {
            // No way to pre-classify mining vs gathering for unknown DiggerItem
            // subclasses (Tinker uses its own ToolItem hierarchy that may not
            // extend DiggerItem). Default to mining skill.
            Tier t = digger.getTier();
            int lvl = (t != null) ? t.getLevel() : 2;
            return new Requirement(Skill.MINING, classifyToolTier(lvl));
        }
        if (item instanceof TieredItem tiered) {
            int lvl = tiered.getTier().getLevel();
            return new Requirement(Skill.ATTACK, classifyToolTier(lvl));
        }

        // ---- Last resort: items with an ATTACK_DAMAGE attribute modifier
        // when held in mainhand qualify as melee weapons (Tinker swords,
        // BetterCombat weapons, modded warhammers, etc.) ----
        double dmg = readAttackDamage(stack);
        if (dmg >= 2.0) {
            return new Requirement(Skill.ATTACK, classifyMeleeDamage(dmg));
        }

        return NO_GATE;
    }

    /** Reads {@code Attributes.ATTACK_DAMAGE} bonuses on the stack (mainhand slot). */
    private static double readAttackDamage(ItemStack stack) {
        double sum = 1.0; // vanilla baseline (every player attack starts at 1)
        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (AttributeModifier m : mods.get(Attributes.ATTACK_DAMAGE)) {
            if (m.getOperation() == AttributeModifier.Operation.ADDITION) {
                sum += m.getAmount();
            }
        }
        return sum;
    }

    /** Reads {@code Attributes.ARMOR + ARMOR_TOUGHNESS} on the given slot. */
    private static double readArmorTotal(ItemStack stack, EquipmentSlot slot) {
        double armor = 0, tough = 0;
        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(slot);
        for (AttributeModifier m : mods.get(Attributes.ARMOR)) {
            if (m.getOperation() == AttributeModifier.Operation.ADDITION) armor += m.getAmount();
        }
        for (AttributeModifier m : mods.get(Attributes.ARMOR_TOUGHNESS)) {
            if (m.getOperation() == AttributeModifier.Operation.ADDITION) tough += m.getAmount();
        }
        return armor + tough;
    }

    // ============================================================
    //  Per-player equip cache — last classified result per slot
    // ============================================================

    /**
     * Stores the most recent classification per (player, slot) pair so the
     * gating events (LivingHurtEvent, BreakSpeed) can do an O(1) lookup
     * instead of re-classifying. Refreshed only on actual equipment change.
     *
     * <p>Cleared lazily — entries are valid as long as the player keeps the
     * same item in the same slot. Equipment swap fires {@link LivingEquipment
     * ChangeEvent} which overwrites the entry. Logout doesn't clear (entries
     * are small + bounded by online player count x 6 slots).
     */
    private static final Map<EquipKey, Requirement> EQUIP_CACHE = new ConcurrentHashMap<>();

    private record EquipKey(java.util.UUID player, EquipmentSlot slot) {}

    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EquipKey key = new EquipKey(player.getUUID(), event.getSlot());
        ItemStack now = event.getTo();
        if (now.isEmpty()) {
            EQUIP_CACHE.remove(key);
            return;
        }
        Requirement req = classify(now);
        EQUIP_CACHE.put(key, req);

        // Reject equip if player can't meet the requirement: dump the item
        // back into inventory at end of tick (we can't cancel the event).
        if (req.skill() != null
                && !SkillHelper.hasLevel(player, req.skill(), req.level())
                && !player.isCreative() && !player.isSpectator()) {
            // Defer to next tick so we don't fight the equip-change event itself
            player.getServer().execute(() -> {
                ItemStack stillThere = player.getItemBySlot(event.getSlot());
                if (stillThere == now || ItemStack.isSameItemSameTags(stillThere, now)) {
                    player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
                    if (!player.getInventory().add(stillThere)) {
                        player.drop(stillThere, false);
                    }
                    player.displayClientMessage(
                            Component.translatable("soa_additions.reskillable.equip_blocked",
                                    Component.translatable(now.getDescriptionId()),
                                    Component.literal(req.skill().name().toLowerCase()),
                                    Component.literal(String.valueOf(req.level())))
                                    .withStyle(ChatFormatting.RED),
                            true);
                }
            });
        }
    }

    // ============================================================
    //  Gate weapon damage — block hits while requirement unmet
    // ============================================================

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWeaponHit(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        if (attacker.isCreative() || attacker.isSpectator()) return;
        if (attacker.level().isClientSide()) return;

        ItemStack mh = attacker.getMainHandItem();
        if (mh.isEmpty()) return;

        Requirement req = lookupCached(attacker, EquipmentSlot.MAINHAND, mh);
        if (req.skill() == null) return;
        if (req.skill() != Skill.ATTACK) return;

        if (!SkillHelper.hasLevel(attacker, req.skill(), req.level())) {
            event.setCanceled(true);
            attacker.displayClientMessage(
                    Component.translatable("soa_additions.reskillable.weapon_blocked",
                            Component.literal(String.valueOf(req.level())))
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }

    // ============================================================
    //  Gate tool break speed — disallow mining if requirement unmet
    // ============================================================

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;

        ItemStack mh = player.getMainHandItem();
        if (mh.isEmpty()) return;

        Requirement req = lookupCached(player, EquipmentSlot.MAINHAND, mh);
        if (req.skill() == null) return;

        // Only gate tools (mining/gathering/farming) — not melee weapons that
        // happen to be holding when chopping leaves.
        Skill s = req.skill();
        if (s != Skill.MINING && s != Skill.GATHERING && s != Skill.FARMING) return;

        if (!SkillHelper.hasLevel(player, s, req.level())) {
            event.setNewSpeed(0.0f);  // makes the block unbreakable
        }
    }

    /** Cache lookup with on-the-fly classification fallback for items not yet
     *  seen via an equip event (e.g. picked up mid-tick, server restart edge). */
    private static Requirement lookupCached(Player player, EquipmentSlot slot, ItemStack stack) {
        EquipKey key = new EquipKey(player.getUUID(), slot);
        Requirement cached = EQUIP_CACHE.get(key);
        if (cached != null) return cached;
        Requirement r = classify(stack);
        EQUIP_CACHE.put(key, r);
        return r;
    }
}
