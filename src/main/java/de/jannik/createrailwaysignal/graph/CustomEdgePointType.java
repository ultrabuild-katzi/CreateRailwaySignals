package de.jannik.createrailwaysignal.graph;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.minecraft.util.Identifier;

public class CustomEdgePointType {

    public static final EdgePointType<SpeedSignalBoundary> SPEED_SIGNAL =
            EdgePointType.register(new Identifier(Createrailwaysignal.MOD_ID, "speed_signal"), SpeedSignalBoundary::new);

}
