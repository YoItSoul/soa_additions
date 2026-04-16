package com.soul.soa_additions.quest.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.soul.soa_additions.quest.QuestLoader;
import com.soul.soa_additions.quest.QuestRegistry;
import com.soul.soa_additions.quest.QuestSource;
import com.soul.soa_additions.quest.model.Chapter;
import com.soul.soa_additions.quest.model.Quest;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server→client full quest definition sync. Sent on login so the client's
 * {@link QuestRegistry} matches the server's — including world-edit chapters,
 * datapack overrides, and programmatic quests that the client JAR doesn't have.
 *
 * <p>The payload is the full chapter list serialized as JSON and sent as a
 * UTF string. The client deserializes it through the same {@link QuestLoader}
 * parser the server uses, then replaces its local registry. This guarantees
 * format parity without duplicating any serialization logic.</p>
 *
 * <p>For typical modpack quest trees (hundreds of quests) this is a few KB —
 * well within Forge's packet size limits. If it ever grows too large,
 * compression or chunking can be added later.</p>
 */
public record QuestDefinitionSyncPacket(String chaptersJson) {

    private static final Logger LOG = LoggerFactory.getLogger("soa_additions/quest-def-sync");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static void encode(QuestDefinitionSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.chaptersJson, 1 << 22); // allow up to 4MB
    }

    public static QuestDefinitionSyncPacket decode(FriendlyByteBuf buf) {
        return new QuestDefinitionSyncPacket(buf.readUtf(1 << 22));
    }

    public static void handle(QuestDefinitionSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.applyOnClient(pkt)));
        c.setPacketHandled(true);
    }

    // ---------- server-side build ----------

    public static QuestDefinitionSyncPacket build() {
        JsonArray arr = new JsonArray();
        for (Chapter ch : QuestRegistry.allChapters()) {
            arr.add(serializeChapter(ch));
        }
        return new QuestDefinitionSyncPacket(GSON.toJson(arr));
    }

    public static void sendTo(ServerPlayer player) {
        com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                build()
        );
    }

    /** Broadcast to all connected players. Used after datapack reload or
     *  editor mutations that change quest structure. */
    public static void sendToAll(net.minecraft.server.MinecraftServer server) {
        QuestDefinitionSyncPacket pkt = build();
        com.soul.soa_additions.network.ModNetworking.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                pkt
        );
    }

    // ---------- client-side apply ----------

    /** Isolated in its own class so client-only references (ClientQuestEditState,
     *  QuestBookScreen) are never resolved on a dedicated server. */
    @OnlyIn(Dist.CLIENT)
    static final class ClientHandler {
        static void applyOnClient(QuestDefinitionSyncPacket pkt) {
            try {
                JsonArray arr = GSON.fromJson(pkt.chaptersJson, JsonArray.class);
                List<Chapter> chapters = new ArrayList<>();
                for (var el : arr) {
                    JsonObject root = el.getAsJsonObject();
                    Chapter ch = QuestLoader.parseChapterForWorldEdits(root);
                    if (ch != null) chapters.add(ch);
                }
                chapters.sort(Comparator.comparingInt(Chapter::sortOrder).thenComparing(Chapter::id));
                QuestRegistry.replace(chapters);
                ClientQuestEditState.clear();
                LOG.debug("Received quest definitions: {} chapters, {} quests",
                        chapters.size(), chapters.stream().mapToInt(c -> c.quests().size()).sum());
                com.soul.soa_additions.quest.client.QuestBookScreen.onChapterMutated(null);
            } catch (Exception e) {
                LOG.error("Failed to apply quest definition sync: {}", e.getMessage(), e);
            }
        }
    }

    // ---------- serialization (mirrors FileQuestOverrideStorage) ----------

    private static JsonObject serializeChapter(Chapter chapter) {
        JsonObject out = new JsonObject();
        out.addProperty("id", chapter.id());
        out.addProperty("title", chapter.title());
        out.addProperty("icon", chapter.icon());
        out.addProperty("sort_order", chapter.sortOrder());

        JsonArray desc = new JsonArray();
        for (String line : chapter.description()) desc.add(line);
        out.add("description", desc);

        JsonArray modes = new JsonArray();
        chapter.modes().forEach(m -> modes.add(m.lower()));
        out.add("modes", modes);

        JsonArray deps = new JsonArray();
        for (String d : chapter.requiresChapters()) deps.add(d);
        out.add("requires_chapter", deps);

        if (!chapter.requiresQuests().isEmpty()) {
            JsonArray rq = new JsonArray();
            for (String q : chapter.requiresQuests()) rq.add(q);
            out.add("requires_quests", rq);
        }
        if (chapter.visibility() != com.soul.soa_additions.quest.model.Visibility.NORMAL) {
            out.addProperty("visibility", chapter.visibility().lower());
        }
        if (chapter.hasParent()) {
            out.addProperty("parent_chapter", chapter.parentChapter());
        }

        JsonArray quests = new JsonArray();
        for (Quest q : chapter.quests()) quests.add(serializeQuest(q));
        out.add("quests", quests);

        return out;
    }

    private static JsonObject serializeQuest(Quest q) {
        JsonObject out = new JsonObject();
        out.addProperty("id", q.id());
        out.addProperty("title", q.title());
        out.addProperty("icon", q.icon());
        if (q.visibility() != com.soul.soa_additions.quest.model.Visibility.NORMAL) {
            out.addProperty("visibility", q.visibility().lower());
        }
        if (q.optional()) out.addProperty("optional", true);
        if (!q.depsAll()) out.addProperty("dependency_logic", "any");
        if (q.minDeps() > 0) out.addProperty("min_deps", q.minDeps());
        if (q.autoClaim()) out.addProperty("auto_claim", true);
        if (!q.showDeps()) out.addProperty("show_deps", false);
        if (q.shape() != com.soul.soa_additions.quest.model.NodeShape.ICON) {
            out.addProperty("shape", q.shape().name().toLowerCase());
        }
        if (q.hasManualPosition()) {
            out.addProperty("x", q.posX());
            out.addProperty("y", q.posY());
        }
        if (q.size() > 0 && q.size() != Quest.DEFAULT_SIZE) {
            out.addProperty("size", q.size());
        }
        if (q.repeatable()) {
            out.addProperty("repeatable", true);
            if (q.repeatScope() != com.soul.soa_additions.quest.model.RewardScope.TEAM) {
                out.addProperty("repeat_scope", q.repeatScope().lower());
            }
        }
        if (q.exclusions() != null && !q.exclusions().isEmpty()) {
            JsonArray ex = new JsonArray();
            for (String s : q.exclusions()) ex.add(s);
            out.add("exclusions", ex);
        }

        JsonArray descArr = new JsonArray();
        for (String line : q.description()) descArr.add(line);
        out.add("description", descArr);

        JsonArray depsArr = new JsonArray();
        for (String d : q.dependencies()) depsArr.add(d);
        out.add("dependencies", depsArr);

        JsonArray tasks = new JsonArray();
        for (QuestTask t : q.tasks()) {
            JsonObject taskJson = new JsonObject();
            t.writeJson(taskJson);
            tasks.add(taskJson);
        }
        out.add("tasks", tasks);

        JsonArray rewards = new JsonArray();
        for (QuestReward r : q.rewards()) {
            JsonObject rewardJson = new JsonObject();
            r.writeJson(rewardJson);
            rewards.add(rewardJson);
        }
        out.add("rewards", rewards);

        JsonArray modesArr = new JsonArray();
        q.modes().forEach(m -> modesArr.add(m.lower()));
        out.add("modes", modesArr);

        return out;
    }
}
