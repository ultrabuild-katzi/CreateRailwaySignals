package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import net.createmod.catnip.math.VecHelper;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Smooths out a canted (banked) connection's own face normal so it reads flat at both literal
 * endpoints and only reaches the full bank angle in the middle - a "hump" instead of Create's
 * default flat two-point ramp/plateau. This is what {@link TrackCantMath#hump} computes; see
 * there for the exact shape. Used by {@link BezierConnection#getNormal(double)}, which train
 * physics (banking/lean) and the track graph read, so this keeps that in sync with the mesh fix
 * in {@link BezierConnectionBezieratorMixin}.
 * <p>
 * Create's own code takes a shortcut and skips interpolation entirely when both ends share the
 * exact same normal (straight track banked uniformly); the first redirect below defeats that
 * shortcut so our hump always gets a chance to taper even then.
 */
@Mixin(value = BezierConnection.class, remap = false)
public class BezierConnectionNormalMixin {

    @Redirect(method = "getNormal", at = @At(value = "INVOKE", desc = @Desc(owner = Vec3d.class, value = "equals", args = Object.class, ret = boolean.class)))
    private boolean crs$neverShortcut(Vec3d fn1, Object fn2) {
        return false;
    }

    @Redirect(method = "getNormal", at = @At(value = "INVOKE", desc = @Desc(owner = VecHelper.class, value = "slerp", args = {float.class, Vec3d.class, Vec3d.class}, ret = Vec3d.class)))
    private Vec3d crs$hump(float t, Vec3d fn1, Vec3d fn2) {
        BezierConnection self = (BezierConnection) (Object) this;
        double eps = 1.0e-3;
        double t0 = Math.max(0, t - eps);
        double t1 = Math.min(1, t + eps);
        Vec3d localTangent = self.getPosition(t1).subtract(self.getPosition(t0)).normalize();
        return TrackCantMath.hump(t, fn1, fn2, self.axes.getFirst(), self.axes.getSecond(), localTangent, self.isPrimary());
    }
}
