package de.jannik.createrailwaysignal.graph;

import com.simibubi.create.content.trains.graph.EdgePointType;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.minecraft.util.Identifier;

public class CustomEdgePointType {
    public static final EdgePointType<SpeedSignalBoundary> SPEED_SIGNAL =
            EdgePointType.register(new Identifier(Createrailwaysignal.MOD_ID, "speed_signal"), SpeedSignalBoundary::new);

    public static final EdgePointType<WhistleBlockBoundary> WHISTLE_BLOCK =
            EdgePointType.register(new Identifier(Createrailwaysignal.MOD_ID, "whistle_block"), WhistleBlockBoundary::new);



}