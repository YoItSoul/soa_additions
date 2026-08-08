package com.soul.soa_additions.oresight;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.potion.SoaBrewingPotions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Populates the "Ore Sight Potions" creative tab.
 *
 * <p>The three ore-sight items ({@code ore_sight_potion} and its splash /
 * lingering siblings) carry their tracked ore on NBT, so an empty stack of the
 * item is meaningless — nothing generic would be worth showing. Without an
 * explicit listing here they appear in no tab at all, which also hides them
 * from JEI's item panel (JEI builds its ingredient list from creative tab
 * contents), making brewed bottles look like they don't exist as items.</p>
 *
 * <p>One drinkable bottle is emitted per ore block for both the base and long
 * potion, plus the three master variants. Splash/lingering per-ore bottles are
 * deliberately not enumerated — they'd triple an already ore-count-sized list
 * and are brewed from splash/lingering mutagenic the same way.</p>
 */
public final class OreSightCreativeEntries {

    private OreSightCreativeEntries() {}

    /** Tab icon — the master bottle, the one variant with no ore to pick from. */
    public static ItemStack icon() {
        return bottle(ModItems.ORE_SIGHT_POTION.get(), SoaBrewingPotions.MASTER_ORE_SIGHT.get(), null);
    }

    public static void addTo(CreativeModeTab.Output output) {
        // Master variants — no block NBT, one per bottle type.
        Potion master = SoaBrewingPotions.MASTER_ORE_SIGHT.get();
        output.accept(bottle(ModItems.ORE_SIGHT_POTION.get(), master, null));
        output.accept(bottle(ModItems.ORE_SIGHT_SPLASH_POTION.get(), master, null));
        output.accept(bottle(ModItems.ORE_SIGHT_LINGERING_POTION.get(), master, null));

        Potion base = SoaBrewingPotions.ORE_SIGHT.get();
        Potion long_ = SoaBrewingPotions.LONG_ORE_SIGHT.get();
        Item drinkable = ModItems.ORE_SIGHT_POTION.get();

        for (ResourceLocation oreId : oreIds()) {
            output.accept(bottle(drinkable, base, oreId));
            output.accept(bottle(drinkable, long_, oreId));
        }
    }

    /** Every ore block's id, sorted so the tab order is stable across loads. */
    private static List<ResourceLocation> oreIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (!OreSight.isOreBlock(block)) continue;
            if (block.asItem() == net.minecraft.world.item.Items.AIR) continue;
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null) ids.add(id);
        }
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        return ids;
    }

    /** Build one bottle stack; {@code oreId} null means "no tracked block". */
    private static ItemStack bottle(Item item, Potion potion, ResourceLocation oreId) {
        ItemStack stack = PotionUtils.setPotion(new ItemStack(item), potion);
        if (oreId != null) {
            stack.getOrCreateTag().putString(OreSightBrewing.NBT_BLOCK, oreId.toString());
        }
        return stack;
    }
}
