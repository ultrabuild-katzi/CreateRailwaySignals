package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DyeColor;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BrSignBlock extends HorizontalFacingBlock implements IWrenchable, IBE<BrSignBlockEntity> {

    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.of("dye_color", DyeColor.class);
    public static final BooleanProperty JEB_MODE = BooleanProperty.of("jeb");

    public BrSignBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(DYE_COLOR, DyeColor.WHITE).with(JEB_MODE, false).with(FACING, net.minecraft.util.math.Direction.NORTH));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        Item held = player.getStackInHand(hand).getItem();
        if (held instanceof WrenchItem) return ActionResult.PASS; // let Create handle wrench hooks

        // ... no quick resize here to avoid blocking the editor open; keep other interactions below

        if (held instanceof DyeItem dyeItem) {
            if (world.isClient) return ActionResult.SUCCESS;
            // shift + dye -> set per-sign text color in BlockEntity (dye the number)
            if (player.isSneaking()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BrSignBlockEntity brBe) {
                    brBe.setTextColor(dyeItem.getColor());
                    brBe.sync();
                    player.sendMessage(Text.literal("Sign text color set to " + dyeItem.getColor().toString()), true);
                    return ActionResult.SUCCESS;
                }
            }
            // otherwise dye the block's dye color property
            world.setBlockState(pos, state.with(DYE_COLOR, dyeItem.getColor()));
            return ActionResult.SUCCESS;
        }

        if (held instanceof NameTagItem) {
            // NameTag with name "_jeb" toggles jeb mode
            String input = player.getStackInHand(hand).getName().getString();
            if ("_jeb".equals(input)) {
                if (!world.isClient) {
                    world.setBlockState(pos, state.with(JEB_MODE, !state.get(JEB_MODE)));
                }
                return ActionResult.SUCCESS;
            }
        }

        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity beRaw = world.getBlockEntity(pos);
        if (!(beRaw instanceof BrSignBlockEntity be)) return ActionResult.PASS;

        // Server: send open-editor packet to the interacting player with current text and width
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeString(be.getDisplayedString());
            buf.writeInt(be.getWidth());
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, de.jannik.createrailwaysignal.Createrailwaysignal.BR_SIGN_OPEN, buf);
            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(FACING, DYE_COLOR, JEB_MODE);
    }

    @Override
    public Class<BrSignBlockEntity> getBlockEntityClass() {
        return BrSignBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BrSignBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.BR_SIGN.get();
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext ctx) {
        var world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        var pos = ctx.getBlockPos();
        var be = world.getBlockEntity(pos);
        if (!(be instanceof BrSignBlockEntity brBe)) return ActionResult.PASS;
        var side = ctx.getSide();

        int vertDelta = 10; // pixels for vertical moves

        // Top or bottom -> vertical movement (both faces behave same: RMB = up, Shift+RMB = down)
        if (side == net.minecraft.util.math.Direction.UP || side == net.minecraft.util.math.Direction.DOWN) {
            brBe.adjustVerticalOffset(vertDelta);
            brBe.sync();
            if (ctx.getPlayer() != null) ctx.getPlayer().sendMessage(Text.literal("Text vertical offset: " + brBe.getVerticalOffset()));
            return ActionResult.SUCCESS;
        } else {
            // Seiten: move depth (vor/zurück). RMB -> vormoven (decrease depth), so use -depthDelta
            brBe.adjustDepth(-1);
            brBe.sync();
            if (ctx.getPlayer() != null) ctx.getPlayer().sendMessage(Text.literal("Text depth: " + brBe.getDepth()));
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext ctx) {
        var world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        var pos = ctx.getBlockPos();
        var be = world.getBlockEntity(pos);
        if (!(be instanceof BrSignBlockEntity brBe)) return ActionResult.PASS;
        var side = ctx.getSide();

        int vertDelta = 10;

        // Top or bottom -> vertical movement down
        if (side == net.minecraft.util.math.Direction.UP || side == net.minecraft.util.math.Direction.DOWN) {
            brBe.adjustVerticalOffset(-vertDelta);
            brBe.sync();
            if (ctx.getPlayer() != null) ctx.getPlayer().sendMessage(Text.literal("Text vertical offset: " + brBe.getVerticalOffset()));
            return ActionResult.SUCCESS;
        } else {
            // Seiten: move depth (hintermoven). Shift+RMB -> increase depth
            brBe.adjustDepth(1);
            brBe.sync();
            if (ctx.getPlayer() != null) ctx.getPlayer().sendMessage(Text.literal("Text depth: " + brBe.getDepth()));
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Nur Spieler sollen durch den Block gehen können
        if (context instanceof EntityShapeContext esc) {
            var entity = esc.getEntity();
            if (entity instanceof PlayerEntity) return VoxelShapes.empty();
        }
        return super.getCollisionShape(state, world, pos, context);
    }



}
