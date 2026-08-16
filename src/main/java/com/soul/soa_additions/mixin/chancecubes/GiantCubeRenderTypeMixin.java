package com.soul.soa_additions.mixin.chancecubes;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.soul.soa_additions.client.render.EntityFormatQuadConsumer;
import com.soul.soa_additions.client.render.ShaderState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the Giant Chance Cube visible under shaders.
 *
 * <p>The 3x3x3 has no block model — {@code giant_chance_cube.json} declares only a
 * particle texture — so every face the player sees is drawn by this block entity
 * renderer, which asks for {@code RenderType.text}. On Forge that is {@code forge_text}:
 * vertex format {@code POSITION_COLOR_TEX_LIGHTMAP}, no normal. Oculus routes the text
 * shader to {@code ShaderKey.TEXT_BE} while block entities render, so the quads reach the
 * shaderpack's block-entity program, which reads a normal attribute that isn't there. GL
 * hands it (0,0,0), the pack normalizes it into NaN, and the cube writes nothing —
 * silently, with no log line. Vanilla's text shader never looks at normals, which is why
 * the cube is fine with shaders off.</p>
 *
 * <p>So with a pack active we re-route the same geometry through an entity render type
 * and fill in the missing attributes (see {@link EntityFormatQuadConsumer}). With shaders
 * off the render type is left exactly as the mod asked for it.</p>
 *
 * <p>The buffer itself is unwrapped in both cases, which fixes a second bug that the
 * invisibility was hiding. {@code Material#buffer} hands back a sprite-wrapped consumer
 * that maps 0..1 coordinates into the sprite's slice of the atlas — but the caller feeds
 * it {@code sprite.getU0()}/{@code getU1()}, which are already atlas-absolute. Mapping
 * those a second time collapses each face's texture to a sub-pixel sliver stretched over
 * all nine blocks, so every side renders as one flat colour. Taking the buffer straight
 * from the source leaves the caller's coordinates alone and the face art comes out.</p>
 *
 * <p>Written against SRG member names with {@code remap = false}, matching the rest of
 * this package: the target is a third-party class and the mixin config carries no refmap.
 * {@code require = 1} is deliberate — a silent no-op here is indistinguishable from the
 * bug it fixes, so a Chance Cubes update that moves this call should fail loudly.</p>
 */
@Pseudo
@Mixin(targets = "chanceCubes.renderer.TileGiantCubeRenderer", remap = false)
public abstract class GiantCubeRenderTypeMixin {

    @Redirect(
            method = "render(LchanceCubes/tileentities/TileGiantCube;FLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/Material;m_119194_("
                            + "Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)"
                            + "Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            require = 1, remap = false)
    private VertexConsumer soa$shaderSafeBuffer(Material material, MultiBufferSource source,
                                                Function<ResourceLocation, RenderType> renderType) {
        boolean shaders = ShaderState.shadersActive();
        ResourceLocation atlas = material.atlasLocation();
        VertexConsumer buffer = source.getBuffer(
                shaders ? RenderType.entityCutoutNoCull(atlas) : renderType.apply(atlas));

        return shaders ? new EntityFormatQuadConsumer(buffer) : buffer;
    }
}
