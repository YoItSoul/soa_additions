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
import net.minecraft.world.item.ItemStack;

/**
 * Craft {@code count} of an item (or any item in a tag), with an optional NBT
 * subset filter. Lifetime counter driven by {@code PlayerEvent.ItemCraftedEvent}.
 *
 * <p>Exactly one of {@code item} or {@code tag} is set. Optional {@code nbt}
 * must be a subset of the crafted stack's NBT for the craft to count.</p>
 */
public record CraftTask(ResourceLocation item, ResourceLocation tag, CompoundTag nbt, int count) implements QuestTask {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "craft");

    @Override public ResourceLocation type() { return TYPE; }
    @Override public int target() { return count; }

    @Override public String describe() {
        String base = tag != null
                ? "Craft " + count + "x any " + TaskNames.prettyTag(tag.toString())
                : "Craft " + count + "x " + TaskNames.item(item.toString());
        if (nbt != null) base += " (with NBT)";
        return base;
    }

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
        if (nbt != null) out.addProperty("nbt", nbt.toString());
    }

    public static CraftTask fromJson(JsonObject body) {
        ResourceLocation item = body.has("item") ? new ResourceLocation(body.get("item").getAsString()) : null;
        ResourceLocation tag  = body.has("tag")  ? new ResourceLocation(body.get("tag").getAsString())  : null;
        if (item == null && tag == null) {
            throw new IllegalArgumentException("CraftTask requires either 'item' or 'tag'");
        }
        CompoundTag nbt = null;
        if (body.has("nbt")) {
            try { nbt = TagParser.parseTag(body.get("nbt").getAsString()); }
            catch (Exception e) { throw new IllegalArgumentException("CraftTask nbt is not valid SNBT: " + e.getMessage()); }
        }
        return new CraftTask(
                item, tag, nbt,
                body.has("count") ? body.get("count").getAsInt() : 1);
    }
}
