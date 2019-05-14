package com.indigoa.minecraft.fabric.informedload;

import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Indigo Amann
 */
public class TaskList {
    public static class Task {
        public final String id, name;
        public Task(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
            return y;
        }

        public static class TaskLoadModels extends  TaskList.Task {
            public static final TaskLoadModels INSTANCE = new TaskLoadModels();
            private int stage = 0;
            public float stagePercentage = 0;
            public TaskLoadModels() {
                super("loadmodels", "Models");
            }
            @Override
            public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
                String name = stage == 0 ? "Adding to Bakery" : stage == 1 ? "Stitching Textures" : "Baking";
                if (InformedLoad.config.maxProgressBarRows > 1) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, Color.RED, Color.BLACK, (stage / 3f) + (stagePercentage / 3f), (stage + 1) + "/3 - " + name, fadeAmount);
                }
                return y + 20;
            }
            public void setStage(int stage) {
                this.stage = stage;
                this.stagePercentage = 0;
            }
        }
    }
    private static List<Task> tasks = new ArrayList();
    public static void addTask(Task task) {
        tasks.add(task);
    }
    public static void removeTask(String id) {
        AtomicReference<Task> task = new AtomicReference();
        tasks.stream().filter(it -> it.id.equals(id)).forEach(task::set);
        tasks.remove(task.get());
    }
    public static Task getTask(String id) {
        return tasks.stream().filter(it -> it.id.equals(id)).findFirst().map(it1 -> it1).orElse(null);
    }
    public static boolean hasTask(String id) {
        return getTask(id) != null;
    }
    public static boolean isEmpty() {
        return tasks.isEmpty();
    }
    public static Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
