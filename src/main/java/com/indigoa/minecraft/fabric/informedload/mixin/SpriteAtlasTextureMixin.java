package com.indigoa.minecraft.fabric.informedload.mixin;

import com.indigoa.minecraft.fabric.informedload.InformedLoad;
import com.indigoa.minecraft.fabric.informedload.TaskList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Indigo Amann
 */
@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
    TaskList.Task.TaskStitchTextures taskStitchTextures;

    @Inject(method = "stitch", at = @At("HEAD"))
    public void showStitch(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        //TaskList.removeTask("addmodels");
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || TaskList.hasTask("texstitch")) return;
        taskStitchTextures = new TaskList.Task.TaskStitchTextures();
        TaskList.addTask(taskStitchTextures);
        TaskList.Task.TaskLoadModels taskLoadModels = TaskList.Task.TaskLoadModels.INSTANCE;
        taskLoadModels.setStage(1);
    }
    @Inject(method = "stitch", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=extracting_frames" }))
    public void showStitch1(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        taskStitchTextures.stage(1);
    }
    @Inject(method = "stitch", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=mipmapping" }))
    public void showStitch2(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        taskStitchTextures.stage(2);
    }
    @Inject(method = "stitch", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=stitching" }))
    public void showStitch3(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        taskStitchTextures.stage(3);
    }
    @Inject(method = "stitch", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=loading" }))
    public void showStitch4(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        taskStitchTextures.stage(4);
    }
    @Inject(method = "stitch", at = @At("RETURN"))
    public void showStitchEnd(ResourceManager manager, Iterable iterable, Profiler profiler, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        TaskList.removeTask("texstitch");
    }
    @Inject(method = "loadSprites", at = @At("HEAD"))
    public void cacheSpritesToLoad(ResourceManager resourceManager_1, Set<Identifier> set_1, CallbackInfoReturnable ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        InformedLoad.spritesToLoad = set_1.size();
    }
    @Inject(method = "method_18160", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ConcurrentLinkedQueue;add(Ljava/lang/Object;)Z"))
    public void showSpriteLoadStatus(Identifier identifier, ResourceManager resourceManager_1, ConcurrentLinkedQueue concurrentLinkedQueue, CallbackInfo ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        int size = concurrentLinkedQueue.size();
        taskStitchTextures.setExtra(size + "/" + InformedLoad.spritesToLoad);
        taskStitchTextures.subPercentage((float) size / InformedLoad.spritesToLoad);
    }
    //@Inject(method = "method_18161", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"))
    //public void showSpriteLoadStatusB(ResourceManager resourceManager_1, TextureStitcher textureStitcher_1, CallbackInfoReturnable ci) {
    //    if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
    //    taskStitchTextures.setExtra(InformedLoad.spritesLoaded++ + "/" + InformedLoad.spritesToLoad);
    //}
    @Inject(method = "method_18162", at = @At("RETURN"))
    public void countLoadedSprites(ResourceManager resourceManager, Sprite sprite, ConcurrentLinkedQueue concurrentLinkedQueue, CallbackInfo ci) {
        if (TaskList.hasTask("addmodels") || !TaskList.hasTask("loadmodels") || !TaskList.hasTask("texstitch") || taskStitchTextures == null) return;
        int size = concurrentLinkedQueue.size();
        taskStitchTextures.setExtra(size + "/" + InformedLoad.spritesToLoad);
        taskStitchTextures.subPercentage((float) size / InformedLoad.spritesToLoad);
    }
}