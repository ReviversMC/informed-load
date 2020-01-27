package io.github.giantnuker.fabric.informedload;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.giantnuker.fabric.informedload.api.ProgressBar;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class Modloader {
    private static Modloader INSTANCE = null;
    public static Modloader getInstance(File runDir) {
        if (INSTANCE == null) INSTANCE = new Modloader(runDir);
        return INSTANCE;
    }
    private static final Identifier LOGO = new Identifier("textures/gui/title/mojang.png");
    MinecraftClient client;
    TextureManager textureManager;
    List<ProgressBar> progressBars = new ArrayList<>();
    String subText1 = "", subText2 = "";
    boolean keepRendering = true;
    File runDirectory;
    private Modloader(File runDirectory) {
        this.runDirectory = runDirectory;
        this.client = MinecraftClient.getInstance();
    }
    public void loadMods(TextureManager textureManager, Window window) {
        this.window = window;
        this.textureManager = textureManager;
        Thread loaderThread = new Thread(() -> runLoad());
        loaderThread.start();
        while (loaderThread.isAlive()) {
            render();
        }
    }
    private List<String> alreadyLoadedMains = new ArrayList<>();
    private List<String> alreadyLoadedClients = new ArrayList<>();
    public void loadExcludedEntrypoints() {
        InformedLoadUtils.LOGGER.info("Loading excluded entrypoints: " + InformedLoadUtils.config.excludedEntrypoints);
        if (InformedLoadUtils.config.excludedEntrypoints.isEmpty()) return;
        Consumer consumer = it -> {
            if (InformedLoadUtils.config.excludedEntrypoints.contains(it.getClass().getName())) {
                InformedLoadUtils.LOGGER.info("Running excluded entrypoint " + it.getClass().getName());
                if (it instanceof ClientModInitializer) {
                    ((ClientModInitializer) it).onInitializeClient();
                    alreadyLoadedClients.add(it.getClass().getName());
                } else if (it instanceof ModInitializer) {
                    ((ModInitializer) it).onInitialize();
                    alreadyLoadedMains.add(it.getClass().getName());
                }
            }
        };
        EntrypointCatcher.NormalOperations.runCommonBegins();
        InformedLoadUtils.logInitErrors("main", FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class), consumer);
        EntrypointCatcher.NormalOperations.runClientBegins();
        InformedLoadUtils.logInitErrors("client", FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class), consumer);
        InformedLoadUtils.config.excludedEntrypoints.forEach(it -> {
            if (!alreadyLoadedMains.contains(it) && !alreadyLoadedClients.contains(it)) System.err.println("[Informed Load] Unable to find entrypoint \"" + it + "\" to run early.");
        });
    }
    Window window;
    public void render() {
        GlStateManager.pushMatrix();
        GlStateManager.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0.0D, -1000.0D, 1000.0D);
        RenderSystem.matrixMode(5888);
        //GlStateManager.enableBlend();
        textureManager.bindTexture(LOGO);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0f);
        DrawableHelper.blit((window.getScaledWidth() - 256) / 2, (window.getScaledHeight() - 256) / 2 - 40, 0, 0, 0, 256, 256, 256, 256);
        for (int i = 0; i < progressBars.size(); i++) {
            ProgressBar progressBar = progressBars.get(i);
            progressBar.render(window);
        }
        renderSubText(subText2, 0);
        renderSubText(subText1, 1);
        GlStateManager.popMatrix();
        glfwSwapBuffers(window.getHandle());
        try {
            glfwPollEvents();
        } catch (NullPointerException ignoreme) {
            // ok boomer
        }
        if (GLX._shouldClose(this.window)) {
            client.stop();
        }
    }
    private void renderSubText(String text, int row) {
        InformedLoadUtils.textRenderer.draw(text, window.getScaledWidth() / 2f - InformedLoadUtils.textRenderer.getStringWidth(text) / 2f, window.getScaledHeight() - (row + 1) * 20, 0x666666);
    }
    private void runLoad() {
        progressBars.clear();
        ProgressBar overall = createProgressBar(0, ProgressBar.SplitType.NONE);
        progressBars.add(overall);
        overall.setText("Locating Entrypoints");
        InformedLoadUtils.LOGGER.info("Locating Entrypoints");
        Map<String, ModContainer> mainToContainer = new HashMap<>();
        Map<String, ModContainer> clientToContainer = new HashMap<>();
        EntrypointCatcher.NormalOperations.runContainerChecks(mainToContainer, clientToContainer);
        InformedLoadUtils.LOGGER.info("Loading Mods");
        int totalMainEntrypoints = FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class).size() - alreadyLoadedMains.size();
        int totalClientEntrypoints = FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class).size() - alreadyLoadedClients.size();
        ProgressBar mainEntrypoints = createProgressBar(1, ProgressBar.SplitType.LEFT);
        mainEntrypoints.setText(totalMainEntrypoints + " Common");
        ProgressBar clientEntrypoints = createProgressBar(1, ProgressBar.SplitType.RIGHT);
        clientEntrypoints.setText(totalClientEntrypoints + " Client");

        AtomicInteger index = new AtomicInteger(alreadyLoadedMains.size());
        AtomicInteger total = new AtomicInteger();
        BiConsumer<Object, Boolean> runInitializer = (initializer, client) -> {
            String id = initializer.getClass().getName();
            if (alreadyLoadedMains.contains(id) || alreadyLoadedClients.contains(id)) return;
            index.set(index.get() + 1);
            subText1 = "";
            subText2 = "";
            ModContainer container = (client ? clientToContainer : mainToContainer).get(id);
            for (EntrypointHandler handler : EntrypointCatcher.getHandlerEntrypoints()) {
                handler.onModInitializeBegin(container, client ? EnvType.CLIENT : EnvType.SERVER);
            }
            ModMetadata metadata = container != null ? container.getMetadata() : null;
            if (metadata != null) {
                subText1 = metadata.getName() + " (" + metadata.getId() + ")";
            } else {
                subText1 = "UNKNOWN MOD";
            }
            subText2 = id;

            InformedLoadUtils.logDebug(metadata == null ? String.format("Loading [UNKNOWN MOD]: %s (%s)", id, client ? "Client" : "Main") : String.format("Loading %s(%s): %s (%s)", metadata.getName(), metadata.getId(), id, client ? "Client" : "Main"));

            if (client) {
                Throwable error = null;
                try {
                    ((ClientModInitializer)initializer).onInitializeClient();
                } catch (Throwable e) {
                    error = e;
                }
                for (EntrypointHandler handler : EntrypointCatcher.getHandlerEntrypoints()) {
                    handler.onModInitializeEnd(container, EnvType.CLIENT, error);
                }
                clientEntrypoints.setText(index.get() + "/" + total.get() + " Client");
                clientEntrypoints.setProgress((float)(index.get()) / total.get());
                overall.setProgress(0.5f + (((float)(index.get()) / total.get()) / 2f));
            } else {
                Throwable error = null;
                try {
                    ((ModInitializer)initializer).onInitialize();
                } catch (Throwable e) {
                    error = e;
                }
                for (EntrypointHandler handler : EntrypointCatcher.getHandlerEntrypoints()) {
                    handler.onModInitializeEnd(container, EnvType.SERVER, error);
                }
                mainEntrypoints.setText(index.get() + "/" + total.get() + " Common");
                mainEntrypoints.setProgress((float)(index.get()) / total.get());
                overall.setProgress(((float)(index.get()) / total.get()) / 2f);
            }
        };
        overall.setText("Creating Render Callbacks");
        overall.setProgress(1f/8f);
        EntrypointCatcher.NormalOperations.runCommonBegins();
        overall.setText("Running Entrypoints - Common");
        progressBars.add(mainEntrypoints);
        progressBars.add(clientEntrypoints);
        total.set(totalMainEntrypoints);
        InformedLoadUtils.logInitErrors("main", FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class), initializer -> runInitializer.accept(initializer, false));
        mainEntrypoints.setProgress(1);
        mainEntrypoints.setText("Common Complete");
        EntrypointCatcher.NormalOperations.runClientBegins();
        overall.setText("Running Entrypoints - Client");
        index.set(alreadyLoadedClients.size());
        total.set(totalClientEntrypoints);
        InformedLoadUtils.logInitErrors("client", FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class), initializer -> runInitializer.accept(initializer, true));
        progressBars.remove(mainEntrypoints);
        progressBars.remove(clientEntrypoints);
        subText1 = "";
        subText2 = "";
        InformedLoadUtils.LOGGER.info("Early Modloading Complete. Starting Minecraft...");
        EntrypointCatcher.NormalOperations.runEnd();
        overall.setText("Starting Minecraft");
        overall.setProgress(1);
        keepRendering = false;
    }
    public ProgressBar createProgressBar(int row, ProgressBar.SplitType splitType) {
        return new ProgressBar.SplitProgressBar(splitType) {
            @Override
            protected int getY(Window window) {
                return row * 20 + window.getScaledHeight() / 4 * 3 - 40;
            }
        };
    }
}
