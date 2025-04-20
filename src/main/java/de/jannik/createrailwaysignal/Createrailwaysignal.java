package de.jannik.createrailwaysignal;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.item.ModItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Createrailwaysignal implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("createrailwaysignal");
    public static final String MOD_ID = "createrailwaysignal";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Createrailwaysignal.MOD_ID)
            .defaultCreativeTab((RegistryKey<ItemGroup>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        ModItemGroup.registerItemGroups();
        ModBlocks.registerModBlocks();
        ModBlockEntityTypes.initialize();
        REGISTRATE.register();
        LOGGER.info("Initialized mod");

        ServerWorldEvents.LOAD.register((server, world) -> {
            Createrailwaysignal.server = server;
            LOGGER.info("Server registered");
        });
    }
}
