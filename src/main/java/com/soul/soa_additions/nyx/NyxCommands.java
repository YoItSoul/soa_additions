package com.soul.soa_additions.nyx;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.soul.soa_additions.nyx.entity.FallingMeteorEntity;
import com.soul.soa_additions.nyx.event.LunarEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.soul.soa_additions.SoaAdditions.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NyxCommands {

    private static final SuggestionProvider<CommandSourceStack> EVENT_SUGGEST = (ctx, b) -> {
        ServerLevel lvl = ctx.getSource().getLevel();
        NyxWorldData data = NyxWorldData.get(lvl);
        if (data != null) {
            for (LunarEvent e : data.lunarEvents) b.suggest(e.name);
        }
        b.suggest("clear");
        return b.buildFuture();
    };

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("nyxforce")
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(EVENT_SUGGEST)
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "event");
                            ServerLevel lvl = ctx.getSource().getLevel();
                            NyxWorldData data = NyxWorldData.get(lvl);
                            if (data == null) return 0;
                            if ("clear".equals(name)) {
                                data.forcedEvent = null;
                                ctx.getSource().sendSuccess(() -> Component.translatable("command.soa_additions.nyx.force.clear"), true);
                            } else {
                                LunarEvent e = data.findEventByName(name);
                                if (e == null) {
                                    ctx.getSource().sendFailure(Component.translatable("command.soa_additions.nyx.force.invalid", name));
                                    return 0;
                                }
                                data.forcedEvent = e;
                                ctx.getSource().sendSuccess(() -> Component.translatable("command.soa_additions.nyx.force.success", name), true);
                            }
                            data.setDirty();
                            return 1;
                        })));

        d.register(Commands.literal("nyxmeteor")
                .requires(s -> s.hasPermission(2))
                .executes(ctx -> spawnMeteor(ctx.getSource(),
                        ctx.getSource().getPosition().x, ctx.getSource().getPosition().z, null, false))
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                .executes(ctx -> spawnMeteor(ctx.getSource(),
                                        DoubleArgumentType.getDouble(ctx, "x"),
                                        DoubleArgumentType.getDouble(ctx, "z"), null, false))
                                .then(Commands.argument("size", IntegerArgumentType.integer(1, 10))
                                        .executes(ctx -> spawnMeteor(ctx.getSource(),
                                                DoubleArgumentType.getDouble(ctx, "x"),
                                                DoubleArgumentType.getDouble(ctx, "z"),
                                                IntegerArgumentType.getInteger(ctx, "size"), false))
                                        .then(Commands.argument("homing", BoolArgumentType.bool())
                                                .executes(ctx -> spawnMeteor(ctx.getSource(),
                                                        DoubleArgumentType.getDouble(ctx, "x"),
                                                        DoubleArgumentType.getDouble(ctx, "z"),
                                                        IntegerArgumentType.getInteger(ctx, "size"),
                                                        BoolArgumentType.getBool(ctx, "homing"))))))));
    }

    private static int spawnMeteor(CommandSourceStack src, double x, double z, Integer size, boolean homing) {
        ServerLevel lvl = src.getLevel();
        BlockPos at = BlockPos.containing(x, 0, z);
        FallingMeteorEntity m = FallingMeteorEntity.spawn(lvl, at);
        if (size != null) m.setSize(size);
        m.homing = homing;
        BlockPos spawnPos = m.blockPosition();
        src.sendSuccess(() -> Component.translatable("command.soa_additions.nyx.meteor.success",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()), true);
        return 1;
    }

    private NyxCommands() {}
}
