package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

/**
 * Root command:
 *   /createrailwaysignal showspeedblock [status|on|off|toggle]
 *   /createrailwaysignal showwhistleblock [status|on|off|toggle]
 *
 * Aliases (optional, same behavior):
 *   /showspeedblock  [status|on|off|toggle]
 *   /showwhistleblock[status|on|off|toggle]
 *
 * Gamerules (persist per world):
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
            // Root command with helpful default executor
            dispatcher.register(
                    CommandManager.literal("createrailwaysignal")
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        """
                                        Use:
                                          /createrailwaysignal showspeedblock [status|on|off|toggle]
                                          /createrailwaysignal showwhistleblock [status|on|off|toggle]
                                        """), false);
                                return 1;
                            })
                            .then(buildToggleBranch("showspeedblock", SHOW_SPEED_BLOCK))
                            .then(buildToggleBranch("showwhistleblock", SHOW_WHISTLE_BLOCK))
            );

            // Optional top-level aliases (handy + avoids root issues)
            dispatcher.register(buildToggleBranch("showspeedblock", SHOW_SPEED_BLOCK));
            dispatcher.register(buildToggleBranch("showwhistleblock", SHOW_WHISTLE_BLOCK));
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
                    src.sendFeedback(() -> Text.literal(pretyToggleName(literal, newVal)), false);
                    return 1;
                }));
    }

    private static String prettyName(String literal) {
        return switch (literal) {
            case "showspeedblock" -> "Speed block location display";
            case "showwhistleblock" -> "Whistle block log display";
            default -> literal;
        };
    }

    private static String pretyToggleName(String literal, boolean on) {
        return prettyName(literal) + ": " + (on ? "ON" : "OFF");
    }
}
