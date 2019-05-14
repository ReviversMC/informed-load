package com.indigoa.minecraft.fabric.informedload.mixin;

import com.indigoa.minecraft.fabric.informedload.TaskList;
import net.fabricmc.loader.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

/**
 * @author Indigo Amann
 */
//@Mixin(FabricLoader.class)
public class FabricLoaderMixin {
    public static final TaskList.Task taskLoadMods = new TaskList.Task("loadmods", "Loading Mods"),
                                taskInstantiateMods = new TaskList.Task("instmods", "Instantiating");
    //@Inject(method = "instantiateMods", at = @At("HEAD"))
    public void showInstantiateMods(File file, Object object, CallbackInfo ci) {
        TaskList.addTask(taskLoadMods);
        TaskList.addTask(taskInstantiateMods);
    }
    //@Inject(method = "instantiateMods", at = @At("RETURN"))
    public void showInstantiateModsEnd(CallbackInfoReturnable ci) {
        TaskList.removeTask(taskInstantiateMods.id);
    }
}
