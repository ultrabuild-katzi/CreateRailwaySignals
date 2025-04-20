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
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpeedSignalBoundary extends SingleBlockEntityEdgePoint {

    private int speedLimitKilometersPerHour = 0; // km/h
    private World world;
    private BlockEntity blockEntity;

    public SpeedSignalBoundary() {
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        if (blockEntity.getWorld() == null) {
            Createrailwaysignal.LOGGER.error("World ist null in blockEntityAdded");
            return;
        }
        this.setSpeedLimitKilometersPerHour(blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(TrackLimitBlock.SPEED_LIMIT) * 10);
        this.world = blockEntity.getWorld();
        this.blockEntity = blockEntity;
    }

    @Override
    public void tick(TrackGraph graph, boolean preTrains) {
        super.tick(graph, preTrains);
        if(this.blockEntity == null) {
            MinecraftServer server = Createrailwaysignal.server;
            server.send(new ServerTask(server.getTicks(), this::removeFromAllGraphs));
            Createrailwaysignal.LOGGER.warn("Detected broken speed signal, scheduled removal from all graphs");
        }
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

    public void setSpeedLimitKilometersPerHour(int speedLimitKilometersPerHour) {
        this.speedLimitKilometersPerHour = speedLimitKilometersPerHour;
        notifyTrains(this.world);
    }
}