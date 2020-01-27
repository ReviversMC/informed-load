package io.github.giantnuker.fabric.informedload.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.giantnuker.fabric.informedload.Config;
import io.github.giantnuker.fabric.informedload.InformedLoadUtils;
import io.github.giantnuker.fabric.informedload.Modloader;
import com.google.gson.JsonParser;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1.serializer.JanksonConfigSerializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointClient;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.ClientResourcePackProfile;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow @Final public File runDirectory;

    @Shadow @Final private ResourcePackManager<ClientResourcePackProfile> resourcePackManager;

    @Shadow public abstract boolean forcesUnicodeFont();

    @Shadow public GameOptions options;

    @Shadow public Window window;

    @Shadow @Final private TextureManager textureManager;

    @Shadow public abstract Framebuffer getFramebuffer();

    @Shadow @Final public static boolean IS_SYSTEM_MAC;

    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;addResourcePackProfilesToManager(Lnet/minecraft/resource/ResourcePackManager;)V"))
    private void moveModload(GameOptions gameOptions, ResourcePackManager<ClientResourcePackProfile> manager) {
        gameOptions.addResourcePackProfilesToManager(manager);
        if (InformedLoadUtils.config.entrypointDisplay) {
            InformedLoadUtils.isDoingEarlyLoad = true;
            RenderSystem.setupDefaultState(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());

            ReloadableResourceManagerImpl resourceManager = (ReloadableResourceManagerImpl) this.resourceManager;
            resourcePackManager.scanPacks();
            List<ResourcePack> list = resourcePackManager.getEnabledProfiles().stream().map(ResourcePackProfile::createResourcePack).collect(Collectors.toList());
            for (ResourcePack resourcePack_1 : list) {
                resourceManager.addPack(resourcePack_1);
            }

            LanguageManager languageManager = new LanguageManager(this.options.language);
            resourceManager.registerListener(languageManager);
            languageManager.reloadResources(list);
            InformedLoadUtils.textureManager = new TextureManager(resourceManager);

            int i = this.window.calculateScaleFactor(this.options.guiScale, this.forcesUnicodeFont());
            this.window.setScaleFactor((double)i);

            Framebuffer framebuffer = this.getFramebuffer();
            framebuffer.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);

            FontManager fontManager = new FontManager(InformedLoadUtils.textureManager, forcesUnicodeFont());
            resourceManager.registerListener(fontManager.getResourceReloadListener());

            final FontStorage fontStorage_1 = new FontStorage(InformedLoadUtils.textureManager, new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(resourceManager)));
            InformedLoadUtils.textRenderer = new TextRenderer(InformedLoadUtils.textureManager, fontStorage_1);

            Modloader.getInstance(runDirectory).loadMods(InformedLoadUtils.textureManager, window);
        }
    }
    @Inject(method = "getTextureManager", at = @At("HEAD"), cancellable = true)
    public void changeTextureManager(CallbackInfoReturnable<TextureManager> cir) {
        if (textureManager == null) cir.setReturnValue(InformedLoadUtils.textureManager);
    }
}
