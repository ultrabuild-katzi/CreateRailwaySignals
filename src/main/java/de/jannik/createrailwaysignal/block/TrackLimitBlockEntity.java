package de.jannik.createrailwaysignal.block;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import de.jannik.createrailwaysignal.graph.SpeedSignalBoundary;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

import java.util.List;

public class TrackLimitBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {
    public TrackTargetingBehaviour<SpeedSignalBoundary> edgePoint;
    public ScrollValueBehaviour speedLimit;

    public TrackLimitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        this.edgePoint.transform(blockEntity, transform);

    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        this.edgePoint = new TrackTargetingBehaviour<>(this, CustomEdgePointType.SPEED_SIGNAL);
        behaviours.add(this.edgePoint);

        speedLimit = new ScrollValueBehaviour(Text.translatable("createrailwaysignal.track_limit.speed_limit"), this, new TrackLimitValueBox());
        speedLimit.between(0, 50);
        speedLimit.withCallback(i -> {
            if (world != null && !world.isClient) {
                world.setBlockState(pos, getCachedState().with(TrackLimitBlock.SPEED_LIMIT, i), 3);
                updateSpeed(null, i * 10);
            }
        });
        behaviours.add(speedLimit);
    }

    @Override
    public void remove() {
        super.remove();
        this.destroy();
    }

    @Override
    public void tick() {
        super.tick();
        SpeedSignalBoundary boundary = this.edgePoint.getEdgePoint();
        if(boundary != null && boundary.migration())
            boundary.setSpeedLimitKilometersPerHour(this.world, this.getCachedState().get(TrackLimitBlock.SPEED_LIMIT) * 10);
    }


    public void updateSpeed(PlayerEntity player, int value) {
        if (this.edgePoint == null) {
            throw new IllegalStateException("Track targeting behaviour not initialized");
        }

        if (this.edgePoint.getEdgePoint() == null) {
            if (player != null) {
                player.sendMessage(Text.literal("Failed updating edge point, are you clicking too quickly?"));
            }
            return;
        }

        this.edgePoint.getEdgePoint().setSpeedLimitKilometersPerHour(player != null ? player.getWorld() : this.world, value);
    }

    private class TrackLimitValueBox extends ValueBoxTransform {
        protected Direction hitSide;

        @Override
        public Vec3d getLocalOffset(WorldAccess world, BlockPos pos, BlockState state) {
            Direction side = hitSide;
            if (side == null) side = Direction.UP;
            double distance = 0.5;
            if (side.getAxis() == Direction.Axis.Y) {
                distance += 0.125;
            } else {
                distance -= 1 / 16.0;
            }
            return new Vec3d(0.5, 0.5, 0.5).add(new Vec3d(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()).multiply(distance));
        }

        @Override
        public void rotate(WorldAccess world, BlockPos pos, BlockState state, net.minecraft.client.util.math.MatrixStack ms) {
            Direction side = hitSide;
            if (side == null) side = Direction.UP;

            if (side == Direction.UP) {
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
            } else if (side == Direction.DOWN) {
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            } else {
                float yRot = side == Direction.SOUTH ? 0 : side == Direction.WEST ? 90 : side == Direction.NORTH ? 180 : side == Direction.EAST ? 270 : 0;
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot));
            }
        }

        @Override
        public boolean testHit(WorldAccess world, BlockPos pos, BlockState state, Vec3d localHit) {
            Direction bestSide = Direction.UP;
            double bestDistance = Double.MAX_VALUE;
            for (Direction side : Direction.values()) {
                Vec3d sideCenter = new Vec3d(0.5, 0.5, 0.5).add(new Vec3d(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()).multiply(0.5));
                double distance = localHit.distanceTo(sideCenter);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestSide = side;
                }
            }
            hitSide = bestSide;
            return true;
        }
    }


}