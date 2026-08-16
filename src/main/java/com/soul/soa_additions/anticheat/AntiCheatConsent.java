package com.soul.soa_additions.anticheat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

/**
 * Persistent record of the player's choice about the anticheat's client-side scan.
 *
 * <p>Deliberately separate from {@code TelemetryConsent}: telemetry sends performance data to the
 * pack author's server, this sends a mod/resource-pack inventory to the Minecraft server being
 * joined. They are different disclosures to different recipients, so they are different decisions
 * with different files. Accepting one never implies the other.</p>
 *
 * <p>{@link ClientModScanner} sends nothing at all until this is {@link State#ACCEPTED}. The choice
 * is made in-game on {@code SoaConsentScreen} at first launch, and can be changed at any time from
 * the title-screen toggle or with {@code /soa anticheat allow|deny}.</p>
 *
 * <p>File: {@code config/soa_additions/anticheat_consent.txt}. First line is {@code accepted} or
 * {@code declined}; a second line records when the choice was made. Deleting the file re-prompts on
 * the next launch. This file must never ship inside a pack export — a pre-answered consent file is
 * not consent.</p>
 */
public final class AntiCheatConsent {

    public enum State { UNDECIDED, ACCEPTED, DECLINED }

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_AntiCheat");
    private static volatile State cached;

    private AntiCheatConsent() {}

    private static Path consentFile() {
        return Path.of("config", "soa_additions", "anticheat_consent.txt");
    }

    public static State get() {
        State s = cached;
        if (s != null) return s;
        s = load();
        cached = s;
        return s;
    }

    /** The only gate the scanner is allowed to ask about. Fails closed on every unknown state. */
    public static boolean isAccepted() {
        return get() == State.ACCEPTED;
    }

    /** Persist the player's choice. UNDECIDED deletes the file (re-prompts next launch). */
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
            LOGGER.info("Anticheat scan consent recorded: {}", word);
        } catch (IOException e) {
            LOGGER.warn("Could not persist anticheat consent ({}); the choice holds for this session only.",
                    e.toString());
            // In-memory `cached` keeps the player's actual choice for this session; an unwritable
            // config dir just means they get asked again next launch.
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
            // Unreadable file: fail closed — no consent, no scan.
            return State.DECLINED;
        }
    }
}
