package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;

public class TrackLimitBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity {
    public TrackTargetingBehaviour<SpeedSignalBoundary> edgePoint;

    public TrackLimitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void transform(StructureTransform transform) {
        this.edgePoint.transform(transform);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.edgePoint = new TrackTargetingBehaviour<>(this, CustomEdgePointType.SPEED_SIGNAL);
        behaviours.add(this.edgePoint);
    }

    @Override
    public void remove() {
        super.remove();
        this.destroy();
    }

    @Override
    protected void removeBehaviour(BehaviourType<?> type) {
        super.removeBehaviour(type);
    }

    public void updateSpeed(int value) {
        if(this.edgePoint.getEdgePoint() == null)
            this.edgePoint.createEdgePoint();

        this.edgePoint.getEdgePoint().setSpeedLimitKilometersPerHour(value);
    }
}
