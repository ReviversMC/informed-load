package com.gitlab.indigoa.fabric.informedload.mixin;

import com.gitlab.indigoa.fabric.informedload.IProgressLogger;
import com.gitlab.indigoa.fabric.informedload.IProgressTracker;
import com.gitlab.indigoa.fabric.informedload.InformedLoad;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

/**
 * @author Indigo A.
 */
@Mixin(LevelLoadingScreen.class)
public class MixinWorldGenProgress  {
    @Shadow
    public  WorldGenerationProgressTracker progressProvider;
    @Inject(method = "render", at = @At("RETURN"))
    private void drawToolTip(int x, int y, float float_1, CallbackInfo ci) {
        if (!InformedLoad.config.worldload_hateDisplay.progressBarDisplay) {
            for(int int_14 = 0; int_14 < InformedLoad.WorldGen.int_8; ++int_14) {
                for(int int_15 = 0; int_15 < InformedLoad.WorldGen.int_8; ++int_15) {
                    ChunkStatus chunkStatus_1 = progressProvider.getChunkStatus(int_14, int_15);
                    int int_16 = InformedLoad.WorldGen.int_10 + int_14 * InformedLoad.WorldGen.int_5;
                    int int_17 = InformedLoad.WorldGen.int_11 + int_15 * InformedLoad.WorldGen.int_5;
                    //fill(int_16, int_17, int_16 + InformedLoad.WorldGen.int_3, int_17 + InformedLoad.WorldGen.int_3, Color.RED.getRGB());
                    if (chunkStatus_1 != null && x >= int_16 && x <= int_16 + InformedLoad.WorldGen.int_3 && y >= int_17 && y <= int_17 + InformedLoad.WorldGen.int_3) {
                        ((LevelLoadingScreen)(Object)this).renderTooltip("Step: " + InformedLoad.STATUS_TO_NAME.get(chunkStatus_1), x, y);
                    }
                }
            }
            //screen.renderTooltip("FOOO1111", x, y);
        }
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/LevelLoadingScreen;drawChunkMap(Lnet/minecraft/client/gui/WorldGenerationProgressTracker;IIII)V"))
    private void redrawChunkMap(WorldGenerationProgressTracker worldGenerationProgressTracker_1, int x, int y, int scale, int int_4) {
        if (!InformedLoad.config.worldload_hateDisplay.progressBarDisplay) {
            if (InformedLoad.config.worldload_loveDisplay.showProgressBar) {
                y -= 35;
            }
            if (InformedLoad.config.worldload_loveDisplay.bigChunkViewer) {
                //x -= amount;
                //y -= amount;
                scale += 1;
                //int_4 += amount;
                if (InformedLoad.config.worldload_loveDisplay.showProgressBar) {
                    y -= 10;
                }
            }
            InformedLoad.WorldGen.int_3 = scale;
            InformedLoad.WorldGen.int_5 = scale + int_4;
            InformedLoad.WorldGen.int_6 = worldGenerationProgressTracker_1.getCenterSize();
            InformedLoad.WorldGen.int_7 = InformedLoad.WorldGen.int_6 * InformedLoad.WorldGen.int_5 - int_4;
            InformedLoad.WorldGen.int_8 = worldGenerationProgressTracker_1.getSize();
            InformedLoad.WorldGen.int_9 = InformedLoad.WorldGen.int_8 * InformedLoad.WorldGen.int_5 - int_4;
            InformedLoad.WorldGen.int_10 = x - InformedLoad.WorldGen.int_9 / 2;
            InformedLoad.WorldGen.int_11 = y - InformedLoad.WorldGen.int_9 / 2;
            InformedLoad.WorldGen.int_12 = InformedLoad.WorldGen.int_7 / 2 + 1;
            InformedLoad.WorldGen.int_13 = -16772609;
            InformedLoad.drawChunkMap(worldGenerationProgressTracker_1, x, y, scale, int_4, InformedLoad.config.worldload_loveDisplay.simplifyColors);
        } else {
            IProgressTracker progressTracker = ((IProgressTracker)((Object)worldGenerationProgressTracker_1));
            WorldGenerationProgressLogger progressLogger = progressTracker.getProgressLogger();
            float totals = ((IProgressLogger)((Object)progressLogger)).getTotalCount();
            int biomes = 0, noise = 0, surface = 0, carvers = 0, liquid_carvers = 0, features = 0, light = 0, spawn = 0, full = 0;
            for (ChunkStatus e: progressTracker.getChunkStatuses().values()) {
                if (e == ChunkStatus.BIOMES) {
                    biomes++;
                }
                else if (e == ChunkStatus.NOISE) {
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.SURFACE) {
                    surface++;
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.CARVERS) {
                    carvers++;
                    surface++;
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.LIQUID_CARVERS) {
                    liquid_carvers++;
                    carvers++;
                    surface++;
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.FEATURES) {
                    features++;
                    liquid_carvers++;
                    carvers++;
                    surface++;
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.LIGHT) {
                    light++;
                    features++;
                    liquid_carvers++;
                    carvers++;
                    surface++;
                    noise++;
                    biomes++;
                }
                else if (e == ChunkStatus.FULL) {
                    full++;
                    light++;
                    features++;
                    liquid_carvers++;
                    carvers++;
                    surface++;
                    noise++;
                    biomes++;
                }
            }
            Color outer = Color.DARK_GRAY;
            Color inner = Color.LIGHT_GRAY;
            // make Progress bars
            float pbiomes = biomes > totals ? totals : biomes, pnoise = noise > totals ? totals : noise,
                    psurface = surface > totals ? totals : surface, pcarvers = carvers > totals ? totals : carvers,
                    pliquid_carvers = liquid_carvers > totals ? totals : liquid_carvers, pfeatures = features > totals ? totals : features,
                    plight = light > totals ? totals : light;
            int py = y - 110;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, pbiomes / totals, "Biomes: " + biomes + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, pnoise / totals, "Noise: " + noise + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, psurface / totals, "Surface: " + surface + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, pcarvers / totals, "Carvers: " + carvers + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, pliquid_carvers / totals, "Liquid Carvers: " + liquid_carvers + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, pfeatures / totals, "Features: " + features + "/" + (int)totals, 1, outer, inner);
            py += 20;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, plight / totals, "Light: " + light + "/" + (int)totals, 1, outer, inner);
            py += 30;
            InformedLoad.makeProgressBar(x - 150, py, x + 150, py + 10, full / totals, "Fully Loaded: " + full + "/" + (int)totals + " - " + (int)((full/totals)*100) + "%", 1, outer, new Color(182, 220, 182));
        }
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/LevelLoadingScreen;drawCenteredString(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"))
    public void drawCenteredStringOrProgressBar(LevelLoadingScreen LevelLoadingScreen, TextRenderer textRenderer_1, String string_1, int x, int y, int int_3) {
        IProgressTracker progressTracker = ((IProgressTracker)((Object)progressProvider));
        WorldGenerationProgressLogger progressLogger = progressTracker.getProgressLogger();
        int totals = ((IProgressLogger)((Object)progressLogger)).getTotalCount();
        int done = ((IProgressLogger)((Object)progressLogger)).getGeneratedCount();
        if (!InformedLoad.config.worldload_hateDisplay.progressBarDisplay) {
            if (InformedLoad.config.worldload_loveDisplay.showProgressBar) {
                y += 95;
                if (InformedLoad.config.worldload_loveDisplay.bigChunkViewer) y += 30;
                InformedLoad.makeProgressBar(x - 150, y, x + 150, y + 10, progressProvider.getProgressPercentage() / 100f, done + "/" + totals + " Chunks Loaded - " + string_1, 1, Color.DARK_GRAY, Color.LIGHT_GRAY);
            } else {
                LevelLoadingScreen.drawCenteredString(textRenderer_1, string_1, x, y, int_3);
            }
        }
    }
}
