package com.soul.soa_additions.cyclicaqua;

import com.soul.soa_additions.config.CyclicFisherConfig;
import com.soul.soa_additions.nyx.event.HarvestMoonLuck;
import com.teammetallurgy.aquaculture.api.bait.IBaitItem;
import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.init.AquaItems;
import com.teammetallurgy.aquaculture.init.AquaLootTables;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import com.teammetallurgy.aquaculture.loot.FishWeightHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Replays Aquaculture's cast-and-catch on behalf of Cyclic's Fishing Net.
 *
 * <p>Cyclic's own {@code TileFisher.doFishing} rolls
 * {@code minecraft:gameplay/fishing} with nothing but the rod's Luck of the Sea
 * and a hardcoded +1, drops the result, and damages the rod in a way that
 * bypasses both Unbreaking and any notion of a hook. Everything Aquaculture
 * layers onto fishing — the hook's luck, its double catch, its durability save,
 * its fluid, bait, lure speed, the fish-weight roll and the open-water gate —
 * lives in {@code AquaFishingRodItem.use} and {@code AquaFishingBobberEntity},
 * neither of which a block ever goes through.</p>
 *
 * <p>So this walks the same path a bobber does, in the same order, with the same
 * numbers, against the same loot tables. The two places it cannot be literal are
 * documented where they occur: lure speed (the net has no bite timer to shorten,
 * so it becomes a catch-rate multiplier) and Mending (deliberately nerfed — see
 * {@link #damageRod}).</p>
 *
 * <p>Server-side only. This class is reachable exclusively from
 * {@link CyclicAquaFisher}, which will not touch it unless both mods are
 * present, so its Cyclic and Aquaculture references are never linked otherwise.</p>
 */
final class NetFishing {

    private NetFishing() {}

    /**
     * One replacement pass for a single {@code doFishing} call. Cyclic has
     * already picked the random block and confirmed it is fishable; the chance
     * roll itself belongs to us because lure speed modifies it.
     */
    static void fish(BlockEntity fisher, ItemStack rod, BlockPos center) {
        Level level = fisher.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RandomSource rand = level.getRandom();

        // An Aquaculture rod at max damage is "broken": it stays in the slot and
        // simply refuses to cast (AquaFishingRodItem.use returns FAIL). Mirror
        // that rather than fishing with a dead rod.
        if (rod.isDamageableItem() && rod.getDamageValue() >= rod.getMaxDamage()) {
            return;
        }

        Hook hook = AquaFishingRodItem.getHookType(rod);           // Hooks.EMPTY for a plain vanilla rod
        ItemStackHandler rodHandler = AquaFishingRodItem.getHandler(rod);
        ItemStack bait = rodHandler.getStackInSlot(1);

        boolean lavaFishing = isLavaHookInLava(hook, level, center);
        if (!lavaFishing && !level.getFluidState(center).is(FluidTags.WATER)) {
            return;
        }

        // --- Lure speed -------------------------------------------------
        // Same three sources and the same cap of 5 as AquaFishingRodItem.use.
        int lureSpeed = EnchantmentHelper.getFishingSpeedBonus(rod);
        if (rod.is(AquaItems.NEPTUNIUM_FISHING_ROD.get())) {
            lureSpeed++;
        }
        if (!bait.isEmpty() && bait.getItem() instanceof IBaitItem baitItem) {
            lureSpeed += baitItem.getLureSpeedModifier();
        }
        lureSpeed = Math.min(5, lureSpeed);

        // Hand-casting spends lure on shortening a bite timer. A block has no
        // timer, only a per-tick chance, so lure buys catch rate instead.
        double chance = baseChance() * (1.0D + lureSpeed * CyclicFisherConfig.LURE_RATE_BONUS_PER_LEVEL.get());
        if (rand.nextDouble() >= chance) {
            return;
        }

        // --- Luck -------------------------------------------------------
        // Bobber: getFishingLuckBonus(rod) + hook luck + the angler's luck
        // attribute. There is no angler here, so the attribute term becomes two
        // explicit pieces: a config constant defaulting to 0 (Cyclic's own +1 is
        // deliberately dropped), and the Harvest Moon boon, which a hand-cast rod
        // would have picked up through that same Luck attribute.
        double moonLuck = HarvestMoonLuck.isHarvestMoon(serverLevel) ? HarvestMoonLuck.bonus() : 0.0D;
        float luck = EnchantmentHelper.getFishingLuckBonus(rod)
                + Math.max(0, hook.getLuckModifier())
                + CyclicFisherConfig.BASE_LUCK_BONUS.get()
                + (float) moonLuck;

        Vec3 origin = Vec3.atCenterOf(center);
        boolean openWater = CyclicFisherConfig.ALLOW_OPEN_WATER_LOOT.get()
                && !lavaFishing
                && calculateOpenWater(level, center);
        NetFishingHook bobber = new NetFishingHook(level, origin, openWater);

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.TOOL, rod)
                .withParameter(LootContextParams.THIS_ENTITY, bobber)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING);

        List<ItemStack> loot = rollLoot(serverLevel, lootParams, lavaFishing);
        if (loot.isEmpty()) {
            return;
        }

        // Aquaculture's own ItemFishedEvent listener assigns fish weight. It is
        // invoked directly rather than by posting, so the feature works without
        // exposing a player-less hook to every other mod's listeners.
        FishWeightHandler.onItemFished(new ItemFishedEvent(loot, 1, bobber));

        if (CyclicFisherConfig.POST_ITEM_FISHED_EVENT.get()) {
            ItemFishedEvent event = new ItemFishedEvent(loot, 1, bobber);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return;     // cancelled: no drops, no bait spent, no rod wear
            }
        }

        if (WitherCatch.tryNetCatch(serverLevel, center, rod, hook)) {
            // Something else took the bite. Bait and rod still pay for the cast.
            spendBait(fisher, rodHandler, bait, level, rand);
            damageRod(fisher, rod, hook, rand);
            return;
        }

        spawnLoot(serverLevel, origin, loot);

        // Double catch rolls a completely independent second pull, exactly as
        // the bobber does — the same loot params, so open water still applies.
        if (hook.getDoubleCatchChance() > 0.0D && rand.nextDouble() <= hook.getDoubleCatchChance()) {
            List<ItemStack> doubleLoot = rollLoot(serverLevel, lootParams, lavaFishing);
            if (!doubleLoot.isEmpty()) {
                FishWeightHandler.onItemFished(new ItemFishedEvent(doubleLoot, 0, bobber));
                if (CyclicFisherConfig.POST_ITEM_FISHED_EVENT.get()) {
                    MinecraftForge.EVENT_BUS.post(new ItemFishedEvent(doubleLoot, 0, bobber));
                }
                spawnLoot(serverLevel, origin, doubleLoot);
            }
        }

        spendBait(fisher, rodHandler, bait, level, rand);
        damageRod(fisher, rod, hook, rand);
    }

    /** Cyclic's configured per-tick chance, with its own default as the fallback. */
    private static double baseChance() {
        try {
            return com.lothrazar.cyclic.block.fishing.TileFisher.CHANCE.get();
        } catch (RuntimeException e) {
            return 0.06D;   // Cyclic's shipped default, if its config is not loaded yet
        }
    }

    /**
     * Whether the equipped hook turns this block into a lava fisher.
     *
     * <p>Aquaculture gates lava fishing on the hook declaring
     * {@link FluidTags#LAVA}. As of 2.5.7 no hook does — the Nether Star hook
     * registers {@code FluidTags.WATER}, which reads like an upstream slip — so
     * this is always false today. It is written against {@code getFluids()}
     * rather than against a hardcoded hook so that a fixed or third-party
     * lava hook starts working here with no further changes.</p>
     */
    private static boolean isLavaHookInLava(Hook hook, Level level, BlockPos pos) {
        return CyclicFisherConfig.ALLOW_HOOK_FLUIDS.get()
                && hook != Hooks.EMPTY
                && hook.getFluids().contains(FluidTags.LAVA)
                && level.getFluidState(pos).is(FluidTags.LAVA);
    }

    /** Same table selection the bobber makes, including the empty-loot fallbacks. */
    private static List<ItemStack> rollLoot(ServerLevel level, LootParams params, boolean lavaFishing) {
        ResourceLocation id;
        if (lavaFishing) {
            id = level.dimensionType().ultraWarm() ? AquaLootTables.NETHER_FISHING : AquaLootTables.LAVA_FISHING;
        } else {
            id = BuiltInLootTables.FISHING;
        }
        LootTable table = level.getServer().getLootData().getLootTable(id);
        // Kept mutable: ItemFishedEvent listeners are allowed to edit the drops.
        List<ItemStack> loot = new ArrayList<>(table.getRandomItems(params));
        if (loot.isEmpty()) {
            loot.add(level.dimension() == Level.NETHER
                    ? new ItemStack(AquaItems.FISH_BONES.get())
                    : new ItemStack(Items.COD));
        }
        return loot;
    }

    private static void spawnLoot(ServerLevel level, Vec3 origin, List<ItemStack> loot) {
        boolean xp = CyclicFisherConfig.DROP_EXPERIENCE.get();
        for (ItemStack stack : loot) {
            ItemEntity item = new ItemEntity(level, origin.x, origin.y, origin.z, stack.copy());
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
            if (xp) {
                // Bobber awards 1-6 per caught item; there is no angler to award
                // it to, so it lands at the catch site with the loot.
                level.addFreshEntity(new ExperienceOrb(level, origin.x, origin.y + 0.5D, origin.z,
                        level.getRandom().nextInt(6) + 1));
            }
        }
    }

    /** Byte-for-byte the bobber's bait handling, minus its creative-mode exemption. */
    private static void spendBait(BlockEntity fisher, ItemStackHandler rodHandler, ItemStack bait,
                                  Level level, RandomSource rand) {
        if (bait.isEmpty()) {
            return;
        }
        if (bait.getItem().canBeDepleted()) {
            if (bait.hurt(1, rand, null)) {
                bait.setDamageValue(0);
                bait.shrink(1);
            }
        } else {
            bait.shrink(1);
        }
        rodHandler.setStackInSlot(1, bait);     // writes back into the rod's NBT
        fisher.setChanged();
    }

    /**
     * One point of wear per catch, as hand-casting charges.
     *
     * <p>Two things differ from Cyclic. Cyclic writes damage straight onto the
     * stack, which skips Unbreaking entirely; this goes through
     * {@code hurtAndBreak}, so Unbreaking counts exactly as it does in your
     * hands. And Cyclic's Mending branch repairs 2-4 points on ~half of all
     * catches against a single point of damage on a quarter of them, which is a
     * net gain — a Mending rod in a Cyclic net is immortal. Here Mending never
     * repairs; it only skips the damage, at a configurable chance. Decay stays
     * slow, but it is decay.</p>
     */
    private static void damageRod(BlockEntity fisher, ItemStack rod, Hook hook, RandomSource rand) {
        if (!rod.isDamageableItem()) {
            return;
        }
        // The hook's durability save comes first, same as AquaFishingRodItem.use.
        if (hook.getDurabilityChance() > 0.0D && rand.nextDouble() < hook.getDurabilityChance()) {
            return;
        }
        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MENDING, rod) > 0
                && rand.nextDouble() < CyclicFisherConfig.MENDING_DAMAGE_SKIP_CHANCE.get()) {
            return;
        }
        // hurt() honours Unbreaking and reports "this hit finished it off"; it
        // does not remove the stack, which is what lets an Aqua rod sit broken.
        if (rod.hurt(1, rand, null)) {
            // Aquaculture rods stay in the slot as "broken" instead of being
            // destroyed; a vanilla rod is consumed the way it would be in hand.
            if (!(rod.getItem() instanceof AquaFishingRodItem)) {
                rod.shrink(1);
            }
        }
        fisher.setChanged();
    }

    // ------------------------------------------------------------------
    // Open water
    // ------------------------------------------------------------------

    private enum OpenWaterType {
        INVALID,
        ABOVE_WATER,
        INSIDE_WATER
    }

    /**
     * Vanilla's {@code FishingHook.calculateOpenWater}, reproduced.
     *
     * <p>It cannot be called directly — it is private, and this project builds
     * without a mixin refmap, so vanilla classes are off limits as mixin
     * targets. The algorithm is copied exactly instead: four horizontal 5x5
     * slabs from {@code y-1} to {@code y+2}, each of which must be uniformly
     * water-or-air, never flipping back from air to water, with lily pads and
     * any collidable block invalidating the slab outright.</p>
     */
    private static boolean calculateOpenWater(Level level, BlockPos pos) {
        OpenWaterType previous = OpenWaterType.INVALID;
        for (int dy = -1; dy <= 2; dy++) {
            OpenWaterType slab = openWaterTypeForArea(level, pos.offset(-2, dy, -2), pos.offset(2, dy, 2));
            switch (slab) {
                case INVALID:
                    return false;
                case ABOVE_WATER:
                    if (previous == OpenWaterType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (previous == OpenWaterType.ABOVE_WATER) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
            previous = slab;
        }
        return true;
    }

    private static OpenWaterType openWaterTypeForArea(Level level, BlockPos from, BlockPos to) {
        return BlockPos.betweenClosedStream(from, to)
                .map(p -> openWaterTypeForBlock(level, p))
                .reduce((a, b) -> a == b ? a : OpenWaterType.INVALID)
                .orElse(OpenWaterType.INVALID);
    }

    private static OpenWaterType openWaterTypeForBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.LILY_PAD)) {
            return OpenWaterType.ABOVE_WATER;
        }
        FluidState fluid = state.getFluidState();
        return fluid.is(FluidTags.WATER) && fluid.isSource() && state.getCollisionShape(level, pos).isEmpty()
                ? OpenWaterType.INSIDE_WATER
                : OpenWaterType.INVALID;
    }
}
