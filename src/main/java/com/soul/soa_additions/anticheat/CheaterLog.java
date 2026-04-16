package com.soul.soa_additions.anticheat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Append-only hash-chained log of cheater flags. One line per event, each
 * line's hash covers the previous hash so editing or deleting a middle
 * entry visibly breaks the chain.
 *
 * <p>Line format (tab-separated, UTF-8):
 * <pre>{ts}\t{uuid}\t{reason}\t{prevHashHex}\t{thisHashHex}</pre>
 *
 * <p>The latest {@code thisHashHex} is mirrored into {@link CheaterData#logHead}
 * so a cheater who deletes the whole log file can't hide it — the SavedData
 * still has a hash head that no longer matches the (now empty) file.</p>
 *
 * <p>This is NOT a security boundary. Anyone with filesystem access to the
 * server can still tamper, but the chain makes tampering detectable after
 * the fact, and fixing up the chain would require editing every subsequent
 * entry plus the SavedData plus every affected player's NBT tag.</p>
 */
public final class CheaterLog {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/cheater-log");

    public record Entry(long ts, UUID uuid, String reason, String prevHash, String thisHash) {}

    private final Path file;

    public CheaterLog(MinecraftServer server) {
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        this.file = worldRoot.resolve("soa_additions").resolve("cheaters.log");
    }

    public Path file() { return file; }

    /** Append a new entry. Returns the new head hash, or "" on I/O failure. */
    public synchronized String append(UUID uuid, String reason, String prevHead) {
        long ts = System.currentTimeMillis();
        String safeReason = reason == null ? "unspecified" : reason.replace('\t', ' ').replace('\n', ' ');
        String hash = sha256Hex(prevHead + "|" + ts + "|" + uuid + "|" + safeReason);
        String line = ts + "\t" + uuid + "\t" + safeReason + "\t" + prevHead + "\t" + hash + "\n";
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return hash;
        } catch (IOException e) {
            LOG.error("Failed to append cheater log entry: {}", e.getMessage());
            return "";
        }
    }

    /** Read all entries in order. Returns empty list if the file is missing or unreadable. */
    public synchronized List<Entry> readAll() {
        List<Entry> out = new ArrayList<>();
        if (!Files.exists(file)) return out;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", 5);
                if (parts.length != 5) continue;
                try {
                    long ts = Long.parseLong(parts[0]);
                    UUID uuid = UUID.fromString(parts[1]);
                    out.add(new Entry(ts, uuid, parts[2], parts[3], parts[4]));
                } catch (IllegalArgumentException ignored) {
                    // Malformed row — don't swallow silently, but keep reading.
                    LOG.warn("Skipping malformed cheater log line: {}", line);
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to read cheater log: {}", e.getMessage());
        }
        return out;
    }

    /**
     * Verify the chain. Returns the head hash of the last valid entry, or
     * {@code null} if the chain is broken (tampering suspected).
     * An empty log is valid and returns the empty string.
     */
    public String verifyAndGetHead() {
        List<Entry> entries = readAll();
        String prev = "";
        for (Entry e : entries) {
            if (!e.prevHash().equals(prev)) return null;
            String expected = sha256Hex(prev + "|" + e.ts() + "|" + e.uuid() + "|" + e.reason());
            if (!expected.equals(e.thisHash())) return null;
            prev = e.thisHash();
        }
        return prev;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
