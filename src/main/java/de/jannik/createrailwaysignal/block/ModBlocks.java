package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
//import de.jannik.createrailwaysignal.item.CopycatBogeyItem;
import de.jannik.createrailwaysignal.item.TrackLimitItem;
import de.jannik.createrailwaysignal.item.WhistleBlockItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;


import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static de.jannik.createrailwaysignal.Createrailwaysignal.MOD_ID;

public class ModBlocks {

    public static final Block FAKE_ENGINE = registerBlock("fake_engine",
            new FakeEngineBlock(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.ANVIL).nonOpaque()));



    public static final BlockEntry<WhistleBlock> WHISTLE_BLOCK = Createrailwaysignal.REGISTRATE.block("whistle_block", WhistleBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.SPRUCE_BROWN)
                    .nonOpaque()
                    .sounds(BlockSoundGroup.NETHERITE))
            .item(WhistleBlockItem::new)
            .transform(customItemModel())
            .register();
    public static final BlockEntry<TrackLimitBlock> TRACK_LIMIT = Createrailwaysignal.REGISTRATE.block("track_limit", TrackLimitBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.SPRUCE_BROWN)
                    .nonOpaque()
                    .sounds(BlockSoundGroup.NETHERITE))
            .item(TrackLimitItem::new)
            .transform(customItemModel())
            .register();
    public static BlockEntry<LightSignalSpeedBlock> LIGHT_SIGNAL_SPEED;
    public static BlockEntry<TrainLightBlock> TRAIN_LIGHT;
    public static BlockEntry<BrSignBlock> BR_SIGN;
    public static BlockEntry<BueSchrankBlock> BUE_SCHRANK_2M;
    public static BlockEntry<BueSchrankBlock> BUE_SCHRANK_3M;
    public static BlockEntry<BueSchrankBlock> BUE_SCHRANK_4M;

    public static void registerModBlocks() {
        LIGHT_SIGNAL_SPEED = Createrailwaysignal.REGISTRATE.block("light_signal_speed", LightSignalSpeedBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .item(LightSignalSpeedItem::new)
                .transform(customItemModel())
                .register();

        TRAIN_LIGHT = Createrailwaysignal.REGISTRATE.block("train_light", TrainLightBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p.mapColor(MapColor.IRON_GRAY)
                        .nonOpaque()
                        .luminance(state -> 15)
                        .sounds(BlockSoundGroup.LANTERN))
                .item()
                .transform(customItemModel())
                .register();

        BR_SIGN = Createrailwaysignal.REGISTRATE.block("brsign", BrSignBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .defaultLoot()
                .properties(p -> p.nonOpaque())
                .item()
                .transform(customItemModel())
                .register();

        BUE_SCHRANK_2M = Createrailwaysignal.REGISTRATE.block("bue_schrank_2m", BueSchrankBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p.nonOpaque())
                .item()
                .transform(customItemModel())
                .register();

        BUE_SCHRANK_3M = Createrailwaysignal.REGISTRATE.block("bue_schrank_3m", BueSchrankBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p.nonOpaque())
                .item()
                .transform(customItemModel())
                .register();

        BUE_SCHRANK_4M = Createrailwaysignal.REGISTRATE.block("bue_schrank_4m", BueSchrankBlock::new)
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p.nonOpaque())
                .item()
                .transform(customItemModel())
                .register();
    }

    public static void register() {
        Createrailwaysignal.LOGGER.info("Registering Mod Blocks for " + MOD_ID);
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    private static Block overrideBlock(Block toOverride, Block newBlock) {
        BlockItem newBlockItem = new BlockItem(newBlock, new FabricItemSettings());
        overrideBlockItem((BlockItem) toOverride.asItem(), newBlockItem);
        return Registry.register(Registries.BLOCK, Registries.BLOCK.getRawId(toOverride), Registries.BLOCK.getId(toOverride).getPath(), newBlock);
    }

    private static Item overrideBlockItem(BlockItem toOverride, BlockItem newItem) {
        return Registry.register(Registries.ITEM, Registries.ITEM.getRawId(toOverride), Registries.ITEM.getId(toOverride).getPath(), newItem);
    }
}