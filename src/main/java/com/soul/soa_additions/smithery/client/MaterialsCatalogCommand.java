package com.soul.soa_additions.smithery.client;

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
 * Registers <code>/soa materials</code>, which opens {@link MaterialsCatalogScreen}.
 *
 * <p>Client-side so opening the screen needs no packet round-trip. Forge tries the client
 * dispatcher first, so this is the path that normally runs. A client-only command is invisible
 * to the command tree the server sends, though, which costs tab-completion and leaves nothing
 * to fall back on if the client dispatcher misses it — so
 * {@code MaterialsCatalogServerCommand} registers the same path server-side and opens the screen
 * over {@code MaterialsCatalogOpenPacket}. Keep the two in sync.</p>
 *
 * <p>Ungated, and listed in {@code AntiCheatHandler.SAFE_SOA_SUBPATHS} — the anti-cheat treats
 * any OP-run command as a cheat unless whitelisted, and this one is a read-only reference UI
 * that grants nothing.</p>
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class MaterialsCatalogCommand {

    private MaterialsCatalogCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        if (!ModList.get().isLoaded("smithery")) return;
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("soa")
                .then(Commands.literal("materials").executes(ctx -> {
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new MaterialsCatalogScreen()));
                    return 1;
                })));
    }
}
