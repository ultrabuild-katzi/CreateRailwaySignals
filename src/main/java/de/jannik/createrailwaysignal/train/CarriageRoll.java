package de.jannik.createrailwaysignal.train;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;

/** Shared helper for reading the bank/roll angle a whole carriage (body, rider, camera, ...)
 * should lean by, averaged across its bogeys for carriages riding two independently-leaning
 * trucks. Keeps every renderer that cares about the carriage's overall lean in agreement. */
public final class CarriageRoll {

    private CarriageRoll() {}

    public static float average(Carriage carriage, float partialTicks) {
        CarriageBogey first = carriage.bogeys.getFirst();
        CarriageBogey second = carriage.bogeys.getSecond();

        float firstRoll = first instanceof RollableBogey rollable
                ? rollable.createRailwaySignal$$roll().getValue(partialTicks) : 0;

        if (second == null)
            return firstRoll;

        float secondRoll = second instanceof RollableBogey rollable
                ? rollable.createRailwaySignal$$roll().getValue(partialTicks) : 0;

        return (firstRoll + secondRoll) / 2f;
    }

}
