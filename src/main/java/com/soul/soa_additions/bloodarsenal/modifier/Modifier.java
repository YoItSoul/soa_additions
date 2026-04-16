package com.soul.soa_additions.bloodarsenal.modifier;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Abstract base class for stasis tool modifiers.
 * Each modifier has a key, type (HEAD/CORE/HANDLE/ABILITY), max level,
 * and counter thresholds that determine when it levels up.
 *
 * <p>Ported from: arcaratus.bloodarsenal.modifier.Modifier</p>
 */
public abstract class Modifier {

    /** Default counter thresholds for leveling: each level requires more usage */
    public static final double[] DEFAULT_COUNTERS = {0, 100, 250, 500, 1000, 2000, 4000};

    private final String key;
    private final EnumModifierType type;
    private final int maxLevel;
    private final double[] counterThresholds;

    protected Modifier(String key, EnumModifierType type, int maxLevel) {
        this(key, type, maxLevel, DEFAULT_COUNTERS);
    }

    protected Modifier(String key, EnumModifierType type, int maxLevel, double[] counterThresholds) {
        this.key = key;
        this.type = type;
        this.maxLevel = maxLevel;
        this.counterThresholds = counterThresholds;
    }

    // ── Event hooks — overridden by concrete modifiers ──────────────────

    /** Called every tick while the stasis tool is in the player's inventory */
    public void onUpdate(Level level, Player player, ItemStack stack, int slotIndex, int modLevel) {}

    /** Called when the stasis tool hits an entity */
    public void hitEntity(Level level, Player player, ItemStack stack, LivingEntity target, int modLevel) {}

    /** Called when the stasis tool breaks a block */
    public void onBlockDestroyed(Level level, Player player, ItemStack stack, BlockPos pos, BlockState state, int modLevel) {}

    /** Called when the stasis tool is right-clicked */
    public void onRightClick(Level level, Player player, ItemStack stack, int modLevel) {}

    /** Called when right-click is released (for charged abilities like AOD) */
    public void onRelease(Level level, Player player, ItemStack stack, int charge, int modLevel) {}

    /** Returns attribute modifiers to apply at the given level */
    public void getAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers, int modLevel) {}

    /** Called when writing special NBT (e.g., enchantments for Looting/Fortune) */
    public void writeSpecialNBT(CompoundTag tag, int modLevel) {}

    /** Called when reading special NBT */
    public void readSpecialNBT(CompoundTag tag) {}

    // ── Getters ─────────────────────────────────────────────────────────

    public String getKey() { return key; }
    public EnumModifierType getType() { return type; }
    public int getMaxLevel() { return maxLevel; }

    public double getCounterThreshold(int level) {
        if (level < 0 || level >= counterThresholds.length) return Double.MAX_VALUE;
        return counterThresholds[level];
    }

    public String getTranslationKey() {
        return "modifier.bloodarsenal." + key;
    }
}
