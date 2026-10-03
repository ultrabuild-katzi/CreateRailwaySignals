package de.jannik.createrailwaysignal.mixin;


import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.station.GlobalStation;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Navigation.class, remap = false)
public abstract class NavigationMixin {
    @Shadow public Train train;

    @Shadow public double distanceToDestination;

    @Shadow public boolean destinationBehindTrain;

    @Shadow public GlobalStation destination;

    @Shadow public abstract TravellingPoint.ITrackSelector controlSignalScout();

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/trains/entity/Train;throttle:D", opcode = Opcodes.GETFIELD))
    public double throttle(Train train){
        SpeedSignalBoundary speedLimit = ((SpeedSignalProvider) train).createRailwaySignal$$speedSignal();

        World world = ((SpeedSignalProvider) train).createRailwaySignal$$world();
        boolean debug = world != null && world.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get();

        if(speedLimit == null) {
            if (debug)
                System.out.println("[Throttle] No active speed signal. throttle=" + train.throttle
                        + " maxSpeed(blocks/tick)=" + train.maxSpeed() + " (=" + (train.maxSpeed() * 72) + " km/h)"
                        + " fuelTicks=" + train.fuelTicks);
            return train.throttle;
        }

        if(speedLimit.resetsLimit()) {
            if (debug)
                System.out.println("[Throttle] Active signal resets limit. throttle=" + train.throttle
                        + " maxSpeed(blocks/tick)=" + train.maxSpeed() + " (=" + (train.maxSpeed() * 72) + " km/h)"
                        + " fuelTicks=" + train.fuelTicks);
            return train.throttle;
        } else {

            double throttle = speedLimit.calculateSpeedLimit();
            double result = Math.min(train.throttle, throttle);
            if (debug)
                System.out.println("[Throttle] Active signal=" + speedLimit.getSpeedLimitKilometersPerHour()
                        + " km/h ratio=" + throttle + " train.throttle=" + train.throttle + " -> using=" + result
                        + " maxSpeed(blocks/tick)=" + train.maxSpeed() + " (=" + (train.maxSpeed() * 72) + " km/h)"
                        + " fuelTicks=" + train.fuelTicks
                        + " effectiveCap(km/h)=" + (result * train.maxSpeed() * 72));
            return result;

        }
    }

    /**
     * Looks ahead for the next speed block boundary on the train's path and, once the train is within
     * braking distance of it, caps the target speed early so the train's actual speed already matches
     * the boundary's limit by the time it physically reaches it (instead of only starting to brake
     * after having crossed it).
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/entity/Train;approachTargetSpeed(F)V"))
    private void createRailwaySignal$$approachTargetSpeed(Train train, float accelerationMod) {
        createRailwaySignal$$applySpeedBoundaryLookahead(train);
        train.approachTargetSpeed(accelerationMod);
    }

    @Unique
    private void createRailwaySignal$$applySpeedBoundaryLookahead(Train train) {
        if (train.graph == null || train.targetSpeed == 0 || train.carriages.isEmpty())
            return;

        double acceleration = train.acceleration();
        double currentSpeed = Math.abs(train.speed);
        double brakingDistance = (currentSpeed * currentSpeed) / (2 * acceleration);
        double scanDistance = Math.min(brakingDistance + 5, distanceToDestination);
        if (scanDistance <= 0)
            return;

        double speedMod = destinationBehindTrain ? -1 : 1;
        Carriage leadingCarriage = destinationBehindTrain ? train.carriages.get(train.carriages.size() - 1) : train.carriages.get(0);
        TravellingPoint leadingPoint = destinationBehindTrain ? leadingCarriage.getTrailingPoint() : leadingCarriage.getLeadingPoint();
        if (leadingPoint.edge == null)
            return;

        TravellingPoint speedScout = new TravellingPoint(leadingPoint.node1, leadingPoint.node2, leadingPoint.edge,
                leadingPoint.position, leadingPoint.upsideDown);

        // controlSignalScout()'s manual-steer branch (destination == null) returns a selector bound to
        // Navigation's own internal signalScout instance, not our speedScout. Reusing it here would make
        // steer() evaluate the wrong TravellingPoint's edge. That branch is only safe to reuse when a
        // destination is set (it then only closes over a fresh copy of currentPath, not any instance state).
        TravellingPoint.ITrackSelector trackSelector = destination == null
                ? speedScout.steer(train.manualSteer, new Vec3d(0, 1, 0))
                : controlSignalScout();

        MutableObject<SpeedSignalBoundary> found = new MutableObject<>(null);
        MutableDouble foundDistance = new MutableDouble(-1);

        speedScout.travel(train.graph, scanDistance * speedMod, trackSelector, (distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary boundary && !boundary.resetsLimit()) {
                // Match TrainMixin#frontSignalListener: the limiter is directional and must be
                // ignored here too, otherwise the train still gets pre-braked for boundaries that
                // don't actually apply to its direction of travel.
                TrackNode fromNode = couple.getSecond().getFirst();
                if (boundary.isPrimary(fromNode))
                    return false;
                found.setValue(boundary);
                foundDistance.setValue(distance);
                return true;
            }
            return false;
        });

        SpeedSignalBoundary boundary = found.getValue();
        if (boundary == null)
            return;

        double boundarySpeed = boundary.calculateSpeedLimit() * train.maxSpeed();

        World world = ((SpeedSignalProvider) train).createRailwaySignal$$world();
        if (world != null && world.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get()) {
            var pos = boundary.getBlockEntityPos();
            System.out.println("[LookAhead] Found " + boundary.getSpeedLimitKilometersPerHour() + " km/h boundary at x="
                    + pos.getX() + " y=" + pos.getY() + " z=" + pos.getZ() + " distance=" + foundDistance.doubleValue()
                    + " currentSpeed(blocks/tick)=" + currentSpeed);
        }

        // Note: a boundary that raises the speed limit (or resets it) is intentionally NOT adopted
        // here in advance. Speeding up only happens once the whole train has physically cleared the
        // boundary (its last bogey has driven over it) - see TrainMixin#backSignalListener. This scan
        // is only used to pre-emptively brake for a *stricter* upcoming limit.
        if (boundarySpeed >= currentSpeed)
            return;

        double distance = foundDistance.doubleValue();
        double brakingDistanceNeeded = (currentSpeed * currentSpeed - boundarySpeed * boundarySpeed) / (2 * acceleration);

        if (distance <= brakingDistanceNeeded) {
            double cappedTarget = boundarySpeed * speedMod;
            if (Math.abs(cappedTarget) < Math.abs(train.targetSpeed))
                train.targetSpeed = cappedTarget;
        }
    }

}