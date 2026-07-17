package com.soul.soa_additions.bloodarsenal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Blood Arsenal sounds — 1.12 BloodArsenalSounds (just the crit striker cue). */
public final class BASounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "bloodarsenal");

    public static final RegistryObject<SoundEvent> CRIT = SOUNDS.register("crit",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath("bloodarsenal", "crit")));

    private BASounds() {}

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
