package com.soul.soa_additions.mining;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts GreedyCraft's mining-level line on every digging tool — "Mining Level: Expert (V)".
 *
 * <p>GC got this from Tool Progression, which prefixed the item's own name with the rank; 1.20
 * has no equivalent, and with eleven tiers above netherite there is otherwise nothing in-game
 * that tells you where a pickaxe sits on the ladder. The block half of the same question is
 * answered by the Jade provider.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MiningLevelTooltip {

    private MiningLevelTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!ModConfigs.SHOW_MINING_LEVEL_TOOLTIP.get()) return;
        if (!MiningLevels.showsMiningLevel(event.getItemStack())) return;

        Tier tier = MiningLevels.toolTier(event.getItemStack());
        if (tier == null) return;

        event.getToolTip().add(Component.translatable("soa_additions.mining_level.tooltip",
                MiningLevels.describe(tier)).withStyle(ChatFormatting.GRAY));
    }
}
