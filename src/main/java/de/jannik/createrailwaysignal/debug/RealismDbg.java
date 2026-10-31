package de.jannik.createrailwaysignal.debug;

/** Lightweight toggle for logging the Create Realism ETCS speed-limit compat (see
 * {@code de.jannik.createrailwaysignal.compat.realism}). Enabled with
 * {@code /createrailwaysignal realismdebug [on|off|toggle]}. */
public final class RealismDbg {
    private static volatile boolean ENABLED = false;

    public static boolean on() { return ENABLED; }
    public static void set(boolean v) { ENABLED = v; }

    public static void log(String msg) {
        if (on()) System.out.println("[Realism Compat] " + msg);
    }
}
