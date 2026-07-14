package com.soul.soa_additions.block.entity;

import com.soul.soa_additions.block.TaskCollectorBlock;
import com.soul.soa_additions.quest.progress.CollectorService;
import com.soul.soa_additions.quest.task.EnergyTask;
import com.soul.soa_additions.quest.task.ManaTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The quest-bound resource sink ("task screen"). Owned by the player who
 * placed it; feeds whatever it receives straight into that player's active
 * consume-tasks and renders the selected task's live progress on its face
 * (see {@code TaskCollectorRenderer}):
 *
 * <ul>
 *   <li><b>Forge Energy</b> — pipe FE in from any side ({@link EnergyTask},
 *       {@link EnergyTask#UNIT} FE per point; sub-unit remainder persists).</li>
 *   <li><b>Botania mana</b> — mana-burst receiver; point a Mana Spreader at it.</li>
 *   <li><b>Items</b> — hopper/pipe items in; consume-variant item tasks eat them.</li>
 *   <li><b>XP / EMC / mana items</b> — right-click handling in the block class.</li>
 * </ul>
 *
 * Input is rejected (not buffered) while the owner is offline or has no
 * active task needing that resource, so nothing is silently wasted.
 */
public class TaskCollectorBlockEntity extends BlockEntity {

    @Nullable private UUID owner;
    /** Sub-unit FE remainder (0..UNIT-1), persisted so piped FE never rounds away. */
    private long feCarry;
    /** Full quest id + task index shown on the screen face. Synced to clients. */
    @Nullable private String selectedQuest;
    private int selectedTask;

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(EnergyReceiver::new);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(ItemReceiver::new);
    private LazyOptional<Object> manaCap = LazyOptional.empty();

    public TaskCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TASK_COLLECTOR.get(), pos, state);
    }

    public void setOwner(@Nullable UUID owner) { this.owner = owner; setChanged(); }
    @Nullable public UUID owner() { return owner; }
    @Nullable public String selectedQuest() { return selectedQuest; }
    public int selectedTask() { return selectedTask; }

    /** Resolve the owner as an online ServerPlayer, else null. */
    @Nullable public ServerPlayer onlineOwner() {
        if (owner == null || level == null || level.isClientSide() || level.getServer() == null) return null;
        return level.getServer().getPlayerList().getPlayer(owner);
    }

    /**
     * Advance the on-screen selection to the player's next active consumable
     * task (wrapping). Returns the newly selected task's description, or null
     * when there is nothing to select.
     */
    @Nullable
    public String cycleSelection(ServerPlayer player) {
        List<CollectorService.ActiveTask> active = CollectorService.activeTasks(player);
        if (active.isEmpty()) {
            selectedQuest = null;
            selectedTask = 0;
            sync();
            return null;
        }
        int current = -1;
        for (int i = 0; i < active.size(); i++) {
            var a = active.get(i);
            if (a.questId().equals(selectedQuest) && a.taskIndex() == selectedTask) { current = i; break; }
        }
        var next = active.get((current + 1) % active.size());
        selectedQuest = next.questId();
        selectedTask = next.taskIndex();
        sync();
        return next.task().describe();
    }

    /** Ensure something sensible is selected (used right after placement). */
    public void autoSelect(ServerPlayer player) {
        if (selectedQuest == null) cycleSelection(player);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ------------------------------------------------------------ energy
    private final class EnergyReceiver implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            ServerPlayer player = onlineOwner();
            if (player == null || maxReceive <= 0) return 0;
            long needRaw = CollectorService.remainingRaw(player, EnergyTask.TYPE) - feCarry;
            if (needRaw <= 0) return 0;
            int accept = (int) Math.min(maxReceive, needRaw);
            if (!simulate) {
                feCarry += accept;
                long units = feCarry / EnergyTask.UNIT;
                feCarry %= EnergyTask.UNIT;
                if (units > 0) CollectorService.creditUnits(player, units, EnergyTask.TYPE);
                setChanged();
            }
            return accept;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() {
            ServerPlayer player = onlineOwner();
            if (player == null) return 0;
            return (int) Math.min(Integer.MAX_VALUE, CollectorService.remainingRaw(player, EnergyTask.TYPE));
        }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    // ------------------------------------------------------------ items (consume item tasks)
    private final class ItemReceiver implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }

        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            ServerPlayer player = onlineOwner();
            return player != null && CollectorService.remainingItems(player, stack) > 0;
        }

        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            ServerPlayer player = onlineOwner();
            if (player == null || stack.isEmpty()) return stack;
            long need = CollectorService.remainingItems(player, stack);
            if (need <= 0) return stack;
            int accept = (int) Math.min(stack.getCount(), need);
            if (!simulate) CollectorService.creditItems(player, accept, stack);
            if (accept >= stack.getCount()) return ItemStack.EMPTY;
            ItemStack rest = stack.copy();
            rest.shrink(accept);
            return rest;
        }

        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
    }

    // ------------------------------------------------------------ mana (botania, optional)
    /** All Botania class references live here so the JVM only loads them when Botania exists. */
    private static final class BotaniaCompat {
        static boolean matches(Capability<?> cap) {
            return cap == vazkii.botania.api.BotaniaForgeCapabilities.MANA_RECEIVER;
        }
        static Object receiver(TaskCollectorBlockEntity be) {
            return new vazkii.botania.api.mana.ManaReceiver() {
                @Override public Level getManaReceiverLevel() { return be.getLevel(); }
                @Override public BlockPos getManaReceiverPos() { return be.getBlockPos(); }
                @Override public int getCurrentMana() { return 0; }
                @Override public boolean isFull() {
                    ServerPlayer player = be.onlineOwner();
                    return player == null || CollectorService.remainingRaw(player, ManaTask.TYPE) <= 0;
                }
                @Override public void receiveMana(int mana) {
                    ServerPlayer player = be.onlineOwner();
                    if (player == null || mana <= 0) return;
                    long need = CollectorService.remainingRaw(player, ManaTask.TYPE);
                    CollectorService.creditUnits(player, Math.min(mana, need), ManaTask.TYPE);
                }
                @Override public boolean canReceiveManaFromBursts() { return true; }
            };
        }
    }

    private static final boolean BOTANIA = ModList.get().isLoaded("botania");

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (BOTANIA && BotaniaCompat.matches(cap)) {
            if (!manaCap.isPresent()) manaCap = LazyOptional.of(() -> BotaniaCompat.receiver(this));
            return manaCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        itemCap.invalidate();
        manaCap.invalidate();
    }

    // ------------------------------------------------------------ render bounds (big screens)
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(com.soul.soa_additions.block.ScreenGeometry.MAX_SIZE);
    }

    // ------------------------------------------------------------ nbt + client sync
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("owner", owner);
        tag.putLong("fe_carry", feCarry);
        if (selectedQuest != null) tag.putString("sel_quest", selectedQuest);
        tag.putInt("sel_task", selectedTask);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        feCarry = tag.getLong("fe_carry");
        selectedQuest = tag.contains("sel_quest") ? tag.getString("sel_quest") : null;
        selectedTask = tag.getInt("sel_task");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
