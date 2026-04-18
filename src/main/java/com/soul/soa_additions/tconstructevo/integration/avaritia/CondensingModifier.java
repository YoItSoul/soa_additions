package com.soul.soa_additions.tconstructevo.integration.avaritia;

import com.soul.soa_additions.tconstructevo.TConEvoConfig;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * Condensing — Neutronium-material trait. RNG-drops an Avaritia neutron pile
 * per effective block break and per kill. The 1.12.2 original wrapped the
 * item through a dedicated hooks interface; on 1.20.1 we just look the item
 * up by its registry name ({@code avaritia:neutron_pile}). If Avaritia isn't
 * running a compatible item id, the trait becomes a silent no-op rather than
 * crashing the tool.
 */
public class CondensingModifier extends Modifier implements BlockBreakModifierHook, MeleeHitModifierHook {

    private static final ResourceLocation NEUTRON_PILE_ID = new ResourceLocation("avaritia", "neutron_pile");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (!context.isEffective()) return;
        BlockPos pos = context.getPos();
        tryDropNeutron(context.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, modifier.getLevel());
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null || target.isAlive() || target.level().isClientSide()) return;
        tryDropNeutron((ServerLevel) target.level(), target.getX(), target.getY(), target.getZ(), modifier.getLevel());
    }

    private static void tryDropNeutron(ServerLevel world, double x, double y, double z, int level) {
        if (world == null) return;
        double chance = TConEvoConfig.CONDENSING_DROP_PROBABILITY.get() * level;
        if (chance <= 0 || ThreadLocalRandom.current().nextDouble() >= chance) return;
        Item item = BuiltInRegistries.ITEM.get(NEUTRON_PILE_ID);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return;
        ItemEntity drop = new ItemEntity(world, x, y, z, new ItemStack(item));
        drop.setDefaultPickUpDelay();
        world.addFreshEntity(drop);
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
