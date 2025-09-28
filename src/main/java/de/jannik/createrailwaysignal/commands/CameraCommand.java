package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.simibubi.create.content.trains.CameraDistanceModifier;
import de.jannik.createrailwaysignal.config.CameraConfig;

@Environment(EnvType.CLIENT)
public final class CameraCommand {

    private CameraCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("camera")
                        .executes(ctx -> {
                            // Help/usage on bare /camera
                            ctx.getSource().sendFeedback(
                                    prefix().append(tr("crs.camera.help")
                                            .formatted(Formatting.GRAY))
                            );
                            return 1;
                        })
                        .then(ClientCommandManager.literal("on")
                                .executes(c -> {
                                    CameraConfig.setUnzoomEnabled(true);
                                    c.getSource().sendFeedback(prefix()
                                            .append(tr("crs.camera.on").formatted(Formatting.GREEN)));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("off")
                                .executes(c -> {
                                    CameraConfig.setUnzoomEnabled(false);
                                    c.getSource().sendFeedback(prefix()
                                            .append(tr("crs.camera.off").formatted(Formatting.YELLOW)));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("reset")
                                .executes(c -> {
                                    // Keep feature ON; just normalize the current zoom once.
                                    CameraConfig.requestNormalizeOnce();
                                    CameraDistanceModifier.reset();
                                    c.getSource().sendFeedback(prefix()
                                            .append(tr("crs.camera.reset").formatted(Formatting.AQUA)));
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("state")
                                .executes(c -> {
                                    boolean on = CameraConfig.isUnzoomEnabled();
                                    c.getSource().sendFeedback(prefix().append(
                                            tr(on ? "crs.camera.state.on" : "crs.camera.state.off")
                                                    .formatted(on ? Formatting.GREEN : Formatting.YELLOW)
                                    ));
                                    return 1;
                                })
                        )
        );
    }

    private static MutableText prefix() {
        return Text.literal("[")
                .formatted(Formatting.DARK_GRAY)
                .append(Text.literal("Camera").formatted(Formatting.GOLD))
                .append(Text.literal("] ").formatted(Formatting.DARK_GRAY));
    }

    private static MutableText tr(String key) {
        return Text.translatable(key);
    }

}
