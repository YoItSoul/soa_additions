package com.soul.soa_additions.tr;

import com.soul.soa_additions.tr.core.Aspect;
import com.soul.soa_additions.tr.core.Aspects;
import com.soul.soa_additions.tr.item.ArcaneMonocleItem;
import com.soul.soa_additions.tr.item.AspectRuneItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thaumic Remnants item registrations. Currently: one {@link AspectRuneItem}
 * per registered aspect (48 items), keyed by aspect id path.
 *
 * <p>All items live in the {@code tr} namespace via the dedicated
 * DeferredRegister — same pattern as Taiga. The runes appear as e.g.
 * {@code tr:aspect_rune_aer}.
 */
public final class TrItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ThaumicRemnants.MODID);

    /** path of aspect → registered rune item. Iteration order matches
     *  {@link Aspects#all()} so the creative tab lays out by tier. */
    public static final Map<String, RegistryObject<AspectRuneItem>> ASPECT_RUNES = new LinkedHashMap<>();

    public static final RegistryObject<ArcaneMonocleItem> ARCANE_MONOCLE = ITEMS.register(
            "arcane_monocle",
            () -> new ArcaneMonocleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    static {
        // Aspects.bootstrap() must have populated the aspect map before we
        // build runes; calling it explicitly keeps the dependency obvious.
        Aspects.bootstrap();
        for (Aspect aspect : Aspects.all()) {
            String path = aspect.id().getPath();
            ASPECT_RUNES.put(path, ITEMS.register(
                    "aspect_rune_" + path,
                    () -> new AspectRuneItem(aspect, new Item.Properties().stacksTo(64))));
        }
    }

    private TrItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
