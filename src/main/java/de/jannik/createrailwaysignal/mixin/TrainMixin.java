package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
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

/** Mixin into Create's Train class for speed-block logging + whistle trigger. */
@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin implements SpeedSignalProvider {

    @Unique
    private SpeedSignalBoundary createRailwaySignal$$speedSignal;

    @Unique
    private boolean createRailwaySignal$$encounteredWhiteBlock = false;

    @Unique
    private World createRailwaySignal$$worldRef;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(World level, CallbackInfo ci) {
        this.createRailwaySignal$$worldRef = level;

        if (createRailwaySignal$$encounteredWhiteBlock) {
            WhistleBlockBoundary.playSound(level, (Train) (Object) this);
            this.createRailwaySignal$$encounteredWhiteBlock = false;
        }
    }

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    private void frontSignalListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        var original = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof SpeedSignalBoundary speedSignalBoundary) {
                this.createRailwaySignal$$speedSignal = speedSignalBoundary;

                World w = this.createRailwaySignal$$worldRef;
                if (w != null && w.getGameRules().get(CreaterailwayCommands.SHOW_SPEED_BLOCK).get()) {
                    var pos = speedSignalBoundary.getBlockEntityPos();
                    System.out.println(
                            "Change speed limit to " + speedSignalBoundary.getSpeedLimitKilometersPerHour() +
                                    " km/h at: x= " + pos.getX() + " y= " + pos.getY() + " z= " + pos.getZ()
                    );
                }
                return false;
            }

            if (couple.getFirst() instanceof de.jannik.createrailwaysignal.graph.WhistleBlockBoundary) {
                this.createRailwaySignal$$encounteredWhiteBlock = true;
                return false;
            }

            return original.test(distance, couple);
        });
    }

    @Inject(method = "collideWithOtherTrains", at = @At("HEAD"), cancellable = true)
    private void collideWithOtherTrains(World level, Carriage carriage, CallbackInfo ci) {
        ci.cancel();
    }

    // SpeedSignalProvider impl
    @Override
    public SpeedSignalBoundary createRailwaySignal$$speedSignal() {
        return this.createRailwaySignal$$speedSignal;
    }
}
