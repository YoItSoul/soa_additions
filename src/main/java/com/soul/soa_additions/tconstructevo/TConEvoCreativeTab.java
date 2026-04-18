package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab that collects every TConstructEvo item — materials, artifacts,
 * sceptre, food items. Icon will swap to the Coalescence Matrix once it's
 * registered; for now we fall back to a placeholder vanilla item so the scaffold
 * compiles regardless of which content has been ported yet.
 */
public final class TConEvoCreativeTab {

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                    new ResourceLocation(SoaAdditions.MODID, "tconstructevo"));

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoaAdditions.MODID);

    public static final RegistryObject<CreativeModeTab> TCONEVO_TAB = TABS.register("tconstructevo",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soa_additions.tconstructevo"))
                    .icon(() -> {
                        // Prefer the Coalescence Matrix once materials are ported; fall
                        // back to a Netherite Ingot so the tab has an icon even if no
                        // material-item has been registered yet.
                        var matrix = com.soul.soa_additions.tconstructevo.item.TCEMiscItems.COALESCENCE_MATRIX;
                        if (matrix != null && matrix.isPresent()) {
                            return new ItemStack(matrix.get());
                        }
                        return new ItemStack(Items.NETHERITE_INGOT);
                    })
                    .displayItems((params, output) -> {
                        for (RegistryObject<? extends net.minecraft.world.item.Item> item : TConEvoItems.all()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    private TConEvoCreativeTab() {}

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
