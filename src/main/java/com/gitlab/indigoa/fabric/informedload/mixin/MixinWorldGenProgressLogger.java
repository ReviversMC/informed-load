package com.gitlab.indigoa.fabric.informedload.mixin; /**
 * @author Indigo A.
 */

import com.gitlab.indigoa.fabric.informedload.IProgressLogger;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.server.WorldGenerationProgressLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenerationProgressLogger.class)
public abstract class MixinWorldGenProgressLogger implements IProgressLogger {
    @Accessor("totalCount")
    public abstract int getTotalCount();
    @Accessor("generatedCount")
    public abstract int getGeneratedCount();
}