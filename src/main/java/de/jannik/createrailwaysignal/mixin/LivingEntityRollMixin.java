package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import de.jannik.createrailwaysignal.train.CarriageRoll;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entities (players included) riding a seat on a banked carriage otherwise render perfectly
 * upright even while the carriage around them leans into the curve. This tilts the whole
 * rendered model - around the carriage's own heading in world space, not the rider's own look
 * direction, so looking around doesn't change which way "down" leans - to match
 * {@link OrientedContraptionEntityRollMixin}'s body roll.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRollMixin {

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void createRailwaySignal$$applyRiderRoll(LivingEntity entity, float yaw, float partialTicks,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (RailwaySignalConfigs.client() != null && !RailwaySignalConfigs.client().tiltRiders.get())
            return;

        Entity vehicle = entity.getVehicle();
        if (!(vehicle instanceof CarriageContraptionEntity carriageEntity))
            return;

        Carriage carriage = carriageEntity.getCarriage();
        if (carriage == null)
            return;

        float roll = CarriageRoll.average(carriage, partialTicks);
        if (roll == 0)
            return;

        // Rotate into the carriage's heading, roll around that (now world-aligned) forward
        // axis, then rotate back out - so the lean always follows the track, never the rider's
        // own camera/look direction.
        float heading = vehicle.getYaw(partialTicks);
        TransformStack.of(matrixStack)
                .rotateYDegrees(180 - heading)
                .rotateZDegrees(-roll)
                .rotateYDegrees(-(180 - heading));
    }

}
