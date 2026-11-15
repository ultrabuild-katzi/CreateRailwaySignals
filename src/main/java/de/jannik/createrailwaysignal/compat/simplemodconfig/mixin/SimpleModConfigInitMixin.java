package de.jannik.createrailwaysignal.compat.simplemodconfig.mixin;

import chase.minecraft.architectury.simplemodconfig.SimpleModConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Mod Config's {@code init()} builds a dev-environment-only "test config" entry whose
 * construction forces loading of client-only classes (e.g. {@code Screen}), crashing a dedicated
 * server started from a development workspace. Real players never hit this (it's only reachable
 * via {@code Platform.isDevelopmentEnvironment()}), but it otherwise blocks {@code runServer} for
 * anyone developing this modpack, so we skip the whole (already server-irrelevant) method there.
 */
@Mixin(value = SimpleModConfig.class, remap = false)
public class SimpleModConfigInitMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private static void crs$skipDevTestConfigOnDedicatedServer(CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            ci.cancel();
    }
}
