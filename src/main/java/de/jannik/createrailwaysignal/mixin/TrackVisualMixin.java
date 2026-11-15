package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackVisual;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes curved/sloped track pieces fully disappearing when the chunk holding the connection's
 * "primary" end unloads, even though the other end (and thus the whole curve, since
 * {@link BezierConnection#starts} are absolute world positions) is still within view.
 * <p>
 * Create only ever builds render geometry for a connection from the end marked
 * {@link BezierConnection#isPrimary()}; the other end just keeps a mirrored copy for bookkeeping.
 * This redirects that check so a block entity also renders its own (non-primary) copy whenever
 * the real primary's chunk isn't currently loaded, picking up the slack until it comes back.
 */
@Mixin(value = TrackVisual.class, remap = false)
public class TrackVisualMixin {

    @Shadow
    @Final
    protected TrackBlockEntity blockEntity;

    @Redirect(method = "createInstance", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/BezierConnection;isPrimary()Z"))
    private boolean crs$renderAsOrphanIfPrimaryUnloaded(BezierConnection bc) {
        if (bc.isPrimary())
            return true;

        World world = blockEntity.getWorld();
        if (world == null)
            return false;

        BlockPos primaryPos = bc.getKey();
        return !world.isChunkLoaded(primaryPos.getX() >> 4, primaryPos.getZ() >> 4);
    }
}
