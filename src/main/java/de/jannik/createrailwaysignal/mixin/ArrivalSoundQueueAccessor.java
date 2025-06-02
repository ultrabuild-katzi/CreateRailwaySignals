package de.jannik.createrailwaysignal.mixin;

import com.google.common.collect.Multimap;
import com.simibubi.create.content.trains.entity.ArrivalSoundQueue;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArrivalSoundQueue.class)
public interface ArrivalSoundQueueAccessor {

    @Accessor
    Multimap<Integer, BlockPos> getSources();

}
