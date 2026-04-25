package com.soul.soa_additions.curios;

import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.item.custom.GreedyBagEvents;
import com.soul.soa_additions.item.custom.GreedyBagItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

/**
 * Curios soft-dependency bootstrap. GreedyBagItem implements {@link
 * top.theillusivec4.curios.api.type.capability.ICurio}, so anything that
 * class-loads GreedyBagItem when Curios is absent will crash with
 * NoClassDefFoundError. Keeping the RegistryObject field and the event-bus
 * wiring behind this class means the JVM only links GreedyBagItem when
 * {@link #init(IEventBus)} actually runs — which SoaAdditions guards with a
 * {@code ModList.isLoaded("curios")} check.
 */
public final class CuriosIntegration {

    public static final RegistryObject<Item> GREEDY_BAG = ModItems.ITEMS.register(
            "greedy_bag",
            () -> new GreedyBagItem(new Item.Properties().rarity(Rarity.EPIC)));

    private CuriosIntegration() {}

    public static void init(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(GreedyBagEvents.class);
        // Bind the Arcane Monocle into curios slots when curios is present.
        // The class hard-references the curios API so it can only be touched
        // from within this curios-gated init path.
        com.soul.soa_additions.tr.compat.curios.MonocleCuriosCap.register();
    }
}
