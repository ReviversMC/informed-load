package io.github.giantnuker.fabric.informedload;

import com.google.common.collect.Lists;
import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1.serializer.JanksonConfigSerializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.metadata.EntrypointMetadata;

import java.util.ArrayList;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        InformedLoadUtils.config = AutoConfig.getConfigHolder(Config.class).getConfig();
        if (InformedLoadUtils.config.printEntrypoints) {
            InformedLoadUtils.config.printEntrypoints = false;
            ((ConfigManager)AutoConfig.getConfigHolder(Config.class)).save();
            //Printing entrypoints
            InformedLoadUtils.LOGGER.info("Displaying mod entrypoints:");
            ArrayList<ModContainer> newArrayList = Lists.newArrayList(FabricLoader.INSTANCE.getAllMods());
            for (int i = 0; i < newArrayList.size(); i++) {
                net.fabricmc.loader.api.ModContainer modContainer = newArrayList.get(i);
                ModMetadata metadata = modContainer.getMetadata();
                if (modContainer instanceof net.fabricmc.loader.ModContainer) {
                    net.fabricmc.loader.ModContainer mod = (net.fabricmc.loader.ModContainer) modContainer;
                    boolean hasMain = !mod.getInfo().getEntrypoints("main").isEmpty();
                    boolean hasClient = !mod.getInfo().getEntrypoints("client").isEmpty();
                    if (!hasClient && !hasMain) continue;
                    System.out.print(metadata.getName() + " (" + metadata.getId() + ")\n");
                    //if (hasMain) System.out.print("|-Main Entrypoints\n");
                    for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("main")) {
                        System.out.print("|--" + entrypoint.getValue() + "\n");
                    }
                    //if (hasClient) System.out.print("|-Client Entrypoints\n");
                    for (EntrypointMetadata entrypoint : mod.getInfo().getEntrypoints("client")) {
                        System.out.print("|--" + entrypoint.getValue() + "\n");
                    }
                }
            }
            InformedLoadUtils.LOGGER.info("Done, Shutting down Minecraft.");
            //End
            System.exit(0);
        }
        //InformedLoadUtils.config.entrypointDisplay = false;
        if (InformedLoadUtils.config.entrypointDisplay) {
            EntrypointCatcher.redirectEntrypointHandler("Informed Load", new ModEntrypointRedirector());
        }
    }
}
