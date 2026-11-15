package de.jannik.createrailwaysignal.client;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Create only keeps a track curve's Flywheel geometry (and, via our own mixin, its embankment
 * mesh) alive for as long as the real {@link TrackBlockEntity} that owns the connection stays
 * loaded. For a curve much longer than the render distance, walking along it eventually puts
 * BOTH of its endpoints out of view at once - at that point the real block entity is gone and
 * there is nothing left client-side to render the curve from, no matter how the fallback in
 * {@link de.jannik.createrailwaysignal.mixin.TrackVisualMixin} is configured.
 * <p>
 * To bridge that gap, {@link TrackVisualChunkListener} hands the about-to-unload block entity's
 * connections to this class, which clones them onto a detached "ghost" block entity that is
 * never part of any chunk. Flywheel only ever tears a block entity's visual down when that block
 * entity is explicitly marked removed (see the chunk-unload path removing the real one); since
 * nothing ever does that to our ghost, its rails/ties/girders keep rendering indefinitely from
 * the cached geometry. The embankment mesh isn't Flywheel-based, so it is driven manually every
 * frame for as long as a ghost is alive (see {@link #renderEmbankments}).
 * <p>
 * Ghosts are retired the moment the real block entity becomes loaded again (handing rendering
 * duty back to it), or pruned once the player has wandered far enough away that they can no
 * longer be relevant.
 */
public final class GhostTrackBlockEntities {
    // Comfortably beyond any vanilla/server render distance, so a ghost only gets pruned once
    // it's clearly no longer bridging a gap the player could be walking through.
    private static final double PRUNE_DISTANCE_SQUARED = 1536.0 * 1536.0;

    private static final Map<BlockPos, TrackBlockEntity> GHOSTS = new HashMap<>();

    private GhostTrackBlockEntities() {
    }

    public static void spawn(ClientWorld world, TrackBlockEntity real) {
        BlockPos pos = real.getPos();
        if (GHOSTS.containsKey(pos))
            return;

        TrackBlockEntity ghost = AllBlockEntityTypes.TRACK.create(pos, real.getCachedState());
        ghost.setWorld(world);
        ghost.getConnections().putAll(real.getConnections());

        if (!VisualizationHelper.tryAddBlockEntity(ghost))
            return;

        GHOSTS.put(pos, ghost);
    }

    public static void despawn(BlockPos pos) {
        TrackBlockEntity ghost = GHOSTS.remove(pos);
        if (ghost == null)
            return;

        VisualizationManager manager = VisualizationManager.get(ghost.getWorld());
        if (manager != null)
            manager.blockEntities().queueRemove(ghost);
    }

    public static void clear() {
        GHOSTS.clear();
    }

    public static void pruneFarAway(PlayerEntity player) {
        if (GHOSTS.isEmpty())
            return;

        GHOSTS.keySet().removeIf(pos -> {
            if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PRUNE_DISTANCE_SQUARED)
                return false;

            TrackBlockEntity ghost = GHOSTS.get(pos);
            VisualizationManager manager = VisualizationManager.get(ghost.getWorld());
            if (manager != null)
                manager.blockEntities().queueRemove(ghost);
            return true;
        });
    }

    // Driven manually each frame: ghosts are never part of a loaded chunk, so vanilla's
    // block entity renderer dispatch (and thus TrackEmbankmentRendererMixin) never sees them.
    public static void renderEmbankments(MatrixStack matrices, VertexConsumerProvider buffers) {
        for (TrackBlockEntity ghost : GHOSTS.values())
            TrackEmbankmentRenderer.render(ghost, matrices, buffers);
    }
}
