package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

import javax.swing.text.html.parser.Entity;
import java.util.Set;


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

