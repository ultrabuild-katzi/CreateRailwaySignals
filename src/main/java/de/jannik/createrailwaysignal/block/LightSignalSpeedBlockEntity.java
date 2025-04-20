package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class LightSignalSpeedBlockEntity extends SmartBlockEntity {

    private BlockPos targetBlock;

    public LightSignalSpeedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.targetBlock = this.pos.add(0, -1, 0);
    }

    @Override
    protected void read(NbtCompound tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        BlockPos targetBlock = readTargetBlock(tag);
        if(targetBlock != null)
            this.targetBlock = targetBlock;
    }

    @Override
    protected void write(NbtCompound tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        writeTargetBlock(tag, this.targetBlock);
    }

    public static BlockPos readTargetBlock(NbtCompound tag) {
        NbtCompound targetBlock = tag.getCompound("targetBlock");
        if (targetBlock == null) {
            return null;
        }

        return new BlockPos(
                targetBlock.getInt("x"),
                targetBlock.getInt("y"),
                targetBlock.getInt("z")
        );
    }


    public static void writeTargetBlock(NbtCompound tag, BlockPos targetBlock) {
        NbtCompound targetBlockNbt = new NbtCompound();
        targetBlockNbt.putInt("x", targetBlock.getX());
        targetBlockNbt.putInt("y", targetBlock.getY());
        targetBlockNbt.putInt("z", targetBlock.getZ());
        tag.put("targetBlock", targetBlockNbt);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public String getDisplayedStrings() {
        assert this.world != null;
        BlockState blockState = this.world.getBlockState(targetBlock);
        if (!blockState.contains(TrackLimitBlock.SPEED_LIMIT)) {
            return "N/A";
        }

        int speedLimit = blockState.get(TrackLimitBlock.SPEED_LIMIT);

        if (speedLimit > 99 || speedLimit < 0) {
            throw new IllegalArgumentException("Speed Limit out of bounds, must be between 0 and 99.");
        }

        return String.format("%02d", speedLimit);
    }

}
