package com.soul.soa_additions.loot.artifact;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Artifact runtime glue: registers the datapack def loader and blocks usage of
 * sealed artifacts (attack, block break, right-click) until unsealed.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID)
public final class ArtifactEvents {

    private ArtifactEvents() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ArtifactDefs());
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (blockSealed(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (ArtifactBuilder.isSealed(event.getEntity().getMainHandItem())) {
            event.setNewSpeed(0.0f);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (ArtifactBuilder.isSealed(event.getItemStack())) {
            event.setCanceled(true);
            notifySealed(event.getEntity());
        }
    }

    private static boolean blockSealed(Player player) {
        ItemStack main = player.getMainHandItem();
        if (ArtifactBuilder.isSealed(main)) {
            notifySealed(player);
            return true;
        }
        return false;
    }

    private static void notifySealed(Player player) {
        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("This artifact is sealed — craft it with an Artifact Unsealer.")
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }
}
