package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.trains.track.TrackBlockOutline;
import de.jannik.createrailwaysignal.config.BankingSettings;
import de.jannik.createrailwaysignal.config.BankingSettingsNetworking;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;

public final class BankingSettingsClient {
    private static ClientWorld lastWorld;
    private static Boolean lastTrackInversion;

    private BankingSettingsClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(BankingSettingsNetworking.CHANNEL, (client, handler, buf, sender) -> {
            BankingSettings settings = new BankingSettings(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                    buf.readBoolean(), buf.readBoolean());
            client.execute(() -> RailwaySignalConfigs.setServerBanking(settings));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RailwaySignalConfigs.setServerBanking(null);
            lastWorld = null;
            lastTrackInversion = null;
            TrackEmbankmentRenderer.clearCache();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null)
                return;
            boolean inverted = RailwaySignalConfigs.banking().invertTrack();
            if (lastWorld == client.world && lastTrackInversion != null && lastTrackInversion == inverted)
                return;
            lastWorld = client.world;
            lastTrackInversion = inverted;
            TrackEmbankmentRenderer.clearCache();
            // Cloning clears Create's baked rail/girder caches without changing the curve's data.
            for (var track : TrackBlockOutline.TRACKS_WITH_TURNS.get(client.world).values()) {
                track.getConnections().replaceAll((pos, curve) -> curve.clone());
                VisualizationHelper.queueUpdate(track);
            }
        });
    }
}
