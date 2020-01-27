package io.github.giantnuker.fabric.informedload;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.world.chunk.ChunkStatus;

/**
 * @author Indigo A.
 */
public interface IProgressTracker {
    WorldGenerationProgressLogger getProgressLogger();
    Long2ObjectOpenHashMap<ChunkStatus> getChunkStatuses();
}
