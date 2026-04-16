package com.soul.soa_additions.quest.net;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side bag of quest position overrides applied on top of the auto
 * layout. Two paths populate this:
 *
 * <ul>
 *   <li>The user is dragging a node locally — the screen calls
 *       {@link #setLocal} every mouse-move to keep the node under the cursor
 *       without waiting for a server round trip.</li>
 *   <li>The server broadcasts a {@link QuestMovePacket} (either confirming
 *       the local drag or reporting another op's edit) — {@link #applyMove}
 *       writes the authoritative coords in.</li>
 * </ul>
 *
 * <p>Both paths write to the same map: the server broadcast arrives after
 * the local drag release, so it harmlessly overwrites the client estimate
 * with the authoritative value.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientQuestEditState {

    private ClientQuestEditState() {}

    private static final Map<String, int[]> POSITIONS = new ConcurrentHashMap<>();

    public static void setLocal(String fullId, int x, int y) {
        POSITIONS.put(fullId, new int[]{x, y});
    }

    public static int[] get(String fullId) {
        return POSITIONS.get(fullId);
    }

    public static void clear() {
        POSITIONS.clear();
    }

    public static void applyMove(QuestMovePacket pkt) {
        POSITIONS.put(pkt.chapterId() + "/" + pkt.questId(), new int[]{pkt.x(), pkt.y()});
    }
}
