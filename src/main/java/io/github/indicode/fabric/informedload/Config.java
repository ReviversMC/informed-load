package io.github.indicode.fabric.informedload;

import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Indigo Amann
 */
@me.sargunvohra.mcmods.autoconfig1.annotation.Config(name = InformedLoadUtils.MODID)
public class Config implements ConfigData {
    public static enum SplitType {
        SPLIT, IN_ORDER, SINGLE
    }
    @Comment("Display entrypoint loading")
    public boolean entrypointDisplay = true;
    @Comment("Entrypoints to exclude from display (Will happen before screen opens)")
    public List<String> excludedEntrypoints = new ArrayList<>();
    @ConfigEntry.Category("splash")
    @Comment("Model add progress bars")
    public SplitType splash_splitProgressBars = SplitType.SPLIT;
    @ConfigEntry.Category("splash")
    @Comment("Use 1 to keep the status on the main bar.")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 3)
    public int splash_maxProgressBarRows = 3;
    @ConfigEntry.Category("splash")
    @Comment("Keep same progress bars even if splash tewakers are installed.")
    public boolean splash_forceVanillaProgressBars = false;
    //@Comment("If you like red, Turn me off")
    //public boolean multicoloredProgressBars = true;
    public static class HateDisplay {
        @Comment("Don't like the new world load thingy?")
        public boolean progressBarDisplay = false;
    }
    public static class LoveDisplay {
        @Comment("Grayscale-ify the chunkload square")
        public boolean simplifyColors = true;
        @Comment("Bigger is better!")
        public boolean bigChunkViewer = true;
        @Comment("Do you like progress bars!!!")
        public boolean showProgressBar = true;
    }
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Category("worldload")
    public LoveDisplay worldload_loveDisplay = new LoveDisplay();
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Category("worldload")
    public HateDisplay worldload_hateDisplay = new HateDisplay();
}
