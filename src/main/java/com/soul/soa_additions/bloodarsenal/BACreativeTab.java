package com.soul.soa_additions.bloodarsenal;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative mode tab for Blood Arsenal content.
 */
public final class BACreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoaAdditions.MODID);

    public static final RegistryObject<CreativeModeTab> BA_TAB = TABS.register("blood_arsenal",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soa_additions.blood_arsenal"))
                    .icon(() -> new ItemStack(BAItems.BLOOD_INFUSED_IRON_INGOT.get()))
                    .displayItems((params, output) -> {
                        // Materials
                        output.accept(BAItems.GLASS_SHARD.get());
                        output.accept(BAItems.BLOOD_INFUSED_STICK.get());
                        output.accept(BAItems.BLOOD_INFUSED_GLOWSTONE_DUST.get());
                        output.accept(BAItems.INERT_BLOOD_INFUSED_IRON_INGOT.get());
                        output.accept(BAItems.BLOOD_INFUSED_IRON_INGOT.get());
                        output.accept(BAItems.BLOOD_BURNED_STRING.get());

                        // Food
                        output.accept(BAItems.BLOOD_ORANGE.get());

                        // Reagents
                        output.accept(BAItems.REAGENT_SWIMMING.get());
                        output.accept(BAItems.REAGENT_ENDER.get());
                        output.accept(BAItems.REAGENT_LIGHTNING.get());
                        output.accept(BAItems.REAGENT_DIVINITY.get());

                        // Gems
                        output.accept(BAItems.GEM_SELF_SACRIFICE.get());
                        output.accept(BAItems.GEM_SACRIFICE.get());
                        output.accept(BAItems.GEM_TARTARIC.get());

                        // Blood Diamonds
                        output.accept(BAItems.BLOOD_DIAMOND.get());
                        output.accept(BAItems.BLOOD_DIAMOND_INERT.get());
                        output.accept(BAItems.BLOOD_DIAMOND_INFUSED.get());
                        output.accept(BAItems.BLOOD_DIAMOND_BOUND.get());

                        // Modifier Tome (base item — variants shown via modifier system)
                        output.accept(BAItems.MODIFIER_TOME.get());

                        // Stasis Tools
                        output.accept(BAItems.STASIS_SWORD.get());
                        output.accept(BAItems.STASIS_PICKAXE.get());
                        output.accept(BAItems.STASIS_AXE.get());
                        output.accept(BAItems.STASIS_SHOVEL.get());

                        // Blood-Infused Wooden Tools
                        output.accept(BAItems.BLOOD_INFUSED_WOODEN_SWORD.get());
                        output.accept(BAItems.BLOOD_INFUSED_WOODEN_AXE.get());
                        output.accept(BAItems.BLOOD_INFUSED_WOODEN_PICKAXE.get());
                        output.accept(BAItems.BLOOD_INFUSED_WOODEN_SHOVEL.get());
                        output.accept(BAItems.BLOOD_INFUSED_WOODEN_SICKLE.get());

                        // Blood-Infused Iron Tools
                        output.accept(BAItems.BLOOD_INFUSED_IRON_SWORD.get());
                        output.accept(BAItems.BLOOD_INFUSED_IRON_AXE.get());
                        output.accept(BAItems.BLOOD_INFUSED_IRON_PICKAXE.get());
                        output.accept(BAItems.BLOOD_INFUSED_IRON_SHOVEL.get());
                        output.accept(BAItems.BLOOD_INFUSED_IRON_SICKLE.get());

                        // Special Weapons
                        output.accept(BAItems.WARP_BLADE.get());
                        output.accept(BAItems.STYGIAN_DAGGER.get());
                        output.accept(BAItems.GLASS_SACRIFICIAL_DAGGER.get());
                        output.accept(BAItems.GLASS_DAGGER_OF_SACRIFICE.get());

                        // Sigils
                        output.accept(BAItems.SIGIL_LIGHTNING.get());
                        output.accept(BAItems.SIGIL_SWIMMING.get());
                        output.accept(BAItems.SIGIL_ENDER.get());
                        output.accept(BAItems.SIGIL_DIVINITY.get());
                        output.accept(BAItems.SIGIL_SENTIENCE.get());
                        output.accept(BAItems.SIGIL_AUGMENTED_HOLDING.get());

                        // Baubles
                        output.accept(BAItems.VAMPIRE_RING.get());
                        output.accept(BAItems.SACRIFICE_AMULET.get());
                        output.accept(BAItems.SELF_SACRIFICE_AMULET.get());
                        output.accept(BAItems.SOUL_PENDANT_PETTY.get());
                        output.accept(BAItems.SOUL_PENDANT_LESSER.get());
                        output.accept(BAItems.SOUL_PENDANT_COMMON.get());
                        output.accept(BAItems.SOUL_PENDANT_GREATER.get());
                        output.accept(BAItems.SOUL_PENDANT_GRAND.get());

                        // Bound Tools
                        output.accept(BAItems.BOUND_STICK.get());
                        output.accept(BAItems.BOUND_IGNITER.get());
                        output.accept(BAItems.BOUND_SHEARS.get());

                        // Blocks — Wood family
                        output.accept(BABlocks.BLOOD_INFUSED_PLANKS.get());
                        output.accept(BABlocks.BLOOD_INFUSED_LOG.get());
                        output.accept(BABlocks.BLOOD_INFUSED_STAIRS.get());
                        output.accept(BABlocks.BLOOD_INFUSED_SLAB.get());
                        output.accept(BABlocks.BLOOD_INFUSED_FENCE.get());
                        output.accept(BABlocks.BLOOD_INFUSED_FENCE_GATE.get());

                        // Blocks — Metal / special
                        output.accept(BABlocks.BLOOD_INFUSED_IRON_BLOCK.get());
                        output.accept(BABlocks.BLOOD_INFUSED_GLOWSTONE.get());
                        output.accept(BABlocks.BLOOD_TORCH.get());

                        // Blocks — Glass
                        output.accept(BABlocks.BLOOD_STAINED_GLASS.get());
                        output.accept(BABlocks.BLOOD_STAINED_GLASS_PANE.get());

                        // Blocks — Decorative
                        output.accept(BABlocks.SLATE.get());
                        output.accept(BABlocks.GLASS_SHARDS.get());

                        // Blocks — Functional
                        output.accept(BABlocks.STASIS_PLATE.get());
                        output.accept(BABlocks.ALTARE.get());
                        output.accept(BABlocks.BLOOD_CAPACITOR.get());
                    })
                    .build());

    private BACreativeTab() {}

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
