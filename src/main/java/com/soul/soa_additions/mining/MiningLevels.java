package com.soul.soa_additions.mining;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.ModList;

/**
 * Names and renders the pack's mining-tier ladder, the way GreedyCraft's Tool Progression
 * did (config/tool_progression/mining_level_names.cfg): every level gets a rank name, a
 * roman numeral and a colour, so "level 5" reads as "Expert (V)" instead of a bare number.
 *
 * <p>Vanilla only ships six tiers and Jade's own harvest-tool line stops at netherite, so
 * every block gated behind {@link com.soul.soa_additions.registry.SoaTiers}'s eleven extra
 * tiers shows nothing at all in-game. Both consumers here — the tool tooltip
 * ({@link MiningLevelTooltip}) and the Jade provider
 * (com.soul.soa_additions.compat.jade.MiningLevelProvider) — resolve tiers through
 * {@link TierSortingRegistry} rather than switching over {@link Tiers}, so they stay correct
 * for any tier the pack adds later.</p>
 *
 * <p>Two tiers can share a numeric level (SoaTiers puts obsidian at level 3 alongside vanilla
 * diamond, sorted above it), so the level name alone can't identify a tier. Where the required
 * tier is one of ours, {@link #describe} appends its material name — matching the
 * "Level - Original Name - Altered Name" triple in the pack's Harvest Levels guide entry.</p>
 */
public final class MiningLevels {

    /**
     * Returned by {@link #harvestLevelOf} when a stack carries no mining level. Aliased to
     * {@link com.soul.soa_additions.smithery.tool.SmitheryTools#NO_LEVEL} rather than restated,
     * so the two sentinels can never drift apart — results flow between the two classes freely.
     */
    public static final int NONE = com.soul.soa_additions.smithery.tool.SmitheryTools.NO_LEVEL;

    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    /** GC paints the infinity numerals one colour per letter; everything else is monochrome. */
    private static final ChatFormatting[] INFINITY_COLOURS = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.GREEN,
            ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE
    };

    private static Boolean smitheryLoaded;

    private MiningLevels() {}

    // ------------------------------------------------------------------ rendering

    /**
     * Renders a tier as {@code Expert (V) [Duranite]} — rank name, roman numeral, and (for the
     * pack's own tiers) the material the level is named after.
     */
    public static MutableComponent describe(Tier tier) {
        int level = tier.getLevel();
        MutableComponent out = Component.empty()
                .append(levelName(level))
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(numeral(level))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY));

        ResourceLocation id = TierSortingRegistry.getName(tier);
        if (id != null && showMaterial(tier, id, level)) {
            out.append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.translatable("soa_additions.mining_tier." + id.getPath())
                       .withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        }
        return out;
    }

    /**
     * Whether to name the tier's material alongside the rank.
     *
     * <p>Two cases earn it. One: the rank alone is ambiguous because more than one registered tier
     * sits at that level — obsidian shares level 3 with vanilla diamond but is sorted above it, so
     * "Apprentice (III)" alone can't explain why a diamond pickaxe gets a ✖. Two: levels 3-7 are
     * named after a material in GC's table, so the bracket carries real information. From Heavenly
     * up the table reads "Original Name: None" and our tier ids just repeat the rank, which would
     * render "Heavenly (VIII) [Heavenly]".</p>
     */
    private static boolean showMaterial(Tier tier, ResourceLocation id, int level) {
        if (sharesLevel(tier, level)) return true;
        return level <= 7 && SoaAdditions.MODID.equals(id.getNamespace());
    }

    private static boolean sharesLevel(Tier tier, int level) {
        for (Tier other : TierSortingRegistry.getSortedTiers()) {
            if (other != tier && other.getLevel() == level) return true;
        }
        return false;
    }

    /** The rank name alone, e.g. a bold gold "Legendary". */
    public static MutableComponent levelName(int level) {
        MutableComponent name = named(level)
                ? Component.translatable("soa_additions.mining_level." + level)
                : Component.translatable("soa_additions.mining_level.unknown", level);
        name.withStyle(colourFor(level));
        if (level >= 10) name.withStyle(ChatFormatting.BOLD);
        return name;
    }

    private static MutableComponent numeral(int level) {
        String roman = toRoman(level);
        if (level != 127) return Component.literal(roman).withStyle(colourFor(level));

        MutableComponent out = Component.empty();
        for (int i = 0; i < roman.length(); i++) {
            out.append(Component.literal(String.valueOf(roman.charAt(i)))
                    .withStyle(INFINITY_COLOURS[i % INFINITY_COLOURS.length]));
        }
        return out;
    }

    /** True for the levels GreedyCraft gave a rank name; anything else falls back to "Level N". */
    private static boolean named(int level) {
        return (level >= 0 && level <= 13) || level == 127;
    }

    /** GreedyCraft's colour for a mining level, for callers that render the rank themselves. */
    public static ChatFormatting levelColour(int level) {
        return colourFor(level);
    }

    private static ChatFormatting colourFor(int level) {
        return switch (level) {
            case 0 -> ChatFormatting.DARK_GRAY;
            case 1 -> ChatFormatting.DARK_GREEN;
            case 2 -> ChatFormatting.DARK_AQUA;
            case 3 -> ChatFormatting.LIGHT_PURPLE;
            case 4 -> ChatFormatting.BLUE;
            case 5 -> ChatFormatting.GREEN;
            case 6 -> ChatFormatting.RED;
            case 7 -> ChatFormatting.DARK_PURPLE;
            case 8 -> ChatFormatting.DARK_RED;
            case 9 -> ChatFormatting.GOLD;
            case 10 -> ChatFormatting.RED;
            case 11 -> ChatFormatting.DARK_GREEN;
            case 12 -> ChatFormatting.DARK_PURPLE;
            case 13, 127 -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }

    private static String toRoman(int value) {
        if (value <= 0 || value > 3999) return String.valueOf(value);
        StringBuilder out = new StringBuilder();
        int remaining = value;
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (remaining >= ROMAN_VALUES[i]) {
                out.append(ROMAN_SYMBOLS[i]);
                remaining -= ROMAN_VALUES[i];
            }
        }
        return out.toString();
    }

    // ------------------------------------------------------------------ lookups

    /**
     * The weakest registered tier that can harvest {@code state}, or null when the block isn't
     * tier-gated at all.
     *
     * <p>Level 0 counts as ungated: wood and gold are the floor of the ladder, so a level-0 result
     * only means "any tool of the right type", which Jade's own harvest-tool line already renders
     * in full. Reporting it would put a "Requires: Fragile (0)" on every stone block.</p>
     *
     * <p>Blocks whose {@code requiresCorrectToolForDrops} is false still count when a tier tag
     * names them: {@link com.soul.soa_additions.registry.HardnessOverrides} makes bedrock and
     * barrier breakable without touching that flag, and both are tagged (mythical / supreme).</p>
     */
    public static Tier requiredTier(BlockState state) {
        for (Tier tier : TierSortingRegistry.getSortedTiers()) {
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state)) {
                return tier.getLevel() <= 0 ? null : tier;
            }
        }
        return null;
    }

    /** The tier a held tool mines at, or null if the stack has no mining tier. */
    public static Tier toolTier(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getItem() instanceof TieredItem tiered) return tiered.getTier();
        int level = harvestLevelOf(stack);
        return level == NONE ? null : tierForLevel(level);
    }

    /**
     * Raw harvest level of a stack, for tools that carry one without being a {@link TieredItem} —
     * currently Smithery's composed tools, whose level comes from the head material.
     */
    public static int harvestLevelOf(ItemStack stack) {
        if (stack.getItem() instanceof TieredItem tiered) return tiered.getTier().getLevel();
        if (smitheryLoaded()) return SmitheryToolLevels.harvestLevel(stack);
        return NONE;
    }

    /**
     * Maps a bare harvest level onto the strongest tier it satisfies. Ties resolve to the later
     * tier in sort order, matching how Smithery's own {@code tierFor} picks a tier for a composed
     * tool — so what a tooltip reports and what the tool can actually break stay in agreement.
     */
    public static Tier tierForLevel(int level) {
        Tier best = Tiers.WOOD;
        int bestLevel = Integer.MIN_VALUE;
        for (Tier tier : TierSortingRegistry.getSortedTiers()) {
            int tierLevel = tier.getLevel();
            if (tierLevel <= level && tierLevel >= bestLevel) {
                best = tier;
                bestLevel = tierLevel;
            }
        }
        return best;
    }

    /**
     * True for items whose mining level can actually decide something.
     *
     * <p>Written as an exclusion, not a pickaxe/shovel/axe whitelist. The tools that most need
     * this line are the ones a whitelist would miss: Draconic and Avaritia mining tools, paxels,
     * and Smithery's mining_hammer / excavator / mattock / lumberaxe are all tier-gated diggers
     * whose classes and tool-type ids aren't "pickaxe". Hardcoding a list of accepted types is the
     * exact failure that leaves Jade's own harvest line blank above netherite.</p>
     *
     * <p>Swords and hoes are dropped instead. A sword's tier only feeds damage, and no
     * hoe-mineable block — hay, leaves, sponge, moss, nether wart block — is tier-gated in this
     * pack, so a hoe's level can never change an outcome. Shovels stay: GC gates ores behind them
     * (aquamarine at 3, cytosinite at 5), so shovel tier is load-bearing here.</p>
     */
    public static boolean showsMiningLevel(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof TieredItem) return !(item instanceof SwordItem) && !(item instanceof HoeItem);
        return smitheryLoaded() && SmitheryToolLevels.showsMiningLevel(stack);
    }

    /** Resolved lazily: {@link ModList} isn't populated while classes are still loading. */
    private static boolean smitheryLoaded() {
        Boolean cached = smitheryLoaded;
        if (cached == null) {
            cached = ModList.get().isLoaded("smithery");
            smitheryLoaded = cached;
        }
        return cached;
    }
}
