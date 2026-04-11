package com.soul.soa_additions;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Extracts the bundled SoA Radiance shaderpack into the game's shaderpacks
 * directory.  Uses a version stamp file to decide when to overwrite:
 * <ul>
 *   <li>No stamp file (legacy / first install) &rarr; always extract</li>
 *   <li>Stamp present but older than current mod version &rarr; extract</li>
 *   <li>Stamp matches current version &rarr; skip</li>
 * </ul>
 */
public final class ShaderpackInstaller {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    private static final String PACK_NAME = "SoA_Radiance";
    private static final String RESOURCE_ROOT = "/assets/soa_additions/shaderpacks/" + PACK_NAME + "/shaders/";
    private static final String STAMP_FILE = ".soa_version";

    private static final String[] SHADER_FILES = {
        "shaders.properties",
        "lib/distortion.glsl",
        "shadow.vsh",
        "shadow.fsh",
        "gbuffers_basic.vsh",
        "gbuffers_basic.fsh",
        "gbuffers_textured.vsh",
        "gbuffers_textured.fsh",
        "gbuffers_textured_lit.vsh",
        "gbuffers_textured_lit.fsh",
        "gbuffers_terrain.vsh",
        "gbuffers_terrain.fsh",
        "gbuffers_water.vsh",
        "gbuffers_water.fsh",
        "gbuffers_skybasic.vsh",
        "gbuffers_skybasic.fsh",
        "gbuffers_entities.vsh",
        "gbuffers_entities.fsh",
        "composite.vsh",
        "composite.fsh",
        "final.vsh",
        "final.fsh",
    };

    private ShaderpackInstaller() {}

    /** Call from mod constructor or common setup. */
    public static void install() {
        String currentVersion = ModList.get()
                .getModContainerById(SoaAdditions.MODID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");

        Path shaderpacksDir = FMLPaths.GAMEDIR.get().resolve("shaderpacks");
        Path packDir = shaderpacksDir.resolve(PACK_NAME);
        Path stampPath = packDir.resolve(STAMP_FILE);

        // Check if extraction is needed
        if (Files.isDirectory(packDir)) {
            if (Files.exists(stampPath)) {
                try {
                    String installedVersion = Files.readString(stampPath).trim();
                    if (currentVersion.equals(installedVersion)) {
                        LOG.debug("SoA Radiance shaderpack v{} already installed, skipping", currentVersion);
                        return;
                    }
                    LOG.info("SoA Radiance shaderpack outdated (installed: {}, current: {}), updating",
                            installedVersion, currentVersion);
                } catch (IOException e) {
                    LOG.warn("Could not read shader version stamp, reinstalling");
                }
            } else {
                LOG.info("SoA Radiance shaderpack has no version stamp, replacing with v{}", currentVersion);
            }
        } else {
            LOG.info("Installing SoA Radiance shaderpack v{} to {}", currentVersion, packDir);
        }

        // Extract all shader files
        try {
            Path shadersDir = packDir.resolve("shaders");
            Files.createDirectories(shadersDir.resolve("lib"));

            int extracted = 0;
            for (String file : SHADER_FILES) {
                String resourcePath = RESOURCE_ROOT + file;
                try (InputStream in = ShaderpackInstaller.class.getResourceAsStream(resourcePath)) {
                    if (in == null) {
                        LOG.warn("Missing bundled shader resource: {}", resourcePath);
                        continue;
                    }
                    Path target = shadersDir.resolve(file.replace('/', java.io.File.separatorChar));
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    extracted++;
                }
            }

            // Write version stamp
            Files.writeString(stampPath, currentVersion);

            LOG.info("SoA Radiance shaderpack v{} installed successfully ({} files)", currentVersion, extracted);
        } catch (IOException e) {
            LOG.error("Failed to install SoA Radiance shaderpack", e);
        }
    }
}
