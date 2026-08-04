package com.soul.soa_additions.itemstages;

import com.google.common.collect.Multimap;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.mixin.itemstages.RestrictionManagerAccessor;
import net.darkhax.itemstages.Restriction;
import net.darkhax.itemstages.RestrictionManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GreedyCraft's per-stage tooltip frame colours, reimplemented for 1.20.
 *
 * <p>GreedyCraft drove this through TieredTooltips 2.0.0, configured by
 * {@code scripts/gamestages/tooltip.zs}. That mod has no 1.20 port, so the
 * colour table is transcribed here verbatim and applied the same way the
 * original did: on {@code RenderTooltipEvent.Color}, setting the tooltip
 * background and the two border gradient stops from the stage the hovered item
 * is gated behind.</p>
 *
 * <h2>Parity notes</h2>
 * <ul>
 *   <li>GreedyCraft shipped {@code Tiered Tooltips.cfg} with
 *       {@code RespectLockedStages=false}, so the frame was coloured whether or
 *       not the player had unlocked the stage. That is reproduced here: the
 *       colour is a property of the item's tier, not of the player's progress.
 *       Paired with the "Unknown Item" rename and question-mark model (see
 *       {@link UnknownItemModels}), a locked item shows as an anonymous but
 *       tier-coloured mystery — which is exactly the information GreedyCraft
 *       chose to leak.</li>
 *   <li>{@code tooltip.zs} colours {@code fearless_man} twice. TieredTooltips
 *       stored these with a plain {@code Map.put}, so the second call won and
 *       the first was dead configuration. The table below is built in file order
 *       so that same overwrite happens, rather than silently picking one.</li>
 *   <li>TieredTooltips resolved the stage with ItemStages' 1.12-only
 *       {@code ItemStages.getStage(stack)}. 8.0.3 has no equivalent — its only
 *       public query is player-aware and deliberately skips stages the player
 *       already has, which is the opposite of what a tier colour needs. The
 *       restriction table is therefore read directly (see
 *       {@link RestrictionManagerAccessor}).</li>
 * </ul>
 *
 * <h2>Interop with LegendaryTooltips</h2>
 * <p>LegendaryTooltips also listens to this event, at default priority, and
 * seeds its own frame definition from the event's <em>current</em> colours —
 * items with no custom frame pass through whatever it was handed. Running at
 * {@link EventPriority#HIGH} therefore puts the stage colour in place in time to
 * be adopted, while still letting LegendaryTooltips win on the items it styles
 * explicitly (the pack configures {@code level0_entries = ["!epic", "!rare"]}).
 * Lower priority would invert that and clobber a mod feature the pack
 * deliberately enables.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StageTooltipColors {

    /** Background, border gradient start, border gradient end - all ARGB. */
    private record Frame(int background, int borderStart, int borderEnd) {}

    /**
     * Transcribed from {@code GreedyCraft/scripts/gamestages/tooltip.zs}, in file
     * order. Every value there is 8-digit ARGB, which TieredTooltips used as-is.
     */
    private static final Map<String, Frame> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put("hardmode",             new Frame(0xb5000000, 0xf0f27121, 0xf0e94057));
        COLORS.put("novice_engineer",      new Frame(0xb5000000, 0xf0ff9966, 0xf0ff5e62));
        COLORS.put("ender_charm",          new Frame(0xb5000000, 0xf0ffafbd, 0xf0ffc3a0));
        COLORS.put("getting_started",      new Frame(0xb5000000, 0xf0fdfc47, 0xf024fe41));
        COLORS.put("skilled_engineer",     new Frame(0xb5000000, 0xf0dce35b, 0xf045b649));
        COLORS.put("master_engineer",      new Frame(0xb5000000, 0xf07f00ff, 0xf0e100ff));
        COLORS.put("novice_wizard",        new Frame(0xb5000000, 0xf0ff9966, 0xf0ff5e62));
        COLORS.put("skilled_wizard",       new Frame(0xb5000000, 0xf0fc4a1a, 0xf0f7b733));
        COLORS.put("master_wizard",        new Frame(0xb5000000, 0xf07f00ff, 0xf0e100ff));
        COLORS.put("wielder_of_infinity",  new Frame(0xb5000000, 0xf0009fff, 0xf0ec2f4b));
        COLORS.put("abyssal_conquerer",    new Frame(0xb5000000, 0xf000f260, 0xf00575e6));
        COLORS.put("wither_slayer",        new Frame(0xb5000000, 0xf0f857a6, 0xf0ff5858));
        // Overwritten below, exactly as it was in GreedyCraft. Kept for the record.
        COLORS.put("fearless_man",         new Frame(0xb5000000, 0xf0ed4264, 0xf0ffedbc));
        COLORS.put("nether",               new Frame(0xb5000000, 0xf0ff4e50, 0xf0f9d423));
        COLORS.put("disabled",             new Frame(0xb5660909, 0xf0f00000, 0xf0dc281e));
        COLORS.put("wyvern",               new Frame(0xb5000000, 0xf0ff00cc, 0xf0333399));
        COLORS.put("awakened",             new Frame(0xb5000000, 0xf0f46b45, 0xf0eea849));
        COLORS.put("chaotic",              new Frame(0xb5000000, 0xf0232526, 0xf0414345));
        COLORS.put("chaotic_dominator",    new Frame(0xb5000000, 0xf0004e92, 0xf0000428));
        COLORS.put("fearless_man",         new Frame(0xb5000000, 0xf0ff5f6d, 0xf0ffc371));
        COLORS.put("fusion_matrix",        new Frame(0xb5000000, 0xf042275a, 0xf0734b6d));
        COLORS.put("graduated",            new Frame(0xb5000000, 0xf0f2994a, 0xf0f2c94c));
        COLORS.put("expert",               new Frame(0xb5000000, 0xf0f953c6, 0xf0b91d73));
        COLORS.put("descendant_of_the_sun",new Frame(0xb5000000, 0xf0f12711, 0xf0f5af19));
        COLORS.put("energy_matter_core",   new Frame(0xb5000000, 0xf0396afc, 0xf02948ff));

        final Frame challenger = new Frame(0xb5000000, 0xf0ff8008, 0xf0ffc837);
        COLORS.put("challenger_a",   challenger);
        COLORS.put("challenger_b",   challenger);
        COLORS.put("challenger_c",   challenger);
        COLORS.put("challenger_d",   challenger);
        COLORS.put("challenger_e",   challenger);
        COLORS.put("challenger_f",   challenger);
        COLORS.put("challenger_g",   challenger);
        COLORS.put("challenger_all", challenger);
    }

    /** Sentinel for "checked, belongs to no coloured stage" - IdentityHashMap has no negative cache otherwise. */
    private static final Frame NONE = new Frame(0, 0, 0);

    private static final Map<Item, Frame> FRAME_CACHE = new IdentityHashMap<>();

    private StageTooltipColors() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        final Frame frame = frameFor(event.getItemStack());
        if (frame == null) {
            return;
        }
        event.setBackground(frame.background());
        event.setBorderStart(frame.borderStart());
        event.setBorderEnd(frame.borderEnd());
    }

    /** The tier frame for this stack, or null when it is not gated behind a coloured stage. */
    private static Frame frameFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        final Frame cached = FRAME_CACHE.get(stack.getItem());
        if (cached != null) {
            return cached == NONE ? null : cached;
        }
        final Frame resolved = resolve(stack);
        FRAME_CACHE.put(stack.getItem(), resolved == null ? NONE : resolved);
        return resolved;
    }

    /**
     * Walks the coloured stages in declaration order and returns the first whose
     * restrictions match this stack.
     *
     * <p>Driving the loop from {@link #COLORS} rather than from the whole
     * restriction table has two benefits: the ~1,500 restrictions filed under
     * uncoloured stages are never tested, and an item gated behind more than one
     * coloured stage resolves the same way every time instead of following
     * Guava's hash order.</p>
     */
    private static Frame resolve(ItemStack stack) {
        final Multimap<String, Restriction> restrictions;
        try {
            restrictions = ((RestrictionManagerAccessor) RestrictionManager.INSTANCE).soa$restrictions();
        } catch (RuntimeException | LinkageError e) {
            // Accessor mixin did not apply, or ItemStages changed shape. Tooltips
            // are cosmetic - fall back to vanilla colours rather than spamming a
            // per-frame failure.
            return null;
        }
        for (Map.Entry<String, Frame> colored : COLORS.entrySet()) {
            for (Restriction restriction : restrictions.get(colored.getKey())) {
                if (restriction.isRestricted(stack)) {
                    return colored.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Restrictions are rebuilt on datapack sync, so which stage an item belongs to
     * can change under us. Stage grants deliberately do <em>not</em> invalidate:
     * unlocking a tier does not move an item out of it.
     */
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        FRAME_CACHE.clear();
    }
}
