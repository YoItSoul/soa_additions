package com.soul.soa_additions.block;

import com.soul.soa_additions.block.entity.TaskCollectorBlockEntity;
import com.soul.soa_additions.quest.progress.CollectorService;
import com.soul.soa_additions.quest.task.EmcTask;
import com.soul.soa_additions.quest.task.EnergyTask;
import com.soul.soa_additions.quest.task.ManaTask;
import com.soul.soa_additions.quest.task.XpTask;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Physical sink + display for consume-tasks ("task screen"). Place several in
 * a flat NxN wall (same facing) and they merge into one large screen — the
 * bottom-left block anchors the display and the rest go blank (see
 * {@link ScreenGeometry}); no GUI or formation step needed.
 *
 * <p>Right-click interactions (owner):</p>
 * <ul>
 *   <li>sneak + empty hand — cycle which task is shown on the screen</li>
 *   <li>empty hand — deposit XP points into active XP tasks</li>
 *   <li>Klein Star (ProjectE EMC holder) — drain EMC into EMC tasks</li>
 *   <li>Mana Tablet (Botania mana item) — drain mana into mana tasks</li>
 *   <li>any other item — submit toward matching consume item-tasks</li>
 * </ul>
 * FE piping, mana bursts, and hopper item input are handled by the block
 * entity's capabilities.
 */
public class TaskCollectorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TaskCollectorBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TaskCollectorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof TaskCollectorBlockEntity be) {
            be.setOwner(sp.getUUID());
            be.autoSelect(sp);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof TaskCollectorBlockEntity be)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        if (be.owner() == null) be.setOwner(sp.getUUID());

        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.isEmpty()) {
            // Selection lives on the screen's anchor block so multi-block
            // screens cycle correctly no matter which panel is clicked.
            BlockPos anchor = ScreenGeometry.anchorOf(level, pos, state.getValue(FACING));
            TaskCollectorBlockEntity target = level.getBlockEntity(anchor) instanceof TaskCollectorBlockEntity a ? a : be;
            if (target.owner() == null) target.setOwner(sp.getUUID());
            String selected = target.cycleSelection(sp);
            if (selected == null) msg(sp, "No active collector tasks.", ChatFormatting.GRAY);
            else msg(sp, "Screen: " + selected, ChatFormatting.AQUA);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            depositXp(sp);
            return InteractionResult.CONSUME;
        }
        if (PROJECTE && ProjectECompat.drainEmc(sp, held)) return InteractionResult.CONSUME;
        if (BOTANIA && BotaniaItemCompat.drainMana(sp, held)) return InteractionResult.CONSUME;
        if (submitItems(sp, held)) return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }

    // ------------------------------------------------------------ items (hand submission)
    private static boolean submitItems(ServerPlayer player, ItemStack held) {
        long need = CollectorService.remainingItems(player, held);
        if (need <= 0) return false;
        int submit = (int) Math.min(held.getCount(), need);
        CollectorService.creditItems(player, submit, held);
        held.shrink(submit);
        msg(player, String.format("Submitted %,d item(s).", submit), ChatFormatting.GREEN);
        return true;
    }

    // ------------------------------------------------------------ xp
    private static void depositXp(ServerPlayer player) {
        long need = CollectorService.remainingRaw(player, XpTask.TYPE);
        if (need <= 0) {
            status(player);
            return;
        }
        long total = totalXpPoints(player);
        int dep = (int) Math.min(Math.min(need, total), Integer.MAX_VALUE);
        if (dep <= 0) {
            msg(player, "You have no XP to deposit.", ChatFormatting.RED);
            return;
        }
        player.giveExperiencePoints(-dep);
        CollectorService.creditUnits(player, dep, XpTask.TYPE);
        msg(player, String.format("Deposited %,d XP. Remaining: %,d", dep,
                CollectorService.remainingRaw(player, XpTask.TYPE)), ChatFormatting.GREEN);
    }

    /** Vanilla total-XP-points formula from level + partial progress. */
    private static long totalXpPoints(Player player) {
        int lvl = player.experienceLevel;
        long points;
        if (lvl <= 16)      points = (long) lvl * lvl + 6L * lvl;
        else if (lvl <= 31) points = (long) (2.5 * lvl * lvl - 40.5 * lvl + 360);
        else                points = (long) (4.5 * lvl * lvl - 162.5 * lvl + 2220);
        return points + (long) (player.experienceProgress * player.getXpNeededForNextLevel());
    }

    // ------------------------------------------------------------ emc (projecte, optional)
    private static final boolean PROJECTE = ModList.get().isLoaded("projecte");

    private static final class ProjectECompat {
        static boolean drainEmc(ServerPlayer player, ItemStack held) {
            var capOpt = held.getCapability(moze_intel.projecte.api.capabilities.PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
            if (capOpt.isEmpty()) return false;
            var holder = capOpt.get();
            long need = CollectorService.remainingRaw(player, EmcTask.TYPE);
            if (need <= 0) {
                msg(player, "No active quest needs EMC right now.", ChatFormatting.GRAY);
                return true;
            }
            long drained = holder.extractEmc(held, need,
                    moze_intel.projecte.api.capabilities.block_entity.IEmcStorage.EmcAction.EXECUTE);
            if (drained <= 0) {
                msg(player, "That item holds no EMC.", ChatFormatting.RED);
                return true;
            }
            CollectorService.creditUnits(player, drained, EmcTask.TYPE);
            msg(player, String.format("Transferred %,d EMC. Remaining: %,d", drained,
                    CollectorService.remainingRaw(player, EmcTask.TYPE)), ChatFormatting.GREEN);
            return true;
        }
    }

    // ------------------------------------------------------------ mana item (botania, optional)
    private static final boolean BOTANIA = ModList.get().isLoaded("botania");

    private static final class BotaniaItemCompat {
        static boolean drainMana(ServerPlayer player, ItemStack held) {
            var capOpt = held.getCapability(vazkii.botania.api.BotaniaForgeCapabilities.MANA_ITEM).resolve();
            if (capOpt.isEmpty()) return false;
            var manaItem = capOpt.get();
            long need = CollectorService.remainingRaw(player, ManaTask.TYPE);
            if (need <= 0) {
                msg(player, "No active quest needs Mana right now.", ChatFormatting.GRAY);
                return true;
            }
            int drain = (int) Math.min(need, manaItem.getMana());
            if (drain <= 0) {
                msg(player, "That item holds no Mana.", ChatFormatting.RED);
                return true;
            }
            manaItem.addMana(-drain);
            CollectorService.creditUnits(player, drain, ManaTask.TYPE);
            msg(player, String.format("Channeled %,d Mana. Remaining: %,d", drain,
                    CollectorService.remainingRaw(player, ManaTask.TYPE)), ChatFormatting.GREEN);
            return true;
        }
    }

    // ------------------------------------------------------------ status
    private static void status(ServerPlayer player) {
        StringBuilder sb = new StringBuilder("Task Collector — outstanding: ");
        boolean any = false;
        any |= append(sb, player, EnergyTask.TYPE, "FE", any);
        any |= append(sb, player, XpTask.TYPE, "XP", any);
        any |= append(sb, player, ManaTask.TYPE, "Mana", any);
        any |= append(sb, player, EmcTask.TYPE, "EMC", any);
        if (!any) sb.append("nothing — no active resource tasks.");
        msg(player, sb.toString(), ChatFormatting.AQUA);
    }

    private static boolean append(StringBuilder sb, ServerPlayer player, ResourceLocation type, String label, boolean anyBefore) {
        long need = CollectorService.remainingRaw(player, type);
        if (need <= 0) return false;
        if (anyBefore) sb.append(", ");
        sb.append(String.format("%,d %s", need, label));
        return true;
    }

    private static void msg(ServerPlayer player, String text, ChatFormatting color) {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }
}
