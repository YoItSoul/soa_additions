package com.soul.soa_additions.quest.task;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestTask;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Have {@code count} of an item (or any item in a tag) in inventory, with an
 * optional NBT subset filter. Set {@code consume=true} to deduct on claim.
 *
 * <p>Exactly one of {@code item} or {@code tag} is set. Optional {@code nbt}
 * is a CompoundTag string (SNBT) — a stack only counts if its NBT contains
 * every key/value listed in the filter. Extra NBT on the stack is fine, so
 * "named Truther" matches whether the stack also has an enchantment.</p>
 *
 * <p>Example — stick named "Truther":
 * {@code { "type":"item", "item":"minecraft:stick", "count":1,
 *          "nbt":"{display:{Name:'\"Truther\"'}}" }}</p>
 */
public record ItemTask(ResourceLocation item, ResourceLocation tag, CompoundTag nbt, int count, boolean consume) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "item");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        String base = tag != null
                ? "Obtain " + count + "x any " + TaskNames.prettyTag(tag.toString())
                : "Obtain " + count + "x " + TaskNames.item(item.toString());
        if (nbt != null) {
            String name = readDisplayName(nbt);
            if (name != null) base += " named \"" + name + "\"";
            else base += " (with NBT)";
        }
        return base;
    }

    /** True if the given stack satisfies this task's filter. */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (tag != null) {
            if (!stack.is(TagKey.create(Registries.ITEM, tag))) return false;
        } else {
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item)) return false;
        }
        if (nbt != null) {
            CompoundTag actual = stack.getTag();
            if (actual == null) return false;
            if (!NbtUtils.compareNbt(nbt, actual, true)) return false;
        }
        return true;
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        if (tag != null) out.addProperty("tag", tag.toString());
        else out.addProperty("item", item.toString());
        out.addProperty("count", count);
        if (consume) out.addProperty("consume", true);
        if (nbt != null) out.addProperty("nbt", nbt.toString());
    }

    public static ItemTask fromJson(JsonObject body) {
        ResourceLocation item = body.has("item") ? new ResourceLocation(body.get("item").getAsString()) : null;
        ResourceLocation tag  = body.has("tag")  ? new ResourceLocation(body.get("tag").getAsString())  : null;
        if (item == null && tag == null) {
            throw new IllegalArgumentException("ItemTask requires either 'item' or 'tag'");
        }
        CompoundTag nbt = null;
        if (body.has("nbt")) {
            try { nbt = TagParser.parseTag(body.get("nbt").getAsString()); }
            catch (Exception e) { throw new IllegalArgumentException("ItemTask nbt is not valid SNBT: " + e.getMessage()); }
        }
        return new ItemTask(
                item, tag, nbt,
                body.has("count") ? body.get("count").getAsInt() : 1,
                body.has("consume") && body.get("consume").getAsBoolean());
    }

    /** Pull a {@code display.Name} text out of a filter compound for pretty
     * task descriptions. Returns null if the filter doesn't constrain a name. */
    private static String readDisplayName(CompoundTag filter) {
        if (!filter.contains("display", 10)) return null;
        CompoundTag display = filter.getCompound("display");
        if (!display.contains("Name", 8)) return null;
        String raw = display.getString("Name");
        try {
            var comp = net.minecraft.network.chat.Component.Serializer.fromJson(raw);
            if (comp != null) return comp.getString();
        } catch (Exception ignored) {}
        return raw;
    }
}
