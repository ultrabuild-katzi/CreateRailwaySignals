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
 */
public final class TrackVisualChunkListener {

    private TrackVisualChunkListener() {
    }

    public static void register() {
        ClientChunkEvents.CHUNK_UNLOAD.register(TrackVisualChunkListener::onChunkUnload);
        ClientChunkEvents.CHUNK_LOAD.register(TrackVisualChunkListener::onChunkLoad);
    }

    // A primary connection's chunk just unloaded: ask the still-loaded counterpart to pick up
    // rendering duty for its own (secondary) copy of that connection.
    private static void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof TrackBlockEntity trackBe))
                continue;

            for (BezierConnection bc : trackBe.getConnections().values()) {
                if (!bc.isPrimary())
                    continue;

                refreshCounterpart(world, bc.getKey());
            }
        }
    }

    // A chunk just (re)loaded: if it holds the true primary end of a connection, tell the
    // counterpart to stop standing in now that the real primary is rendering again.
    private static void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof TrackBlockEntity trackBe))
                continue;

            for (BezierConnection bc : trackBe.getConnections().values()) {
                if (!bc.isPrimary())
                    continue;

                refreshCounterpart(world, bc.getKey());
            }
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
