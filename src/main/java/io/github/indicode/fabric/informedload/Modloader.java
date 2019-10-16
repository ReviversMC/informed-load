package io.github.indicode.fabric.informedload;

import com.mojang.blaze3d.platform.GLX;
import io.github.indicode.fabric.informedload.api.ProgressBar;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.metadata.EntrypointMetadata;
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
    private boolean instancesLoaded = false;
    private List<String> alreadyLoadedMains = new ArrayList<>();
    private List<String> alreadyLoadedClients = new ArrayList<>();
    private void loadInstances() {
        if (!instancesLoaded) FabricLoader.INSTANCE.instantiateMods(runDirectory, client);
        instancesLoaded = true;
    }
    public void loadExcludedEntrypoints() {
        System.out.println("[Informed Load] Loading excluded entrypoints: " + InformedLoadUtils.config.excludedEntrypoints);
        if (InformedLoadUtils.config.excludedEntrypoints.isEmpty()) return;
        loadInstances();
        Consumer consumer = it -> {
            if (InformedLoadUtils.config.excludedEntrypoints.contains(it.getClass().getName())) {
                System.out.println("[Informed Load] Running excluded entrypoint " + it.getClass().getName());
                if (it instanceof ClientModInitializer) {
                    ((ClientModInitializer) it).onInitializeClient();
                    alreadyLoadedClients.add(it.getClass().getName());
                } else if (it instanceof ModInitializer) {
                    ((ModInitializer) it).onInitialize();
                    alreadyLoadedMains.add(it.getClass().getName());
                }
            }
        };
        InformedLoadUtils.logInitErrors("main", FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class), consumer);
        InformedLoadUtils.logInitErrors("client", FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class), consumer);
        InformedLoadUtils.config.excludedEntrypoints.forEach(it -> {
            if (!alreadyLoadedMains.contains(it) && !alreadyLoadedClients.contains(it)) System.err.println("[Informed Load] Unable to find entrypoint \"" + it + "\" to run early.");
        });
    }
    Window window;
    public void render() {
        GlStateManager.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0.0D, -1000.0D, 1000.0D);
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
        glfwSwapBuffers(window.getHandle());
        glfwPollEvents();
        if (GLX.shouldClose(this.window)) {
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
        overall.setText("Instancing Mods");
        loadInstances();
        overall.setText("Locating Entrypoints");
        ProgressBar entrypointBar = createProgressBar(1, ProgressBar.SplitType.NONE);
        entrypointBar.setText("Starting");
        progressBars.add(entrypointBar);
        Map<String, ModMetadata> mainToMeta = new HashMap<>();
        Map<String, ModMetadata> clientToMeta = new HashMap<>();
        ArrayList<net.fabricmc.loader.api.ModContainer> newArrayList = Lists.newArrayList(FabricLoader.INSTANCE.getAllMods());
        for (int i = 0; i < newArrayList.size(); i++) {
            net.fabricmc.loader.api.ModContainer modContainer = newArrayList.get(i);
            ModMetadata metadata = modContainer.getMetadata();
            entrypointBar.setText(i + "/" + newArrayList.size() + " - " + metadata.getName() + " (" + metadata.getId() + ")");
            if (modContainer instanceof ModContainer) {
                ModContainer mod = (ModContainer) modContainer;
                for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("main")) {
                    mainToMeta.put(entrypoint.getValue(), metadata);
                }
                for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("client")) {
                    clientToMeta.put(entrypoint.getValue(), metadata);
                }
            }
        }
        int totalMainEntrypoints = FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class).size();
        int totalClientEntrypoints = FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class).size() - alreadyLoadedClients.size();
        progressBars.remove(entrypointBar);
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
            ModMetadata metadata = (client ? clientToMeta : mainToMeta).get(id);
            if (metadata != null) {
                subText1 = metadata.getName() + " (" + metadata.getId() + ")";
            } else {
                subText1 = "UNKNOWN MOD";
            }
            subText2 = id;

            if (client) {
                ((ClientModInitializer)initializer).onInitializeClient();
                clientEntrypoints.setText(index.get() + "/" + total.get() + " Client");
                clientEntrypoints.setProgress((float)(index.get()) / total.get());
                overall.setProgress((1f/2f) + 0.25f + (((float)(index.get()) / total.get()) / 4f));
            } else {
                ((ModInitializer)initializer).onInitialize();
                mainEntrypoints.setText(index.get() + "/" + total.get() + " Common");
                mainEntrypoints.setProgress((float)(index.get()) / total.get());
                overall.setProgress((1f/2f) + (((float)(index.get()) / total.get()) / 4f));
            }
        };
        overall.setText("Creating Render Callbacks");
        overall.setProgress(1f/2f);
        overall.setText("Running Entrypoints - Common");
        progressBars.add(mainEntrypoints);
        progressBars.add(clientEntrypoints);
        total.set(totalMainEntrypoints);
        InformedLoadUtils.logInitErrors("main", FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class), initializer -> runInitializer.accept(initializer, false));
        mainEntrypoints.setProgress(1);
        mainEntrypoints.setText("Common Complete");
        overall.setText("Running Entrypoints - Client");
        index.set(alreadyLoadedClients.size());
        total.set(totalClientEntrypoints);
        InformedLoadUtils.logInitErrors("client", FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class), initializer -> runInitializer.accept(initializer, true));
        progressBars.remove(mainEntrypoints);
        progressBars.remove(clientEntrypoints);
        subText1 = "";
        subText2 = "";
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
