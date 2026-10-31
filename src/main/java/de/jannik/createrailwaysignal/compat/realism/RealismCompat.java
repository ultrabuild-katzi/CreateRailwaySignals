package de.jannik.createrailwaysignal.compat.realism;

import net.fabricmc.loader.api.FabricLoader;

/** Soft-dependency marker for Create Realism. The mod works fine without it; when present, its
 * ETCS overlay also learns about our own {@link de.jannik.createrailwaysignal.graph.SpeedSignalBoundary}
 * speed limits (see {@link RealismMixinPlugin} and {@link RealismSpeedLimitScanner}). */
public final class RealismCompat {
    public static final String MODID = "realism";

    private RealismCompat() {}

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MODID);
    }
}
