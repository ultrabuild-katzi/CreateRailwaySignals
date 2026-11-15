package de.jannik.createrailwaysignal.mixin;

import com.simibubi.create.content.trains.track.BezierConnection;
import de.jannik.createrailwaysignal.track.EmbankmentTrack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BezierConnection.class, remap = false)
public abstract class BezierConnectionEmbankmentMixin implements EmbankmentTrack {
    @Unique private static final String CRS_EMBANKMENT = "CRSEmbankment";
    @Unique private BlockState crs$embankment;

    @Override
    public @Nullable BlockState crs$getEmbankment() {
        return crs$embankment;
    }

    @Override
    public void crs$setEmbankment(@Nullable BlockState state) {
        crs$embankment = state;
    }

    @Inject(method = {"clone", "secondary"}, at = @At("RETURN"))
    private void crs$copyMaterial(CallbackInfoReturnable<BezierConnection> cir) {
        ((EmbankmentTrack) cir.getReturnValue()).crs$setEmbankment(crs$embankment);
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"))
    private void crs$readMaterial(NbtCompound tag, BlockPos localTo, CallbackInfo ci) {
        if (tag.contains(CRS_EMBANKMENT))
            crs$embankment = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), tag.getCompound(CRS_EMBANKMENT));
    }

    @Inject(method = "write(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void crs$writeMaterial(BlockPos localTo, CallbackInfoReturnable<NbtCompound> cir) {
        if (crs$embankment != null)
            cir.getReturnValue().put(CRS_EMBANKMENT, NbtHelper.fromBlockState(crs$embankment));
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void crs$readPacketMaterial(PacketByteBuf buffer, CallbackInfo ci) {
        if (buffer.readBoolean())
            crs$embankment = Block.getStateFromRawId(buffer.readVarInt());
    }

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void crs$writePacketMaterial(PacketByteBuf buffer, CallbackInfo ci) {
        buffer.writeBoolean(crs$embankment != null);
        if (crs$embankment != null)
            buffer.writeVarInt(Block.getRawIdFromState(crs$embankment));
    }

    @Inject(method = "equalsSansMaterialInner", at = @At("RETURN"), cancellable = true)
    private void crs$compareMaterial(BezierConnection other, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && other != null
                && crs$embankment != ((EmbankmentTrack) other).crs$getEmbankment())
            cir.setReturnValue(false);
    }
}
