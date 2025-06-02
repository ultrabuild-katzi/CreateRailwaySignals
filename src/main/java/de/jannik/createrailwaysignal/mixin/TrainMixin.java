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

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    public void frontSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var returnvalue = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                this.createRailwaySignal$$speedSignal = speedSignalBoundary;
                System.out.println("Change speed limit to " + speedSignalBoundary.getSpeedLimitKilometersPerHour() + " km/h");
                return false;
            }

            if (couple.getFirst() instanceof WhistleBlockBoundary whistleBlockBoundary) {
                whistleBlockBoundary.playSound((Train)((Object) this));

                return false;
            }

            return returnvalue.test(distance, couple);
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
