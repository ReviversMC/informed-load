package io.github.indicode.fabric.informedload.mixin;

import com.google.common.collect.Lists;
import io.github.indicode.fabric.informedload.Config;
import io.github.indicode.fabric.informedload.InformedLoadUtils;
import io.github.indicode.fabric.informedload.Modloader;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1.serializer.Toml4jConfigSerializer;
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
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.ClientResourcePackContainer;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.ArrayList;
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

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/entrypoint/minecraft/hooks/EntrypointClient;start(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private void stopFabricInit(File runDir, Object gameInstance) {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        InformedLoadUtils.config = AutoConfig.getConfigHolder(Config.class).getConfig();
        if (InformedLoadUtils.config.printEntrypoints) {
            InformedLoadUtils.config.printEntrypoints = false;
            ((ConfigManager)AutoConfig.getConfigHolder(Config.class)).save();
            //Printing entrypoints
            System.out.println("[Informed Load] Displaying mod entrypoints:");
            ArrayList<ModContainer> newArrayList = Lists.newArrayList(FabricLoader.INSTANCE.getAllMods());
            for (int i = 0; i < newArrayList.size(); i++) {
                net.fabricmc.loader.api.ModContainer modContainer = newArrayList.get(i);
                ModMetadata metadata = modContainer.getMetadata();
                if (modContainer instanceof net.fabricmc.loader.ModContainer) {
                    net.fabricmc.loader.ModContainer mod = (net.fabricmc.loader.ModContainer) modContainer;
                    boolean hasMain = !mod.getInfo().getEntrypoints("main").isEmpty();
                    boolean hasClient = !mod.getInfo().getEntrypoints("client").isEmpty();
                    if (!hasClient && !hasMain) continue;
                    System.out.print(metadata.getName() + " (" + metadata.getId() + ")\n");
                    //if (hasMain) System.out.print("|-Main Entrypoints\n");
                    for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("main")) {
                        System.out.print("|--" + entrypoint.getValue() + "\n");
                    }
                    //if (hasClient) System.out.print("|-Client Entrypoints\n");
                    for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("client")) {
                        System.out.print("|--" + entrypoint.getValue() + "\n");
                    }
                }
            }
            System.out.println("[Informed Load] Done, Shutting down Minecraft.");
            //End
            System.exit(0);
        }
        if (!InformedLoadUtils.config.entrypointDisplay) {
            EntrypointClient.start(runDir, gameInstance);
        } else {
            Modloader.getInstance(runDirectory).loadExcludedEntrypoints();
        }
    }
    // Note: this reference works because fabric loader creates it with ASM - do not delete
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;addResourcePackContainersToManager(Lnet/minecraft/resource/ResourcePackContainerManager;)V"))
    private void moveModload(GameOptions gameOptions, ResourcePackContainerManager<ClientResourcePackContainer> resourcePackContainerManager_1) {
        if (InformedLoadUtils.config.entrypointDisplay) {
            ReloadableResourceManagerImpl resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES, this.thread);
            this.resourcePackContainerManager.callCreators();
            List<ResourcePack> list_1 = this.resourcePackContainerManager.getEnabledContainers().stream().map(ResourcePackContainer::createResourcePack).collect(Collectors.toList());
            Iterator var8 = list_1.iterator();
            while (var8.hasNext()) {
                ResourcePack resourcePack_1 = (ResourcePack) var8.next();
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
                MinecraftClient client = (MinecraftClient) (Object) this;
                final FontStorage fontStorage_1 = new FontStorage(textureManager, new Identifier("loading"));
                fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(resourceManager)));
                InformedLoadUtils.textRenderer = new TextRenderer(textureManager, fontStorage_1);
            }
            Modloader.getInstance(runDirectory).loadMods(textureManager, window);
        }
    }
}
