package com.soul.soa_additions.bloodarsenal.ritual;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import wayoftime.bloodmagic.ritual.*;

import java.util.function.Consumer;

/**
 * Purification — converts normal Life Essence into Refined Life Essence at a 10:1 ratio.
 * Input tank: center+3 up, Output tank: center+2 up.
 * 4 stasis plates at (+-2,1,0), (0,1,+-2).
 */
@RitualRegister("blood_arsenal_purification")
public class RitualPurification extends Ritual {

    private static final int INPUT_AMOUNT = 10;
    private static final int OUTPUT_AMOUNT = 1;

    public RitualPurification() {
        super("blood_arsenal_purification", 0,
                BAConfig.PURIFICATION_ACTIVATION_COST.get(),
                "ritual.soa_additions.purification");
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos pos = mrs.getMasterBlockPos();

        if (level.isClientSide()) return;

        // Syphon LP
        int refreshCost = getRefreshCost();
        var network = mrs.getOwnerNetwork();
        if (network.syphon(mrs.ticket(refreshCost)) != refreshCost) {
            mrs.setActive(false);
            return;
        }

        // Input tank at center+3 up
        BlockPos inputPos = pos.above(3);
        BlockEntity inputBE = level.getBlockEntity(inputPos);
        if (inputBE == null) return;

        // Output tank at center+2 up
        BlockPos outputPos = pos.above(2);
        BlockEntity outputBE = level.getBlockEntity(outputPos);
        if (outputBE == null) return;

        // Try to drain from input and fill output
        inputBE.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).ifPresent(inputHandler -> {
            outputBE.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).ifPresent(outputHandler -> {
                // Drain INPUT_AMOUNT mB from input
                FluidStack drained = inputHandler.drain(INPUT_AMOUNT, IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty() || drained.getAmount() < INPUT_AMOUNT) return;

                // Fill OUTPUT_AMOUNT mB to output (using the same fluid type for now)
                FluidStack toFill = drained.copy();
                toFill.setAmount(OUTPUT_AMOUNT);

                int filled = outputHandler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                if (filled >= OUTPUT_AMOUNT) {
                    inputHandler.drain(INPUT_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
                    outputHandler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        });
    }

    @Override
    public int getRefreshCost() {
        return BAConfig.PURIFICATION_REFRESH_COST.get();
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> consumer) {
        // Exact glyph pattern from original RitualPurification.java
        addParallelRunes(consumer, 1, 0, EnumRuneType.BLANK);
        addCornerRunes(consumer, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(consumer, 2, 0, EnumRuneType.FIRE);
        addOffsetRunes(consumer, 2, 1, 0, EnumRuneType.AIR);
        addCornerRunes(consumer, 2, 0, EnumRuneType.EARTH);
        addParallelRunes(consumer, 3, 0, EnumRuneType.BLANK);
        addOffsetRunes(consumer, 3, 1, 0, EnumRuneType.AIR);
        addOffsetRunes(consumer, 3, 2, 0, EnumRuneType.WATER);

        addCornerRunes(consumer, 4, 0, EnumRuneType.EARTH);
        addCornerRunes(consumer, 4, 1, EnumRuneType.EARTH);
        addCornerRunes(consumer, 4, 2, EnumRuneType.FIRE);
        addCornerRunes(consumer, 4, 3, EnumRuneType.FIRE);
        addCornerRunes(consumer, 4, 4, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualPurification();
    }
}
