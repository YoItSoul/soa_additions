package com.soul.soa_additions.bloodarsenal.modifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Container managing all modifiers attached to a stasis tool.
 * Stored in the item's NBT under the {@code "stasisModifiers"} tag.
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.StasisModifiable</p>
 */
public class StasisModifiable {

    private static final String TAG_ROOT = "stasisModifiers";
    private static final String TAG_LIST = "modifiers";
    private static final String TAG_KEY = "key";
    private static final String TAG_TRACKER_PREFIX = "bloodarsenal.tracker.";

    private final Map<String, Pair<Modifier, ModifierTracker>> modifiers = new LinkedHashMap<>();

    // ── Modifier management ─────────────────────────────────────────────

    public boolean canApplyModifier(Modifier modifier) {
        // Check if this modifier type has available slots
        int usedSlots = 0;
        for (var entry : modifiers.values()) {
            if (entry.getLeft().getType() == modifier.getType()) {
                usedSlots++;
            }
        }
        if (usedSlots >= modifier.getType().getMaxSlots()) return false;

        // Check incompatibility
        for (String existingKey : modifiers.keySet()) {
            if (ModifierRegistry.areIncompatible(existingKey, modifier.getKey())) {
                return false;
            }
        }

        // Already has this modifier
        return !modifiers.containsKey(modifier.getKey());
    }

    public boolean applyModifier(Modifier modifier) {
        if (!canApplyModifier(modifier)) return false;
        modifiers.put(modifier.getKey(), Pair.of(modifier, new ModifierTracker(modifier)));
        return true;
    }

    public boolean removeModifier(String key) {
        return modifiers.remove(key) != null;
    }

    public boolean upgradeModifier(String key) {
        Pair<Modifier, ModifierTracker> pair = modifiers.get(key);
        if (pair == null) return false;
        ModifierTracker tracker = pair.getRight();
        if (!tracker.isReadyToUpgrade()) return false;
        if (tracker.getLevel() >= pair.getLeft().getMaxLevel()) return false;
        tracker.onModifierUpgraded();
        return true;
    }

    public boolean hasModifier(String key) {
        return modifiers.containsKey(key);
    }

    public int getModifierLevel(String key) {
        Pair<Modifier, ModifierTracker> pair = modifiers.get(key);
        return pair != null ? pair.getRight().getLevel() : -1;
    }

    public ModifierTracker getTracker(String key) {
        Pair<Modifier, ModifierTracker> pair = modifiers.get(key);
        return pair != null ? pair.getRight() : null;
    }

    public Collection<Pair<Modifier, ModifierTracker>> getAllModifiers() {
        return Collections.unmodifiableCollection(modifiers.values());
    }

    public Map<String, Pair<Modifier, ModifierTracker>> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public void clearModifiers() {
        modifiers.clear();
    }

    // ── Event delegation ────────────────────────────────────────────────

    public void onUpdate(Level level, Player player, ItemStack stack, int slot) {
        for (var pair : modifiers.values()) {
            pair.getLeft().onUpdate(level, player, stack, slot, pair.getRight().getLevel());
        }
    }

    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target) {
        for (var pair : modifiers.values()) {
            pair.getLeft().hitEntity(level, player, stack, target, pair.getRight().getLevel());
        }
    }

    public void onBlockDestroyed(Level level, Player player, ItemStack stack, BlockPos pos, BlockState state) {
        for (var pair : modifiers.values()) {
            pair.getLeft().onBlockDestroyed(level, player, stack, pos, state, pair.getRight().getLevel());
        }
    }

    public void onRightClick(Level level, Player player, ItemStack stack) {
        for (var pair : modifiers.values()) {
            pair.getLeft().onRightClick(level, player, stack, pair.getRight().getLevel());
        }
    }

    public void onRelease(Level level, Player player, ItemStack stack, int charge) {
        for (var pair : modifiers.values()) {
            pair.getLeft().onRelease(level, player, stack, charge, pair.getRight().getLevel());
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        Multimap<Attribute, AttributeModifier> result = HashMultimap.create();
        for (var pair : modifiers.values()) {
            pair.getLeft().getAttributeModifiers(result, pair.getRight().getLevel());
        }
        return result;
    }

    // ── NBT ─────────────────────────────────────────────────────────────

    public static StasisModifiable fromStack(ItemStack stack) {
        StasisModifiable modifiable = new StasisModifiable();
        if (!stack.isEmpty() && stack.hasTag()) {
            CompoundTag root = stack.getTag().getCompound(TAG_ROOT);
            modifiable.deserializeNBT(root);
        }
        return modifiable;
    }

    public void saveToStack(ItemStack stack) {
        CompoundTag root = new CompoundTag();
        serializeNBT(root);
        stack.getOrCreateTag().put(TAG_ROOT, root);
    }

    private void serializeNBT(CompoundTag root) {
        ListTag list = new ListTag();
        for (var entry : modifiers.entrySet()) {
            CompoundTag modTag = new CompoundTag();
            modTag.putString(TAG_KEY, entry.getKey());
            modTag.put(TAG_TRACKER_PREFIX + entry.getKey(), entry.getValue().getRight().serializeNBT());
            entry.getValue().getLeft().writeSpecialNBT(modTag, entry.getValue().getRight().getLevel());
            list.add(modTag);
        }
        root.put(TAG_LIST, list);
    }

    private void deserializeNBT(CompoundTag root) {
        modifiers.clear();
        ListTag list = root.getList(TAG_LIST, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag modTag = list.getCompound(i);
            String key = modTag.getString(TAG_KEY);
            Modifier modifier = ModifierRegistry.get(key);
            if (modifier == null) continue;

            ModifierTracker tracker = new ModifierTracker(modifier);
            if (modTag.contains(TAG_TRACKER_PREFIX + key)) {
                tracker.deserializeNBT(modTag.getCompound(TAG_TRACKER_PREFIX + key));
            }
            modifier.readSpecialNBT(modTag);
            modifiers.put(key, Pair.of(modifier, tracker));
        }
    }
}
