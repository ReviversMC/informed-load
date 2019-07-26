package com.gitlab.indigoa.fabric.informedload;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

/**
 * @author Indigo Amann
 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    public ModMenuIntegration() {

    }

    @Override
    public String getModId() {
        return "informedload";
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return (Function<Screen, Screen>) screen -> AutoConfig.getConfigScreen(Config.class, screen).get();
    }
}
