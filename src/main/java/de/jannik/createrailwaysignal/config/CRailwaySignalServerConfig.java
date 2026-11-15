package de.jannik.createrailwaysignal.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

/** Per-world server settings, saved in serverconfig/createrailwaysignal-server.toml. */
public class CRailwaySignalServerConfig extends ConfigBase {
    public final ConfigGroup banking = group(0, "banking", "Banking rendering settings sent to connected clients.");
    public final ConfigBool enforceClientBanking = b(false, "enforceClientBanking",
            "Override connected players' banking rendering settings. Their client files are not changed; personal settings return on disconnect.");
    public final ConfigBool tiltRiders = b(true, "tiltRiders", "Tilt seated rider models with the carriage.");
    public final ConfigBool invertTrackBanking = b(false, "invertTrackBanking", "Reverse rendered track and embankment banking.");
    public final ConfigBool invertBogeyBanking = b(false, "invertBogeyBanking", "Reverse rendered bogey banking.");
    public final ConfigBool invertBodyBanking = b(false, "invertBodyBanking", "Reverse carriage body and seated rider banking.");

    @Override
    public @NotNull String getName() {
        return "server";
    }
}
