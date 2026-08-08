package com.soul.soa_additions.anticheat;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.anticheat.client.PackContentScanner;
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
 * Client-side scanner: on login, gathers the loaded mod list, every resource pack the launcher can
 * see, and a content verdict on each of those packs.
 *
 * <p>Available-not-selected scanning matters: a cheater can disable their xray pack in the selector
 * before joining, but the file is still on disk and still enumerable here.</p>
 *
 * <p>Names alone were never enough. Every string this used to send described what something calls
 * itself, so renaming a folder defeated the entire scan; the content verdicts from
 * {@link PackContentScanner} and the class probes below are the parts that survive a rename.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
public final class ClientModScanner {

    /**
     * Classes that give away a cheat even when its jar has been renamed and its mod id changed.
     * Renaming {@code baritone.jar} changes the file name and can change the mod id; it does not
     * change the package a class lives in, so the mod list can come back clean while this does not.
     * Relocation (repackaging under a new package name) defeats this — that is the ceiling of
     * static detection, and past it lies behavioural analysis, which is a different project.
     */
    private static final String[] TELLTALE_CLASSES = {
            // Baritone — cabaletta/baritone, src/api/java. The API sourceset is what addons and
            // repackaged builds keep, so it survives more repackaging than the impl classes.
            "baritone.api.BaritoneAPI",
            "baritone.api.IBaritoneProvider",
            // AltoClef — adris/altoclef, a Baritone-driven autoplay bot that ships its own copy.
            "adris.altoclef.AltoClef",

            // Advanced XRay (AdvancedXRay/XRay-Mod) — NeoForge + Fabric, ~6.4M downloads, by far
            // the most likely xray a modpack player actually installs. Two anchors: the entrypoint
            // and the scanner, so dropping one class does not blind the probe.
            "pro.mikey.xray.XRay",
            "pro.mikey.xray.core.ScanController",
            // Xray by ate47 — the other widely-used Forge/Fabric xray implementation.
            "fr.atesab.xray.XrayMain",

            // Freecam — hashalite/Freecam. Sees through walls without touching block data, so
            // neither the pack content scan nor ore obfuscation would ever catch it.
            "net.xolt.freecam.Freecam",

            // Standalone Fabric clients. These replace the client outright and cannot load a Forge
            // modpack on their own — they are listed because Sinytra Connector can run Fabric mods
            // on Forge 1.20.1, which makes them reachable. Costs one classloader lookup each.
            "net.wurstclient.WurstClient",
            "meteordevelopment.meteorclient.MeteorClient",
            "net.ccbluex.liquidbounce.LiquidInstruction",
    };

    private ClientModScanner() {}

    /** Mod list is immutable for the JVM lifetime — build the report rows once
     *  instead of re-streaming ~300 mod descriptors on every reconnect. */
    private static List<String> cachedMods;
    /** Class probes are likewise fixed for the JVM lifetime. */
    private static List<String> cachedClassHits;

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (cachedMods == null) {
            cachedMods = ModList.get().getMods().stream()
                    .map(m -> m.getModId() + "|" + m.getDisplayName() + "|" + m.getDescription())
                    .toList();
        }
        if (cachedClassHits == null) {
            cachedClassHits = probeClasses();
        }

        List<Pack> packs = collectPacks();
        List<String> packNames = new ArrayList<>();
        for (Pack pack : packs) {
            packNames.add(pack.getId() + "|" + pack.getTitle().getString() + "|" + pack.getDescription().getString());
        }

        // Content inspection opens and decodes files, so it must not run on the client thread —
        // a login stutter proportional to the player's pack collection would be its own bug.
        // The server treats the report as fire-and-forget, so arriving a moment later is fine.
        List<String> mods = cachedMods;
        List<String> classHits = cachedClassHits;
        Thread worker = new Thread(() -> {
            List<String> findings = new ArrayList<>(classHits);
            for (Pack pack : packs) {
                findings.addAll(PackContentScanner.scan(pack));
            }
            ModNetworking.CHANNEL.sendToServer(new ClientModReportPacket(mods, packNames, findings));
        }, "soa-anticheat-scan");
        worker.setDaemon(true);
        worker.start();
    }

    /** Reports any telltale class actually present on the classpath. */
    private static List<String> probeClasses() {
        List<String> hits = new ArrayList<>();
        ClassLoader loader = ClientModScanner.class.getClassLoader();
        for (String className : TELLTALE_CLASSES) {
            try {
                // initialize=false: presence is the signal; running a cheat's static init is not.
                Class.forName(className, false, loader);
                hits.add("class|" + className + "|present on classpath");
            } catch (Throwable ignored) {
                // Absent, which is the expected case.
            }
        }
        return hits;
    }

    private static List<Pack> collectPacks() {
        PackRepository repo = Minecraft.getInstance().getResourcePackRepository();
        Set<String> seen = new HashSet<>();
        List<Pack> out = new ArrayList<>();
        appendPacks(out, seen, repo.getAvailablePacks());
        appendPacks(out, seen, repo.getSelectedPacks());
        return out;
    }

    private static void appendPacks(List<Pack> out, Set<String> seen, Collection<Pack> packs) {
        for (Pack pack : packs) {
            if (seen.add(pack.getId())) out.add(pack);
        }
    }
}
