package de.jannik.createrailwaysignal.item;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.block.kilometer.ModKilometerContent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {


    public static final RegistryKey<ItemGroup> CREATE_RAILWAY_SIGNAL_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Createrailwaysignal.MOD_ID, "create_railway_signal_group"));
    public static final ItemGroup CREATE_RAILWAY_SIGNAL_GROUP = Registry.register(Registries.ITEM_GROUP, CREATE_RAILWAY_SIGNAL_GROUP_KEY,
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.createrailwaysignal"))
                    .icon(() -> new ItemStack(ModBlocks.TRACK_LIMIT)).entries((displayContext, entries) -> {

                        // Sign blocks
                        entries.add(ModBlocks.TRACK_LIMIT.asItem());
                        entries.add(ModBlocks.LIGHT_SIGNAL_SPEED.asItem());
                        entries.add(ModItems.INCOMPLETE_LIGHT_SIGNAL_SPEED);
                        entries.add(ModBlocks.BR_SIGN.asItem());

                        // Blocky blocks
                        entries.add(ModBlocks.WHISTLE_BLOCK.asItem());
                        entries.add(ModBlocks.PRE_LIGHT_SIGNAL.asItem());
                        entries.add(ModKilometerContent.KILOMETER_MARKER.asItem());

                        // The rest
                        entries.add(ModBlocks.TRAIN_LIGHT.asItem());
                        entries.add(ModBlocks.BUE_SCHRANK_2M.asItem());
                        entries.add(ModBlocks.BUE_SCHRANK_3M.asItem());
                        entries.add(ModBlocks.BUE_SCHRANK_4M.asItem());
                        entries.add(ModItems.VISITOR_ID);
                        entries.add(ModItems.WORKER_ID);
                        entries.add(ModItems.SUPERVISOR_ID);

                    }).build());




    public static void registerItemGroups() {
        Createrailwaysignal.LOGGER.info("Registering Item Groups for " + Createrailwaysignal.MOD_ID);
    }
}