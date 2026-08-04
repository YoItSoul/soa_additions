package com.soul.soa_additions.itemstages;

import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Set;

/**
 * GreedyCraft's two NBT-qualified stage gates.
 *
 * <p>GreedyCraft gated a handful of entries by NBT rather than by item id
 * (items.zs:471-477 and 428-429):</p>
 * <ul>
 *   <li>diamond sword/pickaxe/helmet/chestplate/leggings/boots carrying an
 *       enchantment at level 10 &rarr; {@code wielder_of_infinity}</li>
 *   <li>enchanted books storing an enchantment at level 5 or 10 &rarr;
 *       {@code hardmode}</li>
 * </ul>
 *
 * <p>These are reward-tier items — GreedyCraft hands them out through loot
 * tables and quest rewards rather than letting the enchanting table produce
 * them, which is why ordinary diamond gear and ordinary enchanted books stay
 * ungated. An earlier port flattened these onto the plain items, locking basic
 * diamond tools and every enchanted book behind endgame stages.</p>
 *
 * <p><b>Why this is Java and not a script.</b> ItemStages exposes
 * {@code restrict(Predicate<IItemStack>, String...)} to CraftTweaker, but the
 * level test cannot be written in ZenScript: {@code stack.enchantments} is a
 * {@code Map<Enchantment, Integer>}, and ZenCode compiles a comparison against
 * its boxed values into a primitive {@code if_icmpge}, so the generated
 * predicate class fails JVM verification before it runs. An explicit
 * {@code as int} cast does not change the emitted bytecode. In Java the
 * unboxing is ours to control.</p>
 *
 * <p>Registration is driven from a mixin on {@link RestrictionManager}'s reload
 * hook rather than a reload listener of our own, because Forge gives no way to
 * order one mod's listener after another's — and these restrictions have to be
 * added <em>after</em> ItemStages rebuilds its table, or they are wiped.</p>
 */
public final class NbtStageGates {

    /** The six items GreedyCraft listed in its creative-tier diamond block. */
    private static final Set<Item> DIAMOND_GEAR = Set.of(
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS);

    private static final int DIAMOND_GEAR_MIN_LEVEL = 10;
    private static final int ENCHANTED_BOOK_MIN_LEVEL = 5;

    private NbtStageGates() {}

    /**
     * Adds both gates to the restriction table. Called after ItemStages has
     * rebuilt it, so it must be safe to run repeatedly.
     */
    public static void register() {
        try {
            RestrictionManager manager = RestrictionManager.INSTANCE;

            manager.addRestriction(new Restriction("wielder_of_infinity")
                    .restrict(stack -> DIAMOND_GEAR.contains(stack.getItem())
                            && highestEnchantment(stack) >= DIAMOND_GEAR_MIN_LEVEL));

            manager.addRestriction(new Restriction("hardmode")
                    .restrict(stack -> stack.is(Items.ENCHANTED_BOOK)
                            && highestEnchantment(stack) >= ENCHANTED_BOOK_MIN_LEVEL));
        } catch (Throwable t) {
            // Never let a gate registration failure break the datapack reload.
            org.slf4j.LoggerFactory.getLogger("SOA_NbtStageGates")
                    .error("Could not register NBT stage gates", t);
        }
    }

    /**
     * Highest enchantment level on the stack, or 0.
     *
     * <p>One accessor serves both gates: vanilla's
     * {@code ItemStack.getEnchantmentTags()} — which
     * {@link EnchantmentHelper#getEnchantments} reads — returns
     * {@code StoredEnchantments} for an enchanted book and {@code Enchantments}
     * for everything else.</p>
     */
    private static int highestEnchantment(ItemStack stack) {
        if (stack.isEmpty() || !stack.isEnchanted() && !stack.is(Items.ENCHANTED_BOOK)) {
            return 0;
        }
        int highest = 0;
        for (Integer level : EnchantmentHelper.getEnchantments(stack).values()) {
            if (level != null && level > highest) {
                highest = level;
            }
        }
        return highest;
    }
}
