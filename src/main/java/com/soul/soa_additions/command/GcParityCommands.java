package com.soul.soa_additions.command;

import com.mojang.brigadier.context.CommandContext;
import com.soul.soa_additions.item.ModItems;
import com.soul.soa_additions.quest.task.GameStageTask;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 1:1 ports of GC's endgame admin commands (commands.zs):
 *
 * <ul>
 *   <li>{@code /executor [player]} — the Executor Terminal's backing command:
 *       iswuss holders get Access Denied, everyone else gets the abandoned
 *       programmer's message. (The terminal item calls the same logic
 *       directly; the command exists for admin/parity use.)</li>
 *   <li>{@code /infinitykill [player]} — GC's admin prank/judgement: true
 *       heroes (and creative players) shrug it off with a burst of
 *       resistance/strength (+regen for heroes); everyone else gets Infinity
 *       Stones strapped into their head and chest armor slots.</li>
 * </ul>
 */
public final class GcParityCommands {

    private GcParityCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("executor")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(GcParityCommands::runExecutor)));

        event.getDispatcher().register(
                Commands.literal("infinitykill")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> infinityKill(ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(GcParityCommands::runInfinityKill)));
    }

    private static int runExecutor(CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        int n = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(ctx, "player")) {
            String key = GameStageTask.hasStage(player, "iswuss")
                    ? "greedycraft.command.executorCommand.deny"
                    : "greedycraft.command.executorCommand.message";
            player.sendSystemMessage(Component.translatable(key));
            n++;
        }
        return n;
    }

    private static int runInfinityKill(CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        int n = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(ctx, "player")) {
            n += infinityKill(player);
        }
        return n;
    }

    private static int infinityKill(ServerPlayer player) {
        boolean hero = GameStageTask.hasStage(player, "truehero")
                && !GameStageTask.hasStage(player, "iswuss");
        if (player.isCreative() || hero) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 4, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, 10, false, false));
            if (hero) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 4, false, false));
            }
            return 1;
        }
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.INFINITY_STONE.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.INFINITY_STONE.get()));
        return 1;
    }
}
