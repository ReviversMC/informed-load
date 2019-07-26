package com.gitlab.indigoa.fabric.informedload.mixin; /**
 * @author Indigo A.
 */

import com.gitlab.indigoa.fabric.informedload.IProgressTracker;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenerationProgressTracker.class)
public abstract class MixinWorldGenProgressTracker implements IProgressTracker {
    @Accessor("progressLogger")
    public abstract WorldGenerationProgressLogger getProgressLogger();
    @Accessor("chunkStatuses")
    public abstract Long2ObjectOpenHashMap<ChunkStatus> getChunkStatuses();
}