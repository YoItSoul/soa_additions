package com.soul.soa_additions.deckmode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-click "Steam Deck Mode" toggle. Applies handheld-friendly video
 * settings, deselects the heavy entity-animation resource packs, and turns
 * Oculus shaders off — snapshotting the previous state so a second click
 * restores it exactly. This keeps a single instance playable on both a
 * desktop and a Deck without maintaining two launcher profiles.
 *
 * The snapshot lives in config/soa_deck_mode.json; its presence is what
 * defines "deck mode is active".
 */
public final class DeckMode {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Selected resource packs whose id contains one of these (case-insensitive)
     * are deselected in deck mode. The Fresh* family (Fresh Animations, Fresh
     * Moves, FreshCompats, FreshFoes, Freshly Modded) drives EMF/ETF animated
     * entity models, the single biggest client CPU cost after shaders.
     */
    private static final String[] HEAVY_PACK_MARKERS = {"fresh"};

    private DeckMode() {}

    private static Path snapshotPath() {
        return FMLPaths.CONFIGDIR.get().resolve("soa_deck_mode.json");
    }

    public static boolean isActive() {
        return Files.exists(snapshotPath());
    }

    /** @return true if deck mode is active after the call. */
    public static boolean toggle() {
        if (isActive()) {
            restore();
            return isActive();
        }
        apply();
        return isActive();
    }

    private static void apply() {
        Minecraft mc = Minecraft.getInstance();
        Options opts = mc.options;
        PackRepository repo = mc.getResourcePackRepository();

        JsonObject snap = new JsonObject();
        snap.addProperty("renderDistance", opts.renderDistance().get());
        snap.addProperty("simulationDistance", opts.simulationDistance().get());
        snap.addProperty("graphicsMode", opts.graphicsMode().get().name());
        snap.addProperty("particles", opts.particles().get().name());
        snap.addProperty("entityShadows", opts.entityShadows().get());
        snap.addProperty("mipmapLevels", opts.mipmapLevels().get());
        snap.addProperty("biomeBlendRadius", opts.biomeBlendRadius().get());
        snap.addProperty("framerateLimit", opts.framerateLimit().get());
        snap.addProperty("enableVsync", opts.enableVsync().get());
        snap.addProperty("entityDistanceScaling", opts.entityDistanceScaling().get());
        snap.addProperty("shadersEnabled", areShadersEnabled());
        JsonArray packs = new JsonArray();
        repo.getSelectedIds().forEach(packs::add);
        snap.add("selectedPacks", packs);

        try {
            Files.writeString(snapshotPath(), GSON.toJson(snap));
        } catch (IOException e) {
            LOG.error("Deck mode: could not write snapshot, aborting so state stays restorable", e);
            return;
        }

        opts.renderDistance().set(7);
        opts.simulationDistance().set(5);
        opts.graphicsMode().set(GraphicsStatus.FAST);
        opts.particles().set(ParticleStatus.DECREASED);
        opts.entityShadows().set(false);
        opts.mipmapLevels().set(2);
        opts.biomeBlendRadius().set(1);
        opts.framerateLimit().set(60);
        opts.enableVsync().set(true);
        opts.entityDistanceScaling().set(0.75);
        opts.save();

        setShadersEnabled(false);

        List<String> keep = new ArrayList<>();
        for (String id : repo.getSelectedIds()) {
            if (!isHeavyPack(id)) {
                keep.add(id);
            }
        }
        applyPackSelection(mc, repo, keep);

        LOG.info("Deck mode ON: render 7 / sim 5 / fast graphics, shaders off, {} pack(s) deselected",
                packs.size() - keep.size());
    }

    private static void restore() {
        Minecraft mc = Minecraft.getInstance();
        Options opts = mc.options;
        PackRepository repo = mc.getResourcePackRepository();

        JsonObject snap;
        try {
            snap = JsonParser.parseString(Files.readString(snapshotPath())).getAsJsonObject();
        } catch (Exception e) {
            LOG.error("Deck mode: snapshot unreadable; leaving settings as-is. Delete config/soa_deck_mode.json to clear the toggle.", e);
            return;
        }

        opts.renderDistance().set(snap.get("renderDistance").getAsInt());
        opts.simulationDistance().set(snap.get("simulationDistance").getAsInt());
        opts.graphicsMode().set(GraphicsStatus.valueOf(snap.get("graphicsMode").getAsString()));
        opts.particles().set(ParticleStatus.valueOf(snap.get("particles").getAsString()));
        opts.entityShadows().set(snap.get("entityShadows").getAsBoolean());
        opts.mipmapLevels().set(snap.get("mipmapLevels").getAsInt());
        opts.biomeBlendRadius().set(snap.get("biomeBlendRadius").getAsInt());
        opts.framerateLimit().set(snap.get("framerateLimit").getAsInt());
        opts.enableVsync().set(snap.get("enableVsync").getAsBoolean());
        opts.entityDistanceScaling().set(snap.get("entityDistanceScaling").getAsDouble());
        opts.save();

        setShadersEnabled(snap.get("shadersEnabled").getAsBoolean());

        List<String> packs = new ArrayList<>();
        snap.getAsJsonArray("selectedPacks").forEach(el -> packs.add(el.getAsString()));

        try {
            Files.deleteIfExists(snapshotPath());
        } catch (IOException e) {
            LOG.warn("Deck mode: could not delete snapshot file", e);
        }

        // PackRepository.setSelected silently drops ids that no longer exist,
        // so packs removed from the instance since the snapshot are safe.
        applyPackSelection(mc, repo, packs);

        LOG.info("Deck mode OFF: previous settings restored");
    }

    private static boolean isHeavyPack(String packId) {
        String lower = packId.toLowerCase(Locale.ROOT);
        for (String marker : HEAVY_PACK_MARKERS) {
            if (lower.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static void applyPackSelection(Minecraft mc, PackRepository repo, List<String> ids) {
        boolean changed = !new ArrayList<>(repo.getSelectedIds()).equals(ids);
        repo.setSelected(ids);
        // updateResourcePacks only reloads when the selection changed; force a
        // reload otherwise so the mipmap/graphics changes still take effect.
        mc.options.updateResourcePacks(repo);
        if (!changed) {
            mc.reloadResourcePacks();
        }
    }

    // ── Oculus (Iris API) via reflection — mirrors the mod's soft-dep style,
    //    no compile dependency, no classloading when Oculus is absent. ──

    private static boolean areShadersEnabled() {
        if (!ModList.get().isLoaded("oculus")) {
            return false;
        }
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            Object config = api.getMethod("getConfig").invoke(instance);
            return (Boolean) Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig")
                    .getMethod("areShadersEnabled").invoke(config);
        } catch (Throwable t) {
            LOG.warn("Deck mode: could not query Oculus shader state", t);
            return false;
        }
    }

    private static void setShadersEnabled(boolean enabled) {
        if (!ModList.get().isLoaded("oculus")) {
            return;
        }
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            Object config = api.getMethod("getConfig").invoke(instance);
            Class.forName("net.irisshaders.iris.api.v0.IrisApiConfig")
                    .getMethod("setShadersEnabledAndApply", boolean.class).invoke(config, enabled);
        } catch (Throwable t) {
            LOG.warn("Deck mode: could not set Oculus shader state", t);
        }
    }
}
