package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BueSchrankBlock extends HorizontalFacingBlock implements IBE<BueSchrankBlockEntity>, IWrenchable {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public BueSchrankBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient)
            return;
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED))
            world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_LISTENERS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING, POWERED);
    }

    @Override
    public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
        return Objects.requireNonNull(super.getPlacementState(ctx))
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public Class<BueSchrankBlockEntity> getBlockEntityClass() {
        return BueSchrankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BueSchrankBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.BUE_SCHRANK.get();
    }
}
