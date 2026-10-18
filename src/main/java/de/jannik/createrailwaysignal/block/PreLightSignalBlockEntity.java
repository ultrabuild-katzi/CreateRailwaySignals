package de.jannik.createrailwaysignal.block;

import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import de.jannik.createrailwaysignal.config.CRailwaySignalCommonConfig;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import de.jannik.createrailwaysignal.graph.PreLightSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;

/**
 * Track-side "distant signal" (Vorsignal): previews the speed limit of the next
 * {@link SpeedSignalBoundary} a train is heading towards, once that train is within
 * {@link CRailwaySignalCommonConfig#preLightSignalRange} blocks of this signal along its
 * currently chosen path. Purely informational - never alters a train's actual speed. Only
 * considers trains that are currently being automatically navigated towards a destination
 * (i.e. have an active schedule/route), not manually driven ones, so the displayed number
 * reflects the path automation picked at the last junction, not just whatever is physically
 * closest in space.
 */
public class PreLightSignalBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {
    private static final double LOOKAHEAD_BEYOND_SELF = 300;

    private static double proximityRange() {
        return RailwaySignalConfigs.common().preLightSignalRange.get();
    }

    public TrackTargetingBehaviour<PreLightSignalBoundary> edgePoint;

    /** Upcoming speed limit in tens of km/h (same scale as {@link TrackLimitBlock#SPEED_LIMIT}),
     * 0 = clear/no restriction ahead, -1 = no train near enough / no data. */
    private int previewSpeedTens = -1;

    public PreLightSignalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        this.edgePoint.transform(blockEntity, transform);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.edgePoint = new TrackTargetingBehaviour<>(this, CustomEdgePointType.PRE_LIGHT_SIGNAL);
        behaviours.add(this.edgePoint);
    }

    @Override
    public void remove() {
        super.remove();
        this.destroy();
    }

    @Override
    public void tick() {
        super.tick();
        if (world == null || world.isClient)
            return;

        // Throttled to once a second; this does a track lookahead per nearby train, no need to run every tick.
        if (world.getTime() % 20 != 0)
            return;

        updatePreview();
    }

    public int getPreviewSpeedTens() {
        return previewSpeedTens;
    }

    private boolean debug() {
        return world != null && world.getGameRules().get(CreaterailwayCommands.SHOW_PRE_LIGHT_SIGNAL).get();
    }

    private void updatePreview() {
        boolean debug = debug();
        PreLightSignalBoundary self = this.edgePoint.getEdgePoint();
        if (self == null || self.edgeLocation == null) {
            if (debug)
                System.out.println("[PreLightSignal@" + pos + "] no edge point / not bound to a track yet");
            setPreviewSpeedTens(-1);
            return;
        }

        TrackGraph graph = Create.RAILWAYS.sided(world).getGraph(world, self.edgeLocation.getFirst());
        if (graph == null) {
            if (debug)
                System.out.println("[PreLightSignal@" + pos + "] no track graph found at edge location");
            setPreviewSpeedTens(-1);
            return;
        }

        double proximityRange = proximityRange();
        double bestProximity = Double.MAX_VALUE;
        int bestTens = -1;
        boolean foundTrain = false;

        for (Train train : Create.RAILWAYS.trains.values()) {
            Navigation navigation = train.navigation;
            if (train.graph != graph || train.carriages.isEmpty() || navigation.destination == null) {
                if (debug && train.graph == graph)
                    System.out.println("[PreLightSignal@" + pos + "] skipping train " + train.id
                            + " (not under automatic navigation, destination == null)");
                continue;
            }

            // Mirrors Navigation's own leadingPoint/speedMod selection (see Navigation#tick and
            // NavigationMixin): destinationBehindTrain, not train.speed, is the authoritative
            // indicator of which physical end of the train is currently leading.
            boolean destinationBehindTrain = navigation.destinationBehindTrain;
            Carriage carriage = destinationBehindTrain
                    ? train.carriages.get(train.carriages.size() - 1)
                    : train.carriages.get(0);
            TravellingPoint leadingPoint = destinationBehindTrain ? carriage.getTrailingPoint() : carriage.getLeadingPoint();
            if (leadingPoint == null || leadingPoint.edge == null) {
                if (debug)
                    System.out.println("[PreLightSignal@" + pos + "] train " + train.id + " has no usable leading point");
                continue;
            }

            TravellingPoint scout = new TravellingPoint(leadingPoint.node1, leadingPoint.node2, leadingPoint.edge,
                    leadingPoint.position, leadingPoint.upsideDown);

            TravellingPoint.ITrackSelector trackSelector = navigation.controlSignalScout();

            MutableDouble selfDistance = new MutableDouble(-1);
            MutableObject<SpeedSignalBoundary> upcoming = new MutableObject<>(null);

            double scanDistance = (proximityRange + LOOKAHEAD_BEYOND_SELF) * (destinationBehindTrain ? -1 : 1);
            scout.travel(graph, scanDistance, trackSelector, (distance, couple) -> {
                TrackEdgePoint point = couple.getFirst();
                TrackNode fromNode = couple.getSecond().getFirst();

                if (point == self) {
                    if (self.isPrimary(fromNode))
                        return false; // train is passing this signal from behind, not relevant
                    selfDistance.setValue(distance);
                    return false; // keep travelling to look for the next limit beyond this signal
                }

                if (selfDistance.doubleValue() >= 0 && point instanceof SpeedSignalBoundary boundary) {
                    if (boundary.isPrimary(fromNode))
                        return false;
                    upcoming.setValue(boundary);
                    return true;
                }

                return false;
            });

            double distanceToSelf = selfDistance.doubleValue();

            if (debug)
                System.out.println("[PreLightSignal@" + pos + "] train " + train.id
                        + " destinationBehindTrain=" + destinationBehindTrain
                        + " selfDistance=" + distanceToSelf
                        + " upcoming=" + (upcoming.getValue() == null ? "none" : upcoming.getValue().getBlockEntityPos()
                        + " (" + upcoming.getValue().getSpeedLimitKilometersPerHour() + " km/h)"));

            if (distanceToSelf < 0 || distanceToSelf > proximityRange)
                continue;

            foundTrain = true;
            if (distanceToSelf < bestProximity) {
                bestProximity = distanceToSelf;
                SpeedSignalBoundary boundary = upcoming.getValue();
                bestTens = boundary == null ? -1
                        : boundary.resetsLimit() ? 0 : boundary.getSpeedLimitKilometersPerHour() / 10;
            }
        }

        if (debug)
            System.out.println("[PreLightSignal@" + pos + "] result previewSpeedTens=" + (foundTrain ? bestTens : -1));

        setPreviewSpeedTens(foundTrain ? bestTens : -1);
    }

    private void setPreviewSpeedTens(int tens) {
        if (this.previewSpeedTens == tens)
            return;
        this.previewSpeedTens = tens;
        markDirty();
        sendData();
    }

    @Override
    protected void read(NbtCompound tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("previewSpeedTens"))
            this.previewSpeedTens = tag.getInt("previewSpeedTens");
    }

    @Override
    protected void write(NbtCompound tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("previewSpeedTens", this.previewSpeedTens);
    }
}
