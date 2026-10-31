package de.jannik.createrailwaysignal.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint.ITrackSelector;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Soft dependency on BetterTrainSwitches. While a route is active it sets the switches itself, so
 * look-ahead scans must follow that route instead of the manual A/D steering of the driver.
 * Reflective on purpose: this mod must keep working without BetterTrainSwitches installed.
 */
public final class BetterTrainSwitchesCompat {

    public static final String MODID = "bettertrainswitches";

    private static final MethodHandle SCOUT_SELECTOR = lookup();

    private BetterTrainSwitchesCompat() {}

    private static MethodHandle lookup() {
        if (!FabricLoader.getInstance().isModLoaded(MODID))
            return null;
        try {
            Class<?> assist = Class.forName("de.ultrabuildmc.bettertrainswitches.route.RouteAssist");
            return MethodHandles.publicLookup().findStatic(assist, "scoutSelector",
                    MethodType.methodType(ITrackSelector.class, Train.class, boolean.class, ITrackSelector.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            Createrailwaysignal.LOGGER.warn("BetterTrainSwitches is installed but its route API was not found", e);
            return null;
        }
    }

    /** @return the selector a look-ahead scan should use: the planned route if one is active, else {@code manual}. */
    public static ITrackSelector scoutSelector(Train train, boolean backward, ITrackSelector manual) {
        if (SCOUT_SELECTOR == null)
            return manual;
        try {
            ITrackSelector selector = (ITrackSelector) SCOUT_SELECTOR.invoke(train, backward, manual);
            return selector == null ? manual : selector;
        } catch (Throwable t) {
            return manual;
        }
    }
}
