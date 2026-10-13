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
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;


public class LightSignalSpeedBlock extends HorizontalFacingBlock implements IBE<LightSignalSpeedBlockEntity> {
    public static final EnumProperty<Position> POSITION = EnumProperty.of("position", Position.class);
    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.of("dye_color", DyeColor.class);
    public static final BooleanProperty JEB_MODE = BooleanProperty.of("jeb");

    public enum Position implements StringIdentifiable {
        INNER(5 / 16f), MIDDLE(0), OUTER(-6 / 16f);

        // offset in blocks along the direction the sign faces
        public final float offset;

        Position(float offset) {
            this.offset = offset;
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

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
        float off = state.get(POSITION).offset;
        double cx = 0.5 + dir.getOffsetX() * off;
        double cz = 0.5 + dir.getOffsetZ() * off;

        return switch (dir) {
            case NORTH, SOUTH -> VoxelShapes.cuboid(0.0, -0.2, cz - 0.1, 1.0, 1.25, cz + 0.1);
            case EAST, WEST -> VoxelShapes.cuboid(cx - 0.1, -0.2, 0.0, cx + 0.1, 1.25, 1.0);
            default -> VoxelShapes.fullCube();
        };
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POSITION).add(Properties.HORIZONTAL_FACING).add(DYE_COLOR).add(JEB_MODE); // cognitive loser
    }

    @Override
    public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing();
        Vec3d local = ctx.getHitPos().subtract(Vec3d.of(ctx.getBlockPos()));
        double t = facing.getAxis() == Direction.Axis.X ? local.x : local.z;
        if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) t = 1 - t;
        // t: 0 = side nearest the player, 1 = far side
        Position position = t < 1 / 3.0 ? Position.OUTER : t > 2 / 3.0 ? Position.INNER : Position.MIDDLE;

        return Objects.requireNonNull(super.getPlacementState(ctx))
                .with(Properties.HORIZONTAL_FACING, facing)
                .with(POSITION, position)
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
