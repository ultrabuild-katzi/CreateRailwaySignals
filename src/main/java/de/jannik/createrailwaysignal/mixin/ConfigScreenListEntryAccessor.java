package de.jannik.createrailwaysignal.mixin;

import net.createmod.catnip.config.ui.ConfigScreenList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the protected {@code path} field of a config screen entry (the dotted NightConfig
 * path, e.g. "speedometer.verticalOffset") so {@link SubMenuConfigScreenOrderMixin} can match
 * entries against our desired display order.
 */
@Mixin(value = ConfigScreenList.Entry.class, remap = false)
public interface ConfigScreenListEntryAccessor {

    @Accessor("path")
    String createrailwaysignal$getPath();
}
