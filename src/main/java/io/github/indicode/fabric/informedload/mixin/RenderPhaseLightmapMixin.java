package io.github.indicode.fabric.informedload.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPhase.Lightmap.class)
public class RenderPhaseLightmapMixin {
    @Inject(method={"method_23551","method_23552"}, at = @At("HEAD"), cancellable = true)
    private static void cancelIfNull(CallbackInfo ci) {
        if (MinecraftClient.getInstance().gameRenderer == null || MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager() == null) ci.cancel();
    }
}
