package com.soul.soa_additions.tconstructevo.client.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers <code>/soa materials</code> — a client-only command that opens
 * {@link MaterialsCatalogScreen}. Client-side so no network round-trip is
 * needed; opening a screen from a server command would require a packet.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MaterialsCatalogCommand {

    private MaterialsCatalogCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        if (!ModList.get().isLoaded("tconstruct")) return;
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("soa")
                .then(Commands.literal("materials").executes(ctx -> {
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new MaterialsCatalogScreen()));
                    return 1;
                })));
    }
}
