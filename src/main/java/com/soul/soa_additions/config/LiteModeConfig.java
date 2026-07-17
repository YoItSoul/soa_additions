package com.soul.soa_additions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class LiteModeConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue RENDER_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue REDUCE_RENDER_DISTANCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("liteMode");
        ENABLED = b
                .comment(
                        "Master toggle for Lite Mode. When true, the mod applies maximum-performance",
                        "client settings on every launch: clouds off, particles minimal, dynamic lights off,",
                        "emissive textures off, Botania shaders off, etc. Individual vanilla and mod configs",
                        "are overwritten each startup while this is true — disable to regain manual control."
                )
                .define("enabled", false);
        RENDER_DISTANCE = b
                .comment("Render distance override for lite mode (chunks). Default 5.")
                .defineInRange("renderDistance", 5, 2, 16);
        REDUCE_RENDER_DISTANCE = b
                .comment("If true, lite mode also lowers render distance to the value above.")
                .define("reduceRenderDistance", true);
        b.pop();
        SPEC = b.build();
    }

    private LiteModeConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "soa_additions-litemode.toml");
    }
}
