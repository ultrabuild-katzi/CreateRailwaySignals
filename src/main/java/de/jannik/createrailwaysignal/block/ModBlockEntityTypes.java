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

public class ModBlockEntityTypes {

    public static final BlockEntityEntry<TrackLimitBlockEntity> TRACK_LIMIT = Createrailwaysignal.REGISTRATE
            .blockEntity("track_limit", TrackLimitBlockEntity::new)
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

    public static final BlockEntityEntry<TrainLightBlockEntity> TRAIN_LIGHT = Createrailwaysignal.REGISTRATE
            .blockEntity("train_light", TrainLightBlockEntity::new)
            .validBlocks(ModBlocks.TRAIN_LIGHT)
            .register();

    public static final BlockEntityEntry<BrSignBlockEntity> BR_SIGN = Createrailwaysignal.REGISTRATE
            .blockEntity("brsign", BrSignBlockEntity::new)
            .renderer(() -> BrSignBlockRenderer::new)
            .validBlocks(ModBlocks.BR_SIGN)
            .register();

    public static final BlockEntityEntry<BueSchrankBlockEntity> BUE_SCHRANK = Createrailwaysignal.REGISTRATE
            .blockEntity("bue_schrank", BueSchrankBlockEntity::new)
            .renderer(() -> BueSchrankBlockRenderer::new)
            .validBlocks(ModBlocks.BUE_SCHRANK_2M, ModBlocks.BUE_SCHRANK_3M, ModBlocks.BUE_SCHRANK_4M)
            .register();

    public static final BlockEntityType<FakeEngineEntity> FAKE_ENGINE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(Createrailwaysignal.MOD_ID, "fake_engine"),
            FabricBlockEntityTypeBuilder.create(FakeEngineEntity::new, ModBlocks.FAKE_ENGINE).build()
    );

    public static void initialize() {
        Createrailwaysignal.LOGGER.info("Registered Block Entity Types");
        FluidStorage.SIDED.registerForBlockEntity(

                (machine, direction) -> machine.fluidStorage,
                FAKE_ENGINE_ENTITY
        );
        Createrailwaysignal.LOGGER.info("Registered Block Entity Types");
    }
}
