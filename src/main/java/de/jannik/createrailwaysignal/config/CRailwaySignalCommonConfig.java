package de.jannik.createrailwaysignal.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

/**
 * Common (server-authoritative) settings for CreateRailwaySignal, registered through the
 * Create-style ConfigBase/ForgeConfigSpec system, the same way the client config is.
 */
public class CRailwaySignalCommonConfig extends ConfigBase {

    public final ConfigGroup preLightSignal = group(0, "preLightSignal", Comments.preLightSignal);

    public final ConfigInt preLightSignalRange = i(500, 0, 10000, "preLightSignalRange", Comments.preLightSignalRange);

    @Override
    @NotNull
    public String getName() {
        return "common";
    }

    private static class Comments {
        static String preLightSignal = "Settings for the Pre Light Signal block's train-proximity preview.";
        static String preLightSignalRange = "How far ahead (in blocks, along the track graph) a Pre Light Signal looks for an "
                + "automated train and the next speed boundary on its path before showing a number.";
    }
}
