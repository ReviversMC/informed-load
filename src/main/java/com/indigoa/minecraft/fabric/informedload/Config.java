package com.indigoa.minecraft.fabric.informedload;

import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1.shadowed.blue.endless.jankson.Comment;
import net.minecraft.client.resource.language.I18n;

/**
 * @author Indigo Amann
 */
@me.sargunvohra.mcmods.autoconfig1.annotation.Config(name = InformedLoad.MODID)
public class Config implements ConfigData {
    public static enum SplitType {
        SPLIT, IN_ORDER, SINGLE
    }
    @Comment("Model add progress bars")
    public SplitType splitProgressBars = SplitType.SPLIT;
    @Comment("Use 1 to keep the status on the main bar.")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 3)
    public int maxProgressBarRows = 3;
    //@Comment("If you like red, Turn me off")
    //public boolean multicoloredProgressBars = true;
}
