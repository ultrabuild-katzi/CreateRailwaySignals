package de.jannik.createrailwaysignal.compat.simplemodconfig;

import net.fabricmc.loader.api.FabricLoader;

/** Soft-dependency marker for the "Simple Mod Config" library mod. We only ever need to know
 * whether it's present so {@link SimpleModConfigMixinPlugin} can skip its workaround mixin when
 * the (optional) mod is absent. */
public final class SimpleModConfigCompat {
    public static final String MODID = "simplemodconfig";

    private SimpleModConfigCompat() {}

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(MODID);
    }
}
