package de.jannik.createrailwaysignal.block.kilometer;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static de.jannik.createrailwaysignal.Createrailwaysignal.MOD_ID;

public class ModKilometerContent {
    public static final Block KILOMETER_MARKER = new KilometerMarkerBlock(
            FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).strength(1.0f, 4.0f).nonOpaque());

    public static BlockEntityType<KilometerMarkerBlockEntity> KILOMETER_MARKER_BE;

    public static void registerAll() {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "kilometer_marker"), KILOMETER_MARKER);
        Registry.register(Registries.ITEM,  new Identifier(MOD_ID, "kilometer_marker"),
                new BlockItem(KILOMETER_MARKER, new Item.Settings()));

        KILOMETER_MARKER_BE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(MOD_ID, "kilometer_marker"),
                BlockEntityType.Builder.create(KilometerMarkerBlockEntity::new, KILOMETER_MARKER).build(null)
        );
    }

    public static void registerClient() {
        BlockEntityRendererRegistry.register(KILOMETER_MARKER_BE, KilometerMarkerBER::new);
    }
}
