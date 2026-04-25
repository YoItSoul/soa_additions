package com.soul.soa_additions.tr.compat.jade;

import com.soul.soa_additions.tr.TrItems;
import com.soul.soa_additions.tr.aura.EntityAspectMap;
import com.soul.soa_additions.tr.core.AspectStack;
import com.soul.soa_additions.tr.item.MonocleAccess;
import com.soul.soa_additions.tr.knowledge.ClientKnownAspects;
import com.soul.soa_additions.tr.knowledge.ClientScannedTargets;
import com.soul.soa_additions.tr.network.MonocleScanRequestPacket;
import com.soul.soa_additions.tr.network.TrNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Jade entity-tooltip provider — mob equivalent of {@link MonocleBlockProvider}.
 * When the player is wearing/carrying an Arcane Monocle, looking at a mob
 * appends its aspect composition to the Jade overlay. First look fires a
 * scan request (and the XP-pickup ding); subsequent looks render the known
 * aspects directly.
 */
public final class MonocleEntityProvider implements IEntityComponentProvider {

    public static final MonocleEntityProvider INSTANCE = new MonocleEntityProvider();

    private static final Set<ResourceLocation> SCAN_REQUESTED = new HashSet<>();
    private static boolean wasCarrying = false;
    private static boolean diagFired = false;

    private MonocleEntityProvider() {}

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(com.soul.soa_additions.tr.ThaumicRemnants.MODID, "monocle_entity_aspects");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!diagFired) {
            diagFired = true;
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/jade-ent] appendTooltip fired for first entity");
        }

        boolean carrying = MonocleAccess.hasMonocle(mc.player, TrItems.ARCANE_MONOCLE.get());
        if (!carrying && wasCarrying) SCAN_REQUESTED.clear();
        wasCarrying = carrying;
        if (!carrying) return;

        Entity entity = accessor.getEntity();
        if (entity == null) return;
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (typeId == null) return;

        // JSON override is exclusive — if the entity has a JSON entry, use
        // ONLY those aspects. Heuristic only fires as a fallback when the
        // entity has no JSON (modded mobs, custom variants).
        List<AspectStack> all = EntityAspectMap.forType(entity.getType());
        if (all.isEmpty()) {
            java.util.Map<com.soul.soa_additions.tr.core.Aspect, Integer> sumMap = new java.util.HashMap<>();
            for (AspectStack as : inferEntityAspects(entity)) {
                sumMap.merge(as.aspect(), as.amount(), Integer::sum);
            }
            if (sumMap.isEmpty()) return;
            all = capAspects(sumMap, 5, 6);
        }

        // Unscanned → trigger scan once, render placeholder.
        if (!ClientScannedTargets.hasEntity(typeId)) {
            if (SCAN_REQUESTED.add(typeId)) {
                TrNetworking.CHANNEL.sendToServer(
                        MonocleScanRequestPacket.entity(typeId, entity.getId()));
            }
            tooltip.add(Component.literal("Scanning...")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        // Filter to known aspects only — same gating as items/blocks.
        java.util.List<AspectStack> known = new java.util.ArrayList<>(all.size());
        for (AspectStack as : all) {
            if (ClientKnownAspects.has(as.aspect())) known.add(as);
        }
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

    /** Heuristic fallback delegated to the rich, property-driven inference
     *  in {@link com.soul.soa_additions.tr.aspect.derive.EntityClassHeuristic}. */
    private static List<AspectStack> inferEntityAspects(Entity entity) {
        return com.soul.soa_additions.tr.aspect.derive.EntityClassHeuristic.infer(entity);
    }

    /** Cap a summed-aspect map: keeps the top {@code maxKinds} aspects by
     *  amount, clamps each amount to {@code maxAmount}. Stops bosses with
     *  rich JSON + heuristic summing into unreadable 12-aspect dumps. */
    private static List<AspectStack> capAspects(
            java.util.Map<com.soul.soa_additions.tr.core.Aspect, Integer> sum,
            int maxKinds, int maxAmount) {
        return sum.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(maxKinds)
                .map(e -> new AspectStack(e.getKey(), Math.min(maxAmount, e.getValue())))
                .toList();
    }
}
