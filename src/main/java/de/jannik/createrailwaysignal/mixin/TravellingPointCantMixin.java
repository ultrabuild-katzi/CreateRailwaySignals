package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.track.BezierConnection;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps cant out of the position offset used for train anchors and yaw/pitch calculations. */
@Mixin(value = TravellingPoint.class, remap = false)
public abstract class TravellingPointCantMixin {

    @Redirect(method = "getPositionWithOffset", at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/graph/TrackEdge;getNormal(Lcom/simibubi/create/content/trains/graph/TrackGraph;D)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d crs$positionNormal(TrackEdge edge, TrackGraph graph, double t) {
        Vec3d normal = edge.getNormal(graph, t);
        if (!edge.isTurn()) {
            // The adjoining straight edge can inherit a banked node normal from the curve.
            // Portal normals are sideways and must retain their special positioning offset.
            return normal.y > 1.0e-9
                    ? TrackCantMath.positionNormal(normal, edge.getDirectionAt(t * edge.getLength()))
                    : normal;
        }

        BezierConnection curve = edge.getTurn();
        double handleLength = curve.getHandleLength();
        Vec3d start = curve.starts.getFirst();
        Vec3d end = curve.starts.getSecond();
        Vec3d tangent = VecHelper.bezierDerivative(start, end,
                start.add(curve.axes.getFirst().multiply(handleLength)),
                end.add(curve.axes.getSecond().multiply(handleLength)),
                (float) Math.max(0, Math.min(1, t))).normalize();
        return TrackCantMath.positionNormal(normal, tangent);
    }
}
