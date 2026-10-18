package de.jannik.createrailwaysignal.item;

import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import net.minecraft.block.Block;

public class PreLightSignalItem extends TrackTargetingBlockItem {
    public PreLightSignalItem(Block block, Settings settings) {
        super(block, settings, CustomEdgePointType.PRE_LIGHT_SIGNAL);
    }
}
