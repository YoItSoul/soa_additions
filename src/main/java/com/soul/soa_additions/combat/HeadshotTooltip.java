package com.soul.soa_additions.combat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.HeadshotConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Puts the headshot protection percentage and GC's qualitative rating on any
 * item that can be worn on the head. Both lines read the same
 * {@link HeadgearProtection} call the damage handler uses, so the tooltip can
 * never drift from the actual mitigation.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeadshotTooltip {

    private static final String[] PROTECTION_LEVELS = {
            "no", "weak", "miserable", "awful", "bad",
            "normal", "good", "excellent", "marvelous", "exceptional", "perfect"
    };

    private HeadshotTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (HeadshotConfig.PLAYERS_HAVE_NO_HEADS.get()) return;

        ItemStack stack = event.getItemStack();
        if (HeadgearProtection.getProtection(stack) == 1.0F) return;

        int percent = HeadgearProtection.getProtectionPercent(stack);
        int tier = Math.min(percent / 10, PROTECTION_LEVELS.length - 1);

        event.getToolTip().add(Component.translatable("soa_additions.headshot_protection", percent)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        event.getToolTip().add(Component.translatable("soa_additions.protection_level." + PROTECTION_LEVELS[tier])
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
