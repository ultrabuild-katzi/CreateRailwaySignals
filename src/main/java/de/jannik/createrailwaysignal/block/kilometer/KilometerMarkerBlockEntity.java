package de.jannik.createrailwaysignal.block.kilometer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class KilometerMarkerBlockEntity extends BlockEntity {
    private int kilometer = 0; // 0..?
    private int meters = 0;    // 0..999

    public KilometerMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModKilometerContent.KILOMETER_MARKER_BE, pos, state);
    }

    public int getKilometer() {
        return kilometer;
    }
    public int getMeters() {
        return meters;
    }

    public void setKilometer(int km) {
        this.kilometer = Math.max(0, km);
        markDirty();
        sync();
    }

    public void setMeters(int m) {
        this.meters = Math.floorMod(m, 1000);
        markDirty();
        sync();
    }

    // ---- Persist to NBT ----
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("km", kilometer);
        nbt.putInt("m", meters);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.kilometer = nbt.getInt("km");
        this.meters = nbt.getInt("m");
    }

    // ---- Networking: send updates to client ----
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    /** Call after changing km/m to sync to nearby clients. */
    public void sync() {
        if (world == null) return;
        if (!world.isClient && world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
        world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
    }
}
