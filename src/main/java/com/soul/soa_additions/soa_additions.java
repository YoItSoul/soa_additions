package com.soul.soa_additions;


import com.soul.soa_additions.block.custom.ModBlocks;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.registry.SoaTiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(soa_additions.MODID)
public class soa_additions
{
    public static final String MODID = "soa_additions";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> SOA_TAB = CREATIVE_MODE_TABS.register("soa_additions_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soa_additions_tab"))
                    .icon(() -> ModItems.VOID_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        //Items
                        output.accept(ModItems.ABYSSAL_INGOT.get());
                        output.accept(ModItems.ETHER_INGOT.get());
                        output.accept(ModItems.INFERNIUM_INGOT.get());
                        output.accept(ModItems.VOID_INGOT.get());

                        // Blocks
                        output.accept(ModBlocks.ABYSSAL_ORE_BLOCK.get().asItem());
                        output.accept(ModBlocks.ETHER_ORE_BLOCK.get().asItem());
                        output.accept(ModBlocks.INFERNIUM_ORE_BLOCK.get().asItem());
                        output.accept(ModBlocks.VOID_ORE_BLOCK.get().asItem());
                    })
                    .build()

    );


    public soa_additions()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        SoaTiers.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }
}
