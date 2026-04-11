package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.network.ClientModReportPacket;
import com.soul.soa_additions.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-side scanner: on login, gathers the loaded mod list plus <b>every</b> resource pack
 * currently available to the client (whether selected or not), then sends the results to the
 * server as a {@link ClientModReportPacket} for inspection by {@link AntiCheatHandler}.
 *
 * <p>Available-not-selected scanning matters: a cheater can disable their xray pack in the
 * selector before joining, but the pack file is still on disk and still enumerable here.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
public final class ClientModScanner {

    private ClientModScanner() {}

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        List<String> mods = ModList.get().getMods().stream()
                .map(m -> m.getModId() + "|" + m.getDisplayName() + "|" + m.getDescription())
                .toList();

        List<String> packs = collectAllPacks();

        ModNetworking.CHANNEL.sendToServer(new ClientModReportPacket(mods, packs));
    }

    private static List<String> collectAllPacks() {
        PackRepository repo = Minecraft.getInstance().getResourcePackRepository();
        Set<String> seen = new HashSet<>();
        List<String> out = new ArrayList<>();
        appendPacks(out, seen, repo.getAvailablePacks());
        appendPacks(out, seen, repo.getSelectedPacks());
        return out;
    }

    private static void appendPacks(List<String> out, Set<String> seen, Collection<Pack> packs) {
        for (Pack pack : packs) {
            String id = pack.getId();
            if (!seen.add(id)) continue;
            out.add(id + "|" + pack.getTitle().getString() + "|" + pack.getDescription().getString());
        }
    }
}
