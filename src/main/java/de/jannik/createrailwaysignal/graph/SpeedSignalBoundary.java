package de.jannik.createrailwaysignal.graph;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.simibubi.create.infrastructure.config.AllConfigs;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.TrackLimitBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class SpeedSignalBoundary extends SingleBlockEntityEdgePoint {

    private int speedLimitKilometersPerHour = 0; // km/h
    private boolean migration = false;

    public SpeedSignalBoundary() {
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        if (blockEntity.getWorld() == null) {
            Createrailwaysignal.LOGGER.error("World ist null in blockEntityAdded");
            return;
        }
        int speedLimitKilometersPerHour = blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(TrackLimitBlock.SPEED_LIMIT) * 10;
        this.setSpeedLimitKilometersPerHour(blockEntity.getWorld(), speedLimitKilometersPerHour);
    }

    @Override
    public void tick(TrackGraph graph, boolean preTrains) {
        super.tick(graph, preTrains);
    }

    @Override
    public void read(NbtCompound nbt, boolean migration, DimensionPalette dimensions) {
        super.read(nbt, migration, dimensions);
        if(!nbt.contains("speedLimitKilometersPerHour")) {
            this.migration = true;
        }
        this.speedLimitKilometersPerHour = nbt.getInt("speedLimitKilometersPerHour");

    }

    @Override
    public void write(NbtCompound nbt, DimensionPalette dimensions) {
        super.write(nbt, dimensions);
        nbt.putInt("speedLimitKilometersPerHour", this.speedLimitKilometersPerHour);
    }

    private void notifyTrains(World world) {
        if (world == null || edgeLocation == null) {
            return;
        }
        
        try {
            TrackGraph graph = Create.RAILWAYS.sided(world)
                    .getGraph(world, edgeLocation.getFirst());
            if (graph == null)
                return;
            
            TrackEdge edge = graph.getConnection(edgeLocation.map(graph::locateNode));
            if (edge == null)
                return;

            SignalPropagator.notifyTrains(graph, edge);
        } catch (Exception e) {
            Createrailwaysignal.LOGGER.error("Fehler beim Benachrichtigen der Züge", e);
        }
    }

    public double calculateSpeedLimit() {
        if(speedLimitKilometersPerHour == 0)
            return 0;

        float topSpeed = AllConfigs.server().trains.trainTopSpeed.getF();
        return speedLimitKilometersPerHour / (topSpeed * 3.6);
    }

    public boolean resetsLimit() {
        return this.speedLimitKilometersPerHour == 0;
    }

    public int getSpeedLimitKilometersPerHour() {
        return speedLimitKilometersPerHour;
    }

    public void setSpeedLimitKilometersPerHour(World world, int speedLimitKilometersPerHour) {
        if(this.speedLimitKilometersPerHour == speedLimitKilometersPerHour)
            return;

        this.speedLimitKilometersPerHour = speedLimitKilometersPerHour;
        this.notifyTrains(world);
    }

    public boolean migration() {
        return this.migration;
    }
}