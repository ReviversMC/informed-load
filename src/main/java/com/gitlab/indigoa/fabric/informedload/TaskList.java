package com.gitlab.indigoa.fabric.informedload;

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
                if (InformedLoad.config.splash_maxProgressBarRows > 1) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (stage / 3f) + (stagePercentage / 3f), (stage + 1) + "/3 - " + name, fadeAmount, false);
                }
                return y + 20;
            }
            public void setStage(int stage) {
                this.stage = stage;
                this.stagePercentage = 0;
            }
        }
        public static class TaskStitchTextures extends TaskList.Task {
            private int stage = 0;
            private float subPercentage = 0;
            private String extra = "";
            public TaskStitchTextures() {
                super("texstitch", "Stitching Textures");
            }
            public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
                String name = stage == 0 ? "Preparing" : stage == 1 ? "Extracting Frames" : stage == 2 ? "Mipmapping" : stage == 3 ? "Stitching" : "Loading";
                if (InformedLoad.config.splash_maxProgressBarRows > 2) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (stage / 5f) + (subPercentage / 5f), (stage + 1) + "/5 - " + extra + name, fadeAmount, false);
                }
                TaskLoadModels.INSTANCE.stagePercentage = (stage / 5f) + (subPercentage / 5f);
                return y + 20;
            }
            public void stage(int stage) {
                this.stage = stage;
                this.extra = "";
                this.subPercentage = 0;
            }
            public void subPercentage(float subPercentage) {
                this.subPercentage = subPercentage;
            }
            public void setExtra(String extra) {
                this.extra = extra + " - ";
            }
        }
        public static class TaskAddModels extends TaskList.Task {
            public int items = 0, items_o, blocks = 0, blocks_o;
            private static final Color c2 = new Color(200, 198, 0);
            public TaskAddModels(int items, int blocks) {
                super("addmodels", I18n.translate("Adding to Bakery"));
                TaskLoadModels taskLoadModels = TaskLoadModels.INSTANCE;
                taskLoadModels.setStage(0);
                taskLoadModels.stagePercentage = 0;
                this.items_o = items;
                this.blocks_o = blocks;
            }
            @Override
            public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
                if (items > items_o) items = items_o;      // Well do you want people to see 879/877 Items?
                if (blocks > blocks_o) blocks = blocks_o;  // Hasn't happened yet - but just to make sure
                if (InformedLoad.config.splash_maxProgressBarRows > 2) {
                    if (InformedLoad.config.splash_splitProgressBars == Config.SplitType.SPLIT) {
                        InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 - 5, y + 10, (float) blocks / (float) blocks_o, blocks + "/" + blocks_o + " Blocks Added", fadeAmount, false);
                        InformedLoad.makeProgressBar(window_width / 2 + 5, y, window_width / 2 + 150, y + 10, (float) items / (float) items_o, items + "/" + items_o + " Items Added", fadeAmount, false);
                    } else if (InformedLoad.config.splash_splitProgressBars == Config.SplitType.SINGLE) {
                        InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (float) (blocks + items) / (float) (blocks_o + items_o), blocks + "/" + blocks_o + " Blocks Added - " + items + "/" + items_o + " Items Added", fadeAmount, false);
                    } else {
                        if (blocks < blocks_o) {
                            InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (float) blocks / (float) blocks_o, blocks + "/" + blocks_o + " Blocks Added", fadeAmount, false);
                        } else {
                            InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (float) items / (float) items_o, items + "/" + items_o + " Items Added", fadeAmount, false);
                        }
                    }
                }
                TaskLoadModels taskLoadModels = TaskLoadModels.INSTANCE;
                taskLoadModels.stagePercentage = (float) (blocks + items) / (float) (blocks_o + items_o);
                y += 20;
                return y;
            }
            public void item() {
                items++;
            }
            public void block() {
                blocks++;
            }
        }
        public static class TaskBakeModels extends TaskList.Task {
            private int tobake, baked = 0;
            public TaskBakeModels(int models) {
                super("bakemodels", "Baking");
                TaskLoadModels.INSTANCE.setStage(2);
                this.tobake = models;
            }
            @Override
            public int render(int y, int middle_x, int window_width, int window_height, float fadeAmount) {
                if (baked > tobake) baked = tobake; // Hasn't happened yet - but just to make sure
                if (InformedLoad.config.splash_maxProgressBarRows > 2) {
                    InformedLoad.makeProgressBar(window_width / 2 - 150, y, window_width / 2 + 150, y + 10, (float) baked / (float) tobake, baked + "/" + tobake + " Models Baked", fadeAmount, false);
                }
                TaskLoadModels.INSTANCE.stagePercentage = (float) baked / tobake;
                y += 20;
                return y;
            }
            public void bake(int baked) {
                this.baked = baked;
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
        return new ArrayList(tasks).iterator();
    }
}
