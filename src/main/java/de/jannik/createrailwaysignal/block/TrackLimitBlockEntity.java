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
    if (this.edgePoint == null) {
        throw new IllegalStateException("Track targeting behaviour not initialized");
    }

    if (this.edgePoint.getEdgePoint() == null) {
        // Log position information for debugging
        BlockPos pos = this.getPos();
        System.out.println("Attempting to create edge point at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        
        this.edgePoint.createEdgePoint();
        
        if (this.edgePoint.getEdgePoint() == null) {
            throw new IllegalStateException("Edge point creation failed at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() 
                + " - Ensure block is placed adjacent to a valid track");
        }
    }

    this.edgePoint.getEdgePoint().setSpeedLimitKilometersPerHour(value);
}
}