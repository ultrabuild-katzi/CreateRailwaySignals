package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TrainLightBlock extends HorizontalFacingBlock implements IBE<TrainLightBlockEntity>, IWrenchable {
    public static final EnumProperty<LightColor> LIGHT_COLOR = EnumProperty.of("light_color", LightColor.class);

    public TrainLightBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIGHT_COLOR, LightColor.WHITE));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking()) {
            if (!world.isClient) {
                // Manual toggle when not on train (will be overridden by BlockEntity if on train)
                LightColor currentColor = state.get(LIGHT_COLOR);
                LightColor newColor = currentColor == LightColor.WHITE ? LightColor.RED : LightColor.WHITE;
                world.setBlockState(pos, state.with(LIGHT_COLOR, newColor));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING, LIGHT_COLOR);
    }

    @Override
    public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
        return Objects.requireNonNull(super.getPlacementState(ctx))
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(LIGHT_COLOR, LightColor.WHITE);
    }

    @Override
    public Class<TrainLightBlockEntity> getBlockEntityClass() {
        return TrainLightBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrainLightBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.TRAIN_LIGHT.get();
    }

    public enum LightColor implements StringIdentifiable {
        WHITE("white"),
        RED("red");

        private final String name;

        LightColor(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
