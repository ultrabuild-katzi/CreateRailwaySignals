package de.jannik.createrailwaysignal.graph;

import net.minecraft.world.World;

public interface SpeedSignalProvider {

    SpeedSignalBoundary createRailwaySignal$$speedSignal();

    /**
     * Lets the look-ahead scan proactively update the active speed signal to whatever boundary it
     * found ahead on the train's committed path, instead of waiting for the train to physically
     * cross the sign. Since the scan already guarantees nothing else lies between the train and this
     * boundary, it is safe to treat it as the currently applicable limit right away (this also lets
     * the train speed back up early once it is committed to a faster branch, not just brake early
     * for a slower one).
     */
    void createRailwaySignal$$setSpeedSignal(SpeedSignalBoundary boundary);

    /** World reference captured from Train#tick, used for debug logging in the look-ahead scan. */
    World createRailwaySignal$$world();

}
