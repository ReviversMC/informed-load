package com.gitlab.indigoa.fabric.informedload.mixin;

import com.gitlab.indigoa.fabric.informedload.InformedLoadUtils;
import com.gitlab.indigoa.fabric.informedload.Modloader;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.MinecraftClient.DEFAULT_TEXT_RENDERER_ID;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow @Final public File runDirectory;

    @Shadow @Final private ResourcePackContainerManager<ClientResourcePackContainer> resourcePackContainerManager;

    @Shadow public abstract void onResolutionChanged();

    @Shadow public abstract boolean forcesUnicodeFont();

    @Shadow public GameOptions options;

    @Shadow @Final private Thread thread;

    @Shadow public Window window;

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/entrypoint/hooks/EntrypointClient;start(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private void start(File runDir, Object gameInstance) {
        // You aint doin nothin fabric
    }
    // Note: this reference works because fabric loader creates it with ASM - do not delete
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackContainerManager;callCreators()V"))
    private void moveModload(ResourcePackContainerManager resourcePackContainerManager) {
        ReloadableResourceManagerImpl resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES, this.thread);
        this.resourcePackContainerManager.callCreators();
        List<ResourcePack> list_1 = (List)this.resourcePackContainerManager.getEnabledContainers().stream().map(ResourcePackContainer::createResourcePack).collect(Collectors.toList());
        Iterator var8 = list_1.iterator();
        while(var8.hasNext()) {
            ResourcePack resourcePack_1 = (ResourcePack)var8.next();
            resourceManager.addPack(resourcePack_1);
        }

        LanguageManager languageManager = new LanguageManager(this.options.language);
        resourceManager.registerListener(languageManager);
        languageManager.reloadResources(list_1);
        TextureManager textureManager = new TextureManager(resourceManager);
        this.onResolutionChanged();
        FontManager fontManager = new FontManager(textureManager, forcesUnicodeFont());
        resourceManager.registerListener(fontManager.getResourceReloadListener());
        TextRenderer textRenderer = fontManager.getTextRenderer(DEFAULT_TEXT_RENDERER_ID);
        //if (this.options.language != null) {
        //    this.textRenderer.setRightToLeft(this.languageManager.isRightToLeft());
        //}

        GlStateManager.enableTexture();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(GlStateManager.FaceSides.BACK);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);

        GlStateManager.viewport(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());

        //if (this.options.language != null) {
        //    this.textRenderer.setRightToLeft(this.languageManager.isRightToLeft());
        //}
        if (InformedLoadUtils.textRenderer == null) {
            MinecraftClient client = (MinecraftClient)(Object) this;
            final FontStorage fontStorage_1 = new FontStorage(textureManager, new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(resourceManager)));
            InformedLoadUtils.textRenderer = new TextRenderer(textureManager, fontStorage_1);
        }
        new Modloader().loadMods((MinecraftClient)(Object) this, window, runDirectory);
    }
}
