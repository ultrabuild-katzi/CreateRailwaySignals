package de.jannik.createrailwaysignal.compat.realism;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates every mixin in {@code createrailwaysignal-realism-compat.mixins.json} behind
 * "is Create Realism actually installed". Without this, Mixin would try to transform
 * {@code net.Realism.trains.etcs.ETCS}, which simply doesn't exist when the (optional) mod is
 * absent.
 *
 * <p>Mixin loads this plugin extremely early, well before normal mod initialization - it must
 * therefore use its own lightweight logger here rather than {@code Createrailwaysignal.LOGGER}:
 * touching that class this early would force its (heavy, Create-dependent) static initializer to
 * run far too soon and crash the game/server before Create itself is ready.</p>
 */
public class RealismMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("createrailwaysignal/realism-compat");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Create Realism compat mixin config loaded, Realism detected: {}", RealismCompat.isLoaded());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean apply = RealismCompat.isLoaded();
        LOGGER.info("Create Realism compat: {} mixin {} -> {}",
                apply ? "applying" : "skipping", mixinClassName, targetClassName);
        return apply;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        LOGGER.info("Create Realism compat: mixin {} applied successfully to {}", mixinClassName, targetClassName);
    }
}
