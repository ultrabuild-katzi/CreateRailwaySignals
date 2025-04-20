package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class LightSignalSpeedBlock extends HorizontalFacingBlock implements IBE<LightSignalSpeedBlockEntity> {
    public static final BooleanProperty MIDDLE = BooleanProperty.of("middle");
    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.of("dye_color", DyeColor.class);
    public static final BooleanProperty JEB_MODE = BooleanProperty.of("jeb");

    protected LightSignalSpeedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        if (item instanceof DyeItem dyeItem) {
            world.setBlockState(pos, state.with(DYE_COLOR, dyeItem.getColor()));
            return ActionResult.SUCCESS;
        }
        if(item instanceof NameTagItem) {
            String input = stack.getName().getString();
            if("_jeb".equals(input)) {
                world.setBlockState(pos, state.with(JEB_MODE, !state.get(JEB_MODE)));
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);

        return switch (dir) {
            case NORTH, SOUTH -> VoxelShapes.cuboid(0.0f, -0.2f, 0.4f, 1.0f, 1.25f, 0.6f);
            case EAST, WEST -> VoxelShapes.cuboid(0.4f, -0.2f, 0.0f, 0.6f, 1.25f, 1.0f);
            default -> VoxelShapes.fullCube();
        };
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MIDDLE).add(Properties.HORIZONTAL_FACING).add(DYE_COLOR).add(JEB_MODE); // cognitive loser
    }

    @Override
    public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
        Vec3d centerOfBlock = ctx.getBlockPos().toCenterPos();
        centerOfBlock = new Vec3d(centerOfBlock.x, ctx.getHitPos().y, centerOfBlock.z);
        boolean isMiddle = ctx.getHitPos().isInRange(centerOfBlock, 0.25);

        return Objects.requireNonNull(super.getPlacementState(ctx))
                .with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing())
                .with(MIDDLE, isMiddle)
                .with(DYE_COLOR, DyeColor.WHITE)
                .with(JEB_MODE, false);
    }


    @Override
    public Class<LightSignalSpeedBlockEntity> getBlockEntityClass() {
        return LightSignalSpeedBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LightSignalSpeedBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.LIGHT_SIGNAL_SPEED.get();
    }
}
