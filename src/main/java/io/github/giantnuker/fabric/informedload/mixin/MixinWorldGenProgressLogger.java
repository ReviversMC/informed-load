package io.github.giantnuker.fabric.informedload.mixin; /**
 * @author Indigo A.
 */

import io.github.giantnuker.fabric.informedload.IProgressLogger;
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