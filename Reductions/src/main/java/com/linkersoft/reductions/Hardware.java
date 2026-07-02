package com.linkersoft.reductions;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.HWDiskStore;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public class Hardware {

    public static String getDeviceInfo() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            ComputerSystem computerSystem = hal.getComputerSystem();
            CentralProcessor processor = hal.getProcessor();
            GlobalMemory memory = hal.getMemory();
            List<GraphicsCard> graphicsCards = hal.getGraphicsCards();
            List<HWDiskStore> diskStores = hal.getDiskStores();

            StringBuilder sb = new StringBuilder();
            sb.append(">> <details>\n");
            sb.append(">> <summary>Device</summary>\n");
            sb.append(">> <ul>\n");

            // RAM
            String ramSize = FormatUtil.formatBytes(memory.getTotal());
            long maxFreq = memory.getPhysicalMemory().stream()
                    .mapToLong(pm -> pm.getClockSpeed())
                    .max()
                    .orElse(0);
            String ramInfo = "<strong>RAM:</strong> " + ramSize;
            if (maxFreq > 0) {
                ramInfo += " | " + FormatUtil.formatHertz(maxFreq);
            }
            sb.append(">> <li>").append(ramInfo).append("</li>\n");

            // CPU
            String cpuName = processor.getProcessorIdentifier().getName();
            int physicalCores = processor.getPhysicalProcessorCount();
            int logicalCores = processor.getLogicalProcessorCount();
            long maxCpuFreq = processor.getMaxFreq();
            String cpuInfo = "<strong>CPU:</strong> " + cpuName + " | " + physicalCores + "-CORES/" + logicalCores + "-THREADS";
            if (maxCpuFreq > 0) {
                 cpuInfo += " | " + FormatUtil.formatHertz(maxCpuFreq);
            }
            sb.append(">> <li>").append(cpuInfo).append("</li>\n");

            // GPU
            if (!graphicsCards.isEmpty()) {
                List<String> gpuList = new ArrayList<>();
                for (GraphicsCard gc : graphicsCards) {
                    gpuList.add(gc.getName() + " | " + FormatUtil.formatBytes(gc.getVRam()));
                }
                String gpuInfo = "<strong>GPU:</strong> " + String.join(", ", gpuList);
                 sb.append(">> <li>").append(gpuInfo).append("</li>\n");
            } else {
                 sb.append(">> <li><strong>GPU:</strong> N/A</li>\n");
            }

            // Motherboard
            String mobo = computerSystem.getBaseboard().getManufacturer() + " " + computerSystem.getBaseboard().getModel();
            sb.append(">> <li><strong>MOTHERBOARD:</strong> ").append(mobo).append("</li>\n");

            // Storage (SSD/HDD)
            List<String> disks = new ArrayList<>();
            for (HWDiskStore ds : diskStores) {
                disks.add(ds.getModel() + " " + FormatUtil.formatBytes(ds.getSize()));
            }
            
            if (!disks.isEmpty()) {
                 sb.append(">> <li><strong>STORAGE:</strong> ").append(String.join(", ", disks)).append("</li>\n");
            } else {
                 sb.append(">> <li><strong>STORAGE:</strong> N/A</li>\n");
            }

            sb.append(">> </ul>\n");
            sb.append(">> </details>");
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return ">> <details><summary>Device</summary>\n>> <ul><li><strong>Error:</strong> Unable to query device info: " + e.getMessage() + "</li></ul>\n>> </details>";
        } catch (NoClassDefFoundError e) {
             return ">> <details><summary>Device</summary>\n>> <ul><li><strong>Error:</strong> OSHI library not found/loaded.</li></ul>\n>> </details>";
        }
    }
}
