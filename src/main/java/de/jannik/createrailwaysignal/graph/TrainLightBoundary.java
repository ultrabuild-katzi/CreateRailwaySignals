package de.jannik.createrailwaysignal.graph;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import de.jannik.createrailwaysignal.block.TrainLightBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class TrainLightBoundary extends SingleBlockEntityEdgePoint {

    public TrainLightBoundary() {
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        // Called when the block entity is added to the track
    }

    @Override
    public void tick(TrackGraph graph, boolean preTrains) {
        super.tick(graph, preTrains);
        if (preTrains)
            return;

        // This is called when a train passes this point
        // We can use this to update the light color based on which carriage is passing
    }

    @Override
    public void read(NbtCompound nbt, boolean migration, DimensionPalette dimensions) {
        super.read(nbt, migration, dimensions);
    }

    @Override
    public void write(NbtCompound nbt, DimensionPalette dimensions) {
        super.write(nbt, dimensions);
    }

    public void updateLightForTrain(ServerWorld world, BlockPos pos, Train train, int carriageIndex, int totalCarriages) {
        TrainLightBlock.LightColor targetColor;

        if (carriageIndex == 0) {
            targetColor = TrainLightBlock.LightColor.WHITE;
        } else if (carriageIndex == totalCarriages - 1) {
            targetColor = TrainLightBlock.LightColor.RED;
        } else {
            targetColor = TrainLightBlock.LightColor.WHITE;
        }

        var state = world.getBlockState(pos);
        if (state.contains(TrainLightBlock.LIGHT_COLOR) &&
            state.get(TrainLightBlock.LIGHT_COLOR) != targetColor) {
            world.setBlockState(pos, state.with(TrainLightBlock.LIGHT_COLOR, targetColor));
        }
    }
}
