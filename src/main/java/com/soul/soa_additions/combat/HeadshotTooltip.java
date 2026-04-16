package com.soul.soa_additions.combat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.config.HeadshotConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds a single "Headshot Protection: N%" line to every helmet tooltip. The
 * number is derived from the same {@link HeadshotConfig#profileFor} call the
 * damage handler uses, so the tooltip and the actual mitigation can never
 * drift out of sync — if a config value changes, tooltips update on the next
 * frame because profiles are recomputed on demand.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeadshotTooltip {

    private HeadshotTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof ArmorItem armor)) return;
        if (armor.getEquipmentSlot() != EquipmentSlot.HEAD) return;

        HeadshotConfig.Profile prof = HeadshotConfig.profileFor(armor);
        int pct = Math.round((1.0F - prof.damageTakenMult()) * 100F);

        event.getToolTip().add(
                Component.literal("Headshot Protection: ").withStyle(ChatFormatting.BLUE)
                        .append(Component.literal(pct + "%").withStyle(ChatFormatting.WHITE))
        );
    }
}
