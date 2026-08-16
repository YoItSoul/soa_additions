package com.soul.soa_additions.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

/**
 * Persistent record of the user's telemetry choice. Nothing is ever sent
 * until this is {@link State#ACCEPTED} — on clients that happens through
 * {@code SoaConsentScreen} on first launch; on dedicated servers the
 * owner opts in by writing {@code accepted} to the consent file.
 *
 * <p>File: {@code config/soa_additions/telemetry_consent.txt}. First line is
 * {@code accepted} or {@code declined}; a second timestamp line records when
 * the choice was made. Deleting the file re-prompts on the next launch.
 */
public final class TelemetryConsent {

    public enum State { UNDECIDED, ACCEPTED, DECLINED }

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_Telemetry");
    private static volatile State cached;

    private TelemetryConsent() {}

    private static Path consentFile() {
        return Path.of("config", "soa_additions", "telemetry_consent.txt");
    }

    public static State get() {
        State s = cached;
        if (s != null) return s;
        s = load();
        cached = s;
        return s;
    }

    public static boolean isAccepted() {
        return get() == State.ACCEPTED;
    }

    /** Persist the user's choice. UNDECIDED deletes the file (re-prompts next launch). */
    public static void set(State state) {
        cached = state;
        try {
            Path file = consentFile();
            if (state == State.UNDECIDED) {
                Files.deleteIfExists(file);
                return;
            }
            Files.createDirectories(file.getParent());
            String word = state == State.ACCEPTED ? "accepted" : "declined";
            Files.writeString(file, word + System.lineSeparator() + Instant.now());
            LOGGER.info("Telemetry consent recorded: {}", word);
        } catch (IOException e) {
            LOGGER.warn("Could not persist telemetry consent ({}); treating as declined for this session.", e.toString());
            // In-memory `cached` keeps the user's actual choice for this session;
            // an unwritable file simply means they get re-asked next launch.
        }
    }

    private static State load() {
        try {
            Path file = consentFile();
            if (!Files.exists(file)) return State.UNDECIDED;
            String first = Files.readAllLines(file).stream().findFirst().orElse("").trim()
                    .toLowerCase(Locale.ROOT);
            if (first.equals("accepted")) return State.ACCEPTED;
            if (first.equals("declined")) return State.DECLINED;
            return State.UNDECIDED;
        } catch (IOException e) {
            // Unreadable file: fail closed — no consent, no telemetry.
            return State.DECLINED;
        }
    }
}
