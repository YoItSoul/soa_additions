package com.soul.soa_additions.donor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code /soa donor sync} — pulls the latest donor list from the Souls of
 * Avarice telemetry API and merges it into the local donor registry.
 *
 * <p>Endpoint: {@code GET https://telemetry.soulsofavarice.com/api/supporters}<br>
 * Returns a JSON array of supporter objects with fields:
 * {@code name}, {@code tier}, {@code ign}, {@code message}, {@code date}.
 * The {@code ign} field is the Minecraft username (may be null).
 * The {@code tier} field is one of: "draconium", "voidmetal", "netherite".
 * Entries without an {@code ign} are skipped.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class DonorSyncCommand {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/donor-sync");
    private static final String API_URL = "https://telemetry.soulsofavarice.com/api/supporters";
    private static final Gson GSON = new Gson();

    private DonorSyncCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("soa")
                        .then(Commands.literal("donor")
                                .then(Commands.literal("sync")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(DonorSyncCommand::run))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();

        source.sendSuccess(
                () -> Component.literal("[SOA] Syncing donors from API...")
                        .withStyle(ChatFormatting.YELLOW),
                false);

        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    server.execute(() -> source.sendFailure(
                            Component.literal("[SOA] API returned status " + response.statusCode())));
                    return;
                }

                JsonArray arr = GSON.fromJson(response.body(), JsonArray.class);
                if (arr == null) {
                    server.execute(() -> source.sendFailure(
                            Component.literal("[SOA] API returned invalid JSON")));
                    return;
                }

                GameProfileCache profileCache = server.getProfileCache();
                int added = 0, updated = 0, skipped = 0;

                for (JsonElement elem : arr) {
                    JsonObject obj = elem.getAsJsonObject();

                    // ign is the Minecraft username — skip if null/absent
                    if (!obj.has("ign") || obj.get("ign").isJsonNull()
                            || obj.get("ign").getAsString().trim().isEmpty()) {
                        skipped++;
                        continue;
                    }
                    String ign = obj.get("ign").getAsString().trim();

                    // Look up UUID from the server's profile cache (usercache.json)
                    UUID uuid = null;
                    if (profileCache != null) {
                        Optional<com.mojang.authlib.GameProfile> profile =
                                profileCache.get(ign);
                        if (profile.isPresent()) {
                            uuid = profile.get().getId();
                        }
                    }
                    // Fallback: check online players
                    if (uuid == null) {
                        var online = server.getPlayerList().getPlayerByName(ign);
                        if (online != null) uuid = online.getUUID();
                    }
                    if (uuid == null) {
                        LOG.debug("Skipping supporter '{}' — IGN not found in profile cache", ign);
                        skipped++;
                        continue;
                    }

                    // Tier
                    DonorData.Tier tier = DonorData.Tier.VOID;
                    if (obj.has("tier") && !obj.get("tier").isJsonNull()) {
                        tier = DonorData.Tier.fromName(obj.get("tier").getAsString());
                    }

                    // Date — format is "YYYY-MM" (e.g. "2024-11")
                    Instant donated = Instant.now();
                    if (obj.has("date") && !obj.get("date").isJsonNull()) {
                        try {
                            String dateStr = obj.get("date").getAsString().trim();
                            YearMonth ym = YearMonth.parse(dateStr);
                            donated = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                        } catch (Exception ignored) {}
                    }

                    // Message
                    String message = "";
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        message = obj.get("message").getAsString();
                    }

                    // Ko-fi display name (for the wall)
                    String displayName = ign;
                    if (obj.has("name") && !obj.get("name").isJsonNull()) {
                        displayName = obj.get("name").getAsString();
                    }

                    DonorData existing = DonorRegistry.get(uuid).orElse(null);
                    if (existing == null) {
                        DonorRegistry.add(new DonorData(uuid, displayName, tier, donated, message));
                        added++;
                    } else {
                        // Upgrade tier if higher, update name
                        boolean changed = false;
                        DonorData.Tier bestTier = tier.ordinal() > existing.tier().ordinal() ? tier : existing.tier();
                        String bestName = displayName;
                        String bestMsg = existing.message().isEmpty() ? message : existing.message();
                        if (bestTier != existing.tier() || !bestName.equals(existing.name())) {
                            DonorRegistry.add(new DonorData(uuid, bestName, bestTier, existing.donatedAt(), bestMsg));
                            updated++;
                        }
                    }
                }

                int finalAdded = added, finalUpdated = updated, finalSkipped = skipped;
                server.execute(() -> {
                    DonorLifecycleHandler.syncToAll();
                    source.sendSuccess(
                            () -> Component.literal("[SOA] Sync complete: " + finalAdded + " added, "
                                            + finalUpdated + " updated, " + finalSkipped + " skipped (no IGN/UUID)")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                });

                LOG.info("Donor sync: {} added, {} updated, {} skipped from {} API entries",
                        finalAdded, finalUpdated, finalSkipped, arr.size());

            } catch (Exception e) {
                LOG.error("Donor sync failed", e);
                server.execute(() -> source.sendFailure(
                        Component.literal("[SOA] Sync failed: " + e.getMessage())));
            }
        }, "soa-donor-sync");
        thread.setDaemon(true);
        thread.start();

        return Command.SINGLE_SUCCESS;
    }
}
