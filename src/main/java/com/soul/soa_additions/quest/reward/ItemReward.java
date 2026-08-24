package com.soul.soa_additions.quest.reward;

import com.google.gson.JsonObject;
import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.quest.model.QuestReward;
import com.soul.soa_additions.quest.model.RewardScope;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Hand the player {@code count} of an item, with optional NBT.
 *
 * <p>Optional {@code nbt} is a CompoundTag string (SNBT), mirroring
 * {@link com.soul.soa_additions.quest.task.ItemTask}'s field of the same name.
 * Unlike the task side — where the compound is a <em>subset filter</em> — here
 * it is the literal tag applied to the granted stack, so it must be complete.
 * This is what lets named/enchanted/composed gear be a real item reward instead
 * of a {@link CommandReward} wrapping {@code /give}, which renders as raw
 * command text in the quest book.</p>
 *
 * <p>Example — an enchanted pickaxe:
 * {@code { "type":"item", "item":"minecraft:diamond_pickaxe", "count":1,
 *          "nbt":"{Enchantments:[{id:\"minecraft:efficiency\",lvl:5s}]}" }}</p>
 */
public record ItemReward(ResourceLocation item, int count, CompoundTag nbt, RewardScope scope) implements QuestReward {

    public static final ResourceLocation TYPE = new ResourceLocation(SoaAdditions.MODID, "item");

    @Override public ResourceLocation type() { return TYPE; }

    @Override public String describe() {
        return "Receive " + count + "x " + displayName()
                + (scope == RewardScope.TEAM ? " (team)" : "");
    }

    /** Hover name of the stack this reward grants, so NBT-renamed gear reads as
     *  "Fairy Hat" rather than "Leather Cap" in the book and in describe(). */
    private String displayName() {
        ItemStack stack = toStack();
        if (!stack.isEmpty()) return stack.getHoverName().getString();
        return com.soul.soa_additions.quest.task.TaskNames.item(item.toString());
    }

    /**
     * The stack this reward hands out — also used by the quest book and the JEI
     * reward category so the icon matches what actually lands in the inventory.
     * Returns an empty stack for an unknown item id.
     */
    public ItemStack toStack() {
        Item it = ForgeRegistries.ITEMS.getValue(item);
        if (it == null || it == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(it, count);
        if (nbt != null) stack.setTag(nbt.copy());
        return stack;
    }

    @Override public void grant(ServerPlayer player) {
        ItemStack stack = toStack();
        if (stack.isEmpty()) return;
        com.soul.soa_additions.util.ItemDelivery.give(player, stack);
    }

    @Override public void writeJson(JsonObject out) {
        out.addProperty("type", TYPE.toString());
        out.addProperty("item", item.toString());
        out.addProperty("count", count);
        if (nbt != null) out.addProperty("nbt", nbt.toString());
        if (scope != RewardScope.PLAYER) out.addProperty("scope", scope.lower());
    }

    public static ItemReward fromJson(JsonObject body) {
        CompoundTag nbt = null;
        if (body.has("nbt")) {
            try { nbt = TagParser.parseTag(body.get("nbt").getAsString()); }
            catch (Exception e) { throw new IllegalArgumentException("ItemReward nbt is not valid SNBT: " + e.getMessage()); }
        }
        return new ItemReward(
                new ResourceLocation(body.get("item").getAsString()),
                body.has("count") ? body.get("count").getAsInt() : 1,
                nbt,
                body.has("scope") ? RewardScope.fromString(body.get("scope").getAsString()) : RewardScope.PLAYER);
    }
}
