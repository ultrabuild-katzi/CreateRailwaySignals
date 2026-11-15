package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.track.EmbankmentTrack;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CurvedTrackInteraction.class, remap = false)
public abstract class CurvedTrackEmbankmentInteractionMixin {
    @Inject(method = "onClickInput", at = @At("HEAD"), cancellable = true)
    private static void crs$applyEmbankment(boolean use, boolean attack, CallbackInfoReturnable<Boolean> cir) {
        var client = MinecraftClient.getInstance();
        var selection = TrackBlockOutline.result;
        if (!use || selection == null || client.player == null || client.player.isSpectator())
            return;
        var stack = client.player.getMainHandStack();
        if (!(stack.getItem() instanceof BlockItem) || stack.getItem() instanceof TrackTargetingBlockItem
                || AllTags.AllBlockTags.TRACKS.matches(stack))
            return;
        var curve = selection.blockEntity().getConnections().get(selection.loc().curveTarget());
        if (curve == null)
            return;
        boolean remove = client.player.isSneaking() && ((EmbankmentTrack) curve).crs$getEmbankment() != null;
        if (!remove && Math.abs(TrackCantMath.currentCantMm(curve)) < 0.5)
            return;
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(selection.blockEntity().getPos());
        buf.writeBlockPos(selection.loc().curveTarget());
        buf.writeVarInt(selection.loc().segment());
        buf.writeBoolean(remove);
        ClientPlayNetworking.send(Createrailwaysignal.TRACK_EMBANKMENT, buf);
        client.player.swingHand(Hand.MAIN_HAND);
        cir.setReturnValue(true);
    }
}
