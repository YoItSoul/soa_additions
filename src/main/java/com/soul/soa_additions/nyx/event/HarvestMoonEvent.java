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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

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

        // Iterate loaded chunks around each player without ever waiting on one.
        //
        // `getChunk(x, z, FULL, false)` is NOT safe here, despite appearances:
        // load=false only skips *creating* a ticket. The call still routes into
        // ServerChunkCache.getChunkBlocking, which runs managedBlock on the
        // future of any ChunkHolder that already exists — so whenever a chunk is
        // mid-generation, which is constantly true while a player travels, this
        // parks the server thread. ModernFix's watchdog caught it holding a
        // single tick for over 40 seconds, 17x17 chunks deep per player.
        //
        // getChunkNow returns null rather than waiting, and every lookup below
        // goes through the chunk we already hold, so nothing can re-enter the
        // chunk source and re-introduce the stall.
        for (var p : level.players()) {
            int cx = p.chunkPosition().x, cz = p.chunkPosition().z;
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                    if (chunk == null) continue;
                    for (int i = 0; i < grow; i++) {
                        int wx = (cx + dx) * 16 + level.random.nextInt(16);
                        int wz = (cz + dz) * 16 + level.random.nextInt(16);
                        BlockPos pos = new BlockPos(
                                wx,
                                chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, wx, wz),
                                wz);
                        BlockState state = chunk.getBlockState(pos);
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
