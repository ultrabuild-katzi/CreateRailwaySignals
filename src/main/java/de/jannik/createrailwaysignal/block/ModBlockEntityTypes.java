package de.jannik.createrailwaysignal.block;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.jannik.createrailwaysignal.Createrailwaysignal;

import static de.jannik.createrailwaysignal.Createrailwaysignal.REGISTRATE;

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
    public static final BlockEntityEntry<WhistleBlockEntity> WHISTLE_BLOCK = Createrailwaysignal.REGISTRATE
            .blockEntity("whistle_block", WhistleBlockEntity::new)
            .validBlocks(ModBlocks.WHISTLE_BLOCK)
            .register();


    public static void initialize() {
        Createrailwaysignal.LOGGER.info("Registered Block Entity Types");
    }
}