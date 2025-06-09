package de.jannik.createrailwaysignal.graph;


import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class WhistleBlockBoundary extends SingleBlockEntityEdgePoint {

    public static void playSound(World world, @NotNull Train train) {
        String icon = train.icon.getId().getPath();
        SoundEvent soundEvent = switch(icon) {
            case "traditional" ->  Createrailwaysignal.TRADITIONAL_SOUND_EVENT;
            case "electric" ->  Createrailwaysignal.ELECTRIC_SOUND_EVENT;
            case "modern" ->  Createrailwaysignal.MODERN_SOUND_EVENT;
            default -> AllSoundEvents.HAUNTED_BELL_USE.getMainEvent();
        };

        for (Carriage carriage : train.carriages) {
            CarriageContraptionEntity entity = carriage.getDimensional(world).entity.get();
            if (entity == null || !(entity.getContraption() instanceof CarriageContraption))
                continue;

            entity.getWorld().playSoundFromEntity(null, entity, soundEvent, SoundCategory.NEUTRAL, 5, 1);
        }
    }
}
