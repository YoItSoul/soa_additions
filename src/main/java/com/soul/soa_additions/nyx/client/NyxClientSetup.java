package com.soul.soa_additions.nyx.client;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client wiring for lunar events: the Meteor Finder compass pointer and the
 * event sky/fog tint (1.12 ClientEvents.getSkyColor approximation — 1.20 has no
 * clean sky-color hook without mixins, so the fog is tinted instead).
 */
public final class NyxClientSetup {

    private NyxClientSetup() {}

    @Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(
                    NyxItems.METEOR_FINDER.get(),
                    ResourceLocation.fromNamespaceAndPath("nyx", "angle"),
                    (stack, level, entity, seed) -> {
                        Entity holder = entity != null ? entity : stack.getEntityRepresentation();
                        if (holder == null || level == null) return 0.0f;
                        BlockPos target = NyxClientState.nearestSite(holder.blockPosition());
                        if (target == null) {
                            // no tracked meteor — spin like a broken compass
                            return (level.getGameTime() % 32) / 32.0f;
                        }
                        double angleTo = Math.atan2(target.getZ() + 0.5 - holder.getZ(),
                                target.getX() + 0.5 - holder.getX());
                        double yaw = Math.toRadians(holder.getYRot());
                        double relative = (angleTo - Math.PI / 2.0 - yaw) / (Math.PI * 2.0);
                        return Mth.positiveModulo((float) relative, 1.0f);
                    }));
        }
    }

    @Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onFogColor(ViewportEvent.ComputeFogColor event) {
            if (!NyxConfig.MOON_EVENT_TINT.get()) return;
            int color = switch (NyxClientState.activeEvent()) {
                case "blood_moon" -> 4328707;      // 1.12 sky colors, verbatim
                case "harvest_moon" -> 14598751;
                case "star_shower" -> 4145088;
                default -> -1;
            };
            if (color < 0) return;
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            // blend halfway so nights stay readable
            event.setRed((event.getRed() + r) / 2.0f);
            event.setGreen((event.getGreen() + g) / 2.0f);
            event.setBlue((event.getBlue() + b) / 2.0f);
        }
    }
}
