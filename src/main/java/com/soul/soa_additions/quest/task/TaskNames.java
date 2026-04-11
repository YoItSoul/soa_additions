package com.soul.soa_additions.quest.task;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;

/**
 * Small helpers that turn raw registry ids like {@code minecraft:stone_pickaxe}
 * into their proper localized display names ({@code Stone Pickaxe}) for quest
 * task descriptions. Falls back to the raw id when resolution fails so the
 * description never goes blank.
 */
public final class TaskNames {
    private TaskNames() {}

    public static String item(String id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            if (item != null) {
                String s = item.getDescription().getString();
                if (!s.isEmpty()) return s;
            }
        } catch (Exception ignored) {}
        return id;
    }

    public static String block(String id) {
        try {
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
            if (block != null) {
                String s = block.getName().getString();
                if (!s.isEmpty()) return s;
            }
        } catch (Exception ignored) {}
        return id;
    }

    public static String entity(String id) {
        try {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(id));
            if (type != null) {
                String s = type.getDescription().getString();
                if (!s.isEmpty()) return s;
            }
        } catch (Exception ignored) {}
        return id;
    }

    public static String dimension(String id) {
        // Dimensions rarely have a real lang entry; prettify the path.
        try {
            ResourceLocation rl = new ResourceLocation(id);
            return prettify(rl.getPath());
        } catch (Exception e) {
            return id;
        }
    }

    public static String advancement(String id) {
        try {
            ResourceLocation rl = new ResourceLocation(id);
            int slash = rl.getPath().lastIndexOf('/');
            String tail = slash >= 0 ? rl.getPath().substring(slash + 1) : rl.getPath();
            return prettify(tail);
        } catch (Exception e) {
            return id;
        }
    }

    /** "forge:logs" → "Logs (forge)". Keeps the namespace visible since tags
     * can collide on path alone across mods. */
    public static String prettyTag(String id) {
        try {
            ResourceLocation rl = new ResourceLocation(id);
            return prettify(rl.getPath()) + " (" + rl.getNamespace() + ")";
        } catch (Exception e) {
            return id;
        }
    }

    private static String prettify(String s) {
        String[] parts = s.replace('/', ' ').replace('_', ' ').split(" ");
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (b.length() > 0) b.append(' ');
            b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return b.toString();
    }
}
