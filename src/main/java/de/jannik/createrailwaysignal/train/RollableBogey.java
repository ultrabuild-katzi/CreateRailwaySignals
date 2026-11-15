package de.jannik.createrailwaysignal.train;

import net.createmod.catnip.animation.LerpedFloat;

/** Exposes the smoothed bank/roll angle {@code CarriageBogeyMixin} adds to Create's CarriageBogey,
 * so the renderer mixin can read it without needing its own copy of the field. Lives outside the
 * mixin package since Mixin forbids directly referencing classes inside its own mixin package. */
public interface RollableBogey {

    LerpedFloat createRailwaySignal$$roll();

}
