package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import de.jannik.createrailwaysignal.train.CarriageRoll;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link CarriageContraptionEntityRendererMixin} only tilts each bogey's own wheel/truck model;
 * the actual carriage body (the blocks the player built) is positioned by
 * {@link OrientedContraptionEntity#applyLocalTransforms}, which never knew about bank/roll at
 * all. This mixes the (averaged, for carriages riding two independently-leaning bogeys) roll
 * angle into that same transform, right before it un-centers, so the whole car leans together
 * instead of only its wheels.
 */
@Mixin(value = OrientedContraptionEntity.class, remap = false)
public abstract class OrientedContraptionEntityRollMixin {

    @Inject(method = "applyLocalTransforms",
            at = @At(value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/lib/transform/PoseTransformStack;uncenter()Ldev/engine_room/flywheel/lib/transform/Translate;",
                    shift = At.Shift.BEFORE))
    private void createRailwaySignal$$applyBodyRoll(MatrixStack matrixStack, float partialTicks, CallbackInfo ci) {
        if (!((Object) this instanceof CarriageContraptionEntity carriageEntity))
            return;

        Carriage carriage = carriageEntity.getCarriage();
        if (carriage == null)
            return;

        float roll = CarriageRoll.average(carriage, partialTicks);
        if (roll == 0)
            return;

        // rotateXDegrees produced a pitch-like (nose up/down) tilt instead of a sideways lean,
        // so empirically Z is the axis that rolls this body chain; X is used for slope pitch.
        TransformStack.of(matrixStack).rotateZDegrees(roll);
    }

}
