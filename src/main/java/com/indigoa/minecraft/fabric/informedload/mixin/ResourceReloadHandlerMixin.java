package com.indigoa.minecraft.fabric.informedload.mixin;

import com.indigoa.minecraft.fabric.informedload.InformedLoad;
import net.minecraft.resource.ResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Indigo Amann
 */
@Mixin(net.minecraft.resource.ResourceReloadHandler.class)
public class ResourceReloadHandlerMixin {
    @Shadow
    private Set<ResourceReloadListener> loadStageLoaders;
    @Shadow
    private int listenerCount;
    @Shadow
    private int field_18046;
    @Shadow
    private int field_18047;
    @Shadow
    private AtomicInteger field_18048;
    @Shadow
    private AtomicInteger field_18049;
    @Inject(at = @At("RETURN"), method = "<init>")
    public void getLoadStageLoaders(CallbackInfo ci) {
        //loaders = loadStageLoaders;
        InformedLoad.loadListeners = loadStageLoaders;
        System.out.println("INIT!");
    }
    @Inject(at = @At("RETURN"), method = "getProgress()F")
    public void getProgressExtraInfo(CallbackInfoReturnable ci) {
        int int_1 = this.listenerCount - this.loadStageLoaders.size();
        float float_1 = (float)(this.field_18049.get() * 2 + this.field_18047 * 2 + int_1 * 1);
        float float_2 = (float)(this.field_18048.get() * 2 + this.field_18046 * 2 + this.listenerCount * 1);
        InformedLoad.current = float_1;
        InformedLoad.max = float_2;
        //System.out.println(listenerCount + " " + field_18046 + " " + field_18047 + " " + field_18048.get() + " " + field_18049.get() + " : " + float_1 + " " + float_2);
    }
    //public static Set<ResourceReloadListener> loaders;
}
