package com.soul.soa_additions.donor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Client-side cache of the donor list, populated by {@link DonorSyncPacket}.
 * Used by the donor wall GUI and the chat glow formatter.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientDonorCache {

    private static volatile List<DonorData> donors = List.of();
    private static volatile Map<UUID, DonorData> byUuid = Map.of();

    private ClientDonorCache() {}

    public static void apply(List<DonorData> list) {
        donors = List.copyOf(list);
        Map<UUID, DonorData> map = new HashMap<>();
        for (DonorData d : list) map.put(d.uuid(), d);
        byUuid = Map.copyOf(map);
    }

    public static List<DonorData> all() { return donors; }

    public static boolean isDonor(UUID uuid) { return byUuid.containsKey(uuid); }

    public static Optional<DonorData> get(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    public static void clear() {
        donors = List.of();
        byUuid = Map.of();
    }
}
