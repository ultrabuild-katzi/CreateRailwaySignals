package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin implements SpeedSignalProvider {
    @Unique
    public SpeedSignalBoundary createRailwaySignal$$speedSignal;

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    public void frontSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var returnvalue = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                this.createRailwaySignal$$speedSignal = speedSignalBoundary;
                System.out.println("Change speed limit to " + speedSignalBoundary.getSpeedLimitKilometersPerHour() + " km/h");
                return false;
            }

            return returnvalue.test(distance, couple);
        });
    }

    @Override
    public SpeedSignalBoundary createRailwaySignal$$speedSignal() {
        return this.createRailwaySignal$$speedSignal;
    }
}
