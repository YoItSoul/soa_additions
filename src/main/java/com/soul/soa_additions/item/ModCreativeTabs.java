package com.soul.soa_additions.item;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoaAdditions.MODID);

    public static final RegistryObject<CreativeModeTab> SOA_TAB = CREATIVE_MODE_TABS.register("soa_additions_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soa_additions_tab"))
                    .icon(() -> ModItems.VOID_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Ingots
                        output.accept(ModItems.ABYSSAL_INGOT.get());
                        output.accept(ModItems.ETHER_INGOT.get());
                        output.accept(ModItems.INFERNIUM_INGOT.get());
                        output.accept(ModItems.VOID_INGOT.get());

                        // Misc
                        output.accept(ModItems.CHEATER_COIN.get());
                        output.accept(ModItems.QUEST_BOOK.get());

                        // Ores
                        output.accept(ModBlocks.ABYSSAL_ORE_BLOCK.get());
                        output.accept(ModBlocks.ETHER_ORE_BLOCK.get());
                        output.accept(ModBlocks.INFERNIUM_ORE_BLOCK.get());
                        output.accept(ModBlocks.VOID_ORE_BLOCK.get());

                        // Grove
                        output.accept(ModBlocks.GROVE_SPAWN_BLOCK.get());
                        output.accept(ModBlocks.GROVE_BOON_BLOCK.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
