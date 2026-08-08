package com.soul.soa_additions.anticheat.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether a resource pack is an xray pack by what it does, not by what it is called.
 *
 * <p>Name matching was the whole detection story and it cost nothing to defeat: rename the folder,
 * edit one line of {@code pack.mcmeta}, done. Content cannot be renamed away. An xray pack has
 * exactly one job — make ordinary blocks see-through — and there are only three ways to do it, all
 * of which are visible on disk:</p>
 *
 * <ol>
 *   <li>override a block texture with a fully transparent image;</li>
 *   <li>override a block model with one that draws nothing ({@code elements} absent);</li>
 *   <li>point a blockstate at such a model.</li>
 * </ol>
 *
 * <p>This checks the first two against blocks that are opaque in every legitimate pack. A pack that
 * makes stone invisible is an xray pack whatever it calls itself, and a pack that stops making
 * stone invisible has stopped being an xray pack — which is the property that makes this worth
 * doing where keyword matching was not.</p>
 *
 * <p>Fullbright is a different mechanism and gets a separate, weaker signal: since 1.17 those packs
 * override core shaders to ignore the lightmap, and legitimate packs almost never ship
 * {@code assets/minecraft/shaders/core}. That is reported as suspicion, not proof.</p>
 */
public final class PackContentScanner {

    /**
     * Blocks that are fully opaque in vanilla and in every legitimate pack. Chosen as the bulk
     * blocks you tunnel through — the ones an xray pack must clear to be useful at all.
     */
    private static final String[] MUST_BE_OPAQUE = {
            "stone", "deepslate", "dirt", "andesite", "diorite", "granite",
            "tuff", "netherrack", "sand", "gravel", "end_stone", "blackstone", "basalt"
    };

    /** A pixel this close to invisible counts as transparent. */
    private static final int ALPHA_THRESHOLD = 16;
    /** Fraction of transparent pixels above which the texture is "see-through", not just detailed. */
    private static final double TRANSPARENT_FRACTION = 0.85D;
    /** How many blocks must be cleared before we call it xray rather than a stylistic choice. */
    private static final int MIN_HITS = 2;

    private PackContentScanner() {}

    /**
     * Inspects one pack. Returns findings as {@code <kind>|<pack id>|<evidence>}, empty when clean.
     *
     * <p>Never throws: a corrupt or exotic pack must not stop the player logging in, and a pack we
     * failed to read is reported as nothing rather than as guilt.</p>
     */
    public static List<String> scan(Pack pack) {
        List<String> findings = new ArrayList<>();
        String id = pack.getId();
        try (PackResources resources = pack.open()) {
            List<String> transparent = new ArrayList<>();
            List<String> emptyModels = new ArrayList<>();

            for (String block : MUST_BE_OPAQUE) {
                if (hasTransparentTexture(resources, block)) transparent.add(block);
                else if (hasEmptyModel(resources, block)) emptyModels.add(block);
            }

            if (transparent.size() + emptyModels.size() >= MIN_HITS) {
                List<String> all = new ArrayList<>(transparent);
                all.addAll(emptyModels);
                findings.add("xray|" + id + "|" + String.join(",", all));
            }

            if (overridesCoreShaders(resources)) {
                findings.add("fullbright|" + id + "|overrides core shaders");
            }
        } catch (Throwable ignored) {
            // Unreadable pack — report nothing. A false accusation now costs the player access.
        }
        return findings;
    }

    /** True when the pack replaces this block's texture with an essentially invisible image. */
    private static boolean hasTransparentTexture(PackResources resources, String block) {
        IoSupplier<InputStream> supplier = resources.getResource(PackType.CLIENT_RESOURCES,
                new ResourceLocation("minecraft", "textures/block/" + block + ".png"));
        if (supplier == null) return false;
        try (InputStream in = supplier.get(); NativeImage image = NativeImage.read(in)) {
            long transparent = 0;
            long total = (long) image.getWidth() * image.getHeight();
            if (total == 0) return false;
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    // NativeImage packs ABGR, so alpha is the top byte.
                    int alpha = (image.getPixelRGBA(x, y) >> 24) & 0xFF;
                    if (alpha < ALPHA_THRESHOLD) transparent++;
                }
            }
            return (double) transparent / total >= TRANSPARENT_FRACTION;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * True when the pack overrides this block's model with one that draws nothing.
     *
     * <p>A model with no {@code elements} and no {@code parent} to inherit them from renders as
     * air — the same result as a transparent texture, reached a different way.</p>
     */
    private static boolean hasEmptyModel(PackResources resources, String block) {
        IoSupplier<InputStream> supplier = resources.getResource(PackType.CLIENT_RESOURCES,
                new ResourceLocation("minecraft", "models/block/" + block + ".json"));
        if (supplier == null) return false;
        try (InputStream in = supplier.get(); InputStreamReader reader = new InputStreamReader(in)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            boolean hasElements = json.has("elements") && json.getAsJsonArray("elements").size() > 0;
            boolean hasParent = json.has("parent");
            return !hasElements && !hasParent;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Legitimate packs essentially never ship core shaders; fullbright packs must. */
    private static boolean overridesCoreShaders(PackResources resources) {
        for (String shader : new String[]{"rendertype_solid", "rendertype_cutout", "rendertype_cutout_mipped"}) {
            IoSupplier<InputStream> supplier = resources.getResource(PackType.CLIENT_RESOURCES,
                    new ResourceLocation("minecraft", "shaders/core/" + shader + ".fsh"));
            if (supplier != null) return true;
        }
        return false;
    }
}
