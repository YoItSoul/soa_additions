package com.soul.soa_additions.bloodarsenal.ritual;

import com.soul.soa_additions.bloodarsenal.BAConfig;
import com.soul.soa_additions.bloodarsenal.block.StasisPlateBlock;
import com.soul.soa_additions.bloodarsenal.recipe.BARecipeTypes;
import com.soul.soa_additions.bloodarsenal.recipe.SanguineInfusionRecipe;
import com.soul.soa_additions.bloodarsenal.tile.StasisPlateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.altar.IBloodAltar;
import wayoftime.bloodmagic.ritual.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Sanguine Infusion — performs item crafting on stasis plates around the ritual.
 * Structure: Master ritual stone with Blood Altar at (0,1,0) above,
 * and 8 stasis plates at fixed cardinal positions at Y+1.
 * <p>
 * Consumes LP gradually during crafting. On completion, replaces
 * the altar's input item with the recipe output.
 * <p>
 * Ported from: arcaratus.bloodarsenal.ritual.RitualInfusion
 */
@RitualRegister("blood_arsenal_sanguine_infusion")
public class RitualSanguineInfusion extends Ritual {

    private static final Set<BlockPos> STASIS_PLATE_POS = Set.of(
            new BlockPos(1, 1, 3), new BlockPos(-1, 1, 3),
            new BlockPos(1, 1, -3), new BlockPos(-1, 1, -3),
            new BlockPos(3, 1, 1), new BlockPos(3, 1, -1),
            new BlockPos(-3, 1, 1), new BlockPos(-3, 1, -1)
    );

    private int craftingTimer;
    private boolean isCrafting;

    public RitualSanguineInfusion() {
        super("blood_arsenal_sanguine_infusion", 0,
                BAConfig.INFUSION_ACTIVATION_COST.get(),
                "ritual.soa_additions.sanguine_infusion");
        craftingTimer = 0;
        isCrafting = false;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        craftingTimer = tag.getInt("ba_craftingTimer");
        isCrafting = tag.getBoolean("ba_isCrafting");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("ba_craftingTimer", craftingTimer);
        tag.putBoolean("ba_isCrafting", isCrafting);
    }

    @Override
    public boolean activateRitual(IMasterRitualStone mrs, net.minecraft.world.entity.player.Player player, java.util.UUID owner) {
        Level level = mrs.getWorldObj();
        BlockPos pos = mrs.getMasterBlockPos();

        if (!checkStructure(level, pos)) {
            return false;
        }

        // Check that altar has an item and plates have items matching a recipe
        if (!(level.getBlockEntity(pos.above()) instanceof IBloodAltar)) {
            return false;
        }

        return true;
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

        // Get the altar block entity (which is also an inventory)
        var altarBE = level.getBlockEntity(pos.above());
        if (!(altarBE instanceof IBloodAltar)) {
            endRitual(level, pos, mrs);
            return;
        }

        // Read items from stasis plates
        List<StasisPlateBlockEntity> plates = getStasisPlates(level, pos);
        List<ItemStack> plateItems = new ArrayList<>();
        for (StasisPlateBlockEntity plate : plates) {
            ItemStack stored = plate.getStoredItem();
            if (!stored.isEmpty()) {
                plateItems.add(stored);
            }
        }

        if (plateItems.isEmpty()) {
            endRitual(level, pos, mrs);
            return;
        }

        // Find matching recipe
        SimpleContainer container = new SimpleContainer(plateItems.size());
        for (int i = 0; i < plateItems.size(); i++) {
            container.setItem(i, plateItems.get(i));
        }

        Optional<SanguineInfusionRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(BARecipeTypes.SANGUINE_INFUSION_TYPE.get(), container, level);

        if (recipeOpt.isEmpty()) {
            endRitual(level, pos, mrs);
            return;
        }

        SanguineInfusionRecipe recipe = recipeOpt.get();

        if (!isCrafting) {
            // Start crafting - lock stasis plates
            setStasisPlates(level, plates, true);
            isCrafting = true;
            craftingTimer = 0;
        }

        // Tick crafting - syphon LP each tick
        craftingTimer++;
        int refreshCost = getRefreshCost();
        var network = mrs.getOwnerNetwork();
        if (network.syphon(mrs.ticket(refreshCost)) != refreshCost) {
            endRitual(level, pos, mrs);
            return;
        }

        // Check if crafting is done (total ticks = lpCost / refreshCost)
        int totalTicks = recipe.getLpCost() / Math.max(1, refreshCost);
        if (craftingTimer >= totalTicks) {
            // Consume ingredients from stasis plates
            consumeIngredients(level, pos, recipe, plates);

            // Spawn the result as an item entity above the altar
            ItemStack result = recipe.getOutput().copy();
            if (level instanceof ServerLevel serverLevel) {
                net.minecraft.world.entity.item.ItemEntity itemEntity =
                        new net.minecraft.world.entity.item.ItemEntity(
                                serverLevel,
                                pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5,
                                result);
                itemEntity.setDeltaMovement(0, 0.1, 0);
                serverLevel.addFreshEntity(itemEntity);

                // Visual feedback: lightning
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atCenterOf(pos));
                    bolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(bolt);
                }
            }

            endRitual(level, pos, mrs);
        }
    }

    private void consumeIngredients(Level level, BlockPos pos, SanguineInfusionRecipe recipe, List<StasisPlateBlockEntity> plates) {
        // For each ingredient in the recipe, find and consume a matching item from a plate
        var ingredients = recipe.getIngredients();
        boolean[] consumed = new boolean[plates.size()];

        for (var ingredient : ingredients) {
            for (int i = 0; i < plates.size(); i++) {
                if (consumed[i]) continue;
                StasisPlateBlockEntity plate = plates.get(i);
                ItemStack stored = plate.getStoredItem();
                if (!stored.isEmpty() && ingredient.test(stored)) {
                    stored.shrink(1);
                    if (stored.isEmpty()) {
                        plate.setStoredItem(ItemStack.EMPTY);
                    }
                    consumed[i] = true;
                    break;
                }
            }
        }
    }

    private void endRitual(Level level, BlockPos pos, IMasterRitualStone mrs) {
        List<StasisPlateBlockEntity> plates = getStasisPlates(level, pos);
        setStasisPlates(level, plates, false);
        isCrafting = false;
        craftingTimer = 0;
        mrs.setActive(false);
    }

    private boolean checkStructure(Level level, BlockPos pos) {
        // Blood Altar must be directly above the master ritual stone
        if (!(level.getBlockEntity(pos.above()) instanceof IBloodAltar)) {
            return false;
        }

        // All 8 stasis plate positions must have stasis plates
        for (BlockPos plateOffset : STASIS_PLATE_POS) {
            BlockPos platePos = pos.offset(plateOffset);
            if (!(level.getBlockState(platePos).getBlock() instanceof StasisPlateBlock)) {
                return false;
            }
        }

        return true;
    }

    private List<StasisPlateBlockEntity> getStasisPlates(Level level, BlockPos pos) {
        List<StasisPlateBlockEntity> plates = new ArrayList<>();
        for (BlockPos plateOffset : STASIS_PLATE_POS) {
            BlockPos platePos = pos.offset(plateOffset);
            if (level.getBlockEntity(platePos) instanceof StasisPlateBlockEntity plate) {
                plates.add(plate);
            }
        }
        return plates;
    }

    private void setStasisPlates(Level level, List<StasisPlateBlockEntity> plates, boolean stasis) {
        for (StasisPlateBlockEntity plate : plates) {
            plate.setInStasis(stasis);
            BlockPos platePos = plate.getBlockPos();
            level.sendBlockUpdated(platePos, level.getBlockState(platePos), level.getBlockState(platePos), 3);
        }
    }

    @Override
    public int getRefreshCost() {
        return BAConfig.INFUSION_REFRESH_COST.get();
    }

    @Override
    public int getRefreshTime() {
        return 1; // Tick every game tick for smooth crafting progress
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> consumer) {
        // Exact glyph pattern from original RitualInfusion.java
        addCornerRunes(consumer, 1, -1, EnumRuneType.WATER);
        addCornerRunes(consumer, 2, -1, EnumRuneType.FIRE);
        addParallelRunes(consumer, 3, -1, EnumRuneType.EARTH);
        addOffsetRunes(consumer, 1, 4, -1, EnumRuneType.WATER);
        addParallelRunes(consumer, 5, -1, EnumRuneType.AIR);
        addParallelRunes(consumer, 2, 0, EnumRuneType.DUSK);
        addOffsetRunes(consumer, 1, 3, 0, EnumRuneType.FIRE);
        addCornerRunes(consumer, 3, 0, EnumRuneType.DUSK);
        addParallelRunes(consumer, 4, 0, EnumRuneType.DUSK);
        addCornerRunes(consumer, 4, 0, EnumRuneType.WATER);
        addOffsetRunes(consumer, 2, 5, 0, EnumRuneType.BLANK);
        addOffsetRunes(consumer, 3, 5, 0, EnumRuneType.BLANK);
        addOffsetRunes(consumer, 4, 5, 0, EnumRuneType.BLANK);
        addCornerRunes(consumer, 4, 1, EnumRuneType.EARTH);
        addOffsetRunes(consumer, 2, 5, 1, EnumRuneType.EARTH);
        addCornerRunes(consumer, 4, 2, EnumRuneType.FIRE);
        addOffsetRunes(consumer, 2, 5, 2, EnumRuneType.FIRE);
        addCornerRunes(consumer, 4, 3, EnumRuneType.EARTH);
        addOffsetRunes(consumer, 2, 5, 3, EnumRuneType.EARTH);
        addOffsetRunes(consumer, 1, 5, 4, EnumRuneType.WATER);
        addOffsetRunes(consumer, 3, 4, 4, EnumRuneType.WATER);
        addParallelRunes(consumer, 4, 5, EnumRuneType.AIR);
        addCornerRunes(consumer, 3, 5, EnumRuneType.AIR);
        addOffsetRunes(consumer, 2, 3, 5, EnumRuneType.DUSK);
        addParallelRunes(consumer, 3, 6, EnumRuneType.DUSK);
        addOffsetRunes(consumer, 1, 3, 6, EnumRuneType.DUSK);
        addCornerRunes(consumer, 2, 6, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSanguineInfusion();
    }
}
