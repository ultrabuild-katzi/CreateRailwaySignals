package de.jannik.createrailwaysignal;

/** Global debug flags (safe to access from mixins and commands). */
public final class DebugFlags {
    private DebugFlags() {}

    /** Toggle for printing SpeedSignalBoundary locations. */
    public static volatile boolean SHOW_SPEED_BLOCK = false;
}
