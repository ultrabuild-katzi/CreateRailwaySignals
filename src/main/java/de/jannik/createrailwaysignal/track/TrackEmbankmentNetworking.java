package de.jannik.createrailwaysignal.track;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class TrackEmbankmentNetworking {
    private TrackEmbankmentNetworking() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(Createrailwaysignal.TRACK_EMBANKMENT, (server, player, handler, buf, sender) -> {
            BlockPos owner = buf.readBlockPos();
            BlockPos target = buf.readBlockPos();
            int segment = buf.readVarInt();
            boolean remove = buf.readBoolean();
            server.execute(() -> apply(player, owner, target, segment, remove));
        });
    }

    private static void apply(ServerPlayerEntity player, BlockPos owner, BlockPos target, int segment, boolean remove) {
        var world = player.getServerWorld();
        if (player.isSpectator() || !player.canModifyBlocks()
                || !world.isChunkLoaded(owner) || !world.isChunkLoaded(target)
                || !world.canPlayerModifyAt(player, owner) || !world.canPlayerModifyAt(player, target)) {
            error(player, "unavailable");
            return;
        }
        if (!(world.getBlockEntity(owner) instanceof TrackBlockEntity be)
                || !(world.getBlockEntity(target) instanceof TrackBlockEntity other)) {
            error(player, "unavailable");
            return;
        }
        BezierConnection curve = be.getConnections().get(target);
        if (curve == null || segment < 0 || segment > curve.getSegmentCount()
                || player.getEyePos().squaredDistanceTo(curve.getPosition(curve.getSegmentT(segment))) > 49) {
            error(player, "unavailable");
            return;
        }
        if (!(player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof BlockItem item)) {
            error(player, "material_required");
            return;
        }
        if (!remove && Math.abs(TrackCantMath.currentCantMm(curve)) < 0.5) {
            error(player, "bank_required");
            return;
        }
        BezierConnection updated = curve.clone();
        ((EmbankmentTrack) updated).crs$setEmbankment(remove ? null : item.getBlock().getDefaultState());
        be.addConnection(updated);
        other.addConnection(updated.secondary());
        player.sendMessage(Text.translatable("message.createrailwaysignal.embankment." + (remove ? "removed" : "applied")), true);
    }

    private static void error(ServerPlayerEntity player, String key) {
        player.sendMessage(Text.translatable("message.createrailwaysignal.embankment." + key), true);
    }
}
