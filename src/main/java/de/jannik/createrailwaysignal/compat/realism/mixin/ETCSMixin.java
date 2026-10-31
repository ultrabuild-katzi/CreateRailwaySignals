package de.jannik.createrailwaysignal.compat.realism.mixin;

import com.simibubi.create.content.trains.entity.Train;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.compat.realism.RealismSpeedLimitScanner;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import de.jannik.createrailwaysignal.debug.RealismDbg;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import net.Realism.trains.etcs.ETCS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feeds our own {@link de.jannik.createrailwaysignal.graph.SpeedSignalBoundary} limits into Create
 * Realism's ETCS, exactly like it already does internally for Create: Tramways signs
 * (net.Realism.compat.TramwaysCompat). Only ever applied when Realism is installed - see
 * {@link de.jannik.createrailwaysignal.compat.realism.RealismMixinPlugin}.
 */
@Mixin(value = ETCS.class, remap = false)
public abstract class ETCSMixin {

    @Shadow public Train train;
    @Shadow private List<ETCS.SpeedLimit> cachedSpeedLimits;
    @Shadow private int trackspeedlimit;
    @Shadow private int zoom;
    @Shadow private boolean backward;

    @Unique
    private int createRailwaySignal$$lastLoggedLimit = Integer.MIN_VALUE;

    /** Logged once, not every tick - see {@link RealismSpeedLimitScanner}'s matching flag. */
    @Unique
    private static final AtomicBoolean createRailwaySignal$$loggedError = new AtomicBoolean(false);

    /**
     * Injected right before the first {@code updateBrakingDistances} call in {@code update()},
     * i.e. after {@code cachedSpeedLimits} has been (re)computed for Tramways but before it is
     * used to compute the allowed speed / braking curve for this tick.
     */
    @Inject(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/Realism/trains/etcs/ETCS;updateBrakingDistances(II)V", ordinal = 0))
    private void createRailwaySignal$$injectSpeedLimits(CallbackInfo ci) {
        try {
            double scanDistance = this.zoom > 0 ? 4000.0 / this.zoom : 4000.0;
            List<ETCS.SpeedLimit> extra = RealismSpeedLimitScanner.scan(this.train, scanDistance, this.backward);
            if (!extra.isEmpty())
                this.cachedSpeedLimits.addAll(extra);

            // The currently-enforced limit must stay sticky exactly like it does for AI trains:
            // it only changes once the front reaches a stricter boundary, or once the whole train
            // (its last axle) has cleared a looser/reset one - see TrainMixin's front/backSignalListener.
            // Re-reading that same persistent per-train state here (instead of re-deriving "current"
            // from a fresh forward-only scan each tick) is what keeps the HUD from snapping back to
            // the max speed the instant the train's front passes a sign.
            SpeedSignalBoundary active = ((SpeedSignalProvider) this.train).createRailwaySignal$$speedSignal();
            if (active != null && !active.resetsLimit())
                this.trackspeedlimit = active.getSpeedLimitKilometersPerHour()
                        + RailwaySignalConfigs.common().etcsHudSpeedBuffer.get();

            if (RealismDbg.on() && this.trackspeedlimit != this.createRailwaySignal$$lastLoggedLimit) {
                this.createRailwaySignal$$lastLoggedLimit = this.trackspeedlimit;
                RealismDbg.log("train " + (this.train != null ? this.train.id : null)
                        + " trackspeedlimit -> " + this.trackspeedlimit
                        + " (active=" + active + ")");
            }
        } catch (Throwable t) {
            // A Realism version with a differently-shaped ETCS (fields renamed/removed) must never
            // break the host train's tick - but it also must not fail completely silently.
            if (createRailwaySignal$$loggedError.compareAndSet(false, true))
                Createrailwaysignal.LOGGER.warn(
                        "Create Realism compat: ETCS speed-limit injection failed (further occurrences "
                                + "suppressed, enable /createrailwaysignal realismdebug for details)", t);
            RealismDbg.log("injectSpeedLimits: exception - " + t);
        }
    }
}
