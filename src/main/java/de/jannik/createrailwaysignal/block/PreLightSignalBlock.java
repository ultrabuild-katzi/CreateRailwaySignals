package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;

public class PreLightSignalBlock extends Block implements IBE<PreLightSignalBlockEntity>, IWrenchable {
    public PreLightSignalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add();
    }

    @Override
    public Class<PreLightSignalBlockEntity> getBlockEntityClass() {
        return PreLightSignalBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PreLightSignalBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.PRE_LIGHT_SIGNAL.get();
    }
}
