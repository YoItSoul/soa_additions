package com.soul.soa_additions.bloodarsenal.item.tool;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

/**
 * Utility class providing auto-repair logic for blood-infused tools.
 * The original 1.12 version was an abstract base class extending ItemTool;
 * in 1.20.1 we use a static helper since swords/axes/pickaxes/shovels
 * extend different vanilla base classes.
 */
public final class BloodInfusedToolItem {

    private BloodInfusedToolItem() {}

    /**
     * Called from inventoryTick() of each blood-infused tool.
     * Repairs 1 durability point every {@code repairRate} ticks,
     * costing LP from the player's soul network.
     * Cost scales with enchantment count: {@code baseCost * (1 + totalEnchantLevels)}.
     */
    public static void handleAutoRepair(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide() || stack.getDamageValue() <= 0) return;
        if (!(entity instanceof Player player)) return;

        int repairRate = BAConfig.TOOL_REPAIR_RATE.get();
        if (level.getGameTime() % repairRate != 0) return;
        if (level.random.nextBoolean()) return; // 50% chance per tick like original

        int baseCost = BAConfig.TOOL_REPAIR_COST.get();
        int cost = baseCost;

        // Scale cost by enchantment levels
        ListTag enchants = stack.getEnchantmentTags();
        if (enchants != null && !enchants.isEmpty()) {
            int totalLevels = 0;
            for (int i = 0; i < enchants.size(); i++) {
                CompoundTag tag = enchants.getCompound(i);
                totalLevels += tag.getShort("lvl");
            }
            cost = baseCost * (1 + totalLevels);
        }

        NetworkHelper.getSoulNetwork(player).syphonAndDamage(player,
                SoulTicket.item(stack, level, player, cost));
        stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
