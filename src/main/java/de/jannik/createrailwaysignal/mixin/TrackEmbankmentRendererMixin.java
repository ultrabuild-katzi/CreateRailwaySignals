package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackRenderer;
import de.jannik.createrailwaysignal.client.TrackEmbankmentRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrackRenderer.class, remap = false)
public abstract class TrackEmbankmentRendererMixin {
    // Before Create's Flywheel early return, so the embankment works with either renderer.
    @Inject(method = "renderSafe", at = @At("HEAD"))
    private void crs$renderEmbankment(TrackBlockEntity be, float partialTicks, MatrixStack matrices,
                                    VertexConsumerProvider buffers, int light, int overlay, CallbackInfo ci) {
        TrackEmbankmentRenderer.render(be, matrices, buffers);
    }
}
