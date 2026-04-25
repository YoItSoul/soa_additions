package com.soul.soa_additions.tr.compat.curios;

import com.soul.soa_additions.tr.TrItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curios soft-dep glue — attaches an {@link ICurio} capability to the monocle
 * ItemStack so it can be slotted into the player's curios inventory. Without
 * this the monocle would still work from the regular inventory (the tooltip
 * / world-scan paths check both), but wouldn't be wearable.
 *
 * <p>This class hard-references {@code top.theillusivec4.curios.api}, so it
 * MUST only be class-loaded when Curios is present. The bootstrap call lives
 * in {@link com.soul.soa_additions.curios.CuriosIntegration}, which is itself
 * only invoked when {@code ModList.isLoaded("curios")} returns true.
 */
public final class MonocleCuriosCap {

    private MonocleCuriosCap() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(MonocleCuriosCap.class);
    }

    /** Default ICurio with no special slot behaviour — appearance is just the
     *  in-inventory texture; it's a "carried, not displayed" curio. Slot tag
     *  binding (head) lives in {@code data/curios/tags/items/head.json}. */
    private static final ICurio CURIO = new ICurio() {
        @Override
        public ItemStack getStack() { return ItemStack.EMPTY; }

        @Override
        public boolean canEquipFromUse(SlotContext context) { return true; }
    };

    @SubscribeEvent
    public static void onAttachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty() || stack.getItem() != TrItems.ARCANE_MONOCLE.get()) return;
        ICapabilityProvider provider = new ICapabilityProvider() {
            private final LazyOptional<ICurio> opt = LazyOptional.of(() -> CURIO);
            @Override
            public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                                     net.minecraft.core.Direction side) {
                return CuriosCapability.ITEM.orEmpty(cap, opt);
            }
        };
        event.addCapability(new net.minecraft.resources.ResourceLocation(
                "tr", "curio_monocle"), provider);
    }
}
