package de.jannik.createrailwaysignal.item;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    public static final ItemGroup CREATE_RAILWAY_SIGNAL_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(Createrailwaysignal.MOD_ID, "create_railway_signal_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.createrailwaysignal"))
                    .icon(() -> new ItemStack(ModBlocks.TRACK_LIMIT)).entries((displayContext, entries) -> {
                        entries.add(ModBlocks.TRACK_LIMIT); // cognitive loser
                        entries.add(ModBlocks.LIGHT_SIGNAL_SPEED); // cognitive loser

                    }).build());

    public static void registerItemGroups() {
        Createrailwaysignal.LOGGER.info("Registering Item Groups for " + Createrailwaysignal.MOD_ID);
    }
}