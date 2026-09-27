package de.jannik.createrailwaysignal.config;

/**
 * Real-world speed units the train speedometer HUD can display a train's speed in.
 */
public enum SpeedUnit {
    KMH("km/h"),
    MPH("mph"),
    BPS("bps"),
    KNOTS("kn");

    public final String suffix;

    SpeedUnit(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Converts a train's speed (given in blocks per tick, Create's internal speed unit) into this unit.
     * There are 20 ticks per second, and Create's train speed configs are defined in blocks/second
     * (see {@code CTrains.trainTopSpeed}), so blocks/tick must first be scaled to blocks/second (x20)
     * before applying the usual m/s -> km/h factor (x3.6). This keeps the HUD consistent with the
     * speed limits configured on speed signs via {@code SpeedSignalBoundary}.
     */
    public double convert(double blocksPerTick) {
        double blocksPerSecond = Math.abs(blocksPerTick) * 20.0;
        double kmh = blocksPerSecond * 3.6;
        return switch (this) {
            case BPS -> blocksPerSecond;
            case KMH -> kmh;
            case MPH -> kmh * 0.621371192;
            case KNOTS -> kmh * 0.539956803;
        };
    }
}
