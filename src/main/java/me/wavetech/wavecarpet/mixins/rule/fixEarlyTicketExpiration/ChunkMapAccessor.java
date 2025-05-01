package me.wavetech.wavecarpet.mixins.rule.fixEarlyTicketExpiration;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
	@Invoker("getUpdatingChunkIfPresent")
	ChunkHolder callGetUpdatingChunkIfPresent(long chunkPos);
}
