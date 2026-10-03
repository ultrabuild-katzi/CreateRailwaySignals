package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import de.jannik.createrailwaysignal.config.HudPosition;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;

/**
 * Client command for configuring the train speedometer HUD. Exists mainly because the
 * vanilla/Create config screen's number fields are too narrow to comfortably type offsets into.
 *
 * /trainspeedhud
 * /trainspeedhud position <top_left|top_right|bottom_left|bottom_right>
 * /trainspeedhud offset <horizontal> <vertical>
 * /trainspeedhud state
 */
@Environment(EnvType.CLIENT)
public final class TrainSpeedHudCommand {

    private static final float OFFSET_MIN = -100f;
    private static final float OFFSET_MAX = 100f;

    private TrainSpeedHudCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("trainspeedhud")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(prefix().append(Text.literal(
                                    "Use: /trainspeedhud position <top_left|top_right|bottom_left|bottom_right>, "
                                            + "/trainspeedhud offset <horizontal> <vertical>, /trainspeedhud state")
                                    .formatted(Formatting.GRAY)));
                            return 1;
                        })
                        .then(ClientCommandManager.literal("position")
                                .then(ClientCommandManager.argument("corner", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            for (HudPosition pos : HudPosition.values())
                                                b.suggest(pos.name().toLowerCase());
                                            return b.buildFuture();
                                        })
                                        .executes(c -> {
                                            String name = StringArgumentType.getString(c, "corner").toUpperCase();
                                            HudPosition pos;
                                            try {
                                                pos = HudPosition.valueOf(name);
                                            } catch (IllegalArgumentException e) {
                                                c.getSource().sendError(Text.literal("Unknown position: " + name));
                                                return 0;
                                            }
                                            RailwaySignalConfigs.client().hudPosition.set(pos);
                                            c.getSource().sendFeedback(prefix().append(
                                                    Text.literal("Position set to " + pos.name().toLowerCase())
                                                            .formatted(Formatting.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("offset")
                                .then(ClientCommandManager.argument("horizontal", FloatArgumentType.floatArg(OFFSET_MIN, OFFSET_MAX))
                                        .then(ClientCommandManager.argument("vertical", FloatArgumentType.floatArg(OFFSET_MIN, OFFSET_MAX))
                                                .executes(c -> {
                                                    float horizontal = FloatArgumentType.getFloat(c, "horizontal");
                                                    float vertical = FloatArgumentType.getFloat(c, "vertical");
                                                    var config = RailwaySignalConfigs.client();
                                                    config.horizontalOffset.set((double) horizontal);
                                                    config.verticalOffset.set((double) vertical);
                                                    c.getSource().sendFeedback(prefix().append(
                                                            Text.literal(String.format(
                                                                    "Offset set to horizontal=%.2f, vertical=%.2f",
                                                                    horizontal, vertical))
                                                                    .formatted(Formatting.GREEN)));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(ClientCommandManager.literal("state")
                                .executes(c -> {
                                    var config = RailwaySignalConfigs.client();
                                    c.getSource().sendFeedback(prefix().append(Text.literal(String.format(
                                            "position=%s, horizontalOffset=%.2f, verticalOffset=%.2f",
                                            config.hudPosition.get().name().toLowerCase(),
                                            config.horizontalOffset.getF(),
                                            config.verticalOffset.getF()))
                                            .formatted(Formatting.AQUA)));
                                    return 1;
                                })
                        )
        );
    }

    private static MutableText prefix() {
        return Text.literal("[")
                .formatted(Formatting.DARK_GRAY)
                .append(Text.literal("TrainSpeedHud").formatted(Formatting.GOLD))
                .append(Text.literal("] ").formatted(Formatting.DARK_GRAY));
    }
}
