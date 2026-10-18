package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class LightSignalSpeedBlockEntity extends SmartBlockEntity {

    private BlockPos targetBlock;
    private String lastDebugLog;

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
        // Never let a bad/stale targetBlock crash the renderer for this one instance
        // (the block model would still render fine, but the text overlay would silently
        // disappear for good since the renderer throws before drawing anything).
        try {
            return computeDisplayedStrings();
        } catch (Exception e) {
            if (this.world != null && this.world.getGameRules().get(CreaterailwayCommands.SHOW_PRE_LIGHT_SIGNAL).get()) {
                System.out.println("[LightSignalSpeed@" + pos + "] getDisplayedStrings() threw: " + e);
                e.printStackTrace();
            }
            return "ERR";
        }
    }

    private String computeDisplayedStrings() {
        assert this.world != null;
        boolean debug = this.world.getGameRules().get(CreaterailwayCommands.SHOW_PRE_LIGHT_SIGNAL).get();

        if (targetBlock == null) {
            logDebugIfChanged(debug, "targetBlock is null -> \"N/A\"");
            return "N/A";
        }

        if (this.world.getBlockEntity(targetBlock) instanceof PreLightSignalBlockEntity preLightSignal) {
            int preview = preLightSignal.getPreviewSpeedTens();
            // No train currently near/navigating past the Pre Light Signal: show a blank sign,
            // not "N/A" - this isn't an error state, it's just nothing to display right now.
            String result = preview < 0 ? "" : String.valueOf(preview);
            logDebugIfChanged(debug, "linked to PreLightSignal@" + targetBlock + " previewSpeedTens=" + preview + " -> \"" + result + "\"");
            return result;
        }

        BlockState blockState = this.world.getBlockState(targetBlock);
        if (!blockState.contains(TrackLimitBlock.SPEED_LIMIT)) {
            logDebugIfChanged(debug, "targetBlock=" + targetBlock + " block=" + blockState.getBlock()
                    + " has no PreLightSignalBlockEntity and no SPEED_LIMIT property -> \"N/A\"");
            return "N/A";
        }

        int speedLimit = blockState.get(TrackLimitBlock.SPEED_LIMIT);

        if (speedLimit > 99 || speedLimit < 0) {
            logDebugIfChanged(debug, "targetBlock=" + targetBlock + " SPEED_LIMIT=" + speedLimit + " out of bounds -> \"N/A\"");
            return "N/A";
        }

        logDebugIfChanged(debug, "targetBlock=" + targetBlock + " TrackLimitBlock.SPEED_LIMIT=" + speedLimit);
        return String.valueOf(speedLimit);
    }

    /** Logs at most once per distinct state, to avoid spamming the log every render frame. */
    private void logDebugIfChanged(boolean debug, String message) {
        if (!debug)
            return;
        String full = "[LightSignalSpeed@" + pos + "] " + message;
        if (full.equals(lastDebugLog))
            return;
        lastDebugLog = full;
        System.out.println(full);
    }

}
