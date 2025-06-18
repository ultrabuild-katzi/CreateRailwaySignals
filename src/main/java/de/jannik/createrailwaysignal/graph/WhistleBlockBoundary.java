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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;


public class WhistleBlockBoundary extends SingleBlockEntityEdgePoint {

    public static void playSound(World world, @NotNull Train train) {
        String icon = train.icon.getId().getPath();
        SoundEvent soundEvent = switch (icon) {
            case "traditional" -> Createrailwaysignal.TRADITIONAL_SOUND_EVENT;
            case "electric" -> Createrailwaysignal.ELECTRIC_SOUND_EVENT;
            case "modern" -> Createrailwaysignal.MODERN_SOUND_EVENT;
            default -> AllSoundEvents.HAUNTED_BELL_USE.getMainEvent();
        };

        if (train.carriages.isEmpty()) return;

        // Logical front (first in list)
        CarriageContraptionEntity frontEntity = train.carriages.get(0).getDimensional(world).entity.get();
        // Logical rear (last in list)
        CarriageContraptionEntity rearEntity = train.carriages.get(train.carriages.size() - 1).getDimensional(world).entity.get();


        if (frontEntity != null && train.speed > 0) {
            System.out.println("[Whistle] Playing sound at FRONT carriage: " + frontEntity.getUuid());
            frontEntity.getWorld().playSoundFromEntity(null, frontEntity, soundEvent, SoundCategory.NEUTRAL, 5, 1);
        }

        // Don't double-play if it's a 1-carriage train
        if (rearEntity != null && rearEntity != frontEntity && train.speed < 0) {
            System.out.println("[Whistle] Playing sound at REAR carriage: " + rearEntity.getUuid());
            rearEntity.getWorld().playSoundFromEntity(null, rearEntity, soundEvent, SoundCategory.NEUTRAL, 5, 1);
        }
    }
}