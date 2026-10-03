package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackNode;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import de.jannik.createrailwaysignal.graph.WhistleBlockBoundary;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mixin into Create's Train class for speed-block logging + whistle trigger. */
@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin implements SpeedSignalProvider {

    @Unique
    private SpeedSignalBoundary createRailwaySignal$$speedSignal;

    /** A less restrictive (or reset) boundary the front of the train has already reached, but which
     * should only actually take effect once the whole train (i.e. its last bogey) has cleared it. */
    @Unique
    private SpeedSignalBoundary createRailwaySignal$$pendingSpeedSignal;

    @Unique
    private boolean createRailwaySignal$$encounteredWhiteBlock = false;

    @Unique
    private World createRailwaySignal$$worldRef;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(World level, CallbackInfo ci) {
        this.createRailwaySignal$$worldRef = level;

        if (createRailwaySignal$$encounteredWhiteBlock) {
            WhistleBlockBoundary.playSound(level, (Train) (Object) this);
            this.createRailwaySignal$$encounteredWhiteBlock = false;
        }
    }

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    private void frontSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var original = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                // The speed limiter is directional: it should only take effect when the train
                // travels the same way the block was facing when placed, not when approaching
                // it from the opposite direction.
                TrackNode fromNode = couple.getSecond().getFirst();
                TrackNode toNode = couple.getSecond().getSecond();
                boolean primary = speedSignalBoundary.isPrimary(fromNode);

                World debugWorld = this.createRailwaySignal$$worldRef;
                if (debugWorld != null && debugWorld.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get()) {
                    System.out.println(
                            "[DirCheck] from=" + fromNode.getLocation().getLocation()
                                    + " to=" + toNode.getLocation().getLocation()
                                    + " edgeLoc.first=" + speedSignalBoundary.edgeLocation.getFirst().getLocation()
                                    + " edgeLoc.second=" + speedSignalBoundary.edgeLocation.getSecond().getLocation()
                                    + " isPrimary(from)=" + primary
                    );
                }

                if (primary)
                    return false;

                Train train = (Train) (Object) this;
                double maxSpeed = train.maxSpeed();
                SpeedSignalBoundary active = this.createRailwaySignal$$speedSignal;
                double currentLimit = (active != null && !active.resetsLimit())
                        ? active.calculateSpeedLimit() * maxSpeed
                        : Double.MAX_VALUE;
                double newLimit = speedSignalBoundary.resetsLimit()
                        ? Double.MAX_VALUE
                        : speedSignalBoundary.calculateSpeedLimit() * maxSpeed;

                World w = this.createRailwaySignal$$worldRef;
                boolean debug = w != null && w.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get();
                var pos = speedSignalBoundary.getBlockEntityPos();

                if (newLimit < currentLimit) {
                    // Stricter limit: take effect immediately as the front of the train reaches it.
                    this.createRailwaySignal$$speedSignal = speedSignalBoundary;
                    if (debug)
                        System.out.println(
                                "Change speed limit to " + speedSignalBoundary.getSpeedLimitKilometersPerHour() +
                                        " km/h at: x= " + pos.getX() + " y= " + pos.getY() + " z= " + pos.getZ()
                        );
                } else {
                    // Looser limit (or reset): don't let the train speed up until it has fully cleared
                    // this boundary, i.e. its last bogey has also driven over it.
                    this.createRailwaySignal$$pendingSpeedSignal = speedSignalBoundary;
                    if (debug)
                        System.out.println(
                                "Front reached speed limit " + speedSignalBoundary.getSpeedLimitKilometersPerHour() +
                                        " km/h at: x= " + pos.getX() + " y= " + pos.getY() + " z= " + pos.getZ() +
                                        " (deferred until whole train has passed)"
                        );
                }
                return false;
            }

            if (couple.getFirst() instanceof de.jannik.createrailwaysignal.graph.WhistleBlockBoundary) {
                this.createRailwaySignal$$encounteredWhiteBlock = true;
                return false;
            }

            return original.test(distance, couple);
        });
    }

    @Inject(method = "backSignalListener", at = @At("RETURN"), cancellable = true)
    private void backSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var original = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                if (this.createRailwaySignal$$pendingSpeedSignal == speedSignalBoundary) {
                    this.createRailwaySignal$$speedSignal = speedSignalBoundary;
                    this.createRailwaySignal$$pendingSpeedSignal = null;

                    World w = this.createRailwaySignal$$worldRef;
                    if (w != null && w.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get()) {
                        var pos = speedSignalBoundary.getBlockEntityPos();
                        System.out.println(
                                "Whole train cleared speed limit boundary, now allowing " +
                                        speedSignalBoundary.getSpeedLimitKilometersPerHour() +
                                        " km/h at: x= " + pos.getX() + " y= " + pos.getY() + " z= " + pos.getZ()
                        );
                    }
                }
                return false;
            }

            return original.test(distance, couple);
        });
    }

    @Inject(method = "collideWithOtherTrains", at = @At("HEAD"), cancellable = true)
    private void collideWithOtherTrains(World level, Carriage carriage, CallbackInfo ci) {
        ci.cancel();
    }

    // SpeedSignalProvider impl
    @Override
    public SpeedSignalBoundary createRailwaySignal$$speedSignal() {
        return this.createRailwaySignal$$speedSignal;
    }

    @Override
    public void createRailwaySignal$$setSpeedSignal(SpeedSignalBoundary boundary) {
        this.createRailwaySignal$$speedSignal = boundary;
    }

    @Override
    public World createRailwaySignal$$world() {
        return this.createRailwaySignal$$worldRef;
    }
}