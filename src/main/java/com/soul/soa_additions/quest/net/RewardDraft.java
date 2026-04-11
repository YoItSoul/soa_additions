package com.soul.soa_additions.quest.net;

import com.google.gson.JsonObject;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import com.soul.soa_additions.quest.reward.CommandReward;
import com.soul.soa_additions.quest.reward.ItemReward;
import com.soul.soa_additions.quest.reward.RewardRegistry;
import com.soul.soa_additions.quest.reward.XpReward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * GUI-side representation of a quest reward. Parallel to {@link TaskDraft}.
 *
 * <p>Three primary types are directly editable in the form:
 * <ul>
 *   <li>{@code ITEM}  — {@code value}=item id, {@code count}=stack count.</li>
 *   <li>{@code XP}    — {@code count}=amount, {@code levels}=true for levels
 *                        instead of points.</li>
 *   <li>{@code COMMAND} — {@code value}=command string (no leading slash;
 *                          placeholders {player} / {uuid} supported).</li>
 * </ul>
 *
 * <p>Reward types the editor doesn't know how to express (grant_stage,
 * lock_packmode, or anything registered by downstream mods) round-trip as
 * {@code OTHER}, carrying the original JSON body in {@code aux} and the
 * reward type id in {@code value}. They render read-only in the form but
 * are preserved across save so the editor can't silently drop them.
 */
public record RewardDraft(
        Type type,
        String value,
        int count,
        boolean levels,
        RewardScope scope,
        String aux
) {

    public enum Type {
        ITEM, XP, COMMAND, OTHER;

        public Type next() { return values()[(ordinal() + 1) % values().length]; }
        public boolean usesCount() { return this == ITEM || this == XP; }
        public boolean editable()  { return this != OTHER; }

        public String defaultValue() {
            return switch (this) {
                case ITEM    -> "minecraft:diamond";
                case XP      -> "";
                case COMMAND -> "say {player} finished a quest";
                case OTHER   -> "";
            };
        }
    }

    public static RewardDraft blank() {
        return new RewardDraft(Type.ITEM, Type.ITEM.defaultValue(), 1, false, RewardScope.PLAYER, "");
    }

    public RewardDraft withType(Type t) {
        if (t == type) return this;
        return new RewardDraft(
                t,
                t.defaultValue(),
                t == Type.XP ? Math.max(1, count) : (t == Type.ITEM ? Math.max(1, count) : count),
                levels && t == Type.XP,
                scope,
                "");
    }

    // ---------- network ----------

    public static void encode(RewardDraft d, FriendlyByteBuf buf) {
        buf.writeEnum(d.type);
        buf.writeUtf(d.value == null ? "" : d.value);
        buf.writeVarInt(d.count);
        buf.writeBoolean(d.levels);
        buf.writeEnum(d.scope);
        buf.writeUtf(d.aux == null ? "" : d.aux);
    }

    public static RewardDraft decode(FriendlyByteBuf buf) {
        return new RewardDraft(
                buf.readEnum(Type.class),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readEnum(RewardScope.class),
                buf.readUtf());
    }

    // ---------- conversion ----------

    /** Materialize this draft into a live {@link QuestReward}. Returns null if
     *  the draft is malformed; callers should filter nulls. */
    public QuestReward toReward() {
        try {
            return switch (type) {
                case ITEM -> new ItemReward(new ResourceLocation(value), Math.max(1, count), scope);
                case XP -> new XpReward(Math.max(0, count), levels, scope);
                case COMMAND -> new CommandReward(value == null ? "" : value, scope);
                case OTHER -> {
                    // Rehydrate from the cached JSON body we captured at form open.
                    if (aux == null || aux.isEmpty()) yield null;
                    JsonObject body = com.google.gson.JsonParser.parseString(aux).getAsJsonObject();
                    yield RewardRegistry.deserialize(body);
                }
            };
        } catch (Exception e) {
            return null;
        }
    }

    /** Build a draft from a live reward, round-tripping unknown types via
     *  {@link QuestReward#writeJson(JsonObject)} into {@code aux}. */
    public static RewardDraft fromReward(QuestReward r) {
        if (r instanceof ItemReward it) {
            return new RewardDraft(Type.ITEM, it.item().toString(), it.count(), false, it.scope(), "");
        }
        if (r instanceof XpReward xp) {
            return new RewardDraft(Type.XP, "", xp.amount(), xp.levels(), xp.scope(), "");
        }
        if (r instanceof CommandReward cr) {
            return new RewardDraft(Type.COMMAND, cr.command(), 0, false, cr.scope(), "");
        }
        // Preserve unknown types verbatim.
        JsonObject body = new JsonObject();
        try { r.writeJson(body); } catch (Exception ignored) {}
        String typeId = body.has("type") ? body.get("type").getAsString() : r.type().toString();
        return new RewardDraft(Type.OTHER, typeId, 0, false, r.scope(), body.toString());
    }

    /** Short display label for the form row. */
    public String displayLabel() {
        return switch (type) {
            case ITEM    -> "Item: " + value + " x" + count;
            case XP      -> "+" + count + (levels ? " levels" : " XP");
            case COMMAND -> "Run: " + value;
            case OTHER   -> "Other: " + value;
        };
    }
}
