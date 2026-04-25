package com.soul.soa_additions.tr.aspect.derive;

import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Last-resort aspect inference from the {@link Item} subclass and properties.
 * Catches "leaf" items that have no JSON entry, no tag membership, and no
 * recipe to derive from — mob drops (seashell), world-gen items (meteor),
 * unknown modded items. Mirrors how TC4 ensured every item had at least
 * <em>some</em> aspect signal.
 *
 * <p>Aspects from this layer COMBINE with whatever the tag and recipe
 * layers contribute; the deriver sums them all. Returning aspects here
 * doesn't suppress higher layers — they just add the "essential character"
 * the class hierarchy implies.
 */
public final class ItemClassHeuristic {

    private ItemClassHeuristic() {}

    /** Infer aspects from the item's class + food properties. Always returns
     *  something for any Item — the lower bound is a single primal based on
     *  the instance class. */
    public static List<AspectStack> infer(Item item) {
        if (item == null) return List.of();
        Map<Aspect, Integer> sum = new HashMap<>();

        // ----- Food -----
        FoodProperties food = item.getFoodProperties();
        if (food != null) {
            add(sum, Aspects.VICTUS, 1 + Math.max(0, food.getNutrition() / 4));
            if (food.isMeat()) add(sum, Aspects.BESTIA, 2);
            if (food.canAlwaysEat()) add(sum, Aspects.MESSIS, 1);
        }

        // ----- Weapons / tools / armor -----
        if (item instanceof SwordItem) {
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof AxeItem) {
            add(sum, Aspects.METO, 2);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof PickaxeItem) {
            add(sum, Aspects.PERFODIO, 2);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof ShovelItem) {
            add(sum, Aspects.PERFODIO, 1);
            add(sum, Aspects.INSTRUMENTUM, 1);
            add(sum, Aspects.TERRA, 1);
        } else if (item instanceof HoeItem) {
            add(sum, Aspects.MESSIS, 2);
            add(sum, Aspects.INSTRUMENTUM, 1);
            add(sum, Aspects.HERBA, 1);
        } else if (item instanceof ShearsItem) {
            add(sum, Aspects.METO, 1);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof BowItem) {
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.VOLATUS, 1);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof CrossbowItem) {
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.VOLATUS, 1);
            add(sum, Aspects.MACHINA, 1);
        } else if (item instanceof TridentItem) {
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.AQUA, 1);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof FishingRodItem) {
            add(sum, Aspects.INSTRUMENTUM, 1);
            add(sum, Aspects.AQUA, 1);
        } else if (item instanceof ShieldItem) {
            add(sum, Aspects.TUTAMEN, 2);
            add(sum, Aspects.INSTRUMENTUM, 1);
        } else if (item instanceof ArmorItem armor) {
            add(sum, Aspects.TUTAMEN, 2);
            // Slot flavor — head=cognitio, chest=corpus, legs=motus, feet=iter.
            switch (armor.getType()) {
                case HELMET     -> add(sum, Aspects.COGNITIO, 1);
                case CHESTPLATE -> add(sum, Aspects.CORPUS, 1);
                case LEGGINGS   -> add(sum, Aspects.MOTUS, 1);
                case BOOTS      -> add(sum, Aspects.ITER, 1);
            }
        } else if (item instanceof BowItem) {
            add(sum, Aspects.TELUM, 2);
            add(sum, Aspects.VOLATUS, 1);
        } else if (item instanceof BucketItem) {
            add(sum, Aspects.INSTRUMENTUM, 1);
            add(sum, Aspects.METALLUM, 1);
            add(sum, Aspects.VACUOS, 1);
        } else if (item instanceof BoatItem) {
            add(sum, Aspects.HERBA, 1);
            add(sum, Aspects.ITER, 2);
            add(sum, Aspects.MOTUS, 1);
        } else if (item instanceof RecordItem) {
            add(sum, Aspects.SENSUS, 2);
            add(sum, Aspects.AURAM, 1);
        } else if (item instanceof BlockItem bi) {
            // Generic block — terra is always present even if metallic/
            // wooden/etc. The tag/recipe layers add specific character on top.
            add(sum, Aspects.TERRA, 1);
            // Property-driven enrichment: every Block carries hardness,
            // explosion resistance, light emission, and a map color, all
            // free to read. Lets us give obsidian (huge blast resistance)
            // tutamen, glowstone (light) lux, and so on without needing
            // explicit tag/JSON entries for every block in the game.
            try {
                var block = bi.getBlock();
                var props = block.defaultBlockState();
                // Blast resistance scale: vanilla obsidian = 1200, deepslate = 6,
                // stone = 6, dirt = 0.5. >100 means "very tough" → tutamen.
                float resistance = block.getExplosionResistance();
                if (resistance >= 100f) {
                    add(sum, Aspects.TUTAMEN, 2);
                    add(sum, Aspects.POTENTIA, 1);
                } else if (resistance >= 5f) {
                    add(sum, Aspects.TUTAMEN, 1);
                }
                // Light emission → lux (glowstone, sea lantern, shroomlight,
                // amethyst clusters, etc.).
                int light = props.getLightEmission();
                if (light >= 12) add(sum, Aspects.LUX, 2);
                else if (light >= 4) add(sum, Aspects.LUX, 1);
                // Map color brightness → tenebrae for dark blocks (obsidian
                // is BLACK, deepslate is DEEPSLATE-grey, etc.). Cheap
                // RGB-mean check.
                var map = props.getMapColor(null, net.minecraft.core.BlockPos.ZERO);
                if (map != null) {
                    int rgb = map.col;
                    int brightness = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    if (brightness < 40) add(sum, Aspects.TENEBRAE, 1);
                }
                // Special: redstone-conducting → potentia.
                if (props.isSignalSource()) add(sum, Aspects.POTENTIA, 1);
            } catch (Throwable ignored) {
                // Any block whose property access throws (modded weirdness,
                // blocks needing a Level for state) — skip the bonus aspects,
                // keep the base terra. Better than crashing the deriver.
            }
        }

        // ----- Damage-able tools (catches modded tools we didn't class-match) -----
        if (item.canBeDepleted() && sum.getOrDefault(Aspects.INSTRUMENTUM, 0) == 0) {
            add(sum, Aspects.INSTRUMENTUM, 1);
        }

        // ----- Last-resort minimum signal -----
        // Every item gets at least one aspect, even if it didn't match any
        // class above — id-hash-derived primal so the "feels random" but
        // deterministic. Prevents the player ever seeing "scanned but no
        // aspects" which is the worst UX.
        if (sum.isEmpty()) {
            Aspect fallback = primalFromIdHash(item);
            add(sum, fallback, 1);
        }

        List<AspectStack> out = new ArrayList<>(sum.size());
        sum.forEach((a, amt) -> out.add(new AspectStack(a, amt)));
        return out;
    }

    private static void add(Map<Aspect, Integer> sum, Aspect a, int n) {
        sum.merge(a, n, Integer::sum);
    }

    /** Deterministic primal pick based on item id — same item always gets
     *  the same fallback, but unrelated items spread evenly across primals. */
    private static Aspect primalFromIdHash(Item item) {
        var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        Aspect[] primals = {
                Aspects.AER, Aspects.AQUA, Aspects.IGNIS,
                Aspects.TERRA, Aspects.ORDO, Aspects.PERDITIO
        };
        int hash = id == null ? 0 : (id.hashCode() & 0x7FFFFFFF);
        return primals[hash % primals.length];
    }
}
