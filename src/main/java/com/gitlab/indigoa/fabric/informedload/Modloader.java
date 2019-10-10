package com.gitlab.indigoa.fabric.informedload;

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
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0D, window.getWidth(), window.getHeight(), 0.0D, -1000.0D, 1000.0D);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

//            // replace with more modern opengl?
//            glBegin(GL_QUADS);
//            glColor3f(0.1f, 0.1f, 0.9f);
//            glVertex2f(0, 0);
//            glVertex2f(0, screenHeight);
//            glVertex2f(screenWidth * progress, screenHeight);
//            glVertex2f(screenWidth * progress, 0);
//            glEnd();

        glEnableClientState(GL11.GL_VERTEX_ARRAY);
        glEnable(GL_BLEND);
        forge_renderMessage("Initializing mods:", new float[]{0, 0, 0, 1}, 1, 1);
        forge_renderMessage(mod, new float[]{0, 0, 0, 1}, 2, 1);
        forge_renderMessage(id, new float[]{0.5f, 0.5f, 0.5f, 1}, 4, 1);
        glfwSwapBuffers(window.getHandle());
        glfwPollEvents();
    }
    /*public void renderStatusText(String text) {
        int x = window.getWidth() / 2;
        int y = 50;
        System.out.println("TXT:  " + text);
        //InformedLoadUtils.textRenderer.draw(text, x - InformedLoadUtils.textRenderer.getStringWidth(text) / 2f, y, 20);
        forge_renderMessage(text, new float[]{1f,0f,0f,1f}, 5, 1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
    private void forge_renderMessage(String message, final float[] colour, int row, float alpha) {
        if (message == null) message = "<Unknown>";
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);

        glVertexPointer(3, GL11.GL_FLOAT, 16, charBuffer);
        glEnable(GL_BLEND);
        GL14.glBlendColor(0,0,0, alpha);
        glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        glColor3f(colour[0], colour[1], colour[2]);
        glPushMatrix();
        glTranslatef(10, row * 20, 0);
        glScalef(2, 2, 1);
        glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
        glPopMatrix();
        MemoryUtil.memFree(charBuffer);
    }
}
