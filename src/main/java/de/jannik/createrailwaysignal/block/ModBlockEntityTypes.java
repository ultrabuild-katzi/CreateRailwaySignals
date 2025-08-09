package de.jannik.createrailwaysignal.block;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.entity.FakeEngineEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

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

    public static final BlockEntityType<FakeEngineEntity> FAKE_ENGINE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(Createrailwaysignal.MOD_ID, "fake_engine"),
            FabricBlockEntityTypeBuilder.create(FakeEngineEntity::new, ModBlocks.FAKE_ENGINE).build()
    );


    public static void initialize() {
        FluidStorage.SIDED.registerForBlockEntity((machine, direction) ->
                        machine.fluidStorage
                , FAKE_ENGINE_ENTITY);
        Createrailwaysignal.LOGGER.info("Registered Block Entity Types");
    }
}