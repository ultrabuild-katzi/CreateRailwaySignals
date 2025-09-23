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
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class KilometerMarkerBlock extends BlockWithEntity implements IWrenchable {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty CONNECTED = BooleanProperty.of("connected");

    // thin post along the back side
    private static final VoxelShape SHAPE_NORTH = VoxelShapes.cuboid(1/16.0, 0.0,       0.0,       15/16.0, 1.0, 1/16.0);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapes.cuboid(1/16.0, 0.0,       15/16.0,   15/16.0, 1.0, 1.0);
    private static final VoxelShape SHAPE_WEST  = VoxelShapes.cuboid(0.0,     0.0,      1/16.0,    1/16.0,  1.0, 15/16.0);
    private static final VoxelShape SHAPE_EAST  = VoxelShapes.cuboid(15/16.0, 0.0,      1/16.0,    1.0,     1.0, 15/16.0);

    public KilometerMarkerBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(CONNECTED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(FACING, CONNECTED);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new KilometerMarkerBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /* ---------------- Placement (simple 90°) ----------------
       - If placed ON a connectable lattice: facing = clickedFace, connected = true
       - Else: facing = playerFacing.opposite, connected = behind-scan
    --------------------------------------------------------- */
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos placePos = ctx.getBlockPos();

        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        boolean connected = false;

        // Block we clicked to place against
        Direction clickedFace = ctx.getSide();
        BlockPos clickedPos = placePos.offset(clickedFace.getOpposite());
        BlockState clickedState = world.getBlockState(clickedPos);

        if (KilometerMarkerHelper.isConnectable(clickedState)) {
            // 180° preference you requested earlier: front == clickedFace (back hugs the lattice)
            facing = clickedFace;
            connected = true; // definitely touching it
        } else {
            // not clicked on a connectable → keep default facing, probe behind
            connected = KilometerMarkerHelper.hasConnectableBehind(world, placePos,
                    getDefaultState().with(FACING, facing));
        }

        return getDefaultState().with(FACING, facing).with(CONNECTED, connected);
    }

    /* -------- Neighbor updates: do nothing (no auto-rotation) -------- */
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state; // keep it simple and stable
    }

    /* ---------------- Shapes ---------------- */
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeFor(state.get(FACING));
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeFor(state.get(FACING));
    }
    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return shapeFor(state.get(FACING));
    }
    private static VoxelShape shapeFor(Direction dir) {
        return switch (dir) {
            case SOUTH -> SHAPE_NORTH;
            case NORTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_WEST;
            case WEST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;
        };
    }

    /* ---------------- Interactions (unchanged controls) ----------------
       Right-click           -> KM +1
       Shift + Right-click   -> M  +100
       Wrench Right-click    -> KM -1
       Wrench + Shift        -> M  -100 (wrap 0..900)
    --------------------------------------------------------------------- */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        Item held = player.getStackInHand(hand).getItem();
        if (held instanceof WrenchItem) return ActionResult.PASS; // let Create handle wrench hooks

        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof KilometerMarkerBlockEntity be)) return ActionResult.PASS;

        if (player.isSneaking()) {
            int m = be.getMeters();
            m = ((m + 100) % 1000 + 1000) % 1000;
            be.setMeters(m);
            player.sendMessage(Text.literal("Meters: " + String.format("%03d", be.getMeters())));
        } else {
            int km = be.getKilometer();
            be.setKilometer(Math.max(0, km + 1));
            player.sendMessage(Text.literal("Kilometer: " + be.getKilometer()));
        }
        be.sync();
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(ctx.getBlockPos()) instanceof KilometerMarkerBlockEntity be)) return ActionResult.PASS;

        int km = be.getKilometer();
        be.setKilometer(Math.max(0, km - 1));
        if (ctx.getPlayer() != null) {
            ctx.getPlayer().sendMessage(Text.literal("Kilometer: " + be.getKilometer()));
        }
        be.sync();
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(ctx.getBlockPos()) instanceof KilometerMarkerBlockEntity be)) return ActionResult.PASS;

        int m = be.getMeters();
        m -= 100;
        while (m < 0) m += 1000;
        m %= 1000;
        m = (m / 100) * 100;

        be.setMeters(m);
        if (ctx.getPlayer() != null) {
            ctx.getPlayer().sendMessage(Text.literal("Meters: " + String.format("%03d", be.getMeters())));
        }
        be.sync();
        return ActionResult.SUCCESS;
    }
}
