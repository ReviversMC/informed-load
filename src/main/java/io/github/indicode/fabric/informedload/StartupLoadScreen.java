package io.github.indicode.fabric.informedload;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class StartupLoadScreen {
    private final int screenWidth = 800;
    private final int screenHeight = 400;
    private long window;
    private Thread thread;
    public boolean running = false, actuallyRunning = false;
    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        window = glfwCreateWindow(screenWidth, screenHeight, "Minecraft is starting", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window"); // ignore it and make the GUI optional?
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer monPosLeft = stack.mallocInt(1);
            IntBuffer monPosTop = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwGetMonitorPos(glfwGetPrimaryMonitor(), monPosLeft, monPosTop);
            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2 + monPosLeft.get(0),
                    (vidmode.height() - pHeight.get(0)) / 2 + monPosTop.get(0)
            );
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    private void render() {

    }
    private void stop() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }
    public void start() {
        thread = new Thread(this::run);
        thread.setDaemon(true);
        thread.start();
    }
    public void stopScreen() {
        running = false;
        System.out.println("StartFree");
        while (actuallyRunning) {
            // Aand we wait
        }
        System.out.println("StopFree");
    }
    private void run() {
        init();
        running = true;
        actuallyRunning = true;
        while (running) {
            render();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        stop();
        actuallyRunning = false;
    }
}
