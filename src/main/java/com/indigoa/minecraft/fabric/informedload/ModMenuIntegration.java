package com.indigoa.minecraft.fabric.informedload;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Screen;

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
        return InformedLoad.MODID;
    }

    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return new Function<Screen, Screen>() {
            @Override
            public Screen apply(Screen screen) {
                return AutoConfig.getConfigScreen(Config.class, screen).get();
            }
        };
    }
}
