package com.soul.soa_additions.tr;

import com.soul.soa_additions.tr.core.Aspects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class TrCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumicRemnants.MODID);

    public static final RegistryObject<CreativeModeTab> TR_TAB = TABS.register("tr",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tr.tr"))
                    // Aer rune as the tab icon — first aspect, primal, neutral pale-blue.
                    .icon(() -> new ItemStack(TrItems.ASPECT_RUNES.get("aer").get()))
                    .displayItems((params, output) -> {
                        // Aspects.all() iterates in the registration order from
                        // Aspects.java, which is tier-major (primals → tier 1 → … → tier 4).
                        // That gives a creative tab laid out by aspect lineage.
                        // Creative tab shows ALL runes regardless of discovery —
                        // creative is a curator/builder mode, not a survival progression view.
                        for (var aspect : Aspects.all()) {
                            output.accept(TrItems.ASPECT_RUNES.get(aspect.id().getPath()).get());
                        }
                        output.accept(TrBlocks.ASTRAL_WARD_ITEM.get());
                        output.accept(TrItems.ARCANE_MONOCLE.get());
                    })
                    .build());

    private TrCreativeTab() {}

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
