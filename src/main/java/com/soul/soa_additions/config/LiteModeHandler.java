package com.soul.soa_additions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Applies maximum-performance client settings when lite mode is enabled.
 * Runs once per login (not every tick) to overwrite vanilla + mod configs.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, value = Dist.CLIENT)
public final class LiteModeHandler {
    private static final Logger LOG = LoggerFactory.getLogger("SOA-LiteMode");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private LiteModeHandler() {}

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!LiteModeConfig.ENABLED.get()) return;
        LOG.info("Lite mode enabled — applying performance settings");
        try {
            applyAll();
        } catch (Exception e) {
            LOG.error("Failed to apply lite mode settings", e);
        }
    }

    private static void applyAll() {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();

        applyVanillaOptions();
        applyOculus(gameDir);
        applyEmbeddium(gameDir);
        applyEntityCulling(gameDir);
        applyDynamicLights(gameDir);
        applyFpsReducer(gameDir);
        applyEntityModelFeatures(gameDir);
        applyEntityTextureFeatures(gameDir);
        applyBotania(gameDir);
        applyBetterFoliage(gameDir);
        applyBetterFpsDist(gameDir);
        applyCreate(gameDir);
        applySupplementaries(gameDir);

        LOG.info("Lite mode settings applied");
    }

    // ── Vanilla options (applied via Minecraft.Options) ──────────────

    private static void applyVanillaOptions() {
        Options opts = Minecraft.getInstance().options;
        if (LiteModeConfig.REDUCE_RENDER_DISTANCE.get()) {
            opts.renderDistance().set(LiteModeConfig.RENDER_DISTANCE.get());
        }
        opts.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
        opts.entityDistanceScaling().set(0.5);
        opts.mipmapLevels().set(0);
        opts.particles().set(net.minecraft.client.ParticleStatus.MINIMAL);
        opts.biomeBlendRadius().set(0);
        opts.enableVsync().set(false);
        opts.screenEffectScale().set(0.0);
        opts.fovEffectScale().set(0.0);
        opts.darknessEffectScale().set(0.0);
        opts.entityShadows().set(false);
        opts.graphicsMode().set(net.minecraft.client.GraphicsStatus.FAST);
        opts.ambientOcclusion().set(false);
        opts.save();
    }

    // ── Oculus (shaders + shadow distance) ───────────────────────────

    private static void applyOculus(Path gameDir) {
        Path file = gameDir.resolve("config/oculus.properties");
        if (!Files.exists(file)) return;
        try {
            Properties props = new Properties();
            props.load(Files.newBufferedReader(file, StandardCharsets.UTF_8));
            props.setProperty("enableShaders", "false");
            props.setProperty("maxShadowRenderDistance", "0");
            try (var out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                props.store(out, null);
            }
        } catch (IOException e) { LOG.warn("Failed to patch oculus.properties", e); }
    }

    // ── Embeddium ────────────────────────────────────────────────────

    private static void applyEmbeddium(Path gameDir) {
        Path file = gameDir.resolve("config/embeddium-options.json");
        patchJson(file, json -> {
            setNested(json, "quality", "weather_quality", "FAST");
            setNested(json, "quality", "leaves_quality", "FAST");
            setNested(json, "quality", "enable_vignette", false);
        });
    }

    // ── Entity Culling ───────────────────────────────────────────────

    private static void applyEntityCulling(Path gameDir) {
        Path file = gameDir.resolve("config/entityculling.json");
        patchJson(file, json -> {
            json.addProperty("tracingDistance", 64);
            json.addProperty("solidLeaves", true);
        });
    }

    // ── Dynamic Lights (sodiumdynamiclights) ─────────────────────────

    private static void applyDynamicLights(Path gameDir) {
        Path file = gameDir.resolve("config/sodiumdynamiclights-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*mode\\s*=\\s*)\"?\\w+\"?", "$1\"OFF\"")
                .replaceFirst("(?m)^(\\s*self\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*block_entities\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*entities\\s*=\\s*)\\w+", "$1false"));
    }

    // ── FPS Reducer ──────────────────────────────────────────────────

    private static void applyFpsReducer(Path gameDir) {
        Path file = gameDir.resolve("config/fpsreducer/fpsreducer-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*idleFps\\s*=\\s*)\\d+", "$1" + 5));
    }

    // ── Entity Model Features ────────────────────────────────────────

    private static void applyEntityModelFeatures(Path gameDir) {
        Path file = gameDir.resolve("config/entity_model_features.json");
        patchJson(file, json -> {
            json.addProperty("animationLODDistance", 10);
            json.addProperty("retainDetailOnLowFps", false);
        });
    }

    // ── Entity Texture Features ──────────────────────────────────────

    private static void applyEntityTextureFeatures(Path gameDir) {
        Path file = gameDir.resolve("config/entity_texture_features.json");
        patchJson(file, json -> {
            json.addProperty("textureUpdateFrequency_V2", "Slow");
            json.addProperty("enableEmissiveTextures", false);
            json.addProperty("enableEmissiveBlockEntities", false);
        });
    }

    // ── Botania ──────────────────────────────────────────────────────

    private static void applyBotania(Path gameDir) {
        Path file = gameDir.resolve("config/botania-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*shaders\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*staticFloaters\\s*=\\s*)\\w+", "$1true")
                .replaceFirst("(?m)^(\\s*powerSystem\\s*=\\s*)\\w+", "$1true")
                .replaceFirst("(?m)^(\\s*flowerFrequency\\s*=\\s*)[\\d.]+", "$10.25"));
    }

    // ── Better Foliage ───────────────────────────────────────────────

    private static void applyBetterFoliage(Path gameDir) {
        Path file = gameDir.resolve("config/betterfoliage-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*particleAttempts\\s*=\\s*)\\d+", "$10")
                .replaceFirst("(?m)^(\\s*souls\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*leaves\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*snowballs\\s*=\\s*)\\w+", "$1false"));
    }

    // ── BetterFpsDist ────────────────────────────────────────────────

    private static void applyBetterFpsDist(Path gameDir) {
        Path file = gameDir.resolve("config/betterfpsdist.json");
        patchJson(file, json -> {
            json.addProperty("verticalScaling", 1.5);
            json.addProperty("horizontalScaling", 0.8);
        });
    }

    // ── Create ───────────────────────────────────────────────────────

    private static void applyCreate(Path gameDir) {
        Path file = gameDir.resolve("config/create-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*fanParticleDensity\\s*=\\s*)[\\d.]+", "$10.1")
                .replaceFirst("(?m)^(\\s*filterItemRenderDistance\\s*=\\s*)[\\d.]+", "$15.0"));
    }

    // ── Supplementaries ──────────────────────────────────────────────

    private static void applySupplementaries(Path gameDir) {
        Path file = gameDir.resolve("config/supplementaries-client.toml");
        patchToml(file, content -> content
                .replaceFirst("(?m)^(\\s*fancy_renderers\\s*=\\s*)\\w+", "$1false")
                .replaceFirst("(?m)^(\\s*turn_particles\\s*=\\s*)\\w+", "$1false"));
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static void patchJson(Path file, java.util.function.Consumer<JsonObject> patcher) {
        if (!Files.exists(file)) return;
        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
            patcher.accept(json);
            Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (Exception e) { LOG.warn("Failed to patch {}", file, e); }
    }

    private static void patchToml(Path file, java.util.function.UnaryOperator<String> patcher) {
        if (!Files.exists(file)) return;
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String patched = patcher.apply(content);
            if (!patched.equals(content)) {
                Files.writeString(file, patched, StandardCharsets.UTF_8);
            }
        } catch (IOException e) { LOG.warn("Failed to patch {}", file, e); }
    }

    private static void setNested(JsonObject root, String section, String key, Object value) {
        JsonObject s = root.has(section) ? root.getAsJsonObject(section) : new JsonObject();
        if (value instanceof Boolean b) s.addProperty(key, b);
        else if (value instanceof Number n) s.addProperty(key, n);
        else s.addProperty(key, value.toString());
        root.add(section, s);
    }
}
