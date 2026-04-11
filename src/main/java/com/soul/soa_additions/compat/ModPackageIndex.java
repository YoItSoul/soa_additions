package com.soul.soa_additions.compat;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Maps Java package prefixes → mod ids by reading every loaded mod's {@code @Mod} annotation
 * scan data. Built once on first use; lookups are O(log n) via {@link NavigableMap#floorEntry}.
 *
 * <p>Used by the sampling-based profilers ({@link StartupProfiler}, {@link TickAttribution},
 * {@link ChunkLoadAttribution}) to attribute a stack frame back to the mod that owns it
 * without bytecode instrumentation.
 */
public final class ModPackageIndex {

    private static final NavigableMap<String, String> PREFIX_TO_MOD = new TreeMap<>();
    private static volatile boolean built;

    private ModPackageIndex() {}

    public static synchronized void buildIfNeeded() {
        if (built) return;
        for (IModInfo info : ModList.get().getMods()) {
            try {
                ModFileScanData scan = info.getOwningFile().getFile().getScanResult();
                for (ModFileScanData.AnnotationData a : scan.getAnnotations()) {
                    String type = a.annotationType().getClassName();
                    if (!type.equals("net.minecraftforge.fml.common.Mod")) continue;
                    String cls = a.clazz().getClassName();
                    int dot = cls.lastIndexOf('.');
                    if (dot > 0) PREFIX_TO_MOD.putIfAbsent(cls.substring(0, dot), info.getModId());
                }
            } catch (Throwable ignored) {
            }
        }
        built = true;
    }

    /** @return modid that owns {@code className}, or {@code null} if it belongs to vanilla / forge / unknown. */
    public static String lookup(String className) {
        if (!built) buildIfNeeded();
        if (className == null) return null;
        Map.Entry<String, String> e = PREFIX_TO_MOD.floorEntry(className);
        if (e == null) return null;
        return className.startsWith(e.getKey() + ".") || className.equals(e.getKey()) ? e.getValue() : null;
    }
}
