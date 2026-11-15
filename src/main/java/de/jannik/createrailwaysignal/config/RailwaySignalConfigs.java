package de.jannik.createrailwaysignal.config;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.util.math.Vec3d;
import de.jannik.createrailwaysignal.util.TrackCantMath;

/**
 * Registers CreateRailwaySignal's Forge-style config(s), mirroring Create's own
 * {@code AllConfigs} registration pattern.
 */
public class RailwaySignalConfigs {

    private static CRailwaySignalClientConfig client;
    private static CRailwaySignalCommonConfig common;
    private static CRailwaySignalServerConfig server;
    private static BankingSettings serverBanking;

    public static CRailwaySignalClientConfig client() {
        return client;
    }

    public static CRailwaySignalCommonConfig common() {
        return common;
    }

    public static CRailwaySignalServerConfig server() {
        return server;
    }

    public static void setServerBanking(BankingSettings settings) {
        serverBanking = settings;
    }

    public static BankingSettings banking() {
        BankingSettings personal = new BankingSettings(false, client == null || client.tiltRiders.get(),
                client != null && client.invertTrackBanking.get(),
                client != null && client.invertBogeyBanking.get(),
                client != null && client.invertBodyBanking.get());
        return serverBanking == null ? personal : serverBanking.effective(personal);
    }

    public static Vec3d renderedTrackNormal(Vec3d normal, Vec3d tangent) {
        return TrackCantMath.renderedNormal(normal, tangent, banking().invertTrack());
    }

    public static float renderedBogeyRoll(float roll) {
        return banking().invertBogey() ? -roll : roll;
    }

    public static float renderedBodyRoll(float roll) {
        return banking().invertBody() ? -roll : roll;
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
        common = register(CRailwaySignalCommonConfig::new, ModConfig.Type.COMMON);
        server = register(CRailwaySignalServerConfig::new, ModConfig.Type.SERVER);
        BankingSettingsNetworking.register();
    }

    public static void registerClient() {
        client = register(CRailwaySignalClientConfig::new, ModConfig.Type.CLIENT);
    }
}
