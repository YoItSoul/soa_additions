package com.soul.soa_additions.nyx.event;

import com.soul.soa_additions.nyx.NyxConfig;
import com.soul.soa_additions.nyx.NyxWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

public class HarvestMoonEvent extends LunarEvent {

    public final Counter counter = new Counter(
            () -> NyxConfig.HARVEST_MOON_CHANCE.get(),
            () -> NyxConfig.HARVEST_MOON_START_NIGHT.get(),
            () -> NyxConfig.HARVEST_MOON_GRACE.get(),
            () -> NyxConfig.HARVEST_MOON_INTERVAL.get());

    public HarvestMoonEvent(NyxWorldData data) { super("harvest_moon", data); }

    @Override
    public Component getStartMessage() {
        return Component.translatable("info.soa_additions.harvest_moon")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (NyxConfig.HARVEST_MOON_ON_FULL.get() && level.getMoonBrightness() < 1.0f) return false;
        return lastDaytime && !NyxWorldData.isDaytime(level) && counter.canStart(data, this);
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) { return NyxWorldData.isDaytime(level); }

    @Override public int getSkyColor() { return 4145088; }

    @Override
    public void update(boolean lastDaytime) {
        counter.update(data, this, lastDaytime);
        if (data.currentEvent != this) return;
        int grow = NyxConfig.HARVEST_MOON_GROW_AMOUNT.get();
        int interval = NyxConfig.HARVEST_MOON_GROW_INTERVAL.get();
        if (grow <= 0 || level.getGameTime() % interval != 0L) return;

        // Iterate loaded chunks around each player. Use the non-loading
        // accessor: `getChunk(x, z, FULL, false)` returns null for unloaded
        // chunks instead of synchronously loading (and, with C2ME, parking
        // the server thread on the async loader). Using load=true here was
        // a latent stall — every harvest-moon tick could request 17×17×N
        // chunks per player, blocking the server while they generated.
        for (var p : level.players()) {
            int cx = p.chunkPosition().x, cz = p.chunkPosition().z;
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    ChunkAccess chunk = level.getChunkSource().getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
                    if (!(chunk instanceof LevelChunk)) continue;
                    for (int i = 0; i < grow; i++) {
                        int rx = level.random.nextInt(16);
                        int rz = level.random.nextInt(16);
                        BlockPos pos = new BlockPos(
                                (cx + dx) * 16 + rx,
                                level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (cx + dx) * 16 + rx, (cz + dz) * 16 + rz),
                                (cz + dz) * 16 + rz);
                        BlockState state = level.getBlockState(pos);
                        Block b = state.getBlock();
                        if (b instanceof BonemealableBlock growable
                                && !(b instanceof GrassBlock)
                                && !(b instanceof TallGrassBlock)
                                && !(b instanceof DoublePlantBlock)) {
                            try {
                                if (growable.isValidBonemealTarget(level, pos, state, false)) {
                                    growable.performBonemeal(level, level.random, pos, state);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
    }

    @Override public CompoundTag serialize() { return counter.serialize(); }
    @Override public void deserialize(CompoundTag tag) { counter.deserialize(tag); }
}
