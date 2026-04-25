package com.soul.soa_additions.tconstructevo.integration.greedy;

import javax.annotation.Nullable;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Warm — −5% damage in snowy biomes (-7.5% if also raining). */
public class WarmModifier extends Modifier implements ModifyDamageModifierHook {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry mod, EquipmentContext ctx,
                                   EquipmentSlot slot, DamageSource src, float dmg, boolean isDirect) {
        var ent = ctx.getEntity();
        var biome = ent.level().getBiome(ent.blockPosition());
        if (!biome.is(BiomeTags.IS_FOREST) && !biome.containsTag(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.BIOME,
                new net.minecraft.resources.ResourceLocation("forge", "is_snowy")))) return dmg;
        float reduction = 0.05F;
        if (ent.level().isRaining()) reduction += 0.025F;
        return dmg * (1.0F - reduction);
    }
}
