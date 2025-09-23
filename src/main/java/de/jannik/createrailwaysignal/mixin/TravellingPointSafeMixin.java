package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(TravellingPoint.class)
public abstract class TravellingPointSafeMixin {

    @Inject(method = "getPositionWithOffset", at = @At("HEAD"), cancellable = true, remap = false)
    private void crs$guardNullEdge_offset(TrackGraph trackGraph, double offset, boolean flipUpsideDown, CallbackInfoReturnable<Vec3d> cir) {
        if (crs$edgeIsNull(this)) {
            cir.setReturnValue(Vec3d.ZERO);
        }
    }

    private static boolean crs$edgeIsNull(Object self) {
        try {
            Class<?> c = self.getClass();
            while (c != null) {
                for (Field f : c.getDeclaredFields()) {
                    Class<?> t = f.getType();
                    if (t != null && t.getName().endsWith("content.trains.graph.TrackEdge")) {
                        f.setAccessible(true);
                        return f.get(self) == null;
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
