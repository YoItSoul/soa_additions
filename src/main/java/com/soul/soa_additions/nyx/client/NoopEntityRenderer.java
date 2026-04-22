package com.soul.soa_additions.nyx.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Empty renderer for invisible Nyx entities (falling_star, falling_meteor,
 * cauldron_tracker). The 1.12 Nyx entities are particle/event-driven and
 * have no model; the visible falling-star streak is produced by spawn-side
 * particle emission, not by a model. Without *some* registered renderer,
 * Oculus's shadow pass NPEs when iterating entities with null renderers.
 */
@OnlyIn(Dist.CLIENT)
public class NoopEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final ResourceLocation EMPTY =
            new ResourceLocation("minecraft", "textures/misc/white.png");

    public NoopEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
        this.shadowStrength = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return EMPTY;
    }

    @Override
    public void render(T entity, float yaw, float pt, PoseStack pose,
                       MultiBufferSource buffer, int light) {
        // intentionally nothing
    }
}
