package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the same "hump" shaping as {@link BezierConnectionNormalMixin} to the private
 * {@code BezierConnection.Bezierator} iterator, which is what actually bakes the rendered
 * rail/tie/girder mesh ({@code SegmentAngles}/{@code GirderAngles} both iterate a connection via
 * this class). Without this, the fix in {@link BezierConnectionNormalMixin} would only affect
 * train physics, not the visible seam the mesh itself has at a banked connection's endpoints.
 */
@Mixin(targets = "com.simibubi.create.content.trains.track.BezierConnection$Bezierator", remap = false)
public class BezierConnectionBezieratorMixin {

    @Shadow
    private Vec3d end1;
    @Shadow
    private Vec3d end2;
    @Shadow
    private Vec3d finish1;
    @Shadow
    private Vec3d finish2;
    @Shadow
    private BezierConnection.Segment segment;

    @Unique
    private boolean crs$primary;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void crs$capturePrimary(BezierConnection bc, Vec3d offset, CallbackInfo ci) {
        crs$primary = bc.isPrimary();
    }

    @Redirect(method = "next", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;equals(Ljava/lang/Object;)Z"))
    private boolean crs$neverShortcut(Vec3d fn1, Object fn2) {
        return false;
    }

    @Redirect(method = "next", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/VecHelper;slerp(FLnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d crs$hump(float t, Vec3d fn1, Vec3d fn2) {
        // The connection's own start/end tangent directions are just the (normalized) direction
        // from each end's anchor point to its bezier handle - same vectors as BezierConnection's
        // public "axes" field, just not directly reachable from here.
        Vec3d tangentStart = finish1.subtract(end1).normalize();
        Vec3d tangentEnd = finish2.subtract(end2).normalize();
        return TrackCantMath.hump(t, fn1, fn2, tangentStart, tangentEnd, segment.derivative, crs$primary);
    }
}
