package de.jannik.createrailwaysignal.client;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import de.jannik.createrailwaysignal.config.RailwaySignalConfigs;
import de.jannik.createrailwaysignal.config.SpeedUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Renders a small "(train name): (speed)" text in the bottom right of the screen
 * while the local player is riding inside a train.
 */
public final class TrainSpeedHud {

    private TrainSpeedHud() {}

    public static void render(DrawContext context, float tickDelta) {
        var config = RailwaySignalConfigs.client();
        if (config == null || !config.showTrainSpeedHud.get())
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.options.hudHidden)
            return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof CarriageContraptionEntity cce))
            return;

        Carriage carriage = cce.getCarriage();
        if (carriage == null)
            return;

        Train train = carriage.train;
        if (train == null)
            return;

        // train.speed is only kept in sync with the server for the player currently manning the
        // controls (Create only broadcasts TrainHUDUpdatePacket to the driver). For any other
        // passenger it would otherwise stay stuck at 0, so derive the actual speed from how far
        // the carriage entity itself moved this tick instead - that position is synced to everyone.
        double blocksPerTick = computeSpeedBlocksPerTick(vehicle);

        SpeedUnit unit = config.speedUnit.get();
        double speed = unit.convert(blocksPerTick);
        String trainName = train.name != null ? train.name.getString() : "Train";

        String text = String.format("%s: %.1f %s", trainName, speed, unit.suffix);

        TextRenderer textRenderer = mc.textRenderer;
        int textWidth = textRenderer.getWidth(text);
        int x = context.getScaledWindowWidth() - textWidth - 4;
        int y = context.getScaledWindowHeight() - 14;

        context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
    }

    private static double computeSpeedBlocksPerTick(Entity vehicle) {
        double dx = vehicle.getX() - vehicle.prevX;
        double dy = vehicle.getY() - vehicle.prevY;
        double dz = vehicle.getZ() - vehicle.prevZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
