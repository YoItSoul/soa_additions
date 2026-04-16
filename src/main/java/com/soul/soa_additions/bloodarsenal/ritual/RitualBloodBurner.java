package com.soul.soa_additions.bloodarsenal.ritual;

import com.google.common.collect.Sets;
import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.BAItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * Blood Burner — converts lava + life essence -> RF energy.
 * Reads fluid tanks at specific positions. Glowstone dust on stasis plates provides multiplier.
 * Energy formula: TOTAL_RF = u * 0.5 * ((a^1.5 / 100) * (g+1) + 2*l - 2000)
 * Time formula: TIME = (400 * (a*g + 2*a^1.1)) / (132 * l^1.3) seconds
 *
 * <p>Ported from: arcaratus.bloodarsenal.ritual.RitualBloodBurner</p>
 */
@RitualRegister("blood_arsenal_blood_burner")
public class RitualBloodBurner extends Ritual {

    // Structure positions from original
    private static final Set<BlockPos> FIRE_POS = Sets.newHashSet(
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(-1, 0, -1));
    private static final Set<BlockPos> GLOWSTONE_POS = Sets.newHashSet(
            new BlockPos(2, 1, 0), new BlockPos(-2, 1, 0), new BlockPos(0, 1, 2), new BlockPos(0, 1, -2),
            new BlockPos(2, 1, 1), new BlockPos(2, 1, -1), new BlockPos(-2, 1, 1), new BlockPos(-2, 1, -1),
            new BlockPos(1, 1, 2), new BlockPos(1, 1, -2), new BlockPos(-1, 1, 2), new BlockPos(-1, 1, -2),
            new BlockPos(2, 1, 2), new BlockPos(2, 1, -2), new BlockPos(-2, 1, 2), new BlockPos(-2, 1, -2));
    private static final Set<BlockPos> LAVA_POS = Sets.newHashSet(
            new BlockPos(3, 1, 1), new BlockPos(3, 1, -1), new BlockPos(-3, 1, 1), new BlockPos(-3, 1, -1),
            new BlockPos(3, 1, 2), new BlockPos(-3, 1, 2), new BlockPos(3, 1, -2), new BlockPos(-3, 1, -2),
            new BlockPos(1, 1, 3), new BlockPos(-1, 1, 3), new BlockPos(1, 1, -3), new BlockPos(-1, 1, -3),
            new BlockPos(2, 1, 3), new BlockPos(-2, 1, 3), new BlockPos(2, 1, -3), new BlockPos(-2, 1, -3));
    private static final Set<BlockPos> LIFE_ESSENCE_POS = Sets.newHashSet(
            new BlockPos(4, 1, 0), new BlockPos(-4, 1, 0), new BlockPos(0, 1, 4), new BlockPos(0, 1, -4),
            new BlockPos(4, 1, 2), new BlockPos(-4, 1, 2), new BlockPos(4, 1, -2), new BlockPos(-4, 1, -2),
            new BlockPos(2, 1, 4), new BlockPos(-2, 1, 4), new BlockPos(2, 1, -4), new BlockPos(-2, 1, -4),
            new BlockPos(4, 1, 4), new BlockPos(-4, 1, 4), new BlockPos(4, 1, -4), new BlockPos(-4, 1, -4));
    private static final Set<BlockPos> FIRING_POS = Sets.newHashSet(
            new BlockPos(6, 6, 6), new BlockPos(-6, 6, 6), new BlockPos(6, 6, -6), new BlockPos(-6, 6, -6),
            new BlockPos(5, 5, 6), new BlockPos(-5, 5, 6), new BlockPos(5, 5, -6), new BlockPos(-5, 5, -6),
            new BlockPos(6, 5, 5), new BlockPos(6, 5, -5), new BlockPos(-6, 5, 5), new BlockPos(-6, 5, -5),
            new BlockPos(5, 3, 6), new BlockPos(-5, 3, 6), new BlockPos(5, 3, -6), new BlockPos(-5, 3, -6),
            new BlockPos(6, 3, 5), new BlockPos(6, 3, -5), new BlockPos(-6, 3, 5), new BlockPos(-6, 3, -5),
            new BlockPos(5, 1, 6), new BlockPos(-5, 1, 6), new BlockPos(5, 1, -6), new BlockPos(-5, 1, -6),
            new BlockPos(6, 1, 5), new BlockPos(6, 1, -5), new BlockPos(-6, 1, 5), new BlockPos(-6, 1, -5));

    private boolean active = false;
    private int secondsLeft = 0;
    private int rateRF = 0;

    public RitualBloodBurner() {
        super("blood_arsenal_blood_burner", 1,
                BAConfig.BURNER_ACTIVATION_COST.get(),
                "ritual.soa_additions.blood_burner");
    }

    @Override
    public boolean activateRitual(IMasterRitualStone mrs,
                                  net.minecraft.world.entity.player.Player player,
                                  java.util.UUID uuid) {
        Level level = mrs.getWorldObj();
        BlockPos pos = mrs.getMasterBlockPos();

        // Validate structure
        if (!checkStructure(level, pos)) return false;

        // Read fluid amounts from tanks
        int lavaAmount = getFluidAmount(level, pos, LAVA_POS, true);
        if (lavaAmount < 1000) return false; // Need at least 1 bucket of lava

        int lifeEssenceAmount = getFluidAmount(level, pos, LIFE_ESSENCE_POS, false);
        if (lifeEssenceAmount < 1000) return false; // Need at least 1 bucket of life essence

        // Check for glowstone dust on stasis plates
        int dustAmount = getDustAmount(level, pos);
        if (dustAmount <= 0) return false;

        // Check for igniter on stasis plate above master
        BlockEntity plateTile = level.getBlockEntity(pos.above());
        if (plateTile == null) return false;
        // Check igniter via item handler capability
        var itemCap = plateTile.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (!itemCap.isPresent()) return false;
        var handler = itemCap.resolve().get();
        ItemStack igniter = handler.getStackInSlot(0);
        if (igniter.isEmpty()) return false;
        boolean isFlintAndSteel = igniter.getItem() == Items.FLINT_AND_STEEL;
        boolean isBoundIgniter = igniter.getItem() == BAItems.BOUND_IGNITER.get();
        if (!isFlintAndSteel && !isBoundIgniter) return false;

        return super.activateRitual(mrs, player, uuid);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos pos = mrs.getMasterBlockPos();
        if (level.isClientSide()) return;

        if (!checkStructure(level, pos)) {
            endRitual(level, pos, mrs);
            return;
        }

        int lavaAmount = getFluidAmount(level, pos, LAVA_POS, true);
        int lifeEssenceAmount = getFluidAmount(level, pos, LIFE_ESSENCE_POS, false);
        int dustAmount = getDustAmount(level, pos);

        if (active) {
            if (secondsLeft > 0 && rateRF > 0) {
                // Push energy to output
                BlockEntity energyBE = level.getBlockEntity(pos.above(2));
                if (energyBE != null) {
                    energyBE.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).ifPresent(storage -> {
                        storage.receiveEnergy(rateRF, false);
                    });
                }
                secondsLeft--;

                // LP drain every 4 ticks
                if (level.getGameTime() % 4 == 0) {
                    int lpCost = (int) (BAConfig.BURNER_REFRESH_COST.get() * Math.log10(Math.max(1, rateRF)));
                    mrs.getOwnerNetwork().syphon(SoulTicket.block(level, pos, lpCost));
                }

                // Lightning VFX at firing positions
                if (level instanceof ServerLevel serverLevel) {
                    for (BlockPos firingPos : FIRING_POS) {
                        BlockPos actual = pos.offset(firingPos);
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (bolt != null) {
                            bolt.moveTo(Vec3.atBottomCenterOf(actual));
                            bolt.setVisualOnly(true);
                            serverLevel.addFreshEntity(bolt);
                        }
                        if (level.getBlockState(actual).isAir()) {
                            level.setBlock(actual, Blocks.FIRE.defaultBlockState(), 11);
                        }
                    }
                }
            } else if (secondsLeft <= 0) {
                // Try to start next burn cycle
                if (lifeEssenceAmount >= 1000 && lavaAmount >= 1000 && dustAmount > 0) {
                    startBurn(level, pos, lavaAmount, lifeEssenceAmount);
                } else {
                    endRitual(level, pos, mrs);
                }
            }
        } else if (lifeEssenceAmount >= 1000 && lavaAmount >= 1000 && dustAmount > 0) {
            startBurn(level, pos, lavaAmount, lifeEssenceAmount);
        } else {
            endRitual(level, pos, mrs);
        }
    }

    private void startBurn(Level level, BlockPos pos, int lavaAmount, int lifeEssenceAmount) {
        // Check igniter multiplier
        int u = 1;
        BlockEntity plateTile = level.getBlockEntity(pos.above());
        if (plateTile != null) {
            plateTile.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                // Can't assign to local var from lambda, handled below
            });
            var cap = plateTile.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (cap.isPresent()) {
                ItemStack igniter = cap.resolve().get().getStackInSlot(0);
                if (!igniter.isEmpty() && igniter.getItem() == BAItems.BOUND_IGNITER.get()) {
                    u = 2;
                }
            }
        }

        int dustAmount = getDustAmount(level, pos);
        int g = dustAmount * dustAmount; // g = dust^2

        int time = (int) ((400.0 * (lifeEssenceAmount * g + 2 * Math.pow(lifeEssenceAmount, 1.1)))
                / (132.0 * Math.pow(lavaAmount, 1.3)));
        time = Math.max(time, 1);

        int totalRF = (int) (u * 0.5 * ((Math.pow(lifeEssenceAmount, 1.5) / 100.0) * (g + 1.0) + (2 * lavaAmount) - 2000));
        totalRF = Math.max(totalRF, 0);

        int rate = totalRF / time;

        active = true;
        secondsLeft = time;
        rateRF = Math.max(rate, 1);

        // Drain fluids from tanks
        drainFluidTanks(level, pos, LAVA_POS, 1000);
        drainFluidTanks(level, pos, LIFE_ESSENCE_POS, 1000);

        // Consume glowstone dust from stasis plates
        consumeDust(level, pos);

        // Initial lightning VFX
        if (level instanceof ServerLevel serverLevel) {
            for (BlockPos corner : List.of(
                    new BlockPos(6, 5, 6), new BlockPos(6, 5, -6),
                    new BlockPos(-6, 5, 6), new BlockPos(-6, 5, -6))) {
                BlockPos actual = pos.offset(corner);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(actual));
                    bolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(bolt);
                }
            }
        }
    }

    private void endRitual(Level level, BlockPos pos, IMasterRitualStone mrs) {
        active = false;
        secondsLeft = 0;
        rateRF = 0;
        mrs.setActive(false);
    }

    private boolean checkStructure(Level level, BlockPos pos) {
        // Energy handler 2 blocks above
        BlockEntity energyBE = level.getBlockEntity(pos.above(2));
        if (energyBE == null || !energyBE.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).isPresent()) {
            return false;
        }

        // Stasis plate at pos+1 up
        BlockEntity plateTile = level.getBlockEntity(pos.above());
        if (plateTile == null || !plateTile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            return false;
        }

        // Fire at all fire positions
        for (BlockPos firePos : FIRE_POS) {
            if (!(level.getBlockState(pos.offset(firePos)).getBlock() instanceof FireBlock)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets total fluid amount from tanks at given positions.
     * For lava: counts 1 bucket per tank that has lava.
     * For life essence: counts actual amount, with refined life essence counting 4x.
     */
    private int getFluidAmount(Level level, BlockPos center, Set<BlockPos> positions, boolean isLava) {
        int total = 0;
        for (BlockPos offset : positions) {
            BlockPos tankPos = center.offset(offset);
            BlockEntity be = level.getBlockEntity(tankPos);
            if (be == null) continue;

            var fluidCap = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
            if (!fluidCap.isPresent()) continue;

            IFluidHandler handler = fluidCap.resolve().get();
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (fluid.isEmpty()) continue;

                if (isLava) {
                    // Check if it's lava
                    if (fluid.getFluid().isSame(net.minecraft.world.level.material.Fluids.LAVA)) {
                        total += Math.min(fluid.getAmount(), 1000);
                    }
                } else {
                    // Life essence - check for refined (4x multiplier)
                    int amount = Math.min(fluid.getAmount(), 1000);
                    // Refined life essence has 4x value
                    String fluidName = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid.getFluid()).toString();
                    if (fluidName.contains("refined")) {
                        total += amount * 4;
                    } else {
                        total += amount;
                    }
                }
            }
        }
        return total;
    }

    private int getDustAmount(Level level, BlockPos center) {
        int amount = 0;
        for (BlockPos offset : GLOWSTONE_POS) {
            BlockEntity be = level.getBlockEntity(center.offset(offset));
            if (be == null) continue;
            var itemCap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (!itemCap.isPresent()) continue;
            var handler = itemCap.resolve().get();
            ItemStack stack = handler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.GLOWSTONE_DUST) {
                    amount += stack.getCount();
                } else if (stack.getItem() == BAItems.BLOOD_INFUSED_GLOWSTONE_DUST.get()) {
                    amount += stack.getCount() * 2;
                }
            }
        }
        return amount;
    }

    private void drainFluidTanks(Level level, BlockPos center, Set<BlockPos> positions, int drainAmount) {
        for (BlockPos offset : positions) {
            BlockEntity be = level.getBlockEntity(center.offset(offset));
            if (be == null) continue;
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                handler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
            });
        }
    }

    private void consumeDust(Level level, BlockPos center) {
        for (BlockPos offset : GLOWSTONE_POS) {
            BlockEntity be = level.getBlockEntity(center.offset(offset));
            if (be == null) continue;
            be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                ItemStack stack = handler.getStackInSlot(0);
                if (!stack.isEmpty() && (stack.getItem() == Items.GLOWSTONE_DUST
                        || stack.getItem() == BAItems.BLOOD_INFUSED_GLOWSTONE_DUST.get())) {
                    handler.extractItem(0, 1, false);
                }
            });
        }
    }

    @Override
    public int getRefreshCost() {
        return BAConfig.BURNER_REFRESH_COST.get();
    }

    @Override
    public int getRefreshTime() {
        return 1; // Run every tick for energy output
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        active = tag.getBoolean("ba_active");
        secondsLeft = tag.getInt("ba_secondsLeft");
        rateRF = tag.getInt("ba_rfPerTick");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putBoolean("ba_active", active);
        tag.putInt("ba_secondsLeft", secondsLeft);
        tag.putInt("ba_rfPerTick", rateRF);
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> consumer) {
        // Exact glyph pattern from original RitualBloodBurner.java
        addRune(consumer, 0, -1, 0, EnumRuneType.FIRE);
        addParallelRunes(consumer, 2, -1, EnumRuneType.FIRE);
        addOffsetRunes(consumer, 1, 2, -1, EnumRuneType.FIRE);
        addCornerRunes(consumer, 2, -1, EnumRuneType.BLANK);
        addParallelRunes(consumer, 2, 0, EnumRuneType.AIR);
        addOffsetRunes(consumer, 1, 2, 0, EnumRuneType.AIR);
        addCornerRunes(consumer, 2, 0, EnumRuneType.AIR);
        addParallelRunes(consumer, 3, 0, EnumRuneType.DUSK);
        addOffsetRunes(consumer, 3, 1, 0, EnumRuneType.FIRE);
        addOffsetRunes(consumer, 3, 2, 0, EnumRuneType.FIRE);
        addCornerRunes(consumer, 3, 0, EnumRuneType.BLANK);
        addParallelRunes(consumer, 4, 0, EnumRuneType.WATER);
        addOffsetRunes(consumer, 4, 1, 0, EnumRuneType.EARTH);
        addOffsetRunes(consumer, 4, 2, 0, EnumRuneType.WATER);
        addOffsetRunes(consumer, 4, 3, 0, EnumRuneType.EARTH);
        addCornerRunes(consumer, 4, 0, EnumRuneType.WATER);

        addOffsetRunes(consumer, 6, 5, -1, EnumRuneType.DUSK);
        addCornerRunes(consumer, 6, -1, EnumRuneType.DUSK);
        addCornerRunes(consumer, 5, -1, EnumRuneType.DUSK);

        for (int y = 0; y < 6; y++) {
            if (y % 2 == 0)
                addOffsetRunes(consumer, 6, 5, y, EnumRuneType.FIRE);
            addCornerRunes(consumer, 6, y, EnumRuneType.FIRE);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualBloodBurner();
    }
}
