package de.jannik.createrailwaysignal.graph;


import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.mixin.ArrivalSoundQueueAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WhistleBlockBoundary extends SingleBlockEntityEdgePoint {
    private World world;

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        this.world = blockEntity.getWorld();

        System.out.println("Hello from WhistleBlockBoundary");
    }

    public void playSound(Train train) {
        if(this.world == null) {
            return;
        }

        String icon = train.icon.getId().getPath();
        SoundEvent soundEvent = switch(icon) {
            case "traditional" ->  Createrailwaysignal.TRADITIONAL_SOUND_EVENT;
            case "electric" ->  Createrailwaysignal.ELECTRIC_SOUND_EVENT;
            case "modern" ->  Createrailwaysignal.MODERN_SOUND_EVENT;
            default -> AllSoundEvents.HAUNTED_BELL_USE.getMainEvent();
        };

        for (Carriage carriage : train.carriages) {
            CarriageContraptionEntity entity = carriage.getDimensional(this.world).entity.get();
            if (entity == null || !(entity.getContraption() instanceof CarriageContraption otherCC))
                continue;

            entity.getWorld().playSoundFromEntity(null, entity, soundEvent, SoundCategory.NEUTRAL, 5, 1);
        }
    }
}
