package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Works around Create's curved/sloped tracks fully disappearing when the chunk holding a
 * connection's "primary" end unloads, even though the other end (and the rest of the curve)
 * might still be in view. {@link de.jannik.createrailwaysignal.mixin.TrackVisualMixin} makes a
 * block entity willing to render its non-primary copy of a connection once its real primary is
 * unloaded; this class nudges Flywheel to actually re-check that condition promptly whenever a
 * relevant chunk loads or unloads, instead of waiting for an unrelated NBT resync.
 * <p>
 * For curves longer than the render distance, both of a connection's ends can end up unloaded
 * at once (e.g. while walking through the middle of the curve), at which point there is no
 * live block entity left to fall back to at all. {@link GhostTrackBlockEntities} covers that
 * case by keeping a detached copy of the connection rendering independently of chunk state.
 */
public final class TrackVisualChunkListener {

    private TrackVisualChunkListener() {
    }

    public static void register() {
        ClientChunkEvents.CHUNK_UNLOAD.register(TrackVisualChunkListener::onChunkUnload);
        ClientChunkEvents.CHUNK_LOAD.register(TrackVisualChunkListener::onChunkLoad);
    }

    // A primary connection's chunk just unloaded: ask the still-loaded counterpart to pick up
    // rendering duty for its own (secondary) copy of that connection, and also leave a detached
    // "ghost" copy of this block entity behind so the curve keeps rendering even once the
    // counterpart itself goes out of view too (see GhostTrackBlockEntities).
    private static void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof TrackBlockEntity trackBe))
                continue;

            boolean hasPrimary = false;
            for (BezierConnection bc : trackBe.getConnections().values()) {
                if (!bc.isPrimary())
                    continue;

                hasPrimary = true;
                refreshCounterpart(world, bc.getKey());
            }

            if (hasPrimary)
                GhostTrackBlockEntities.spawn(world, trackBe);
        }
    }

    // A chunk just (re)loaded: if it holds the true primary end of a connection, tell the
    // counterpart to stop standing in now that the real primary is rendering again.
    //
    // It can also hold the *secondary* end of a connection whose real primary is far away and
    // was never loaded in this session (so it will never fire an unload event of its own). That
    // secondary copy's own Flywheel visual may have been built once, before this chunk ever
    // loaded, and would otherwise never re-check whether it should render as the fallback - so
    // nudge it to re-check its own connections right now too.
    private static void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof TrackBlockEntity trackBe))
                continue;

            // The real block entity is back - it can take rendering duty back from any ghost
            // that was standing in for it while its chunk was unloaded.
            GhostTrackBlockEntities.despawn(trackBe.getPos());

            boolean hasSecondary = false;
            for (BezierConnection bc : trackBe.getConnections().values()) {
                if (bc.isPrimary()) {
                    refreshCounterpart(world, bc.getKey());
                } else {
                    hasSecondary = true;
                }
            }

            if (hasSecondary)
                VisualizationHelper.queueUpdate(trackBe);
        }
    }

    private static void refreshCounterpart(ClientWorld world, BlockPos counterpartPos) {
        if (!world.isChunkLoaded(counterpartPos.getX() >> 4, counterpartPos.getZ() >> 4))
            return;

        BlockEntity counterpart = world.getBlockEntity(counterpartPos);
        if (counterpart instanceof TrackBlockEntity)
            VisualizationHelper.queueUpdate(counterpart);
    }
}
