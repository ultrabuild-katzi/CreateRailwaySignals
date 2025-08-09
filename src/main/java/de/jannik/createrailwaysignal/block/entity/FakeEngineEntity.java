package de.jannik.createrailwaysignal.block.entity;

import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class FakeEngineEntity extends BlockEntity {

    public final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank(); // This should always be FluidVariant.blank() for fluid storages.
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            // Here, you can pick your capacity depending on the fluid variant.
            // For example, if we want to store 8 buckets of any fluid:
            return 0L;
        }

        @Override
        protected void onFinalCommit() {
            markDirty();
        }

        @Override
        protected boolean canInsert(FluidVariant variant) {
            // Optional: can be used to prevent insertion of some fluid variants.
            return true;
        }

        @Override
        protected boolean canExtract(FluidVariant variant) {
            // Optional: can be used to prevent extraction of some fluid variants.
            return true;
        }
    };
    public FakeEngineEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FAKE_ENGINE_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        fluidStorage.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        fluidStorage.amount = nbt.getLong("amount");
    }
}