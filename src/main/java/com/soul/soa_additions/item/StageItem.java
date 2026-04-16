package com.soul.soa_additions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class StageItem extends Item {

    private final boolean foil;
    private final String[] tooltipLines;
    /** When non-null, right-clicking this item adds the named GameStages stage
     *  to the player and consumes one from the stack. Items used purely as
     *  trophies (e.g. WITHER_SOUL) leave this null. */
    @Nullable private final String stageToGrant;

    public StageItem(Properties props, boolean foil, String... tooltip) {
        this(props, foil, null, tooltip);
    }

    public StageItem(Properties props, boolean foil, @Nullable String stageToGrant, String... tooltip) {
        super(props);
        this.foil = foil;
        this.tooltipLines = tooltip;
        this.stageToGrant = stageToGrant;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return foil || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        for (String line : tooltipLines) {
            list.add(Component.literal(line));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (stageToGrant == null) return InteractionResultHolder.pass(held);

        // Client predicts swing; the server-side branch below is authoritative.
        if (level.isClientSide) return InteractionResultHolder.success(held);

        if (hasStage(player, stageToGrant)) {
            player.displayClientMessage(
                    Component.literal("You already have stage: " + stageToGrant)
                            .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.pass(held);
        }
        if (!addStage(player, stageToGrant)) {
            player.displayClientMessage(
                    Component.literal("GameStages mod is missing — cannot unlock stage.")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.pass(held);
        }

        player.displayClientMessage(
                Component.literal("✔ Stage unlocked: " + stageToGrant)
                        .withStyle(ChatFormatting.GREEN), false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);

        if (!player.getAbilities().instabuild) held.shrink(1);
        return InteractionResultHolder.consume(held);
    }

    private static boolean hasStage(Player player, String stage) {
        try {
            Class<?> helperCls = Class.forName("net.darkhax.gamestages.GameStageHelper");
            return (boolean) helperCls.getMethod("hasStage", Player.class, String.class)
                    .invoke(null, player, stage);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static boolean addStage(Player player, String stage) {
        if (!(player instanceof ServerPlayer sp)) return false;
        try {
            Class<?> helperCls = Class.forName("net.darkhax.gamestages.GameStageHelper");
            // GameStages 1.20.1: addStage(ServerPlayer, String...) — varargs,
            // reflected as String[].class.
            helperCls.getMethod("addStage", ServerPlayer.class, String[].class)
                    .invoke(null, sp, new String[]{stage});
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            org.slf4j.LoggerFactory.getLogger("soa_additions/stage_item")
                    .warn("Failed to grant stage {}: {}", stage, e.getMessage());
            return false;
        }
    }
}
