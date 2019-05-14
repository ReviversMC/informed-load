package com.indigoa.minecraft.fabric.informedload.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Indigo Amann
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    /*@Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModelManager;<init>(Lnet/minecraft/client/texture/SpriteAtlasTexture;)V"))
    private void onModelLoadStart(CallbackInfo ci) {
        TaskList.addTask(new TaskList.Task("modelload", "Loading Models"));
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManager;registerListener(Lnet/minecraft/resource/ResourceReloadListener;)V", ordinal = 6, shift = At.Shift.AFTER))
    private void onModelLoadEnd(CallbackInfo ci) {
        TaskList.removeTask("modelload");
        System.out.println("remove");
    }*/
}
