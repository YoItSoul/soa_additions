package com.soul.soa_additions.smithery;

import com.soul.smithery.api.modifier.ModifierEffect;
import com.soul.smithery.item.tool.SmitheryToolData;
import com.soul.smithery.item.tool.SmitheryToolItem;
import com.soul.smithery.item.tool.ToolComposition;
import com.soul.smithery.item.tool.ToolCompositions;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool leveling — port of TinkerToolLeveling, which GC's balance assumed.
 *
 * <p>Smithery tools gain XP: +1 per block broken, +5 per kill. Levels follow GC's
 * config (base 250 XP, doubling per level, no cap). Each level applies/upgrades the
 * hidden {@code well_used} effect whose {@code bonus_slots} param natively grants
 * bonus modifier slots (one net slot per level — the +1 offsets the effect itself
 * occupying a modifier entry).</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class ToolLevelingEvents {

    private static final String XP_TAG = "SoaToolXp";
    private static final int BASE_XP = 250;

    private static final String[] LEVEL_UP_FLAVOR = {
            "Your tool grows more experienced!",
            "Your tool feels more capable!",
            "Your tool has learned from its labors!",
            "Your tool is honed by hard work!"
    };

    private ToolLevelingEvents() {}

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;
        addXp(player.getMainHandItem(), player, 1);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        addXp(player.getMainHandItem(), player, 5);
    }

    private static void addXp(ItemStack tool, Player player, int amount) {
        if (!(tool.getItem() instanceof SmitheryToolItem)) return;
        ToolComposition comp = SmitheryToolData.getComposition(tool);
        if (comp == null || !comp.isValid()) return;

        var tag = tool.getOrCreateTag();
        long xp = tag.getLong(XP_TAG) + amount;
        tag.putLong(XP_TAG, xp);

        int level = currentLevel(tool);
        if (xp < xpForLevel(level + 1)) return;

        applyLevel(tool, comp, level + 1);
        player.displayClientMessage(Component.literal(
                        LEVEL_UP_FLAVOR[player.getRandom().nextInt(LEVEL_UP_FLAVOR.length)]
                                + " (Level " + (level + 1) + ")")
                .withStyle(ChatFormatting.AQUA), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    /** Cumulative XP required to reach {@code level}: 250, 750, 1750, ... (doubling steps). */
    private static long xpForLevel(int level) {
        long total = 0;
        long step = BASE_XP;
        for (int i = 0; i < level; i++) {
            total += step;
            step *= 2;
        }
        return total;
    }

    private static int currentLevel(ItemStack tool) {
        for (ModifierEffect e : SmitheryToolData.getAppliedModifiers(tool)) {
            if (SoaSmitheryModifiers.WELL_USED.equals(e.modifierId())) {
                return e.paramInt("level", 0);
            }
        }
        return 0;
    }

    private static void applyLevel(ItemStack tool, ToolComposition comp, int newLevel) {
        List<ModifierEffect> applied = new ArrayList<>(SmitheryToolData.getAppliedModifiers(tool));
        applied.removeIf(e -> SoaSmitheryModifiers.WELL_USED.equals(e.modifierId()));
        applied.add(ModifierEffect.of(SoaSmitheryModifiers.WELL_USED,
                Map.of("level", newLevel, "bonus_slots", newLevel + 1)));
        SmitheryToolData.setAppliedModifiers(tool, applied);
        ToolCompositions.apply(tool, comp);
    }
}
