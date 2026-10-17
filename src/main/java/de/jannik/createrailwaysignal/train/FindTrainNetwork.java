package de.jannik.createrailwaysignal.train;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.station.GlobalStation;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Networking + lookup helpers backing the {@code /findtrain} GUI: the client requests the
 * current list of trains, and can ask the server to teleport the player onto any of them.
 */
public final class FindTrainNetwork {

    private FindTrainNetwork() {}

    public static final Identifier REQUEST = Identifier.of(Createrailwaysignal.MOD_ID, "findtrain_request");
    public static final Identifier RESPONSE = Identifier.of(Createrailwaysignal.MOD_ID, "findtrain_response");
    public static final Identifier TELEPORT = Identifier.of(Createrailwaysignal.MOD_ID, "findtrain_teleport");

    /** Snapshot of a train, sent from the server to the client for display in the GUI. */
    public record TrainInfo(UUID id, String name, int carriageCount, String destination) {}

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST, (server, player, handler, buf, responseSender) ->
                server.execute(() -> sendTrainList(player)));

        ServerPlayNetworking.registerGlobalReceiver(TELEPORT, (server, player, handler, buf, responseSender) -> {
            UUID trainId = buf.readUuid();
            server.execute(() -> teleportToTrain(player, trainId));
        });
    }

    private static void sendTrainList(ServerPlayerEntity player) {
        List<Train> trains = new ArrayList<>(Create.RAILWAYS.trains.values());
        trains.sort(Comparator.comparing(t -> t.name.getString().toLowerCase(Locale.ROOT)));

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(trains.size());
        for (Train train : trains) {
            buf.writeUuid(train.id);
            buf.writeString(train.name.getString());
            buf.writeInt(train.carriages.size());
            buf.writeString(resolveDestination(train));
        }
        ServerPlayNetworking.send(player, RESPONSE, buf);
    }

    private static String resolveDestination(Train train) {
        GlobalStation destination = train.navigation.destination;
        if (destination != null)
            return destination.name;

        if (train.runtime.getSchedule() != null && train.runtime.currentTitle != null
                && !train.runtime.currentTitle.isEmpty())
            return train.runtime.currentTitle;

        return "None";
    }

    private static void teleportToTrain(ServerPlayerEntity player, UUID trainId) {
        Train train = Create.RAILWAYS.trains.get(trainId);
        if (train == null) {
            player.sendMessage(Text.literal("That train no longer exists.").formatted(Formatting.RED), false);
            return;
        }

        for (Carriage carriage : train.carriages) {
            for (RegistryKey<World> dimension : carriage.getPresentDimensions()) {
                Optional<BlockPos> posOpt = carriage.getPositionInDimension(dimension);
                if (posOpt.isEmpty())
                    continue;

                ServerWorld world = player.getServer().getWorld(dimension);
                if (world == null)
                    continue;

                BlockPos pos = posOpt.get();
                player.teleport(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        player.getYaw(), player.getPitch());
                player.sendMessage(Text.literal("Teleported to train \"" + train.name.getString() + "\".")
                        .formatted(Formatting.GREEN), false);
                return;
            }
        }

        player.sendMessage(Text.literal("Could not locate that train right now (its chunks may be unloaded).")
                .formatted(Formatting.RED), false);
    }
}
