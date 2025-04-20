package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntityTypes {

    public static final BlockEntityEntry<TrackLimitBlockEntity> TRACK_LIMIT = Createrailwaysignal.REGISTRATE
            .blockEntity("track_limit", TrackLimitBlockEntity::new)
//            .renderer(() -> SignalRenderer::new)
            .validBlocks(ModBlocks.TRACK_LIMIT)
            .register();
    public static final BlockEntityEntry<LightSignalSpeedBlockEntity> LIGHT_SIGNAL_SPEED = Createrailwaysignal.REGISTRATE
            .blockEntity("light_signal_speed", LightSignalSpeedBlockEntity::new)
            .renderer(() -> LightSignalSpeedBlockRenderer::new)
            .validBlocks(ModBlocks.LIGHT_SIGNAL_SPEED)
            .register();

    public static void initialize() {
        Createrailwaysignal.LOGGER.info("Registered Block Entity Types");
    }
}

