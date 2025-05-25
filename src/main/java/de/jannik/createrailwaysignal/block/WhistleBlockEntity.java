package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.WhistleBlockBoundary;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class WhistleBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity {
    public TrackTargetingBehaviour<WhistleBlockBoundary> edgePoint;

    public WhistleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void transform(StructureTransform transform) {
        this.edgePoint.transform(transform);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.edgePoint = new TrackTargetingBehaviour<>(this, CustomEdgePointType.WHISTLE_BLOCK);
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
}