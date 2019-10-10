package com.gitlab.indigoa.fabric.informedload;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class Modloader {
    MinecraftClient client;
    String mod = "", id = "";
    public void loadMods(MinecraftClient mcclient, Window window, File runDirectory) {
        this.client = mcclient;
        this.window = window;

        /*if (runDir == null) {
            runDir = new File(".");
        }*/
        long ttimeStart = new Date().getTime();
        long itimeStart = new Date().getTime();
        FabricLoader.INSTANCE.instantiateMods(runDirectory, this);
        long itimeEnd = new Date().getTime();
        System.out.println("Instantiation took " + (itimeEnd - itimeStart) + " ms");
        Map<String, ModMetadata> mainToMeta = new HashMap<>();
        Map<String, ModMetadata> clientToMeta = new HashMap<>();
        for (net.fabricmc.loader.api.ModContainer modContainer : FabricLoader.INSTANCE.getAllMods()) {
            ModMetadata metadata = modContainer.getMetadata();
            System.out.println("Mod Discovered: " + metadata.getName() + "(" + metadata.getId() + ")");
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
        AtomicInteger index = new AtomicInteger();
        BiConsumer<Object, Boolean> runInitializer = (initializer, client) -> {
            index.set(index.get() + 1);
            long timeStart = new Date().getTime();
            String id = initializer.getClass().getName();
            ModMetadata metadata = (client ? clientToMeta : mainToMeta).get(id);
            String ownerString = null;
            if (metadata != null) {
                ownerString = metadata.getName() + " (" + metadata.getId() + ")";
            }
            //--status.get().accept("Running " + (client ? "client" : "") + " initializer " + id + " for mod " + ownerString);
            //System.out.println("Attempting to run " + (client ? "client" : "") + " initializer " + id + " for mod " + ownerString);
            //renderStatusText(ownerString);
            mod = ownerString;
            this.id = id;
            render();
            if (client) ((ClientModInitializer)initializer).onInitializeClient();
            else ((ModInitializer)initializer).onInitialize();
            long timeEnd = new Date().getTime();
            System.out.println("Ran " + (client ? "client" : "") + " initializer " + id + " for mod " + ownerString + " (" + (timeEnd - timeStart) + " ms)");
            //--progress.get().accept((double)(index.get()) / (clientToMeta.size() + mainToMeta.size()));
        };
        InformedLoadUtils.logInitErrors("main", FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class), initializer -> runInitializer.accept(initializer, false));
        InformedLoadUtils.logInitErrors("client", FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class), initializer -> runInitializer.accept(initializer, true));
        long ttimeEnd = new Date().getTime();
        System.out.println("Total Init took " + (ttimeEnd - ttimeStart) + " ms");
    }
    Window window;
    public void render() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0.0D, -1000.0D, 1000.0D);
        renderProgressBar(id, 0.6f, 1);
        glfwSwapBuffers(window.getHandle());
        glfwPollEvents();
    }
    public void renderProgressBar(String text, float progress, int row) {
        if (text == null) text = "<NULL>";
        int x = window.getScaledWidth() / 2 - 150;
        int xm = window.getScaledWidth() / 2 + 150;
        int y = window.getScaledHeight() / 2 + row * 20;
        int ym = y + 10;
        InformedLoadUtils.makeProgressBar(x, y, xm, ym, progress, text, 1, true);
    }
}
