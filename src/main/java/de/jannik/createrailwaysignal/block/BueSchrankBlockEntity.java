package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BueSchrankBlockEntity extends SmartBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation UP_ANIM = RawAnimation.begin().thenPlayAndHold("up");
    private static final RawAnimation DOWN_ANIM = RawAnimation.begin().thenPlayAndHold("down");

    public BueSchrankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            BlockState state = getCachedState();
            if (state.getBlock() instanceof BueSchrankBlock) {
                if (state.get(BueSchrankBlock.POWERED)) {
                    return event.setAndContinue(DOWN_ANIM);
                } else {
                    return event.setAndContinue(UP_ANIM);
                }
            }
            return event.setAndContinue(UP_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public Box getRenderBoundingBox() {
        // Extend the bounding box to cover a larger area, assuming the rod extends upwards.
        // The default block is 1x1x1. We're extending it 2 blocks upwards.
        return new Box(pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10, pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10);
    }
}