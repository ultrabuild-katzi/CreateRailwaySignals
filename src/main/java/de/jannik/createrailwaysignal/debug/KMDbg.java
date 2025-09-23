package de.jannik.createrailwaysignal.debug;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class KMDbg {
    private static volatile boolean ENABLED = false;

    public static boolean on() { return ENABLED; }
    public static void set(boolean v) { ENABLED = v; }

    public static void tell(ServerPlayerEntity p, String msg) {
        if (p != null && on()) p.sendMessage(Text.literal(msg), false);
    }

    public static void log(String msg) {
        if (on()) System.out.println("[KM DEBUG] " + msg);
    }
}
