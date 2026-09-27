package de.jannik.createrailwaysignal.config;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Registers CreateRailwaySignal's Forge-style config(s), mirroring Create's own
 * {@code AllConfigs} registration pattern.
 */
public class RailwaySignalConfigs {

    private static CRailwaySignalClientConfig client;

    public static CRailwaySignalClientConfig client() {
        return client;
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        ForgeConfigRegistry.INSTANCE.register(Createrailwaysignal.MOD_ID, side, config.specification);
        return config;
    }

    public static void register() {
        client = register(CRailwaySignalClientConfig::new, ModConfig.Type.CLIENT);
    }
}
