package com.soul.soa_additions.tconstructevo.item.artifact;

import com.mojang.serialization.Codec;
import com.soul.soa_additions.SoaAdditions;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Holder for the global-loot-modifier codec our {@link ArtifactLootModifier}
 * registers under. Wiring lives here so {@code TConstructEvoPlugin.init} can
 * call one method instead of touching the modifier class directly.
 */
public final class ArtifactLootSerializers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SoaAdditions.MODID);

    public static final RegistryObject<Codec<ArtifactLootModifier>> ARTIFACT =
            SERIALIZERS.register("tconevo_artifact", () -> ArtifactLootModifier.CODEC);

    private ArtifactLootSerializers() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
