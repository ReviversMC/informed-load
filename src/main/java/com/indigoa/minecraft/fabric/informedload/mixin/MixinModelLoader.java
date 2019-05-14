package com.indigoa.minecraft.fabric.informedload.mixin;

import com.indigoa.minecraft.fabric.informedload.Config;
import com.indigoa.minecraft.fabric.informedload.InformedLoad;
import com.indigoa.minecraft.fabric.informedload.TaskList;
import net.minecraft.block.Block;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.Line;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Indigo Amann
 */
@Mixin(ModelLoader.class)
public class MixinModelLoader {
    @Shadow
    private Map<Identifier, UnbakedModel> modelsToBake;
    @Shadow
    private Map<Identifier, BakedModel> bakedModels;
    TaskAddModels taskAddModels = null;
    static class TaskAddModels extends TaskList.Task {
        private int items = 0, items_o, blocks = 0, blocks_o;
        private static final Color c2 = new Color(200, 198, 0);
        public TaskAddModels(int items, int blocks) {
            super("addmodels", I18n.translate("Adding to Bakery"));
            TaskLoadModels taskLoadModels = TaskLoadModels.INSTANCE;
            taskLoadModels.setStage(0);
            taskLoadModels.stagePercentage = 0;
            this.items_o = items;
            this.blocks_o = blocks;
        }
        @Override
        public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
            if (items > items_o) items = items_o;      // Well do you want people to see 879/877 Items?
            if (blocks > blocks_o) blocks = blocks_o;  // Hasn't happened yet - but just to make sure
            if (InformedLoad.config.maxProgressBarRows > 2) {
                if (InformedLoad.config.splitProgressBars == Config.SplitType.SPLIT) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 - 5, y + 10, Color.RED, Color.BLACK, (float) blocks / (float) blocks_o, blocks + "/" + blocks_o + " Blocks Added", fadeAmount);
                    InformedLoad.makeProgressBar(window_width / 2 + 5, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (float) items / (float) items_o, items + "/" + items_o + " Items Added", fadeAmount);
                } else if (InformedLoad.config.splitProgressBars == Config.SplitType.SINGLE) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (float) (blocks + items) / (float) (blocks_o + items_o), blocks + "/" + blocks_o + " Blocks Added - " + items + "/" + items_o + " Items Added", fadeAmount);
                } else {
                    if (blocks < blocks_o) {
                        InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (float) blocks / (float) blocks_o, blocks + "/" + blocks_o + " Blocks Added", fadeAmount);
                    } else {
                        InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (float) items / (float) items_o, items + "/" + items_o + " Items Added", fadeAmount);
                    }
                }
            }
            TaskLoadModels taskLoadModels = TaskLoadModels.INSTANCE;
            taskLoadModels.stagePercentage = (float) (blocks + items) / (float) (blocks_o + items_o);
            y += 20;
            return y;
        }
        public void item() {
            items++;
        }
        public void block() {
            blocks++;
        }
    }
    TaskBakeModels taskBakeModels;
    static class TaskBakeModels extends TaskList.Task {
        private int tobake, baked = 0;
        public TaskBakeModels(int models) {
            super("bakemodels", "Baking");
            TaskLoadModels.INSTANCE.setStage(2);
            this.tobake = models;
        }
        @Override
        public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
            if (baked > tobake) baked = tobake; // Hasn't happened yet - but just to make sure
            if (InformedLoad.config.maxProgressBarRows > 2) {
                InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (float) baked / (float) tobake, baked + "/" + tobake + " Models Baked", fadeAmount);
            }
            TaskLoadModels.INSTANCE.stagePercentage = (float) baked / tobake;
            y += 20;
            return y;
        }
        public void bake(int baked) {
            this.baked = baked;
        }
    }
    @Inject(method = "addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", at = @At("RETURN"))
    private void onModelAddStart(ModelIdentifier modelIdentifier_1, CallbackInfo ci) {
        if (taskAddModels == null) { // Basically an init for this...
            if (!TaskList.hasTask("loadmodels")) TaskList.addTask(TaskList.Task.TaskLoadModels.INSTANCE);
            int items = 0;
            AtomicInteger blocks = new AtomicInteger();
            //Shamelessly stolen from ModelLoader
            Iterator var4 = Registry.BLOCK.iterator();

            while(var4.hasNext()) {
                Block block_1 = (Block)var4.next();
                block_1.getStateFactory().getStates().forEach((blockState_1) -> {
                    blocks.getAndIncrement();
                });
            }

            var4 = Registry.ITEM.getIds().iterator();

            while(var4.hasNext()) {
                Identifier identifier_1 = (Identifier)var4.next();
                items++;
            }
            taskAddModels = new TaskAddModels(items, blocks.get());
            TaskList.addTask(taskAddModels);
        } else {
            //System.out.println(TaskList.getTask("addmodels"));
        }
    }

    @Inject(method = "addModel(Lnet/minecraft/client/util/ModelIdentifier;)V", at = @At("RETURN"))
    private void onModelAddEnd(ModelIdentifier modelIdentifier_1, CallbackInfo ci) {
        //InformedLoad.taskAddModelss.remove(InformedLoad.taskAddModelss.size() - 1);
        String[] split = modelIdentifier_1.toString().split("#");
        //if (STATIC_DEFINITIONS.containsKey(new Identifier(split[0]))) {
        //    System.out.println("STATIC -- " + split[0]);
        //} else {
            if (Registry.BLOCK.containsId(new Identifier(split[0]))) {
                //System.out.println("Block contains " + modelIdentifier_1.toString().split("#")[0]);
                if (split.length == 1 || !split[1].equals("inventory")) taskAddModels.block();
                else if (Registry.ITEM.containsId(new Identifier(split[0]))) {
                    taskAddModels.item();
                }
            }
            else if (Registry.ITEM.containsId(new Identifier(split[0]))) {
                taskAddModels.item();
            }
            if (taskAddModels.items == taskAddModels.items_o && taskAddModels.blocks == taskAddModels.blocks_o) TaskList.removeTask("addmodels");
        //}
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    private void initDone(ResourceManager resourceManager, SpriteAtlasTexture spriteAtlasTexture, Profiler profiler, CallbackInfo ci) {
        //TaskList.removeTask("addmodels");
    }
    @Inject(method = "upload(Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
    private void listProgressUpload(Profiler profiler, CallbackInfo ci) {
        taskBakeModels = new TaskBakeModels(modelsToBake.size());
        TaskList.addTask(taskBakeModels);
    }

    @Inject(method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", at = @At("RETURN"))
    private void listProgressModelBake(Identifier identifier_1, ModelBakeSettings modelBakeSettings_1, CallbackInfoReturnable ci) {
        taskBakeModels.bake(bakedModels.size());
    }

}
