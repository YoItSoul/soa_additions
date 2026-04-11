package com.soul.soa_additions.worldgen;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Places the one-time grove shrine near world spawn the first time the server starts. State is
 * persisted via {@link ShrineData} in the overworld's dimension data storage.
 */
@Mod.EventBusSubscriber(modid = SoaAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModWorldEvents {

    private ModWorldEvents() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        ShrineData shrineData = ShrineData.get(level);
        if (shrineData.isShrinePlaced()) return;

        BlockPos spawnPos = level.getSharedSpawnPos();
        ModGrovePlacement.placeShrineNearSpawn(level, spawnPos);
        shrineData.setShrinePlaced(true);
        shrineData.setDirty();
    }

    public static final class ShrineData extends SavedData {

        private static final String DATA_NAME = "soa_additions_shrine_data";

        private boolean shrinePlaced = false;

        public boolean isShrinePlaced() {
            return shrinePlaced;
        }

        public void setShrinePlaced(boolean placed) {
            this.shrinePlaced = placed;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean("shrinePlaced", shrinePlaced);
            return tag;
        }

        public static ShrineData load(CompoundTag tag) {
            ShrineData data = new ShrineData();
            data.shrinePlaced = tag.getBoolean("shrinePlaced");
            return data;
        }

        public static ShrineData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(ShrineData::load, ShrineData::new, DATA_NAME);
        }
    }
}
