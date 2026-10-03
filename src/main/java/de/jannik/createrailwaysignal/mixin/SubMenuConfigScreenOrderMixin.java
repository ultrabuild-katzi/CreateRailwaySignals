package de.jannik.createrailwaysignal.mixin;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Comparator;
import java.util.List;

/**
 * The Forge Config API Port's config screen ({@code SubMenuConfigScreen}) always lists its
 * entries alphabetically by display label (every value entry extends {@code LabeledEntry}, which
 * the screen's sort comparator compares by label text). This wraps that sort so that, only while
 * viewing our own mod's config screen, entries are instead shown in our preferred order.
 * Every other mod's config screen (including Create's own) keeps its normal alphabetical sort.
 */
@Mixin(value = SubMenuConfigScreen.class, remap = false)
public abstract class SubMenuConfigScreenOrderMixin {

    private static final List<String> CREATERAILWAYSIGNAL_ORDER = List.of(
            "speedometer.showTrainSpeedHud",
            "speedometer.speedUnit",
            "speedometer.position",
            "speedometer.verticalOffset",
            "speedometer.horizontalOffset"
    );

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"
            ),
            require = 0,
            expect = 0
    )
    private Comparator<ConfigScreenList.Entry> createrailwaysignal$useCustomOrder(Comparator<ConfigScreenList.Entry> original) {
        if (!Createrailwaysignal.MOD_ID.equals(ConfigScreen.modID))
            return original;

        return (e1, e2) -> {
            int i1 = CREATERAILWAYSIGNAL_ORDER.indexOf(createrailwaysignal$path(e1));
            int i2 = CREATERAILWAYSIGNAL_ORDER.indexOf(createrailwaysignal$path(e2));
            if (i1 == -1 || i2 == -1)
                return original.compare(e1, e2);
            return Integer.compare(i1, i2);
        };
    }

    private static String createrailwaysignal$path(ConfigScreenList.Entry entry) {
        return ((ConfigScreenListEntryAccessor) entry).createrailwaysignal$getPath();
    }
}
