package de.jannik.createrailwaysignal.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Client-side camera configuration.
 * Stored at: .minecraft/config/createrailwaysignal-camera.properties
 */
public final class CameraConfig {
    private static final File FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("createrailwaysignal-camera.properties").toFile();
    private static final Properties PROPS = new Properties();

    /** Feature toggle: when true, Create's zoom-in while mounted is blocked. */
    private static boolean unzoomEnabled = false; // default OFF

    /** One-time bypass to allow a single reset() to pass (used by /camera reset). */
    private static volatile boolean normalizeOnce = false;

    private CameraConfig() {}

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (var reader = Files.newBufferedReader(FILE.toPath())) {
            PROPS.load(reader);
            unzoomEnabled = Boolean.parseBoolean(PROPS.getProperty("unzoomEnabled", "false"));
        } catch (IOException ignored) {
            // Keep defaults if file is missing/corrupt; do not spam console in release builds.
        }
    }

    public static void save() {
        try (var writer = Files.newBufferedWriter(FILE.toPath())) {
            PROPS.setProperty("unzoomEnabled", Boolean.toString(unzoomEnabled));
            PROPS.store(writer, "CreateRailwaySignal camera settings");
        } catch (IOException ignored) {
            // Silent fail to avoid noisy logs in user setups.
        }
    }

    public static boolean isUnzoomEnabled() {
        return unzoomEnabled;
    }

    public static void setUnzoomEnabled(boolean value) {
        unzoomEnabled = value;
        save();
    }

    /** Resets the persisted config to default (feature OFF). */
    public static void resetConfig() {
        unzoomEnabled = false;
        save();
    }

    /** Request a single normalize pass-through for Create's reset(). */
    public static void requestNormalizeOnce() {
        normalizeOnce = true;
    }

    /** Returns true exactly once after a requestNormalizeOnce(); then clears the flag. */
    public static boolean consumeNormalizeOnce() {
        if (normalizeOnce) {
            normalizeOnce = false;
            return true;
        }
        return false;
    }
}
