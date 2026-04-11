package com.soul.soa_additions.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * OSHI-backed deep hardware probe. OSHI ships with Minecraft (used by F3), so
 * no new dependencies. First invocation is ~200-500ms — results are cached
 * for the lifetime of the JVM so heartbeats pay nothing.
 */
public final class HardwareProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger("soa_additions/hw");

    private static volatile Telemetry.HardwareInfo cached;

    private HardwareProbe() {}

    public static Telemetry.HardwareInfo get() {
        Telemetry.HardwareInfo c = cached;
        if (c != null) return c;
        synchronized (HardwareProbe.class) {
            if (cached != null) return cached;
            try {
                cached = probe();
            } catch (Throwable t) {
                LOGGER.debug("OSHI probe failed (ignored): {}", t.toString());
                cached = new Telemetry.HardwareInfo();
            }
            return cached;
        }
    }

    private static Telemetry.HardwareInfo probe() {
        Telemetry.HardwareInfo h = new Telemetry.HardwareInfo();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        // CPU
        try {
            CentralProcessor cpu = hal.getProcessor();
            CentralProcessor.ProcessorIdentifier id = cpu.getProcessorIdentifier();
            Telemetry.CpuInfo c = new Telemetry.CpuInfo();
            c.name = id.getName();
            c.identifier = id.getIdentifier();
            c.vendor = id.getVendor();
            c.microarchitecture = id.getMicroarchitecture();
            c.model = id.getModel();
            c.family = id.getFamily();
            c.stepping = id.getStepping();
            c.is_64bit = id.isCpu64bit();
            c.physical_package_count = cpu.getPhysicalPackageCount();
            c.physical_core_count = cpu.getPhysicalProcessorCount();
            c.logical_core_count = cpu.getLogicalProcessorCount();
            c.max_freq_hz = cpu.getMaxFreq();
            h.cpu = c;
        } catch (Throwable ignored) {}

        // Memory (total + per-DIMM)
        try {
            GlobalMemory mem = hal.getMemory();
            h.memory_total_mb = mem.getTotal() / (1024 * 1024);
            h.memory_available_mb = mem.getAvailable() / (1024 * 1024);
            h.memory_page_size = mem.getPageSize();

            List<Telemetry.MemoryStickInfo> sticks = new ArrayList<>();
            for (PhysicalMemory pm : mem.getPhysicalMemory()) {
                Telemetry.MemoryStickInfo s = new Telemetry.MemoryStickInfo();
                s.bank_label = pm.getBankLabel();
                s.capacity_mb = pm.getCapacity() / (1024 * 1024);
                s.clock_speed_hz = pm.getClockSpeed();
                s.manufacturer = pm.getManufacturer();
                s.memory_type = pm.getMemoryType();
                sticks.add(s);
            }
            h.memory_sticks = sticks;
        } catch (Throwable ignored) {}

        // Computer system / motherboard
        try {
            ComputerSystem cs = hal.getComputerSystem();
            Telemetry.SystemInfoBlock sys = new Telemetry.SystemInfoBlock();
            sys.manufacturer = cs.getManufacturer();
            sys.model = cs.getModel();
            if (cs.getBaseboard() != null) {
                sys.board_manufacturer = cs.getBaseboard().getManufacturer();
                sys.board_model = cs.getBaseboard().getModel();
                sys.board_version = cs.getBaseboard().getVersion();
            }
            h.system = sys;
        } catch (Throwable ignored) {}

        // GPUs (OSHI sees *all* adapters, not just the GL-active one)
        try {
            List<Telemetry.GpuCardInfo> gpus = new ArrayList<>();
            for (GraphicsCard g : hal.getGraphicsCards()) {
                Telemetry.GpuCardInfo gi = new Telemetry.GpuCardInfo();
                gi.name = g.getName();
                gi.vendor = g.getVendor();
                gi.vram_mb = g.getVRam() / (1024 * 1024);
                gi.version_info = g.getVersionInfo();
                gpus.add(gi);
            }
            h.gpus = gpus;
        } catch (Throwable ignored) {}

        // Disks (sizes only; no file paths, no labels)
        try {
            List<Telemetry.DiskInfo> disks = new ArrayList<>();
            for (HWDiskStore d : hal.getDiskStores()) {
                Telemetry.DiskInfo di = new Telemetry.DiskInfo();
                di.model = d.getModel();
                di.size_mb = d.getSize() / (1024 * 1024);
                disks.add(di);
            }
            h.disks = disks;
        } catch (Throwable ignored) {}

        // OS extras (family, bitness)
        try {
            OperatingSystem os = si.getOperatingSystem();
            h.os_family = os.getFamily();
            h.os_manufacturer = os.getManufacturer();
            h.os_version_build = os.getVersionInfo() != null ? os.getVersionInfo().toString() : null;
            h.os_bitness = os.getBitness();
        } catch (Throwable ignored) {}

        return h;
    }
}
