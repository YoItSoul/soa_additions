package com.soul.soa_additions.tconstructevo;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom entity attributes owned by the TConstructEvo subsystem. 1.12.2
 * tconevo used these attributes (accuracy, evasion, healing received, damage
 * dealt/taken, flight speed) as a common substrate that its traits and
 * potions both wrote to and read from. We port them incrementally as each
 * consumer lands.
 *
 * <p>Currently only {@link #ACCURACY} is registered — it is populated by
 * {@code AccuracyModifier} and consumed by the TrueStrike potion (×1.5 melee
 * damage placeholder in {@code TConEvoEventHandler} until an evasion side
 * exists).</p>
 */
public final class TConEvoAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "tconevo");

    public static final RegistryObject<Attribute> ACCURACY = ATTRIBUTES.register(
            "accuracy",
            () -> new RangedAttribute(
                    "attribute.name.soa_additions.tconevo.accuracy",
                    1.0D,
                    1.0D,
                    1.0D + TConEvoConfig.ACCURACY_ATTR_MAX.get()
            ).setSyncable(true));

    private TConEvoAttributes() {}

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.register(Handler.class);
    }

    public static final class Handler {
        private Handler() {}

        @SubscribeEvent
        public static void onAttach(EntityAttributeModificationEvent event) {
            Attribute attr = ACCURACY.get();
            for (EntityType<? extends LivingEntity> type : event.getTypes()) {
                if (!event.has(type, attr)) {
                    event.add(type, attr);
                }
            }
        }
    }
}
