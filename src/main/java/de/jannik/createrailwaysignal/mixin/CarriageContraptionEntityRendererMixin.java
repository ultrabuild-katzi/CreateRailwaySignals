package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntityRenderer;
import de.jannik.createrailwaysignal.train.RollableBogey;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@link CarriageBogeyMixin}'s roll angle on top of Create's own bogey transform. This
 * static method is used both by the plain renderer and by the Flywheel visual path, so a single
 * injection here covers both rendering modes.
 */
@Mixin(value = CarriageContraptionEntityRenderer.class, remap = false)
public abstract class CarriageContraptionEntityRendererMixin {

    @Inject(method = "translateBogey", at = @At("TAIL"))
    private static void createRailwaySignal$$applyRoll(MatrixStack ms, CarriageBogey bogey, int bogeySpacing,
                                                         float viewYRot, float viewXRot, float partialTicks, CallbackInfo ci) {
        if (bogey instanceof RollableBogey rollable) {
            float roll = RailwaySignalConfigs.renderedBogeyRoll(rollable.createRailwaySignal$$roll().getValue(partialTicks));
            // Negated: the bogey's local Z axis at this point in the transform chain rolls
            // opposite to the track-normal-derived sign TrackCantMath.rollDegrees produces.
            TransformStack.of(ms).rotateZDegrees(-roll);
        }
    }

}
