package de.jannik.createrailwaysignal.block.kilometer;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class KilometerMarkerBlock extends BlockWithEntity implements IWrenchable {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;


    private static final VoxelShape SHAPE_NORTH = VoxelShapes.cuboid(1/16.0, 0.0,       15/16.0,   15/16.0, 1.0,    1.0);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.cuboid(1/16.0, 0.0,       0.0,       15/16.0, 1.0,    1/16.0);
    private static final VoxelShape SHAPE_WEST  = VoxelShapes.cuboid(15/16.0, 0.0,      1/16.0,    1.0,     1.0,    15/16.0);
    private static final VoxelShape SHAPE_EAST  = VoxelShapes.cuboid(0.0,     0.0,      1/16.0,    1/16.0,  1.0,    15/16.0);


    public KilometerMarkerBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new KilometerMarkerBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // BER-only rendering (your numbers renderer draws the text; plate comes from BER or model as you choose)
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    // ===== Outline / Collision / Raycast shapes (rotate with FACING) =====
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeFor(state.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // If you want it walk-through, return VoxelShapes.empty(); (keep outline/raycast for clicks)
        return shapeFor(state.get(FACING));
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return shapeFor(state.get(FACING));
    }

    private static VoxelShape shapeFor(Direction dir) {
        return switch (dir) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH; // NORTH
        };
    }

    // =========================
    // Hand (= PLUS) interactions
    // =========================
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof KilometerMarkerBlockEntity marker)) return ActionResult.PASS;

        boolean shift = player.isSneaking();
        Item held = player.getStackInHand(hand).getItem();

        // Only handle empty-hand here; Create's wrench goes to IWrenchable below
        if (player.getStackInHand(hand).isEmpty()) {
            if (shift) {
                marker.setMeters((marker.getMeters() + 100) % 1000);   // m +100 (wrap)
                player.sendMessage(Text.literal("Meters: " + marker.getMeters()), true);
            } else {
                marker.setKilometer(marker.getKilometer() + 1);        // km +1
                player.sendMessage(Text.literal("Kilometer: " + marker.getKilometer()), true);
            }
            marker.sync();
            return ActionResult.CONSUME;
        }

        if (held instanceof WrenchItem) {
            // Let Create handle wrench via IWrenchable
            return ActionResult.PASS;
        }

        return ActionResult.PASS;
    }

    // ======================================
    // Wrench (= MINUS) interactions via Create
    // ======================================
    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        BlockPos pos = ctx.getBlockPos();
        PlayerEntity player = ctx.getPlayer();
        if (!(world.getBlockEntity(pos) instanceof KilometerMarkerBlockEntity marker) || player == null)
            return ActionResult.PASS;

        // Right-click with wrench => km -1 (clamp at 0)
        int newKm = Math.max(0, marker.getKilometer() - 1);
        marker.setKilometer(newKm);
        player.sendMessage(Text.literal("Kilometer: " + newKm), true);
        marker.sync();
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        BlockPos pos = ctx.getBlockPos();
        PlayerEntity player = ctx.getPlayer();
        if (!(world.getBlockEntity(pos) instanceof KilometerMarkerBlockEntity marker) || player == null)
            return ActionResult.PASS;

        // Shift + right-click with wrench => m -100 (wrap)
        marker.setMeters((marker.getMeters() + 900) % 1000);
        player.sendMessage(Text.literal("Meters: " + marker.getMeters()), true);
        marker.sync();
        return ActionResult.SUCCESS;
    }
}
