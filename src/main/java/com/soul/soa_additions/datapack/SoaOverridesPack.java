package com.soul.soa_additions.datapack;

import com.mojang.logging.LogUtils;
import com.soul.soa_additions.SoaAdditions;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;

import java.nio.file.Path;

/**
 * Registers the {@code soa_overrides} folder inside this jar as a FORCED,
 * top-priority server datapack.
 *
 * <p>Why: several GC-parity fixes must SHADOW data files that other mods ship
 * inside their own jars (ScalingHealth {@code sh_mechanics/difficulty.json}
 * max difficulty 250→2400, ProgressiveBosses wither/dragon stats, Terralith's
 * first-join promo function). Those overrides originally lived in
 * {@code kubejs/data}, but the KubeJS virtual pack was observed NOT to shadow
 * mod-jar data files at initial world load (SH kept its jar default
 * maxValue=250 — visible as the difficulty bar capping at 250). A
 * mod-registered pack with {@link Pack.Position#TOP} + required=true is
 * appended at the high-priority end of the enabled list deterministically,
 * on both fresh worlds and existing saves ("loading it automatically").</p>
 */
public final class SoaOverridesPack {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PACK_ID = "soa_additions:overrides";

    private SoaOverridesPack() {}

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        Path path = ModList.get().getModFileById(SoaAdditions.MODID).getFile().findResource("soa_overrides");
        Pack pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal("SoA GC-Parity Overrides"),
                true, // required → always enabled, players can't disable it
                id -> new PathPackResources(id, path, true),
                PackType.SERVER_DATA,
                Pack.Position.TOP,
                PackSource.BUILT_IN);
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
            LOGGER.info("Registered forced override datapack {}", PACK_ID);
        } else {
            LOGGER.error("Failed to read soa_overrides pack metadata - GC-parity overrides will NOT apply!");
        }
    }
}
