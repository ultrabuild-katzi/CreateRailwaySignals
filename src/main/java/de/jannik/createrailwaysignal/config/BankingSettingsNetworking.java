package de.jannik.createrailwaysignal.config;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class BankingSettingsNetworking {
    public static final Identifier CHANNEL = new Identifier(Createrailwaysignal.MOD_ID, "banking_settings");
    private static BankingSettings lastSent;

    private BankingSettingsNetworking() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player, current()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> lastSent = null);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            BankingSettings settings = current();
            if (settings.equals(lastSent))
                return;
            lastSent = settings;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                send(player, settings);
        });
    }

    private static BankingSettings current() {
        var config = RailwaySignalConfigs.server();
        return new BankingSettings(config.enforceClientBanking.get(), config.tiltRiders.get(),
                config.invertTrackBanking.get(), config.invertBogeyBanking.get(), config.invertBodyBanking.get());
    }

    private static void send(ServerPlayerEntity player, BankingSettings settings) {
        if (!ServerPlayNetworking.canSend(player, CHANNEL))
            return;
        var buf = PacketByteBufs.create();
        buf.writeBoolean(settings.enforced());
        buf.writeBoolean(settings.tiltRiders());
        buf.writeBoolean(settings.invertTrack());
        buf.writeBoolean(settings.invertBogey());
        buf.writeBoolean(settings.invertBody());
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
