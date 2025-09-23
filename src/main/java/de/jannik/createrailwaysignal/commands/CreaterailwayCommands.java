package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jannik.createrailwaysignal.block.kilometer.KilometerMarkerBlock;
import de.jannik.createrailwaysignal.block.kilometer.KilometerMarkerHelper;
import de.jannik.createrailwaysignal.debug.KMDbg;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Root command:
 *   /createrailwaysignal debugspeedblock [status|on|off|toggle]
 *   /createrailwaysignal debugwhistleblock [status|on|off|toggle]
 *   /createrailwaysignal kmdebug [on|off|toggle]
 *   /createrailwaysignal kmdump
 *
 * Aliases:
 *   /debugspeedblock  [status|on|off|toggle]
 *   /debugwhistleblock[status|on|off|toggle]
 *   /kmdebug [on|off|toggle]
 *   /kmdump
 *
 * Gamerules:
 *   /gamerule showSpeedBlock true|false
 *   /gamerule showWhistleBlock true|false
 */
public final class CreaterailwayCommands {
    private CreaterailwayCommands() {}

    /** Public so other classes (mixins/util) can read the keys. */
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_SPEED_BLOCK;
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_WHISTLE_BLOCK;

    static {
        SHOW_SPEED_BLOCK = GameRuleRegistry.register(
                "showSpeedBlock", GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(false) // default OFF
        );
        SHOW_WHISTLE_BLOCK = GameRuleRegistry.register(
                "showWhistleBlock", GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(false) // default OFF
        );
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Root command
            dispatcher.register(
                    CommandManager.literal("createrailwaysignal")
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        """
                                        Use:
                                          /createrailwaysignal debugspeedblock [status|on|off|toggle]
                                          /createrailwaysignal debugwhistleblock [status|on|off|toggle]
                                          /createrailwaysignal kmdebug [on|off|toggle]
                                          /createrailwaysignal kmdump
                                        """), false);
                                return 1;
                            })
                            .then(buildToggleBranch("debugspeedblock", SHOW_SPEED_BLOCK))
                            .then(buildToggleBranch("debugwhistleblock", SHOW_WHISTLE_BLOCK))
                            .then(buildKmDebugNode())   // NEW
                            .then(buildKmDumpNode())    // NEW
            );

            // Existing aliases
            dispatcher.register(buildToggleBranch("debugspeedblock", SHOW_SPEED_BLOCK));
            dispatcher.register(buildToggleBranch("debugwhistleblock", SHOW_WHISTLE_BLOCK));

            // NEW aliases
            dispatcher.register(buildKmDebugNode());
            dispatcher.register(buildKmDumpNode());
        });
    }

    /** Build a subcommand branch that supports: [status|on|off|toggle], default -> status. */
    private static LiteralArgumentBuilder<ServerCommandSource> buildToggleBranch(
            String literal, GameRules.Key<GameRules.BooleanRule> key) {

        return CommandManager.literal(literal)
                // /<literal>
                .executes(ctx -> {
                    boolean on = ctx.getSource().getWorld().getGameRules().get(key).get();
                    ctx.getSource().sendFeedback(
                            () -> Text.literal(prettyName(literal) + " is currently: " + (on ? "ON" : "OFF")),
                            false
                    );
                    return 1;
                })
                // /<literal> status
                .then(CommandManager.literal("status").executes(ctx -> {
                    boolean on = ctx.getSource().getWorld().getGameRules().get(key).get();
                    ctx.getSource().sendFeedback(
                            () -> Text.literal(prettyName(literal) + " is currently: " + (on ? "ON" : "OFF")),
                            false
                    );
                    return 1;
                }))
                // /<literal> on
                .then(CommandManager.literal("on").executes(ctx -> {
                    var src = ctx.getSource();
                    var rules = src.getWorld().getGameRules();
                    rules.get(key).set(true, src.getServer());
                    src.sendFeedback(() -> Text.literal(prettyName(literal) + ": ON"), false);
                    return 1;
                }))
                // /<literal> off
                .then(CommandManager.literal("off").executes(ctx -> {
                    var src = ctx.getSource();
                    var rules = src.getWorld().getGameRules();
                    rules.get(key).set(false, src.getServer());
                    src.sendFeedback(() -> Text.literal(prettyName(literal) + ": OFF"), false);
                    return 1;
                }))
                // /<literal> toggle
                .then(CommandManager.literal("toggle").executes(ctx -> {
                    var src = ctx.getSource();
                    var rules = src.getWorld().getGameRules();
                    var rule = rules.get(key);
                    boolean newVal = !rule.get();
                    rule.set(newVal, src.getServer());
                    src.sendFeedback(() -> Text.literal(prettyToggleName(literal, newVal)), false);
                    return 1;
                }));
    }

    private static String prettyName(String literal) {
        return switch (literal) {
            case "debugspeedblock" -> "Speed block location display";
            case "debugwhistleblock" -> "Whistle block log display";
            default -> literal;
        };
    }

    private static String prettyToggleName(String literal, boolean on) {
        return prettyName(literal) + ": " + (on ? "ON" : "OFF");
    }

    /* ===================== NEW: KM debug toggle ===================== */

    private static LiteralArgumentBuilder<ServerCommandSource> buildKmDebugNode() {
        return literal("kmdebug")
                .then(argument("mode", StringArgumentType.word())
                        .suggests((c, b) -> {
                            b.suggest("on"); b.suggest("off"); b.suggest("toggle");
                            return b.buildFuture();
                        })
                        .executes(ctx -> {
                            String mode = StringArgumentType.getString(ctx, "mode").toLowerCase();
                            switch (mode) {
                                case "on" -> KMDbg.set(true);
                                case "off" -> KMDbg.set(false);
                                case "toggle" -> KMDbg.set(!KMDbg.on());
                                default -> {
                                    ctx.getSource().sendError(Text.literal("Usage: /kmdebug <on|off|toggle>"));
                                    return 0;
                                }
                            }
                            ctx.getSource().sendFeedback(() -> Text.literal("KM debug: " + (KMDbg.on() ? "ON" : "OFF")), false);
                            return 1;
                        })
                )
                .executes(ctx -> {
                    // no-arg: toggle
                    KMDbg.set(!KMDbg.on());
                    ctx.getSource().sendFeedback(() -> Text.literal("KM debug: " + (KMDbg.on() ? "ON" : "OFF")), false);
                    return 1;
                });
    }

    /* ===================== NEW: KM dump ===================== */

    private static LiteralArgumentBuilder<ServerCommandSource> buildKmDumpNode() {
        return literal("kmdump")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendError(Text.literal("Players only."));
                        return 0;
                    }

                    // Raycast to the block the player is looking at
                    BlockHitResult hit = (BlockHitResult) player.raycast(8.0D, 0.0F, false);
                    BlockPos pos = hit.getBlockPos();
                    ServerWorld world = ctx.getSource().getWorld();
                    BlockState state = world.getBlockState(pos);

                    if (!(state.getBlock() instanceof KilometerMarkerBlock)) {
                        ctx.getSource().sendError(Text.literal("Look at a Kilometer Marker to use /kmdump."));
                        return 0;
                    }
                    return 0;
                });
    }
}
