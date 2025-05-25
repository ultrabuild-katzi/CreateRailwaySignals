package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class WhistleBlock extends Block implements IBE<WhistleBlockEntity>, IWrenchable {

    public WhistleBlock(Settings settings) {
        super(settings);
    }



    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add();
    }

    @Override
    public Class<WhistleBlockEntity> getBlockEntityClass() {
        return WhistleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WhistleBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.WHISTLE_BLOCK.get();
    }
}
