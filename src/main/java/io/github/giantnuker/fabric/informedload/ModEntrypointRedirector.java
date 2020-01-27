package io.github.giantnuker.fabric.informedload;

import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointRunnable;

import java.io.File;

public class ModEntrypointRedirector implements EntrypointRunnable {
    @Override
    public void run(File runDirectory, Object gameInstance) {
        EntrypointCatcher.NormalOperations.runBegins();
        EntrypointCatcher.instantiateMods(runDirectory, gameInstance);
        Modloader.getInstance(runDirectory).loadExcludedEntrypoints();
        InformedLoadUtils.LOGGER.info("Postponing modloading to wait for the window to open");
    }
}
