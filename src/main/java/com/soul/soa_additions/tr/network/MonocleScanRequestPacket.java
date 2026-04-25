package com.soul.soa_additions.tr.network;

import com.soul.soa_additions.tr.TrItems;
import com.soul.soa_additions.tr.item.MonocleAccess;
import com.soul.soa_additions.tr.knowledge.ScannedTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Client → server: requests a scan of the named target. Three target kinds:
 * BLOCK (with optional position so we can drill into containers), ITEM (the
 * pure item-tooltip path), ENTITY (mob looked at via raycast).
 *
 * <p>Server-side validation:
 * <ul>
 *   <li>Player must hold an Arcane Monocle in their inventory or curio slots.</li>
 *   <li>For BLOCK with a position, the block must actually exist there
 *       (nothing fancy — just sanity); the position must be within 8 blocks
 *       of the player (slack for net jitter; client-side limit is 4).</li>
 *   <li>For ENTITY, the entity must exist in the player's level and be
 *       within 8 blocks of the player.</li>
 * </ul>
 *
 * <p>Side effects on a successful new scan:
 * <ul>
 *   <li>Plays the XP-pickup sound at the player.</li>
 *   <li>For containers (BlockEntity that is also a {@link Container}), every
 *       non-empty stack inside is also passed to scanItem — single ding,
 *       multiple aspects unlocked.</li>
 * </ul>
 */
public final class MonocleScanRequestPacket {

    public enum Kind { ITEM, BLOCK, ENTITY }

    private final Kind kind;
    private final ResourceLocation targetId;
    /** For BLOCK only — position of the world block to drill containers from.
     *  null means "ignore position, just scan the block id". */
    private final BlockPos pos;
    /** For ENTITY only — the entity id (network id) for in-world lookup. */
    private final int entityNetId;

    public MonocleScanRequestPacket(Kind kind, ResourceLocation targetId, BlockPos pos, int entityNetId) {
        this.kind = kind;
        this.targetId = targetId;
        this.pos = pos;
        this.entityNetId = entityNetId;
    }

    public static MonocleScanRequestPacket item(ResourceLocation id) {
        return new MonocleScanRequestPacket(Kind.ITEM, id, null, -1);
    }
    public static MonocleScanRequestPacket block(ResourceLocation id, BlockPos pos) {
        return new MonocleScanRequestPacket(Kind.BLOCK, id, pos, -1);
    }
    public static MonocleScanRequestPacket entity(ResourceLocation id, int netId) {
        return new MonocleScanRequestPacket(Kind.ENTITY, id, null, netId);
    }

    public static void encode(MonocleScanRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.kind.ordinal());
        buf.writeResourceLocation(pkt.targetId);
        buf.writeBoolean(pkt.pos != null);
        if (pkt.pos != null) buf.writeBlockPos(pkt.pos);
        buf.writeVarInt(pkt.entityNetId);
    }

    public static MonocleScanRequestPacket decode(FriendlyByteBuf buf) {
        Kind k = Kind.values()[buf.readByte() & 0xFF];
        ResourceLocation id = buf.readResourceLocation();
        BlockPos p = buf.readBoolean() ? buf.readBlockPos() : null;
        int netId = buf.readVarInt();
        return new MonocleScanRequestPacket(k, id, p, netId);
    }

    public static void handle(MonocleScanRequestPacket pkt, Supplier<NetworkEvent.Context> ctxRef) {
        NetworkEvent.Context ctx = ctxRef.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                com.soul.soa_additions.tr.ThaumicRemnants.LOG.info("[monocle/srv] packet received but sender null");
                return;
            }
            boolean has = MonocleAccess.hasMonocle(player, TrItems.ARCANE_MONOCLE.get());
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/srv] packet kind={} target={} from {} (hasMonocle={})",
                    pkt.kind, pkt.targetId, player.getName().getString(), has);
            if (!has) return;

            switch (pkt.kind) {
                case ITEM -> handleItem(player, pkt.targetId);
                case BLOCK -> handleBlock(player, pkt.targetId, pkt.pos);
                case ENTITY -> handleEntity(player, pkt.targetId, pkt.entityNetId);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleItem(ServerPlayer player, ResourceLocation id) {
        boolean newly = ScannedTargets.scanItem(player, id);
        com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                "[monocle/srv] scanItem({}) -> newly={}", id, newly);
        if (newly) playDing(player);
    }

    private static void handleBlock(ServerPlayer player, ResourceLocation id, BlockPos pos) {
        if (pos != null && pos.distSqr(player.blockPosition()) > 8 * 8 * 3) {
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/srv] block {} rejected: too far ({}>192)",
                    id, pos.distSqr(player.blockPosition()));
            return;
        }
        Block reg = ForgeRegistries.BLOCKS.getValue(id);
        if (reg == null) {
            com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                    "[monocle/srv] block {} not in registry", id);
            return;
        }
        boolean dinged = false;
        boolean newly = ScannedTargets.scanBlock(player, reg);
        com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                "[monocle/srv] scanBlock({}) -> newly={}", id, newly);
        if (newly) {
            playDing(player);
            dinged = true;
        }
        // Container drilldown: if the world block at this position has an
        // inventory, scan everything inside. Single ding even if multiple
        // items are newly scanned — chord of aspects unlocking, one cue.
        if (pos != null && player.serverLevel().isLoaded(pos)) {
            BlockEntity be = player.serverLevel().getBlockEntity(pos);
            if (be instanceof Container container) {
                boolean anyNew = false;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.isEmpty()) continue;
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (ScannedTargets.scanItem(player, itemId)) anyNew = true;
                }
                if (anyNew && !dinged) playDing(player);
            }
        }
    }

    private static void handleEntity(ServerPlayer player, ResourceLocation id, int netId) {
        // Two-path validation:
        //  - If netId resolves to a nearby entity, validate against that entity.
        //  - Else fall back to type-id validation only — Jade tooltips can
        //    target entities the server has briefly despawned, and the
        //    distance check at 32 blocks (vs vanilla 8 reach) gives Jade's
        //    extended reach plenty of slack.
        EntityType<?> type;
        Entity e = player.serverLevel().getEntity(netId);
        if (e != null) {
            if (e.distanceToSqr(player) > 32 * 32) {
                com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                        "[monocle/srv] entity {} rejected: too far ({}>1024)",
                        id, e.distanceToSqr(player));
                return;
            }
            type = e.getType();
            ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (typeId == null || !typeId.equals(id)) {
                com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                        "[monocle/srv] entity netId {} type mismatch (got {}, want {})",
                        netId, typeId, id);
                return;
            }
        } else {
            type = ForgeRegistries.ENTITY_TYPES.getValue(id);
            if (type == null) {
                com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                        "[monocle/srv] entity {} not in registry", id);
                return;
            }
        }
        boolean newly = ScannedTargets.scanEntity(player, type);
        com.soul.soa_additions.tr.ThaumicRemnants.LOG.info(
                "[monocle/srv] scanEntity({}) -> newly={}", id, newly);
        if (newly) playDing(player);
    }

    private static void playDing(ServerPlayer player) {
        player.serverLevel().playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.5f, 1.4f);
    }
}
