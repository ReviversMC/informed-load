package com.indigoa.minecraft.fabric.informedload.mixin;

import com.indigoa.minecraft.fabric.informedload.TaskList;
import net.fabricmc.loader.entrypoint.hooks.EntrypointClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

/**
 * @author Indigo Amann
 */
//@Mixin(EntrypointClient.class)
public class EntrypointClientMixin {
    //@Inject(method = "<init>", at = @At("RETURN"))
    public void showModLoadEnd(CallbackInfo ci) {
        TaskList.removeTask(FabricLoaderMixin.taskLoadMods.id);
    }
}
