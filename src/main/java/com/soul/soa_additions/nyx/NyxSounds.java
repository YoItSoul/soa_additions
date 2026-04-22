package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NyxSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SoaAdditions.MODID);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SoaAdditions.MODID, name)));
    }

    public static final RegistryObject<SoundEvent> LUNAR_WATER           = reg("lunar_water");
    public static final RegistryObject<SoundEvent> FALLING_STAR          = reg("falling_star");
    public static final RegistryObject<SoundEvent> FALLING_STAR_IMPACT   = reg("falling_star_impact");
    public static final RegistryObject<SoundEvent> FALLING_METEOR        = reg("falling_meteor");
    public static final RegistryObject<SoundEvent> FALLING_METEOR_IMPACT = reg("falling_meteor_impact");
    public static final RegistryObject<SoundEvent> HAMMER_START          = reg("hammer_start");
    public static final RegistryObject<SoundEvent> HAMMER_END            = reg("hammer_end");

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private NyxSounds() {}
}
