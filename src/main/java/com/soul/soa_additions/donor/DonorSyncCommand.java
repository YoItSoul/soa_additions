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
import java.util.UUID;

/**
 * {@code /soa donor sync} — pulls the latest donor list from the Souls of
 * Avarice telemetry API and merges it into the local donor registry.
 *
 * <p>Endpoint: {@code GET https://telemetry.soulsofavarice.com/api/supporters}
 * Returns a JSON array of supporter objects.</p>
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

        // Run the HTTP fetch off the server thread
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

                int added = 0, updated = 0;
                for (JsonElement elem : arr) {
                    JsonObject obj = elem.getAsJsonObject();
                    DonorData donor = parseSupporter(obj);
                    if (donor == null) continue;

                    DonorData existing = DonorRegistry.get(donor.uuid()).orElse(null);
                    if (existing == null) {
                        DonorRegistry.add(donor);
                        added++;
                    } else if (donor.tier().ordinal() > existing.tier().ordinal()
                            || !donor.name().equals(existing.name())) {
                        // Upgrade tier or update name
                        DonorRegistry.add(new DonorData(
                                donor.uuid(), donor.name(),
                                donor.tier().ordinal() > existing.tier().ordinal()
                                        ? donor.tier() : existing.tier(),
                                existing.donatedAt(),
                                existing.message().isEmpty() ? donor.message() : existing.message()));
                        updated++;
                    }
                }

                int finalAdded = added, finalUpdated = updated;
                server.execute(() -> {
                    DonorLifecycleHandler.syncToAll();
                    source.sendSuccess(
                            () -> Component.literal("[SOA] Sync complete: " + finalAdded + " added, "
                                            + finalUpdated + " updated, " + arr.size() + " total from API")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                });

                LOG.info("Donor sync: {} added, {} updated from {} API entries",
                        finalAdded, finalUpdated, arr.size());

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

    /**
     * Parse a supporter JSON object. Expected fields vary by API — this
     * handles common patterns gracefully. At minimum needs a UUID or
     * player name to be useful.
     */
    private static DonorData parseSupporter(JsonObject obj) {
        try {
            // Try to get UUID
            UUID uuid = null;
            if (obj.has("uuid") && !obj.get("uuid").getAsString().isEmpty()) {
                uuid = UUID.fromString(obj.get("uuid").getAsString());
            } else if (obj.has("player_uuid") && !obj.get("player_uuid").getAsString().isEmpty()) {
                uuid = UUID.fromString(obj.get("player_uuid").getAsString());
            }
            if (uuid == null) return null;

            // Name
            String name = "Unknown";
            if (obj.has("name")) name = obj.get("name").getAsString();
            else if (obj.has("player_name")) name = obj.get("player_name").getAsString();
            else if (obj.has("username")) name = obj.get("username").getAsString();

            // Tier
            DonorData.Tier tier = DonorData.Tier.SUPPORTER;
            if (obj.has("tier")) {
                tier = DonorData.Tier.fromName(obj.get("tier").getAsString());
            } else if (obj.has("level")) {
                tier = DonorData.Tier.fromName(obj.get("level").getAsString());
            }

            // Date
            Instant donated = Instant.now();
            if (obj.has("donated_at")) {
                try { donated = Instant.parse(obj.get("donated_at").getAsString()); }
                catch (Exception ignored) {}
            } else if (obj.has("created_at")) {
                try { donated = Instant.parse(obj.get("created_at").getAsString()); }
                catch (Exception ignored) {}
            }

            // Message
            String message = "";
            if (obj.has("message")) message = obj.get("message").getAsString();

            return new DonorData(uuid, name, tier, donated, message);
        } catch (Exception e) {
            LOG.warn("Failed to parse supporter entry: {}", obj, e);
            return null;
        }
    }
}
