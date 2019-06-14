package com.gitlab.indigoa.fabric.informedload.mixin; /**
 * @author Indigo A.
 */

import com.gitlab.indigoa.fabric.informedload.IProgressTracker;
import com.gitlab.indigoa.fabric.informedload.InformedLoad;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenerationProgressTracker.class)
public abstract class MixinWorldGenProgressTracker implements IProgressTracker {
    @Accessor("progressLogger")
    public abstract WorldGenerationProgressLogger getProgressLogger();
    @Accessor("chunkStatuses")
    public abstract Long2ObjectOpenHashMap<ChunkStatus> getChunkStatuses();
}