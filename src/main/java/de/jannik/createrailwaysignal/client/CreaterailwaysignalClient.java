package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.logistics.depot.EjectorTargetHandler;
import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.block.BrSignBlockRenderer;
//import de.jannik.createrailwaysignal.block.CopycatBogeyBlockRenderer;
import de.jannik.createrailwaysignal.block.LightSignalSpeedBlockRenderer;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.block.ModBlockEntityTypes;
import de.jannik.createrailwaysignal.block.TrainLightBlockRenderer;
import de.jannik.createrailwaysignal.block.kilometer.ModKilometerContent;
import de.jannik.createrailwaysignal.commands.CameraCommand;
import de.jannik.createrailwaysignal.config.CameraConfig;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;


public class CreaterailwaysignalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModKilometerContent.registerClient();
        CameraConfig.load();
        CameraCommand.register();
        registerClientEvents();

        // Register block entity renderers
        BlockEntityRendererFactories.register(ModBlockEntityTypes.LIGHT_SIGNAL_SPEED.get(), LightSignalSpeedBlockRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntityTypes.TRAIN_LIGHT.get(), TrainLightBlockRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntityTypes.BR_SIGN.get(), BrSignBlockRenderer::new);

        // Register client receiver to open sign editor
        ClientPlayNetworking.registerGlobalReceiver(Createrailwaysignal.BR_SIGN_OPEN, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String text = buf.readString(32767);
            int width = buf.readInt();
            client.execute(() -> {
                client.setScreen(new BrSignEditScreen(pos, text, width));
            });
        });
    }

    public static void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null)
                return;

            ItemStack heldItemMainhand = player.getMainHandStack();
            if (!ModBlocks.LIGHT_SIGNAL_SPEED.isIn(heldItemMainhand)) {
                LightSignalSpeedItem.currentItem = null;
            } else {
                if (heldItemMainhand != LightSignalSpeedItem.currentItem) {
                    LightSignalSpeedItem.currentSelection = null;
                    LightSignalSpeedItem.currentItem = heldItemMainhand;
                }
                EjectorTargetHandler.drawOutline(LightSignalSpeedItem.currentSelection);
            }
        });
    }
}
