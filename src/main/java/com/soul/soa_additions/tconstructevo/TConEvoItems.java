package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every Item added by TConstructEvo (materials, artifacts, sceptre variants,
 * misc. food/trinket items) is registered through this DeferredRegister so
 * they end up in the {@code soa_additions:} namespace and participate in the
 * standard Forge registration lifecycle.
 *
 * <p>Sub-systems call into the helpers on this class during their own init —
 * {@link TConstructEvoPlugin#init} runs before the DeferredRegister fires
 * {@code RegisterEvent}, so all {@code register(...)} calls are safe.</p>
 */
public final class TConEvoItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SoaAdditions.MODID);

    // Registered items are tracked here so the creative tab can enumerate them.
    private static final List<RegistryObject<? extends Item>> ALL = new ArrayList<>();

    private TConEvoItems() {}

    public static <I extends Item> RegistryObject<I> register(String path, java.util.function.Supplier<I> factory) {
        RegistryObject<I> obj = ITEMS.register("tconevo/" + path, factory);
        ALL.add(obj);
        return obj;
    }

    static ResourceLocation id(String path) {
        return new ResourceLocation(SoaAdditions.MODID, "tconevo/" + path);
    }

    static List<RegistryObject<? extends Item>> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
