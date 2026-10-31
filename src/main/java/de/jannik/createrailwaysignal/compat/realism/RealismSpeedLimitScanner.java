package de.jannik.createrailwaysignal.compat.realism;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackNode;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.compat.BetterTrainSwitchesCompat;
import de.jannik.createrailwaysignal.debug.RealismDbg;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import net.Realism.trains.etcs.ETCS;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scans ahead of a (manually driven) train for our own {@link SpeedSignalBoundary} signs, the same
 * way {@code net.Realism.trains.SignalFinder} already scans ahead for Create: Tramways signs. The
 * result is fed into Create Realism's ETCS so its HUD/braking curve also reacts to our speed
 * blocks - see {@link de.jannik.createrailwaysignal.compat.realism.mixin.ETCSMixin}.
 */
public final class RealismSpeedLimitScanner {

    private RealismSpeedLimitScanner() {}

    /**
     * Realism's own ETCS.calculateAllowedSpeed() snaps {@code trackspeedlimit} to whatever
     * cachedSpeedLimits entry has {@code distance() < 5}, regardless of who added it - this runs
     * *after* our mixin's injection, in the same tick. If we fed it a preview entry for a sign the
     * train is merely approaching (not yet crossed), that native snap would prematurely jump the
     * displayed limit before the correct axle (front/back) actually reaches it, fighting the sticky
     * state from TrainMixin. Staying strictly outside that snap radius keeps trackspeedlimit solely
     * driven by the persistent SpeedSignalProvider state set in ETCSMixin.
     */
    private static final double NATIVE_SNAP_RADIUS = 5.0;

    /** Logged once (not every tick) so a Realism-internals mismatch is still visible in the log
     * even if nobody remembered to turn on {@code /createrailwaysignal realismdebug}. */
    private static final AtomicBoolean LOGGED_ERROR = new AtomicBoolean(false);

    public static List<ETCS.SpeedLimit> scan(Train train, double maxDistance, boolean backward) {
        List<ETCS.SpeedLimit> limits = new ArrayList<>();
        if (train == null) {
            RealismDbg.log("scan: train is null");
            return limits;
        }
        if (train.graph == null) {
            RealismDbg.log("scan: train " + train.id + " has no track graph (not on track yet?)");
            return limits;
        }
        if (train.carriages.isEmpty()) {
            RealismDbg.log("scan: train " + train.id + " has no carriages");
            return limits;
        }

        try {
            TravellingPoint startPoint = backward
                    ? train.carriages.get(train.carriages.size() - 1).getTrailingPoint()
                    : train.carriages.get(0).getLeadingPoint();

            if (startPoint.edge == null) {
                RealismDbg.log("scan: train " + train.id + " leading/trailing point has no edge");
                return limits;
            }

            TravellingPoint scout = new TravellingPoint(startPoint.node1, startPoint.node2,
                    startPoint.edge, startPoint.position, startPoint.upsideDown);

            TravellingPoint.SteerDirection steerDirection = train.manualSteer != null
                    ? train.manualSteer
                    : TravellingPoint.SteerDirection.NONE;

            double distance = backward ? -maxDistance : maxDistance;

            TravellingPoint.ITrackSelector selector = BetterTrainSwitchesCompat.scoutSelector(train, backward,
                    scout.steer(steerDirection, new Vec3d(0, 1, 0)));

            scout.travel(train.graph, distance, selector, (dist, couple) -> {
                if (!(couple.getFirst() instanceof SpeedSignalBoundary boundary))
                    return false;

                // Same directionality rule as TrainMixin/NavigationMixin: the sign only applies
                // when approached from its non-primary side.
                TrackNode fromNode = couple.getSecond().getFirst();
                if (boundary.isPrimary(fromNode))
                    return false;

                double absDist = Math.abs(dist);
                if (!boundary.resetsLimit() && absDist >= NATIVE_SNAP_RADIUS)
                    limits.add(new ETCS.SpeedLimit(absDist, boundary.getSpeedLimitKilometersPerHour()));

                return false; // keep scanning for further signs ahead
            });

            if (RealismDbg.on())
                RealismDbg.log("scan: train " + train.id + " found " + limits.size() + " upcoming sign(s): " + limits);
        } catch (Throwable t) {
            // Never let a mismatch with Realism's internals break the host train's tick, but make
            // sure such a mismatch is actually visible somewhere instead of failing silently forever.
            if (LOGGED_ERROR.compareAndSet(false, true))
                Createrailwaysignal.LOGGER.warn(
                        "Create Realism compat: speed-sign scan failed (further occurrences suppressed, "
                                + "enable /createrailwaysignal realismdebug for details)", t);
            RealismDbg.log("scan: exception - " + t);
            return new ArrayList<>();
        }

        return limits;
    }
}
