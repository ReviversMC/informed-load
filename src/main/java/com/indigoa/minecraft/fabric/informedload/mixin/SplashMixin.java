package com.indigoa.minecraft.fabric.informedload.mixin;

import com.google.gson.JsonParser;
import com.indigoa.minecraft.fabric.informedload.InformedLoad;
import com.indigoa.minecraft.fabric.informedload.TaskList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Overlay;
import net.minecraft.client.gui.SplashScreen;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Indigo Amann
 */
@Mixin(SplashScreen.class)
public class SplashMixin extends Overlay {
    //How mojang makes a progress bar
    //@Inject(at = @At("RETURN"), method = "renderProgressBar(IIIIFF)V")
    @Shadow
    private static Identifier LOGO;
    @Shadow
    private MinecraftClient client;
    @Shadow
    private ResourceReloadMonitor reloadMonitor;
    @Shadow
    private Runnable field_18218;
    @Shadow
    private boolean field_18219;
    @Shadow
    private float field_17770;
    @Shadow
    private long field_17771 = -1L;
    @Shadow
    private long field_18220 = -1L;


    //@Inject(at = @At("RETURN"), method = "render(IIF)V")
    public void extraRender(int int_1, int int_2, float float_1, CallbackInfo ci) {

    }
    @Overwrite
    public void render(int int_1, int int_2, float float_1) {
        if (InformedLoad.textRenderer == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            final FontStorage fontStorage_1 = new FontStorage(client.getTextureManager(), new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoad.FONT_JSON).getAsJsonObject()).load(client.getResourceManager())));
            InformedLoad.textRenderer = new TextRenderer(client.getTextureManager(), fontStorage_1);
        }
        int window_width = this.client.window.getScaledWidth();
        int window_height = this.client.window.getScaledHeight();
        long long_1 = SystemUtil.getMeasuringTimeMs();
        if (this.field_18219 && (this.reloadMonitor.isLoadStageComplete() || this.client.currentScreen != null) && this.field_18220 == -1L) {
            this.field_18220 = long_1;
        }

        float float_2 = this.field_17771 > -1L ? (float)(long_1 - this.field_17771) / 1000.0F : -1.0F;
        float float_3 = this.field_18220 > -1L ? (float)(long_1 - this.field_18220) / 500.0F : -1.0F;
        float float_6;
        int logo_x;
        if (float_2 >= 1.0F) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(0, 0, float_1);
            }

            logo_x = MathHelper.ceil((1.0F - MathHelper.clamp(float_2 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(0, 0, window_width, window_height, 16777215 | logo_x << 24);
            float_6 = 1.0F - MathHelper.clamp(float_2 - 1.0F, 0.0F, 1.0F);
        } else if (this.field_18219) {
            if (this.client.currentScreen != null && float_3 < 1.0F) {
                this.client.currentScreen.render(int_1, int_2, float_1);
            }

            logo_x = MathHelper.ceil(MathHelper.clamp((double)float_3, 0.15D, 1.0D) * 255.0D);
            fill(0, 0, window_width, window_height, 16777215 | logo_x << 24);
            float_6 = MathHelper.clamp(float_3, 0.0F, 1.0F);
        } else {
            fill(0, 0, window_width, window_height, -1);
            float_6 = 1.0F;
        }

        logo_x = (this.client.window.getScaledWidth() - 256) / 2;
        int logo_y = (this.client.window.getScaledHeight() - 256) / 2 - 30;
        this.client.getTextureManager().bindTexture(LOGO);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, float_6);
        this.blit(logo_x, logo_y, 0, 0, 256, 256);
        float float_7 = this.reloadMonitor.getProgress();
        this.field_17770 = this.field_17770 * 0.95F + float_7 * 0.050000012F;
        if (float_2 < 1.0F) { // Display in here
            int y = logo_y + window_height / 4 * 3;
            int text_height = 8;
            int middle_x = window_width / 2;

            String status = "Loading";
            if (!TaskList.isEmpty()) {
                Iterator iterator = TaskList.iterator();
                while (iterator.hasNext()) {
                    status += " - " + ((TaskList.Task) iterator.next()).name;
                }
            }
            float fadeAmount = 1.0F - MathHelper.clamp(float_2, 0.0F, 1.0F);
            InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, this.field_17770, status, fadeAmount);
            y += 20;
            if (!TaskList.isEmpty()) {
                Iterator iterator = TaskList.iterator();
                while (iterator.hasNext()) {
                    y = ((TaskList.Task)iterator.next()).render(y, middle_x, window_width, window_height, fadeAmount);
                }
            }
        }

        if (float_2 >= 2.0F) {
            this.client.setOverlay((Overlay)null);
        }

        if (this.field_17771 == -1L && this.reloadMonitor.isApplyStageComplete() && (!this.field_18219 || float_3 >= 2.0F)) {
            this.reloadMonitor.throwExceptions();
            this.field_17771 = SystemUtil.getMeasuringTimeMs();
            this.field_18218.run();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, this.client.window.getScaledWidth(), this.client.window.getScaledHeight());
            }
        }

    }
}
