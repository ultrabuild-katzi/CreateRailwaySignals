package de.jannik.createrailwaysignal.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

/**
 * Client-only settings for CreateRailwaySignal, registered through the Create-style
 * ConfigBase/ForgeConfigSpec system so they show up in the mod's own config screen
 * (provided by the Forge Config API Port), the same way Create's own config does.
 */
public class CRailwaySignalClientConfig extends ConfigBase {

    public final ConfigGroup speedometer = group(0, "speedometer", Comments.speedometer);

    public final ConfigBool showTrainSpeedHud = b(false, "showTrainSpeedHud", Comments.showTrainSpeedHud);

    public final ConfigEnum<SpeedUnit> speedUnit = e(SpeedUnit.KMH, "speedUnit", Comments.speedUnit);

    public final ConfigEnum<HudPosition> hudPosition = e(HudPosition.BOTTOM_RIGHT, "position", Comments.position);

    public final ConfigFloat verticalOffset = f(0f, -Float.MAX_VALUE, Float.MAX_VALUE, "verticalOffset", Comments.verticalOffset);

    public final ConfigFloat horizontalOffset = f(0f, -Float.MAX_VALUE, Float.MAX_VALUE, "horizontalOffset", Comments.horizontalOffset);

    @Override
    @NotNull
    public String getName() {
        return "client";
    }

    private static class Comments {
        static String speedometer = "Settings for the train speedometer HUD, shown while riding a train.";
        static String showTrainSpeedHud = "Show a text in the bottom right of the screen with the name and current speed of the train you are riding.";
        static String speedUnit = "The unit the train speedometer HUD displays speed in.";
        static String position = "The corner of the screen the train speedometer HUD is anchored to.";
        static String verticalOffset = "Moves the train speedometer HUD vertically, as a multiplier of its own text size. 0 = no offset.";
        static String horizontalOffset = "Moves the train speedometer HUD horizontally, as a multiplier of its own text size. 0 = no offset.";
    }
}
