package de.jannik.createrailwaysignal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BoatFlyCommand {

    @Unique
    private static ArrayList<UUID> enabledPlayers = new ArrayList<>();



    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("boatfly")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    setEnabled(context.getSource().getPlayerOrThrow().getUuid(), enabled);

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Boat fly has been " + (enabled ? "enabled" : "disabled")),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
    }


    public static boolean isEnabled(UUID uuid) {
        return enabledPlayers.contains(uuid);
    }


    public static void setEnabled(UUID uuid, boolean enabled) {
        if(enabled)
            enabledPlayers.add(uuid);
        else
            enabledPlayers.remove(uuid);
    }

}