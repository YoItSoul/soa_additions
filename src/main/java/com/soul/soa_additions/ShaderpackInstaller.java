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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts the bundled SoA Radiance shaderpack into the game's shaderpacks
 * directory.  The pack is bundled as a single zip resource
 * ({@code assets/soa_additions/shaderpacks/SoA_Radiance.zip}, produced by the
 * {@code zipShaderpack} gradle task from the repo-root {@code SoA_Radiance/}
 * folder) so the file list can never drift out of date.
 * Uses a version stamp file to decide when to overwrite:
 * <ul>
 *   <li>No stamp file (legacy / first install) &rarr; always extract</li>
 *   <li>Stamp present but older than current mod version &rarr; extract</li>
 *   <li>Stamp matches current version &rarr; skip</li>
 * </ul>
 */
public final class ShaderpackInstaller {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions");
    private static final String PACK_NAME = "SoA_Radiance";
    private static final String PACK_ZIP = "/assets/soa_additions/shaderpacks/" + PACK_NAME + ".zip";
    private static final String STAMP_FILE = ".soa_version";

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

        // Extract the bundled zip
        try (InputStream raw = ShaderpackInstaller.class.getResourceAsStream(PACK_ZIP)) {
            if (raw == null) {
                LOG.error("Bundled shaderpack zip missing from jar: {}", PACK_ZIP);
                return;
            }

            int extracted = 0;
            try (ZipInputStream zip = new ZipInputStream(raw)) {
                Path packDirNormalized = packDir.normalize();
                for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                    if (entry.isDirectory()) continue;
                    Path target = packDir.resolve(entry.getName()).normalize();
                    if (!target.startsWith(packDirNormalized)) { // zip-slip guard
                        LOG.warn("Skipping suspicious zip entry: {}", entry.getName());
                        continue;
                    }
                    Files.createDirectories(target.getParent());
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
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
