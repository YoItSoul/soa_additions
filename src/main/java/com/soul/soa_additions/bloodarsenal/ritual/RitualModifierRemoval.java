package com.soul.soa_additions.bloodarsenal.ritual;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.modifier.Modifier;
import com.soul.soa_additions.bloodarsenal.modifier.ModifierTracker;
import com.soul.soa_additions.bloodarsenal.modifier.StasisModifiable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.ritual.*;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Modifier Removal — removes ALL modifiers from items in the area above the altar.
 * Creates ModifierTome items per removed modifier (with key, level, readyToUpgrade).
 * One-shot ritual.
 */
@RitualRegister("blood_arsenal_modifier_removal")
public class RitualModifierRemoval extends Ritual {

    public RitualModifierRemoval() {
        super("blood_arsenal_modifier_removal", 0,
                BAConfig.MODIFIER_REMOVAL_COST.get(),
                "ritual.soa_additions.modifier_removal");
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos pos = mrs.getMasterBlockPos();

        if (level.isClientSide()) return;

        // Search for item entities in 1 block above
        AABB area = new AABB(pos.above()).inflate(0.5);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        boolean didWork = false;

        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            StasisModifiable modifiable = StasisModifiable.fromStack(stack);
            if (modifiable == null || modifiable.getModifiers().isEmpty()) continue;

            // Remove all modifiers and drop tomes
            Map<String, Pair<Modifier, ModifierTracker>> modifiers = modifiable.getModifiers();
            for (var entry : modifiers.entrySet()) {
                Modifier mod = entry.getValue().getLeft();
                ModifierTracker tracker = entry.getValue().getRight();

                // Create a modifier tome with the removed modifier data
                ItemStack tome = new ItemStack(
                        com.soul.soa_additions.bloodarsenal.BAItems.MODIFIER_TOME.get());
                var tag = tome.getOrCreateTag();
                tag.putString("key", mod.getKey());
                tag.putInt("level", tracker.getLevel());
                tag.putBoolean("readyToUpgrade", tracker.isReadyToUpgrade());

                // Drop the tome
                ItemEntity tomeEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, tome);
                level.addFreshEntity(tomeEntity);
            }

            // Clear all modifiers from the item
            modifiable.clearModifiers();
            modifiable.saveToStack(stack);
            itemEntity.setItem(stack);
            didWork = true;
        }

        if (didWork && level instanceof ServerLevel serverLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(Vec3.atCenterOf(pos.above()));
                bolt.setVisualOnly(true);
                serverLevel.addFreshEntity(bolt);
            }
        }

        // One-shot: deactivate after performing
        mrs.setActive(false);
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> consumer) {
        // Exact glyph pattern from original RitualModifierRemove.java
        addCornerRunes(consumer, 1, 0, EnumRuneType.DUSK);
        addCornerRunes(consumer, 2, 0, EnumRuneType.FIRE);
        addOffsetRunes(consumer, 1, 2, 0, EnumRuneType.FIRE);
        addCornerRunes(consumer, 1, 1, EnumRuneType.WATER);
        addParallelRunes(consumer, 4, 0, EnumRuneType.EARTH);
        addCornerRunes(consumer, 1, 3, EnumRuneType.WATER);
        addParallelRunes(consumer, 1, 4, EnumRuneType.AIR);

        for (int i = 0; i < 4; i++)
            addCornerRunes(consumer, 3, i, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualModifierRemoval();
    }
}
