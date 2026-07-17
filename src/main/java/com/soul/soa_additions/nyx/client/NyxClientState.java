package com.soul.soa_additions.nyx.client;

import com.soul.soa_additions.nyx.net.NyxSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Client-side lunar event state (1.12 PacketNyxWorld + ClientEvents port).
 *
 * <p>Moon texture: the nyx blood/harvest moon textures are full 128x64 phase
 * atlases, so swapping is done by re-registering the vanilla moon_phases
 * texture location in the TextureManager — no render hook needed. Restoring
 * registers a SimpleTexture of the vanilla location, which reloads the
 * original from the resource stack.</p>
 */
public final class NyxClientState {

    private static final ResourceLocation MOON_PHASES =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/moon_phases.png");
    private static final ResourceLocation BLOOD_MOON =
            ResourceLocation.fromNamespaceAndPath("nyx", "textures/moon/blood_moon.png");
    private static final ResourceLocation HARVEST_MOON =
            ResourceLocation.fromNamespaceAndPath("nyx", "textures/moon/harvest_moon.png");

    private static String activeEvent = "";
    private static List<BlockPos> landingSites = List.of();

    private NyxClientState() {}

    public static void accept(NyxSyncPacket pkt) {
        landingSites = pkt.landingSites();
        String next = pkt.eventName();
        if (next.equals(activeEvent)) return;
        activeEvent = next;
        applyMoonTexture();
    }

    public static String activeEvent() {
        return activeEvent;
    }

    public static List<BlockPos> landingSites() {
        return landingSites;
    }

    /** Nearest tracked landing site to the given position, or null. */
    public static BlockPos nearestSite(BlockPos from) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : landingSites) {
            double d = p.distSqr(from);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    private static void applyMoonTexture() {
        var tm = Minecraft.getInstance().getTextureManager();
        ResourceLocation source = switch (activeEvent) {
            case "blood_moon" -> BLOOD_MOON;
            case "harvest_moon" -> HARVEST_MOON;
            default -> MOON_PHASES;
        };
        tm.register(MOON_PHASES, new SimpleTexture(source));
    }
}
