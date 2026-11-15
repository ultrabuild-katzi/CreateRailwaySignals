package de.jannik.createrailwaysignal.util;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.TrackLimitBlock;
import de.jannik.createrailwaysignal.block.TrackLimitBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side logic backing the {@link de.jannik.createrailwaysignal.item.TrackCantItem} GUI:
 * looking up the targeted curve, finding a nearby speed limit for the "auto" suggestion, and
 * applying the chosen cant to both ends of the connection.
 */
public final class TrackCantNetworking {

    /** How far (in blocks) to search for a nearby speed-limit sign for the auto-suggestion. */
    private static final int SPEED_SEARCH_RADIUS = 10000;

    private TrackCantNetworking() {
    }

    public static void handleOpen(ServerPlayerEntity player, BlockPos ownerPos, BlockPos targetPos) {
        World world = player.getWorld();
        BezierConnection bc = getConnection(world, ownerPos, targetPos);
        if (bc == null)
            return;

        double radius = TrackCantMath.estimateRadiusMeters(bc);
        double currentCantMm = TrackCantMath.currentCantMm(bc);

        TrackLimitBlockEntity nearestLimit = findNearestSpeedLimit(world, ownerPos);
        int foundSpeedKmh = 0;
        double autoCantMm = 0;
        if (nearestLimit != null) {
            foundSpeedKmh = world.getBlockState(nearestLimit.getPos()).get(TrackLimitBlock.SPEED_LIMIT) * 10;
            autoCantMm = TrackCantMath.balancingCantMm(foundSpeedKmh, radius);
        }

        var buf = PacketByteBufs.create();
        buf.writeBlockPos(ownerPos);
        buf.writeBlockPos(targetPos);
        buf.writeFloat((float) radius);
        buf.writeFloat((float) currentCantMm);
        buf.writeInt(foundSpeedKmh);
        buf.writeFloat((float) autoCantMm);
        ServerPlayNetworking.send(player, Createrailwaysignal.TRACK_CANT_DATA, buf);
    }

    public static void handleApply(ServerPlayerEntity player, BlockPos ownerPos, BlockPos targetPos, float cantMm, boolean leanRight) {
        World world = player.getWorld();
        if (!(world.getBlockEntity(ownerPos) instanceof TrackBlockEntity ownerBe))
            return;
        BezierConnection bc = ownerBe.getConnections().get(targetPos);
        if (bc == null)
            return;

        double clamped = Math.max(0, Math.min(TrackCantMath.MAX_CANT_MM, cantMm));
        BezierConnection updated = bc.clone();
        TrackCantMath.applyCantMm(updated, clamped, leanRight);
        ownerBe.addConnection(updated);

        if (world.getBlockEntity(targetPos) instanceof TrackBlockEntity targetBe)
            targetBe.addConnection(updated.secondary());

        player.sendMessage(Text.literal(String.format("Track cant set to %.0f mm", clamped)), true);
    }

    @Nullable
    private static BezierConnection getConnection(World world, BlockPos ownerPos, BlockPos targetPos) {
        if (!(world.getBlockEntity(ownerPos) instanceof TrackBlockEntity ownerBe))
            return null;
        return ownerBe.getConnections().get(targetPos);
    }

    @Nullable
    private static TrackLimitBlockEntity findNearestSpeedLimit(World world, BlockPos origin) {
        TrackLimitBlockEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        double searchRadiusSq = (double) SPEED_SEARCH_RADIUS * SPEED_SEARCH_RADIUS;

        ChunkPos originChunk = new ChunkPos(origin);
        int chunkRadius = (SPEED_SEARCH_RADIUS >> 4) + 1;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                int chunkX = originChunk.x + dx;
                int chunkZ = originChunk.z + dz;
                if (!world.isChunkLoaded(chunkX, chunkZ))
                    continue;

                WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof TrackLimitBlockEntity limit))
                        continue;
                    int speed = world.getBlockState(limit.getPos()).get(TrackLimitBlock.SPEED_LIMIT);
                    if (speed <= 0)
                        continue;

                    double distSq = limit.getPos().getSquaredDistance(origin);
                    if (distSq <= searchRadiusSq && distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = limit;
                    }
                }
            }
        }
        return nearest;
    }
}
