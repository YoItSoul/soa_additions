package com.soul.soa_additions.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Global loot modifier that injects weighted item entries into a matching
 * loot table. Mirrors GreedyCraft's {@code LootUtil.addItem} semantics:
 * per invocation, perform {@code rolls} picks from a weighted pool; for each
 * pick, spawn a stack with count in [minCount, maxCount]. The pack JSON
 * carries the per-table conditions (e.g. a {@code forge:loot_table_id}
 * predicate) plus the entry list.
 */
public class AddItemLootModifier extends LootModifier {

    public static final Codec<AddItemLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst)
                    .and(Codec.INT.optionalFieldOf("rolls", 1).forGetter(m -> m.rolls))
                    .and(Entry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries))
                    .apply(inst, AddItemLootModifier::new));

    private final int rolls;
    private final List<Entry> entries;
    private final int totalWeight;

    public AddItemLootModifier(LootItemCondition[] conditions, int rolls, List<Entry> entries) {
        super(conditions);
        this.rolls = rolls;
        this.entries = entries;
        int sum = 0;
        for (Entry e : entries) sum += Math.max(1, e.weight);
        this.totalWeight = sum;
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (entries.isEmpty() || totalWeight <= 0) return generatedLoot;
        for (int r = 0; r < rolls; r++) {
            int pick = context.getRandom().nextInt(totalWeight);
            int cum = 0;
            for (Entry entry : entries) {
                cum += Math.max(1, entry.weight);
                if (pick < cum) {
                    int min = Math.min(entry.minCount, entry.maxCount);
                    int max = Math.max(entry.minCount, entry.maxCount);
                    int count = min >= max ? min : min + context.getRandom().nextInt(max - min + 1);
                    if (count > 0) {
                        generatedLoot.add(new ItemStack(entry.item, count));
                    }
                    break;
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public record Entry(Item item, int weight, int minCount, int maxCount) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("item").xmap(
                        rl -> BuiltInRegistries.ITEM.get(rl),
                        item -> BuiltInRegistries.ITEM.getKey(item)
                ).forGetter(Entry::item),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight),
                Codec.INT.optionalFieldOf("min", 1).forGetter(Entry::minCount),
                Codec.INT.optionalFieldOf("max", 1).forGetter(Entry::maxCount)
        ).apply(inst, Entry::new));
    }
}
