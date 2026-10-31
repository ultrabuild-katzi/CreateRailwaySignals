package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jannik.createrailwaysignal.debug.RealismDbg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/**
 * Client-side {@code /realismdebug [on|off|toggle]}.
 *
 * <p>The Create Realism ETCS compat ({@code ETCSMixin}, {@code RealismSpeedLimitScanner}) only
 * ever runs on the logical client - {@code ETCS} is a client-only HUD class. The
 * {@code /createrailwaysignal realismdebug} command registered via
 * {@link net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback} only ever toggles
 * {@link RealismDbg} on the logical side that executes it: on a dedicated server that is the
 * SERVER's JVM, which {@code RealismDbg} in the (separate) client JVM never observes. This client
 * command toggles the flag directly where the ETCS compat logging actually happens, so it works
 * even when connected to a dedicated server.</p>
 */
@Environment(EnvType.CLIENT)
public final class RealismDebugClientCommand {

    private RealismDebugClientCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("realismdebug")
                        .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    b.suggest("on"); b.suggest("off"); b.suggest("toggle");
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    String mode = StringArgumentType.getString(ctx, "mode").toLowerCase();
                                    switch (mode) {
                                        case "on" -> RealismDbg.set(true);
                                        case "off" -> RealismDbg.set(false);
                                        case "toggle" -> RealismDbg.set(!RealismDbg.on());
                                        default -> {
                                            ctx.getSource().sendError(Text.literal("Usage: /realismdebug <on|off|toggle>"));
                                            return 0;
                                        }
                                    }
                                    ctx.getSource().sendFeedback(Text.literal(
                                            "Realism compat debug (client): " + (RealismDbg.on() ? "ON" : "OFF")));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            RealismDbg.set(!RealismDbg.on());
                            ctx.getSource().sendFeedback(Text.literal(
                                    "Realism compat debug (client): " + (RealismDbg.on() ? "ON" : "OFF")));
                            return 1;
                        })
        );
    }
}
