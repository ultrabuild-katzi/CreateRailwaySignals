package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import de.jannik.createrailwaysignal.train.RollableBogey;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create's CarriageBogey only tracks yaw/pitch, so trains never visually lean on a banked curve
 * even though the rails themselves are tilted ({@link de.jannik.createrailwaysignal.util.TrackCantMath}).
 * This adds a smoothed roll angle, computed from the track's own face-normal at the bogey's
 * current position, which {@code CarriageContraptionEntityRendererMixin} reads to tilt the
 * rendered bogey/wheel model to match the rail.
 */
@Mixin(value = CarriageBogey.class, remap = false)
public abstract class CarriageBogeyMixin implements RollableBogey {

    @Unique
    private LerpedFloat createRailwaySignal$$rollField;

    @Override
    public LerpedFloat createRailwaySignal$$roll() {
        if (createRailwaySignal$$rollField == null)
            createRailwaySignal$$rollField = LerpedFloat.angular();
        return createRailwaySignal$$rollField;
    }

    @Inject(method = "updateAngles", at = @At("TAIL"))
    private void createRailwaySignal$$updateRoll(CarriageContraptionEntity entity, double distanceMoved, CallbackInfo ci) {
        CarriageBogey self = (CarriageBogey) (Object) this;
        double rollDeg = 0;

        TravellingPoint point = self.leading();
        if (point.edge != null && self.carriage != null && self.carriage.train != null && !self.carriage.train.derailed) {
            // Banking/cant is only ever a curve concept here (TrackCantItem rotates a
            // BezierConnection's own face normals); straight edges store an unrelated per-shape
            // normal (e.g. portal track shapes use a purely sideways normal) that must never be
            // read as a bank angle, so roll is forced flat off of curves.
            if (point.edge.isTurn()) {
                double length = point.edge.getLength();
                if (length > 0) {
                    double t = point.position / length;
                    Vec3d normal = point.edge.getNormal(self.carriage.train.graph, t);
                    Vec3d tangent = point.edge.getDirectionAt(point.position);
                    rollDeg = TrackCantMath.rollDegrees(normal, tangent);
                }
            }
        }

        for (boolean twice : Iterate.trueAndFalse) {
            if (twice && !entity.firstPositionUpdate)
                continue;
            createRailwaySignal$$roll().setValue(rollDeg);
        }
    }

}
