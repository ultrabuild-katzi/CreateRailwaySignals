package de.jannik.createrailwaysignal;


import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.block.BrSignBlockEntity;
import de.jannik.createrailwaysignal.block.BrSignBlock;
import de.jannik.createrailwaysignal.block.kilometer.ModKilometerContent;
import de.jannik.createrailwaysignal.commands.CreaterailwayCommands;
import de.jannik.createrailwaysignal.item.ModItemGroup;
import de.jannik.createrailwaysignal.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.math.Direction;

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

    // Network identifiers for brsign
    public static final Identifier BR_SIGN_OPEN = new Identifier(MOD_ID, "brsign_open");
    public static final Identifier BR_SIGN_UPDATE = new Identifier(MOD_ID, "brsign_update");
    public static final Identifier BR_SIGN_ROTATE = new Identifier(MOD_ID, "brsign_rotate");

    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        ModItemGroup.registerItemGroups();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntityTypes.initialize();
        CreaterailwayCommands.register();
        ModItems.registerModItems();
        ModKilometerContent.register();
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

        // Register server-side receiver to accept sign text updates from client
        ServerPlayNetworking.registerGlobalReceiver(BR_SIGN_UPDATE, (server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            var text = buf.readString(32767);
            int width = buf.readInt();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BrSignBlockEntity be) {
                    be.setText(text);
                    be.setWidth(width);
                    be.sync();
                }
            });
        });

        // Register server-side receiver to accept rotation requests from client GUI
        ServerPlayNetworking.registerGlobalReceiver(BR_SIGN_ROTATE, (server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            int horiz = buf.readInt();
            server.execute(() -> {
                var world = player.getWorld();
                var state = world.getBlockState(pos);
                if (state.getBlock() instanceof BrSignBlock) {
                    Direction dir = Direction.fromHorizontal(horiz);
                    var newState = state.with(BrSignBlock.FACING, dir);
                    // use flag 3 to notify clients and update comparators
                    world.setBlockState(pos, newState, 3);
                    // if there's a block entity, ask it to sync as well
                    var be = world.getBlockEntity(pos);
                    if (be instanceof BrSignBlockEntity brBe) brBe.sync();
                }
            });
        });
    }
}