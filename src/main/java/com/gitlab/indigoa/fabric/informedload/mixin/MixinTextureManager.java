package com.gitlab.indigoa.fabric.informedload.mixin;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;

/**
 * @author Indigo Amann
 */
@Mixin(TextureManager.class)
public class MixinTextureManager {
    @Inject(method = "reload", at = @At("INVOKE"))
    public void startReloadCheck(ResourceReloadListener.Synchronizer resourceReloadListener$Synchronizer_1, ResourceManager resourceManager_1, Profiler profiler_1, Profiler profiler_2, Executor executor_1, Executor executor_2, CallbackInfoReturnable ci) {
        System.out.println("started reloading textures");
    }
    @Inject(method = "method_18167", at = @At("INVOKE"))
    public void actuallyLoadTextures(ResourceManager resourceManager, Executor executor, Void idkWhatThisIsFor, CallbackInfo ci) {
        System.out.println("actually reloading textures");
    }
}
