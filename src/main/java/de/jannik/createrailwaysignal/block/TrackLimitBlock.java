package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class TrackLimitBlock extends Block implements IBE<TrackLimitBlockEntity>, IWrenchable {
    public static final int MAX_SPEED = 20;
    public static final int MIN_SPEED = 0; // 0 → Speed Limit aufgehoben
    public static final IntProperty SPEED_LIMIT = IntProperty.of("speed_limit", MIN_SPEED, MAX_SPEED);

    public TrackLimitBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(SPEED_LIMIT, MIN_SPEED)); // 0 = no speed limit
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof LightSignalSpeedItem) {
            return ActionResult.PASS;
        }

        int value = state.get(SPEED_LIMIT) + (player.isSneaking() ? -1 : 1);
        if(value > MAX_SPEED)
            value = MIN_SPEED;
        if(value < MIN_SPEED)
            value = MAX_SPEED;

        player.sendMessage(Text.of("Speed Limit: " + (value * 10) + " km/h"), true);
        world.setBlockState(pos, state.with(SPEED_LIMIT, value));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof TrackLimitBlockEntity trackLimitBlockEntity) {
            trackLimitBlockEntity.updateSpeed(value * 10);
        }

        return ActionResult.SUCCESS;
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SPEED_LIMIT); // cognitive loser
    }

    @Override
    public Class<TrackLimitBlockEntity> getBlockEntityClass() {
        return TrackLimitBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrackLimitBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.TRACK_LIMIT.get();
    }
}
