package com.soul.soa_additions.sound;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** soa_additions-namespace sound events (nyx sounds live in {@code NyxSounds}). */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SoaAdditions.MODID);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SoaAdditions.MODID, name)));
    }

    // Music by Mangonade — shipped as jukebox discs.
    public static final RegistryObject<SoundEvent> MUSIC_DISC_STENCIL = reg("music_disc.stencil");
    public static final RegistryObject<SoundEvent> MUSIC_DISC_SUNRISE = reg("music_disc.sunrise");

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private ModSounds() {}
}
