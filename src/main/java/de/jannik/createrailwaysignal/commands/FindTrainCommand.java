package de.jannik.createrailwaysignal.commands;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.client.FindTrainScreen;
import de.jannik.createrailwaysignal.train.FindTrainNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /findtrain
 *
 * Opens a GUI that lists every train currently known to Create on the server, with a search
 * box and a teleport button per entry.
 */
@Environment(EnvType.CLIENT)
public final class FindTrainCommand {

    private FindTrainCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("findtrain")
                        .executes(ctx -> {
                            try {
                                ClientPlayNetworking.send(FindTrainNetwork.REQUEST, PacketByteBufs.create());
                                // Defer opening the screen: ChatScreen closes itself (setScreen(null))
                                // right after this command runs. client.execute() runs immediately when
                                // already called from the client thread (as we are here), so it would
                                // not actually postpone anything - client.send() always queues the task
                                // for the next tick, after ChatScreen has finished closing itself.
                                MinecraftClient client = MinecraftClient.getInstance();
                                client.send(() -> client.setScreen(new FindTrainScreen()));
                            } catch (Throwable t) {
                                Createrailwaysignal.LOGGER.error("Failed to open Find Train screen", t);
                                ctx.getSource().sendFeedback(
                                        Text.literal("[FindTrain] Error: " + t).formatted(Formatting.RED));
                            }
                            return 1;
                        })
                )
        );
    }
}
