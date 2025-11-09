package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TrainLightBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity {
    private boolean isOnTrain = false;
    private boolean isAtFront = false;
    private Integer assignedCarriageId = null;
    private BlockPos relativePosition = null;
    public TrainLightBlock.LightColor currentColor = TrainLightBlock.LightColor.WHITE; // Made public for renderer access

    public TrainLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void transform(StructureTransform transform) {
        isOnTrain = true;

        // Find and remember which carriage we're on
        if (world != null) {
            List<CarriageContraptionEntity> nearby = world.getEntitiesByClass(
                CarriageContraptionEntity.class,
                new net.minecraft.util.math.Box(pos).expand(10),
                entity -> entity.getCarriage() != null
            );

            if (!nearby.isEmpty()) {
                CarriageContraptionEntity carriage = nearby.get(0);
                assignedCarriageId = carriage.getCarriage().id;
                relativePosition = pos.subtract(carriage.getBlockPos());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        if (world == null || world.isClient)
            return;

        // Only check if we're marked as being on a train
        if (!isOnTrain || assignedCarriageId == null) {
            // Check every 20 ticks if we should be on a train
            if (world.getTime() % 20 == 0) {
                checkIfOnTrain();
            }
            return;
        }

        // Check every 20 ticks (1 second) to save performance
        if (world.getTime() % 20 != 0)
            return;

        updateTrainLightState();
    }

    private void checkIfOnTrain() {
        // This is a fallback check - try to assign ourselves to a nearby carriage
        List<CarriageContraptionEntity> nearby = world.getEntitiesByClass(
            CarriageContraptionEntity.class,
            new net.minecraft.util.math.Box(pos).expand(10),
            entity -> entity.getCarriage() != null && entity.getCarriage().train != null
        );

        if (!nearby.isEmpty()) {
            // Find the closest carriage
            CarriageContraptionEntity closest = null;
            double minDistance = Double.MAX_VALUE;

            for (CarriageContraptionEntity carriage : nearby) {
                double distance = Math.sqrt(pos.getSquaredDistance(carriage.getBlockPos()));
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = carriage;
                }
            }

            if (closest != null && minDistance < 15) {
                // We found a carriage we're probably on!
                assignedCarriageId = closest.getCarriage().id;
                relativePosition = pos.subtract(closest.getBlockPos());
                isOnTrain = true;
                // Don't spam the message anymore
                return;
            }
        }
    }

    private void updateTrainLightState() {
        if (world == null || assignedCarriageId == null)
            return;

        // Find OUR specific carriage by ID
        List<CarriageContraptionEntity> allCarriages = world.getEntitiesByClass(
            CarriageContraptionEntity.class,
            new net.minecraft.util.math.Box(pos).expand(200),
            entity -> entity.getCarriage() != null &&
                     entity.getCarriage().train != null &&
                     entity.getCarriage().id == assignedCarriageId
        );

        if (allCarriages.isEmpty()) {
            return;
        }

        CarriageContraptionEntity ourCarriage = allCarriages.get(0);
        Carriage carriage = ourCarriage.getCarriage();
        Train train = carriage.train;

        if (train != null && !train.carriages.isEmpty()) {
            int carriageIndex = train.carriages.indexOf(carriage);
            int totalCarriages = train.carriages.size();

            // Get train speed to determine direction
            double trainSpeed = train.speed;
            boolean isMovingBackward = trainSpeed < 0;

            TrainLightBlock.LightColor targetColor;

            // Determine if this is front or back based on direction
            boolean isPhysicallyFirst = (carriageIndex == 0);
            boolean isPhysicallyLast = (carriageIndex == totalCarriages - 1);

            // If moving backward, the "front" and "back" are swapped
            boolean isEffectiveFront = isMovingBackward ? isPhysicallyLast : isPhysicallyFirst;
            boolean isEffectiveBack = isMovingBackward ? isPhysicallyFirst : isPhysicallyLast;

            if (isEffectiveFront) {
                targetColor = TrainLightBlock.LightColor.WHITE;
                isAtFront = true;
            } else if (isEffectiveBack) {
                targetColor = TrainLightBlock.LightColor.RED;
                isAtFront = false;
            } else {
                targetColor = TrainLightBlock.LightColor.WHITE;
                isAtFront = false;
            }

            // Update internal state and sync to client
            if (currentColor != targetColor) {
                currentColor = targetColor;
                markDirty();
                sendData();
            }
        }
    }

    public TrainLightBlock.LightColor getCurrentColor() {
        return currentColor;
    }

    @Override
    protected void read(NbtCompound tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        isOnTrain = tag.getBoolean("isOnTrain");
        isAtFront = tag.getBoolean("isAtFront");
        if (tag.contains("assignedCarriageId")) {
            assignedCarriageId = tag.getInt("assignedCarriageId");
        }
        if (tag.contains("relativePosition")) {
            NbtCompound relPos = tag.getCompound("relativePosition");
            relativePosition = new BlockPos(
                relPos.getInt("x"),
                relPos.getInt("y"),
                relPos.getInt("z")
            );
        }
        if (tag.contains("currentColor")) {
            TrainLightBlock.LightColor newColor = TrainLightBlock.LightColor.valueOf(tag.getString("currentColor").toUpperCase());
            if (clientPacket && newColor != currentColor) {
                currentColor = newColor;

                // Force client to re-render this block entity
                if (world != null && world.isClient) {
                    world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                }
            } else {
                currentColor = newColor;
            }
        }
    }

    @Override
    protected void write(NbtCompound tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putBoolean("isOnTrain", isOnTrain);
        tag.putBoolean("isAtFront", isAtFront);
        if (assignedCarriageId != null) {
            tag.putInt("assignedCarriageId", assignedCarriageId);
        }
        if (relativePosition != null) {
            NbtCompound relPos = new NbtCompound();
            relPos.putInt("x", relativePosition.getX());
            relPos.putInt("y", relativePosition.getY());
            relPos.putInt("z", relativePosition.getZ());
            tag.put("relativePosition", relPos);
        }
        tag.putString("currentColor", currentColor.asString());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // No special behaviours needed
    }
}
