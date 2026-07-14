package com.soul.soa_additions.tinkersaether.modifier;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registry for the eleven custom traits ported from {@code tinkersaether-1.4.1}
 * (Skyrooted, Enlightened, Gilded, Antigrav, Launching, Reach, Cushy, Festive,
 * Refrigeration, Swetty, Zany). Mirrors the queue-and-drain pattern used by
 * {@link com.soul.soa_additions.taiga.modifier.TaigaModifiers} since TC3's
 * modifier registry isn't a Forge registry.
 *
 * <p>Modifier static fields are populated by {@link TinkersAetherTraitList}.
 * Static IDs are namespaced under {@code soa_additions:} so the material JSON
 * traits files reference {@code soa_additions:&lt;trait&gt;}.</p>
 */
public final class TinkersAetherModifiers {

    private static final List<Holder<?>> PENDING = new ArrayList<>();

    private TinkersAetherModifiers() {}

    public static <M extends Modifier> Holder<M> register(String name, Supplier<M> factory) {
        Holder<M> holder = new Holder<>(new ResourceLocation(SoaAdditions.MODID, name), factory);
        PENDING.add(holder);
        return holder;
    }

    public static void register(IEventBus modEventBus) {
        TinkersAetherTraitList.bootstrap();
        modEventBus.addListener(TinkersAetherModifiers::onModifierRegistration);
    }

    private static void onModifierRegistration(ModifierManager.ModifierRegistrationEvent event) {
        for (Holder<?> holder : PENDING) {
            holder.bindAndRegister(event);
        }
    }

    public static final class Holder<M extends Modifier> implements Supplier<M> {
        private final ResourceLocation id;
        private final Supplier<M> factory;
        private M instance;

        private Holder(ResourceLocation id, Supplier<M> factory) {
            this.id = id;
            this.factory = factory;
        }

        public ResourceLocation getId() { return id; }

        @Override public M get() {
            if (instance == null) {
                throw new IllegalStateException(
                        "Modifier " + id + " accessed before ModifierRegistrationEvent fired");
            }
            return instance;
        }

        @SuppressWarnings("unchecked")
        private void bindAndRegister(ModifierManager.ModifierRegistrationEvent event) {
            instance = factory.get();
            event.registerStatic(new ModifierId(id), instance);
        }
    }
}
