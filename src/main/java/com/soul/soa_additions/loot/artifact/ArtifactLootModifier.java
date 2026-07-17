package com.soul.soa_additions.loot.artifact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * {@code tconevo:artifact} — with probability {@code chance}, injects one weighted-random
 * sealed artifact into the matched loot table. Mirrors 1.12 TConEvo's dungeon-loot
 * artifact injection; per-table gating comes from the JSON's forge:loot_table_id condition.
 */
public class ArtifactLootModifier extends LootModifier {

    public static final Codec<ArtifactLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst)
                    .and(Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(m -> m.chance))
                    .apply(inst, ArtifactLootModifier::new));

    private final float chance;

    public ArtifactLootModifier(LootItemCondition[] conditions, float chance) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() >= chance) return generatedLoot;
        ArtifactDefs.Def def = ArtifactDefs.pick(context.getRandom());
        if (def == null) return generatedLoot;
        ItemStack artifact = ArtifactBuilder.build(def);
        if (!artifact.isEmpty()) generatedLoot.add(artifact);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
