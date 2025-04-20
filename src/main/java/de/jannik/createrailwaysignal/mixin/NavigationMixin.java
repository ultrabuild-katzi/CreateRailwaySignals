package de.jannik.createrailwaysignal.mixin;


import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.content.trains.entity.Train;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import de.jannik.createrailwaysignal.graph.SpeedSignalProvider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Navigation.class, remap = false)
public class NavigationMixin {
    @Shadow public Train train;

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/trains/entity/Train;throttle:D", opcode = Opcodes.GETFIELD))
    public double throttle(Train train){
        SpeedSignalBoundary speedLimit = ((SpeedSignalProvider) train).createRailwaySignal$$speedSignal();
        if(speedLimit == null)
            return train.throttle;

        if(speedLimit.resetsLimit()) {
            return train.throttle;
        } else {

            double throttle = speedLimit.calculateSpeedLimit();
            System.out.println("Throttle: " + throttle);
            return Math.min(train.throttle, throttle);

        }
    }



}
