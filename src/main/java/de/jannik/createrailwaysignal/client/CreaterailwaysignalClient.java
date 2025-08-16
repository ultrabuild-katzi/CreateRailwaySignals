package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.logistics.depot.EjectorTargetHandler;
import de.jannik.createrailwaysignal.block.ModBlocks;
import de.jannik.createrailwaysignal.block.kilometer.ModKilometerContent;
import de.jannik.createrailwaysignal.item.LightSignalSpeedItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;


public class CreaterailwaysignalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModKilometerContent.registerClient();
        registerClientEvents();
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

