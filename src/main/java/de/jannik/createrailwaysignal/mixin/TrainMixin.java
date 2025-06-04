package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import de.jannik.createrailwaysignal.graph.WhistleBlockBoundary;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin implements SpeedSignalProvider {

    @Unique
    public SpeedSignalBoundary createRailwaySignal$$speedSignal;

    @Unique
    public boolean createRailwaySignal$$encounteredWhiteBlock = false;


    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/entity/Carriage;travel(Lnet/minecraft/world/World;Lcom/simibubi/create/content/trains/graph/TrackGraph;DLcom/simibubi/create/content/trains/entity/TravellingPoint;Lcom/simibubi/create/content/trains/entity/TravellingPoint;I)D",
                    shift = At.Shift.AFTER
            )
    )
    public void tick(World level, CallbackInfo ci) {
        if(createRailwaySignal$$encounteredWhiteBlock) {
            WhistleBlockBoundary.playSound(level, (Train) ((Object) this));
            this.createRailwaySignal$$encounteredWhiteBlock = false;
        }
    }

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    public void frontSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var returnValue = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                this.createRailwaySignal$$speedSignal = speedSignalBoundary;
                System.out.println("Change speed limit to " + speedSignalBoundary.getSpeedLimitKilometersPerHour() + " km/h");
                return false;
            }

            if (couple.getFirst() instanceof WhistleBlockBoundary) {
                this.createRailwaySignal$$encounteredWhiteBlock = true;
                return false;
            }

            return returnValue.test(distance, couple);
        });
    }

    @Inject(method = "collideWithOtherTrains", at = @At("HEAD"), cancellable = true)
    public void collideWithOtherTrains(World level, Carriage carriage, CallbackInfo ci) {
        ci.cancel();
    }


    @Override
    public SpeedSignalBoundary createRailwaySignal$$speedSignal() {
        return this.createRailwaySignal$$speedSignal;
    }
}
