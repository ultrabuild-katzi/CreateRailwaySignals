package de.jannik.createrailwaysignal.track;

import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public interface EmbankmentTrack {
    @Nullable BlockState crs$getEmbankment();
    void crs$setEmbankment(@Nullable BlockState state);
}
