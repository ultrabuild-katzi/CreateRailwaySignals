package de.jannik.createrailwaysignal;


import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import de.jannik.createrailwaysignal.item.ModItemGroup;
import de.jannik.createrailwaysignal.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Createrailwaysignal implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("createrailwaysignal");
    public static final String MOD_ID = "createrailwaysignal";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab(ModItemGroup.CREATE_RAILWAY_SIGNAL_GROUP_KEY)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public static final Identifier TRADITIONAL_SOUND_ID = Identifier.of(MOD_ID, "traditional");
    public static SoundEvent TRADITIONAL_SOUND_EVENT = SoundEvent.of(TRADITIONAL_SOUND_ID);

    public static final Identifier ELECTRIC_SOUND_ID = Identifier.of(MOD_ID, "electric");
    public static SoundEvent ELECTRIC_SOUND_EVENT = SoundEvent.of(ELECTRIC_SOUND_ID);

    public static final Identifier MODERN_SOUND_ID = Identifier.of(MOD_ID, "modern");
    public static SoundEvent MODERN_SOUND_EVENT = SoundEvent.of(MODERN_SOUND_ID);

    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        ModItemGroup.registerItemGroups();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntityTypes.initialize();
        CreaterailwayCommands.register();
        LOGGER.info("Initialized mod");
        REGISTRATE.register();


        Registry.register(Registries.SOUND_EVENT, Createrailwaysignal.TRADITIONAL_SOUND_ID, TRADITIONAL_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, Createrailwaysignal.ELECTRIC_SOUND_ID, ELECTRIC_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, Createrailwaysignal.MODERN_SOUND_ID, MODERN_SOUND_EVENT);


        ServerWorldEvents.LOAD.register((server, world) -> {
            Createrailwaysignal.server = server;
            LOGGER.info("Server registered");
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            de.jannik.createrailwaysignal.commands.BoatFlyCommand.register(dispatcher);
        });
    }
}