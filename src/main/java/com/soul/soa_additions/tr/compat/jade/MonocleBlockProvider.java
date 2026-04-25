package com.soul.soa_additions.tr.compat.jade;

import com.soul.soa_additions.tr.TrItems;
import com.soul.soa_additions.tr.core.AspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.item.MonocleAccess;
import com.soul.soa_additions.tr.knowledge.ClientKnownAspects;
import com.soul.soa_additions.tr.knowledge.ClientScannedTargets;
import com.soul.soa_additions.tr.network.MonocleScanRequestPacket;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Jade block-tooltip provider: when the player is wearing/carrying an Arcane
 * Monocle, this appends the looked-at block's aspect composition to its Jade
 * overlay. Same gates as the inventory tooltip:
 * <ul>
 *   <li>If the block isn't in the player's scanned set, fires a scan request
 *       (debounced per block id) and skips drawing this frame — the next
 *       frame after the server replies will render the aspects.</li>
 *   <li>If scanned, renders only the aspects the player has discovered;
 *       compounds they don't know yet stay hidden.</li>
 * </ul>
 *
 * <p>Keeping this on the Jade side rather than in a custom HUD means we get
 * positioning, theming, and toggleability from Jade for free.
 */
public final class MonocleBlockProvider implements IBlockComponentProvider {

    public static final MonocleBlockProvider INSTANCE = new MonocleBlockProvider();

    /** Per-block-id debounce so we send at most one scan packet per id while
     *  the player keeps looking at it. Cleared when the player stops carrying
     *  the monocle so re-equipping re-arms the trigger. */
    private static final Set<ResourceLocation> SCAN_REQUESTED = new HashSet<>();
    private static boolean wasCarrying = false;

    private MonocleBlockProvider() {}

    @Override
    public ResourceLocation getUid() {
        return JadeMonoclePlugin.MONOCLE_UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean carrying = MonocleAccess.hasMonocle(mc.player, TrItems.ARCANE_MONOCLE.get());
        if (!carrying && wasCarrying) SCAN_REQUESTED.clear();
        wasCarrying = carrying;
        if (!carrying) return;

        Block block = accessor.getBlock();
        if (block == null) return;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null) return;

        // TC4-style: every looked-at block gets scanned (once per session per
        // id), regardless of whether we have aspect data on file. The ding
        // plays even for empty-aspect blocks so the player gets feedback the
        // gaze was registered. The Scanning... placeholder + scan trigger
        // also fire when we DO have aspect data but haven't scanned yet.
        if (!ClientScannedTargets.has(block) && SCAN_REQUESTED.add(blockId)) {
            BlockPos pos = accessor.getPosition();
            TrNetworking.CHANNEL.sendToServer(MonocleScanRequestPacket.block(blockId, pos));
        }

        // Render aspect line only when we have data AND it's been scanned AND
        // the player knows at least one aspect from the composition.
        List<AspectStack> all = AspectMap.forBlock(block);
        if (all.isEmpty()) return;
        if (!ClientScannedTargets.has(block)) {
            tooltip.add(Component.literal("Scanning...")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        List<AspectStack> known = AspectMap.filter(all, ClientKnownAspects::has);
        if (known.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < known.size(); i++) {
            AspectStack as = known.get(i);
            if (i > 0) sb.append("  ");
            sb.append(as.aspect().englishName()).append(" ×").append(as.amount());
        }
        tooltip.add(Component.literal(sb.toString())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
