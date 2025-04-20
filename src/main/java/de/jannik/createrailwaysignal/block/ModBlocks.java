package de.jannik.createrailwaysignal.block;

import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.graph.CustomEdgePointType;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
import de.jannik.createrailwaysignal.item.TrackLimitItem;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class ModBlocks {

    public static final BlockEntry<TrackLimitBlock> TRACK_LIMIT = Createrailwaysignal.REGISTRATE.block("track_limit", TrackLimitBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.SPRUCE_BROWN)
                    .nonOpaque()
                    .sounds(BlockSoundGroup.NETHERITE))
            .item(TrackLimitItem::new)
            .transform(customItemModel())
//           .blockstate((c, p) -> p.getVariantBuilder(c.get())
//                    .forAllStates(state -> ConfiguredModel.builder()
//                            .modelFile(AssetLookup.partialBaseModel(c, p, state.getValue(SignalBlock.TYPE)
//                                    .getSerializedName()))
//                            .build()))
            .register();

    public static BlockEntry<LightSignalSpeedBlock> LIGHT_SIGNAL_SPEED;

    public static void registerModBlocks() {
        LIGHT_SIGNAL_SPEED = Createrailwaysignal.REGISTRATE.block("light_signal_speed", LightSignalSpeedBlock::new)
                .initialProperties(SharedProperties::softMetal)
//            .properties(p -> p.mapColor(MapColor.SPRUCE_BROWN)
//                    .nonOpaque()
//                    .sounds(BlockSoundGroup.NETHERITE))
                .item(LightSignalSpeedItem::new)
                .transform(customItemModel())
//           .blockstate((c, p) -> p.getVariantBuilder(c.get())
//                    .forAllStates(state -> ConfiguredModel.builder()
//                            .modelFile(AssetLookup.partialBaseModel(c, p, state.getValue(SignalBlock.TYPE)
//                                    .getSerializedName()))
//                            .build()))
                .register();

        Createrailwaysignal.LOGGER.info("Registering ModBlocks for " + Createrailwaysignal.MOD_ID);
    }
}
