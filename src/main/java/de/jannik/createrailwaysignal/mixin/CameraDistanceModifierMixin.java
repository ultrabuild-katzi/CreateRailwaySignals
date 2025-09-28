package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.CameraDistanceModifier;
import de.jannik.createrailwaysignal.config.CameraConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks Create's "zoom back in" while mounted on Create entities when enabled.
 * /camera reset allows one normalize pass-through.
 */
@Mixin(value = CameraDistanceModifier.class, remap = false)
public class CameraDistanceModifierMixin {

    @Inject(method = "reset", at = @At("HEAD"), cancellable = true)
    private static void crs$blockZoomInWhileOnCreateMount(CallbackInfo ci) {
        // Allow exactly one reset() to pass (requested by /camera reset)
        if (CameraConfig.consumeNormalizeOnce())
            return;

        if (!CameraConfig.isUnzoomEnabled())
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null)
            return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null)
            return;

        Identifier id = Registries.ENTITY_TYPE.getId(vehicle.getType());
        if (id != null && "create".equals(id.getNamespace())) {
            ci.cancel(); // keep zoom-out; block zoom-in while mounted
        }
    }
}
