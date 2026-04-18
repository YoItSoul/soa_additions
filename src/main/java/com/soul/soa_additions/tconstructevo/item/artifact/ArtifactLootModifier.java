package com.soul.soa_additions.tconstructevo.item.artifact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Forge global loot modifier that injects a single tconevo artifact into a
 * loot pool whose conditions match. Artifact selection is weighted by each
 * spec's {@code weight} field; the modifier's {@code chance} parameter
 * gates whether any artifact rolls at all (analogous to the {@code DropChance}
 * field on the 1.12.2 ArtifactLootEntry).
 *
 * <p>Sub-pack JSONs at {@code data/soa_additions/loot_modifiers/*.json}
 * declare which loot tables receive artifacts via the standard
 * {@code conditions} array (e.g. a {@code minecraft:loot_table_id} condition).</p>
 */
public class ArtifactLootModifier extends LootModifier {

    public static final Codec<ArtifactLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst)
                    .and(Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(m -> m.chance))
                    .apply(inst, ArtifactLootModifier::new));

    private final float chance;

    public ArtifactLootModifier(LootItemCondition[] conditions, float chance) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!TConEvoConfig.ENABLE_ARTIFACTS.get()) return generatedLoot;
        float effectiveChance = chance * TConEvoConfig.ARTIFACT_DROP_CHANCE.get().floatValue();
        if (effectiveChance <= 0F || context.getRandom().nextFloat() >= effectiveChance) return generatedLoot;

        Map<net.minecraft.resources.ResourceLocation, ItemStack> stacks =
                ArtifactManager.INSTANCE.stacks();
        if (stacks.isEmpty()) return generatedLoot;

        int totalWeight = 0;
        List<WeightedEntry> entries = new ArrayList<>(stacks.size());
        for (Map.Entry<net.minecraft.resources.ResourceLocation, ItemStack> e : stacks.entrySet()) {
            ArtifactSpec spec = ArtifactManager.INSTANCE.get(e.getKey());
            int w = spec != null ? Math.max(1, spec.weight) : 1;
            totalWeight += w;
            entries.add(new WeightedEntry(w, e.getValue()));
        }
        int roll = context.getRandom().nextInt(totalWeight);
        int cum = 0;
        for (WeightedEntry entry : entries) {
            cum += entry.weight;
            if (roll < cum) {
                generatedLoot.add(entry.stack.copy());
                return generatedLoot;
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    private record WeightedEntry(int weight, ItemStack stack) {}
}
