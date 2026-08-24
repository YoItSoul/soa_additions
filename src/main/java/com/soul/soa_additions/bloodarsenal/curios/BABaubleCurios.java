package com.soul.soa_additions.bloodarsenal.curios;

import com.soul.soa_additions.bloodarsenal.item.bauble.SacrificeAmuletItem;
import com.soul.soa_additions.bloodarsenal.item.bauble.SelfSacrificeAmuletItem;
import com.soul.soa_additions.bloodarsenal.item.bauble.SoulPendantItem;
import com.soul.soa_additions.bloodarsenal.item.bauble.VampireRingItem;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curios behaviour for the Blood Arsenal baubles, kept in a class that only loads when Curios does.
 *
 * <p>The four bauble items used to {@code implement ICurio} directly. That linked the interface into
 * the item classes themselves, and {@code BAItems}' constructor method references resolve at class
 * init — so a Blood Magic install without Curios (a valid combination: BM requires only forge,
 * minecraft and patchouli) died with NoClassDefFoundError before the registry event even ran. The
 * items are now plain {@link Item}s and the capability is attached from here, which
 * {@link com.soul.soa_additions.bloodarsenal.BloodArsenalPlugin} touches only behind a
 * {@code ModList.isLoaded("curios")} check — the same shape
 * {@code com.soul.soa_additions.curios.CuriosIntegration} uses for the Greedy Bag.</p>
 */
public final class BABaubleCurios {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("soa_additions", "ba_bauble");

    private BABaubleCurios() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BABaubleCurios.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        Item item = event.getObject().getItem();
        if (item instanceof VampireRingItem
                || item instanceof SacrificeAmuletItem
                || item instanceof SelfSacrificeAmuletItem
                || item instanceof SoulPendantItem) {
            event.addCapability(ID, new Provider(event.getObject()));
        }
    }

    /** Right-click-to-equip, and a {@code getStack} that answers with the real stack. */
    private static final class Provider implements ICapabilityProvider {

        private final LazyOptional<ICurio> curio;

        Provider(ItemStack stack) {
            this.curio = LazyOptional.of(() -> new ICurio() {
                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public boolean canEquipFromUse(SlotContext ctx) {
                    return true;
                }
            });
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap,
                                                          @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, curio);
        }
    }
}
